package dev.equo.swt.harness;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import dev.equo.swt.WebFlutterServer;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic Java↔Flutter test harness: boots a <b>real</b> Flutter renderer wired to a live
 * Java↔Flutter comm, so a test can drive the production SWT→bridge→Flutter path all the way down
 * (serialization in both directions, deterministic frame sync) and then read back the rendered
 * widget state for assertions. State read-back uses the dormant {@code evolve.test.query} channel
 * registered by Flutter's {@code main()} (see {@code flutter-lib/lib/test_harness.dart}).
 *
 * <p>The renderer is selectable and works on both <b>desktop</b> and <b>web</b>: by default it serves
 * the Flutter web build in a headless browser; {@code -Dharness.client=native} uses the native engine
 * and {@code -Ddev.equo.swt.mode=chromium} a CEF standalone window. This is the harness for any
 * Java↔Flutter integration test (tag them {@code @Tag("flutter-it")}); {@link BrowserFlutterHarness}
 * extends it with the Browser-widget specifics. {@link dev.equo.swt.bench.BenchBridge} also extends
 * it to reuse the shared bridge plumbing.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * FlutterHarness flutter = new FlutterHarness();
 * flutter.init();                 // wire as the global bridge BEFORE creating widgets
 * Shell shell = new Shell(new Display());
 * Button r1 = new Button(new Composite(shell, SWT.NONE), SWT.RADIO);
 * flutter.show(shell);            // boot renderer for the root, await ClientReady
 * flutter.flush();                // push pending state and wait for the painted frame
 * Map<String,Object> s = flutter.queryState(r1);  // live Flutter state of r1
 * flutter.teardown();
 * }</pre>
 */
public class FlutterHarness extends SwtFlutterBridgeBase {

    private static final boolean WEB = !"native".equals(System.getProperty("harness.client", "web"));
    /** Render the web client in a real CEF standalone window (instead of headless Chrome) so the
     *  popup-window + HTTP-auth events that originate there fire. Implies the WEB client. */
    private static final boolean CHROMIUM = "chromium".equalsIgnoreCase(System.getProperty("dev.equo.swt.mode"));
    protected static final long READY_TIMEOUT_MS = Long.getLong("harness.readyTimeoutMs", 30_000);
    protected static final long QUERY_TIMEOUT_MS = Long.getLong("harness.queryTimeoutMs", 10_000);

    private static final String Q_REQUEST = "evolve.test.query";
    private static final String Q_RESPONSE = "evolve.test.queryResponse";
    private static final String FRAME_SYNC = "evolve.test.frameSync";
    private static final String FRAME_SYNCED = "evolve.test.frameSynced";

    private final Gson gson = new Gson();
    private final AtomicInteger queryGen = new AtomicInteger();
    private final AtomicInteger syncGen = new AtomicInteger();
    private final Map<Integer, CompletableFuture<Map<String, Object>>> pendingQueries = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<Void>> pendingSyncs = new ConcurrentHashMap<>();

    private long context;
    private ChromiumStandaloneLauncher chromiumLauncher;
    private WebFlutterServer webServer;
    private Process browserProc;
    private Path profileDir;
    private boolean shown;
    /**
     * This harness's own comm, created/torn down per boot. {@link FlutterBridge#comm()} returns the
     * JVM-wide {@code desktopComm}, which a second harness in the same JVM would reuse stale and never
     * see ClientReady — so each harness owns its comm. {@link #ownComm()} lets a subclass opt out
     * (BenchBridge keeps the shared comm).
     */
    private CommService comm;

    public FlutterHarness() {
        super(null);
    }

    /**
     * Whether this harness owns a private comm (default) or shares the JVM-wide desktop comm.
     * {@link dev.equo.swt.bench.BenchBridge} overrides this to {@code false} to preserve its original
     * shared-comm behavior.
     */
    protected boolean ownComm() {
        return true;
    }

    @Override
    protected CommService comm() {
        if (!ownComm()) return super.comm();
        if (comm == null) comm = newComm();
        return comm;
    }

    // ---------------- lifecycle ----------------

    /**
     * Wire this harness as the global bridge and register the comm handlers. Call once before any
     * Display/widget is created, paired with {@link #teardown()}.
     */
    public void init() {
        if (!WEB) FlutterLibraryLoader.initialize();
        // Injecting the bridge before any Display is created makes every widget route through the
        // harness, and makes the web Display skip standing up its own server + browser (see
        // SwtFlutterBridgeWeb.initForDisplay) — so we don't double-boot.
        FlutterBridge.set(this);
        Config.forceEquo();
        registerCommHandlers();
    }

    /** Register the generic query + frame-sync handlers. Subclasses may override to add more. */
    protected void registerCommHandlers() {
        // Query-response handler (persistent). The far side replies with the live V*.toJson().
        comm().on(Q_RESPONSE, byte[].class, bytes -> {
            if (bytes == null) return;
            Map<String, Object> resp = parseJson(bytes);
            Integer qid = ((Number) resp.get("queryId")).intValue();
            CompletableFuture<Map<String, Object>> f = pendingQueries.remove(qid);
            if (f != null) f.complete(resp);
        });
        // Post-frame ack: completes the future for the matching syncId once Flutter has built +
        // painted the frame requested by flush() (see test_harness.dart).
        comm().on(FRAME_SYNCED, byte[].class, bytes -> {
            if (bytes == null) return;
            Map<String, Object> m = parseJson(bytes);
            Object sid = m == null ? null : m.get("syncId");
            if (sid == null) return;
            CompletableFuture<Void> f = pendingSyncs.remove(((Number) sid).intValue());
            if (f != null) f.complete(null);
        });
    }

    protected Map<String, Object> parseJson(byte[] bytes) {
        Type t = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), t);
    }

    /** Tear down this harness's renderer, server, and comm. Paired with {@link #init()}. */
    public void teardown() {
        if (browserProc != null) browserProc.destroy();
        if (chromiumLauncher != null) {
            chromiumLauncher.close(false);
            chromiumLauncher = null;
        }
        deleteProfileDir();
        if (webServer != null) webServer.stop();
        if (context != 0) {
            dispose(context);
            context = 0;
        }
        if (comm != null) {
            comm.stop();
            comm = null;
        }
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    // ---------------- public API ----------------

    /** Boot the renderer for {@code root}, then block until Flutter signals ClientReady. */
    public void show(Control root) {
        if (shown) throw new IllegalStateException("show() already called");
        shown = true;
        DartControl impl = (DartControl) root.getImpl();
        long rootId = id(impl);
        String rootName = widgetName(impl);
        onReady(impl, Void.class); // registers the ClientReady handler + dirties the root
        if (WEB) startWebClient(rootId, rootName);
        else context = initializeFlutterWindow(comm().getPort(), 0, rootId, rootName, "", 0, 0);
        awaitClientReady();
        flush();
    }

    /**
     * Push all pending Java-side state to Flutter and block until Flutter has built + painted the
     * resulting frame (a deterministic frame barrier; see test_harness.dart {@code frameSync}).
     */
    public void flush() {
        FlutterBridge.update();
        awaitFrame();
    }

    private void awaitFrame() {
        int sid = syncGen.incrementAndGet();
        CompletableFuture<Void> f = new CompletableFuture<>();
        pendingSyncs.put(sid, f);
        comm().send(FRAME_SYNC, ("{\"syncId\":" + sid + "}").getBytes(StandardCharsets.UTF_8));
        long deadline = System.currentTimeMillis() + QUERY_TIMEOUT_MS;
        while (!f.isDone() && System.currentTimeMillis() < deadline) pump(1);
        pendingSyncs.remove(sid);
    }

    /**
     * Returns the live Flutter state (the widget's {@code V*.toJson()}) of {@code w}, by id. The map
     * contains {@code found} plus the serialized fields under {@code state} (e.g. {@code selection}).
     */
    public Map<String, Object> queryState(Widget w) {
        int qid = queryGen.incrementAndGet();
        CompletableFuture<Map<String, Object>> f = new CompletableFuture<>();
        pendingQueries.put(qid, f);
        String json = "{\"queryId\":" + qid + ",\"targetId\":" + w.hashCode() + "}";
        comm().send(Q_REQUEST, json.getBytes(StandardCharsets.UTF_8));
        return awaitFuture(f, QUERY_TIMEOUT_MS, "query for id " + w.hashCode());
    }

    /** Convenience: the rendered selection of a Check/Radio/Toggle Button (false if absent/null). */
    @SuppressWarnings("unchecked")
    public boolean renderedSelection(Button b) {
        Map<String, Object> resp = queryState(b);
        if (!Boolean.TRUE.equals(resp.get("found"))) return false;
        Map<String, Object> state = (Map<String, Object>) resp.get("state");
        Object sel = state == null ? null : state.get("selection");
        return Boolean.TRUE.equals(sel);
    }

    // ---------------- await / pump ----------------

    /**
     * Advance the renderer's own loop one step (CEF {@code pumpOnce} under chromium-standalone on
     * mac / the native engine / a web yield). A test's spin-wait must call this each iteration.
     */
    public void pumpClient() {
        pump(1);
    }

    private void awaitClientReady() {
        long deadline = System.currentTimeMillis() + READY_TIMEOUT_MS;
        while (!clientReady.isDone() && System.currentTimeMillis() < deadline) pump(1);
        if (!clientReady.isDone())
            throw new IllegalStateException("Flutter client did not become ready within " + READY_TIMEOUT_MS + "ms");
    }

    protected <R> R awaitFuture(CompletableFuture<R> f, long timeoutMs, String what) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!f.isDone() && System.currentTimeMillis() < deadline) pump(1);
        if (!f.isDone())
            throw new RuntimeException("Timed out waiting for " + what + " (" + timeoutMs + "ms)");
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed waiting for " + what, e);
        }
    }

    private void pump(int maxMessages) {
        if (CHROMIUM && chromiumLauncher != null) {
            chromiumLauncher.pump();
        }
        if (WEB) {
            try { Thread.sleep(2); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            pumpMessages(maxMessages);
        }
    }

    // ---------------- web client boot ----------------

    private void startWebClient(long rootId, String rootName) {
        File webDir = resolveWebDir();
        webServer = new WebFlutterServer.Builder()
                .webDirectory(webDir)
                .commPort(comm().getPort())
                .widgetId(rootId)
                .widgetName(rootName)
                .browserCommand("none")
                .serveServiceWorker(false) // deterministic boot: skip the SW install/activate race
                .build();
        try {
            webServer.start();
            String url = webServer.getApplicationUrl();
            System.out.println("[FlutterHarness] web harness at " + url + " serving " + webDir
                    + " (comm port " + comm().getPort() + ", root " + rootName + "/" + rootId + ")");
            if (CHROMIUM) {
                // window.open() from a fixture page usually runs without a user gesture, which
                // Chromium's popup blocker suppresses BEFORE CEF's onBeforePopup sees it. Disable the
                // blocker for the harness only (production keeps it on). The SDK reads chromium.args.
                String extra = "--disable-popup-blocking";
                String existing = System.getProperty("chromium.args", "");
                System.setProperty("chromium.args", existing.isEmpty() ? extra : existing + ";" + extra);
                chromiumLauncher = new ChromiumStandaloneLauncher();
                chromiumLauncher.open(url);
                System.out.println("[FlutterHarness] opened Chromium standalone window");
            } else {
                launchBrowser(url);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to start web harness client", e);
        }
    }

    private void launchBrowser(String url) throws IOException {
        String override = System.getProperty("equo.swt.browser");
        if ("none".equalsIgnoreCase(override)) {
            System.out.println("[FlutterHarness] browser launch disabled — open: " + url);
            return;
        }
        String chrome = chromeBinary(override);
        boolean headless = Boolean.parseBoolean(System.getProperty("harness.web.headless", "true"));
        if (chrome != null) {
            // Unique, throwaway profile per run so Chrome's per-user-data-dir singleton can't hand the
            // URL to an already-running instance, and an unclean exit can't leave a crash-restore flag.
            profileDir = Files.createTempDirectory("equo-harness-profile-");
            List<String> cmd = new ArrayList<>();
            cmd.add(chrome);
            if (headless) cmd.add("--headless=new");
            if (Boolean.getBoolean("harness.web.console")) {
                cmd.add("--enable-logging=stderr");
                cmd.add("--v=1");
            }
            cmd.add("--disable-gpu");
            cmd.add("--no-first-run");
            cmd.add("--no-default-browser-check");
            cmd.add("--hide-crash-restore-bubble");
            cmd.add("--disable-session-crashed-bubble");
            cmd.add("--user-data-dir=" + profileDir);
            cmd.add(url);
            browserProc = new ProcessBuilder(cmd).inheritIO().start();
            System.out.println("[FlutterHarness] launched Chrome (headless=" + headless + ") at " + url
                    + " profile " + profileDir);
            return;
        }
        throw new IOException("Chrome/Chromium not found; set -Dequo.swt.browser=<path> (or -Dharness.client=native)");
    }

    /** Locate the Chrome/Chromium binary, honoring {@code -Dequo.swt.browser}; null if none found. */
    protected static String chromeBinary(String override) {
        if (override != null && !override.isEmpty() && new File(override).canExecute()) return override;
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] candidates = os.contains("mac")
                ? new String[]{"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                               "/Applications/Chromium.app/Contents/MacOS/Chromium"}
                : os.contains("win")
                ? new String[]{System.getenv("ProgramFiles") + "\\Google\\Chrome\\Application\\chrome.exe"}
                : new String[]{"/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser"};
        for (String c : candidates) if (c != null && new File(c).canExecute()) return c;
        return null;
    }

    /** Best-effort: wait for Chrome to exit (so it releases the profile), then recursively delete it. */
    private void deleteProfileDir() {
        if (profileDir == null) return;
        try {
            if (browserProc != null) browserProc.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
            try (var paths = Files.walk(profileDir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        } catch (Exception ignored) {
            // temp dir; the OS will reap it if we couldn't.
        } finally {
            profileDir = null;
        }
    }

    /** Locate {@code flutter-lib/build/web} relative to the test working dir. */
    protected static File resolveWebDir() {
        for (String p : new String[]{"../flutter-lib/build/web", "flutter-lib/build/web"}) {
            File f = new File(p);
            if (f.isDirectory()) return f.getAbsoluteFile();
        }
        return new File("../flutter-lib/build/web").getAbsoluteFile();
    }

    // ---------------- unused SwtFlutterBridgeBase overrides ----------------

    /** No-op: creating Shells must not boot a native window — {@link #show} boots the client. */
    @Override public void initFlutterView(Composite parent, DartControl control) { }
    @Override protected long getHandle(Control control) { return 0; }
    @Override protected void setHandle(DartControl control, long view) { }
    @Override public void destroy(DartWidget control) { }
    @Override protected void destroyHandle(DartControl dartControl) { }
    @Override public Object container(DartComposite parent) { return null; }
    @Override public void reparent(DartControl control, Composite newParent) { }
    @Override protected void sendSwtEvolveProperties() { }
}

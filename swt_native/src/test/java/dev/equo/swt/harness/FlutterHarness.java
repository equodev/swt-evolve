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
 * JUnit 5 extension that boots a <b>real</b> Flutter renderer (web browser or the native engine)
 * wired to a live Java↔Flutter comm, so a test can drive the production SWT→bridge→Flutter path all
 * the way down and then read back the rendered widget state for assertions.
 *
 * <p>Boot/transport plumbing is adapted from {@link dev.equo.swt.bench.BenchBridge}. The state
 * read-back uses the dormant {@code evolve.test.query} channel registered by Flutter's
 * {@code main()} (see {@code flutter-lib/lib/test_harness.dart}).
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @RegisterExtension static Mocks mocks = Mocks.withNativeBridge(); // mocked Display/Shell, real bridge
 * @RegisterExtension static FlutterHarness flutter = new FlutterHarness();
 *
 * Composite group = new Composite(Mocks.swtShell(), SWT.NONE);
 * Button r1 = new Button(group, SWT.RADIO);
 * flutter.show(group);        // boot renderer for the root, await ClientReady
 * ... drive the widgets ...
 * flutter.flush();            // push pending state to Flutter and let it paint
 * Map<String,Object> s = flutter.queryState(r1);  // live Flutter state of r1
 * assertThat(s.get("selection")).isEqualTo(true);
 * }</pre>
 *
 * <p>Requires the Flutter web build ({@code flutter-lib/build/web}) for web mode (the default), and
 * Chrome/Chromium on the machine. Run native instead with {@code -Dharness.client=native}.
 * Tag tests with {@code @Tag("flutter-it")}; that tag is excluded from the default test run.
 */
public class FlutterHarness extends SwtFlutterBridgeBase {

    private static final boolean WEB = !"native".equals(System.getProperty("harness.client", "web"));
    /** Render the web client in a real CEF standalone window (instead of headless Chrome) so the
     *  popup-window + HTTP-auth events that originate there fire. Implies the WEB client. */
    private static final boolean CHROMIUM = "chromium".equalsIgnoreCase(System.getProperty("dev.equo.swt.mode"));
    private static final long READY_TIMEOUT_MS = Long.getLong("harness.readyTimeoutMs", 15_000);
    private static final long QUERY_TIMEOUT_MS = Long.getLong("harness.queryTimeoutMs", 3_000);

    private static final String Q_REQUEST  = "evolve.test.query";
    private static final String Q_RESPONSE = "evolve.test.queryResponse";

    private static final String IFRAME_LOADED = "evolve.test.iframeLoaded";

    private static final String FRAME_SYNC = "evolve.test.frameSync";
    private static final String FRAME_SYNCED = "evolve.test.frameSynced";

    private final Gson gson = new Gson();
    private final AtomicInteger queryGen = new AtomicInteger();
    private final AtomicInteger syncGen = new AtomicInteger();
    private final Map<Integer, CompletableFuture<Map<String, Object>>> pendingQueries = new ConcurrentHashMap<>();
    private final Map<Integer, CompletableFuture<Void>> pendingSyncs = new ConcurrentHashMap<>();
    /** Marks (page titles) of embedded pages that posted their on-load ping. */
    private final java.util.List<String> iframeLoads = new java.util.concurrent.CopyOnWriteArrayList<>();

    private long context;
    private ChromiumStandaloneLauncher chromiumLauncher;
    private WebFlutterServer webServer;
    private Process browserProc;
    private Path profileDir;
    private boolean shown;
    /**
     * This harness's own comm, created/torn down per boot. {@link FlutterBridge#comm()} returns the
     * JVM-wide {@code desktopComm}, which a second harness in the same JVM (another
     * {@code *BrowserFlutterTest} subclass) would reuse stale and never see ClientReady — so each
     * harness owns its comm to stay self-contained.
     */
    private CommService comm;

    public FlutterHarness() {
        super(null);
    }

    @Override
    protected CommService comm() {
        if (comm == null) comm = newComm();
        return comm;
    }

    // ---------------- lifecycle ----------------

    /**
     * Wire this harness as the global bridge and register the comm handlers. Called once per
     * parameterized-class invocation (a fresh harness per (kit, proxy) variant), paired with
     * {@link #teardown()}.
     */
    public void init() {
        if (!WEB) FlutterLibraryLoader.initialize();
        // Injecting the bridge before any Display is created makes every widget route through the
        // harness, and makes the web Display skip standing up its own server + browser (see
        // SwtFlutterBridgeWeb.initForDisplay) — so we don't double-boot.
        FlutterBridge.set(this);
        Config.forceEquo();

        // Query-response handler (persistent). The far side replies with the live V*.toJson().
        comm().on(Q_RESPONSE, byte[].class, bytes -> {
            if (bytes == null) return;
            Type t = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> resp = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), t);
            Integer qid = ((Number) resp.get("queryId")).intValue();
            CompletableFuture<Map<String, Object>> f = pendingQueries.remove(qid);
            if (f != null) f.complete(resp);
        });

        // On-load pings from embedded pages (posted by the page itself; see test_harness_iframe_web.dart).
        comm().on(IFRAME_LOADED, byte[].class, bytes -> {
            if (bytes == null) return;
            Type t = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> m = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), t);
            Object page = m == null ? null : m.get("page");
            if (page != null) iframeLoads.add(String.valueOf(page));
        });

        // Post-frame ack: completes the future for the matching syncId once Flutter has built +
        // painted the frame requested by flush() (see test_harness.dart).
        comm().on(FRAME_SYNCED, byte[].class, bytes -> {
            if (bytes == null) return;
            Type t = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> m = gson.fromJson(new String(bytes, StandardCharsets.UTF_8), t);
            Object sid = m == null ? null : m.get("syncId");
            if (sid == null) return;
            CompletableFuture<Void> f = pendingSyncs.remove(((Number) sid).intValue());
            if (f != null) f.complete(null);
        });
    }

    /** Tear down this variant's renderer, server, and comm. Paired with {@link #init()}. */
    public void teardown() {
        if (browserProc != null) browserProc.destroy();
        if (chromiumLauncher != null) { chromiumLauncher.close(false); chromiumLauncher = null; }
        deleteProfileDir();
        if (webServer != null) webServer.stop();
        if (this.context != 0) dispose(this.context);
        // Stop this harness's own comm so the next harness in the JVM starts clean.
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
     * resulting frame. Deterministic: instead of pumping a fixed number of times, we send a frame
     * barrier and wait for Flutter's post-frame ack (see test_harness.dart {@code frameSync}). On the
     * rare timeout (e.g. before ClientReady) it degrades to a short pump rather than failing.
     */
    public void flush() {
        FlutterBridge.update();
        awaitFrame();
    }

    /** Send a frame barrier and wait for Flutter's post-frame ack. */
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

    /** Forget prior on-load pings, so a following {@link #awaitIframeRendered} only sees fresh ones. */
    public void clearIframeLoads() {
        iframeLoads.clear();
    }

    /**
     * Awaits the on-load ping an embedded page posts once it actually renders (its
     * {@code document.title} contains {@code expectedTitle}). Returns {@code false} on timeout — the
     * page never rendered (e.g. a cross-origin {@code <iframe>} blocked by COOP/COEP or
     * X-Frame-Options). The only reliable web signal: {@code onPageFinished} fires even for a blocked
     * load and {@code contentDocument} is unreadable cross-origin.
     */
    public boolean awaitIframeRendered(String expectedTitle, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (true) {
            for (String p : iframeLoads) if (p.contains(expectedTitle)) return true;
            if (System.currentTimeMillis() >= deadline) return false;
            pump(1);
        }
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

    private void awaitClientReady() {
        long deadline = System.currentTimeMillis() + READY_TIMEOUT_MS;
        while (!clientReady.isDone() && System.currentTimeMillis() < deadline) pump(1);
        if (!clientReady.isDone())
            throw new IllegalStateException("Flutter client did not become ready within " + READY_TIMEOUT_MS + "ms");
    }

    private <R> R awaitFuture(CompletableFuture<R> f, long timeoutMs, String what) {
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

    /**
     * Advance the renderer's own loop one step (CEF {@code pumpOnce} under chromium-standalone on
     * mac / the native engine / a web yield). A test's spin-wait must call this each iteration —
     * otherwise, in chromium mode the CEF message loop only runs inside {@link #flush}/{@link
     * #queryState}, so a page navigated during a {@code pumpUntil} wait never finishes loading.
     */
    public void pumpClient() {
        pump(1);
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

    // ---------------- web client boot (adapted from BenchBridge) ----------------

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
            int httpPort = webServer.start();
            String url = webServer.getApplicationUrl();
            System.out.println("[FlutterHarness] web harness at " + url + " serving " + webDir
                    + " (comm port " + comm().getPort() + ", root " + rootName + "/" + rootId + ")");
            if (CHROMIUM) {
                // window.open() from a fixture page usually runs without a user gesture (e.g. a
                // setTimeout), which Chromium's popup blocker suppresses BEFORE CEF's onBeforePopup
                // sees it — so the OpenWindowListener would never fire and the popup tests couldn't
                // run. Disable the blocker for the harness only (this is a test concern; production
                // must keep the blocker on). The chromium SDK reads chromium.args directly.
                String extra = "--disable-popup-blocking";
                String existing = System.getProperty("chromium.args", "");
                System.setProperty("chromium.args", existing.isEmpty() ? extra : existing + ";" + extra);
                // Real CEF standalone window (the production chromium-mode path) — its window/auth
                // handlers are what feed OpenWindow/CloseWindow/VisibilityWindow/Authentication.
                chromiumLauncher = new ChromiumStandaloneLauncher();
                chromiumLauncher.open(webServer.getApplicationUrl());
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
            // Unique, throwaway profile per run. Sharing one profile lets Chrome's per-user-data-dir
            // singleton hand the URL to an already-running instance (so a headless launch surfaces as
            // a tab in your real Chrome), and an unclean exit leaves a "restore pages" crash flag.
            profileDir = Files.createTempDirectory("equo-harness-profile-");
            List<String> cmd = new ArrayList<>();
            cmd.add(chrome);
            if (headless) cmd.add("--headless=new");
            // -Dharness.web.console=true forwards the page console to stderr (captured via
            // inheritIO), so Dart print()/errors from the web client are visible for debugging.
            if (Boolean.getBoolean("harness.web.console")) {
                cmd.add("--enable-logging=stderr");
                cmd.add("--v=1");
            }
            cmd.add("--disable-gpu");
            cmd.add("--no-first-run");
            cmd.add("--no-default-browser-check");
            cmd.add("--hide-crash-restore-bubble");      // suppress the "restore pages?" bubble
            cmd.add("--disable-session-crashed-bubble"); // (older flag, same intent)
            cmd.add("--user-data-dir=" + profileDir);
            cmd.add(url);
            browserProc = new ProcessBuilder(cmd).inheritIO().start();
            System.out.println("[FlutterHarness] launched Chrome (headless=" + headless + ") at " + url
                    + " profile " + profileDir);
            return;
        }
        throw new IOException("Chrome/Chromium not found; set -Dequo.swt.browser=<path> (or run -Dharness.client=native)");
    }

    private static String chromeBinary(String override) {
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

    private static File resolveWebDir() {
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

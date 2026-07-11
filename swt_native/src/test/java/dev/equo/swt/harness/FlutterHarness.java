package dev.equo.swt.harness;

import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.FlutterLibraryLoader;
import dev.equo.swt.HeadlessChrome;
import dev.equo.swt.WebFlutterServer;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Boot/comm-only Java↔Flutter test harness: boots a <b>real</b> Flutter renderer wired to a live
 * Java↔Flutter comm (headless-Chrome + {@link WebFlutterServer} on web, the native engine on
 * desktop, or a CEF standalone window), and exposes the primitives ({@link #startWebClient},
 * {@link #initNativeRenderer}, {@link #awaitClientReadyResilient}, {@link #teardown}) needed to
 * drive that boot sequence. It does not assume a real widget tree — subclasses that need one
 * (running a widget snippet and reading back its rendered state) should extend
 * {@link WidgetFlutterHarness} instead; subclasses that only need to boot a renderer and speak a
 * custom request/response protocol over the comm (e.g. a size-measurement bridge) extend this
 * class directly.
 *
 * <p>The renderer is selectable and works on both <b>desktop</b> and <b>web</b>: by default it serves
 * the Flutter web build in a headless browser; {@code -Dharness.client=native} uses the native engine
 * and {@code -Ddev.equo.swt.mode=chromium} a CEF standalone window.
 *
 * <p>Known subclasses: {@link WidgetFlutterHarness} (widget-tree tests), {@code GenericSizeBridge} /
 * {@code FontMeasureBridge} (size-measurement bridges with no widget tree), and
 * {@link dev.equo.swt.bench.BenchBridge} (reuses the shared bridge plumbing).
 */
public abstract class FlutterHarness extends EmbeddedBridge {

    protected static final boolean WEB = !"native".equals(System.getProperty("harness.client", "web"));
    /** Render the web client in a real CEF standalone window (instead of headless Chrome) so the
     *  popup-window + HTTP-auth events that originate there fire. Implies the WEB client. */
    private static final boolean CHROMIUM = ConfigFlags.isChromiumMode();
    protected static final long READY_TIMEOUT_MS = Long.getLong("harness.readyTimeoutMs", 30_000);
    protected static final long QUERY_TIMEOUT_MS = Long.getLong("harness.queryTimeoutMs", 10_000);

    private long context;
    private ChromiumStandaloneLauncher chromiumLauncher;
    private WebFlutterServer webServer;
    /** The headless Chrome we launched (see {@link #launchBrowser}); reaped in {@link #teardown}. */
    private HeadlessChrome harnessChrome;
    /** Boot-resilience self-test knob: number of upcoming browser launches to force to fail (load a
     *  blank page that never connects), so {@link #awaitClientReadyResilient}'s relaunch path is
     *  exercised deterministically. 0 (default) in CI/production. */
    private int bootFailuresToInject = Integer.getInteger("harness.web.failBoots", 0);
    /**
     * This harness's own comm, created/torn down per boot. {@link FlutterBridge#comm()} returns the
     * JVM-wide {@code desktopComm}, which a second harness in the same JVM would reuse stale and never
     * see ClientReady — so each harness owns its comm. {@link #ownComm()} lets a subclass opt out
     * (BenchBridge keeps the shared comm).
     */
    private CommService comm;

    protected FlutterHarness() {
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

    /** Tear down this harness's renderer, server, and comm. */
    public void teardown() {
        // Reaps the launched Chrome — its whole process tree (zygote/GPU/renderer helpers a plain
        // destroy() would orphan) — and deletes its throwaway profile.
        if (harnessChrome != null) {
            harnessChrome.close();
            harnessChrome = null;
        }
        if (chromiumLauncher != null) {
            chromiumLauncher.close(false);
            chromiumLauncher = null;
        }
        if (webServer != null) webServer.stop();
        if (context != 0) {
            dev.equo.swt.FlutterNative.dispose(context);
            context = 0;
        }
        if (comm != null) {
            comm.stop();
            comm = null;
        }
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    /** Boots the native (desktop) Flutter engine for {@code rootId}/{@code rootName}. Exposed
     *  (alongside {@link #startWebClient}) so subclasses without a real widget tree — e.g. a
     *  measurement bridge — can drive the same boot sequence a widget-tree harness uses, instead of
     *  reimplementing it. */
    protected void initNativeRenderer(long rootId, String rootName) {
        context = dev.equo.swt.FlutterNative.initialize(comm().getPort(), 0, rootId, rootName, "", 0, 0, 0, 0);
    }

    /** Initializes the native Flutter libraries (desktop client only) and wires this harness as the
     *  active {@link FlutterBridge}. Every subclass's boot sequence starts with this, before
     *  creating any Display/widget. */
    protected void registerBridge() {
        if (!WEB) FlutterLibraryLoader.initialize();
        FlutterBridge.set(this);
    }

    /** Boots the renderer (web or native, per {@code -Dharness.client}) for {@code rootId}/
     *  {@code rootName}, then blocks until Flutter reports ClientReady. Shared boot sequence for
     *  every subclass, whether it drives a real widget tree or a bare comm channel. */
    protected void bootRenderer(long rootId, String rootName) {
        if (WEB) startWebClient(rootId, rootName);
        else initNativeRenderer(rootId, rootName);
        awaitClientReadyResilient();
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

    /**
     * Await ClientReady, but for the headless-web client relaunch a fresh Chrome if it hasn't
     * reported ready within {@code harness.bootAttemptMs}. Headless Chrome intermittently wedges
     * during startup in the CI container — it hangs right after its dbus/GCM/optimization-guide init
     * and never loads the Flutter app, so the comm never even opens and the whole boot times out.
     * A one-off hang clears on a clean relaunch (the other boots in the same run prove it's
     * intermittent), so we kill the wedged Chrome and try again. This is boot resilience, not a
     * longer timeout: each attempt is short and we make a few. Only the headless path can relaunch;
     * native / Chromium-standalone / browser-disabled fall back to the single {@link #awaitClientReady}.
     */
    protected void awaitClientReadyResilient() {
        boolean canRelaunch = WEB && !CHROMIUM
                && !"none".equalsIgnoreCase(System.getProperty("equo.swt.browser", ""));
        if (!canRelaunch) {
            awaitClientReady();
            return;
        }
        int attempts = Integer.getInteger("harness.bootAttempts", 3);
        long perAttemptMs = Long.getLong("harness.bootAttemptMs", 12_000);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            if (awaitReadyFor(perAttemptMs)) return;
            if (attempt < attempts) {
                System.out.println("[FlutterHarness] Flutter client not ready in " + perAttemptMs
                        + "ms (attempt " + attempt + "/" + attempts + "); relaunching Chrome");
                relaunchBrowser();
            }
        }
        if (!clientReady.isDone())
            throw new IllegalStateException("Flutter client did not become ready after " + attempts
                    + " boot attempts of " + perAttemptMs + "ms each");
    }

    /** Pump the loop until ClientReady arrives or {@code ms} elapses; returns whether it is ready. */
    private boolean awaitReadyFor(long ms) {
        long deadline = System.currentTimeMillis() + ms;
        while (!clientReady.isDone() && System.currentTimeMillis() < deadline) pump(1);
        return clientReady.isDone();
    }

    /** Kill a wedged Chrome and launch a fresh one against the still-running server + comm. */
    private void relaunchBrowser() {
        if (harnessChrome != null) {
            harnessChrome.close();
            harnessChrome = null;
        }
        try {
            launchBrowser(webServer.getApplicationUrl());
        } catch (IOException e) {
            throw new RuntimeException("Failed to relaunch web harness client", e);
        }
    }

    /** Spin the pump loop until {@code f} completes or {@code timeoutMs} elapses. Does not throw
     *  on timeout; callers that need a hard failure should check {@code f.isDone()} themselves
     *  (see {@link #awaitFuture}) or decide their own fallback. */
    protected void awaitQuiet(CompletableFuture<?> f, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!f.isDone() && System.currentTimeMillis() < deadline) pump(1);
    }

    protected <R> R awaitFuture(CompletableFuture<R> f, long timeoutMs, String what) {
        awaitQuiet(f, timeoutMs);
        if (!f.isDone())
            throw new RuntimeException("Timed out waiting for " + what + " (" + timeoutMs + "ms)");
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed waiting for " + what, e);
        }
    }

    void pump(int maxMessages) {
        if (CHROMIUM && chromiumLauncher != null) {
            chromiumLauncher.pump();
        }
        if (WEB) {
            try { Thread.sleep(2); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } else {
            dev.equo.swt.FlutterNative.pumpMessages(maxMessages);
        }
    }

    // ---------------- web client boot ----------------

    protected void startWebClient(long rootId, String rootName) {
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
        String chrome = HeadlessChrome.resolveBinary(override);
        if (chrome == null) {
            throw new IOException("Chrome/Chromium not found; set -Dequo.swt.browser=<path> (or -Dharness.client=native)");
        }
        boolean headless = Boolean.parseBoolean(System.getProperty("harness.web.headless", "true"));
        boolean console = Boolean.getBoolean("harness.web.console");
        // Boot-resilience self-test: when harness.web.failBoots>0, the first N launches load a blank
        // page that never connects, forcing awaitClientReadyResilient() to relaunch — so the retry path
        // can be exercised deterministically (off by default).
        String openUrl = url;
        if (bootFailuresToInject > 0) {
            bootFailuresToInject--;
            openUrl = "data:text/html,harness-forced-boot-failure";
            System.out.println("[FlutterHarness] injecting boot failure (blank page); "
                    + bootFailuresToInject + " more to inject");
        }
        // The harness inherits Chrome's stdio so -Dharness.web.console output is visible.
        harnessChrome = HeadlessChrome.launch(chrome, openUrl, headless, console, HeadlessChrome.Io.INHERIT);
        System.out.println("[FlutterHarness] launched Chrome (headless=" + headless + ") at " + openUrl);
    }

    /** Locate {@code flutter-lib/build/web} relative to the test working dir. */
    protected static File resolveWebDir() {
        for (String p : new String[]{"../flutter-lib/build/web", "flutter-lib/build/web"}) {
            File f = new File(p);
            if (f.isDirectory()) return f.getAbsoluteFile();
        }
        return new File("../flutter-lib/build/web").getAbsoluteFile();
    }

    // ---------------- unused EmbeddedBridge overrides ----------------

    /** No-op: creating Shells must not boot a native window — a widget-tree subclass's {@code show}
     *  boots the client. */
    @Override public void initFlutterView(Composite parent, DartControl control) { }
    @Override protected long getHandle(Control control) { return 0; }
    @Override protected void setHandle(DartControl control, long view) { }
    @Override public void destroy(DartWidget control) { }
    @Override protected void destroyHandle(DartControl dartControl) { }
    @Override public Object container(DartComposite parent) { return null; }
    @Override public void reparent(DartControl control, Composite newParent) { }
    @Override protected void sendSwtEvolveProperties() { }
}

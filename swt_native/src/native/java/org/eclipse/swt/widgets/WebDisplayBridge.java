package org.eclipse.swt.widgets;

import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.Java8;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.WebFlutterServer;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.spi.FlutterBridgeSpi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Web surface for a Display-level bridge: the whole Dart-backed SWT tree is rendered by a Flutter
 * web app in a browser (or an Equo Chromium standalone window) talking to a {@link WebFlutterServer}
 * over this Display's comm. All the Display-level plumbing lives in {@link DisplayBridge}; this class
 * only adds the browser/Chromium transport (incl. the FlutterBridgeSpi lookups and CSD window
 * controls). The per-OS native init (GTK/OLE) is done once via {@link DisplayBridgePlatform}, whose concrete
 * lives in each web-OS source set (no-op for the pure-web build).
 */
public class WebDisplayBridge extends DisplayBridge {

    private WebFlutterServer webServer;
    private ChromiumStandaloneLauncher chromiumLauncher;
    /** Dev-only `flutter run` process for the -PdartDebug web introspection path (null in production). */
    private Process flutterRunProc;
    /** CSD maximize strategy: "bounds" (default), "native", or "fullscreen". */
    private String csdMaxStrategy = "bounds";

    /**
     * Grace period for a browser tab teardown ({@code WinUnload}) before the SWT shells are actually
     * closed. A refresh (F5) and a real tab close are indistinguishable at {@code pagehide}/
     * {@code beforeunload} time, so instead of closing immediately we wait this long: if the page was
     * merely refreshing, the reloaded client reconnects and re-sends {@code ClientReady} within the
     * window (see {@link #onDisplayClientReady}) and the close is cancelled; a genuine close lets the
     * timer elapse and tears the shells down. Tuned to comfortably cover a Flutter-web reboot; override
     * with {@code -Ddev.equo.swt.web.refreshGraceMs} (0 disables deferral — close immediately again).
     */
    private static final long REFRESH_GRACE_MS = Long.getLong("dev.equo.swt.web.refreshGraceMs", 3000);

    /** Single daemon thread that fires deferred tab-closes; created lazily, shut down with the Display. */
    private ScheduledExecutorService closeScheduler;
    /** The currently-armed deferred close (a pending {@code WinUnload}), or null. Guarded by {@code this}. */
    private ScheduledFuture<?> pendingClose;

    boolean isChromium = ConfigFlags.isChromiumMode();

    WebDisplayBridge(DartDisplay display) {
        super(display);
        DisplayBridgePlatform.init();
    }

    /**
     * SPI implementation backing {@link FlutterBridgeSpi#getWebServerUrl(Object)}.
     * Returns null when the display is null, isn't a web surface, or has no web server yet.
     */
    static String lookupWebServerUrl(Object displayObj) {
        if (!(displayObj instanceof Display)) return null;
        Display display = (Display) displayObj;
        if (!(display.getImpl() instanceof DartDisplay)) return null;
        DartDisplay dartDisplay = (DartDisplay) display.getImpl();
        if (!(dartDisplay.displayBridge instanceof WebDisplayBridge)) return null;
        WebDisplayBridge bridge = (WebDisplayBridge) dartDisplay.displayBridge;
        if (bridge.webServer == null) return null;
        return bridge.webServer.getApplicationUrl();
    }

    /**
     * SPI implementation backing {@link FlutterBridgeSpi#getCommPort(Object)}.
     * Resolves the Display's own per-session comm port so an embedding host's WS proxy dials the
     * right comm. Returns -1 when unavailable.
     */
    static int lookupCommPort(Object displayObj) {
        if (!(displayObj instanceof Display)) return -1;
        Display display = (Display) displayObj;
        if (!(display.getImpl() instanceof DartDisplay)) return -1;
        DartDisplay dartDisplay = (DartDisplay) display.getImpl();
        if (!(dartDisplay.displayBridge instanceof WebDisplayBridge)) return -1;
        CommService c = ((WebDisplayBridge) dartDisplay.displayBridge).comm();
        return c != null ? c.getPort() : -1;
    }

    @Override
    protected void start(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        CommService comm = comm();

        registerDisplayClientReady(display);
        registerDisplayKeyEvents(display);
        registerWindowControls(display);

        int port = comm.getPort();

        // Dev/introspection path (Phase 2): serve the app via a live `flutter run -d chrome` instead
        // of static WebFlutterServer, so the Dart VM Service is available for DTD/MCP + flutter_driver.
        // The app connects back to this comm port via --dart-define. Static serving + the Browser-widget
        // endpoints (/proxy, /equo-browser-function) are skipped in this mode.
        if (isDartDebug()) {
            launchFlutterRunDev(port, displayId, "Display");
            FlutterBridgeSpi.registerWebServerUrlLookup(WebDisplayBridge::lookupWebServerUrl);
            FlutterBridgeSpi.registerCommPortLookup(WebDisplayBridge::lookupCommPort);
            FlutterBridgeSpi.notifyDisplayCreated(display.getApi());
            return;
        }

        WebFlutterServer.Builder serverBuilder = new WebFlutterServer.Builder()
                .commPort(port)
                .widgetId(displayId)
                .widgetName("Display");
        // Optional override of the served web build directory, so a combined/host app can serve its
        // own Flutter web build (with any extension hooks installed) instead of the one extracted from
        // Evolve's jar. Set via -Ddev.equo.swt.web.dir=<absolute dir>; otherwise, in a packaged product,
        // discover the external web bundle owner (EWT) via the ExternalWebBundleProvider SPI (mirrors
        // the desktop ExternalBundleProvider path). Standalone Evolve finds no provider.
        String webDirOverride = System.getProperty("dev.equo.swt.web.dir");
        if (webDirOverride == null || webDirOverride.trim().isEmpty()) {
            webDirOverride = dev.equo.swt.FlutterLibraryLoader.firstExternalWebDir(
                    java.util.ServiceLoader.load(dev.equo.swt.ExternalWebBundleProvider.class));
        }
        if (webDirOverride != null && !webDirOverride.trim().isEmpty()) {
            serverBuilder.webDirectory(new java.io.File(webDirOverride));
        }
        webServer = serverBuilder.build();
        try {
            webServer.start();
            if (isChromium) {
                try {
                    chromiumLauncher = new ChromiumStandaloneLauncher();
                    chromiumLauncher.setOnWindowClosed(this::onClientWindowClosed);
                    chromiumLauncher.open(webServer.getApplicationUrl());
                } catch (LinkageError e) {
                    // Chromium bundle not wired to this classloader (OSGi optional import not
                    // satisfied, or API version mismatch). Fall back to the system browser.
                    System.err.println("[WebDisplayBridge] Chromium unavailable (" + e.getMessage() + "), falling back to system browser: ");
                    e.printStackTrace();
                    webServer.launchBrowser();
                }
            } else {
                webServer.launchBrowser();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start web server for Display", e);
        }

        FlutterBridgeSpi.registerWebServerUrlLookup(WebDisplayBridge::lookupWebServerUrl);
        FlutterBridgeSpi.registerCommPortLookup(WebDisplayBridge::lookupCommPort);
        FlutterBridgeSpi.notifyDisplayCreated(display.getApi());
    }

    /**
     * Wires the window-control channels the Flutter client sends instead of calling {@code window.equo.*}
     * directly. The Client-Side-Decoration buttons (minimize/maximize/restore) and — crucially — the
     * tab/window close ({@code WinClose}) all arrive here: on mac the native maximize/zoom and close
     * spin a nested run loop while the single-threaded message pump is blocked, freezing the window;
     * minimize and a plain setWindowBounds don't, so we "maximize" by sizing the window to the monitor
     * work area (like a resize) and close via the existing shell teardown.
     *
     * <p>Two distinct close signals arrive here. {@code WinClose} is an <em>explicit</em> close (the CSD
     * close button) and tears the shells down immediately. {@code WinUnload} is the browser tab/window
     * teardown the Dart client sends on pagehide/beforeunload — which fires for a refresh just as it does
     * for a real close, so it is deferred by a grace period rather than closing at once (see
     * {@link #scheduleDeferredClose} / {@link #onDisplayClientReady}).</p>
     */
    protected void registerWindowControls(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        CommService comm = comm();
        String win = "Display/" + displayId + "/";
        Display winApi = display.getApi();
        comm.on(win + "WinMinimize", String.class, s -> winApi.asyncExec(() -> {
            if (chromiumLauncher != null) chromiumLauncher.minimizeWindow();
        }));
        csdMaxStrategy = System.getProperty("dev.equo.swt.csd.maximize", "direct");
        comm.on(win + "WinMaximize", Rectangle.class, rect -> applyCsdMaximize(winApi, rect, true));
        comm.on(win + "WinRestore", Rectangle.class, rect -> applyCsdMaximize(winApi, rect, false));
        comm.on(win + "WinClose", String.class, s -> onClientWindowClosed());
        comm.on(win + "WinUnload", String.class, s -> scheduleDeferredClose());
        // A browser refused the window (popup blocker, or the user's browser opened nothing). The
        // shell has to go back to being drawn inside this one, or it would exist nowhere at all.
        // Read as a plain Object: that reader is registered for every comm, whereas a payload class
        // of our own needs one generated for it, and without it this never arrives at all.
        comm.on(win + "WindowOpenFailed", Object.class, payload -> {
            long shellId = shellIdOf(payload);
            if (shellId != 0) winApi.asyncExec(() -> onShellWindowRejected(shellId));
        });
    }

    // ---- one browser window per detached shell (see WindowPolicy) ---------------------------------

    /** The {@code shellId} in a window payload, or 0 when it carries none. Package-private so a test
     *  can put a really-decoded payload through it. */
    static long shellIdOf(Object payload) {
        if (payload instanceof java.util.Map<?, ?>) {
            Object shellId = ((java.util.Map<?, ?>) payload).get("shellId");
            if (shellId instanceof Number) return ((Number) shellId).longValue();
        }
        return 0;
    }

    /** Shells whose window the browser refused, so the policy is not asked about them again. */
    private final java.util.Set<Long> rejectedShellWindows =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * Only with a served application: opening a second window means pointing a browser at a URL, and
     * under the {@code flutter run} dev path there is no {@code WebFlutterServer} to name one.
     */
    @Override
    protected boolean supportsShellWindows() {
        return webServer != null;
    }

    @Override
    protected boolean ownsWindow(Shell shell) {
        if (shell != null && rejectedShellWindows.contains((long) shell.hashCode())) return false;
        return super.ownsWindow(shell);
    }

    @Override
    protected ShellWindow createShellWindow(Shell shell) {
        Rectangle bounds = shell.getBounds();
        String url = shellWindowUrl(webServer.getApplicationUrl(), shell.hashCode(),
                effectiveTheme(), isTestSemanticsEnabled());
        WebShellWindow window = new WebShellWindow(shell);
        window.command("OpenWindow", Java8.map(
                "shellId", (long) shell.hashCode(),
                "url", url,
                "x", bounds.x,
                "y", bounds.y,
                "width", bounds.width > 0 ? bounds.width : 640,
                "height", bounds.height > 0 ? bounds.height : 480,
                "title", shell.getText() == null ? "" : shell.getText()));
        // The window the user closes is the one that tells us; the shell has no other way to hear it.
        comm().on("Shell/" + shell.hashCode() + "/WinUnload", String.class,
                s -> closeShellFromWindow(shell));
        return window;
    }

    /**
     * The address a detached shell's window is opened at: the same application, rooted at that shell
     * rather than at the Display.
     *
     * <p>The theme is carried in the URL as well as being pushed over the comm, because the client
     * paints its first frames before anything arrives on the socket. Without it the window opens
     * light and then corrects itself, which against a dark application reads as a broken window
     * rather than as a flash.
     */
    static String shellWindowUrl(String baseUrl, long shellId, String theme, boolean testSemantics) {
        String url = baseUrl + "/?widgetName=Shell&widgetId=" + shellId + "&theme=" + theme;
        if (testSemantics) url += "&enableTestSemantics=true";
        return url;
    }

    /** Whether the served page is asked to build the semantics tree (the E2E runtime toggle). */
    private static boolean isTestSemanticsEnabled() {
        return Boolean.getBoolean("dev.equo.swt.web.enableTestSemantics");
    }

    /** The theme a new window should open in: what the application forces, else the system's. */
    private static String effectiveTheme() {
        String forced = dev.equo.swt.Config.getConfigFlags().force_theme;
        if (forced != null) {
            String normalized = forced.trim().toLowerCase();
            if ("dark".equals(normalized) || "light".equals(normalized)) return normalized;
        }
        return Display.isSystemDarkTheme() ? "dark" : "light";
    }

    /** The browser would not open the window: remember it, and re-push so the shell is drawn inline. */
    private void onShellWindowRejected(long shellId) {
        if (!rejectedShellWindows.add(shellId)) return;
        System.err.println("[WebDisplayBridge] the browser refused a window for Shell/" + shellId
                + " (popup blocked?); drawing it inside the main window instead");
        if (forDisplay != null && !forDisplay.getApi().isDisposed()) sendDisplayUpdate(forDisplay);
    }

    /** The user closed a detached shell's browser window. */
    private void closeShellFromWindow(Shell shell) {
        Display api = forDisplay == null ? null : forDisplay.getApi();
        if (api == null || api.isDisposed()) return;
        api.asyncExec(() -> {
            if (api.isDisposed() || shell.isDisposed()) return;
            shell.close();
            // The window is gone either way, so a vetoed close would strand the shell unreachable.
            if (!shell.isDisposed()) shell.dispose();
        });
    }

    /** A detached shell's browser window, driven entirely by commands to the client that opened it. */
    private class WebShellWindow implements ShellWindow {
        private final Shell shell;
        private boolean open = true;

        WebShellWindow(Shell shell) {
            this.shell = shell;
        }

        void command(String name, java.util.Map<String, Object> payload) {
            if (forDisplay == null) return;
            try {
                serializeAndSend("Display/" + forDisplay.getApi().hashCode() + "/" + name, payload);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isAlive() {
            return open;
        }

        @Override
        public void setTitle(String title) {
            command("WindowTitle", Java8.map("shellId", (long) shell.hashCode(), "title", title));
        }

        @Override
        public void setBounds(Rectangle bounds) {
            command("WindowBounds", Java8.map(
                    "shellId", (long) shell.hashCode(),
                    "x", bounds.x, "y", bounds.y,
                    "width", bounds.width, "height", bounds.height));
        }

        @Override
        public void setState(int state) {
            command("WindowState", Java8.map(
                    "shellId", (long) shell.hashCode(), "state", state));
        }

        @Override
        public void setVisible(boolean visible) {
            // A browser window cannot be hidden and brought back: the platform has no such gesture,
            // and a tab that is closed is gone. Left showing, which is the lesser wrong -- the shell
            // it renders is hidden, so the window is empty rather than stale.
            if (visible) command("WindowState", Java8.map(
                    "shellId", (long) shell.hashCode(), "state", ShellWindow.STATE_NORMAL));
        }

        @Override
        public void close() {
            if (!open) return;
            open = false;
            comm().remove("Shell/" + shell.hashCode() + "/WinUnload");
            command("CloseWindow", Java8.map("shellId", (long) shell.hashCode()));
        }
    }

    /** Whether the Phase 2 dev/introspection path is active (serve via `flutter run`, VM Service on). */
    private static boolean isDartDebug() {
        return Boolean.getBoolean("dev.equo.swt.dartDebug");
    }

    /**
     * Dev-only: spawn `flutter run -d chrome` to serve the web app with a live Dart VM Service, passing
     * this Display's comm port + identity as --dart-define values (web_platform.dart reads them). flutter
     * run opens Chrome and prints the VM Service URI to stdout (inheritIO). Requires the flutter-lib path
     * and flutter command via system properties (set by the :examples runWebExample task under -PdartDebug).
     * Falls back to the static browser if the flutter-lib dir wasn't provided.
     *
     * <p>{@code dev.equo.swt.dartDriver=true} additionally targets {@code lib/main_driver.dart} instead
     * of the default {@code lib/main.dart} — that entrypoint calls {@code enableFlutterDriverExtension()}
     * before delegating to the real app, so {@code ext.flutter.driver} is live on the VM Service and a
     * repro can be scripted with {@code docs/design/dtd-helpers/driver_cmd.dart} (tap/scroll/enter_text/
     * get_text by finder — no coordinates). {@code main.dart} itself never imports {@code main_driver.dart},
     * so this never affects a production build; only the dev/introspection path can even reach it.
     */
    private void launchFlutterRunDev(int commPort, long widgetId, String widgetName) {
        String flutterLibDir = System.getProperty("dev.equo.swt.flutterLibDir");
        String flutterCmd = System.getProperty("dev.equo.swt.flutterCmd", "flutter");
        if (flutterLibDir == null || flutterLibDir.trim().isEmpty()) {
            System.err.println("[WebDisplayBridge] dartDebug set but dev.equo.swt.flutterLibDir is missing; "
                    + "cannot launch `flutter run`. Run via :examples:runWebExample -PdartDebug.");
            return;
        }
        boolean dartDriver = Boolean.getBoolean("dev.equo.swt.dartDriver");
        java.util.List<String> cmd = new java.util.ArrayList<>();
        for (String tok : flutterCmd.trim().split("\\s+"))
            if (!tok.trim().isEmpty()) cmd.add(tok);
        cmd.add("run");
        if (dartDriver) {
            cmd.add("-t");
            cmd.add("lib/main_driver.dart");
        }
        cmd.add("-d");
        cmd.add("chrome");
        cmd.add("--dart-define=equo.comm_port=" + commPort);
        cmd.add("--dart-define=equo.widget_id=" + widgetId);
        cmd.add("--dart-define=equo.widget_name=" + widgetName);
        try {
            flutterRunProc = new ProcessBuilder(cmd)
                    .directory(new java.io.File(flutterLibDir))
                    .inheritIO()
                    .start();
            System.out.println("[WebDisplayBridge] dartDebug: launched `" + String.join(" ", cmd)
                    + "` in " + flutterLibDir + " (comm port " + commPort + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch `flutter run` for dartDebug", e);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (chromiumLauncher != null)
            chromiumLauncher.pump();
    }

    /**
     * Releases the Display's wake permit so an off-thread dirty unparks {@code DartDisplay.sleep()}
     * promptly instead of waiting for the safety-net cap. See {@link FlutterBridge#wakeForDirty()}.
     */
    @Override
    protected void wakeForDirty() {
        DartDisplay display = forDisplay;
        if (display != null)
            display.wakeThread();
    }

    /**
     * Whether the event loop must keep ticking rather than parking indefinitely in {@code sleep()}.
     * True only while a CEF standalone window is open: its message loop is pull-driven from
     * {@link #onUpdate()} ({@code chromiumLauncher.pump()}), so {@code sleep()} caps the wait at 16ms
     * (~60fps). Pure web mode is fully event-driven, so it parks until woken.
     */
    @Override
    public boolean needsPump() {
        return chromiumLauncher != null;
    }

    /**
     * The Chromium standalone window (the real OS window) was closed by the user. Dispose the SWT
     * side to match, so snippet event loops and the Eclipse workbench shut down cleanly instead of
     * spinning with no visible window. Fired from the CEF thread (or the SWT thread during pump()
     * on mac), so marshal onto the UI thread with asyncExec and close the top-level shells.
     */
    /**
     * A browser tab/window teardown ({@code WinUnload}) arrived — ambiguous between a refresh and a real
     * close. Arm a deferred close after {@link #REFRESH_GRACE_MS}; if the page was refreshing, the
     * reloaded client reconnects and {@link #onDisplayClientReady} cancels this before it fires. Any
     * previously-armed close is replaced. With the grace set to 0 (or a Chromium standalone window,
     * where the real OS-window close is the authority) the close is immediate.
     */
    private synchronized void scheduleDeferredClose() {
        if (REFRESH_GRACE_MS <= 0 || hasNativeWindow()) {
            onClientWindowClosed();
            return;
        }
        if (closeScheduler == null) {
            closeScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "equo-web-close-grace");
                t.setDaemon(true);
                return t;
            });
        }
        if (pendingClose != null)
            pendingClose.cancel(false);
        pendingClose = closeScheduler.schedule(this::onClientWindowClosed, REFRESH_GRACE_MS, TimeUnit.MILLISECONDS);
    }

    /** Cancels a pending deferred close, if any — the tab was refreshing, not closing. */
    private synchronized void cancelDeferredClose() {
        if (pendingClose != null) {
            pendingClose.cancel(false);
            pendingClose = null;
        }
    }

    /**
     * A refreshed browser client has reconnected and re-sent ClientReady within the grace window: it was
     * a refresh, not a close, so cancel the deferred tab-close armed by {@link #scheduleDeferredClose}.
     */
    @Override
    protected void onDisplayClientReady(boolean first) {
        cancelDeferredClose();
        // On refresh, a fresh client reconnects with the widget-tree state but not the one-shot GC
        // paints Java already fired — re-fire them so custom-drawn content isn't blank.
        if (!first) {
            refirePaintsForFreshClient();
        }
    }

    /** Re-fire SWT.Paint for every custom-painted control in the tree, on the Display thread. */
    private void refirePaintsForFreshClient() {
        DartDisplay display = forDisplay;
        if (display == null) return;
        Display api = display.getApi();
        if (api == null || api.isDisposed()) return;
        api.asyncExec(() -> {
            if (api.isDisposed()) return;
            for (Shell shell : api.getShells()) {
                refirePaints(shell);
            }
        });
    }

    private void refirePaints(Control c) {
        if (c == null || c.isDisposed()) return;
        // Only paint-listening controls emit GC ops; skip the rest to avoid needless GC churn.
        if (c.isListening(SWT.Paint) && c.getImpl() instanceof DartControl) {
            ControlHelper.paint((DartControl) c.getImpl());
        }
        if (c instanceof Composite) {
            for (Control child : ((Composite) c).getChildren()) {
                refirePaints(child);
            }
        }
    }

    private void onClientWindowClosed() {
        DartDisplay display = forDisplay;
        if (display == null)
            return;
        Display api = display.getApi();
        if (api == null || api.isDisposed())
            return;
        api.asyncExec(() -> {
            if (api.isDisposed())
                return;
            for (Shell shell : display._shells()) {
                if (shell != null && !shell.isDisposed() && shell.getParent() == null) {
                    shell.close();
                }
            }
        });
    }

    @Override
    public void destroyDisplay() {
        cancelDeferredClose();
        if (closeScheduler != null) {
            closeScheduler.shutdownNow();
            closeScheduler = null;
        }
        if (chromiumLauncher != null) {
            chromiumLauncher.close(true);
            chromiumLauncher = null;
        }
        if (flutterRunProc != null) {
            flutterRunProc.destroy();
            flutterRunProc = null;
        }
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        super.destroyDisplay();
    }

    /**
     * True when window operations on this control should drive the hosting Chromium standalone
     * window: only under Equo Chromium standalone mode, and only for the main top-level shell.
     */
    private boolean isChromiumWindow(DartControl control) {
        return hasNativeWindow()
                && control instanceof DartShell
                && isMainShell(forDisplay, (Shell) ((DartShell) control).getApi());
    }

    /** Whether a real OS window (Chromium standalone) is hosting this Display (false in tests / pure web). */
    protected boolean hasNativeWindow() {
        return isChromium && chromiumLauncher != null;
    }

    /** Pushes a genuine, user-/app-driven geometry to the Chromium window. Seam for tests to observe. */
    protected void forwardWindowBounds(Rectangle bounds) {
        chromiumLauncher.setWindowBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * The geometry last pushed to (or already matching) the Chromium window. An embedding app can
     * call {@code Control.setLocation(x, y)} on the main shell with the x/y it already has (e.g.
     * while centering an unrelated secondary dialog), which reconstructs a full, unchanged
     * Rectangle. That's a no-op on a native OS window, but a raw CEF setWindowBounds isn't
     * guaranteed to be — so skip re-issuing it when nothing actually changed.
     */
    private Rectangle lastWindowBounds;

    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
        if (!isChromiumWindow(control) || bounds == null)
            return;
        // The main shell is slaved to the Chromium viewport: applyClientViewport()/setVisible() resize
        // it whenever Chromium echoes a new size. Forwarding those back to the OS window fights the
        // window manager (the resize loop), so only genuine app-driven geometry reaches the window.
        if (applyingClientBounds) {
            lastWindowBounds = bounds;
            return;
        }
        if (bounds.equals(lastWindowBounds))
            return; // no-op: the window already has exactly this geometry
        Shell shell = (Shell) ((DartShell) control).getApi();
        if (shell.getMaximized() || shell.getFullScreen())
            return;
        lastWindowBounds = bounds;
        forwardWindowBounds(bounds);
    }

    /** Applies a CSD maximize/restore. "bounds" sets the Dart-computed window rect (reliable,
     *  non-animating); "native"/"fullscreen" use the host ops (which can freeze the mac pump). */
    private void applyCsdMaximize(Display winApi, Rectangle rect, boolean maximize) {
        winApi.asyncExec(() -> {
            if (chromiumLauncher == null) return;
            switch (csdMaxStrategy) {
                case "native":
                    if (maximize) chromiumLauncher.maximizeWindow();
                    else chromiumLauncher.restoreWindow();
                    break;
                case "fullscreen":
                    chromiumLauncher.setFullscreen(maximize);
                    break;
                default: // bounds
                    if (rect != null && rect.width > 0 && rect.height > 0)
                        chromiumLauncher.setWindowBounds(rect.x, rect.y, rect.width, rect.height);
            }
        });
    }

    @Override
    public void setWindowMaximized(DartControl control, boolean maximized) {
        if (!isChromiumWindow(control))
            return;
        if (maximized)
            chromiumLauncher.maximizeWindow();
        else
            chromiumLauncher.restoreWindow();
    }

    @Override
    public void setWindowMinimized(DartControl control, boolean minimized) {
        if (!isChromiumWindow(control))
            return;
        if (minimized)
            chromiumLauncher.minimizeWindow();
        else
            chromiumLauncher.restoreWindow();
    }

    @Override
    public void setWindowFullScreen(DartControl control, boolean fullScreen) {
        if (isChromiumWindow(control))
            chromiumLauncher.setFullscreen(fullScreen);
    }

    @Override
    public void setWindowTitle(DartControl control, String title) {
        if (isChromiumWindow(control))
            chromiumLauncher.setWindowTitle(title);
    }
}

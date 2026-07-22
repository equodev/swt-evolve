package org.eclipse.swt.widgets;

import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.WebFlutterServer;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.spi.FlutterBridgeSpi;
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
        if (!(displayObj instanceof Display display)) return null;
        if (!(display.getImpl() instanceof DartDisplay dartDisplay)) return null;
        if (!(dartDisplay.displayBridge instanceof WebDisplayBridge bridge)) return null;
        if (bridge.webServer == null) return null;
        return bridge.webServer.getApplicationUrl();
    }

    /**
     * SPI implementation backing {@link FlutterBridgeSpi#getCommPort(Object)}.
     * Resolves the Display's own per-session comm port so an embedding host's WS proxy dials the
     * right comm. Returns -1 when unavailable.
     */
    static int lookupCommPort(Object displayObj) {
        if (!(displayObj instanceof Display display)) return -1;
        if (!(display.getImpl() instanceof DartDisplay dartDisplay)) return -1;
        if (!(dartDisplay.displayBridge instanceof WebDisplayBridge bridge)) return -1;
        CommService c = bridge.comm();
        return c != null ? c.getPort() : -1;
    }

    @Override
    protected void start(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        CommService comm = comm();

        registerDisplayClientReady(display);
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

        webServer = new WebFlutterServer.Builder()
                .commPort(port)
                .widgetId(displayId)
                .widgetName("Display")
                .build();
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
     */
    private void launchFlutterRunDev(int commPort, long widgetId, String widgetName) {
        String flutterLibDir = System.getProperty("dev.equo.swt.flutterLibDir");
        String flutterCmd = System.getProperty("dev.equo.swt.flutterCmd", "flutter");
        if (flutterLibDir == null || flutterLibDir.isBlank()) {
            System.err.println("[WebDisplayBridge] dartDebug set but dev.equo.swt.flutterLibDir is missing; "
                    + "cannot launch `flutter run`. Run via :examples:runWebExample -PdartDebug.");
            return;
        }
        java.util.List<String> cmd = new java.util.ArrayList<>();
        for (String tok : flutterCmd.trim().split("\\s+"))
            if (!tok.isBlank()) cmd.add(tok);
        cmd.add("run");
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
                && control instanceof DartShell dartShell
                && shouldTrackDisplayBounds((Shell) dartShell.getApi());
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

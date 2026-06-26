package org.eclipse.swt.widgets;

import dev.equo.swt.ChromiumStandaloneLauncher;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WebFlutterServer;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.WindowBridge;
import dev.equo.swt.spi.FlutterBridgeSpi;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class SwtFlutterBridgeWeb extends FlutterBridge implements WindowBridge {
    private DartDisplay forDisplay;
    private DartControl focused = null;
    private WebFlutterServer webServer;
    /** One comm (and one web server) per Display, unlike desktop's shared static comm. */
    private CommService comm;
    private ChromiumStandaloneLauncher chromiumLauncher;
    /** CSD maximize strategy: "bounds" (default), "native", or "fullscreen". */
    private String csdMaxStrategy = "bounds";

    boolean isChromium = "chromium".equalsIgnoreCase(System.getProperty("dev.equo.swt.mode"));

    SwtFlutterBridgeWeb(DartDisplay display) {
        this.forDisplay = display;
        System.setProperty("dev.equo.swt.desktop", "false");
    }

    @Override
    protected CommService comm() {
        if (comm == null) {
            comm = newComm();
        }
        return comm;
    }

    /**
     * Wake this Display's UI thread when a widget/resource is marked dirty, so a parked {@code sleep()}
     * returns and flushes the dirty set to Dart promptly. A dirty on the UI thread releases a permit
     * the same thread's next {@code sleep()} drains, so it's free there; an off-thread dirty unparks
     * the loop. See {@link FlutterBridge#wakeForDirty()}.
     */
    @Override
    protected void wakeForDirty() {
        DartDisplay display = forDisplay;
        if (display != null)
            display.wakeThread();
    }

    /**
     * Whether the event loop must keep iterating rather than parking indefinitely in {@code sleep()}.
     * True only while a CEF standalone window is open: its message loop is pull-driven from
     * {@link #onUpdate()} ({@code chromiumLauncher.pump()}), so the loop has to tick to stay
     * responsive — {@code sleep()} caps its wait at 16ms (~60fps) in that case. In pure web mode
     * (websocket comm) everything is event-driven — every wake source releases the Display's wake
     * permit — so {@code sleep()} can block until woken.
     */
    public boolean needsPump() {
        return chromiumLauncher != null;
    }

    /**
     * Compatibility constructor required so that {@link SwtFlutterBridgeBase#of(DartWidget)}
     * compiles when the web class is on the classpath. This constructor is not
     * called at runtime in the web context.
     */
    SwtFlutterBridgeWeb(DartWidget widget) {
        this.forDisplay = null;
    }

    /**
     * Creates and starts a bridge for the given Display. Called once during Display.init().
     */
    public static SwtFlutterBridgeWeb initForDisplay(DartDisplay display) {
        // A test/bench harness may inject its own bridge (FlutterBridge.set) before the Display is
        // created; it then owns the comm, HTTP server and browser for a specific root, and every
        // widget already routes through it (see FlutterBridge.of). Creating a per-Display bridge here
        // would double-boot a second server + browser (the latter popping a tab in the dev's real
        // browser). Skip it; leaving displayBridge null is safe — all its uses are null-guarded.
        if (FlutterBridge.injected() != null) return null;
        SwtFlutterBridgeWeb bridge = new SwtFlutterBridge(display);
        display.setBridge(bridge);
        bridge.startWebServer(display);
        FlutterBridgeSpi.registerWebServerUrlLookup(SwtFlutterBridgeWeb::lookupWebServerUrl);
        FlutterBridgeSpi.registerCommPortLookup(SwtFlutterBridgeWeb::lookupCommPort);
        FlutterBridgeSpi.notifyDisplayCreated(display.getApi());
        return bridge;
    }

    /**
     * SPI implementation backing {@link FlutterBridgeSpi#getWebServerUrl(Object)}.
     * Returns null when the display is null, has no web impl, or no web server yet.
     */
    static String lookupWebServerUrl(Object displayObj) {
        if (!(displayObj instanceof Display display)) return null;
        if (!(display.getImpl() instanceof DartDisplay dartDisplay)) return null;
        SwtFlutterBridgeWeb bridge = dartDisplay.displayBridge;
        if (bridge == null || bridge.webServer == null) return null;
        return bridge.webServer.getApplicationUrl();
    }

    /**
     * SPI implementation backing {@link FlutterBridgeSpi#getCommPort(Object)}.
     * Resolves the Display's own per-session comm port so an embedding host's WS
     * proxy dials the right comm. Returns -1 when unavailable. This is the
     * Display-aware path {@code FlutterBridge.commFor(Object)} cannot provide from
     * the shared module (it can't reach the web-only display bridge).
     */
    static int lookupCommPort(Object displayObj) {
        if (!(displayObj instanceof Display display)) return -1;
        if (!(display.getImpl() instanceof DartDisplay dartDisplay)) return -1;
        SwtFlutterBridgeWeb bridge = dartDisplay.displayBridge;
        if (bridge == null) return -1;
        CommService c = bridge.comm();
        return c != null ? c.getPort() : -1;
    }

    /**
     * Returns the display-level bridge for the given widget's display.
     * Called from DartWidget.register() for every control.
     * Also registers top-level shells so the display can track them.
     */
    public static SwtFlutterBridgeWeb of(DartWidget widget) {
        if (widget instanceof DartControl dartControl) {
            Display display = dartControl._display();
            if (display != null) {
                DartDisplay dartDisplay = (DartDisplay) display.getImpl();
                if (widget instanceof DartShell dartShell) {
                    dartDisplay.addShell((Shell) dartShell.getApi());
                    dartShell.bounds = dartDisplay.bounds;
                }
                return dartDisplay.displayBridge;
            }
        }
        return null;
    }

    /**
     * Register a raw string-channel handler on this Display's comm. Used by widget-less surfaces
     * (Dialog/MessageBox) that have no bridge of their own but belong to a Display.
     */
    public void onChannel(String channel, java.util.function.Consumer<String> cb) {
        comm().on(channel, String.class, cb);
    }

    /** Remove a handler registered via {@link #onChannel}. */
    public void offChannel(String channel) {
        comm().remove(channel);
    }

    private void startWebServer(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        CommService comm = comm();

        comm.on("Display/" + displayId + "/ClientReady", ClientReadyPayload.class, p -> {
            if (p == null) return;

            boolean[] boundsChanged = {false};
            display.getApi().syncExec(() -> {
                Rectangle nextBounds = new Rectangle(0, 0, p.width, p.height);
                if (p.isFirst) {
                    display.bounds = nextBounds;
                    boundsChanged[0] = true;
                } else if (!display.bounds.equals(nextBounds)) {
                    resizeMainShells(display, display.bounds, nextBounds);
                    display.bounds = nextBounds;
                    boundsChanged[0] = true;
                }
            });

            if (!boundsChanged[0]) return;

            if (p.isFirst) {
                clientReady.complete(true);
                sendSwtEvolveProperties();
            }

            sendDisplayUpdate(display);
        });

        // Client-Side-Decoration window controls. The Flutter overlay sends these instead of
        // calling window.equo.* directly: on mac the native maximize/zoom and close spin a
        // nested run loop while the single-threaded message pump is blocked, freezing the
        // window. Minimize and a plain setWindowBounds don't, so we "maximize" by sizing the
        // window to the monitor work area (like a resize) and close via the existing teardown.
        String win = "Display/" + displayId + "/";
        Display winApi = display.getApi();
        comm.on(win + "WinMinimize", String.class, s -> winApi.asyncExec(() -> {
            if (chromiumLauncher != null) chromiumLauncher.minimizeWindow();
        }));
        // Maximize strategy. "bounds" (default) applies the window rect computed on the Dart
        // side (which has the real screen + window geometry — the Java DartDisplay monitor is
        // zero-sized) via setWindowBounds; it doesn't animate, so it doesn't freeze the mac
        // pump. "native"/"fullscreen" use the host window ops (which freeze on mac).
        //   -Ddev.equo.swt.csd.maximize=bounds|native|fullscreen
        csdMaxStrategy = System.getProperty("dev.equo.swt.csd.maximize", "direct");
        comm.on(win + "WinMaximize", Rectangle.class, rect -> applyCsdMaximize(winApi, rect, true));
        comm.on(win + "WinRestore", Rectangle.class, rect -> applyCsdMaximize(winApi, rect, false));
        comm.on(win + "WinClose", String.class, s -> onChromiumWindowClosed());

        int port = comm.getPort();
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
                    chromiumLauncher.setOnWindowClosed(this::onChromiumWindowClosed);
                    chromiumLauncher.open(webServer.getApplicationUrl());
                } catch (LinkageError e) {
                    // Chromium bundle not wired to this classloader (OSGi optional import not
                    // satisfied, or API version mismatch). Fall back to the system browser.
                    System.err.println("[SwtFlutterBridge] Chromium unavailable (" + e.getMessage() + "), falling back to system browser: ");
                    e.printStackTrace();
                    webServer.launchBrowser();
                }
            } else {
                webServer.launchBrowser();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start web server for Display", e);
        }
    }

    public void sendDisplayUpdate(DartDisplay display) {
        if (!clientReady.isDone()) return;
        try {
            VDisplay vd = VDisplay.of(display);
            serializeAndSend("Display/" + vd.id, vd);
            FlutterBridge.displayBootstrapped = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpdate() {
        if (chromiumLauncher != null)
            chromiumLauncher.pump();
    }

    /**
     * The Chromium standalone window (the real OS window) was closed by the user. Dispose the SWT
     * side to match, so snippet event loops and the Eclipse workbench shut down cleanly instead of
     * spinning with no visible window. Fired from the CEF thread (or the SWT thread during pump()
     * on mac), so marshal onto the UI thread with asyncExec and close the top-level shells —
     * shell.close() runs SWT.Close listeners / workbench shutdown rather than a hard dispose.
     */
    private void onChromiumWindowClosed() {
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

    private void resizeMainShells(DartDisplay display, Rectangle previousBounds, Rectangle nextBounds) {
        for (Shell shell : display._shells()) {
            if (shell == null || shell.isDisposed()) {
                continue;
            }
            if (!shouldTrackDisplayBounds(shell)) {
                continue;
            }
            Rectangle shellBounds = shell.getBounds();
            boolean tracksDisplayBounds =
                    shellBounds != null
                            && shellBounds.x == 0
                            && shellBounds.y == 0
                            && shellBounds.width == previousBounds.width
                            && shellBounds.height == previousBounds.height;
            boolean isLikelyWorkbenchShell =
                    shellBounds != null
                            && shellBounds.x == 0
                            && shellBounds.y == 0
                            && shellBounds.width > 0
                            && shellBounds.height > 0;
            if (tracksDisplayBounds || isLikelyWorkbenchShell || shell.getMaximized() || shell.getFullScreen()) {
                shell.setBounds(0, 0, nextBounds.width, nextBounds.height);
            }
        }
    }

    private boolean shouldTrackDisplayBounds(Shell shell) {
        if (shell.getParent() != null) {
            return false;
        }
        String text = shell.getText();
        if (text != null && text.contains("limbo")) {
            return false;
        }
        int style = shell.getStyle();
        int modalMask = org.eclipse.swt.SWT.PRIMARY_MODAL
                | org.eclipse.swt.SWT.APPLICATION_MODAL
                | org.eclipse.swt.SWT.SYSTEM_MODAL;
        if ((style & modalMask) != 0) {
            return false;
        }
        return (style & org.eclipse.swt.SWT.TOOL) == 0;
    }

    public String registerBrowserHtml(long browserId, String html) {
        return "";
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
        // Bridge is initialized at Display level in initForDisplay().
    }

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartShell dartShell && forDisplay != null) {
            forDisplay.removeShell((Shell) dartShell.getApi());
            sendDisplayUpdate(forDisplay);
        }
    }

    public void destroyDisplay() {
        if (chromiumLauncher != null) {
            chromiumLauncher.close(true);
            chromiumLauncher = null;
        }
        if (webServer != null) {
            webServer.stop();
            webServer = null;
        }
        if (comm != null) {
            comm.stop();
            comm = null;
        }
    }

    @Override
    public Object container(DartComposite parent) {
        return null;
    }

    @Override
    public boolean setFocus(DartControl widget) {
        focused = widget;
        return true;
    }

    @Override
    public boolean hasFocus(DartControl widget) {
        return widget == focused;
    }

    @Override
    public void setVisible(DartControl control, boolean visible) {
        if (control instanceof DartShell dartShell) {
            Shell shell = (Shell) dartShell.getApi();
            if (visible && forDisplay != null) {
                if (shouldTrackDisplayBounds(shell)) {
                    Rectangle displayBounds = forDisplay.bounds;
                    Rectangle shellBounds = shell.getBounds();
                    if (shellBounds != null
                            && shellBounds.x == 0
                            && shellBounds.y == 0
                            && shellBounds.width < displayBounds.width
                            && shellBounds.height < displayBounds.height) {
                        shell.setBounds(0, 0, displayBounds.width, displayBounds.height);
                    }
                }
            }
            if (visible) {
                shell.layout(true, true);
            }
            sendDisplayUpdate(forDisplay);
        }
    }

    @Override
    public void reparent(DartControl control, Composite parent) {
    }

    @Override
    public void setZOrder(DartControl control, Control sibling, boolean above) {
    }

    /**
     * True when window operations on this control should drive the hosting Chromium
     * standalone window: only under Equo Chromium standalone mode, and only for the
     * main top-level shell (the single primary shell, never dialogs/tool/modal shells).
     */
    private boolean isChromiumWindow(DartControl control) {
        return isChromium
                && chromiumLauncher != null
                && control instanceof DartShell dartShell
                && shouldTrackDisplayBounds((Shell) dartShell.getApi());
    }

    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
        if (!isChromiumWindow(control) || bounds == null)
            return;
        Shell shell = (Shell) ((DartShell) control).getApi();
        // The main shell is slaved to the Chromium viewport: resizeMainShells()/setVisible()
        // push (0,0,displayW,displayH) whenever Chromium echoes a new size. Forwarding those
        // back to the OS window fights the window manager and makes the window drift, so we
        // only forward genuine user-driven geometry. Skip while maximized/fullscreen (those
        // states own the window geometry) and skip the viewport-fill case.
        if (shell.getMaximized() || shell.getFullScreen())
            return;
        Rectangle displayBounds = forDisplay != null ? forDisplay.bounds : null;
        if (displayBounds != null
                && bounds.x == 0 && bounds.y == 0
                && bounds.width == displayBounds.width
                && bounds.height == displayBounds.height)
            return;
        chromiumLauncher.setWindowBounds(bounds.x, bounds.y, bounds.width, bounds.height);
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

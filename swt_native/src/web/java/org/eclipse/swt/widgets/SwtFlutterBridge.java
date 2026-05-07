package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WebFlutterServer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class SwtFlutterBridge extends FlutterBridge {
    private DartDisplay forDisplay;
    private DartControl focused = null;
    private WebFlutterServer webServer;

    public SwtFlutterBridge(DartDisplay display) {
        this.forDisplay = display;
        System.setProperty("dev.equo.swt.desktop", "false");
    }

    /**
     * Compatibility constructor required so that {@link SwtFlutterBridgeBase#of(DartWidget)}
     * compiles when the web class is on the classpath. This constructor is not
     * called at runtime in the web context.
     */
    SwtFlutterBridge(DartWidget widget) {
        this.forDisplay = null;
    }

    /**
     * Creates and starts a bridge for the given Display. Called once during Display.init().
     */
    public static SwtFlutterBridge initForDisplay(DartDisplay display) {
        SwtFlutterBridge bridge = new SwtFlutterBridge(display);
        display.setBridge(bridge);
        bridge.startWebServer(display);
        return bridge;
    }

    /**
     * Returns the display-level bridge for the given widget's display.
     * Called from DartWidget.register() for every control.
     * Also registers top-level shells so the display can track them.
     */
    public static SwtFlutterBridge of(DartWidget widget) {
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

    private void startWebServer(DartDisplay display) {
        long displayId = display.getApi().hashCode();

        client.getComm().on("Display/" + displayId + "/ClientReady", Point.class, p -> {
            if (p != null) {
                Point size = p;
                Display d = display.getApi();
                d.syncExec(() -> {
                    Rectangle previousBounds = display.bounds;
                    Rectangle nextBounds = new Rectangle(0, 0, size.x, size.y);
                    if (!previousBounds.equals(nextBounds)) {
                        display.bounds = nextBounds;
                        resizeMainShells(display, previousBounds, nextBounds);
                    }
                });
            }

            if (!clientReady.isDone()) {
                clientReady.complete(true);
                sendSwtEvolveProperties();
            }

            sendDisplayUpdate(display);
        });

        int port = client.getPort();
        webServer = new WebFlutterServer.Builder()
                .commPort(port)
                .widgetId(displayId)
                .widgetName("Display")
                .build();
        try {
            webServer.start();
            webServer.launchBrowser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start web server for Display", e);
        }
    }

    public void sendDisplayUpdate(DartDisplay display) {
        if (!clientReady.isDone()) return;
        try {
            VDisplay vd = VDisplay.of(display);
            serializeAndSend("Display/" + vd.id, vd);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (webServer != null) {
            webServer.stop();
            webServer = null;
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
}

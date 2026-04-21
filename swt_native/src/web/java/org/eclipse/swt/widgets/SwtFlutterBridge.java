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
     * compiles when the web class is on the classpath.  This constructor is <em>not</em>
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
                }
                return dartDisplay.displayBridge;
            }
        }
        return null;
    }

    private void startWebServer(DartDisplay display) {
        long displayId = display.getApi().hashCode();

        // Listen for Display/{id}/ClientReady from the Flutter app
        client.getComm().on("Display/" + displayId + "/ClientReady", Point.class, p -> {
            if (!clientReady.isDone()) {
                System.out.println("Display ClientReady: " + displayId);
                clientReady.complete(true);
                sendSwtEvolveProperties();
                // Send initial display state (shells) to Flutter
                sendDisplayUpdate(display);
                // Inform the display of the reported window size
                if (p != null) {
                    Point size = p;
                    Display d = display.getApi();
                    d.syncExec(() -> {
                        display.bounds = new Rectangle(0, 0, size.x, size.y);
                    });
                }
            } else {
                // hot reload — re-send the current state
                sendDisplayUpdate(display);
            }
        });

        int port = client.getPort();
        webServer = new WebFlutterServer.Builder()
                .commPort(port)
                .widgetId(displayId)
                .widgetName("Display")
                .build();
        try {
            webServer.start();
            System.out.println("Display " + displayId + " URL: " + webServer.getApplicationUrl());
            webServer.launchBrowser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start web server for Display", e);
        }
    }

    /** Serializes the display's visible shells and pushes the update to Flutter. */
    public void sendDisplayUpdate(DartDisplay display) {
        if (!clientReady.isDone()) return;
        try {
            VDisplay vd = VDisplay.of(display);
            System.out.println("sendDisplayUpdate: " + vd.shells.length + " visible shell(s)");
            serializeAndSend("Display/" + vd.id, vd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
        // Bridge is initialized at Display level in initForDisplay() — nothing to do here.
    }

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartShell dartShell && forDisplay != null) {
            forDisplay.removeShell((Shell) dartShell.getApi());
            sendDisplayUpdate(forDisplay);
        }
    }

    /** Stop the web server when the display is being disposed. */
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
        if (control instanceof DartShell) {
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

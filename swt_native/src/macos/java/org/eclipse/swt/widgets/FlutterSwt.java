package org.eclipse.swt.widgets;

import java.nio.file.Paths;

public class FlutterSwt {
    static boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
    private static final FlutterClient client;

    static {
//        System.load(
//                "/Users/guillez/ws/equoswt/flutter-lib/build/macos/Build/Products/Release/swtflutter.app");

        if (isMac) {
            String app = "/Users/guillez/ws/equoswt/flutter-lib/build/macos/Build/Products/Release";
            System.load(Paths.get(app, "FlutterMacOS.framework/FlutterMacOS").toAbsolutePath().toString());
            System.load(Paths.get(app, "libFlutterBridge.dylib").toAbsolutePath().toString());
        } else
            System.loadLibrary("flutter_library");

        client = new FlutterClient();
        client.createComm();
    }

    public static void initializeFlutterView(long parent, DartControl control) {
        InitializeFlutterWindow(client.getPort(), parent, control.hashCode(), getWidgetName(control));
    }

    public static String getWidgetName(DartWidget w) {
        return w.getClass().getSimpleName().substring(4);
    }

    static native void InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);
}

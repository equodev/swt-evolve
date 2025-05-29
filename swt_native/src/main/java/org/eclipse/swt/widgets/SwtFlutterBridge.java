package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;

import java.nio.file.Paths;

public class SwtFlutterBridge extends FlutterBridge {
    static boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
    private long context;

    static {
//        System.load(
//                "/Users/guillez/ws/equoswt/flutter-lib/build/macos/Build/Products/Release/swtflutter.app");

        if (isMac) {
            String app = "/Users/guillez/ws/equoswt/flutter-lib/build/macos/Build/Products/Release";
            System.load(Paths.get(app, "FlutterMacOS.framework/FlutterMacOS").toAbsolutePath().toString());
            System.load(Paths.get(app, "libFlutterBridge.dylib").toAbsolutePath().toString());
        } else
            System.loadLibrary("flutter_library");
    }

    public static SwtFlutterBridge of(DartControl dartControl) {
        if (dartControl.parent.getImpl() instanceof SwtComposite) {
            SwtComposite parentComposite = new SwtComposite(dartControl.parent, SWT.NONE);
            SwtFlutterBridge bridge = new SwtFlutterBridge();
            bridge.initFlutterView(parentComposite.getApi().view.id, dartControl);
            return bridge;
        }
        return null;
    }

    void initFlutterView(long parent, DartControl control) {
        super.onReady(control);
        context = InitializeFlutterWindow(client.getPort(), parent, control.hashCode(), widgetName(control));
    }

    static native long InitializeFlutterWindow(int port, long parentHandle, long widgetId, String widgetName);

}


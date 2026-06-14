package org.eclipse.swt.widgets;

/**
 * Pure-web {@link SwtFlutterBridge}. Unlike the per-OS web variants
 * (webLinux/webMacos/webWindows) it performs no native GTK/Cocoa/Win32
 * initialization — the pure-web build has no native platform to bind to.
 */
public class SwtFlutterBridge extends SwtFlutterBridgeWeb {

    SwtFlutterBridge(DartDisplay display) {
        super(display);
    }

    SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }
}

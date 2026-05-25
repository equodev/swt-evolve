package org.eclipse.swt.widgets;

public class SwtFlutterBridge extends SwtFlutterBridgeWeb {
    SwtFlutterBridge(DartDisplay display) {
        super(display);
    }

    SwtFlutterBridge(DartWidget widget) {
        super(widget);
    }
}
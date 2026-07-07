package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.graphics.Point;

public class DartSideBar extends DartComposite {

    public DartSideBar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        // Empty rail (no minimized views) reserves no space; a fixed 30px left a phantom strip on load.
        int width = _getChildren().length == 0 ? 0 : 30;
        // On the 0<->30 flip, re-dirty the parent so its NoLayout snapshot re-serializes with the new slot.
        if (width != this.bounds.width) {
            FlutterBridge bridge = getBridge();
            if (bridge != null && parent != null && parent.getImpl() instanceof DartWidget dw)
                bridge.dirty(dw);
        }
        return new Point(width, width == 0 ? 0 : 20);
    }
}

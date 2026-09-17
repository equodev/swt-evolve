package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.graphics.Point;

public class DartSideBar extends DartComposite {

    public DartSideBar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        return railSize(this);
    }

    /**
     * Size of a vertical trim rail. Shared with {@link DartStatusBar}, which a window whose trim
     * bars are not built in the canonical order can land on a side slot.
     */
    static Point railSize(DartComposite rail) {
        // Empty rail (no minimized views) reserves no space; a fixed 30px left a phantom strip on load.
        int width = rail._getChildren().length == 0 ? 0 : 30;
        // On the 0<->30 flip, re-dirty the parent so its NoLayout snapshot re-serializes with the new slot.
        if (width != rail.bounds.width) {
            FlutterBridge bridge = rail.getBridge();
            if (bridge != null && rail.parent != null && rail.parent.getImpl() instanceof DartWidget)
                bridge.dirty(((DartWidget) rail.parent.getImpl()));
        }
        return new Point(width, width == 0 ? 0 : 20);
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.size.ViewRailsSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewRails;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class DartSideBar extends DartComposite {

    public DartSideBar(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    @Override
    public void _dirtied() {
        ViewRails.touched(this);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int rail = ViewRails.railWidth(this);
        return rail >= 0 ? new Point(rail, rail == 0 ? 0 : ViewRailsSizes.MIN_HEIGHT) : railSize(this);
    }

    /**
     * A view rail centers what else its trim holds (a minimized stack's trim icons) on the rail's
     * axis, where its buttons are: the trim layout stretches each control across the bar and its
     * content sits at the leading edge.
     */
    @Override
    public void updateLayout(boolean all) {
        super.updateLayout(all);
        if (ViewRails.railWidth(this) <= 0) return;
        int width = getClientArea().width;
        for (Control child : _getChildren()) {
            Rectangle bounds = child.getBounds();
            int w = Math.min(width, child.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
            int x = Math.max(0, (width - w) / 2);
            if (bounds.x != x || bounds.width != w) child.setBounds(x, bounds.y, w, bounds.height);
        }
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

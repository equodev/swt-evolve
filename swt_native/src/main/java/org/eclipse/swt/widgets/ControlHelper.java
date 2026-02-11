package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ControlHelper {

    private static Control walkToSwtAncestor(Control control, int[] offset) {
        Control current = control;
        while (current != null && !(current.getImpl() instanceof SwtControl)) {
            Rectangle bounds = current.getBounds();
            offset[0] += bounds.x;
            offset[1] += bounds.y;
            current = current.getParent();
        }
        return current;
    }

    static Point toDisplay(DartControl dartControl, int x, int y) {
        int[] offset = new int[2];
        Control ancestor = walkToSwtAncestor(dartControl.getApi(), offset);
        if (ancestor != null) {
            return ancestor.toDisplay(x + offset[0], y + offset[1]);
        }
        return dartControl.display.map(dartControl.getApi(), null, x, y);
    }

    static Point toControl(DartControl dartControl, int x, int y) {
        int[] offset = new int[2];
        Control ancestor = walkToSwtAncestor(dartControl.getApi(), offset);
        if (ancestor != null) {
            Point result = ancestor.toControl(x, y);
            return new Point(result.x - offset[0], result.y - offset[1]);
        }
        return dartControl.display.map(null, dartControl.getApi(), x, y);
    }

    static void paint(DartControl c, Event e) {
        if (c.drawCount > 0) return;
        firePaint(c);
    }

    static void paint(DartControl c) {
        if (c.drawCount > 0) return;
        c.drawCount++;
        c.getDisplay().asyncExec(() -> {
            c.drawCount--;
            firePaint(c);
        });
    }

    private static void firePaint(DartControl c) {
        if (c.isDisposed()) return;
        try {
            if (!Class.forName("org.eclipse.draw2d.FigureCanvas").isInstance(c.getApi())) {
                Event event = new Event();
                event.gc = new GC(c.getApi());
                c.sendEvent(SWT.Paint, event);
            }
        } catch (ClassNotFoundException ex) {
            Event event = new Event();
            event.gc = new GC(c.getApi());
            c.sendEvent(SWT.Paint, event);
        }
    }

}

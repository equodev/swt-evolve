package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

public class ControlHelper {

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

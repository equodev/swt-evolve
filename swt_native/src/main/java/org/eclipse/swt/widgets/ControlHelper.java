package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ControlHelper {

    private static Control walkToSwtAncestor(Control control, int[] offset) {
        Control current = control;
        while (current != null && (current.getImpl() instanceof DartControl)) {
            Rectangle bounds = current.getBounds();
            offset[0] += bounds.x;
            offset[1] += bounds.y;
            if (current instanceof Shell) {
                int st = current.getStyle();
                boolean showsTitleBar = (st & SWT.NO_TRIM) == 0
                    && ((st & SWT.TITLE) != 0 || (st & SWT.CLOSE) != 0);
                if (showsTitleBar && !isMainShell((Shell) current)) {
                    offset[1] += (st & SWT.TOOL) != 0 ? 22 : 30;
                }
                return null;
            }
            current = current.getParent();
        }
        return current;
    }

    private static boolean isMainShell(Shell shell) {
        Rectangle b = shell.getBounds();
        if (b.x != 0 || b.y != 0)
            return false;
        int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;
        if ((shell.getStyle() & modal) != 0)
            return false;
        if (b.width == 1024 && b.height == 768)
            return true;
        Rectangle view = shell.getMonitor().getClientArea();
        return b.width >= Math.round(view.width * 0.8f)
            && b.height >= Math.round(view.height * 0.8f);
    }

    static Point toDisplay(DartControl dartControl, int x, int y) {
        int[] offset = new int[2];
        Control ancestor = walkToSwtAncestor(dartControl.getApi(), offset);
        if (ancestor != null) {
            return ancestor.toDisplay(x + offset[0], y + offset[1]);
        }
        return new Point(x + offset[0], y + offset[1]);
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

    public static void paint(DartControl c) {
        if (c.drawCount > 0) return;
        c.drawCount++;
        c.getDisplay().asyncExec(() -> {
            c.drawCount--;
            firePaint(c);
            c.dirty();
        });
    }

    private static void firePaint(DartControl c) {
        if (c.isDisposed()) return;
        Composite parent = c.getParent();
        while (parent != null) {
            if (parent.isDisposed())
                return;
            parent = parent.getParent();
        }
        Rectangle bounds = c.getBounds();
        try {
            if (!Class.forName("org.eclipse.draw2d.FigureCanvas").isInstance(c.getApi())) {
                Event event = new Event();
                event.x = 0;
                event.y = 0;
                event.width = bounds.width;
                event.height = bounds.height;
                event.gc = new GC(c.getApi());
                c.sendEvent(SWT.Paint, event);
                event.gc.dispose();
            }
        } catch (ClassNotFoundException ex) {
            Event event = new Event();
            event.x = 0;
            event.y = 0;
            event.width = bounds.width;
            event.height = bounds.height;
            event.gc = new GC(c.getApi());
            c.sendEvent(SWT.Paint, event);
            event.gc.dispose();
        }
    }

    public static void setEnabled(DartControl c, boolean enabled) {
        boolean newValue = enabled;
        if (!java.util.Objects.equals(c.enabled, newValue)) {
            c.dirty();
        }
        c.checkWidget();
        if (((c.getApi().state & DartWidget.DISABLED) == 0) == enabled)
            return;
        Control control = null;
        boolean fixFocus = false;
        if (!enabled) {
//            if (((SwtDisplay) c.display.getImpl()).focusEvent != SWT.FocusOut) {
//                control = c.display.getFocusControl();
//                fixFocus = c.isFocusAncestor(control);
//            }
        }
        if (enabled) {
            c.getApi().state &= ~DartWidget.DISABLED;
        } else {
            c.getApi().state |= DartWidget.DISABLED;
        }
        c.enabled = newValue;
        c.enableWidget(enabled);
        if (fixFocus)
            c.fixFocus(control);
    }

}

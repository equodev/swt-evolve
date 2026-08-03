package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ControlHelper {

    // True while a Flutter-originated KeyDown is being dispatched. Widgets that edit content on
    // KeyDown (StyledText.handleKey) consult it to skip re-applying what the Flutter editor
    // already inserted; Java-simulated keys (upstream tests, apps) keep the full SWT behavior.
    private static final ThreadLocal<Boolean> FLUTTER_KEY = ThreadLocal.withInitial(() -> false);

    public static void sendFlutterKeyDown(DartWidget widget, Event event) {
        FLUTTER_KEY.set(true);
        try {
            widget.sendEvent(SWT.KeyDown, event);
        } finally {
            FLUTTER_KEY.set(false);
        }
        translateTraversal(widget, event);
    }

    /**
     * Dart controls have no OS window proc, so nothing turns an incoming KeyDown into an
     * {@link SWT#Traverse} event the way {@code SwtControl.translateTraversal} does natively.
     * Popups and dialogs (e.g. the Command Palette) close on Escape via a Traverse listener on
     * their Shell, which never fires without this translation. Mirror the native ancestor walk by
     * delegating to {@code Control.traverse(int, Event)}, which sends SWT.Traverse up the parent
     * chain to the Shell.
     */
    private static void translateTraversal(DartWidget widget, Event keyEvent) {
        if (!(widget instanceof DartControl))
            return;
        DartControl origin = (DartControl) widget;
        if (origin.isDisposed())
            return;
        int detail;
        switch (keyEvent.keyCode) {
            case SWT.ESC:
                detail = SWT.TRAVERSE_ESCAPE;
                break;
            default:
                return;
        }
        Control start = resolveTraverseStart(origin);
        if (start == null || start.isDisposed() || !(start.getImpl() instanceof DartControl))
            return;
        Event event = new Event();
        event.character = keyEvent.character;
        event.keyCode = keyEvent.keyCode;
        event.keyLocation = keyEvent.keyLocation;
        event.stateMask = keyEvent.stateMask;
        event.doit = true;
        ((DartControl) start.getImpl()).traverse(detail, event);
    }

    /**
     * Native SWT starts a traversal at {@code display.getFocusControl()}, not necessarily the widget
     * that observed the key. When the key was forwarded by an actual control that control is already
     * the right start. But a shell-level Escape (the shell's Flutter focus scope caught the key
     * because no child holds Flutter focus) must start at the control the app's Traverse listener
     * sits on — the focused control — or the walk fires only on the shell and misses the listener.
     * {@code getFocusControl()} is Java-side tracked (see {@code DisplayBridge}), so it is set even
     * though no Flutter FocusIn reached Java.
     */
    private static Control resolveTraverseStart(DartControl origin) {
        Control self = origin.getApi();
        if (!(self instanceof Shell))
            return self;
        Shell shell = (Shell) self;
        Control fc = shell.getDisplay().getFocusControl();
        if (fc != null && !fc.isDisposed() && fc.getShell() == shell && fc.getImpl() instanceof DartControl)
            return fc;
        return self;
    }

    public static boolean isFlutterOriginatedKey() {
        return FLUTTER_KEY.get();
    }

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
        // Native SWT never delivers a Paint event for an empty area. Some controls
        // (e.g. FormText) allocate a back-buffer Image of the paint size and would
        // throw ERROR_INVALID_ARGUMENT (ImageData with width/height <= 0) when asked
        // to paint a 0-sized region. This happens here because a Paint can be
        // dispatched re-entrantly while bounds are still 0 (e.g. event loop pumped
        // from DartImage.getImageData during serialization).
        if (bounds.width <= 0 || bounds.height <= 0)
            return;
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

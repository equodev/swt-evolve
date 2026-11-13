package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Java8;
import dev.equo.swt.WidgetWire;
import dev.equo.swt.WindowBridge;
import dev.equo.swt.WireObject;
import dev.equo.swt.WireTable;
import java.util.Map;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

/**
 * Carries a DragSource's drag into the application's other windows. Flutter's drag and drop only
 * meets inside one engine and every window has an engine of its own, so the window a drag starts in
 * reports where the pointer is, and the shell under it in another window is told the drag is over
 * it, has left it, or was dropped on it. That window finds the DropTarget at the point and
 * negotiates with it as it does for a drag of its own.
 *
 * <p>The window the drag started in keeps the pointer until the release, so it draws the cursor,
 * from what that DropTarget answers; the other window draws the drag image, which the source sends
 * once and Java relays ahead of the first step into each window.
 *
 * <p>One per DragSource; what it holds lasts one drag.
 */
final class RemoteDrag {

    /** A step's phase, as {@code Event.detail}: the numbers the client's RemoteDragReceiver reads. */
    static final int OVER = 1;
    static final int LEAVE = 2;
    static final int DROP = 3;

    private final DartDragSource source;

    /** The shell in another window the drag is over, or null. */
    private Shell target;

    /** The shell the drag was released over, until it answers or the drag ends. */
    private Shell dropShell;

    /** The drag image as PNG bytes and its size in points, or null while the source has sent none. */
    private byte[] image;
    private int imageWidth;
    private int imageHeight;

    /** The detail the source's client was last told, which it keeps until told another. */
    private int answered;

    private final Step step = new Step();

    RemoteDrag(DartDragSource source) {
        this.source = source;
    }

    /** Listens for where the drag goes, on the channel of the control it started from. */
    void begin() {
        target = null;
        image = null;
        answered = DND.DROP_NONE;
        Control control = source._control();
        if (control == null || control.isDisposed() || !(control.getImpl() instanceof DartWidget)) return;
        DartWidget impl = (DartWidget) control.getImpl();
        Display display = control.getDisplay();
        FlutterBridge.on(impl, WireTable.CHANNEL_DRAG_DETECT_DRAG_MOVE, e -> onDisplay(display, () -> moved(e.x, e.y)));
        FlutterBridge.on(impl, WireTable.CHANNEL_DRAG_DETECT_DRAG_RELEASE, e -> onDisplay(display, () -> released(e.x, e.y)));
        FlutterBridge.on(impl, WireTable.CHANNEL_DRAG_DETECT_DRAG_IMAGE, e -> onDisplay(display, () -> imaged(e)));
        FlutterBridge.on(impl, WireTable.CHANNEL_DRAG_DETECT_DRAG_CANCEL, e -> onDisplay(display, this::cancelled));
    }

    /** Escape cancelled the drag: the shell it is over hears it leave, and it ends with nothing dropped. */
    void cancelled() {
        if (!active()) return;
        Shell shell = target;
        target = null;
        if (shell != null && !shell.isDisposed()) send(shell, LEAVE, 0, 0);
        source.fireDragEnd(DND.DROP_NONE);
    }

    void end() {
        target = null;
        image = null;
        forgetDrop();
    }

    private void imaged(Event e) {
        if (!active() || e.segments == null) return;
        image = png(e.segments);
        imageWidth = e.width;
        imageHeight = e.height;
    }

    /** The pointer, in display coordinates, moved while the drag is out of reach of its window. */
    void moved(int x, int y) {
        if (!active()) return;
        Shell shell = shellAt(x, y);
        boolean entering = shell != target;
        if (target != null && entering && !target.isDisposed()) send(target, LEAVE, 0, 0);
        target = shell;
        // Until a DropTarget in the new shell answers, nothing there takes the drop.
        if (entering) answer(DND.DROP_NONE);
        if (shell == null) return;
        if (entering && image != null) {
            Map<String, Object> png = Java8.map("segments", image);
            png.put("width", imageWidth);
            png.put("height", imageHeight);
            // Not held behind a pending update, as the step it comes ahead of is not.
            FlutterBridge.sendNow((DartWidget) shell.getImpl(), WireTable.CHANNEL_DRAG_DETECT_DRAG_IMAGE, png);
        }
        send(shell, OVER, x, y);
    }

    /** What a DropTarget answered a drag over it; the one in the shell the drag is over sets the cursor. */
    void answered(Control control, int detail) {
        if (!active() || target == null || control == null || control.isDisposed() || control.getShell() != target) return;
        answer(detail);
    }

    private void answer(int detail) {
        if (detail == answered) return;
        answered = detail;
        Control control = source._control();
        if (control == null || control.isDisposed() || !(control.getImpl() instanceof DartWidget)) return;
        FlutterBridge.send((DartWidget) control.getImpl(), WireTable.CHANNEL_DRAG_DETECT_REMOTE_ANSWER, Java8.map("detail", detail));
    }

    /** The drag ended with no target in its own window taking it. */
    void released(int x, int y) {
        if (!active()) return;
        moved(x, y);
        Shell shell = target;
        target = null;
        if (shell == null) {
            source.fireDragEnd(DND.DROP_NONE);
            return;
        }
        // Answered only when nothing there takes the drop: a DropTarget that does ends the drag.
        Display display = shell.getDisplay();
        dropShell = shell;
        FlutterBridge.on((DartWidget) shell.getImpl(), WireTable.CHANNEL_DN_D_REMOTE_DROP_NONE, e -> onDisplay(display, () -> {
            forgetDrop();
            if (active()) source.fireDragEnd(DND.DROP_NONE);
        }));
        send(shell, DROP, x, y);
    }

    /** Stops listening for the drop's answer, so the shell holds no finished drag's source. */
    private void forgetDrop() {
        Shell shell = dropShell;
        dropShell = null;
        if (shell != null && !shell.isDisposed()) FlutterBridge.removeEvent(shell.getImpl(), WireTable.CHANNEL_DN_D_REMOTE_DROP_NONE);
    }

    private boolean active() {
        return !source.isDisposed() && ActiveDrag.is(source.getApi());
    }

    /**
     * The shell at a display point that another window draws, or null when the point is in the drag's
     * own window, which overlaps the others where the pointer is over it.
     */
    private Shell shellAt(int x, int y) {
        Control control = source._control();
        if (control == null || control.isDisposed()) return null;
        FlutterBridge bridge = ((DartWidget) control.getImpl()).getBridge();
        if (!(bridge instanceof WindowBridge)) return null;
        WindowBridge windows = (WindowBridge) bridge;
        Object home = windows.windowOf(control.getShell());
        Shell found = null;
        // Bottom first: the last shell at the point is drawn over the others there.
        for (Object drawn : windows.shellsInDrawOrder()) {
            Shell shell = (Shell) drawn;
            if (shell.isDisposed() || !shell.isVisible() || !shell.getBounds().contains(x, y)) continue;
            if (windows.windowOf(shell) == home) return null;
            found = shell;
        }
        return found;
    }

    /** Tells {@code shell} the drag is at {@code phase}, at the display point {@code x, y} unless it left. */
    private void send(Shell shell, int phase, int x, int y) {
        step.phase = phase;
        if (phase != LEAVE) {
            Rectangle bounds = shell.getBounds();
            step.x = x - bounds.x;
            step.y = y - bounds.y;
        }
        FlutterBridge.sendFields((DartWidget) shell.getImpl(), WireTable.CHANNEL_DN_D_REMOTE_DRAG, step);
    }

    /** A drag step as the client's Event carries it: the phase, and where the drag is relative to the shell. */
    private static final class Step implements WireObject {
        int phase;
        int x;
        int y;

        @Override
        public void writeWireFields(WidgetWire w) {
            w.key(WireTable.EVENT_DETAIL);
            w.write(phase);
            if (phase == LEAVE) return;
            w.key(WireTable.EVENT_X);
            w.write(x);
            w.key(WireTable.EVENT_Y);
            w.write(y);
        }

        @Override
        public int wireTypeId() {
            return WireTable.TYPE_EVENT;
        }
    }

    /** The PNG the client sent as {@code Event.segments}, one byte per element. */
    private static byte[] png(int[] segments) {
        byte[] bytes = new byte[segments.length];
        for (int i = 0; i < segments.length; i++) bytes[i] = (byte) segments[i];
        return bytes;
    }

    private static void onDisplay(Display display, Runnable action) {
        if (!display.isDisposed()) display.asyncExec(action);
    }
}

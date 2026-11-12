package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code Control.dragDetect} for a Dart control. The pointer is followed through the reports the client sends while
 * the event loop keeps running; the reports read meanwhile are held and reach the listeners once the call has
 * answered, as the natives hand back the events they pulled off the queue.
 */
public class DragDetectHelper {

    /** How far, on either axis, the pointer moves before a press becomes a drag (Cocoa's hysteresis). */
    static final int HYSTERESIS = 5;

    /** How long a press that neither moves nor comes up waits before it counts as a drag (Cocoa's timeout). */
    static final long TIMEOUT_MS = 300;

    private static final String TRACKING = "dev.equo.swt.dragDetect";

    /** The control whose held press a dragDetect call answered true, until its release or the next press. */
    private static final String ANSWERED = "dev.equo.swt.dragDetect.answered";

    private DragDetectHelper() {
    }

    private static final class Held {
        final DartControl control;
        final int type;
        final Event event;

        Held(DartControl control, int type, Event event) {
            this.control = control;
            this.type = type;
            this.event = event;
        }
    }

    private static final class Tracking {
        final DartControl control;
        final Point start;
        final List<Held> held = new ArrayList<>();
        Boolean dragging;

        Tracking(DartControl control, int x, int y) {
            this.control = control;
            this.start = control.getApi().toDisplay(x, y);
        }

        void moved(DartControl over, int moveX, int moveY) {
            Point now = over.getApi().toDisplay(moveX, moveY);
            if (Math.abs(now.x - start.x) > HYSTERESIS || Math.abs(now.y - start.y) > HYSTERESIS)
                dragging = true;
        }
    }

    /**
     * Whether a first-button press at {@code (x, y)} becomes a drag: true once the pointer moves past the hysteresis
     * or when {@link #TIMEOUT_MS} pass with neither a move past it nor a release, false on the release.
     */
    static boolean dragDetect(DartControl control, int x, int y) {
        Display display = control.display;
        Tracking tracking = new Tracking(control, x, y);
        Object outer = display.getData(TRACKING);
        display.setData(TRACKING, tracking);
        long deadline = System.nanoTime() + TIMEOUT_MS * 1_000_000L;
        try {
            while (tracking.dragging == null && System.nanoTime() < deadline
                    && !display.isDisposed() && !control.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } finally {
            if (!display.isDisposed())
                display.setData(TRACKING, outer);
        }
        if (display.isDisposed())
            return false;
        boolean released = false;
        for (Held held : tracking.held) {
            released |= held.type == SWT.MouseUp;
            if (held.control.isDisposed())
                continue;
            held.control.postEvent(held.type, held.event);
            if (held.type == SWT.MouseUp)
                display.asyncExec(() -> afterRelease(held.control));
        }
        if (control.isDisposed())
            return false;
        boolean dragging = tracking.dragging == null || tracking.dragging;
        if (dragging && !released)
            display.setData(ANSWERED, control);
        return dragging;
    }

    /** What a release completes, once its MouseUp has reached the listeners: see {@link ControlHelper#released}. */
    private static void afterRelease(DartControl control) {
        if (!control.isDisposed())
            ControlHelper.released(control);
    }

    /** Forgets the press a dragDetect call answered, once it comes up or another press starts. */
    static void forgetAnswer(Display display) {
        if (display.getData(ANSWERED) != null)
            display.setData(ANSWERED, null);
    }

    /**
     * Delivers a DragDetect the client reports, unless a dragDetect call answers for that press: the call posts the
     * press's one DragDetect itself.
     */
    static void clientDragDetect(DartControl control, Event event) {
        Display display = control.display;
        Object tracking = display.getData(TRACKING);
        if (tracking instanceof Tracking && ((Tracking) tracking).control == control)
            return;
        if (display.getData(ANSWERED) == control) {
            display.setData(ANSWERED, null);
            return;
        }
        control.sendEvent(SWT.DragDetect, event);
    }

    /** Delivers a MouseMove or MouseUp the client reports, or holds it while a drag detection is under way. */
    static void mouse(DartControl control, int type, Event event) {
        // On every native the release that ends a Tracker's loop reaches the Tracker alone.
        if (type == SWT.MouseUp && ControlHelper.releaseOwned(control.display))
            return;
        if (type == SWT.MouseUp && control instanceof DartSash)
            SashHelper.placeRelease((DartSash) control, event);
        Object data = control.display.getData(TRACKING);
        boolean tracked = data instanceof Tracking && (type == SWT.MouseMove || event.button == 1);
        if (!tracked) {
            if (type == SWT.MouseUp && event.button == 1)
                forgetAnswer(control.display);
            control.sendEvent(type, event);
            if (type == SWT.MouseUp)
                afterRelease(control);
            return;
        }
        Tracking tracking = (Tracking) data;
        tracking.held.add(new Held(control, type, event));
        if (tracking.dragging != null)
            return;
        if (type == SWT.MouseUp)
            tracking.dragging = false;
        else
            tracking.moved(control, event.x, event.y);
    }
}

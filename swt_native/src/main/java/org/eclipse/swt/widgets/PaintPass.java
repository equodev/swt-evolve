package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import dev.equo.swt.FlutterBridge;

/**
 * The Paints a Display's controls asked for, run together as one runnable of the event loop, the way a
 * native toolkit paints everything invalidated in one pass, and sent to the client as one message.
 *
 * <p>A pass sends what it paints, then the answer to the moves handled ({@code MoveCompression}), in
 * one message, so the client never draws a layout before the contents painted for it.
 *
 * <p>A Paint asked for while the pass runs (a control that redraws another from its paint listener, as
 * a ruler follows its text) is run in the same pass, so it travels with what asked for it. A pass never
 * runs a control twice: one asked for again after it painted goes in the next pass.
 */
public final class PaintPass {

    private static final String KEY = PaintPass.class.getName();

    private final Display display;
    private final LinkedHashSet<DartControl> queued = new LinkedHashSet<>();
    private boolean posted;
    private boolean running;

    private PaintPass(Display display) {
        this.display = display;
    }

    /** Paints {@code c} in its Display's next pass, or in the one running now. */
    static void add(DartControl c) {
        Display display = c.getDisplay();
        PaintPass pass = of(display, true);
        synchronized (pass) {
            pass.queued.add(c);
            if (pass.posted || pass.running) return;
            pass.posted = true;
        }
        display.asyncExec(pass::run);
    }

    /** Whether the current thread's Display has a pass waiting to run: what it paints answers moves too. */
    public static boolean pending() {
        Display display = Display.getCurrent();
        if (display == null || display.isDisposed()) return false;
        PaintPass pass = of(display, false);
        if (pass == null) return false;
        synchronized (pass) {
            return pass.posted;
        }
    }

    private static PaintPass of(Display display, boolean make) {
        Object data = display.getData(KEY);
        if (data instanceof PaintPass) return (PaintPass) data;
        if (!make) return null;
        PaintPass pass = new PaintPass(display);
        display.setData(KEY, pass);
        return pass;
    }

    private void run() {
        synchronized (this) {
            posted = false;
            running = true;
        }
        try {
            if (display.isDisposed()) return;
            Set<DartControl> painted = new HashSet<>();
            FlutterBridge.inOneMessage(() -> {
                // A listener that throws fails its own Paint, as it did in a runnable of its own: the rest
                // still paint, and the first failure is reported once they have.
                Throwable failed = null;
                for (ArrayList<DartControl> controls = take(painted); !controls.isEmpty(); controls = take(painted)) {
                    for (DartControl c : controls) {
                        try {
                            ControlHelper.runDispatchedPaint(c);
                        } catch (RuntimeException | Error e) {
                            if (failed == null) failed = e;
                        }
                    }
                }
                if (failed instanceof Error) throw (Error) failed;
                if (failed != null) throw (RuntimeException) failed;
            });
        } finally {
            boolean again;
            synchronized (this) {
                running = false;
                again = !queued.isEmpty() && !posted && !display.isDisposed();
                if (again) posted = true;
            }
            if (again) display.asyncExec(this::run);
        }
    }

    /** The queued controls this pass has not painted yet, taken off the queue and marked painted. */
    private synchronized ArrayList<DartControl> take(Set<DartControl> painted) {
        ArrayList<DartControl> controls = new ArrayList<>();
        for (Iterator<DartControl> it = queued.iterator(); it.hasNext(); ) {
            DartControl c = it.next();
            if (painted.add(c)) {
                controls.add(c);
                it.remove();
            }
        }
        return controls;
    }
}

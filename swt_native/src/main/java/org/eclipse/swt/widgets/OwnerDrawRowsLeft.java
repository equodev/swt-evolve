package org.eclipse.swt.widgets;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The owner-drawn rows of a Tree or Table that a repaint left out, named a few at a time once the
 * repaints stop. Naming a row runs the application's PaintItem listener for it in the next flush:
 * a couple of thousand named at once held the UI thread for tens of milliseconds, in the way of
 * whatever the user did next.
 */
final class OwnerDrawRowsLeft implements Runnable {

    /** How long after the last repaint the rows it left out start to be named. */
    static final int SETTLE_MS = 100;

    /** Rows named per step: a few milliseconds of PaintItem listener. */
    static final int ROWS_PER_STEP = 200;

    /** Between steps: about a frame, so input arriving meanwhile is handled first. */
    static final int STEP_MS = 16;

    private final Display display;
    private final Supplier<List<? extends Item>> rows;
    private final Consumer<Item> name;
    private final Runnable done;
    private List<? extends Item> left;
    private int next;

    /**
     * @param rows every row, as of when the naming starts; null when the control is gone
     * @param name names one row
     * @param done called once every row is named, or the control is gone
     */
    OwnerDrawRowsLeft(Display display, Supplier<List<? extends Item>> rows, Consumer<Item> name, Runnable done) {
        this.display = display;
        this.rows = rows;
        this.name = name;
        this.done = done;
    }

    /** A repaint left rows out: the naming starts over once the repaints have stopped. */
    void arm() {
        left = null;
        next = 0;
        display.timerExec(SETTLE_MS, this);
    }

    @Override
    public void run() {
        if (left == null) {
            left = rows.get();
            if (left == null) {
                done.run();
                return;
            }
        }
        int end = Math.min(left.size(), next + ROWS_PER_STEP);
        while (next < end) {
            Item row = left.get(next++);
            if (!row.isDisposed()) name.accept(row);
        }
        if (next < left.size()) {
            display.timerExec(STEP_MS, this);
        } else {
            left = null;
            done.run();
        }
    }
}

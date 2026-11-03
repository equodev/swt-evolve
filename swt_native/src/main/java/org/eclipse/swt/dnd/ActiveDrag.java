package org.eclipse.swt.dnd;

import org.eclipse.swt.widgets.Display;

/**
 * The drag a display has under way, kept on the display: one JVM serves several sessions, and a
 * drop or a drag end in one must leave another session's drag alone.
 */
public final class ActiveDrag {

    private static final String KEY = "org.eclipse.swt.dnd.ActiveDrag";

    /** A drag its DragSource refused: Flutter carries the gesture on, but natively nothing drops. */
    private static final class Refused {
        final DragSource source;

        Refused(DragSource source) {
            this.source = source;
        }
    }

    private ActiveDrag() {
    }

    public static void begin(DragSource source) {
        source.getDisplay().setData(KEY, source);
    }

    /** The gesture {@code source} refused in DragStart, until it ends or the next drag starts. */
    public static void refuse(DragSource source) {
        source.getDisplay().setData(KEY, new Refused(source));
    }

    public static boolean is(DragSource source) {
        return source.getDisplay().getData(KEY) == source;
    }

    /** Whether the display's gesture is one its DragSource refused. */
    public static boolean refused(Display display) {
        return display.getData(KEY) instanceof Refused;
    }

    /** Ends {@code source}'s drag, refused or not. A disposed source has no display to clear; {@link #of} skips it. */
    public static void end(DragSource source) {
        if (source.isDisposed()) return;
        Object current = source.getDisplay().getData(KEY);
        if (current == source || current instanceof Refused && ((Refused) current).source == source)
            source.getDisplay().setData(KEY, null);
    }

    /** Ends a refused gesture at its drop. */
    public static void endRefused(Display display) {
        if (refused(display)) display.setData(KEY, null);
    }

    /** The display's drag, or null when none is under way, it was refused or its source is disposed. */
    public static DragSource of(Display display) {
        Object source = display.getData(KEY);
        return source instanceof DragSource && !((DragSource) source).isDisposed() ? (DragSource) source : null;
    }
}

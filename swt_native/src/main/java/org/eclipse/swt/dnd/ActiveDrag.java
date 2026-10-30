package org.eclipse.swt.dnd;

import org.eclipse.swt.widgets.Display;

/**
 * The drag a display has under way, kept on the display: one JVM serves several sessions, and a
 * drop or a drag end in one must leave another session's drag alone.
 */
public final class ActiveDrag {

    private static final String KEY = "org.eclipse.swt.dnd.ActiveDrag";

    private ActiveDrag() {
    }

    public static void begin(DragSource source) {
        source.getDisplay().setData(KEY, source);
    }

    public static boolean is(DragSource source) {
        return source.getDisplay().getData(KEY) == source;
    }

    /** A disposed source has no display to clear; {@link #of} skips it. */
    public static void end(DragSource source) {
        if (!source.isDisposed() && is(source)) source.getDisplay().setData(KEY, null);
    }

    /** The display's drag, or null when none is under way or its source is disposed. */
    public static DragSource of(Display display) {
        Object source = display.getData(KEY);
        return source instanceof DragSource && !((DragSource) source).isDisposed() ? (DragSource) source : null;
    }
}

package org.eclipse.swt.widgets;

/** Reads a widget's state for Evolve's own plumbing, which must describe a widget even once it is disposed. */
public final class WidgetHelper {

    private WidgetHelper() {
    }

    /** The style bits, without the disposed check {@link Widget#getStyle()} makes. */
    public static int styleOf(Widget widget) {
        return widget.style;
    }

    /**
     * Whether the widget's release has started: it is not disposed yet, but its own state (a StyledText's
     * renderer) may already be gone, so it has nothing left to describe.
     */
    public static boolean isReleased(Widget widget) {
        return (widget.state & DartWidget.RELEASED) != 0;
    }
}

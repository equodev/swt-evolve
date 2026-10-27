package org.eclipse.swt.widgets;

/** Reads a widget's state for Evolve's own plumbing, which must describe a widget even once it is disposed. */
public final class WidgetHelper {

    private WidgetHelper() {
    }

    /** The style bits, without the disposed check {@link Widget#getStyle()} makes. */
    public static int styleOf(Widget widget) {
        return widget.style;
    }
}

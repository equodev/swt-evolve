package dev.equo.swt.outside;

import org.eclipse.swt.widgets.Display;

/** Display subclasses declared outside org.eclipse.swt.widgets, as an application declares them. */
public final class DisplaySubclasses {

    private DisplaySubclasses() {
    }

    public static Display plain() {
        return new Display() {};
    }

    public static Display allowingItself() {
        return new Display() {
            @Override
            protected void checkSubclass() {
            }
        };
    }
}

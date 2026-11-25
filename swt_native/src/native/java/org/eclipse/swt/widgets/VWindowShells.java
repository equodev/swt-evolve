package org.eclipse.swt.widgets;


/**
 * The shells a window of its own draws inside itself, bottom first, sent as a {@code WindowShells}
 * record under the id of the shell that window is rooted at.
 *
 * <p>A shell drawn inline belongs to the window its parent is drawn in. The client of a window of
 * its own never listens to the Display, so this is what hands it those shells;
 * {@code VDisplay.windowedShellIds} has the Display's client leave them out, so exactly one client
 * draws each.
 */
class VWindowShells {

    long windowShellId;
    Shell[] shells;

    /** {@link VDisplay#activeShellId}: only the shell it names takes the keyboard by itself. */
    long activeShellId;

    /** The properties the record describes, numbered as the Display's of the same name. */
    static int[] wireKeys() {
        return new int[] { VDisplay.SHELLS, VDisplay.ACTIVE_SHELL_ID };
    }

    /** Writes one of {@link #wireKeys}, as a widget's keyed writer does. */
    boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        if (key == VDisplay.SHELLS) {
            w.refs(shells);
            return true;
        }
        if (key == VDisplay.ACTIVE_SHELL_ID) {
            w.write(activeShellId);
            return true;
        }
        return false;
    }
}

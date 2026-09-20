package dev.equo.swt.harness;

import org.eclipse.swt.widgets.DartControl;

/**
 * A {@link RecordingBridge} that remembers which control holds focus, the way the whole-tree
 * {@code DisplayBridge} does. The base bridge answers {@code hasFocus} from nothing, so a test about
 * focus behaviour (re-focusing an already-focused control, activation on click) cannot tell the two
 * states apart without this.
 */
public class FocusTrackingBridge extends RecordingBridge {

    private DartControl focused;

    @Override
    public boolean setFocus(DartControl control) {
        focused = control;
        return true;
    }

    @Override
    public boolean hasFocus(DartControl control) {
        return control == focused;
    }

    @Override
    public void clearFocus(DartControl control) {
        if (focused == control)
            focused = null;
    }
}

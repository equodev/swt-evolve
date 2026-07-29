package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Event;

/**
 * Dart→Java glue for {@code DartSashForm}. When the user drags a SashForm divider, the Flutter
 * side ({@code sashform_evolve.dart}) recomputes the panel weights locally and reports them as a
 * Selection event whose {@code text} is the comma-separated weight list. Applying them here keeps
 * Java the owner of the layout truth: without it the next Java-side layout (e.g. a window resize)
 * recomputes the panels from the stale weights and discards the user's drag (issue #509 — the
 * Windows Action Recorder panels snapped back on resize).
 */
public class SashFormHelper {

    public static void applyWeightsFromFlutter(DartSashForm form, Event e) {
        if (form == null || form.isDisposed() || e == null || e.text == null) {
            return;
        }
        String[] parts = e.text.split(",");
        int[] weights = new int[parts.length];
        int total = 0;
        for (int i = 0; i < parts.length; i++) {
            try {
                weights[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException nfe) {
                return; // malformed payload — ignore, keep current weights
            }
            if (weights[i] < 0) {
                return;
            }
            total += weights[i];
        }
        // Mirror setWeights' own validation instead of letting it throw: a stale or
        // mid-rebuild payload (wrong arity, all-zero) must not take down the UI thread.
        if (total == 0 || weights.length != form.getControls(false).length) {
            return;
        }
        form.setWeights(weights);
    }
}

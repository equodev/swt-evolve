package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Logic of {@code DartScrolledCompositeLayout} kept out of the generated class: the generator strips every call on
 * a {@code GC} from the Dart implementations, and the scroll bars' increments are measured with one.
 */
public class ScrolledCompositeHelper {

    private ScrolledCompositeHelper() {
    }

    /** Sets the bars' increments to the composite font's average character width and line height. */
    public static void setIncrements(ScrolledComposite sc, ScrollBar hBar, ScrollBar vBar) {
        if (hBar == null && vBar == null) return;
        GC gc = new GC(sc);
        // Measurement only: disposing it must not announce a paint cycle to Flutter. Only DartScrolledCompositeLayout
        // calls this, and every GC of that backend is a DartGC.
        ((DartGC) gc.getImpl()).silentDispose = true;
        try {
            FontMetrics metrics = gc.getFontMetrics();
            if (hBar != null) hBar.setIncrement(metrics.getAverageCharWidth());
            if (vBar != null) vBar.setIncrement(metrics.getHeight());
        } finally {
            gc.dispose();
        }
    }
}

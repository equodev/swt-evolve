package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.DartGC;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Logic of {@code DartScrolledComposite} and its layout kept out of the generated classes: the generator strips
 * every call on a {@code GC} from the Dart implementations, and the scroll bars' increments are measured with one.
 */
public class ScrolledCompositeHelper {

    private ScrolledCompositeHelper() {
    }

    /** Runs {@code change}, then names the origin when the layout it ran moved the content. */
    public static void keepOrigin(DartScrolledComposite sc, Runnable change) {
        org.eclipse.swt.graphics.Point origin = sc.getValue().getOrigin();
        change.run();
        dev.equo.swt.ChangeMarks.ifChanged(sc, VScrolledComposite.ORIGIN, origin, self -> self.getValue().getOrigin());
    }

    /** The origin the client is sent: a content disposed under the composite has none, as no content has. */
    public static org.eclipse.swt.graphics.Point wireOrigin(DartScrolledComposite sc) {
        org.eclipse.swt.widgets.Control content = sc.content;
        if (content == null || content.isDisposed()) return new org.eclipse.swt.graphics.Point(0, 0);
        return sc.getOrigin();
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

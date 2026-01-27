package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartSlider;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Slider widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class SliderSizes {

    static class HORIZONTAL {
        static final double MIN_WIDTH = 100.0;
        static final double MIN_HEIGHT = 24.0;
    }

    static class VERTICAL {
        static final double MIN_WIDTH = 24.0;
        static final double MIN_HEIGHT = 100.0;
    }

    public static Point computeSize(DartSlider widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartSlider widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.VERTICAL)) {
            width = VERTICAL.MIN_WIDTH;
            height = VERTICAL.MIN_HEIGHT;
        } else { // HORIZONTAL
            width = HORIZONTAL.MIN_WIDTH;
            height = HORIZONTAL.MIN_HEIGHT;
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

}

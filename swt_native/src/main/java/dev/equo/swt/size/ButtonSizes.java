package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartButton;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Button widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class ButtonSizes {

    static class ARROW {
        static final double MIN_WIDTH = 34.0;
        static final double MIN_HEIGHT = 26.0;
        static final double HORIZONTAL_PADDING = 34.0;
    }

    static class CHECK {
        static final double MIN_WIDTH = 16.8;
        static final double MIN_HEIGHT = 16.8;
        static final double HORIZONTAL_PADDING = 24.800000000000008;
        static final double VERTICAL_PADDING = 1.4000000000000004;
    }

    static class PUSH {
        static final double MIN_WIDTH = 2.0;
        static final double MIN_HEIGHT = 2.0;
        static final double HORIZONTAL_PADDING = 2.0;
        static final double VERTICAL_PADDING = 2.0;
    }

    static class RADIO {
        static final double MIN_WIDTH = 20.0;
        static final double MIN_HEIGHT = 20.0;
        static final double HORIZONTAL_PADDING = 28.0;
        static final double VERTICAL_PADDING = 3.0;
    }

    public static Point computeSize(DartButton widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartButton widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.CHECK) || hasFlags(style, (SWT.CHECK | SWT.FLAT)) || hasFlags(style, (SWT.CHECK | SWT.WRAP))) {
            m.text = computeText(widget, m);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? CHECK.HORIZONTAL_PADDING : 0), CHECK.MIN_WIDTH);
            height = Math.max(m.text.y() + (m.text.y() > 0 ? CHECK.VERTICAL_PADDING : 0), CHECK.MIN_HEIGHT);
        } else if (hasFlags(style, SWT.PUSH) || hasFlags(style, (SWT.PUSH | SWT.FLAT)) || hasFlags(style, (SWT.PUSH | SWT.WRAP)) || hasFlags(style, SWT.TOGGLE) || hasFlags(style, (SWT.TOGGLE | SWT.FLAT)) || hasFlags(style, (SWT.TOGGLE | SWT.WRAP))) {
            m.text = computeText(widget, m);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? PUSH.HORIZONTAL_PADDING : 0), PUSH.MIN_WIDTH);
            height = Math.max(m.text.y() + (m.text.y() > 0 ? PUSH.VERTICAL_PADDING : 0), PUSH.MIN_HEIGHT);
        } else if (hasFlags(style, SWT.RADIO) || hasFlags(style, (SWT.RADIO | SWT.FLAT)) || hasFlags(style, (SWT.RADIO | SWT.WRAP))) {
            m.text = computeText(widget, m);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? RADIO.HORIZONTAL_PADDING : 0), RADIO.MIN_WIDTH);
            height = Math.max(m.text.y() + (m.text.y() > 0 ? RADIO.VERTICAL_PADDING : 0), RADIO.MIN_HEIGHT);
        } else { // ARROW, ARROW|FLAT, ARROW|WRAP
            m.text = computeText(widget, m);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? ARROW.HORIZONTAL_PADDING : 0), ARROW.MIN_WIDTH);
            height = Math.max(m.text.y(), ARROW.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeText(DartButton widget, Measure m) {
        String text = widget.getText();
        if (text != null && !text.isEmpty()) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = ButtonTheme.get().textStyle();
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

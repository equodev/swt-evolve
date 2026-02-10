package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.ImageMetricUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
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
        static final double MIN_WIDTH = 26.0;
        static final double MIN_HEIGHT = 26.0;
    }

    static class CHECK {
        static final double MIN_WIDTH = 16.8;
        static final double MIN_HEIGHT = 16.8;
        static final double HORIZONTAL_PADDING = 24.800000000000004;
        static final double VERTICAL_PADDING = 1.4000000000000004;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class PUSH {
        static final double MIN_WIDTH = 18.0;
        static final double MIN_HEIGHT = 10.0;
        static final double HORIZONTAL_PADDING = 18.0;
        static final double VERTICAL_PADDING = 10.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class TOGGLE {
        static final double MIN_WIDTH = 2.0;
        static final double MIN_HEIGHT = 2.0;
        static final double HORIZONTAL_PADDING = 2.0;
        static final double VERTICAL_PADDING = 2.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    public static Point computeSize(DartButton widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartButton widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.CHECK) || hasFlags(style, (SWT.CHECK | SWT.FLAT)) || hasFlags(style, (SWT.CHECK | SWT.WRAP)) || hasFlags(style, SWT.RADIO) || hasFlags(style, (SWT.RADIO | SWT.FLAT)) || hasFlags(style, (SWT.RADIO | SWT.WRAP))) {
            m.text = computeText(widget, m, CHECK.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? CHECK.HORIZONTAL_PADDING : 0), CHECK.MIN_WIDTH);
            height = Math.max(Math.max(m.text.y(), m.image.y()) + ((m.text.y() > 0 || m.image.y() > 0) ? CHECK.VERTICAL_PADDING : 0), CHECK.MIN_HEIGHT);
        } else if (hasFlags(style, SWT.PUSH) || hasFlags(style, (SWT.PUSH | SWT.FLAT)) || hasFlags(style, (SWT.PUSH | SWT.WRAP))) {
            m.text = computeText(widget, m, PUSH.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? PUSH.HORIZONTAL_PADDING : 0), PUSH.MIN_WIDTH);
            height = Math.max(Math.max(m.text.y(), m.image.y()) + ((m.text.y() > 0 || m.image.y() > 0) ? PUSH.VERTICAL_PADDING : 0), PUSH.MIN_HEIGHT);
        } else if (hasFlags(style, SWT.TOGGLE) || hasFlags(style, (SWT.TOGGLE | SWT.FLAT)) || hasFlags(style, (SWT.TOGGLE | SWT.WRAP))) {
            m.text = computeText(widget, m, TOGGLE.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? TOGGLE.HORIZONTAL_PADDING : 0), TOGGLE.MIN_WIDTH);
            height = Math.max(Math.max(m.text.y(), m.image.y()) + ((m.text.y() > 0 || m.image.y() > 0) ? TOGGLE.VERTICAL_PADDING : 0), TOGGLE.MIN_HEIGHT);
        } else { // ARROW, ARROW|FLAT, ARROW|WRAP
            width = ARROW.MIN_WIDTH;
            height = ARROW.MIN_HEIGHT;
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeImage(DartButton widget) {
        Image image = widget.getImage();
        if (image != null) {
            return ImageMetricUtil.getImageSize(image.getImageData());
        }
        return PointD.zero;
    }

    private static PointD computeText(DartButton widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = ButtonTheme.get().textStyle().withStyleFrom(widget.getFont());
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

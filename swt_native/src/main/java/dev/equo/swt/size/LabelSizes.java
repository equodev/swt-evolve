package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.ImageMetricUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartLabel;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Label widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class LabelSizes {

    static class HORIZONTAL {
        static final double MIN_WIDTH = 8.0;
        static final double MIN_HEIGHT = 4.0;
        static final double HORIZONTAL_PADDING = 8.0;
        static final double VERTICAL_PADDING = 4.0;
        static final double IMAGE_SPACING = 8.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class HORIZONTAL_SEPARATOR {
        static final double MIN_WIDTH = 7.0;
        static final double MIN_HEIGHT = 1.0;
    }

    static class VERTICAL {
        static final double MIN_WIDTH = 8.0;
        static final double MIN_HEIGHT = 4.0;
        static final double HORIZONTAL_PADDING = 8.0;
        static final double VERTICAL_PADDING = 4.0;
        static final double IMAGE_SPACING = 8.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class VERTICAL_SEPARATOR {
        static final double MIN_WIDTH = 1.0;
        static final double MIN_HEIGHT = 7.0;
    }

    public static Point computeSize(DartLabel widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartLabel widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, (SWT.HORIZONTAL | SWT.SEPARATOR))) {
            width = HORIZONTAL_SEPARATOR.MIN_WIDTH;
            height = HORIZONTAL_SEPARATOR.MIN_HEIGHT;
        } else if (hasFlags(style, (SWT.VERTICAL | SWT.SEPARATOR))) {
            width = VERTICAL_SEPARATOR.MIN_WIDTH;
            height = VERTICAL_SEPARATOR.MIN_HEIGHT;
        } else if (hasFlags(style, SWT.VERTICAL)) {
            m.text = computeText(widget, m, VERTICAL.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = Math.max((m.text.y() + m.image.x() + (m.image.x() > 0 ? VERTICAL.IMAGE_SPACING : 0)) + (m.text.y() > 0 ? VERTICAL.HORIZONTAL_PADDING : 0), VERTICAL.MIN_WIDTH);
            height = Math.max(Math.max(m.text.x(), m.image.y()) + ((m.text.x() > 0 || m.image.y() > 0) ? VERTICAL.VERTICAL_PADDING : 0), VERTICAL.MIN_HEIGHT);
        } else { // HORIZONTAL
            m.text = computeText(widget, m, HORIZONTAL.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = Math.max((m.text.x() + m.image.x() + (m.image.x() > 0 ? HORIZONTAL.IMAGE_SPACING : 0)) + (m.text.x() > 0 ? HORIZONTAL.HORIZONTAL_PADDING : 0), HORIZONTAL.MIN_WIDTH);
            height = Math.max(Math.max(m.text.y(), m.image.y()) + ((m.text.y() > 0 || m.image.y() > 0) ? HORIZONTAL.VERTICAL_PADDING : 0), HORIZONTAL.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeImage(DartLabel widget) {
        Image image = widget.getImage();
        if (image != null) {
            return ImageMetricUtil.getImageSize(image.getImageData());
        }
        return PointD.zero;
    }

    private static PointD computeText(DartLabel widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = LabelTheme.get().textStyle();
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartText;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Text widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class TextSizes {

    static class LEFT {
        static final double MIN_WIDTH = 27.0;
        static final double MIN_HEIGHT = 27.0;
        static final double HORIZONTAL_PADDING = 24.0;
        static final double VERTICAL_PADDING = 12.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    public static Point computeSize(DartText widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartText widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        m.text = computeText(widget, m, LEFT.EMPTY_TEXT_AFFECTS_SIZING);
        width = Math.max(m.text.x() + (m.text.x() > 0 ? LEFT.HORIZONTAL_PADDING : 0), LEFT.MIN_WIDTH);
        height = Math.max(m.text.y() + LEFT.VERTICAL_PADDING, LEFT.MIN_HEIGHT);

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeText(DartText widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && hasFlags(widget.getStyle(), SWT.PASSWORD)) {
            text = "*".repeat(text.length());
        }
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = TextTheme.get().textStyle();
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

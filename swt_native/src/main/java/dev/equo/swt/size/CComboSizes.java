package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.custom.DartCCombo;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for CCombo widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class CComboSizes {

    static class BORDER {
        static final double MIN_WIDTH = 66.0;
        static final double MIN_HEIGHT = 54.0;
        static final double HORIZONTAL_PADDING = 68.66666666666667;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    static class FLAT {
        static final double MIN_WIDTH = 62.0;
        static final double MIN_HEIGHT = 50.0;
        static final double HORIZONTAL_PADDING = 64.66666666666667;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    public static Point computeSize(DartCCombo widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartCCombo widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.READ_ONLY) || hasFlags(style, SWT.FLAT) || hasFlags(style, SWT.LEAD) || hasFlags(style, SWT.LEFT) || hasFlags(style, SWT.CENTER) || hasFlags(style, SWT.TRAIL) || hasFlags(style, SWT.RIGHT)) {
            m.text = computeText(widget, m, FLAT.EMPTY_TEXT_AFFECTS_SIZING);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? FLAT.HORIZONTAL_PADDING : 0), FLAT.MIN_WIDTH);
            height = Math.max(m.text.y(), FLAT.MIN_HEIGHT);
        } else { // BORDER
            m.text = computeText(widget, m, BORDER.EMPTY_TEXT_AFFECTS_SIZING);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? BORDER.HORIZONTAL_PADDING : 0), BORDER.MIN_WIDTH);
            height = Math.max(m.text.y(), BORDER.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeText(DartCCombo widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = CComboTheme.get().textStyle().withStyleFrom(widget.getFont());
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

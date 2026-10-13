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

    private TextSizes() {
    }

    static class LEFT {
        static final double MIN_WIDTH = 35.0;
        static final double MIN_HEIGHT = 17.0;
        static final double HORIZONTAL_PADDING = 32.0;
        static final double VERTICAL_PADDING = 5.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;

        private LEFT() {
        }
    }

    static class LEFT_WRAP {
        static final double MIN_WIDTH = 35.0;
        static final double MIN_HEIGHT = 17.0;
        static final double HORIZONTAL_PADDING = 32.0;
        static final double VERTICAL_PADDING = 5.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;

        private LEFT_WRAP() {
        }
    }

    public static Point computeSize(DartText widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartText widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.WRAP)) {
            m.text = computeText(widget, m, LEFT_WRAP.EMPTY_TEXT_AFFECTS_SIZING);
            double naturalWidth = Math.max(m.text.x() + (m.text.x() > 0 ? LEFT_WRAP.HORIZONTAL_PADDING : 0), LEFT_WRAP.MIN_WIDTH);
            width = wHint != SWT.DEFAULT ? wHint : naturalWidth;
            boolean wraps = hasFlags(style, SWT.WRAP);
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            } else if (wHint != SWT.DEFAULT && wraps && m.textStyle != null) {
                double availableWidth = Math.max(1.0, wHint - LEFT_WRAP.HORIZONTAL_PADDING);
                PointD wrapped = FontMetricsUtil.getFontSizeWrapped(Drawn.text(widget), m.textStyle, availableWidth);
                wrapped = new PointD(wrapped.x(), Math.max(wrapped.y(), m.text.y()));
                height = Math.max(wrapped.y() + LEFT_WRAP.VERTICAL_PADDING, LEFT_WRAP.MIN_HEIGHT);
            } else {
                height = Math.max(m.text.y() + LEFT_WRAP.VERTICAL_PADDING, LEFT_WRAP.MIN_HEIGHT);
            }
        } else { // LEFT, LEFT|PASSWORD, LEFT|SEARCH, LEFT|READ_ONLY, CENTER, CENTER|PASSWORD, CENTER|SEARCH, CENTER|READ_ONLY, RIGHT, RIGHT|PASSWORD, RIGHT|SEARCH, RIGHT|READ_ONLY, MULTI, MULTI|PASSWORD, MULTI|SEARCH, MULTI|READ_ONLY, MULTI|LEFT, MULTI|LEFT|PASSWORD, MULTI|LEFT|SEARCH, MULTI|LEFT|READ_ONLY, MULTI|CENTER, MULTI|CENTER|PASSWORD, MULTI|CENTER|SEARCH, MULTI|CENTER|READ_ONLY, MULTI|RIGHT, MULTI|RIGHT|PASSWORD, MULTI|RIGHT|SEARCH, MULTI|RIGHT|READ_ONLY, SINGLE, SINGLE|PASSWORD, SINGLE|SEARCH, SINGLE|READ_ONLY, SINGLE|LEFT, SINGLE|LEFT|PASSWORD, SINGLE|LEFT|SEARCH, SINGLE|LEFT|READ_ONLY, SINGLE|CENTER, SINGLE|CENTER|PASSWORD, SINGLE|CENTER|SEARCH, SINGLE|CENTER|READ_ONLY, SINGLE|RIGHT, SINGLE|RIGHT|PASSWORD, SINGLE|RIGHT|SEARCH, SINGLE|RIGHT|READ_ONLY
            m.text = computeText(widget, m, LEFT.EMPTY_TEXT_AFFECTS_SIZING);
            double naturalWidth = Math.max(m.text.x() + (m.text.x() > 0 ? LEFT.HORIZONTAL_PADDING : 0), LEFT.MIN_WIDTH);
            width = wHint != SWT.DEFAULT ? wHint : naturalWidth;
            height = hHint != SWT.DEFAULT ? hHint : Math.max(m.text.y() + LEFT.VERTICAL_PADDING, LEFT.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeText(DartText widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = Drawn.text(widget);
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts || widget.getExplicitFont() == null) {
                m.textStyle = TextTheme.get().textStyle().withStyleFrom(widget.getFont());
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

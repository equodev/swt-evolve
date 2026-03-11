package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartCombo;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Combo widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class ComboSizes {

    static class DROP_DOWN {
        static final double MIN_WIDTH = 52.0;
        static final double MIN_HEIGHT = 31.0;
        static final double HORIZONTAL_PADDING = 53.333333333333336;
        static final double VERTICAL_PADDING = 18.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    static class SIMPLE {
        static final double MIN_WIDTH = 24.0;
        static final double MIN_HEIGHT = 32.0;
        static final double HORIZONTAL_PADDING = 25.333333333333332;
        static final double VERTICAL_PADDING = 19.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    public static Point computeSize(DartCombo widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartCombo widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.SIMPLE) || hasFlags(style, (SWT.SIMPLE | SWT.READ_ONLY))) {
            m.text = computeText(widget, m, SIMPLE.EMPTY_TEXT_AFFECTS_SIZING, wHint);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? SIMPLE.HORIZONTAL_PADDING : 0), SIMPLE.MIN_WIDTH);
            height = Math.max(m.text.y() + SIMPLE.VERTICAL_PADDING, SIMPLE.MIN_HEIGHT);
        } else { // DROP_DOWN, DROP_DOWN|READ_ONLY
            m.text = computeText(widget, m, DROP_DOWN.EMPTY_TEXT_AFFECTS_SIZING, wHint);
            width = Math.max(m.text.x() + (m.text.x() > 0 ? DROP_DOWN.HORIZONTAL_PADDING : 0), DROP_DOWN.MIN_WIDTH);
            height = Math.max(m.text.y() + DROP_DOWN.VERTICAL_PADDING, DROP_DOWN.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }
    private static PointD computeText(DartCombo widget, Measure m, boolean emptyTextAffectsSizing, int wHint) {
        if (widget.isDisposed()) return PointD.zero;
        String text = widget.getText();
        try {
            if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
                m.textStyle = ComboTheme.get().textStyle();
                if (!Config.getConfigFlags().use_swt_fonts) {
                    m.textStyle = m.textStyle.withStyleFrom(widget.getFont());
                    return FontMetricsUtil.getFontSize(text, m.textStyle);
                } else {
                    org.eclipse.swt.graphics.Font font = widget.getFont();
                    if (font != null && !font.isDisposed()) {
                        m.textStyle = TextStyle.from(font);
                        org.eclipse.swt.graphics.TextLayout layout = new org.eclipse.swt.graphics.TextLayout(org.eclipse.swt.widgets.Display.getDefault());
                        layout.setFont(font);
                        layout.setText(text);
                        if (hasFlags(widget.getStyle(), org.eclipse.swt.SWT.WRAP) && wHint > 0 && wHint != org.eclipse.swt.SWT.DEFAULT) {
                            layout.setWidth(wHint);
                        }
                        org.eclipse.swt.graphics.Rectangle bounds = layout.getBounds();
                        layout.dispose();
                        return new PointD(bounds.width, bounds.height);
                    }
                }
            }
            return PointD.zero;
        } catch (Exception e) {
            m.textStyle = m.textStyle.withStyleFrom(widget.getFont());
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
    }
}

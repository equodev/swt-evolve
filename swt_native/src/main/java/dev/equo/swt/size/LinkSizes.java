package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartLink;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for Link widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class LinkSizes {

    static class NONE {
        static final double MIN_WIDTH = 0.0;
        static final double MIN_HEIGHT = 18.0;
        static final double HORIZONTAL_PADDING = 0.0;
        static final double VERTICAL_PADDING = 4.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    public static Point computeSize(DartLink widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartLink widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        m.text = computeText(widget, m, NONE.EMPTY_TEXT_AFFECTS_SIZING, wHint);
        width = Math.max(m.text.x() + (m.text.x() > 0 ? NONE.HORIZONTAL_PADDING : 0), NONE.MIN_WIDTH);
        height = Math.max(m.text.y() + NONE.VERTICAL_PADDING, NONE.MIN_HEIGHT);

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }
    private static PointD computeText(DartLink widget, Measure m, boolean emptyTextAffectsSizing, int wHint) {
        if (widget.isDisposed()) return PointD.zero;
        String text = widget.getText();
        try {
            if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
                m.textStyle = LinkTheme.get().textStyle();
                if (!Config.getConfigFlags().use_swt_fonts) {
                    m.textStyle = m.textStyle.withStyleFrom(widget.getFont());
                    return FontMetricsUtil.getFontSize(text, m.textStyle);
                } else {
                    org.eclipse.swt.graphics.Font font = widget.getFont();
                    if (font != null && !font.isDisposed()) {
                        m.textStyle = TextStyle.from(font);
                        org.eclipse.swt.graphics.TextLayout layout = new org.eclipse.swt.graphics.TextLayout(org.eclipse.swt.widgets.Display.getDefault());
                        layout.setFont(font);
                        layout.setText(text.replaceAll("<[^>]*>", "").replace("&", ""));
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

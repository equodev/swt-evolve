package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTableItem;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for TableItem widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class TableItemSizes {

    static class NONE {
        static final double MIN_WIDTH = 16.0;
        static final double MIN_HEIGHT = 20.0;
        static final double HORIZONTAL_PADDING = 16.0;
        static final double VERTICAL_PADDING = 8.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = true;
    }

    public static Point computeSize(DartTableItem widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartTableItem widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        m.text = computeText(widget, m, NONE.EMPTY_TEXT_AFFECTS_SIZING);
        m.image = computeImage(widget, m.textStyle);
        width = wHint != SWT.DEFAULT ? wHint : Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? NONE.HORIZONTAL_PADDING : 0), NONE.MIN_WIDTH);
        if (hHint != SWT.DEFAULT) {
            height = hHint;
        } else if (wHint != SWT.DEFAULT && m.textStyle != null) {
            String rawText = widget.getText();
            String visualText = rawText != null ? rawText.replaceAll("<[^>]+>", "") : "";
            double availableWidth = Math.max(1.0, wHint - NONE.HORIZONTAL_PADDING);
            PointD wrapped = FontMetricsUtil.getFontSizeWrapped(visualText, m.textStyle, availableWidth);
            height = Math.max(wrapped.y() + NONE.VERTICAL_PADDING, NONE.MIN_HEIGHT);
        } else {
            height = Math.max(Math.max(m.text.y(), m.image.y()) + NONE.VERTICAL_PADDING, NONE.MIN_HEIGHT);
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    private static PointD computeImage(DartTableItem widget, TextStyle ts) {
        if (widget.getImage() == null) return PointD.zero;
        double iconSize = ts != null ? (double) ts.size() : 16.0;
        return new PointD(iconSize, iconSize);
    }

    private static PointD computeText(DartTableItem widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts) {
                m.textStyle = TableItemTheme.get().textStyle().withStyleFrom(widget.getFont());
            } else {
                m.textStyle = TextStyle.from(widget.getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

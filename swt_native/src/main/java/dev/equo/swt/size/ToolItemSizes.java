package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartToolItem;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for ToolItem widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class ToolItemSizes {

    static class CHECK {
        static final double MIN_WIDTH = 24.0;
        static final double MIN_HEIGHT = 20.0;
        static final double HORIZONTAL_PADDING = 28.0;
        static final double VERTICAL_PADDING = 0.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class PUSH {
        static final double MIN_WIDTH = 24.0;
        static final double MIN_HEIGHT = 20.0;
        static final double HORIZONTAL_PADDING = 12.0;
        static final double VERTICAL_PADDING = 0.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class SEPARATOR {
        static final double MIN_WIDTH = 20.0;
        static final double MIN_HEIGHT = 23.0;
        static final double HORIZONTAL_PADDING = 12.0;
        static final double VERTICAL_PADDING = 0.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    static class DROP_DOWN {
        static final double MIN_WIDTH = 41.0;
        static final double MIN_HEIGHT = 20.0;
        static final double HORIZONTAL_PADDING = 29.0;
        static final double VERTICAL_PADDING = 0.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    public static Point computeSize(DartToolItem widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartToolItem widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        if (hasFlags(style, SWT.PUSH)) {
            m.text = computeText(widget, m, PUSH.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = wHint != SWT.DEFAULT ? wHint : Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? PUSH.HORIZONTAL_PADDING : 0), PUSH.MIN_WIDTH);
            boolean wraps = hasFlags(style, SWT.WRAP);
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            } else if (wHint != SWT.DEFAULT && wraps && m.textStyle != null) {
                double imageWidth = m.image.x();
                double imageSpacing = 0;
                double availableWidth = Math.max(1.0, wHint - ((m.text.x() > 0 || imageWidth > 0) ? PUSH.HORIZONTAL_PADDING : 0) - imageWidth - imageSpacing);
                PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, availableWidth);
                height = Math.max(wrapped.y() + PUSH.VERTICAL_PADDING, PUSH.MIN_HEIGHT);
            } else {
                height = Math.max(m.text.y() + (m.text.y() > 0 ? PUSH.VERTICAL_PADDING : 0), PUSH.MIN_HEIGHT);
            }
        } else if (hasFlags(style, SWT.SEPARATOR)) {
            m.text = computeText(widget, m, SEPARATOR.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = wHint != SWT.DEFAULT ? wHint : Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? SEPARATOR.HORIZONTAL_PADDING : 0), SEPARATOR.MIN_WIDTH);
            boolean wraps = hasFlags(style, SWT.WRAP);
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            } else if (wHint != SWT.DEFAULT && wraps && m.textStyle != null) {
                double imageWidth = m.image.x();
                double imageSpacing = 0;
                double availableWidth = Math.max(1.0, wHint - ((m.text.x() > 0 || imageWidth > 0) ? SEPARATOR.HORIZONTAL_PADDING : 0) - imageWidth - imageSpacing);
                PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, availableWidth);
                height = Math.max(wrapped.y() + SEPARATOR.VERTICAL_PADDING, SEPARATOR.MIN_HEIGHT);
            } else {
                height = Math.max(m.text.y() + (m.text.y() > 0 ? SEPARATOR.VERTICAL_PADDING : 0), SEPARATOR.MIN_HEIGHT);
            }
        } else if (hasFlags(style, SWT.DROP_DOWN)) {
            m.text = computeText(widget, m, DROP_DOWN.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = wHint != SWT.DEFAULT ? wHint : Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? DROP_DOWN.HORIZONTAL_PADDING : 0), DROP_DOWN.MIN_WIDTH);
            boolean wraps = hasFlags(style, SWT.WRAP);
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            } else if (wHint != SWT.DEFAULT && wraps && m.textStyle != null) {
                double imageWidth = m.image.x();
                double imageSpacing = 0;
                double availableWidth = Math.max(1.0, wHint - ((m.text.x() > 0 || imageWidth > 0) ? DROP_DOWN.HORIZONTAL_PADDING : 0) - imageWidth - imageSpacing);
                PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, availableWidth);
                height = Math.max(wrapped.y() + DROP_DOWN.VERTICAL_PADDING, DROP_DOWN.MIN_HEIGHT);
            } else {
                height = Math.max(m.text.y() + (m.text.y() > 0 ? DROP_DOWN.VERTICAL_PADDING : 0), DROP_DOWN.MIN_HEIGHT);
            }
        } else { // CHECK, RADIO
            m.text = computeText(widget, m, CHECK.EMPTY_TEXT_AFFECTS_SIZING);
            m.image = computeImage(widget);
            width = wHint != SWT.DEFAULT ? wHint : Math.max((m.text.x() + m.image.x()) + ((m.text.x() > 0 || m.image.x() > 0) ? CHECK.HORIZONTAL_PADDING : 0), CHECK.MIN_WIDTH);
            boolean wraps = hasFlags(style, SWT.WRAP);
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            } else if (wHint != SWT.DEFAULT && wraps && m.textStyle != null) {
                double imageWidth = m.image.x();
                double imageSpacing = 0;
                double availableWidth = Math.max(1.0, wHint - ((m.text.x() > 0 || imageWidth > 0) ? CHECK.HORIZONTAL_PADDING : 0) - imageWidth - imageSpacing);
                PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, availableWidth);
                height = Math.max(wrapped.y() + CHECK.VERTICAL_PADDING, CHECK.MIN_HEIGHT);
            } else {
                height = Math.max(m.text.y() + (m.text.y() > 0 ? CHECK.VERTICAL_PADDING : 0), CHECK.MIN_HEIGHT);
            }
        }

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }

    /** The size the icon is drawn at, whatever the size of the image. */
    public static final double ICON_SIZE = 30.0;

    private static PointD computeImage(DartToolItem widget) {
        if (widget.getImage() == null) return PointD.zero;
        return new PointD(ICON_SIZE, ICON_SIZE);
    }

    private static PointD computeText(DartToolItem widget, Measure m, boolean emptyTextAffectsSizing) {
        String text = widget.getText();
        if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
            if (!Config.getConfigFlags().use_swt_fonts || ((DartControl) widget.getParent().getImpl()).getExplicitFont() == null) {
                m.textStyle = ToolItemTheme.get().textStyle().withStyleFrom(((DartControl) widget.getParent().getImpl()).getFont());
            } else {
                m.textStyle = TextStyle.from(((DartControl) widget.getParent().getImpl()).getFont());
            }
            return FontMetricsUtil.getFontSize(text, m.textStyle);
        }
        return PointD.zero;
    }
}

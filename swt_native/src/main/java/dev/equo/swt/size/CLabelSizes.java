package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.ImageMetricUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.custom.DartCLabel;

import static dev.equo.swt.Styles.hasFlags;

/**
 * Auto-generated sizing for CLabel widgets.
 * Generated from Flutter measurements.
 *
 * DO NOT EDIT MANUALLY - regenerate from measure.dart
 */
public class CLabelSizes {

    static class LEFT {
        static final double MIN_WIDTH = 0.0;
        static final double MIN_HEIGHT = 0.0;
        static final double HORIZONTAL_PADDING = 0.0;
        static final double VERTICAL_PADDING = 0.0;
        static final double IMAGE_SPACING = 4.0;
        static final boolean EMPTY_TEXT_AFFECTS_SIZING = false;
    }

    public static Point computeSize(DartCLabel widget, int wHint, int hHint, boolean changed) {
        return computeSizes(widget, wHint, hHint, changed).widget;
    }

    static Measure computeSizes(DartCLabel widget, int wHint, int hHint, boolean changed) {
        int style = widget.getStyle();

        Measure m = new Measure();

        double width, height;

        m.text = computeText(widget, m, LEFT.EMPTY_TEXT_AFFECTS_SIZING, wHint);
        m.image = computeImage(widget);
        width = Math.max((m.text.x() + m.image.x() + (m.image.x() > 0 && m.text.x() > 0 ? LEFT.IMAGE_SPACING : 0)) + ((m.text.x() > 0 || m.image.x() > 0) ? LEFT.HORIZONTAL_PADDING : 0), LEFT.MIN_WIDTH);
        height = Math.max(Math.max(m.text.y(), m.image.y()) + ((m.text.y() > 0 || m.image.y() > 0) ? LEFT.VERTICAL_PADDING : 0), LEFT.MIN_HEIGHT);

        m.widget = new Point((int) Math.ceil(width), (int) Math.ceil(height));
        return m;
    }
    private static PointD computeImage(DartCLabel widget) {
        Image image = widget.getImage();
        if (image != null) {
            return ImageMetricUtil.getImageSize(image.getImageData());
        }
        return PointD.zero;
    }

    private static PointD computeText(DartCLabel widget, Measure m, boolean emptyTextAffectsSizing, int wHint) {
        if (widget.isDisposed()) return PointD.zero;
        String text = widget.getText();
        try {
            if (text != null && (emptyTextAffectsSizing || !text.isEmpty())) {
                m.textStyle = CLabelTheme.get().textStyle();
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

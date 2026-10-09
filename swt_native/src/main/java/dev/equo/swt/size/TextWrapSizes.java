package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartText;

import static dev.equo.swt.Styles.hasFlags;

/**
 * The size of a Text, which breaks its lines at the width it is given when it wraps. {@link TextSizes} is
 * generated from measurements of one line of text; a wrapping Text is as tall as the lines its words fill
 * at the width it is laid out in.
 */
public final class TextWrapSizes {

    private TextWrapSizes() {
    }

    public static Point computeSize(DartText widget, int wHint, int hHint, boolean changed) {
        Measure m = TextSizes.computeSizes(widget, wHint, hHint, changed);
        if (hHint != SWT.DEFAULT || wHint == SWT.DEFAULT || !hasFlags(widget.getStyle(), SWT.WRAP) || widget.getText().isEmpty()) {
            return m.widget;
        }
        double available = Math.max(1.0, wHint - TextSizes.LEFT.HORIZONTAL_PADDING);
        PointD wrapped = FontMetricsUtil.getFontSizeWrapped(widget.getText(), m.textStyle, available);
        double height = Math.max(wrapped.y() + TextSizes.LEFT.VERTICAL_PADDING, TextSizes.LEFT.MIN_HEIGHT);
        return new Point(m.widget.x, (int) Math.ceil(height));
    }
}

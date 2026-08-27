package org.eclipse.swt.graphics;

import org.eclipse.swt.custom.StyledTextHelper;

/** Hand-written support for {@code DartTextLayout}. */
public final class TextLayoutHelper {

    private TextLayoutHelper() {
    }

    /**
     * Metrics of one laid-out line. There is no OS text layout to interrogate, so the line box
     * comes from the same glyph table the renderer sizes text with. That is what keeps a caller
     * aligning to {@code getLineMetrics(...).getAscent()} on the same baseline as one reading
     * {@code StyledTextRenderer.getBaseline()} — StyledText takes the first route only when the
     * line height is not fixed, i.e. under word wrap.
     *
     * <p>{@code minAscent}/{@code minDescent} are the layout's own {@code setAscent}/
     * {@code setDescent} floors, {@code -1} when unset; they raise the box exactly as
     * {@code _effLineHeight()} does, so the reported height stays equal to the line's height.
     */
    public static FontMetrics lineMetrics(Font font, int minAscent, int minDescent) {
        StyledTextHelper.RendererFontMetrics m = StyledTextHelper.calculateFontMetrics(font, 1);
        int ascent = m.ascent;
        int height = m.getLineHeight();
        if (minAscent >= 0 && minDescent >= 0 && minAscent + minDescent > height) {
            ascent = minAscent;
            height = minAscent + minDescent;
        }
        DartFontMetrics metrics = new DartFontMetrics(null);
        metrics.setMetrics(ascent, height - ascent, height, m.averageCharWidth);
        return metrics.getApi();
    }
}

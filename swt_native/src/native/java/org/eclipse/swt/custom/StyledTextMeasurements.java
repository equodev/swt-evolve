package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.DartTextLayout;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextLayoutMeasurement;

/** Hands a line's {@link TextLayout} the layout the render side pushed for that line. */
public final class StyledTextMeasurements {

    private StyledTextMeasurements() {
    }

    public static void attach(StyledText styledText, int lineIndex, TextLayout layout) {
        if (styledText == null || layout == null || !(styledText.getImpl() instanceof DartStyledText)
                || !(layout.getImpl() instanceof DartTextLayout))
            return;
        ((DartTextLayout) layout.getImpl()).setMeasurement(
                measurement((DartStyledText) styledText.getImpl(), lineIndex));
    }

    static TextLayoutMeasurement measurement(DartStyledText styledText, int lineIndex) {
        StyledTextHelper.VisualLine[] rows = StyledTextHelper.rowsOfLine(styledText, lineIndex);
        if (rows == null)
            return null;
        int lineStart = rows[0].start;
        int length = rows[rows.length - 1].end - lineStart;
        int[] rowStarts = new int[rows.length];
        double[] x = new double[rows.length], y = new double[rows.length];
        double[] width = new double[rows.length], height = new double[rows.length];
        double[][] charX = new double[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            StyledTextHelper.VisualLine row = rows[i];
            rowStarts[i] = row.start - lineStart;
            x[i] = row.x;
            y[i] = row.y - rows[0].y;
            width[i] = row.w;
            height[i] = row.h;
            // Positions come only for rows near the viewport; a row without them spreads its
            // measured width over its characters rather than losing its real top and height.
            charX[i] = row.charX != null ? row.charX : evenlySpread(row);
        }
        return new TextLayoutMeasurement(length, rowStarts, x, y, width, height, charX, (int) Math.round(rows[0].vi));
    }

    private static double[] evenlySpread(StyledTextHelper.VisualLine row) {
        int count = Math.max(0, row.end - row.start);
        double[] xs = new double[count + 1];
        for (int k = 0; k <= count; k++) xs[k] = row.x + (count == 0 ? 0 : row.w * k / count);
        return xs;
    }
}

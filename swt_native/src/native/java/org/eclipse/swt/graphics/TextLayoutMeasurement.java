package org.eclipse.swt.graphics;

/**
 * One paragraph as the render side laid it out, so a {@link TextLayout} answers position queries from
 * what is painted. Layout coordinates; the first row's glyphs sit {@code verticalIndent} below the top.
 */
public final class TextLayoutMeasurement {

    private final int length;
    /** Offset each row starts at, plus the paragraph length: {@code rowCount + 1} entries. */
    private final int[] rowStarts;
    private final double[] rowX, rowY, rowWidth, rowHeight;
    private final double[][] charX;
    private final int verticalIndent;

    /**
     * @param rowStarts the offset each row starts at, relative to the paragraph, ascending
     * @param charX per row, the x of each boundary from the row's start to its end, inclusive
     */
    public TextLayoutMeasurement(int length, int[] rowStarts, double[] rowX, double[] rowY,
            double[] rowWidth, double[] rowHeight, double[][] charX, int verticalIndent) {
        this.length = length;
        this.rowStarts = new int[rowStarts.length + 1];
        System.arraycopy(rowStarts, 0, this.rowStarts, 0, rowStarts.length);
        this.rowStarts[rowStarts.length] = length;
        this.rowX = rowX;
        this.rowY = rowY;
        this.rowWidth = rowWidth;
        this.rowHeight = rowHeight;
        this.charX = charX;
        this.verticalIndent = verticalIndent;
    }

    /** Whether this describes a paragraph of {@code text}'s length. */
    boolean fits(String text) {
        return text != null && text.length() == length;
    }

    int[] lineOffsets() {
        return rowStarts.clone();
    }

    int rowCount() {
        return rowStarts.length - 1;
    }

    /** The row holding {@code offset}; an offset that ends one row and starts the next is the next's. */
    int rowOf(int offset) {
        for (int row = rowCount() - 1; row > 0; row--) {
            if (offset >= rowStarts[row]) return row;
        }
        return 0;
    }

    /** Top of the row's glyphs. */
    private double top(int row) {
        return row == 0 ? verticalIndent : rowY[row];
    }

    private double height(int row) {
        return row == 0 ? rowHeight[0] - verticalIndent : rowHeight[row];
    }

    Rectangle bounds(int wrapWidth) {
        double width = 0;
        for (int row = 0; row < rowCount(); row++) width = Math.max(width, rowX[row] + rowWidth[row]);
        int last = rowCount() - 1;
        int w = wrapWidth != -1 ? wrapWidth : (int) Math.ceil(width);
        return new Rectangle(0, 0, w, (int) Math.round(rowY[last] + rowHeight[last]));
    }

    Rectangle lineBounds(int row) {
        return new Rectangle((int) Math.round(rowX[row]), (int) Math.round(top(row)),
                (int) Math.ceil(rowWidth[row]), (int) Math.round(height(row)));
    }

    Point location(int offset, boolean trailing) {
        int row = rowOf(offset);
        int index = offset - rowStarts[row];
        double[] xs = charX[row];
        if (trailing && index + 1 < xs.length) index++;
        index = Math.max(0, Math.min(index, xs.length - 1));
        return new Point((int) Math.round(xs[index]), (int) Math.round(top(row)));
    }

    /** The character at a point, with {@code trailing[0]} set when the point is past its middle. */
    int offset(int x, int y, int[] trailing) {
        if (trailing != null) trailing[0] = 0;
        int row = 0;
        while (row + 1 < rowCount() && y >= rowY[row + 1]) row++;
        int start = rowStarts[row], end = rowStarts[row + 1];
        if (end == start) return start;
        double[] xs = charX[row];
        for (int c = 0; c < end - start; c++) {
            if (x >= xs[c] && x < xs[c + 1]) {
                if (trailing != null && x >= (xs[c] + xs[c + 1]) / 2) trailing[0] = 1;
                return start + c;
            }
        }
        if (x >= xs[end - start]) {
            if (trailing != null) trailing[0] = 1;
            return end - 1;
        }
        return start;
    }
}

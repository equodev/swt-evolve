package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The position API looks rows up in the render side's table by halving it rather than walking it,
 * as JFace's rulers ask for every visible line on every paint. A table of a few hundred lines, some
 * wrapped onto two rows and some taller than the rest, answers every lookup as walking the rows does.
 */
@Tag("native-unit")
class StyledTextGeometryLookupNativeTest {

    private static final int LINES = 300;
    private static final int LINE_LENGTH = 4;

    private final List<double[]> rows = new ArrayList<>(); // logicalLine, start, end, y, h
    private double contentHeight;

    @Test
    void everyLookupAnswersAsWalkingTheRows() {
        DartStyledText st = mock(DartStyledText.class);
        st.content = mock(StyledTextContent.class);
        int charCount = LINES * (LINE_LENGTH + 1) - 1;
        when(st.getCharCount()).thenReturn(charCount);
        when(st.getVerticalScrollOffset()).thenReturn(0);
        StyledTextHelper.applyTextGeometry(st, table());

        for (int line = 0; line <= LINES; line++) {
            assertThat(StyledTextHelper.geometryLinePixel(st, line)).as("pixel of line %d", line)
                    .isEqualTo((int) Math.round(walkLinePixel(line)));
        }
        for (int y = -3; y < contentHeight + 3; y++) {
            assertThat(StyledTextHelper.geometryLineIndex(st, y)).as("line at y %d", y).isEqualTo(walkLineIndex(y));
        }
        for (int offset = 0; offset <= charCount; offset++) {
            Point point = StyledTextHelper.geometryPointAtOffset(st, offset);
            int row = walkRowOf(offset);
            assertThat(point == null ? null : point.y).as("row of offset %d", offset)
                    .isEqualTo(row < 0 ? null : (int) Math.round(rows.get(row)[3]));
        }
    }

    @Test
    void theTopIndexAtEveryScrollOffsetAnswersAsWalkingTheRows() {
        DartStyledText st = mock(DartStyledText.class);
        st.content = mock(StyledTextContent.class);
        st.clientAreaHeight = 100;
        when(st.getCharCount()).thenReturn(LINES * (LINE_LENGTH + 1) - 1);
        when(st.getVerticalScrollOffset()).thenReturn(0);
        StyledTextHelper.applyTextGeometry(st, table());

        for (int top = 0; top < contentHeight; top++) {
            when(st.getVerticalScrollOffset()).thenReturn(top);
            assertThat(StyledTextHelper.geometryTopIndex(st)).as("top index scrolled to %d", top)
                    .isEqualTo(walkTopIndex(top, st.clientAreaHeight));
        }
    }

    /** The first line whose first row starts at or below the offset and ends within the view, else the line at the top edge. */
    private int walkTopIndex(int top, int viewHeight) {
        for (int i = 0; i < rows.size(); i++) {
            double[] r = rows.get(i);
            boolean firstRow = i == 0 || rows.get(i - 1)[0] != r[0];
            if (!firstRow || Math.round(r[3]) < top) continue;
            double bottom = contentHeight;
            for (int k = i + 1; k < rows.size(); k++) {
                if (rows.get(k)[0] != r[0]) {
                    bottom = rows.get(k)[3];
                    break;
                }
            }
            if (bottom - top <= viewHeight) return (int) r[0];
            break;
        }
        return walkLineIndex(top);
    }

    /** Every fifth line wraps onto two rows of two characters, every seventh is taller than the rest. */
    private Map<String, Object> table() {
        List<Map<String, Object>> lines = new ArrayList<>();
        double y = 0;
        for (int line = 0; line < LINES; line++) {
            int start = line * (LINE_LENGTH + 1);
            double h = line % 7 == 0 ? 20 : 15;
            if (line % 5 == 0) {
                y = row(lines, line, start, start + 2, y, h);
                y = row(lines, line, start + 2, start + LINE_LENGTH, y, h);
            } else {
                y = row(lines, line, start, start + LINE_LENGTH, y, h);
            }
        }
        contentHeight = y;
        Map<String, Object> table = new HashMap<>();
        table.put("charCount", LINES * (LINE_LENGTH + 1) - 1);
        table.put("contentWidth", 40.0);
        table.put("contentHeight", y);
        table.put("lines", lines);
        return table;
    }

    private double row(List<Map<String, Object>> lines, int line, int start, int end, double y, double h) {
        List<Double> cx = new ArrayList<>();
        for (int i = 0; i <= end - start; i++) cx.add(i * 10.0);
        lines.add(Map.of("l", line, "s", start, "e", end, "x", 0.0, "y", y, "w", (end - start) * 10.0, "h", h, "cx", cx));
        rows.add(new double[] { line, start, end, y, h });
        return y + h;
    }

    private double walkLinePixel(int line) {
        if (line > rows.get(rows.size() - 1)[0]) return contentHeight;
        for (double[] r : rows) if (r[0] == line) return r[3];
        throw new AssertionError("no row for line " + line);
    }

    private int walkLineIndex(double y) {
        if (y < 0) return 0;
        for (double[] r : rows) if (y < r[3] + r[4]) return (int) r[0];
        return (int) rows.get(rows.size() - 1)[0];
    }

    private int walkRowOf(int offset) {
        int found = -1;
        for (int i = 0; i < rows.size(); i++) {
            double[] r = rows.get(i);
            if (offset < r[1]) break;
            if (offset <= r[2]) {
                found = i;
                if (offset < r[2]) break;
            }
        }
        return found;
    }
}

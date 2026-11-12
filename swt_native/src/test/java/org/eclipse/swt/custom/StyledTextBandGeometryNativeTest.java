package org.eclipse.swt.custom;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * With every line one row of one height, the client describes only the band around the view; every
 * other row follows from the row height and the content's line starts. A band answers every lookup
 * as the table of every row does.
 */
@Tag("native-unit")
class StyledTextBandGeometryNativeTest {

    private static final int LINES = 1000;
    private static final int LINE_LENGTH = 4;
    /** Not a whole hundredth apart from row to row, so the rows' rounding shows. */
    private static final double ROW_HEIGHT = 16.4;
    private static final int BAND_FIRST = 400, BAND_LAST = 460;
    private static final int CHAR_COUNT = LINES * (LINE_LENGTH + 1) - 1;
    private static final double CONTENT_HEIGHT = round(LINES * ROW_HEIGHT);

    @Test
    void aBandAnswersEveryLookupAsEveryRowDoes() {
        DartStyledText band = editor(), whole = editor();
        StyledTextHelper.applyTextGeometry(band, table(true));
        StyledTextHelper.applyTextGeometry(whole, table(false));

        for (int line = 0; line <= LINES; line++) {
            assertThat(StyledTextHelper.geometryLinePixel(band, line)).as("pixel of line %d", line)
                    .isEqualTo(StyledTextHelper.geometryLinePixel(whole, line));
        }
        for (int line = 0; line < LINES; line++) {
            assertThat(StyledTextHelper.measuredLineTop(band, line)).as("top of line %d", line)
                    .isEqualTo(StyledTextHelper.measuredLineTop(whole, line));
            assertThat(StyledTextHelper.measuredLineHeight(band, line)).as("height of line %d", line)
                    .isEqualTo(StyledTextHelper.measuredLineHeight(whole, line));
        }
        for (int y = -3; y < CONTENT_HEIGHT + 3; y++) {
            assertThat(StyledTextHelper.geometryLineIndex(band, y)).as("line at y %d", y)
                    .isEqualTo(StyledTextHelper.geometryLineIndex(whole, y));
        }
        for (int offset = 0; offset <= CHAR_COUNT; offset++) {
            assertThat(StyledTextHelper.geometryPointAtOffset(band, offset)).as("location of offset %d", offset)
                    .isEqualTo(StyledTextHelper.geometryPointAtOffset(whole, offset));
        }
        assertThat(StyledTextHelper.geometryPointAtOffset(band, BAND_FIRST * (LINE_LENGTH + 1) + 2))
                .as("a character in the band").isNotNull();
        assertThat(StyledTextHelper.measuredContentHeight(band)).isEqualTo(StyledTextHelper.measuredContentHeight(whole));
    }

    @Test
    void theTopIndexOfABandAnswersAsEveryRowDoes() {
        DartStyledText band = editor(), whole = editor();
        band.clientAreaHeight = whole.clientAreaHeight = 100;
        StyledTextHelper.applyTextGeometry(band, table(true));
        StyledTextHelper.applyTextGeometry(whole, table(false));

        for (int top = 0; top < CONTENT_HEIGHT; top += 7) {
            when(band.getVerticalScrollOffset()).thenReturn(top);
            when(whole.getVerticalScrollOffset()).thenReturn(top);
            assertThat(StyledTextHelper.geometryTopIndex(band)).as("top index scrolled to %d", top)
                    .isEqualTo(StyledTextHelper.geometryTopIndex(whole));
        }
    }

    @Test
    void aLineOutsideTheBandLeavesItsLayoutToMeasureItself() {
        DartStyledText band = editor();
        StyledTextHelper.applyTextGeometry(band, table(true));

        assertThat(StyledTextMeasurements.measurement(band, BAND_FIRST)).as("a line in the band").isNotNull();
        assertThat(StyledTextMeasurements.measurement(band, BAND_FIRST - 1)).as("the line above the band").isNull();
        assertThat(StyledTextMeasurements.measurement(band, BAND_LAST + 1)).as("the line below the band").isNull();
    }

    @Test
    void aVerticalIndentDropsABand() {
        DartStyledText band = editor();
        StyledTextHelper.applyTextGeometry(band, table(true));

        StyledTextHelper.applyLineVerticalIndentDelta(band, BAND_FIRST + 10, 9);

        // The rows below it no longer follow from the row height, so no lookup may answer from them.
        assertThat(StyledTextHelper.geometryLinePixel(band, BAND_LAST + 100)).as("a line below the band").isNull();
        assertThat(StyledTextHelper.geometryLinePixel(band, BAND_FIRST + 20)).as("a line in the band").isNull();
    }

    @Test
    void eachEditorAnswersFromItsOwnTable() {
        DartStyledText band = editor(), whole = editor(), none = editor();
        StyledTextHelper.applyTextGeometry(band, table(true));
        StyledTextHelper.applyTextGeometry(whole, table(false));

        StyledTextHelper.applyLineVerticalIndentDelta(band, BAND_FIRST + 10, 9);

        assertThat(StyledTextHelper.geometryLinePixel(band, BAND_FIRST + 20)).as("the band its indent dropped").isNull();
        assertThat(StyledTextHelper.geometryLinePixel(whole, BAND_FIRST + 20)).as("another editor's table").isNotNull();
        assertThat(StyledTextHelper.freshGeometry(none)).as("an editor no table was applied to").isNull();
    }

    private static DartStyledText editor() {
        DartStyledText st = mock(DartStyledText.class);
        StyledTextContent content = mock(StyledTextContent.class);
        when(content.getLineCount()).thenReturn(LINES);
        when(content.getCharCount()).thenReturn(CHAR_COUNT);
        when(content.getOffsetAtLine(anyInt())).thenAnswer(i -> (int) i.getArgument(0) * (LINE_LENGTH + 1));
        when(content.getLine(anyInt())).thenReturn("x".repeat(LINE_LENGTH));
        when(content.getLineAtOffset(anyInt())).thenAnswer(i -> (int) i.getArgument(0) / (LINE_LENGTH + 1));
        st.content = content;
        when(st.getCharCount()).thenReturn(CHAR_COUNT);
        when(st.getVerticalScrollOffset()).thenReturn(0);
        return st;
    }

    /** The client's table of the document: the band only, or every row with the band's per-character x. */
    private static Map<String, Object> table(boolean bandOnly) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (int line = bandOnly ? BAND_FIRST : 0; line <= (bandOnly ? BAND_LAST : LINES - 1); line++) {
            Map<String, Object> row = new HashMap<>();
            int start = line * (LINE_LENGTH + 1);
            row.put("l", line);
            row.put("s", start);
            row.put("e", start + LINE_LENGTH);
            row.put("x", 0.0);
            row.put("y", round(line * ROW_HEIGHT));
            row.put("w", LINE_LENGTH * 8.0);
            row.put("h", round(ROW_HEIGHT));
            if (line >= BAND_FIRST && line <= BAND_LAST) row.put("cxu", List.of(0.0, 8.0));
            lines.add(row);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("charCount", CHAR_COUNT);
        map.put("contentWidth", LINE_LENGTH * 8.0);
        map.put("contentHeight", CONTENT_HEIGHT);
        map.put("v", 1);
        map.put("lines", lines);
        if (bandOnly) {
            map.put("bandFirstLine", BAND_FIRST);
            map.put("rowHeight", ROW_HEIGHT);
        }
        return map;
    }

    /** As the client rounds its rows: to a hundredth. */
    private static double round(double v) {
        return Math.round(v * 100) / 100.0;
    }
}

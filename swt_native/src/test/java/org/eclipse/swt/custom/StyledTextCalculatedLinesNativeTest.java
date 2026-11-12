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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A push describing a new layout drops the line sizes the renderer worked out, and the content width
 * outside a band comes from those lines: both visit the lines the renderer calculated, not the whole
 * document, so a font change on a long document costs what is shown.
 */
@Tag("native-unit")
class StyledTextCalculatedLinesNativeTest {

    private static final int LINES = 1000;
    private static final int LINE_LENGTH = 4;
    private static final int BAND_FIRST = 400, BAND_LAST = 460;
    private static final int CHAR_COUNT = LINES * (LINE_LENGTH + 1) - 1;

    private final DartStyledTextRenderer renderer = mock(DartStyledTextRenderer.class);
    private final DartStyledText editor = editor(renderer);

    @Test
    void aPushWithANewRowHeightResetsOnlyTheLinesCalculatedSinceThePreviousOne() {
        StyledTextHelper.applyTextGeometry(editor, band(16));
        StyledTextHelper.calculating(renderer, BAND_FIRST, 20);
        StyledTextHelper.calculating(renderer, 900, 1);

        StyledTextHelper.applyTextGeometry(editor, band(20));

        verify(renderer).reset(BAND_FIRST, 20);
        verify(renderer).reset(900, 1);
        verify(renderer, times(2)).reset(anyInt(), anyInt());
        assertThat(renderer.maxWidthLineIndex).as("the widest line went with the rest").isEqualTo(-1);
    }

    @Test
    void anEditThatMovesLinesLeavesThePushToResetEveryLine() {
        StyledTextHelper.applyTextGeometry(editor, band(16));
        StyledTextHelper.calculating(renderer, BAND_FIRST, 20);
        StyledTextHelper.sendTextChange(editor, newLineAt(10));

        StyledTextHelper.applyTextGeometry(editor, band(20));

        verify(renderer).reset(0, LINES);
        verify(renderer, never()).reset(BAND_FIRST, 20);
    }

    @Test
    void theWidthOutsideTheBandAsksOnlyTheLinesCalculatedThere() {
        StyledTextHelper.applyTextGeometry(editor, band(16));
        StyledTextHelper.calculating(renderer, 10, 2);
        StyledTextHelper.calculating(renderer, BAND_FIRST, BAND_LAST - BAND_FIRST + 1);
        when(renderer._isWidthCalculated(anyInt())).thenReturn(true);
        when(renderer._lineWidth(11)).thenReturn(500);

        assertThat(StyledTextHelper.measuredContentWidth(renderer, editor)).isEqualTo(500);
        verify(renderer, times(2))._isWidthCalculated(anyInt());
    }

    @Test
    void aLineCalculatedForALongerTextIsNotAskedAboutOnceTheTextIsShorter() {
        StyledTextHelper.applyTextGeometry(editor, band(16));
        StyledTextHelper.calculating(renderer, 10, 1);
        StyledTextHelper.calculating(renderer, LINES, 5);
        when(renderer._isWidthCalculated(anyInt())).thenReturn(true);
        when(renderer._lineWidth(10)).thenReturn(200);

        assertThat(StyledTextHelper.measuredContentWidth(renderer, editor)).isEqualTo(200);
        verify(renderer, never())._isWidthCalculated(LINES);
    }

    @Test
    void anEmptyOrNegativeRangeMarksNoLine() {
        StyledTextHelper.calculating(renderer, 10, 0);
        StyledTextHelper.calculating(renderer, 10, -3);

        assertThat(StyledTextHelper.calculatedLines(renderer).lines.isEmpty()).isTrue();
    }

    @Test
    void afterAnEditThatMovesLinesOnePassOverEveryLineFindsTheCalculatedOnesAgain() {
        StyledTextHelper.applyTextGeometry(editor, band(16));
        StyledTextHelper.sendTextChange(editor, newLineAt(10));
        when(renderer._isWidthCalculated(700)).thenReturn(true);
        when(renderer._lineWidth(700)).thenReturn(300);

        assertThat(StyledTextHelper.measuredContentWidth(renderer, editor)).as("after the edit").isEqualTo(300);
        verify(renderer, times(LINES))._isWidthCalculated(anyInt());
        assertThat(StyledTextHelper.measuredContentWidth(renderer, editor)).as("once more").isEqualTo(300);
        verify(renderer, times(LINES + 1))._isWidthCalculated(anyInt());
    }

    private static DartStyledText editor(DartStyledTextRenderer renderer) {
        DartStyledText st = mock(DartStyledText.class);
        StyledTextContent content = mock(StyledTextContent.class);
        when(content.getLineCount()).thenReturn(LINES);
        when(content.getCharCount()).thenReturn(CHAR_COUNT);
        when(content.getOffsetAtLine(anyInt())).thenAnswer(i -> (int) i.getArgument(0) * (LINE_LENGTH + 1));
        when(content.getLine(anyInt())).thenReturn("x".repeat(LINE_LENGTH));
        when(content.getLineAtOffset(anyInt())).thenAnswer(i -> (int) i.getArgument(0) / (LINE_LENGTH + 1));
        st.content = content;
        StyledTextRenderer api = mock(StyledTextRenderer.class);
        when(api.getImpl()).thenReturn(renderer);
        st.renderer = api;
        when(st.getApi()).thenReturn(mock(StyledText.class));
        when(st.getCharCount()).thenReturn(CHAR_COUNT);
        when(st.getVerticalScrollOffset()).thenReturn(0);
        // A mock skips the field initializers, and sendTextChange writes the edit into this one.
        try {
            java.lang.reflect.Field edit = DartStyledText.class.getDeclaredField("textEdit");
            edit.setAccessible(true);
            edit.set(st, new TextEdit(0, 0, 0, 0, 0));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        return st;
    }

    private static TextChangingEvent newLineAt(int line) {
        TextChangingEvent event = new TextChangingEvent(mock(StyledTextContent.class));
        event.start = line * (LINE_LENGTH + 1);
        event.newText = "\n";
        event.newCharCount = 1;
        event.newLineCount = 1;
        return event;
    }

    /** The band the client sends with every line one row of {@code rowHeight}. */
    private static Map<String, Object> band(double rowHeight) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (int line = BAND_FIRST; line <= BAND_LAST; line++) {
            Map<String, Object> row = new HashMap<>();
            int start = line * (LINE_LENGTH + 1);
            row.put("l", line);
            row.put("s", start);
            row.put("e", start + LINE_LENGTH);
            row.put("x", 0.0);
            row.put("y", line * rowHeight);
            row.put("w", LINE_LENGTH * 8.0);
            row.put("h", rowHeight);
            lines.add(row);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("charCount", CHAR_COUNT);
        map.put("contentWidth", LINE_LENGTH * 8.0);
        map.put("contentHeight", LINES * rowHeight);
        map.put("v", 1);
        map.put("lines", lines);
        map.put("bandFirstLine", BAND_FIRST);
        map.put("rowHeight", rowHeight);
        return map;
    }
}

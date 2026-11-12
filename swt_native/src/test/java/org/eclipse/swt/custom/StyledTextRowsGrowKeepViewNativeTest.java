package org.eclipse.swt.custom;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A line laid out taller than the rest (a fallback font's glyphs) makes every row taller, after Java
 * has already scrolled by the old rows. The view stays on the line it showed, and on the caret when the
 * caret was in view, as native SWT's does: it lays the line out before it scrolls.
 */
@Tag("native-unit")
class StyledTextRowsGrowKeepViewNativeTest {

    private static final int LINES = 100, LINE_LENGTH = 4;
    private static final int TOP_LINE = 80, VIEW = 140;

    @Test
    void theViewStaysOnItsTopLineAndTheCaretWhenTheRowsGrow() {
        DartStyledText st = scrolledTo(TOP_LINE * 14, (TOP_LINE + 9) * (LINE_LENGTH + 1));

        StyledTextHelper.applyTextGeometry(st, table(18));

        verify(st).setTopPixel(TOP_LINE * 18);
        verify(st).showCaret();
    }

    @Test
    void aCaretOutOfViewIsLeftThere() {
        DartStyledText st = scrolledTo(TOP_LINE * 14, 0);

        StyledTextHelper.applyTextGeometry(st, table(18));

        verify(st).setTopPixel(TOP_LINE * 18);
        verify(st, never()).showCaret();
    }

    @Test
    void rowsThatDoNotMoveLeaveTheViewAlone() {
        DartStyledText st = scrolledTo(TOP_LINE * 14, (TOP_LINE + 9) * (LINE_LENGTH + 1));

        StyledTextHelper.applyTextGeometry(st, table(14));

        verify(st, never()).setTopPixel(org.mockito.ArgumentMatchers.anyInt());
        verify(st, never()).showCaret();
    }

    /** A widget showing {@value #VIEW} px from {@code top}, its rows 14 px tall as the client first measured. */
    private static DartStyledText scrolledTo(int top, int caret) {
        DartStyledText st = mock(DartStyledText.class);
        st.content = mock(StyledTextContent.class);
        st.clientAreaHeight = VIEW;
        when(st.getCharCount()).thenReturn(LINES * (LINE_LENGTH + 1) - 1);
        when(st.getVerticalScrollOffset()).thenReturn(top);
        when(st.getCaretOffset()).thenReturn(caret);
        StyledTextHelper.applyTextGeometry(st, table(14));
        return st;
    }

    private static Map<String, Object> table(double rowHeight) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (int line = 0; line < LINES; line++) {
            int start = line * (LINE_LENGTH + 1);
            List<Double> cx = new ArrayList<>();
            for (int i = 0; i <= LINE_LENGTH; i++) cx.add(i * 10.0);
            lines.add(Map.of("l", line, "s", start, "e", start + LINE_LENGTH, "x", 0.0, "y", line * rowHeight,
                    "w", LINE_LENGTH * 10.0, "h", rowHeight, "cx", cx));
        }
        Map<String, Object> table = new HashMap<>();
        table.put("charCount", LINES * (LINE_LENGTH + 1) - 1);
        table.put("contentWidth", LINE_LENGTH * 10.0);
        table.put("contentHeight", LINES * rowHeight);
        table.put("lines", lines);
        return table;
    }
}

package org.eclipse.swt.custom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code lineSizes} grows past {@code lineCount}, so a reset range can run past the last line. As in
 * SWT 3.136 (eclipse-platform/eclipse.platform.swt#3654), only the lines below {@code lineCount}
 * are reset and leave the average line height.
 */
@Tag("native-unit")
class StyledTextRendererResetTest {

    private static final int LINES = 3;
    private static final int CAPACITY = LINES + 32;

    /** Three measured lines 10 high, line 1 the widest, in room for 32 more. */
    private static DartStyledTextRenderer measuredRenderer() {
        DartStyledTextRenderer renderer = new DartStyledTextRenderer(null, null, null);
        renderer.lineCount = LINES;
        renderer.lineSizes = new DartStyledTextRenderer.LineSizeInfo[CAPACITY];
        for (int i = 0; i < LINES; i++) {
            DartStyledTextRenderer.LineSizeInfo size = renderer.getLineSize(i);
            size.height = 10;
            size.width = i == 1 ? 80 : 40;
        }
        renderer.averageLineHeight = 10;
        renderer.linesInAverageLineHeight = LINES;
        renderer.maxWidth = 80;
        renderer.maxWidthLineIndex = 1;
        return renderer;
    }

    @Test
    @DisplayName("a range past the last line resets only the lines it holds")
    void rangePastLastLine() {
        DartStyledTextRenderer renderer = measuredRenderer();

        renderer.reset(2, 4);

        assertThat(renderer.linesInAverageLineHeight).isEqualTo(2);
        assertThat(renderer.averageLineHeight).isEqualTo(10f);
        assertThat(renderer.maxWidth).isEqualTo(80);
        assertThat(renderer.maxWidthLineIndex).isEqualTo(1);
        assertThat(renderer.lineSizes[2].width).isEqualTo(-1);
        assertThat(Arrays.copyOfRange(renderer.lineSizes, LINES, CAPACITY))
                .as("no size is allocated past the last line").containsOnlyNulls();
    }

    @Test
    @DisplayName("resetting every line past the widest one finds the widest among the rest")
    void widestLineReset() {
        DartStyledTextRenderer renderer = measuredRenderer();

        renderer.reset(1, 5);

        assertThat(renderer.linesInAverageLineHeight).isEqualTo(1);
        assertThat(renderer.averageLineHeight).isEqualTo(10f);
        assertThat(renderer.maxWidth).isEqualTo(40);
        assertThat(renderer.maxWidthLineIndex).isEqualTo(0);
    }
}

package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Guards that a line's vertical indent moves both the line and the painted caret. Its own class
 * so the build can leave it out where StyledText.setLineVerticalIndent is not available.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextVerticalIndentFlutterTest {

    private final StyledTextFlutterStage stage = new StyledTextFlutterStage();
    private StyledText text;

    @BeforeAll
    void boot() {
        stage.boot();
    }

    @AfterAll
    void shutdown() {
        stage.shutdown();
    }

    @Test
    @DisplayName("a vertical indent pushes its line down")
    void verticalIndent() {
        stage.fresh(SWT.MULTI, "one\ntwo");
        text = stage.subject;
        text.setLineVerticalIndent(1, 12);
        stage.focusAt(text.getOffsetAtLine(1) + 1);

        assertThat(text.getLocationAtOffset(text.getOffsetAtLine(1)).y)
                .isEqualTo(text.getLinePixel(1) + 12);
        assertThat(stage.renderedCaretRect(text)[1]).isCloseTo(text.getLocationAtOffset(text.getCaretOffset()).y, within(1.0));
    }
}

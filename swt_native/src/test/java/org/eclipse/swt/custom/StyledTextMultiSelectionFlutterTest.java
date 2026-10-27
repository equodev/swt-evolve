package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards that every selection range and caret of a multi-selection is painted. Its own class because
 * StyledText.setSelectionRanges only exists from SWT 3.117; older releases leave it out of the build.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StyledTextMultiSelectionFlutterTest {

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

    private void fresh(int style, String content) {
        stage.fresh(style, content);
        text = stage.subject;
    }

    /** Waits for the client to show what the test just changed. */
    private void show() {
        stage.pushState();
    }

    @Test
    @DisplayName("several selection ranges are all painted")
    void multipleSelectionsArePainted() {
        fresh(SWT.MULTI, "alpha beta gamma delta");
        stage.focusAt(0);
        text.setSelectionRanges(new int[] { 0, 5, 11, 5 });
        show();

        assertThat(stage.renderedSelections(text)).containsExactly(new Point(0, 5), new Point(11, 16));
    }

    @Test
    @DisplayName("several carets are all painted")
    void multipleCaretsArePainted() {
        fresh(SWT.MULTI, "alpha beta gamma delta");
        stage.focusAt(0);
        text.setSelectionRanges(new int[] { 2, 0, 13, 0 });
        show();

        assertThat(stage.renderedCarets(text)).containsExactly(2, 13);
    }
}

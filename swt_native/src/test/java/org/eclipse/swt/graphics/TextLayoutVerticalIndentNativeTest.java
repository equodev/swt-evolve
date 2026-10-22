package org.eclipse.swt.graphics;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A TextLayout's vertical indent ({@link TextLayout#setVerticalIndent}, SWT 3.109 and later), in a class of its own
 * so the build can leave it out where that method is not available.
 */
@Tag("native-unit")
class TextLayoutVerticalIndentNativeTest extends TextLayoutDrawTestBase {

    @Test
    void theVerticalIndentIsKept() {
        TextLayout layout = layout("abc");
        layout.setVerticalIndent(6);
        assertThat(((DartTextLayout) layout.getImpl())._verticalIndentInPoints()).isEqualTo(6);
    }

    @Test
    void theIndentAndTheVerticalIndentMoveWhatIsDrawn() {
        TextLayout layout = layout("abc\ndef");
        layout.setIndent(15);
        layout.setVerticalIndent(7);
        layout.setSpacing(3);
        layout.draw(gc(), 10, 5);
        assertThat(run("abc").x()).isEqualTo(25);
        assertThat(run("abc").y()).isEqualTo(12);
        assertThat(run("def").x()).isEqualTo(25);
        assertThat(run("def").y()).isEqualTo(12 + layout.getLineBounds(1).y);
    }
}

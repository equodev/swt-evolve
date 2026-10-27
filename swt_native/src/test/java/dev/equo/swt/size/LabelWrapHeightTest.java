package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A wrapped label is as tall as the same number of lines separated by line breaks. Applications
 * size wrapping text by that equivalence: they measure "line1\nline2" to get the height of two lines
 * and then search for the narrowest width whose wrapped height fits it. If wrapping adds height of
 * its own, no width fits, and the text is laid out on one line at full width.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class LabelWrapHeightTest {

    private static final String TEXT =
            "The saved stash can be applied later. By default, Index and Working Tree are cleaned, but you may keep the Index or both.";

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Label.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Label");
    }

    @Test
    void textWrappedOntoTwoLines_isAsTallAsTwoExplicitLines() {
        Label label = new Label(shell(), SWT.WRAP);
        label.setText("line1\nline2");
        int twoLines = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        label.setText(TEXT);
        int oneLineWidth = label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        int wrapped = label.computeSize(oneLineWidth * 2 / 3, SWT.DEFAULT).y;

        assertThat(wrapped).isEqualTo(twoLines);
    }

    @Test
    void widthHintWiderThanTheText_keepsTheOneLineHeight() {
        Label label = new Label(shell(), SWT.WRAP);
        label.setText(TEXT);
        var oneLine = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        assertThat(label.computeSize(oneLine.x + 50, SWT.DEFAULT).y).isEqualTo(oneLine.y);
    }
}

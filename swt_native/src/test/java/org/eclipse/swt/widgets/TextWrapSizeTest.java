package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Text that wraps is as tall as the lines its words fill at the width it is laid out in; one that does
 * not wrap keeps the height of its lines whatever width it is given.
 */
@Tag("native-unit")
@ExtendWith(Mocks.class)
class TextWrapSizeTest {

    private static final String LONG = "one two three four five six seven eight nine ten eleven twelve";

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Text text(int style, String content) {
        Text text = new Text(Mocks.shell(), style);
        text.setText(content);
        return text;
    }

    @Test
    void aNarrowWidthMakesAWrappingTextTaller() {
        Text text = text(SWT.MULTI | SWT.WRAP, LONG);
        Point natural = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point narrow = text.computeSize(80, SWT.DEFAULT);
        assertThat(narrow.x).isEqualTo(80);
        assertThat(narrow.y).isGreaterThan(natural.y);
    }

    @Test
    void aNarrowerWidthMakesItTallerStill() {
        Text text = text(SWT.MULTI | SWT.WRAP, LONG);
        assertThat(text.computeSize(60, SWT.DEFAULT).y).isGreaterThan(text.computeSize(160, SWT.DEFAULT).y);
    }

    @Test
    void aWideWidthKeepsTheHeightOfOneLine() {
        Text text = text(SWT.MULTI | SWT.WRAP, LONG);
        assertThat(text.computeSize(2000, SWT.DEFAULT).y).isEqualTo(text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    @Test
    void aHeightHintIsReturnedAsGiven() {
        Text text = text(SWT.MULTI | SWT.WRAP, LONG);
        assertThat(text.computeSize(80, 33)).isEqualTo(new Point(80, 33));
    }

    @Test
    void aTextThatDoesNotWrapKeepsItsHeight() {
        Text text = text(SWT.MULTI, LONG);
        assertThat(text.computeSize(80, SWT.DEFAULT).y).isEqualTo(text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    @Test
    void anEmptyWrappingTextHasTheHeightOfOneLine() {
        Text text = text(SWT.MULTI | SWT.WRAP, "");
        assertThat(text.computeSize(80, SWT.DEFAULT).y).isEqualTo(text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }
}

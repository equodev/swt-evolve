package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The render side lays every line out the renderer's line height tall, and measures only the lines in
 * view, when it is told the editor has a fixed line height: it has to be told exactly when
 * StyledText#isFixedLineHeight holds.
 */
@Tag("native-unit")
public class StyledTextFixedLineHeightWireNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private StyledText text() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("one\ntwo\nthree");
        return text;
    }

    private static VStyledText value(StyledText text) {
        return (VStyledText) ((DartStyledText) text.getImpl()).getValue();
    }

    /** Makes the change on a value with nothing pending and checks the flag went out, cleared. */
    private static void assertSentVaryingAfter(StyledText text, Runnable change) {
        value(text).clearDirty();
        change.run();
        assertThat(value(text).changedKeys()).as("queued for the client").contains(VStyledText.FIXED_LINE_HEIGHT);
        assertThat(value(text).getFixedLineHeight()).isFalse();
    }

    @Test
    public void a_plain_editor_has_a_fixed_line_height() {
        assertThat(value(text()).getFixedLineHeight()).isTrue();
    }

    @Test
    public void word_wrap_and_line_spacing_vary_the_height() {
        StyledText wrapped = text();
        assertSentVaryingAfter(wrapped, () -> wrapped.setWordWrap(true));

        StyledText spaced = text();
        assertSentVaryingAfter(spaced, () -> spaced.setLineSpacing(2));
    }

    @Test
    public void a_style_with_its_own_font_varies_the_height() {
        StyledText text = text();
        Font big = new Font(display, "Arial", 30, SWT.NORMAL);
        try {
            StyleRange style = new StyleRange(0, 3, null, null);
            style.font = big;
            assertSentVaryingAfter(text, () -> text.setStyleRange(style));
        } finally {
            big.dispose();
        }
    }
}

package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The styles band is reused only while its source still matches the renderer, including runs the
 * renderer moved in place across an edit.
 */
// setText routes renderer font metrics into GTK/GDI on the Linux/Windows embed backends, which
// cannot run under the mocked display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class StyledTextBandMemoTest {

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

    private static StyledText styledText() {
        StyledText text = new StyledText(Mocks.shell(), SWT.NONE);
        text.setText("alpha beta gamma delta");
        return text;
    }

    private static DartStyledText impl(StyledText text) {
        return (DartStyledText) text.getImpl();
    }

    @Test
    void an_unchanged_renderer_is_answered_from_the_band_it_built() {
        StyledText text = styledText();
        text.setStyleRange(new StyleRange(6, 4, new Color(Mocks.device(), 255, 0, 0), null));

        int[] first = StyledTextHelper.wireStyleIndex(impl(text));
        assertThat(StyledTextHelper.wireStyleIndex(impl(text)))
                .as("nothing changed, so nothing is rebuilt")
                .isSameAs(first);
    }

    @Test
    void a_run_an_edit_moved_is_a_new_band() {
        StyledText text = styledText();
        text.setStyleRange(new StyleRange(6, 4, new Color(Mocks.device(), 255, 0, 0), null));
        int[] before = StyledTextHelper.wireStyleIndex(impl(text));
        int startBefore = before[2];

        text.replaceTextRange(0, 0, "xx");

        int[] after = StyledTextHelper.wireStyleIndex(impl(text));
        assertThat(after).as("the renderer moved the run, so the band is rebuilt").isNotSameAs(before);
        assertThat(after[2]).as("the run starts where the edit moved it").isEqualTo(startBefore + 2);
    }

    @Test
    void a_new_style_is_a_new_band() {
        StyledText text = styledText();
        Color red = new Color(Mocks.device(), 255, 0, 0);
        text.setStyleRange(new StyleRange(6, 4, red, null));
        int[] before = StyledTextHelper.wireStyleIndex(impl(text));

        text.setStyleRange(new StyleRange(11, 5, new Color(Mocks.device(), 0, 0, 255), null));

        assertThat(StyledTextHelper.wireStyleIndex(impl(text)))
                .as("a second run in a second colour")
                .isNotSameAs(before)
                .hasSize(1 + 3 * 2);
    }
}

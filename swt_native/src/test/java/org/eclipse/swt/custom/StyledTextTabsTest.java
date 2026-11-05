package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * setTabs reaches the renderer as setFont(null, tabs), which measures the tab width again with the
 * font it already has: the width every TextLayout the renderer builds places its tab stops by.
 */
@ExtendWith(Mocks.class)
class StyledTextTabsTest {

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

    private static DartStyledTextRenderer renderer(StyledText text) {
        return (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
    }

    @Test
    void the_tab_width_follows_the_tab_count() {
        StyledText text = new StyledText(Mocks.shell(), SWT.NONE);
        int perSpace = renderer(text)._tabWidth() / text.getTabs();

        text.setTabs(8);
        assertThat(renderer(text)._tabWidth()).isEqualTo(8 * perSpace);
        text.setTabs(2);
        assertThat(renderer(text)._tabWidth()).isEqualTo(2 * perSpace);
    }

    @Test
    void a_negative_tab_count_fails_as_it_does_natively() {
        StyledText text = new StyledText(Mocks.shell(), SWT.NONE);

        assertThatThrownBy(() -> text.setTabs(-1)).isInstanceOf(NegativeArraySizeException.class);
    }
}

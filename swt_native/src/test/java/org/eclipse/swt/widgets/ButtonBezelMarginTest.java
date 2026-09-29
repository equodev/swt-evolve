package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code push_button_margin} is a transparent margin on each side of a push or toggle button: the
 * preferred width reserves it and the render side paints the surface inside it. An application that
 * expects native frames wider than what the buttons draw overlaps adjacent frames, and the margin
 * keeps those buttons apart. It is off unless the application configures it.
 */
class ButtonBezelMarginTest extends SerializeTestBase {

    private static final String PROPERTY = "swt.evolve.push_button_margin";

    private ConfigFlags savedFlags;
    private String savedOsName;
    private String savedMargin;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedOsName = System.getProperty("os.name");
        savedMargin = System.getProperty(PROPERTY);
    }

    @AfterEach
    void restoreState() {
        System.setProperty("os.name", savedOsName);
        if (savedMargin == null) System.clearProperty(PROPERTY);
        else System.setProperty(PROPERTY, savedMargin);
        Config.setConfigFlags(savedFlags);
    }

    private static void margin(int px) {
        ConfigFlags flags = new ConfigFlags();
        flags.push_button_margin = px;
        Config.setConfigFlags(flags);
    }

    private static Point preferred(int style, int wHint) {
        Button button = new Button(Mocks.shell(), style);
        button.setText("Cherry-Pick");
        return button.computeSize(wHint, SWT.DEFAULT);
    }

    @Test
    void a_push_button_reserves_the_margin_on_both_sides() {
        margin(0);
        Point bare = preferred(SWT.PUSH, SWT.DEFAULT);
        margin(6);
        Point withMargin = preferred(SWT.PUSH, SWT.DEFAULT);

        assertThat(withMargin.x).isEqualTo(bare.x + 12);
        assertThat(withMargin.y).isEqualTo(bare.y);
    }

    @Test
    void a_toggle_button_has_the_same_bezel() {
        margin(0);
        Point bare = preferred(SWT.TOGGLE, SWT.DEFAULT);
        margin(6);

        assertThat(preferred(SWT.TOGGLE, SWT.DEFAULT).x).isEqualTo(bare.x + 12);
    }

    @Test
    void buttons_without_a_bezel_are_unchanged() {
        for (int style : new int[] {SWT.PUSH | SWT.FLAT, SWT.PUSH | SWT.WRAP, SWT.CHECK, SWT.RADIO, SWT.ARROW}) {
            margin(0);
            Point bare = preferred(style, SWT.DEFAULT);
            margin(6);
            assertThat(preferred(style, SWT.DEFAULT)).as("style 0x%x", style).isEqualTo(bare);
        }
    }

    @Test
    void a_width_hint_is_the_whole_frame() {
        margin(6);

        assertThat(preferred(SWT.PUSH, 120).x).isEqualTo(120);
    }

    @Test
    void the_margin_is_off_by_default_on_every_os() {
        System.clearProperty(PROPERTY);
        for (String os : new String[] {"Mac OS X", "Windows 11", "Linux"}) {
            Config.setConfigFlags(null);
            System.setProperty("os.name", os);
            assertThat(Config.getConfigFlags().push_button_margin).as(os).isZero();
        }
    }

    @Test
    void the_application_configures_the_margin() {
        System.setProperty(PROPERTY, "6");
        Config.setConfigFlags(null);

        assertThat(Config.getConfigFlags().push_button_margin).isEqualTo(6);
    }
}

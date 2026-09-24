package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Under the desktop surface text is painted by the host's own font stack, so it has to be measured
 * with the metrics taken from that stack — the ones the embedded backend already uses on this OS.
 * The web surface paints with the fonts it bundles and substitutes, and keeps its own table.
 */
class DesktopFontMetricsTest {

    private static final String SYSTEM_FONT = FontMetricsUtil.getId("system", false, false);

    private String savedMode;

    @BeforeEach
    void saveMode() {
        savedMode = ConfigFlags.mode();
    }

    @AfterEach
    void restoreMode() {
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else ConfigFlags.setMode(savedMode);
    }

    private static double width(String text) {
        return FontMetricsUtil.getFontSize(text, SYSTEM_FONT, 11).x();
    }

    /** 11pt ".AppleSystemUIFont", the face Flutter paints an unbundled family with on macOS. */
    @Test
    @EnabledOnOs(OS.MAC)
    void desktop_mode_on_macos_measures_the_system_font_macos_paints() {
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);

        assertThat(width("Cherry-Pick")).isCloseTo(62.0, within(0.1));
    }

    @Test
    void web_mode_keeps_the_web_table() {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        double web = width("Cherry-Pick");

        assertThat(web).isCloseTo(55.8, within(0.1));
    }
}

package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@code marketplace} theme preset supplies a look, not a veto: it may pick dark when nothing
 * else says otherwise, but an explicitly configured {@code swt.evolve.force_theme} is the product's
 * own choice and outranks it.
 */
class ConfigThemePresetTest {

    private ConfigFlags savedFlags;
    private String savedForceTheme;
    private String savedThemeName;

    @BeforeEach
    void captureState() {
        // The flags are a JVM singleton computed once and cached; null forces a recompute from the
        // system properties each test sets.
        savedFlags = Config.getConfigFlags();
        savedForceTheme = System.getProperty("swt.evolve.force_theme");
        savedThemeName = System.getProperty("swt.evolve.theme_name");
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        restore("swt.evolve.force_theme", savedForceTheme);
        restore("swt.evolve.theme_name", savedThemeName);
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    @Test
    void marketplace_preset_keeps_an_explicitly_configured_light_theme() {
        System.setProperty("swt.evolve.theme_name", "marketplace");
        System.setProperty("swt.evolve.force_theme", "light");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("light");
    }

    @Test
    void marketplace_preset_still_supplies_dark_when_nothing_was_configured() {
        System.setProperty("swt.evolve.theme_name", "marketplace");
        System.clearProperty("swt.evolve.force_theme");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("dark");
    }

    @Test
    void marketplace_preset_ignores_a_blank_force_theme() {
        System.setProperty("swt.evolve.theme_name", "marketplace");
        System.setProperty("swt.evolve.force_theme", "   ");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("dark");
    }
}

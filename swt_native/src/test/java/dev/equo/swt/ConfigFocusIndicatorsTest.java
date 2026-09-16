package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code swt.evolve.focus_indicators} reaches the flags only when it is set, so the Flutter side can
 * fall back to the named theme's default.
 */
class ConfigFocusIndicatorsTest {

    private static final String KEY = "swt.evolve.focus_indicators";

    private ConfigFlags savedFlags;
    private String savedValue;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedValue = System.getProperty(KEY);
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        if (savedValue == null) System.clearProperty(KEY);
        else System.setProperty(KEY, savedValue);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void unset_leaves_the_choice_to_the_theme() {
        System.clearProperty(KEY);
        assertThat(Config.getConfigFlags().focus_indicators).isNull();
    }

    @Test
    void blank_leaves_the_choice_to_the_theme() {
        System.setProperty(KEY, "  ");
        assertThat(Config.getConfigFlags().focus_indicators).isNull();
    }

    @Test
    void explicit_false_turns_them_off() {
        System.setProperty(KEY, "false");
        assertThat(Config.getConfigFlags().focus_indicators).isFalse();
    }

    @Test
    void explicit_true_turns_them_on() {
        System.setProperty(KEY, "true");
        assertThat(Config.getConfigFlags().focus_indicators).isTrue();
    }
}

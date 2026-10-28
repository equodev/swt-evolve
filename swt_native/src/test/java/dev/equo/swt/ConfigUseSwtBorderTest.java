package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code swt.use_swt_border} is off unless the application sets it, so a bordered control keeps the
 * theme's flat frame by default.
 */
class ConfigUseSwtBorderTest {

    private static final String KEY = "swt.use_swt_border";

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
    void unset_keeps_the_theme_frame() {
        System.clearProperty(KEY);
        assertThat(Config.getConfigFlags().use_swt_border).isFalse();
    }

    @Test
    void set_draws_the_swt_border() {
        System.setProperty(KEY, "true");
        assertThat(Config.getConfigFlags().use_swt_border).isTrue();
    }
}

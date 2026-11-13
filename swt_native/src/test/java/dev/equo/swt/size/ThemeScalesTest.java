package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A theme name the client does not know draws the default theme there, so Java has to size it in
 * the default scale here: the two sides agree on every name, known or not.
 */
class ThemeScalesTest {

    @Test
    void a_name_without_a_scale_of_its_own_is_sized_in_the_default_one() {
        assertThat(ThemeScales.forThemeName(null)).isEqualTo(Themes.Theme.NonDefault);
        assertThat(ThemeScales.forThemeName("")).isEqualTo(Themes.Theme.NonDefault);
        assertThat(ThemeScales.forThemeName("no-such-theme")).isEqualTo(Themes.Theme.NonDefault);
        assertThat(ThemeScales.forThemeName(ConfigFlags.DEFAULT_THEME_NAME)).isEqualTo(Themes.Theme.NonDefault);
    }

    @Test
    void the_flags_always_name_a_theme() {
        ConfigFlags saved = Config.getConfigFlags();
        String savedName = System.getProperty("swt.evolve.theme_name");
        try {
            System.clearProperty("swt.evolve.theme_name");
            Config.setConfigFlags(null);
            assertThat(Config.getConfigFlags().theme_name).isNotBlank();
        } finally {
            if (savedName != null) System.setProperty("swt.evolve.theme_name", savedName);
            Config.setConfigFlags(saved);
        }
    }
}

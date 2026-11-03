package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The widget/list system colors report the scheme the theme paints in, whatever the system looks
 * like. One that mixes them with fixed colors such as {@code COLOR_WHITE} keeps them light under a
 * dark theme with {@code swt.evolve.system_colors=light}.
 */
class ConfigSystemColorsTest {

    private static final String SYSTEM_COLORS = "swt.evolve.system_colors";

    private ConfigFlags savedFlags;
    private String savedSystemColors;
    private SystemAppearance system;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedSystemColors = System.getProperty(SYSTEM_COLORS);
        System.clearProperty(SYSTEM_COLORS);
    }

    @AfterEach
    void restoreState() {
        if (savedSystemColors == null) System.clearProperty(SYSTEM_COLORS);
        else System.setProperty(SYSTEM_COLORS, savedSystemColors);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void applicationColorsAreDarkUnderADarkTheme() {
        start("dark", false, false, "dark");

        assertThat(colorsAreDark()).isTrue();
    }

    @Test
    void aDarkThemeForcedOnALightDesktopStillReportsDarkColors() {
        start("dark", false, false, "light");

        assertThat(colorsAreDark())
                .as("an application coloring its containers from this palette must get the scheme "
                        + "the theme paints in, or it paints them light on a dark theme")
                .isTrue();
    }

    @Test
    void aDarkThemeReportsDarkColorsWhenTheSystemIsUnknown() {
        start("dark", false, false, null);

        assertThat(colorsAreDark()).isTrue();
    }

    @Test
    void withNoThemeOfItsOwnTheColorsFollowTheSystem() {
        start(null, false, false, "dark");
        assertThat(colorsAreDark()).isTrue();

        start(null, false, false, "light");
        assertThat(colorsAreDark()).isFalse();
    }

    @Test
    void lightSystemColorsStayLightUnderADarkTheme() {
        System.setProperty(SYSTEM_COLORS, "light");
        start("dark", false, false, "dark");

        assertThat(colorsAreDark()).isFalse();
    }

    @Test
    void themedCanvasIsDarkWhateverTheDesktop() {
        start("dark", true, false, "light");

        assertThat(colorsAreDark()).isTrue();
    }

    @Test
    void lightThemeStaysLightOnADarkDesktop() {
        start("light", false, false, "dark");

        assertThat(colorsAreDark()).isFalse();
    }

    @Test
    void useSwtColorsStaysLightOnADarkDesktop() {
        start("dark", true, true, "dark");

        assertThat(colorsAreDark()).isFalse();
    }

    private boolean colorsAreDark() {
        return Config.systemColorsAreDark(Config.darkTheme(system));
    }

    private void start(String theme, boolean themedCanvas, boolean useSwtColors, String reported) {
        system = new SystemAppearance();
        system.report(reported);
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        flags.disable_swt_canvas_colors = themedCanvas;
        flags.use_swt_colors = useSwtColors;
        Config.setConfigFlags(flags);
    }
}

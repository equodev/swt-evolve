package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The widget/list system colors report the scheme the theme paints in, whatever the desktop looks
 * like. One that mixes them with fixed colors such as {@code COLOR_WHITE} keeps them light under a
 * dark theme with {@code swt.evolve.system_colors=light}.
 */
class ConfigSystemColorsTest {

    private static final String SYSTEM_COLORS = "swt.evolve.system_colors";

    private ConfigFlags savedFlags;
    private Supplier<String> savedOsAppearance;
    private String savedSystemColors;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedOsAppearance = EclipseWorkspaceTheme.osAppearance;
        savedSystemColors = System.getProperty(SYSTEM_COLORS);
        System.clearProperty(SYSTEM_COLORS);
    }

    @AfterEach
    void restoreState() {
        EclipseWorkspaceTheme.osAppearance = savedOsAppearance;
        if (savedSystemColors == null) System.clearProperty(SYSTEM_COLORS);
        else System.setProperty(SYSTEM_COLORS, savedSystemColors);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void applicationColorsAreDarkUnderADarkTheme() {
        start("dark", false, false, "dark");

        assertThat(Config.systemColorsAreDark()).isTrue();
    }

    @Test
    void aDarkThemeForcedOnALightDesktopStillReportsDarkColors() {
        start("dark", false, false, "light");

        assertThat(Config.systemColorsAreDark())
                .as("an application coloring its containers from this palette must get the scheme "
                        + "the theme paints in, or it paints them light on a dark theme")
                .isTrue();
    }

    @Test
    void aDarkThemeReportsDarkColorsWhenTheDesktopCannotBeRead() {
        start("dark", false, false, null);

        assertThat(Config.systemColorsAreDark()).isTrue();
    }

    @Test
    void theDesktopAppearanceIsNeverRead() {
        int[] reads = { 0 };
        start("dark", false, false, "light");
        EclipseWorkspaceTheme.osAppearance = () -> {
            reads[0]++;
            return "light";
        };

        Config.systemColorsAreDark();
        Config.systemColorsAreDark();

        assertThat(reads[0])
                .as("the desktop already decides force_theme when nothing else does; reading it "
                        + "again here would undo a theme forced against it")
                .isZero();
    }

    @Test
    void lightSystemColorsStayLightUnderADarkTheme() {
        System.setProperty(SYSTEM_COLORS, "light");
        start("dark", false, false, "dark");

        assertThat(Config.systemColorsAreDark()).isFalse();
    }

    @Test
    void themedCanvasIsDarkWhateverTheDesktop() {
        start("dark", true, false, "light");

        assertThat(Config.systemColorsAreDark()).isTrue();
    }

    @Test
    void lightThemeStaysLightOnADarkDesktop() {
        start("light", false, false, "dark");

        assertThat(Config.systemColorsAreDark()).isFalse();
    }

    @Test
    void useSwtColorsStaysLightOnADarkDesktop() {
        start("dark", true, true, "dark");

        assertThat(Config.systemColorsAreDark()).isFalse();
    }

    private static void start(String theme, boolean themedCanvas, boolean useSwtColors, String desktop) {
        EclipseWorkspaceTheme.osAppearance = () -> desktop;
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        flags.disable_swt_canvas_colors = themedCanvas;
        flags.use_swt_colors = useSwtColors;
        Config.setConfigFlags(flags);
    }
}

package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An application that keeps its own Canvas/GC colors gets the system colors native SWT would report:
 * the desktop's appearance. One that mixes them with fixed colors such as {@code COLOR_WHITE} keeps
 * them light under a dark theme with {@code swt.evolve.system_colors=light}.
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
    void applicationColorsFollowADarkDesktop() {
        start("dark", false, false, "dark");

        assertThat(Config.systemColorsAreDark()).isTrue();
    }

    @Test
    void applicationColorsFollowALightDesktop() {
        start("dark", false, false, "light");

        assertThat(Config.systemColorsAreDark()).isFalse();
    }

    @Test
    void applicationColorsStayLightWhenTheDesktopCannotBeRead() {
        start("dark", false, false, null);

        assertThat(Config.systemColorsAreDark()).isFalse();
    }

    @Test
    void lightSystemColorsStayLightOnADarkDesktop() {
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

    @Test
    void desktopIsReadOncePerConfiguration() {
        int[] reads = { 0 };
        start("dark", false, false, "dark");
        EclipseWorkspaceTheme.osAppearance = () -> {
            reads[0]++;
            return "dark";
        };
        Config.setConfigFlags(Config.getConfigFlags());

        Config.systemColorsAreDark();
        Config.systemColorsAreDark();

        assertThat(reads[0]).isEqualTo(1);
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

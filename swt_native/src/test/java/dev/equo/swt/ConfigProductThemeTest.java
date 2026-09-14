package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A product that ships its own e4 CSS theme paints its widgets itself, so Evolve has to render in
 * the colors and fonts the CSS engine sets rather than in a theme of its own -- otherwise the
 * application comes out wholly light or wholly dark and none of its styling shows.
 */
class ConfigProductThemeTest {

    @TempDir
    Path workspace;

    private ConfigFlags savedFlags;
    private String savedInstanceArea;
    private String savedForceTheme;
    private String savedUseColors;
    private String savedUseFonts;
    private Supplier<String> savedOsAppearance;

    @BeforeEach
    void captureState() {
        // The flags are a JVM singleton computed once and cached; null forces a recompute from the
        // system properties each test sets.
        savedFlags = Config.getConfigFlags();
        savedInstanceArea = System.getProperty("osgi.instance.area");
        savedForceTheme = System.getProperty("swt.evolve.force_theme");
        savedUseColors = System.getProperty("swt.use_swt_colors");
        savedUseFonts = System.getProperty("swt.use_swt_fonts");
        savedOsAppearance = EclipseWorkspaceTheme.osAppearance;
        System.clearProperty("swt.evolve.force_theme");
        System.clearProperty("swt.use_swt_colors");
        System.clearProperty("swt.use_swt_fonts");
        EclipseWorkspaceTheme.osAppearance = () -> "light";
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        restore("osgi.instance.area", savedInstanceArea);
        restore("swt.evolve.force_theme", savedForceTheme);
        restore("swt.use_swt_colors", savedUseColors);
        restore("swt.use_swt_fonts", savedUseFonts);
        EclipseWorkspaceTheme.osAppearance = savedOsAppearance;
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private void workspaceThemedWith(String themeId) throws IOException {
        Path settings = workspace.resolve(
                ".metadata/.plugins/org.eclipse.core.runtime/.settings");
        Files.createDirectories(settings);
        Files.writeString(settings.resolve("org.eclipse.e4.ui.css.swt.theme.prefs"),
                "eclipse.preferences.version=1\nthemeid=" + themeId + "\n");
        System.setProperty("osgi.instance.area", workspace.toString());
    }

    @Test
    void a_products_own_theme_keeps_evolves_palette_until_asked_otherwise() throws IOException {
        workspaceThemedWith("com.acme.rcp.theme.corporate");

        ConfigFlags flags = Config.getConfigFlags();

        assertThat(flags.use_swt_colors).isFalse();
        assertThat(flags.use_swt_fonts).isFalse();
    }

    @Test
    void the_applications_colors_and_fonts_are_an_explicit_opt_in() throws IOException {
        workspaceThemedWith("com.acme.rcp.theme.corporate");
        System.setProperty("swt.use_swt_colors", "true");
        System.setProperty("swt.use_swt_fonts", "true");

        ConfigFlags flags = Config.getConfigFlags();

        assertThat(flags.use_swt_colors).isTrue();
        assertThat(flags.use_swt_fonts).isTrue();
    }

    @Test
    void a_products_own_theme_falls_back_to_the_desktop_instead_of_dark() throws IOException {
        workspaceThemedWith("com.acme.rcp.theme.corporate");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("light");
    }

    @Test
    void a_products_own_theme_still_names_its_variant_when_it_can() throws IOException {
        workspaceThemedWith("com.acme.rcp.theme.dark");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("dark");
    }

    @Test
    void a_platform_theme_keeps_evolves_own_palette() throws IOException {
        workspaceThemedWith("org.eclipse.e4.ui.css.theme.e4_dark");

        ConfigFlags flags = Config.getConfigFlags();

        assertThat(flags.force_theme).isEqualTo("dark");
        assertThat(flags.use_swt_colors).isFalse();
        assertThat(flags.use_swt_fonts).isFalse();
    }

    @Test
    void an_application_with_no_workspace_is_unaffected() {
        System.clearProperty("osgi.instance.area");

        ConfigFlags flags = Config.getConfigFlags();

        assertThat(flags.force_theme).isEqualTo("dark");
        assertThat(flags.use_swt_colors).isFalse();
    }

    @Test
    void an_explicit_setting_outranks_the_detected_theme() throws IOException {
        workspaceThemedWith("com.acme.rcp.theme.corporate");
        System.setProperty("swt.evolve.force_theme", "dark");

        assertThat(Config.getConfigFlags().force_theme).isEqualTo("dark");
    }
}

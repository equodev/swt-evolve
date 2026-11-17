package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.TestThemeSystemColors;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@code getSystemColor} answers what a named theme's provider sets, and Evolve's own value otherwise. */
class DisplayNamedThemeSystemColorsTest {

    private static final RGB EVOLVE_SELECTION = new RGB(51, 153, 255);

    private ConfigFlags savedFlags;
    private String savedThemeName;
    private String savedForceTheme;
    private Display display;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        savedThemeName = System.getProperty("swt.evolve.theme_name");
        savedForceTheme = System.getProperty("swt.evolve.force_theme");
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (!display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        restore("swt.evolve.theme_name", savedThemeName);
        restore("swt.evolve.force_theme", savedForceTheme);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void the_theme_sets_its_light_selection() {
        configure(TestThemeSystemColors.THEME_NAME, "light");
        assertThat(rgb(SWT.COLOR_LIST_SELECTION)).isEqualTo(TestThemeSystemColors.LIGHT_SELECTION);
    }

    @Test
    void what_the_theme_does_not_set_keeps_evolves_colour() {
        configure(TestThemeSystemColors.THEME_NAME, "dark");
        assertThat(rgb(SWT.COLOR_LIST_SELECTION)).isEqualTo(EVOLVE_SELECTION);
        configure(TestThemeSystemColors.THEME_NAME, "light");
        assertThat(rgb(SWT.COLOR_LIST_SELECTION_TEXT)).isEqualTo(new RGB(255, 255, 255));
    }

    @Test
    void another_theme_keeps_evolves_colour() {
        configure("another-theme", "light");
        assertThat(rgb(SWT.COLOR_LIST_SELECTION)).isEqualTo(EVOLVE_SELECTION);
        configure(null, "light");
        assertThat(rgb(SWT.COLOR_LIST_SELECTION)).isEqualTo(EVOLVE_SELECTION);
    }

    private static void configure(String themeName, String mode) {
        restore("swt.evolve.theme_name", themeName);
        System.setProperty("swt.evolve.force_theme", mode);
        Config.setConfigFlags(null);
    }

    private static void restore(String property, String value) {
        if (value == null) System.clearProperty(property);
        else System.setProperty(property, value);
    }

    private RGB rgb(int id) {
        return display.getSystemColor(id).getRGB();
    }
}

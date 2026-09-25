package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@link Display#getSystemColor(int)}'s widget and list colors to the scheme the application's
 * Canvas/GC content is painted in. They follow a dark {@code force_theme} when the theme also colors
 * that content ({@code disable_swt_canvas_colors}). An application painting in its own colors gets the
 * desktop's scheme ({@code ConfigSystemColorsTest}), unless it mixes them with fixed ones such as
 * {@code COLOR_WHITE} and keeps them light with {@code swt.evolve.system_colors=light}.
 *
 * <p>The dark values are the app's default dark {@code ColorScheme.surface}/{@code onSurface}, mirrored
 * in {@code CanvasTheme}.
 */
@Tag("flutter-it")
class DisplaySystemColorThemeFlutterTest {

    private static final String SYSTEM_COLORS = "swt.evolve.system_colors";

    private Display display;
    private ConfigFlags savedFlags;
    private String savedSystemColors;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        savedSystemColors = System.getProperty(SYSTEM_COLORS);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedSystemColors == null) System.clearProperty(SYSTEM_COLORS);
        else System.setProperty(SYSTEM_COLORS, savedSystemColors);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void widgetBackgroundFollowsDarkForceTheme() {
        themedCanvas("dark");

        Color background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .as("dark force_theme must resolve to the app's own dark ColorScheme.surface")
                .isEqualTo(new int[] { 31, 41, 55 });
    }

    @Test
    void widgetForegroundFollowsDarkForceTheme() {
        themedCanvas("dark");

        Color foreground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .as("a foreground must come from the same scheme as the background it sits on")
                .isEqualTo(new int[] { 249, 250, 251 });
    }

    @Test
    void widgetForegroundKeepsBlackUnderLightForceTheme() {
        forceTheme("light");

        Color foreground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .isEqualTo(new int[] { 0, 0, 0 });
    }

    @Test
    void widgetBackgroundKeepsLightDefaultUnderLightForceTheme() {
        forceTheme("light");

        Color background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .isEqualTo(new int[] { 240, 240, 240 });
    }

    /**
     * The color a {@code LineNumberRulerColumn} (JFace's line-number ruler, the widget originally
     * reported) actually falls back to when the application never calls {@code setBackground} on it
     * — confirmed by decompiling {@code LineNumberRulerColumn.getBackground(Display)}, which queries
     * {@code SWT.COLOR_LIST_BACKGROUND}, not {@code COLOR_WIDGET_BACKGROUND}.
     */
    @Test
    void listBackgroundFollowsDarkForceTheme() {
        themedCanvas("dark");

        Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .as("dark force_theme must not resolve to the fixed light list background")
                .isNotEqualTo(new int[] { 255, 255, 255 });
    }

    @Test
    void listForegroundFollowsDarkForceTheme() {
        themedCanvas("dark");

        Color foreground = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .isEqualTo(new int[] { 249, 250, 251 });
    }

    @Test
    void listForegroundKeepsBlackUnderLightForceTheme() {
        forceTheme("light");

        Color foreground = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .isEqualTo(new int[] { 0, 0, 0 });
    }

    @Test
    void listBackgroundKeepsLightDefaultUnderLightForceTheme() {
        forceTheme("light");

        Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .isEqualTo(new int[] { 255, 255, 255 });
    }

    @Test
    void widgetBackgroundKeepsLightDefaultWhenLightSystemColorsAreRequested() {
        System.setProperty(SYSTEM_COLORS, "light");
        forceTheme("dark");

        assertThat(rgb(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)))
                .as("an application mixing this with COLOR_WHITE rows must get one scheme")
                .isEqualTo(new int[] { 240, 240, 240 });
    }

    @Test
    void listColorsKeepLightDefaultWhenLightSystemColorsAreRequested() {
        System.setProperty(SYSTEM_COLORS, "light");
        forceTheme("dark");

        assertThat(rgb(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND))).isEqualTo(new int[] { 255, 255, 255 });
        assertThat(rgb(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND))).isEqualTo(new int[] { 0, 0, 0 });
        assertThat(rgb(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND))).isEqualTo(new int[] { 0, 0, 0 });
    }

    @Test
    void useSwtColorsKeepsLightDefaultEvenWhenCanvasColorsAreDisabled() {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "dark";
        flags.disable_swt_canvas_colors = true;
        flags.use_swt_colors = true;
        start(flags);

        assertThat(rgb(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND))).isEqualTo(new int[] { 240, 240, 240 });
    }

    // ---- harness ----------------------------------------------------------------------------------

    private void forceTheme(String theme) {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        start(flags);
    }

    private void themedCanvas(String theme) {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        flags.disable_swt_canvas_colors = true;
        start(flags);
    }

    private static int[] rgb(Color color) {
        return new int[] { color.getRed(), color.getGreen(), color.getBlue() };
    }

    private void start(ConfigFlags flags) {
        Config.setConfigFlags(flags);
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
    }
}

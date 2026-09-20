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
 * Pins {@link Display#getSystemColor(int)} to {@link Display#isSystemDarkTheme()} for the widget
 * colors a JFace control (e.g. a {@code LineNumberRulerColumn}) falls back to when the application
 * never sets its own — these are queried directly, not resolved through a theme extension, so they
 * used to stay their fixed light-mode RGB regardless of {@code force_theme}.
 *
 * <p>The dark background value ({@code (31, 41, 55)}) is not invented here: it's the app's own
 * default dark {@code ColorScheme.surface} ({@code flutter-lib/lib/src/theme/theme.dart},
 * {@code createDarkColorScheme()}), the same color {@code CanvasThemeExtension.backgroundColor}
 * resolves to for a genuinely Dart-rendered Canvas ({@code canvas_theme_settings.dart}'s
 * {@code _getCanvasTheme}). Java has no synchronous channel to ask Dart for that value at
 * {@code getSystemColor()} call time, so it's mirrored here as a literal rather than computed — but
 * it's the literal the theme actually uses, not an approximation.
 *
 * <p>The foregrounds are covered too, which they once were not: {@code DartControl}'s
 * generator-added {@code _foreground} field used to default to a hardcoded {@code Color(0, 0, 0)}
 * (unlike {@code _background}, which defaults to {@code null}), so {@code Control.getForeground()}
 * never reached {@code defaultForeground()} and making the constants theme-aware changed nothing a
 * control reported. On Cocoa the field is now left unset, so the fallback runs and a foreground is
 * read from the same scheme as the background it is drawn on — without which content drawn from a
 * {@code Paint} listener inherits black onto the dark surface above.
 */
@Tag("flutter-it")
class DisplaySystemColorThemeFlutterTest {

    private Display display;
    private ConfigFlags savedFlags;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void widgetBackgroundFollowsDarkForceTheme() {
        forceTheme("dark");

        Color background = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .as("dark force_theme must resolve to the app's own dark ColorScheme.surface")
                .isEqualTo(new int[] { 31, 41, 55 });
    }

    @Test
    void widgetForegroundFollowsDarkForceTheme() {
        forceTheme("dark");

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
        forceTheme("dark");

        Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        assertThat(new int[] { background.getRed(), background.getGreen(), background.getBlue() })
                .as("dark force_theme must not resolve to the fixed light list background")
                .isNotEqualTo(new int[] { 255, 255, 255 });
    }

    @Test
    void listForegroundFollowsDarkForceTheme() {
        forceTheme("dark");

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

    // ---- harness ----------------------------------------------------------------------------------

    private void forceTheme(String theme) {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        Config.setConfigFlags(flags);
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
    }
}

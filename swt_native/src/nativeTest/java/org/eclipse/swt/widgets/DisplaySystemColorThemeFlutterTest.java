package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
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
 * <p>{@code COLOR_WIDGET_FOREGROUND} and {@code COLOR_LIST_FOREGROUND} are deliberately NOT covered
 * here: {@code DartControl}'s generator-added {@code _foreground} field defaults to a hardcoded
 * {@code Color(0, 0, 0)} (unlike {@code _background}, which defaults to {@code null}), so
 * {@code Control.getForeground()} almost never actually reaches {@code defaultForeground()} —
 * making either foreground constant dark-theme-aware broke upstream-parity coverage
 * ({@code Test_org_eclipse_swt_widgets_Text#test_setForegroundAfterBackground}, which — on Cocoa —
 * exercises {@code COLOR_LIST_FOREGROUND}, not {@code COLOR_WIDGET_FOREGROUND}) without fixing
 * anything real. Follow-up, not in scope here.
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
    void widgetForegroundStaysFixedRegardlessOfTheme() {
        forceTheme("dark");

        Color foreground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .as("COLOR_WIDGET_FOREGROUND stays fixed — see the class Javadoc for why")
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
    void listForegroundStaysFixedRegardlessOfTheme() {
        forceTheme("dark");

        Color foreground = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);

        assertThat(new int[] { foreground.getRed(), foreground.getGreen(), foreground.getBlue() })
                .as("COLOR_LIST_FOREGROUND stays fixed — see the class Javadoc for why")
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
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}

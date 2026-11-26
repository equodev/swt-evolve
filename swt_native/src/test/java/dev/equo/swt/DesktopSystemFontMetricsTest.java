package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Under the desktop surface on Linux the client draws the "System" family in the Inter face it
 * bundles, so text measured in "System" has to come out at that face's width. A narrower measure
 * clips custom-drawn text sized by {@code GC.textExtent}, such as the forms {@code Hyperlink}s of
 * Project Explorer's empty-workspace page.
 */
public class DesktopSystemFontMetricsTest {

    private static final String TEXT = "Create a new C or C++ project";
    private static final int SIZE = 9;

    /**
     * The width the Flutter Linux engine lays {@link #TEXT} out at in the bundled Inter at 12 px,
     * which is {@link #SIZE} pt at 96 DPI. font_symbol_fallback_test.dart holds the client to it.
     */
    private static final double PAINTED_PX = 170.21;

    private static final double PX_PER_POINT = 96.0 / 72.0;

    private String savedMode;

    @BeforeEach
    void desktopMode() {
        savedMode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
    }

    @AfterEach
    void restoreMode() {
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else ConfigFlags.setMode(savedMode);
    }

    @Test
    void system_text_is_measured_at_the_width_the_linux_client_paints_it() {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("linux"), "Linux desktop host");

        double measuredPx = FontMetricsUtil.getFontSize(TEXT, "System-0-3", SIZE, 0).x() * PX_PER_POINT;

        assertThat(measuredPx)
                .as("width of \"%s\" in System %dpt, painted in the bundled Inter", TEXT, SIZE)
                .isCloseTo(PAINTED_PX, org.assertj.core.data.Offset.offset(1.0))
                .isGreaterThanOrEqualTo(PAINTED_PX);
    }

    @Test
    void every_system_style_is_measured_with_the_bundled_face_on_linux() {
        assumeTrue(System.getProperty("os.name").toLowerCase().contains("linux"), "Linux desktop host");

        for (String style : new String[] {"-0-2", "-0-3", "-1-3", "-0-4", "-0-5", "-0-6", "-1-6"}) {
            assertThat(FontMetricsUtil.metrics("System" + style))
                    .as("System%s", style)
                    .isNotNull()
                    .isSameAs(FontMetricsUtil.metrics(FontMetricsUtil.LINUX_DESKTOP_SYSTEM_FACE + style));
        }
    }

    @Test
    void only_the_system_family_is_redirected() {
        assertThat(FontMetricsUtil.linuxDesktopFontId("System-1-6")).isEqualTo("Inter-1-6");
        assertThat(FontMetricsUtil.linuxDesktopFontId("System-ui-0-3")).isEqualTo("System-ui-0-3");
        assertThat(FontMetricsUtil.linuxDesktopFontId("System Web-0-3")).isEqualTo("System Web-0-3");
        assertThat(FontMetricsUtil.linuxDesktopFontId("Noto Sans-0-3")).isEqualTo("Noto Sans-0-3");
    }

    @Test
    void the_redirect_applies_on_a_linux_host_only() {
        assertThat(FontMetricsUtil.desktopFontId("System-0-3", "Linux")).isEqualTo("Inter-0-3");
        for (String os : new String[] {"Windows 11", "Mac OS X"}) {
            for (String id : new String[] {"System-0-3", "System-1-6", "Segoe UI-0-3", "Inter-0-3"}) {
                assertThat(FontMetricsUtil.desktopFontId(id, os)).as("%s on %s", id, os).isEqualTo(id);
            }
        }
    }

    @Test
    void the_web_surface_keeps_its_own_system_metrics() {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);

        assertThat(FontMetricsUtil.metrics("System-0-3")).isSameAs(GenFontMetrics.DATA.get("System-0-3"));
    }
}

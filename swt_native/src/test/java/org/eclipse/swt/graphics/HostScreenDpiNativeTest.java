package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.IntegerOn150;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * There is no OS to ask for a screen DPI on this backend. It reports the host's base DPI (72 on
 * macOS, which lays out in points, 96 elsewhere) over the zoom SWT draws at, as Win32 does, so that
 * {@code getDPI() / 96} grows with the text an application sizes by it.
 */
@Tag("native-unit")
public class HostScreenDpiNativeTest {

    @Test
    public void host_dpi_follows_the_operating_system() {
        assertThat(FontMetricsUtil.hostScreenDPI("Mac OS X").x).isEqualTo(72);
        assertThat(FontMetricsUtil.hostScreenDPI("Windows 11").x).isEqualTo(96);
        assertThat(FontMetricsUtil.hostScreenDPI("Linux").x).isEqualTo(96);
    }

    @Test
    public void the_dpi_is_the_host_dpi_over_the_zoom_swt_draws_at() {
        assertThat(FontMetricsUtil.screenDPI(96, 1.0).x).isEqualTo(96);
        assertThat(FontMetricsUtil.screenDPI(96, 100 / 150.0).x).isEqualTo(144);
        assertThat(FontMetricsUtil.screenDPI(96, 100 / 125.0).x).isEqualTo(120);
        assertThat(FontMetricsUtil.screenDPI(96, 200 / 175.0).x).isEqualTo(84);
        assertThat(FontMetricsUtil.screenDPI(72, 1.0).x).isEqualTo(72);
        assertThat(FontMetricsUtil.screenDPI(72, 100 / 150.0).x).isEqualTo(108);
    }

    @Test
    public void device_reports_the_screen_dpi_at_the_zoom_swt_draws_at() {
        DartDevice device = mock(DartDevice.class);
        when(device.getScreenDPI()).thenCallRealMethod();
        String savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        try {
            IntegerOn150.run(() -> {
                assertThat(Config.uiScale()).isNotEqualTo(1.0);
                assertThat(device.getScreenDPI())
                        .isEqualTo(FontMetricsUtil.screenDPI())
                        .isNotEqualTo(FontMetricsUtil.hostScreenDPI(System.getProperty("os.name")));
            });
        } finally {
            if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
            else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
        }
    }
}

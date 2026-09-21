package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * There is no OS to ask for a screen DPI on this backend, so it answers for the host it runs on.
 * A fixed 96 reports a Windows/GTK screen everywhere; macOS lays out in 72-dpi points, and text
 * extents and the render side both scale by this, so it sizes every point-sized font a third too
 * large there.
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
    public void device_reports_the_host_dpi_rather_than_a_fixed_one() {
        DartDevice device = mock(DartDevice.class);
        when(device.getScreenDPI()).thenCallRealMethod();

        assertThat(device.getScreenDPI()).isEqualTo(FontMetricsUtil.hostScreenDPI());
    }
}

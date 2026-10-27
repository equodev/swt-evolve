package org.eclipse.swt.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Applications built against Windows SWT call {@code Win32DPIUtils} directly (an editor sizing its
 * fonts with {@code pointToPixel}, say). The class is upstream's, except the members that query or
 * change Win32 DPI state: Flutter owns DPI handling, so those report the client's zoom or do nothing.
 */
@Tag("native-unit")
public class Win32DPIUtilsNativeTest {

    @AfterEach
    public void clearCustomDpiAwareness() {
        System.clearProperty(Win32DPIUtils.CUSTOM_DPI_AWARENESS_PROPERTY);
    }

    @Test
    public void points_convert_to_pixels_at_the_given_zoom() {
        assertThat(Win32DPIUtils.pointToPixel(10f, 150)).isEqualTo(15f);
        assertThat(Win32DPIUtils.pointToPixel(10f, 100)).isEqualTo(10f);
    }

    @Test
    public void dpi_awareness_has_nothing_to_change() {
        assertThat(Win32DPIUtils.setDPIAwareness(2)).isTrue();
        assertThat(Win32DPIUtils.hasProperDpiAwarenessForMonitorSpecificScaling()).isTrue();
        assertThat(Win32DPIUtils.<String>runWithProperDPIAwareness(null, () -> "ran")).isEqualTo("ran");
    }

    @Test
    public void a_custom_dpi_awareness_is_not_applied() {
        System.setProperty(Win32DPIUtils.CUSTOM_DPI_AWARENESS_PROPERTY, "PerMonitorV2");
        assertThat(Win32DPIUtils.initializeCustomDpiAwareness()).isFalse();
    }

    @Test
    public void the_startup_zoom_is_the_native_device_zoom() {
        int previous = DPIUtil.getNativeDeviceZoom();
        try {
            DPIUtil.setDeviceZoom(150);
            assertThat(Win32DPIUtils.getPrimaryMonitorZoomAtStartup()).isEqualTo(150);
        } finally {
            DPIUtil.setDeviceZoom(previous);
        }
    }
}

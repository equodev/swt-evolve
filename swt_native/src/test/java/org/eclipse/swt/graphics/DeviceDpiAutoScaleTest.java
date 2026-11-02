package org.eclipse.swt.graphics;

import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.IntegerOn150;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

/**
 * Win32 reports the monitor's DPI in SWT units: {@code GetDeviceCaps(LOGPIXELSX)} scaled down by the
 * zoom SWT draws at. Applications size what holds text by it -- NatTable scales its row heights and
 * column widths by {@code getDPI() / 96} -- so a DPI that ignores the monitor leaves text taller
 * than the rows drawn around it. Every host reports it that way; the figures here are a 96-DPI host's.
 */
@Tag("native-unit")
@DisabledOnOs(value = OS.MAC, disabledReason = "macOS lays out at 72 DPI, not the 96 these figures assume")
class DeviceDpiAutoScaleTest {

    private static Point dpi() {
        return mock(DartDevice.class, CALLS_REAL_METHODS).getDPI();
    }

    @Test
    void at_100_percent_the_dpi_is_96() {
        assertThat(dpi()).isEqualTo(new Point(96, 96));
    }

    @Test
    void integer_autoscale_on_a_150_percent_monitor_reports_144() {
        // The desktop window follows the autoscale policy, and SWT draws at 100 there, so one unit
        // is one physical pixel of a 144-DPI monitor.
        inDesktopMode(() -> IntegerOn150.run(() -> assertThat(dpi()).isEqualTo(new Point(144, 144))));
    }

    @Test
    void no_autoscale_policy_with_monitor_wide_scaling_on_a_150_percent_monitor_reports_144() {
        // An application that sets no swt.autoScale, on a release that scales the whole display at
        // one zoom (all of them before monitor-specific scaling): the default rounds 150 down to 100.
        inDesktopMode(() -> withProperty(UPDATE_ON_RUNTIME, "false", () -> on150(null, () -> {
            assertThat(DPIUtil.getDeviceZoom()).isEqualTo(100);
            assertThat(dpi()).isEqualTo(new Point(144, 144));
        })));
    }

    private static final String UPDATE_ON_RUNTIME = "swt.autoScale.updateOnRuntime";

    /** {@code policy} on a 150 % monitor, the property itself cleared when {@code null}. */
    private static void on150(String policy, Runnable body) {
        int savedZoom = DPIUtil.getDeviceZoom();
        withProperty("swt.autoScale", policy, () -> {
            try {
                AutoScalePolicy.runWith(policy, () -> {
                    DPIUtil.setDeviceZoom(150);
                    Config.setClientDeviceZoom(150);
                    body.run();
                });
            } finally {
                DPIUtil.setDeviceZoom(savedZoom);
                Config.setClientDeviceZoom(100);
            }
        });
    }

    private static void inDesktopMode(Runnable body) {
        withProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP, body);
    }

    private static void withProperty(String key, String value, Runnable body) {
        String saved = System.getProperty(key);
        set(key, value);
        try {
            body.run();
        } finally {
            set(key, saved);
        }
    }

    private static void set(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }
}

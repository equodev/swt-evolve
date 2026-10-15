package org.eclipse.swt.widgets;

import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A desktop window is placed and sized by the window system in its logical pixels, and the shell it
 * hosts has its bounds in SWT units. The two differ by the factor the tree is drawn at, which is 1
 * only at 100 % or when {@code swt.autoScale} keeps the monitor's zoom -- so a window placed with
 * SWT units unconverted lands and sizes right on those and wrong everywhere else.
 */
class DeskWindowUnitsTest {

    private int savedZoom;
    private String savedMode;

    @BeforeEach
    void desktop() {
        savedZoom = DPIUtil.getDeviceZoom();
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
    }

    @AfterEach
    void restore() {
        DPIUtil.setDeviceZoom(savedZoom);
        Config.setClientDeviceZoom(100);
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
    }

    @Test
    void a_window_is_placed_in_the_window_systems_pixels_not_in_swt_units() {
        // `integer` on a 150 % monitor, the default on the oldest supported SWT: SWT draws at 100, so
        // a 600-unit shell takes 400 logical pixels -- the 600 physical pixels it is drawn in.
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(150);
            Config.setClientDeviceZoom(150);

            assertThat(DeskDisplayBridge.toWindowUnits(new Rectangle(300, 150, 600, 450)))
                    .isEqualTo(new Rectangle(200, 100, 400, 300));
        });
    }

    @Test
    void windows_that_touch_in_swt_units_still_touch_on_screen() {
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(150);
            Config.setClientDeviceZoom(150);

            Rectangle left = DeskDisplayBridge.toWindowUnits(new Rectangle(0, 0, 101, 50));
            Rectangle right = DeskDisplayBridge.toWindowUnits(new Rectangle(101, 0, 101, 50));

            assertThat(left.x + left.width).isEqualTo(right.x);
        });
    }

    @Test
    void at_the_monitors_own_zoom_the_two_coincide() {
        DPIUtil.setDeviceZoom(100);
        Config.setClientDeviceZoom(100);

        assertThat(DeskDisplayBridge.toWindowUnits(new Rectangle(300, 150, 600, 450)))
                .isEqualTo(new Rectangle(300, 150, 600, 450));
    }
}

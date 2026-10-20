package org.eclipse.swt.widgets;

import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A desktop window is placed and sized in the window system's logical pixels; the shell it hosts has
 * its bounds in SWT units. The two differ by the factor the tree is drawn at.
 */
class DeskWindowUnitsTest {

    private int savedZoom;
    private String savedMode;
    private String savedDesktop;

    @BeforeEach
    void desktop() {
        savedZoom = DPIUtil.getDeviceZoom();
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedDesktop = System.getProperty("dev.equo.swt.desktop");
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
    }

    @AfterEach
    void restore() {
        DPIUtil.setDeviceZoom(savedZoom);
        Config.setClientDeviceZoom(100);
        restore(ConfigFlags.MODE_PROPERTY, savedMode);
        restore("dev.equo.swt.desktop", savedDesktop);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
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
    void a_window_origin_is_read_back_in_swt_units() {
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(150);
            Config.setClientDeviceZoom(150);
            DeskDisplayBridge desk = new DeskDisplayBridge(null) {
                @Override
                protected long nativeOrigin(long context) {
                    return (200L << 32) | 100L;
                }
            };

            assertThat(desk.shellWindowOrigin(1)).isEqualTo(new Point(300, 150));
        });
    }

    @Test
    void the_displays_window_is_placed_in_the_window_systems_pixels() {
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(150);
            Config.setClientDeviceZoom(150);
            List<Rectangle> placed = new ArrayList<>();
            DeskDisplayBridge desk = new DeskDisplayBridge(null) {
                @Override
                protected void placeNativeWindow(long context, Rectangle w) {
                    placed.add(w);
                }
            };

            desk.forwardWindowBounds(new Rectangle(300, 150, 600, 450));

            assertThat(placed).containsExactly(new Rectangle(200, 100, 400, 300));
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

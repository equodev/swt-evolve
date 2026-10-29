package org.eclipse.swt.widgets;

import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.IntegerOn150;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        // A 600-unit shell takes 400 logical pixels -- the 600 physical pixels it is drawn in.
        IntegerOn150.run(() -> assertThat(DeskDisplayBridge.toWindowUnits(new Rectangle(300, 150, 600, 450)))
                .isEqualTo(new Rectangle(200, 100, 400, 300)));
    }

    @Test
    void windows_that_touch_in_swt_units_still_touch_on_screen() {
        IntegerOn150.run(() -> {
            Rectangle left = DeskDisplayBridge.toWindowUnits(new Rectangle(0, 0, 101, 50));
            Rectangle right = DeskDisplayBridge.toWindowUnits(new Rectangle(101, 0, 101, 50));

            assertThat(left.x + left.width).isEqualTo(right.x);
        });
    }

    @Test
    void a_window_origin_is_read_back_in_swt_units() {
        IntegerOn150.run(() -> {
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
        IntegerOn150.run(() -> {
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

    /** The height of the overlay title strip the client draws above the shell, in SWT units. */
    private static final int STRIP = 32;

    /**
     * {@code swt.autoScale=integer} on a monitor at {@code zoom}: SWT draws at 100, so an SWT unit
     * takes 100/zoom of a logical pixel. The window coordinates below are multiples of 15 units,
     * which convert exactly at both 125 % and 150 %.
     */
    private static void atIntegerScale(int zoom, Runnable body) {
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(zoom);
            Config.setClientDeviceZoom(zoom);
            body.run();
        });
    }

    /** A bridge whose window goes where it is placed, on a monitor whose work area starts at {@code workAreaTop}. */
    private static DeskDisplayBridge windowFollowingPlacement(int workAreaTop, int inset, List<Rectangle> placed) {
        DeskDisplayBridge desk = new DeskDisplayBridge(null) {
            @Override
            protected int workAreaTop(Rectangle window) {
                return workAreaTop;
            }

            @Override
            protected void placeNativeWindow(long context, Rectangle w) {
                placed.add(w);
            }

            @Override
            protected long nativeOrigin(long context) {
                Rectangle w = placed.get(placed.size() - 1);
                return ((long) w.x << 32) | (w.y & 0xFFFFFFFFL);
            }
        };
        desk.applyClientInsetTop(inset);
        return desk;
    }

    /** Places {@code shell}'s window and answers where the shell reads back from it. */
    private static Point roundTrip(DeskDisplayBridge desk, Rectangle shell) {
        desk.forwardWindowBounds(desk.windowBoundsFor(shell));
        return desk.shellWindowOrigin(1);
    }

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void a_shell_keeps_its_place_through_its_window_with_a_title_strip(int zoom) {
        atIntegerScale(zoom, () -> {
            DeskDisplayBridge desk = windowFollowingPlacement(0,STRIP, new ArrayList<>());

            assertThat(roundTrip(desk, new Rectangle(300, 152, 600, 450))).isEqualTo(new Point(300, 152));
        });
    }

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void a_shell_restored_at_the_top_of_the_screen_keeps_its_title_strip_on_the_screen(int zoom) {
        atIntegerScale(zoom, () -> {
            List<Rectangle> placed = new ArrayList<>();
            DeskDisplayBridge desk = windowFollowingPlacement(0,STRIP, placed);

            Point shell = roundTrip(desk, new Rectangle(15, 0, 900, 600));

            assertThat(placed.get(0).y)
                    .as("native SWT puts a shell's title bar at the y it is given; the strip above "
                            + "the shell must not push the window off the top of the screen instead")
                    .isZero();
            assertThat(shell)
                    .as("the shell is moved down below the strip rather than shrunk by it")
                    .isEqualTo(new Point(15, STRIP));
        });
    }

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void a_shell_restored_just_above_the_screen_still_gets_its_title_strip_on_it(int zoom) {
        atIntegerScale(zoom, () -> {
            List<Rectangle> placed = new ArrayList<>();
            DeskDisplayBridge desk = windowFollowingPlacement(0,STRIP, placed);

            Point shell = roundTrip(desk, new Rectangle(150, -13, 1552, 942));

            assertThat(placed.get(0).y)
                    .as("native SWT would still show most of this shell's title bar")
                    .isZero();
            roundTrip(desk, new Rectangle(shell.x, shell.y, 1552, 942));
            assertThat(placed.get(1).y)
                    .as("the bounds read back from that window place it where it already is")
                    .isZero();
        });
    }

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void a_shell_a_strip_or_more_above_the_screen_is_left_there(int zoom) {
        atIntegerScale(zoom, () -> {
            DeskDisplayBridge desk = windowFollowingPlacement(0,STRIP, new ArrayList<>());

            assertThat(roundTrip(desk, new Rectangle(0, -898, 600, 450))).isEqualTo(new Point(0, -898));
        });
    }

    @Test
    void the_strip_moves_onto_the_screen_only_while_native_swt_would_show_its_title_bar() {
        DeskDisplayBridge desk = windowFollowingPlacement(0,STRIP, new ArrayList<>());

        assertThat(desk.windowBoundsFor(new Rectangle(0, -STRIP + 1, 600, 450)).y)
                .as("one unit of the title bar still on the screen").isZero();
        assertThat(desk.windowBoundsFor(new Rectangle(0, -STRIP, 600, 450)).y)
                .as("the whole title bar above the screen").isEqualTo(-2 * STRIP);
    }

    /** A taskbar docked at the top of the screen, in SWT units: the work area starts below it. */
    private static final int TOP_TASKBAR = 45;

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void a_taskbar_docked_at_the_top_is_where_the_screen_starts(int zoom) {
        atIntegerScale(zoom, () -> {
            DeskDisplayBridge desk = windowFollowingPlacement(TOP_TASKBAR, STRIP, new ArrayList<>());

            assertThat(desk.windowBoundsFor(new Rectangle(15, 30, 900, 600)).y)
                    .as("native SWT's title bar would show below the taskbar, so the strip goes there")
                    .isEqualTo(TOP_TASKBAR);
            assertThat(roundTrip(desk, new Rectangle(15, 30, 900, 600))).isEqualTo(new Point(15, TOP_TASKBAR + STRIP));
            assertThat(desk.windowBoundsFor(new Rectangle(15, TOP_TASKBAR - STRIP, 900, 600)).y)
                    .as("a title bar native SWT would put wholly behind the taskbar is left there")
                    .isEqualTo(TOP_TASKBAR - 2 * STRIP);
        });
    }

    @Test
    void without_a_native_window_the_work_area_starts_at_the_top_of_the_display() {
        assertThat(new DeskDisplayBridge(null).workAreaTop(new Rectangle(0, 0, 600, 450))).isZero();
    }

    @ParameterizedTest(name = "{0} %")
    @ValueSource(ints = {125, 150})
    void without_a_title_strip_the_window_is_placed_at_the_shells_own_bounds(int zoom) {
        atIntegerScale(zoom, () -> {
            DeskDisplayBridge desk = windowFollowingPlacement(0,0, new ArrayList<>());

            for (Rectangle shell : List.of(new Rectangle(15, 0, 900, 600), new Rectangle(150, -13, 1552, 942))) {
                assertThat(desk.windowBoundsFor(shell))
                        .as("a framed window's caption is the window system's, outside these bounds")
                        .isEqualTo(shell);
            }
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

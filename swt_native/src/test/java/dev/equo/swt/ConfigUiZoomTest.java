package dev.equo.swt;

import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ui_zoom} is how the render layer learns the zoom SWT draws its UI at. It draws at the
 * monitor's own zoom, and {@code swt.autoScale} is free to pick a different one -- so without this
 * the two halves of a mixed tree end up different sizes, and anything SWT places in screen
 * coordinates lands off by the ratio between them.
 */
class ConfigUiZoomTest {

    private ConfigFlags savedFlags;
    private int savedZoom;
    private String savedMode;
    private String savedAutoScale;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedZoom = DPIUtil.getDeviceZoom();
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedAutoScale = System.getProperty("swt.autoScale");
        // The autoscale policy below is what the desktop window follows; the browser tab is covered
        // separately at the end.
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        DPIUtil.setDeviceZoom(savedZoom);
        Config.setClientDeviceZoom(100);
        restoreProperty(ConfigFlags.MODE_PROPERTY, savedMode);
        restoreProperty("swt.autoScale", savedAutoScale);
        Config.setConfigFlags(savedFlags);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    @Test
    void the_flags_carry_the_zoom_swt_draws_at() {
        DPIUtil.setDeviceZoom(100);

        assertThat(Config.getConfigFlags().ui_zoom).isEqualTo(DPIUtil.getDeviceZoom());
    }

    @Test
    void the_scale_follows_autoscale_before_the_client_has_reported_anything() {
        // A control that measures text this early can cache the result for the whole run -- FormText
        // does -- so the scale has to be right on the first call and not only once the report
        // arrives. Until then the monitor is taken to be at 100%, the same assumption DPIUtil boots
        // with, which is what makes `swt.autoScale=150` read as 1.5 from the start.
        DPIUtil.setDeviceZoom(100);
        assertThat(Config.uiScale()).isEqualTo(1.0);
        assertThat(Config.uiScaleFor(150, 100)).isEqualTo(1.5);
        // A monitor the client has reported pulls the other way: the UI is drawn at 150 on a screen
        // that already draws at 200, so the tree is shrunk, not grown.
        assertThat(Config.uiScaleFor(150, 200)).isEqualTo(0.75);
        assertThat(Config.uiScaleFor(0, 100)).isEqualTo(1.0);
        assertThat(Config.uiScaleFor(150, 0)).isEqualTo(1.0);
    }

    @Test
    void an_off_screen_buffer_is_sized_by_the_zoom_alone_not_by_the_part_left_to_the_render_side() {
        // A coordinate is magnified by uiScale and then rasterized at the monitor's zoom, so the
        // pixels an off-screen buffer needs per coordinate are the UI zoom whatever the monitor is.
        // The two only coincide at 100%, which is why reading uiScale() here looks right until
        // someone runs the app on a HiDPI screen and every owner-drawn control goes soft.
        assertThat(Config.rasterScaleFor(150)).isEqualTo(1.5);
        assertThat(Config.rasterScaleFor(200)).isEqualTo(2.0);
        assertThat(Config.rasterScaleFor(100)).isEqualTo(1.0);
        assertThat(Config.rasterScaleFor(0)).isEqualTo(1.0);

        // And it is the zoom SWT ended up with that is read, not the one asked for: setDeviceZoom
        // runs the value through the autoscale policy, which is free to round it.
        DPIUtil.setDeviceZoom(150);
        assertThat(Config.rasterScale()).isEqualTo(DPIUtil.getDeviceZoom() / 100.0);
    }

    @Test
    void the_zoom_is_read_at_every_call_because_the_client_reports_its_monitor_late() {
        // The flags are computed and cached the first time anything asks for them, which happens
        // before the client has said what monitor it is on -- so a cached zoom would be the boot
        // default forever, and the render layer would never be told to follow autoScale.
        DPIUtil.setDeviceZoom(100);
        assertThat(Config.getConfigFlags().ui_zoom).isEqualTo(100);

        DPIUtil.setDeviceZoom(150);

        assertThat(Config.getConfigFlags().ui_zoom).isEqualTo(DPIUtil.getDeviceZoom());
    }

    // ---- browser tab ------------------------------------------------------------------------------

    @Test
    void in_a_browser_tab_the_ui_is_drawn_at_the_reported_zoom_when_the_app_sets_no_autoScale() {
        // A browser reports its page zoom as part of the device pixel ratio. The integer policy (the
        // default of the older SWT releases) rounds 125 down to 100, and drawing at that would shrink
        // a zoomed page back to its old size.
        browserTab();
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(125);
            assertThat(DPIUtil.getDeviceZoom()).isEqualTo(100);

            Config.setClientDeviceZoom(125);

            assertThat(Config.uiZoom()).isEqualTo(125);
            assertThat(Config.getConfigFlags().ui_zoom).isEqualTo(125);
            assertThat(Config.uiScale()).isEqualTo(1.0);
            assertThat(Config.rasterScale()).isEqualTo(1.25);
        });
    }

    @Test
    void in_a_browser_tab_an_explicit_autoScale_is_still_honoured() {
        browserTab();
        System.setProperty("swt.autoScale", "integer");
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(125);

            Config.setClientDeviceZoom(125);

            assertThat(Config.uiZoom()).isEqualTo(100);
            assertThat(Config.uiScale()).isEqualTo(0.8);
        });
    }

    @Test
    void the_desktop_window_keeps_following_the_autoscale_policy() {
        AutoScalePolicy.runWith("integer", () -> {
            DPIUtil.setDeviceZoom(125);

            Config.setClientDeviceZoom(125);

            assertThat(Config.uiZoom()).isEqualTo(100);
        });
    }

    private static void browserTab() {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        System.clearProperty("swt.autoScale");
    }
}

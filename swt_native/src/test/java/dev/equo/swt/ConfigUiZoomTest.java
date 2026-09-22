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

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedZoom = DPIUtil.getDeviceZoom();
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        DPIUtil.setDeviceZoom(savedZoom);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void the_flags_carry_the_zoom_swt_draws_at() {
        DPIUtil.setDeviceZoom(100);

        assertThat(Config.getConfigFlags().ui_zoom).isEqualTo(DPIUtil.getDeviceZoom());
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
}

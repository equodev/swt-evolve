package dev.equo.swt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code swt.autoScale} grows boxes, rows and images and leaves text alone -- native SWT builds a
 * font from the monitor's real DPI, never from the flag. Evolve reaches the same place by dividing
 * a point by the zoom before the render side magnifies the tree back up.
 */
class FontPointScaleZoomTest {

    @Test
    void a_point_shrinks_by_the_factor_the_tree_is_magnified_by() {
        assertThat(FontMetricsUtil.pointScaleForZoom(96 / 72.0, 1.5))
                .isEqualTo(96 / 72.0 / 1.5);
    }

    @Test
    void a_run_that_leaves_autoscale_alone_measures_exactly_as_it_always_did() {
        assertThat(FontMetricsUtil.pointScaleForZoom(96 / 72.0, 1.0)).isEqualTo(96 / 72.0);
        // The client reports its monitor well after boot; until it does there is no ratio to apply.
        assertThat(FontMetricsUtil.pointScaleForZoom(96 / 72.0, 0.0)).isEqualTo(96 / 72.0);
        assertThat(FontMetricsUtil.pointScaleForZoom(96 / 72.0, -1.0)).isEqualTo(96 / 72.0);
    }
}

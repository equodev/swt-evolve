package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.DartLabel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The render side draws the tree at {@code uiScale} and paints every {@code Text} through a
 * {@code textScaler} of {@code 1 / uiScale}, so a glyph keeps its true size whatever the zoom. A
 * size computed here for text the render side paints that way has to grow by the same factor, or
 * the box it lays out is narrower and shorter than the text drawn into it.
 */
class ThemeTextZoomSizeTest {

    private int savedZoom;
    private String savedMode;

    // A native window: in a browser tab without swt.autoScale the UI zoom follows the client's, and
    // there is no rounding to make up for.
    @BeforeEach
    void captureState() {
        savedZoom = DPIUtil.getDeviceZoom();
        savedMode = ConfigFlags.mode();
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        DPIUtil.setDeviceZoom(100);
        Config.setClientDeviceZoom(100);
    }

    @AfterEach
    void restoreState() {
        Config.setClientDeviceZoom(100);
        DPIUtil.setDeviceZoom(savedZoom);
        if (savedMode == null)
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else
            ConfigFlags.setMode(savedMode);
    }

    @Test
    void label_text_grows_with_the_text_scaler_on_a_monitor_autoscale_rounds_down() {
        DartLabel label = label("Always run in background");
        PointD atMonitorZoom = LabelSizes.computeSizes(label, SWT.DEFAULT, SWT.DEFAULT, true).text;
        assertThat(atMonitorZoom.x()).isPositive();

        // A 125% monitor under the default autoScale: SWT lays out at 100, so the tree is drawn at
        // 0.8 and the render side paints text 1.25 times larger in layout units.
        Config.setClientDeviceZoom(125);
        assertThat(Config.uiScale()).isEqualTo(0.8);
        PointD scaled = LabelSizes.computeSizes(label, SWT.DEFAULT, SWT.DEFAULT, true).text;

        assertThat(scaled.x()).isCloseTo(atMonitorZoom.x() / 0.8, within(0.01));
        assertThat(scaled.y()).isCloseTo(atMonitorZoom.y() / 0.8, within(0.01));
    }

    private static DartLabel label(String text) {
        DartLabel label = mock(DartLabel.class);
        when(label.getStyle()).thenReturn(SWT.HORIZONTAL);
        when(label.getText()).thenReturn(text);
        when(label._displayedText()).thenReturn(text);
        return label;
    }
}

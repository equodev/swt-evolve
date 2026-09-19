package javafx.embed.swt;

import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * An embedded JavaFX scene is rasterised by FXCanvas and blitted into a canvas measured in points.
 * The render side composites that blit at the display's real pixel density, so a raster taken at
 * the point size is magnified there and every sub-point detail the scene drew — glyph stems, shape
 * edges — is already lost by the time it is.
 *
 * <p>Reported against a scene holding a labelled diagram and a colour legend: the small text came
 * out barely legible and the flat colour shapes looked pixelated. The canvas was 373x573 points on
 * a 125% display and the frame FXCanvas produced was 373x573 — the raster carried no pixel density
 * at all. The sizes below are that report's.</p>
 */
class FXCanvasSnapshotScaleTest {

    /** The reported canvas, in points. */
    private static final int CANVAS_W = 373;
    private static final int CANVAS_H = 573;

    private final int originalZoom = DPIUtil.getDeviceZoom();

    @AfterEach
    void restoreZoom() {
        DPIUtil.setDeviceZoom(originalZoom);
    }

    @Test
    void a_scaled_display_rasterises_at_its_pixel_density() {
        assertThat(FXCanvas.pixelExtent(CANVAS_W, 125)).isEqualTo(466);
        assertThat(FXCanvas.pixelExtent(CANVAS_H, 125)).isEqualTo(716);
    }

    @Test
    void an_unscaled_display_rasterises_at_the_point_size() {
        assertThat(FXCanvas.pixelExtent(CANVAS_W, 100)).isEqualTo(CANVAS_W);
        assertThat(FXCanvas.pixelExtent(CANVAS_H, 100)).isEqualTo(CANVAS_H);
    }

    /**
     * A zoom below 100 would rasterise smaller than the slot the frame is blitted into, discarding
     * detail to no purpose.
     */
    @Test
    void a_zoom_under_100_never_shrinks_the_raster() {
        assertThat(FXCanvas.pixelExtent(CANVAS_W, 75)).isEqualTo(CANVAS_W);
        assertThat(FXCanvas.pixelExtent(CANVAS_W, 0)).isEqualTo(CANVAS_W);
    }

    /** Rounding a small dimension down must not produce an image JavaFX rejects. */
    @Test
    void a_one_point_dimension_stays_at_least_one_pixel() {
        assertThat(FXCanvas.pixelExtent(1, 125)).isGreaterThanOrEqualTo(1);
    }

    /**
     * The raster tracks the display's own ratio. {@code swt.autoScale} rounds that ratio for
     * layout — under the {@code integer} policy a 125% display lays out at 100 — and rasterising
     * at the rounded value would hand back the unscaled frame this bug reported. Whether the two
     * differ depends on the policy in force, so this pins the unrounded value rather than the gap.
     */
    @Test
    void the_raster_follows_the_display_ratio() {
        assumeTrue(unroundedZoomAvailable(), "SWT before 3.128 exposes only the rounded zoom");
        DPIUtil.setDeviceZoom(125);

        assertThat(FXCanvas.deviceZoomPercent()).isEqualTo(125);
        assertThat(FXCanvas.pixelExtent(CANVAS_W, FXCanvas.deviceZoomPercent())).isEqualTo(466);
    }

    private static boolean unroundedZoomAvailable() {
        try {
            DPIUtil.class.getMethod("getNativeDeviceZoom");
            return true;
        } catch (NoSuchMethodException absent) {
            return false;
        }
    }
}

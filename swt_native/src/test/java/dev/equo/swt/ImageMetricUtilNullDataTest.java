package dev.equo.swt;

import dev.equo.swt.size.PointD;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An Image whose imageData was never populated answers getImageData() with null rather than
 * throwing, so the sizing helpers have to survive it.
 */
public class ImageMetricUtilNullDataTest {

    @Test
    public void null_image_data_measures_as_zero() {
        PointD size = ImageMetricUtil.getImageSize((ImageData) null);

        assertThat(size).isNotNull();
        assertThat(size.x()).isZero();
        assertThat(size.y()).isZero();
    }

    @Test
    public void null_image_measures_as_zero() {
        PointD size = ImageMetricUtil.getImageSize((org.eclipse.swt.graphics.Image) null);

        assertThat(size).isNotNull();
        assertThat(size.x()).isZero();
        assertThat(size.y()).isZero();
    }

    @Test
    public void real_image_data_still_measures_its_own_size() {
        ImageData data = new ImageData(24, 16, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));

        PointD size = ImageMetricUtil.getImageSize(data);

        assertThat(size.x()).isEqualTo(24);
        assertThat(size.y()).isEqualTo(16);
    }
}

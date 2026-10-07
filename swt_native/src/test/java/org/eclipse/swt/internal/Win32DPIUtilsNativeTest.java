package org.eclipse.swt.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Applications built against Windows SWT call {@code Win32DPIUtils} directly (an editor sizing its
 * fonts with {@code pointToPixel}, say); without it they fail with NoClassDefFoundError.
 */
@Tag("native-unit")
public class Win32DPIUtilsNativeTest {

    @Test
    public void points_and_pixels_convert_at_the_given_zoom() {
        assertThat(Win32DPIUtils.pointToPixel(10f, 150)).isEqualTo(15f);
        assertThat(Win32DPIUtils.pointToPixel(10f, 100)).isEqualTo(10f);
        assertThat(Win32DPIUtils.pointToPixel((Drawable) null, 7, 150)).isEqualTo(11);
        assertThat(Win32DPIUtils.pixelToPoint((Drawable) null, 15f, 150)).isEqualTo(10f);
        assertThat(Win32DPIUtils.pointToPixel(new int[] {2, 4}, 200)).containsExactly(4, 8);
    }

    @Test
    public void swt_default_is_never_scaled() {
        assertThat(Win32DPIUtils.pointToPixel((float) SWT.DEFAULT, 200)).isEqualTo((float) SWT.DEFAULT);
        assertThat(Win32DPIUtils.pointToPixel((Drawable) null, SWT.DEFAULT, 200)).isEqualTo(SWT.DEFAULT);
        assertThat(Win32DPIUtils.pointToPixelAsSize(new Point(SWT.DEFAULT, 10), 200)).isEqualTo(new Point(SWT.DEFAULT, 20));
    }

    @Test
    public void a_sufficiently_large_size_rounds_up() {
        assertThat(Win32DPIUtils.pointToPixelAsSize(new Point(1, 1), 120)).isEqualTo(new Point(1, 1));
        assertThat(Win32DPIUtils.pointToPixelAsSufficientlyLargeSize(new Point(1, 1), 120)).isEqualTo(new Point(2, 2));
        assertThat(Win32DPIUtils.pointToPixelWithSufficientlyLargeSize(new Rectangle(0, 0, 1, 1), 120))
                .isEqualTo(new Rectangle(0, 0, 2, 2));
    }

    @Test
    public void rectangles_and_bounds_scale_every_edge() {
        assertThat(Win32DPIUtils.pointToPixel(new Rectangle(1, 1, 10, 10), 150)).isEqualTo(new Rectangle(2, 2, 15, 15));
        assertThat(Win32DPIUtils.pixelToPoint(new Rectangle(2, 2, 15, 15), 150)).isEqualTo(new Rectangle(1, 1, 10, 10));
        assertThat(Win32DPIUtils.scaleBounds(new Rectangle(10, 10, 20, 20), 200, 100)).isEqualTo(new Rectangle(20, 20, 40, 40));
    }

    @Test
    public void dpi_awareness_has_nothing_to_change() {
        assertThat(Win32DPIUtils.setDPIAwareness(2)).isTrue();
        assertThat(Win32DPIUtils.<String>runWithProperDPIAwareness(null, () -> "ran")).isEqualTo("ran");
    }
}

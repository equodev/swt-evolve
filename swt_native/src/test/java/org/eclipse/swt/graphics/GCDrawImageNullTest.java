package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.eclipse.swt.widgets.Mocks.drawable;

class GCDrawImageNullTest extends SerializeTestBase {

    // e4's ImageBasedFrame paints its nine-patch with 0x0 rectangles and a null frame image when CSS
    // supplied only the handle image, so the zero-size early return has to win over the null check --
    // as it does upstream. Rejecting the null first turns a documented no-op into an error on every
    // repaint tick.
    @Test
    void zero_sized_draw_of_a_null_image_is_a_no_op() {
        GC gc = new GC(drawable());

        assertThatCode(() -> gc.drawImage(null, 0, 0, 0, 0, 0, 0, 0, 0)).doesNotThrowAnyException();
    }

    @Test
    void null_image_is_still_rejected_when_the_rectangles_are_real() {
        GC gc = new GC(drawable());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> gc.drawImage(null, 0, 0, 8, 8, 0, 0, 8, 8));
    }
}

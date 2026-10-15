package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Flutter lays a button's image out at the image's own size, so the preferred size has to make
 * room for all of it. JFace's {@code ColorSelector} paints its swatch into a button image sized
 * from the font, which is wider than an icon.
 */
class ButtonImageSizeTest extends SerializeTestBase {

    private static Point preferred(int style, Image image) {
        Button button = new Button(Mocks.shell(), style);
        button.setImage(image);
        return button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.PUSH, SWT.TOGGLE, SWT.CHECK, SWT.RADIO})
    void the_preferred_size_holds_the_whole_image(int style) {
        Image swatch = new Image(Mocks.device(), 54, 20);
        try {
            Point size = preferred(style, swatch);

            assertThat(size.x).isGreaterThanOrEqualTo(54);
            assertThat(size.y).isGreaterThanOrEqualTo(20);
        } finally {
            swatch.dispose();
        }
    }

    /** {@code ColorSelector} disposes the button's image before it sets the next one, and a layout can run in between. */
    @ParameterizedTest
    @ValueSource(ints = {SWT.PUSH, SWT.TOGGLE, SWT.CHECK, SWT.RADIO})
    void a_disposed_image_does_not_break_layout(int style) {
        Button button = new Button(Mocks.shell(), style);
        Image swatch = new Image(Mocks.device(), 54, 20);
        button.setImage(swatch);
        swatch.dispose();

        assertThatCode(() -> button.computeSize(SWT.DEFAULT, SWT.DEFAULT)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.PUSH, SWT.TOGGLE, SWT.CHECK, SWT.RADIO})
    void a_wider_image_makes_a_wider_button(int style) {
        Image narrow = new Image(Mocks.device(), 16, 16);
        Image wide = new Image(Mocks.device(), 48, 16);
        try {
            assertThat(preferred(style, wide).x - preferred(style, narrow).x).isEqualTo(32);
        } finally {
            narrow.dispose();
            wide.dispose();
        }
    }
}

package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Both direction bits together mean "automatic" only where the release has an automatic text direction;
 * elsewhere SWT resolves them to left to right, as {@code setOrientation} does.
 */
class TextLayoutHelperTextDirectionTest {

    private static final int BOTH = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;

    @Test
    void both_bits_without_auto_are_left_to_right() {
        assertThat(TextLayoutHelper.textDirection(SWT.RIGHT_TO_LEFT, BOTH, false)).isEqualTo(SWT.LEFT_TO_RIGHT);
    }

    @Test
    void both_bits_with_auto_stay_automatic() {
        assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, BOTH, true)).isEqualTo(BOTH);
        assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, BOTH)).isEqualTo(BOTH);
    }

    @Test
    void no_direction_bit_keeps_the_current_direction() {
        for (boolean auto : new boolean[] {false, true}) {
            assertThat(TextLayoutHelper.textDirection(SWT.RIGHT_TO_LEFT, 0, auto)).isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.textDirection(BOTH, SWT.BORDER, auto)).isEqualTo(BOTH);
        }
    }

    @Test
    void a_single_direction_bit_is_taken_as_is() {
        for (boolean auto : new boolean[] {false, true}) {
            assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT, auto)).isEqualTo(SWT.RIGHT_TO_LEFT);
            assertThat(TextLayoutHelper.textDirection(SWT.RIGHT_TO_LEFT, SWT.LEFT_TO_RIGHT, auto)).isEqualTo(SWT.LEFT_TO_RIGHT);
            assertThat(TextLayoutHelper.textDirection(SWT.LEFT_TO_RIGHT, SWT.RIGHT_TO_LEFT | SWT.BORDER, auto))
                    .isEqualTo(SWT.RIGHT_TO_LEFT);
        }
    }
}

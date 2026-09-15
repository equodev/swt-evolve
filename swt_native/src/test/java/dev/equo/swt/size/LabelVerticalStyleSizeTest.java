package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartLabel;
import org.junit.jupiter.api.Test;

/**
 * SWT.VERTICAL and SWT.V_SCROLL are the same bit (1 << 9). A Label only reads it when
 * SWT.SEPARATOR is set -- Label.checkStyle() normalizes HORIZONTAL/VERTICAL inside the
 * SEPARATOR branch only -- so on a text label the bit is inert and must not change its size.
 * JFace clients reach this: a MessageDialog that returns SWT.WRAP | SWT.V_SCROLL from
 * getMessageLabelStyle() builds its message label with that style.
 */
class LabelVerticalStyleSizeTest {

    private static final String MESSAGE =
            "A required field is still empty. Fill it in before saving.";

    private static DartLabel label(int style) {
        DartLabel label = mock(DartLabel.class);
        when(label.getStyle()).thenReturn(style);
        when(label.getText()).thenReturn(MESSAGE);
        when(label.getImage()).thenReturn(null);
        return label;
    }

    @Test
    void an_inert_vertical_bit_does_not_rotate_a_text_label() {
        Point plain = LabelSizes.computeSize(label(SWT.WRAP), SWT.DEFAULT, SWT.DEFAULT, true);
        Point withBit = LabelSizes.computeSize(
                label(SWT.WRAP | SWT.V_SCROLL), SWT.DEFAULT, SWT.DEFAULT, true);

        assertThat(withBit).isEqualTo(plain);
    }

    @Test
    void an_inert_vertical_bit_keeps_the_label_wrapping_under_a_width_hint() {
        Point plain = LabelSizes.computeSize(label(SWT.WRAP), 260, SWT.DEFAULT, true);
        Point withBit = LabelSizes.computeSize(label(SWT.WRAP | SWT.V_SCROLL), 260, SWT.DEFAULT, true);

        assertThat(withBit).isEqualTo(plain);
    }
}

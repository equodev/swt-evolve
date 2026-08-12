package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartLabel;
import org.junit.jupiter.api.Test;

class LabelEmptySizeTest {

    @Test
    void empty_label_computes_to_zero_width() {
        DartLabel label = mock(DartLabel.class);
        when(label.getStyle()).thenReturn(SWT.HORIZONTAL);
        when(label.getText()).thenReturn("");
        when(label.getImage()).thenReturn(null);

        Point size = LabelSizes.computeSize(label, SWT.DEFAULT, SWT.DEFAULT, true);

        assertThat(size.x)
                .as("an empty label (no text, no image) must not reserve a MIN_WIDTH column")
                .isZero();
    }
}

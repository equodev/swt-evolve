package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A Table's click Selection carries the held button in its stateMask, whichever button it is. */
class TableButtonMaskTest {

    @Test
    @DisplayName("each mouse button maps to its own stateMask bit, the middle one included")
    void everyButtonHasItsBit() {
        assertThat(TableHelper.buttonMask(1)).isEqualTo(SWT.BUTTON1);
        assertThat(TableHelper.buttonMask(2)).isEqualTo(SWT.BUTTON2);
        assertThat(TableHelper.buttonMask(3)).isEqualTo(SWT.BUTTON3);
        assertThat(TableHelper.buttonMask(4)).isEqualTo(SWT.BUTTON4);
        assertThat(TableHelper.buttonMask(5)).isEqualTo(SWT.BUTTON5);
        assertThat(TableHelper.buttonMask(0)).isZero();
    }
}

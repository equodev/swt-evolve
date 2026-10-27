package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * The CCombo field renders {@code text}; a selection made on the Java side, such as the one an
 * arrow key makes while the list is open, has to ship it.
 */
@ExtendWith(Mocks.class)
public class CComboSelectPushesTextTest extends SerializeTestBase {

    private static VCCombo selectOn(int style, int from, int to) {
        CCombo combo = new CCombo(shell(), style);
        combo.setItems(new String[] {"MD", "TVD", "TVDSS", "TWT"});
        combo.select(from);
        VCCombo value = ((DartCCombo) combo.getImpl()).getValue();
        value.clearDirty();
        combo.select(to);
        return value;
    }

    @Test
    void selectingAnItemShipsItsText() {
        assertThat(selectOn(SWT.BORDER, 1, 2).changedKeys()).contains(VCCombo.TEXT);
    }

    @Test
    void selectingAnItemOnAReadOnlyComboShipsItsText() {
        assertThat(selectOn(SWT.BORDER | SWT.READ_ONLY, 3, 2).changedKeys()).contains(VCCombo.TEXT);
    }

    @Test
    void clearingTheSelectionShipsTheEmptyText() {
        assertThat(selectOn(SWT.BORDER, 1, -1).changedKeys()).contains(VCCombo.TEXT);
    }
}

package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** What {@link DartCCombo} answers to the API and keeps for the render side: its text is its Text's. */
@ExtendWith(Mocks.class)
public class CComboImplementationNativeTest extends SerializeTestBase {

    private static CCombo abc(int style, int selected) {
        CCombo combo = new CCombo(shell(), style);
        combo.setItems(new String[] {"a", "bb", "ccc"});
        if (selected != -1)
            combo.select(selected);
        return combo;
    }

    private static DartCCombo impl(CCombo combo) {
        return (DartCCombo) combo.getImpl();
    }

    @Test
    void removingTheSelectedItemLeavesNothingSelectedAndKeepsTheText() {
        CCombo combo = abc(SWT.BORDER, 1);

        combo.remove(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
        assertThat(combo.getText()).isEqualTo("bb");
        assertThat(combo.getItems()).containsExactly("a", "ccc");
        assertThat(impl(combo).getValue().changedKeys()).contains(VCCombo.ITEMS);
    }

    @Test
    void removingAnItemBeforeTheSelectionMovesTheSelectionWithItsItem() {
        CCombo combo = abc(SWT.BORDER, 2);

        combo.remove(0);

        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(combo.getText()).isEqualTo("ccc");
    }

    @Test
    void removingARangeBeforeTheSelectionMovesTheSelectionWithItsItem() {
        CCombo combo = abc(SWT.BORDER, 2);

        combo.remove(0, 1);

        assertThat(combo.getSelectionIndex()).isEqualTo(0);
        assertThat(combo.getText()).isEqualTo("ccc");
    }

    @Test
    void removingTheLastSelectedItemLeavesNothingSelected() {
        CCombo combo = abc(SWT.BORDER, 2);

        combo.remove(2);

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
    }

    @Test
    void removingEveryItemEmptiesTheSelectionAndTheText() {
        CCombo combo = abc(SWT.BORDER, 1);

        combo.removeAll();

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
        assertThat(combo.getItems()).isEmpty();
        assertThat(combo.getText()).isEmpty();
    }

    @Test
    void anItemInsertedAtTheSelectionPushesTheSelectionDown() {
        CCombo combo = abc(SWT.BORDER, 1);

        combo.add("z", 0);

        assertThat(combo.getItems()).containsExactly("z", "a", "bb", "ccc");
        assertThat(combo.getSelectionIndex()).isEqualTo(2);
        assertThat(combo.getText()).isEqualTo("bb");
    }

    @Test
    void replacingTheSelectedItemKeepsTheSelectionAndTheText() {
        CCombo combo = abc(SWT.BORDER, 1);

        combo.setItem(1, "B");
        combo.select(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(combo.getText()).isEqualTo("bb");
        assertThat(combo.getItem(1)).isEqualTo("B");
    }

    @Test
    void setItemsClearsTheTextOfAReadOnlyCombo() {
        CCombo combo = abc(SWT.BORDER | SWT.READ_ONLY, 1);

        combo.setItems(new String[] {"x", "y"});

        assertThat(combo.getText()).isEmpty();
    }

    @Test
    void theItemsAreCopiedOnTheWayInAndOut() {
        String[] given = {"a", "b"};
        CCombo combo = new CCombo(shell(), SWT.BORDER);
        combo.setItems(given);

        given[0] = "changed";
        combo.getItems()[1] = "changed";

        assertThat(combo.getItems()).containsExactly("a", "b");
    }

    @Test
    void textChangesShipTheText() {
        CCombo combo = abc(SWT.BORDER, -1);
        impl(combo).getValue().clearDirty();

        combo.setText("typed");

        assertThat(impl(combo).getValue().changedKeys()).contains(VCCombo.TEXT);
    }

    @Test
    void theItemToolTipsKeyIsAlsoPlainData() {
        CCombo combo = abc(SWT.BORDER, -1);
        String key = "org.eclipse.swt.custom.CCombo.itemToolTips";
        String[] tips = {"first", "second", "third"};

        combo.setData(key, tips);

        assertThat(combo.getData(key)).isSameAs(tips);
        assertThat(impl(combo)._itemTooltips()).isSameAs(tips);
    }

    @Test
    void aComboThatIsNotShownKeepsItsListClosed() {
        CCombo combo = abc(SWT.BORDER, 1);
        combo.getShell().setVisible(false);

        combo.setListVisible(true);

        assertThat(combo.getListVisible()).isFalse();
    }
}

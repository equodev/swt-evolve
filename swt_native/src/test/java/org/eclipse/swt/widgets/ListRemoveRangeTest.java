package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** Removing a range of items from a List keeps the selection on the items that stay. */
@ExtendWith(Mocks.class)
class ListRemoveRangeTest extends SerializeTestBase {

    private static List list(int style, int... selected) {
        List list = new List(shell(), style);
        list.setItems(new String[] {"a", "b", "c", "d", "e", "f"});
        list.select(selected);
        return list;
    }

    @Test
    void aSelectedItemAfterTheRangeKeepsItsSelection() {
        List list = list(SWT.SINGLE, 5);

        list.remove(0, 1);

        assertThat(list.getSelectionIndices()).containsExactly(3);
        assertThat(list.getSelection()).containsExactly("f");
    }

    @Test
    void theSelectedItemsInTheRangeGoAndTheLaterOnesMoveUp() {
        List list = list(SWT.MULTI, 0, 2, 4);

        list.remove(1, 2);

        assertThat(list.getSelectionIndices()).containsExactly(0, 2);
        assertThat(list.getSelection()).containsExactly("a", "e");
    }
}

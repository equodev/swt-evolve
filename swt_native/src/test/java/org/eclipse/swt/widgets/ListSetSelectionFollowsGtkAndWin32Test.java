package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * Where GTK and Win32 agree against Cocoa, setSelection follows them: a null among the strings is
 * skipped rather than rejected, and the item brought into view is the start of a range, the first index
 * given and the first match of the first string.
 */
@ExtendWith(Mocks.class)
class ListSetSelectionFollowsGtkAndWin32Test extends SerializeTestBase {

    private static List list(int style) {
        List list = new List(shell(), style);
        list.setBounds(0, 0, 200, 120);
        list.setItems(new String[] {"a", "b", "c", "d", "e", "f", "g", "h"});
        return list;
    }

    @Test
    void aNullAmongTheStringsIsSkipped() {
        List list = list(SWT.MULTI);

        list.setSelection(new String[] { "b", null, "d" });

        assertThat(list.getSelectionIndices()).containsExactlyInAnyOrder(1, 3);
    }

    @Test
    void aSingleListTakesNothingFromOneNullString() {
        List list = list(SWT.SINGLE);

        assertThatCode(() -> list.setSelection(new String[] { null })).doesNotThrowAnyException();

        assertThat(list.getSelectionIndices()).isEmpty();
    }

    @Test
    void theNullArrayItselfIsStillRejected() {
        List list = list(SWT.MULTI);

        assertThatCode(() -> list.setSelection((String[]) null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRangeShowsItsStart() {
        List list = list(SWT.MULTI);
        list.setTopIndex(7);

        list.setSelection(2, 6);

        assertThat(list.getTopIndex()).isEqualTo(2);
    }

    @Test
    void indicesShowTheFirstOneGiven() {
        List list = list(SWT.MULTI);
        list.setTopIndex(7);

        list.setSelection(new int[] { 2, 6 });

        assertThat(list.getTopIndex()).isEqualTo(2);
    }

    @Test
    void stringsShowTheFirstMatchOfTheFirstString() {
        List list = list(SWT.MULTI);
        list.setTopIndex(7);

        list.setSelection(new String[] { "c", "g" });

        assertThat(list.getTopIndex()).isEqualTo(2);
    }
}

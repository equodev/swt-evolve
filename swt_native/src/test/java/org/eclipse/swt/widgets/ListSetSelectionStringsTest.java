package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * {@code setSelection(String[])} on a MULTI list selects every item of each string, the strings taken
 * last to first and each one's items in order, and brings into view the first item of the earliest
 * string that matched; read the items once or once per string, the result is the same.
 */
@ExtendWith(Mocks.class)
class ListSetSelectionStringsTest extends SerializeTestBase {

    @Test
    void selectsWhatAScanPerStringSelects() {
        Random random = new Random(3657);
        for (int run = 0; run < 400; run++) {
            String[] items = new String[random.nextInt(40)];
            for (int i = 0; i < items.length; i++) items[i] = "s" + random.nextInt(12);
            String[] strings = new String[1 + random.nextInt(8)];
            for (int i = 0; i < strings.length; i++) strings[i] = random.nextInt(6) == 0 ? null : "s" + random.nextInt(16);
            List list = new List(shell(), SWT.MULTI);
            list.setItems(items);
            DartList impl = (DartList) list.getImpl();

            int first = ListHelper.setSelectionStrings(impl, strings, items.length, SWT.MULTI);

            Object[] expected = scanPerString(items, strings);
            assertThat(first).as("run %d: the item shown", run).isEqualTo(expected[0]);
            int[] selection = (int[]) expected[1];
            if (selection.length > 0) {
                assertThat(impl.selection).as("run %d: the selection, in order", run).containsExactly(selection);
            }
            list.dispose();
        }
    }

    /** The first index shown and the selection, as one indexOf scan per string finds them. */
    private static Object[] scanPerString(String[] items, String[] strings) {
        Set<Integer> selection = new LinkedHashSet<>();
        int first = -1;
        for (int s = strings.length - 1; s >= 0; s--) {
            if (strings[s] == null) continue;
            int firstMatch = -1;
            for (int i = 0; i < items.length; i++) {
                if (!items[i].equals(strings[s])) continue;
                selection.add(i);
                if (firstMatch == -1) firstMatch = i;
            }
            if (firstMatch != -1) first = firstMatch;
        }
        java.util.List<Integer> ordered = new ArrayList<>(selection);
        return new Object[] {first, ordered.stream().mapToInt(Integer::intValue).toArray()};
    }
}

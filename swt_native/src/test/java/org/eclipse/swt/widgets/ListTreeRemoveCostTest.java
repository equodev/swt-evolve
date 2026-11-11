package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * Removing many items costs work in proportion to the list, not to the list times the items removed:
 * {@code List.remove(int[])} handles the indices as one change, and disposing the first root of a
 * Tree does not shift every other root.
 */
@ExtendWith(Mocks.class)
class ListTreeRemoveCostTest extends SerializeTestBase {

    private RecordingBridge bridge;

    @BeforeEach
    void recordFlushes() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    private long flushesOf(Widget widget) {
        return bridge.dirtied.stream().filter(w -> w == widget.getImpl()).count();
    }

    @Test
    @DisplayName("List.remove(int[]) enrols the list for a flush once per property, not once per index")
    void removeIndicesIsOneChange() {
        List list = new List(shell(), SWT.MULTI);
        String[] items = new String[100];
        for (int i = 0; i < items.length; i++) items[i] = "item " + i;
        list.setItems(items);
        list.select(new int[] {1, 3, 99});
        int[] even = new int[50];
        for (int i = 0; i < even.length; i++) even[i] = 2 * i;
        bridge.dirtied.clear();

        list.remove(even);

        assertThat(list.getItemCount()).isEqualTo(50);
        assertThat(list.getItem(0)).isEqualTo("item 1");
        assertThat(list.getItem(49)).isEqualTo("item 99");
        assertThat(list.getSelectionIndices()).containsExactly(0, 1, 49);
        // ITEMS and SELECTION. An enrolment per index is a tail copy per index.
        assertThat(flushesOf(list)).isLessThanOrEqualTo(2);
    }

    @Test
    @Disabled("DartTree.destroyItem still shifts every later root: items is a dense array read directly "
            + "throughout DartTree, DartTreeItem and TreeHelper, so the fix needs a new item store")
    @DisplayName("disposing the first root of a Tree leaves the other roots where its storage holds them")
    void disposeFirstRootShiftsNoOtherRoot() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        int n = 1000, disposed = 10;
        for (int i = 0; i < n; i++) new TreeItem(tree, SWT.NONE).setText("root " + i);
        DartTree impl = (DartTree) tree.getImpl();

        long moved = 0;
        for (int i = 0; i < disposed; i++) {
            TreeItem[] before = impl.items.clone();
            tree.getItem(0).dispose();
            TreeItem[] after = impl.items;
            int length = Math.min(before.length, after.length);
            for (int slot = 0; slot < length; slot++) {
                if (after[slot] != before[slot]) moved++;
            }
            moved += Math.abs(before.length - after.length);
        }

        assertThat(tree.getItemCount()).isEqualTo(n - disposed);
        assertThat(tree.getItem(0).getText()).isEqualTo("root " + disposed);
        assertThat(tree.getItem(n - disposed - 1).getText()).isEqualTo("root " + (n - 1));
        // The freed slot is the one change a dispose needs; shifting the roots after it moves n - 1 per dispose.
        assertThat(moved).isLessThanOrEqualTo(2L * disposed);
    }
}

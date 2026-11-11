package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

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
    @DisplayName("disposing the first root of a Tree leaves the other roots where its storage holds them")
    void disposeFirstRootShiftsNoOtherRoot() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        int n = 1000, disposed = 10;
        for (int i = 0; i < n; i++) new TreeItem(tree, SWT.NONE).setText("root " + i);
        DartTree impl = (DartTree) tree.getImpl();

        long moved = slotsMovedByFrontDisposes(() -> impl.items, () -> tree.getItem(0), disposed);

        assertThat(tree.getItemCount()).isEqualTo(n - disposed);
        assertThat(tree.getItem(0).getText()).isEqualTo("root " + disposed);
        assertThat(tree.getItem(n - disposed - 1).getText()).isEqualTo("root " + (n - 1));
        // The freed slot is the one change a dispose needs; shifting the roots after it moves n - 1 per dispose.
        assertThat(moved).isLessThanOrEqualTo(2L * disposed);
    }

    @Test
    @DisplayName("disposing the first child of a TreeItem leaves its other children where its storage holds them")
    void disposeFirstChildShiftsNoOtherChild() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        int n = 1000, disposed = 10;
        for (int i = 0; i < n; i++) new TreeItem(parent, SWT.NONE).setText("child " + i);
        DartTreeItem impl = (DartTreeItem) parent.getImpl();

        long moved = slotsMovedByFrontDisposes(() -> impl.items, () -> parent.getItem(0), disposed);

        assertThat(parent.getItemCount()).isEqualTo(n - disposed);
        assertThat(parent.getItem(0).getText()).isEqualTo("child " + disposed);
        assertThat(parent.getItem(n - disposed - 1).getText()).isEqualTo("child " + (n - 1));
        assertThat(moved).isLessThanOrEqualTo(2L * disposed);
    }

    @Test
    @DisplayName("after front disposes every index-based Tree and TreeItem call still sees the items in order")
    void frontDisposesKeepEveryIndex() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        for (int i = 0; i < 20; i++) new TreeItem(tree, SWT.NONE).setText("root " + i);
        for (int i = 0; i < 6; i++) tree.getItem(0).dispose();

        new TreeItem(tree, SWT.NONE, 0).setText("root first");
        new TreeItem(tree, SWT.NONE).setText("root last");
        new TreeItem(tree, SWT.NONE, 2).setText("root third");
        assertItemsInOrder(tree.getItems(), tree.getItemCount(), tree::getItem, tree::indexOf,
                "root first", "root 6", "root third", "root 7", "root 8", "root 9", "root 10", "root 11",
                "root 12", "root 13", "root 14", "root 15", "root 16", "root 17", "root 18", "root 19", "root last");

        TreeItem holder = new TreeItem(tree, SWT.NONE);
        for (int i = 0; i < 8; i++) new TreeItem(holder, SWT.NONE).setText("child " + i);
        holder.setExpanded(true);
        for (int i = 0; i < 3; i++) holder.getItem(0).dispose();
        new TreeItem(holder, SWT.NONE, 1).setText("child inserted");
        assertItemsInOrder(holder.getItems(), holder.getItemCount(), holder::getItem, holder::indexOf,
                "child 3", "child inserted", "child 4", "child 5", "child 6", "child 7");

        holder.setItemCount(3);
        assertItemsInOrder(holder.getItems(), holder.getItemCount(), holder::getItem, holder::indexOf,
                "child 3", "child inserted", "child 4");
        tree.setItemCount(2);
        assertItemsInOrder(tree.getItems(), tree.getItemCount(), tree::getItem, tree::indexOf,
                "root first", "root 6");
    }

    @Test
    @DisplayName("roots that no longer start at the front stay in order through middle and last disposes and appends that fill the array")
    void offsetRootsKeepOrderThroughBackChanges() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        for (int i = 0; i < 8; i++) new TreeItem(tree, SWT.NONE).setText("root " + i);
        tree.getItem(0).dispose();
        tree.getItem(0).dispose();

        new TreeItem(tree, SWT.NONE).setText("a");
        tree.getItem(3).dispose();
        tree.getItem(0).dispose();
        tree.getItem(4).dispose();
        new TreeItem(tree, SWT.NONE, 2).setText("middle");
        for (String text : new String[] {"b", "c", "d", "e"}) new TreeItem(tree, SWT.NONE).setText(text);

        assertItemsInOrder(tree.getItems(), tree.getItemCount(), tree::getItem, tree::indexOf,
                "root 3", "root 4", "middle", "root 6", "root 7", "b", "c", "d", "e");
    }

    // The tests below dispose items from the front first, so that the last items sit past the
    // item count in their array: a loop from 0 that skipped the compaction would miss them.

    @Test
    @DisplayName("clearAll(true) after front disposes clears the last root and the last child")
    void clearAllAfterFrontDisposesReachesTheLastItems() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem holder = rootsAfterFrontDisposes(tree);
        TreeItem lastChild = holder.getItem(holder.getItemCount() - 1);
        lastChild.setChecked(true);

        tree.clearAll(true);

        assertThat(holder.getText()).isEmpty();
        assertThat(lastChild.getText()).isEmpty();
        assertThat(lastChild.getChecked()).isFalse();
    }

    @Test
    @DisplayName("a column added at index 0 after front disposes moves the text of the last root and the last child")
    void firstColumnAfterFrontDisposesReachesTheLastItems() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        new TreeColumn(tree, SWT.NONE);
        TreeItem holder = rootsAfterFrontDisposes(tree);
        TreeItem lastChild = holder.getItem(holder.getItemCount() - 1);
        assertThat(holder.getText(0)).isEqualTo("holder");

        new TreeColumn(tree, SWT.NONE, 0);

        assertThat(holder.getText(0)).isEmpty();
        assertThat(lastChild.getText(0)).isEmpty();
    }

    @Test
    @DisplayName("the redraw-deferred pass after front disposes reaches the last root and its children")
    void deferredPassAfterFrontDisposesReachesTheLastItems() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        tree.setRedraw(false);
        TreeItem holder = rootsAfterFrontDisposes(tree);
        DartTree impl = (DartTree) tree.getImpl();
        DartTreeItem holderImpl = (DartTreeItem) holder.getImpl();
        assertThat(impl.itemsStart).isPositive();
        assertThat(holderImpl.itemsStart).isPositive();

        // checkItems() runs updateExpanded() on every root, and each on its children, from index 0.
        tree.selectAll();

        assertThat(impl.itemsStart).isZero();
        assertThat(holderImpl.itemsStart).isZero();
        assertItemsInOrder(holder.getItems(), holder.getItemCount(), holder::getItem, holder::indexOf,
                "child 3", "child 4", "child 5", "child 6", "child 7");
    }

    @Test
    @DisplayName("a recursive expand after front disposes reaches the last child")
    void recursiveExpandAfterFrontDisposesReachesTheLastChild() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem holder = rootsAfterFrontDisposes(tree);
        for (TreeItem child : holder.getItems()) new TreeItem(child, SWT.NONE).setText("grandchild");

        ((DartTreeItem) holder.getImpl()).sendExpand(true, true);

        assertThat(holder.getItems()).allMatch(TreeItem::getExpanded);
    }

    /** Five roots and a last root "holder" with five children, both lists past the front of their arrays. */
    private static TreeItem rootsAfterFrontDisposes(Tree tree) {
        for (int i = 0; i < 8; i++) new TreeItem(tree, SWT.NONE).setText("root " + i);
        TreeItem holder = new TreeItem(tree, SWT.NONE);
        holder.setText("holder");
        for (int i = 0; i < 8; i++) new TreeItem(holder, SWT.NONE).setText("child " + i);
        for (int i = 0; i < 3; i++) {
            tree.getItem(0).dispose();
            holder.getItem(0).dispose();
        }
        return holder;
    }

    private static void assertItemsInOrder(TreeItem[] items, int count, IntFunction<TreeItem> byIndex,
            ToIntFunction<TreeItem> indexOf, String... expected) {
        assertThat(count).isEqualTo(expected.length);
        assertThat(items).extracting(TreeItem::getText).containsExactly(expected);
        for (int i = 0; i < expected.length; i++) {
            assertThat(byIndex.apply(i).getText()).isEqualTo(expected[i]);
            assertThat(indexOf.applyAsInt(items[i])).isEqualTo(i);
        }
    }

    /** Slots of the backing array whose occupant changed, over {@code disposed} disposes of the first item. */
    private static long slotsMovedByFrontDisposes(Supplier<TreeItem[]> storage,
            Supplier<TreeItem> first, int disposed) {
        long moved = 0;
        for (int i = 0; i < disposed; i++) {
            TreeItem[] before = storage.get().clone();
            first.get().dispose();
            TreeItem[] after = storage.get();
            int length = Math.min(before.length, after.length);
            for (int slot = 0; slot < length; slot++) {
                if (after[slot] != before[slot]) moved++;
            }
            moved += Math.abs(before.length - after.length);
        }
        return moved;
    }
}

package org.eclipse.swt.widgets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("native-unit")
class ListItemsHelperTest {

    private static final String[] ABC = {"a", "b", "c"};

    @Test
    @DisplayName("an item goes in at its index, at the start and at the end too")
    void insert() {
        assertThat(ListItemsHelper.insert(ABC, 1, "x")).containsExactly("a", "x", "b", "c");
        assertThat(ListItemsHelper.insert(ABC, 0, "x")).containsExactly("x", "a", "b", "c");
        assertThat(ListItemsHelper.insert(ABC, 3, "x")).containsExactly("a", "b", "c", "x");
        assertThat(ListItemsHelper.insert(new String[0], 0, "x")).containsExactly("x");
        assertThat(ABC).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("a range of items goes, its ends included")
    void remove() {
        assertThat(ListItemsHelper.remove(ABC, 1, 1)).containsExactly("a", "c");
        assertThat(ListItemsHelper.remove(ABC, 0, 1)).containsExactly("c");
        assertThat(ListItemsHelper.remove(ABC, 0, 2)).isEmpty();
        assertThat(ABC).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("a selected index follows its item when one goes in at or before it")
    void indexAfterInsert() {
        assertThat(ListItemsHelper.indexAfterInsert(1, 0)).isEqualTo(2);
        assertThat(ListItemsHelper.indexAfterInsert(1, 1)).isEqualTo(2);
        assertThat(ListItemsHelper.indexAfterInsert(1, 2)).isEqualTo(1);
        assertThat(ListItemsHelper.indexAfterInsert(-1, 0)).isEqualTo(-1);
    }

    @Test
    @DisplayName("a selected index goes with its item and follows it when items before it go")
    void indexAfterRemove() {
        assertThat(ListItemsHelper.indexAfterRemove(4, 0, 1)).isEqualTo(2);
        assertThat(ListItemsHelper.indexAfterRemove(1, 0, 1)).isEqualTo(-1);
        assertThat(ListItemsHelper.indexAfterRemove(0, 1, 2)).isZero();
        assertThat(ListItemsHelper.indexAfterRemove(-1, 0, 0)).isEqualTo(-1);
    }

    @Test
    @DisplayName("removing items deselects them and moves the later selected indices up")
    void indicesAfterRemove() {
        assertThat(ListItemsHelper.indicesAfterRemove(new int[] {5, 0, 2, 3}, 1, 2)).containsExactly(3, 0, 1);
        assertThat(ListItemsHelper.indicesAfterRemove(new int[0], 0, 3)).isEmpty();
    }

    @Test
    @DisplayName("removing scattered items deselects them and moves each later selected index up by those before it")
    void indicesAfterRemoveScattered() {
        assertThat(ListItemsHelper.indicesAfterRemove(new int[] {6, 0, 2, 3, 5}, new int[] {1, 3, 4})).containsExactly(3, 0, 1, 2);
        assertThat(ListItemsHelper.indicesAfterRemove(new int[0], new int[] {0})).isEmpty();
    }

    @Test
    @DisplayName("removing scattered items closes them up in place and clears the freed slots")
    void removeAll() {
        String[] items = {"a", "b", "c", "d", "e", "f", null, null};
        assertThat(ListItemsHelper.removeAll(items, 6, new int[] {1, 2, 4})).isEqualTo(3);
        assertThat(items).containsExactly("a", "d", "f", null, null, null, null, null);
    }

    @Test
    @DisplayName("indices to remove come out once each, lowest first")
    void distinctAscending() {
        assertThat(ListItemsHelper.distinctAscending(new int[] {4, 4, 1, 3, 1})).containsExactly(1, 3, 4);
    }

    @Test
    @DisplayName("the trailing run counts the removed indices that end the list, from its last item back")
    void trailingRun() {
        assertThat(ListItemsHelper.trailingRun(new int[] {1, 3, 4}, 5)).isEqualTo(2);
        assertThat(ListItemsHelper.trailingRun(new int[] {1, 3}, 5)).isZero();
        assertThat(ListItemsHelper.trailingRun(new int[] {0, 1, 2}, 3)).isEqualTo(3);
    }

    @Test
    @DisplayName("anyIn finds an index inside the range, ends included, and nothing in an empty range")
    void anyIn() {
        assertThat(ListItemsHelper.anyIn(new int[] {7, 2}, 2, 4)).isTrue();
        assertThat(ListItemsHelper.anyIn(new int[] {7, 1}, 2, 4)).isFalse();
        assertThat(ListItemsHelper.anyIn(new int[] {3}, 3, 2)).isFalse();
    }

    @Test
    @DisplayName("deselecting a range leaves the other indices where they are")
    void deselect() {
        assertThat(ListItemsHelper.deselect(new int[] {5, 0, 2, 3}, 1, 2)).containsExactly(5, 0, 3);
        assertThat(ListItemsHelper.deselect(new int[] {1}, 1, 1)).isEmpty();
    }

    @Test
    @DisplayName("an array of any item type keeps its type when an item goes in or out")
    void insertAndRemoveKeepTheArrayType() {
        Integer[] numbers = {1, 2, 3};
        Integer[] inserted = ListItemsHelper.insert(numbers, 1, 9);
        assertThat(inserted).containsExactly(1, 9, 2, 3);
        assertThat(inserted.getClass()).isEqualTo(Integer[].class);
        assertThat(ListItemsHelper.remove(numbers, 0, 0).getClass()).isEqualTo(Integer[].class);
    }

    @Test
    @DisplayName("an item goes into spare capacity in place, and a full array grows by four slots")
    void insertInCapacity() {
        String[] spare = {"a", "c", null, null};
        assertThat(ListItemsHelper.insertInCapacity(spare, 2, 1, "b")).isSameAs(spare);
        assertThat(spare).containsExactly("a", "b", "c", null);

        String[] full = {"a", "b"};
        String[] grown = ListItemsHelper.insertInCapacity(full, 2, 2, "c");
        assertThat(grown).containsExactly("a", "b", "c", null, null, null);
        assertThat(full).containsExactly("a", "b");
    }

    @Test
    @DisplayName("every selected index at or after an inserted item moves one place down")
    void indicesAfterInsert() {
        assertThat(ListItemsHelper.indicesAfterInsert(new int[] {3, 0, 1}, 1)).containsExactly(4, 0, 2);
        assertThat(ListItemsHelper.indicesAfterInsert(new int[0], 0)).isEmpty();
    }

    @Test
    @DisplayName("the indices given leave the selection, in any order and with ones never selected among them")
    void deselectIndices() {
        assertThat(ListItemsHelper.deselect(new int[] {4, 1, 2}, new int[] {2, 7, 4})).containsExactly(1);
        assertThat(ListItemsHelper.deselect(new int[] {4, 1}, new int[0])).containsExactly(4, 1);
    }

    @Test
    @DisplayName("an index is selected only when it is one of the selected indices")
    void contains() {
        assertThat(ListItemsHelper.contains(new int[] {2, 5}, 5)).isTrue();
        assertThat(ListItemsHelper.contains(new int[] {2, 5}, 3)).isFalse();
        assertThat(ListItemsHelper.contains(new int[0], 0)).isFalse();
    }
}

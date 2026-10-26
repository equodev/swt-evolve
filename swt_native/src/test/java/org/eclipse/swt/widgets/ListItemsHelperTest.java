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
    @DisplayName("deselecting a range leaves the other indices where they are")
    void deselect() {
        assertThat(ListItemsHelper.deselect(new int[] {5, 0, 2, 3}, 1, 2)).containsExactly(5, 0, 3);
        assertThat(ListItemsHelper.deselect(new int[] {1}, 1, 1)).isEmpty();
    }
}

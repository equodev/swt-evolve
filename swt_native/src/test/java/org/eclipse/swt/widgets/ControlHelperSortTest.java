package org.eclipse.swt.widgets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("native-unit")
class ControlHelperSortTest {

    @Test
    @DisplayName("indices come out in descending order, whatever order they come in")
    void descending() {
        assertThat(sorted(1, 2, 3, 4)).containsExactly(4, 3, 2, 1);
        assertThat(sorted(4, 3, 2, 1)).containsExactly(4, 3, 2, 1);
        assertThat(sorted(3, 1, 4, 1, 5, 9, 2, 6)).containsExactly(9, 6, 5, 4, 3, 2, 1, 1);
        assertThat(sorted(7)).containsExactly(7);
        assertThat(sorted()).isEmpty();
    }

    @Test
    @DisplayName("50,000 random indices: the same result as sorting and reversing")
    void large() {
        int[] items = new Random(42).ints(50_000, 0, 100_000).toArray();
        int[] expected = Arrays.stream(items).boxed().sorted((a, b) -> b - a).mapToInt(Integer::intValue).toArray();
        ControlHelper.sortDescending(items);
        assertThat(items).containsExactly(expected);
    }

    private static int[] sorted(int... items) {
        ControlHelper.sortDescending(items);
        return items;
    }
}

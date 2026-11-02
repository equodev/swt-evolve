package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChangedKeysTest {

    private static ChangedKeys of(int... keys) {
        ChangedKeys set = new ChangedKeys();
        for (int key : keys) set.add(key);
        return set;
    }

    @Test
    void keepsTheOrderKeysChangedInAndEachOnce() {
        ChangedKeys keys = of(7, 3, 7, 9);
        assertThat(keys).containsExactly(7, 3, 9);
        assertThat(keys.add(3)).isFalse();
        assertThat(keys.get(1)).isEqualTo(3);
    }

    @Test
    void findsAKeyByNumber() {
        ChangedKeys keys = of(7);
        assertThat(keys.contains(7)).isTrue();
        assertThat(keys.contains(Integer.valueOf(7))).isTrue();
        assertThat(keys.contains("7")).isFalse();
    }

    @Test
    void removingKeepsTheRestInOrder() {
        ChangedKeys keys = of(1, 2, 3, 4);
        assertThat(keys.remove(Integer.valueOf(2))).isTrue();
        assertThat(keys.remove(Integer.valueOf(9))).isFalse();
        assertThat(keys).containsExactly(1, 3, 4);
        keys.removeAll(new int[] { 1, 4 });
        assertThat(keys).containsExactly(3);
        assertThat(keys.toIntArray()).containsExactly(3);
    }

    @Test
    void anIteratorRemovesWhatItLastReturned() {
        ChangedKeys keys = of(1, 2, 3);
        Iterator<Integer> it = keys.iterator();
        assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        it.next();
        it.next();
        it.remove();
        assertThat(it.next()).isEqualTo(3);
        assertThat(keys).containsExactly(1, 3);
    }

    @Test
    void growsPastItsFirstArrayAndClears() {
        ChangedKeys keys = new ChangedKeys();
        for (int i = 1; i <= 20; i++) keys.add(i);
        assertThat(keys).hasSize(20).contains(1, 20);
        keys.clear();
        assertThat(keys).isEmpty();
        keys.add(5);
        assertThat(keys).containsExactly(5);
    }
}

package dev.equo.swt.comm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class LongTableTest {

    @Test
    @DisplayName("holds what a map holds through puts, replacements, removals and growth, key 0 included")
    void matchesAMap() {
        LongTable<Long> table = new LongTable<>();
        Map<Long, Long> model = new HashMap<>();
        Random random = new Random(7);
        // Keys from a small range collide and probe past each other, so removals shift entries back.
        long[] keys = new long[600];
        for (int i = 0; i < keys.length; i++) keys[i] = i % 3 == 0 ? random.nextLong() : random.nextInt(400) - 200;
        for (int step = 0; step < 50_000; step++) {
            long key = keys[random.nextInt(keys.length)];
            switch (random.nextInt(3)) {
                case 0:
                    table.put(key, (long) step);
                    model.put(key, (long) step);
                    break;
                case 1:
                    assertThat(table.remove(key)).isEqualTo(model.remove(key));
                    break;
                default:
                    Long value = (long) -step;
                    assertThat(table.compute(key, old -> old == null ? value : null))
                            .isEqualTo(model.compute(key, (k, old) -> old == null ? value : null));
            }
            if (step % 997 == 0) {
                for (long k : keys) assertThat(table.get(k)).isEqualTo(model.get(k));
            }
        }
        for (long k : keys) assertThat(table.get(k)).isEqualTo(model.get(k));
    }

    @Test
    @DisplayName("a read racing writes sees each key's value or its absence, never another key's")
    void readsDuringWrites() throws InterruptedException {
        LongTable<Long> table = new LongTable<>();
        for (long k = 1; k <= 64; k++) table.put(k, k);
        Thread writer = new Thread(() -> {
            for (long k = 65; k < 20_000; k++) {
                table.put(k, k);
                if (k % 3 == 0 && k - 32 > 64) table.remove(k - 32);
            }
        });
        writer.start();
        while (writer.isAlive()) {
            for (long k = 1; k <= 64; k++) assertThat(table.get(k)).isEqualTo(k);
        }
        writer.join();
    }
}

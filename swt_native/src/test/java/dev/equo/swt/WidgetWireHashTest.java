package dev.equo.swt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A property is sent only when its encoding hashes differently from the one last sent, so two values
 * whose short encodings collide leave the client showing the wrong one.
 */
class WidgetWireHashTest {

    private static final byte[] VALUES = { 0, 1, 2, 3, 4, 8, 127, (byte) 128, (byte) 255 };

    @Test
    @DisplayName("one zero and an empty array hash apart: a first selection of row 0 is sent")
    void oneZeroIsNotAnEmptyArray() {
        assertThat(WidgetWire.hash(new byte[] { 2, 0 }, 0, 2)).isNotEqualTo(WidgetWire.EMPTY_HASH);
        assertThat(WidgetWire.hash(new byte[] { 1, 0 }, 0, 2)).isNotEqualTo(WidgetWire.ZERO_HASH);
    }

    @Test
    @DisplayName("every short encoding hashes apart from every other")
    void shortEncodingsHashApart() {
        Set<Long> hashes = new HashSet<>();
        int count = 0;
        for (int length = 0; length <= 3; length++) {
            int combinations = (int) Math.pow(VALUES.length, length);
            for (int n = 0; n < combinations; n++) {
                byte[] b = new byte[length];
                for (int i = 0, rest = n; i < length; i++, rest /= VALUES.length) b[i] = VALUES[rest % VALUES.length];
                hashes.add(WidgetWire.hash(b, 0, length));
                count++;
            }
        }
        assertThat(hashes).hasSize(count);
    }
}

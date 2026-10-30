package dev.equo.swt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The client sends every length {@link WireReader} reads, so a length past the bytes it was lent is
 * refused before anything is sized by it, and nothing is read from past its end.
 */
class WireReaderBoundsTest {

    /** A varint count of 2^28 + 1, then nothing: an array that size would be a gigabyte of ints. */
    private static final byte[] HUGE_COUNT = {(byte) 0x81, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01};

    @Test
    void anArrayLongerThanItsBytesIsRefused() {
        assertThatThrownBy(() -> new WireReader(HUGE_COUNT, 0, HUGE_COUNT.length).ints())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new WireReader(HUGE_COUNT, 0, HUGE_COUNT.length).strings())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aStringLongerThanItsBytesIsRefused() {
        // Header 2 * 3 + 1: three Latin-1 bytes announced, two present.
        byte[] bytes = {7, 'a', 'b'};
        assertThatThrownBy(() -> new WireReader(bytes, 0, bytes.length).string())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nothingIsReadPastTheBytesLent() {
        // A float's four bytes are in the buffer, but only two were lent.
        byte[] buffer = {0, 0, (byte) 0x80, 0x3F};
        assertThatThrownBy(() -> new WireReader(buffer, 0, 2).float32())
                .isInstanceOf(IllegalStateException.class);
        assertThat(new WireReader(buffer, 0, 4).float32()).isEqualTo(1f);
    }
}

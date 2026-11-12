package dev.equo.swt;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A string written by {@link WidgetWire} reads back as itself, whichever form it travels in. */
class WireStringTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "plain ascii", "héllo — wörld ÿ", "日本語", "mixed é and 日 and 🎉"})
    void roundTrips(String s) {
        WidgetWire w = new WidgetWire();
        w.writeString(s);
        byte[] bytes = w.written();
        assertThat(new WireReader(bytes, 0, bytes.length).string()).isEqualTo(s);
    }

    @Test
    void nullStaysNull() {
        WidgetWire w = new WidgetWire();
        w.writeString(null);
        byte[] bytes = w.written();
        assertThat(new WireReader(bytes, 0, bytes.length).string()).isNull();
    }

    @Test
    void asciiTakesOneBytePerChar() {
        WidgetWire w = new WidgetWire();
        w.writeString("hello");
        assertThat(w.size()).isEqualTo(1 + 5);
    }

    /** The one-byte form is ASCII alone, so a browser client can decode it natively as it is. */
    @Test
    void aCharPastAsciiTakesTheTwoByteForm() {
        WidgetWire w = new WidgetWire();
        w.writeString("héllo");
        assertThat(w.size()).isEqualTo(1 + 2 * 5);
    }
}

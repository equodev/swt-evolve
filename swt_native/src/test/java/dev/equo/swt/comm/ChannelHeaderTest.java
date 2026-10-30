package dev.equo.swt.comm;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * A message names its channel by numbers: a channel addressed to one widget by the name's, the
 * type's and the id, a global one by its number alone. Only a name nothing numbers is spelled out.
 */
class ChannelHeaderTest {

    private static String roundTrip(String channel) {
        byte[] header = ChannelHeader.encode(channel);
        byte[] frame = new byte[header.length + 3];
        System.arraycopy(header, 0, frame, 0, header.length);
        ChannelHeader.Decoded decoded = ChannelHeader.decode(frame, 0, frame.length);
        assertThat(decoded.payloadStart()).isEqualTo(header.length);
        return decoded.channel();
    }

    private static boolean spellsOut(String channel, String part) {
        return new String(ChannelHeader.encode(channel), StandardCharsets.ISO_8859_1).contains(part);
    }

    @Test
    void aChannelAddressedToAWidgetTravelsAsNumbers() {
        assertThat(roundTrip("Label/123456789/Paint/Paint")).isEqualTo("Label/123456789/Paint/Paint");
        assertThat(spellsOut("Label/123456789/Paint/Paint", "Paint")).isFalse();
        assertThat(spellsOut("Label/123456789/Paint/Paint", "Label")).isFalse();
        assertThat(ChannelHeader.encode("Label/123456789/Paint/Paint")).hasSizeLessThanOrEqualTo(8);
    }

    @Test
    void aNegativeIdAndAWidgetsOwnChannelRoundTrip() {
        assertThat(roundTrip("Button/-2147483648/MouseTrack/MouseEnter")).isEqualTo("Button/-2147483648/MouseTrack/MouseEnter");
        assertThat(roundTrip("Shell/42")).isEqualTo("Shell/42");
    }

    @Test
    void aGlobalChannelTravelsAsItsNumber() {
        assertThat(roundTrip("swt.evolve.wire")).isEqualTo("swt.evolve.wire");
        assertThat(roundTrip("GC/create")).isEqualTo("GC/create");
        assertThat(spellsOut("swt.evolve.wire", "wire")).isFalse();
    }

    @Test
    void aNameNothingNumbersIsSpelledOut() {
        assertThat(roundTrip("evolve.test.frameSync")).isEqualTo("evolve.test.frameSync");
        assertThat(roundTrip("Label/12/noSuchChannel")).isEqualTo("Label/12/noSuchChannel");
        assertThat(spellsOut("evolve.test.frameSync", "evolve.test.frameSync")).isTrue();
    }

    @Test
    void theOldTwoByteHeaderReadsAsASpelledOutName() {
        byte[] name = "evolve.test.query".getBytes(StandardCharsets.UTF_8);
        byte[] frame = new byte[2 + name.length + 1];
        frame[1] = (byte) name.length;
        System.arraycopy(name, 0, frame, 2, name.length);
        ChannelHeader.Decoded decoded = ChannelHeader.decode(frame, 0, frame.length);
        assertThat(decoded.channel()).isEqualTo("evolve.test.query");
        assertThat(decoded.payloadStart()).isEqualTo(2 + name.length);
    }
}

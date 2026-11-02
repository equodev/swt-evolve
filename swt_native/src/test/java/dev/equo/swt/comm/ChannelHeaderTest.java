package dev.equo.swt.comm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * A message names its channel by numbers: a channel addressed to one widget by the name's, the
 * type's and the id, a global one by its number alone. A name without a number is refused.
 */
class ChannelHeaderTest {

    private static String roundTrip(String channel) {
        byte[] header = dev.equo.swt.harness.Channels.header(channel);
        byte[] frame = new byte[header.length + 3];
        System.arraycopy(header, 0, frame, 0, header.length);
        ChannelHeader.Decoded decoded = ChannelHeader.decode(frame, 0, frame.length);
        assertThat(decoded.payloadStart()).isEqualTo(header.length);
        return decoded.channel();
    }

    private static boolean spellsOut(String channel, String part) {
        return new String(dev.equo.swt.harness.Channels.header(channel), StandardCharsets.ISO_8859_1).contains(part);
    }

    @Test
    void aChannelAddressedToAWidgetTravelsAsNumbers() {
        assertThat(roundTrip("Label/123456789/Paint/Paint")).isEqualTo("Label/123456789/Paint/Paint");
        assertThat(spellsOut("Label/123456789/Paint/Paint", "Paint")).isFalse();
        assertThat(spellsOut("Label/123456789/Paint/Paint", "Label")).isFalse();
        assertThat(dev.equo.swt.harness.Channels.header("Label/123456789/Paint/Paint")).hasSizeLessThanOrEqualTo(8);
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
    void aNameWithoutANumberIsRefusedNeverSpelledOut() {
        assertThatThrownBy(() -> dev.equo.swt.harness.Channels.header("Label/12/noSuchChannel")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> dev.equo.swt.harness.Channels.header("NoSuchType/12/Paint/Paint")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> dev.equo.swt.harness.Channels.header("no.such.channel")).isInstanceOf(IllegalArgumentException.class);
        byte[] name = "evolve.test.query".getBytes(StandardCharsets.UTF_8);
        byte[] literal = new byte[2 + name.length];
        literal[1] = (byte) name.length;
        System.arraycopy(name, 0, literal, 2, name.length);
        assertThat(ChannelHeader.decode(literal, 0, literal.length)).as("a header with no channel number is not one").isNull();
    }

    @Test
    void aNumberNamesAChannelOrATypeOnlyWithinItsTable() {
        int label = ChannelHeader.requireType("Label");
        assertThat(ChannelHeader.typeName(label)).isEqualTo("Label");
        assertThat(ChannelHeader.name(ChannelHeader.requireChannel("Paint/Paint"))).isEqualTo("Paint/Paint");
        assertThat(ChannelHeader.number("no.such.channel")).isZero();
        assertThat(ChannelHeader.name(-1)).isNull();
        assertThat(ChannelHeader.name(Integer.MAX_VALUE)).isNull();
        assertThat(ChannelHeader.typeName(-1)).isNull();
        assertThat(ChannelHeader.typeName(Integer.MAX_VALUE)).isNull();
    }

    @Test
    void aHeaderCutShortOrNumberedOutsideTheTablesIsNotOne() {
        int label = ChannelHeader.requireType("Label");
        int paint = ChannelHeader.requireChannel("Paint/Paint");
        byte[] header = ChannelHeader.encode(label, 300, paint);

        assertThat(decode(new byte[0])).as("nothing").isNull();
        for (int cut = 1; cut < header.length; cut++)
            assertThat(decode(Arrays.copyOf(header, cut))).as("cut after %d bytes", cut).isNull();
        byte[] overlong = new byte[11];
        Arrays.fill(overlong, (byte) 0x80);
        assertThat(decode(overlong)).as("a varint longer than 64 bits").isNull();

        assertThat(decode(ChannelHeader.encode(0, 0, 1_000_000))).as("a global channel past the table").isNull();
        assertThat(decode(ChannelHeader.encode(label, 300, 1_000_000))).as("a widget's channel past the table").isNull();
        assertThat(decode(ChannelHeader.encode(label, 300, 0))).as("a widget's channel numbered 0").isNull();
        assertThat(decode(ChannelHeader.encode(1_000_000, 300, paint))).as("a type past the table").isNull();
        assertThat(decode(varints(((long) paint << 1) | 1, 0, 600))).as("a widget of type 0").isNull();
    }

    private static ChannelHeader.Decoded decode(byte[] header) {
        return ChannelHeader.decode(header, 0, header.length);
    }

    private static byte[] varints(long... values) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (long v : values) {
            while ((v & ~0x7FL) != 0) {
                out.write((int) ((v & 0x7F) | 0x80));
                v >>>= 7;
            }
            out.write((int) v);
        }
        return out.toByteArray();
    }
}

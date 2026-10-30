package dev.equo.swt.comm;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A run of frames goes as one message: per entry its channel as a header names it, its payload's
 * length and its payload, so each is applied on the far side as if it had come on its own.
 */
class MessageBatchTest {

    /** What one batch put on the wire. */
    private static byte[] sent(MessageBatch batch) {
        List<byte[]> frames = new ArrayList<>();
        batch.sendTo(new dev.equo.swt.harness.RecordingComm() {
            @Override
            public void sendFrame(byte[] frame, int offset, int length) {
                frames.add(java.util.Arrays.copyOfRange(frame, offset, offset + length));
            }
        });
        assertThat(frames).hasSize(1);
        return frames.get(0);
    }

    /** The entries of a batch frame, each as its channel and its payload as text. */
    private static List<String> entries(byte[] frame) {
        ChannelHeader.Decoded batch = ChannelHeader.decode(frame, 0, frame.length);
        assertThat(batch.channel()).isEqualTo(MessageBatch.EVENT);
        List<String> out = new ArrayList<>();
        int pos = batch.payloadStart();
        while (pos < frame.length) {
            ChannelHeader.Decoded entry = ChannelHeader.decode(frame, pos, frame.length);
            int at = entry.payloadStart();
            int length = 0;
            for (int shift = 0; ; shift += 7) {
                int b = frame[at++] & 0xFF;
                length |= (b & 0x7F) << shift;
                if ((b & 0x80) == 0) break;
            }
            out.add(entry.channel() + " " + new String(frame, at, length, StandardCharsets.UTF_8));
            pos = at + length;
        }
        return out;
    }

    private static byte[] text(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("each entry reads back on its own channel, in order")
    void entriesReadBackInOrder() {
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("a"));
        dev.equo.swt.harness.Channels.add(batch, "Label/9/Paint/Paint", text(""));
        dev.equo.swt.harness.Channels.add(batch, "evolve.test.query", text("b"));

        assertThat(entries(sent(batch)))
                .containsExactly("GC/7/gcDispose a", "Label/9/Paint/Paint ", "evolve.test.query b");
    }

    @Test
    @DisplayName("a channel both sides number spells out no name")
    void numberedChannelsSpellNothing() {
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("a"));
        dev.equo.swt.harness.Channels.add(batch, "Label/9/Paint/Paint", text("b"));

        String wire = new String(sent(batch), StandardCharsets.ISO_8859_1);
        assertThat(wire).doesNotContain("gcDispose").doesNotContain("Paint").doesNotContain("swt.evolve.batch");
    }

    @Test
    @DisplayName("a lone frame is sent unwrapped, on its own channel")
    void aSingleEntryIsNotBatched() {
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("payload"));

        byte[] frame = sent(batch);
        ChannelHeader.Decoded header = ChannelHeader.decode(frame, 0, frame.length);
        assertThat(header.channel()).isEqualTo("GC/7/gcDispose");
        assertThat(new String(frame, header.payloadStart(), frame.length - header.payloadStart(), StandardCharsets.UTF_8))
                .isEqualTo("payload");
    }

    @Test
    @DisplayName("clearing starts the next run empty")
    void clearStartsOver() {
        MessageBatch batch = new MessageBatch();
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("old"));
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("old"));
        batch.clear();
        dev.equo.swt.harness.Channels.add(batch, "GC/7/gcDispose", text("x"));
        dev.equo.swt.harness.Channels.add(batch, "Label/9/Paint/Paint", text("y"));

        assertThat(entries(sent(batch))).containsExactly("GC/7/gcDispose x", "Label/9/Paint/Paint y");
    }
}

package dev.equo.swt.comm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A batch spells a repeated channel name once and refers back to it, leaving every payload and
 * channel intact.
 */
class MessageBatchDictionaryTest {

    /** The bytes a batch would put on the wire, as the client receives them. */
    private static String wire(MessageBatch batch) {
        StringBuilder out = new StringBuilder();
        batch.sendTo(new dev.equo.swt.harness.RecordingComm() {
            @Override
            public void sendFrame(byte[] frame, int offset, int length) {
                out.append(new String(frame, offset, length, StandardCharsets.UTF_8));
            }
        });
        return out.toString();
    }

    private static byte[] json(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("a channel is spelled out once and pointed at afterwards")
    void repeatsTravelAsAnIndex() {
        MessageBatch batch = new MessageBatch();
        batch.add("GC/7/drawStringStringintintboolean", json("{\"string\":\"71\"}"));
        batch.add("GC/7/drawStringStringintintboolean", json("{\"string\":\"72\"}"));
        batch.add("GC/7/gcDispose", json("{}"));
        batch.add("GC/7/drawStringStringintintboolean", json("{\"string\":\"73\"}"));

        String sent = wire(batch);

        assertThat(sent)
                .as("the first use names it")
                .contains("[\"GC/7/drawStringStringintintboolean\",{\"string\":\"71\"}]");
        assertThat(sent)
                .as("later ones point at the entry that did")
                .contains("[0,{\"string\":\"72\"}]")
                .contains("[0,{\"string\":\"73\"}]");
        assertThat(sent)
                .as("a different channel is spelled out for itself")
                .contains("[\"GC/7/gcDispose\",{}]");
        assertThat(sent.split("drawStringStringintintboolean", -1).length - 1)
                .as("and the long name appears exactly once")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("an index names the entry's own position, not the count of repeats")
    void indexIsThePositionOfTheNamingEntry() {
        MessageBatch batch = new MessageBatch();
        batch.add("a/one", json("{}"));
        batch.add("b/two", json("{}"));
        batch.add("c/three", json("{}"));
        batch.add("b/two", json("{}"));

        assertThat(wire(batch))
                .as("b/two was entry 1, so its repeat carries 1")
                .contains("[1,{}]");
    }

    @Test
    @DisplayName("a repeated run costs far less than spelling it out every time")
    void theRunGetsSubstantiallySmaller() {
        String channel = "GC/49719830/drawStringStringintintboolean";
        MessageBatch batch = new MessageBatch();
        for (int i = 0; i < 40; i++) {
            batch.add(channel, json("{\"y\":" + (i * 18) + ",\"isTransparent\":true,\"x\":10,\"string\":\"" + i + "\"}"));
        }
        int withDictionary = batch.byteSize();
        int spelledOut = withDictionary + 39 * (channel.length() - 2);

        assertThat(withDictionary)
                .as("40 ops of %d B channel: %d B against %d spelled out", channel.length(),
                        withDictionary, spelledOut)
                .isLessThan(spelledOut * 2 / 3);
    }

    @Test
    @DisplayName("a lone frame is still sent unwrapped, on its own channel")
    void aSingleEntryIsNotBatched() {
        MessageBatch batch = new MessageBatch();
        batch.add("GC/7/gcDispose", json("{\"fullRepaint\":true}"));

        assertThat(wire(batch))
                .as("no batch wrapper, and no dictionary to resolve")
                .contains("GC/7/gcDispose")
                .contains("{\"fullRepaint\":true}")
                .doesNotContain("[\"GC/7/gcDispose\"");
    }

    @Test
    @DisplayName("clearing forgets the names, so the next run spells its own")
    void clearResetsTheDictionary() {
        MessageBatch batch = new MessageBatch();
        batch.add("GC/7/drawLine", json("{}"));
        batch.add("GC/7/drawLine", json("{}"));
        batch.clear();
        batch.add("GC/7/drawLine", json("{\"x\":1}"));
        batch.add("GC/7/drawLine", json("{\"x\":2}"));

        String sent = wire(batch);
        assertThat(sent).contains("[\"GC/7/drawLine\",{\"x\":1}]").contains("[0,{\"x\":2}]");
    }
}

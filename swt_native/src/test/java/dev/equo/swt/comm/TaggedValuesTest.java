package dev.equo.swt.comm;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A payload the wire has no class encoding for travels as tagged values: maps, lists and scalars in
 * binary, map keys by number, and text the only string.
 */
class TaggedValuesTest {

    private static Map<String, Object> values() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("doit", true);
        m.put("text", "déjà vu");
        m.put("x", -12);
        m.put("width", 3_000_000_000L);
        m.put("alpha", 0.5);
        m.put("nothing", null);
        m.put("items", List.of(1, "two", false));
        m.put("aKeyNothingNumbers", "spelled out");
        m.put("nested", Map.of("aKeyNothingNumbers", 1));
        return m;
    }

    @Test
    @DisplayName("maps, lists and scalars read back as they were written")
    void roundTrip() {
        byte[] encoded = TaggedValues.encode(values(), null);
        @SuppressWarnings("unchecked")
        Map<String, Object> read = (Map<String, Object>) TaggedValues.decode(encoded, 0, encoded.length);
        assertThat(read).containsEntry("doit", true).containsEntry("text", "déjà vu").containsEntry("x", -12L)
                .containsEntry("width", 3_000_000_000L).containsEntry("alpha", 0.5).containsEntry("nothing", null)
                .containsEntry("items", List.of(1L, "two", false)).containsEntry("aKeyNothingNumbers", "spelled out")
                .containsEntry("nested", Map.of("aKeyNothingNumbers", 1L));
    }

    @Test
    @DisplayName("a numbered key is not spelled out; one nothing numbers is spelled out once")
    void keysGoByNumber() {
        String wire = new String(TaggedValues.encode(values(), null), StandardCharsets.ISO_8859_1);
        assertThat(wire).doesNotContain("doit").doesNotContain("width");
        assertThat(wire.split("aKeyNothingNumbers", -1)).hasSize(2);
    }

    @Test
    @DisplayName("bytes read back as bytes, and an object of no other kind as the tree it is given as")
    void bytesAndObjects() {
        byte[] bytes = {0, 1, (byte) 255};
        byte[] encoded = TaggedValues.encode(Map.of("pixels", bytes, "point", new Object()), o -> List.of(1, 2));
        @SuppressWarnings("unchecked")
        Map<String, Object> read = (Map<String, Object>) TaggedValues.decode(encoded, 0, encoded.length);
        assertThat(Arrays.equals((byte[]) read.get("pixels"), bytes)).isTrue();
        assertThat(read.get("point")).isEqualTo(List.of(1L, 2L));
    }

    @Test
    @DisplayName("text reads back as String.getBytes would carry it, past the two-byte range too")
    void textIsUtf8() {
        for (String text : new String[] {"a\uD83D\uDE00\u00E9\u20AC", "\uD800x", "x\uDC00"}) {
            byte[] encoded = TaggedValues.encode(text, null);
            String expected = new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            assertThat(TaggedValues.decode(encoded, 0, encoded.length)).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("a payload opens with a mark text cannot start with")
    void mark() {
        byte[] encoded = TaggedValues.encode("x", null);
        assertThat(TaggedValues.isTagged(encoded, 0, encoded.length)).isTrue();
        byte[] text = "{\"a\":1}".getBytes(StandardCharsets.UTF_8);
        assertThat(TaggedValues.isTagged(text, 0, text.length)).isFalse();
    }
}

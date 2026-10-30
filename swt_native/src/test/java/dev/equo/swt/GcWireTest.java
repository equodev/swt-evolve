package dev.equo.swt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.VGC;

import static org.assertj.core.api.Assertions.assertThat;

public class GcWireTest {

    private static final String CHANNEL = "GC/7";

    /** Numbers no channel has, for ops only these tests write. */
    private static final int EVERY_KIND = 4001, CLIP_TO = 4002;

    /** drawLine's shape: four ints. */
    private static GcWire.Op line(int x1, int y1, int x2, int y2) {
        return new GcWire.Op() {
            @Override
            public int wireNumber() {
                return dev.equo.swt.comm.ChannelHeader.number("drawLineintintintint");
            }

            @Override
            public String signature() {
                return "iiii";
            }

            @Override
            public void writeBinary(GcWire w) {
                w.i(x1);
                w.i(y1);
                w.i(x2);
                w.i(y2);
            }
        };
    }

    private static GcWire.Op everyKind() {
        return new GcWire.Op() {
            @Override
            public int wireNumber() {
                return EVERY_KIND;
            }

            @Override
            public String signature() {
                return "IIbsfd";
            }

            @Override
            public void writeBinary(GcWire w) {
                w.ints(new int[] { -1, 0, 70000, Integer.MIN_VALUE });
                w.ints(null);
                w.b(true);
                w.s("dé\"jà");
                w.f(1.5f);
                w.d(-0.25);
            }
        };
    }

    /** {@code w}'s entries as {@code [channel, value]} pairs. */
    private static List<Object> describe(GcWire w) {
        List<Object> out = new ArrayList<>();
        for (GcWire.Entry e : w.entries()) out.add(Arrays.asList(e.channel, e.value));
        return out;
    }

    private static List<Object> entry(String channel, Object value) {
        return Arrays.asList(channel, value);
    }

    private static Map<String, Object> record(Object... keyThenValue) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyThenValue.length; i += 2) m.put((String) keyThenValue[i], keyThenValue[i + 1]);
        return m;
    }

    private static void assertEntries(GcWire w, Object... expected) {
        List<Object> actual = describe(w);
        assertThat(dev.equo.swt.harness.WireValues.same(actual, Arrays.asList(expected)))
                .as("entries %s, expected %s", actual, Arrays.asList(expected))
                .isTrue();
    }

    @Test
    @DisplayName("a frame reads back past its own channel header, which is numbered rather than spelled out")
    void frameReadsBackPastItsNumberedHeader() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        w.op(line(1, 2, 3, 4));
        int spelledOut = 2 + GcWire.frameEvent(CHANNEL).length();
        assertThat(dev.equo.swt.comm.ChannelHeader.decode(w.buffer(), 0, w.byteSize()).payloadStart())
                .isLessThan(spelledOut);
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)));
    }

    @Test
    @DisplayName("an op reads back as its arguments in order, on its own channel")
    void opReadsBack() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        w.op(line(-3, 0, 40000, Integer.MAX_VALUE));
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(-3, 0, 40000, Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("every argument type reads back as the value written")
    void everyArgumentKindReadsBack() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        w.op(everyKind());
        assertEntries(w, entry("GC/7/op" + EVERY_KIND,
                Arrays.asList(List.of(-1, 0, 70000, Integer.MIN_VALUE), null, true, "dé\"jà", 1.5, -0.25)));
    }

    @Test
    @DisplayName("a repeated op is named once, then referred to by number")
    void repeatedOpIsNamedOnce() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        w.op(line(1, 2, 3, 4));
        int afterFirst = w.byteSize();
        w.op(line(1, 2, 3, 4));
        assertThat(w.byteSize() - afterFirst).as("a repeat carries no name").isEqualTo(5);
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)),
                entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)));
    }

    @Test
    @DisplayName("a change and a whole state carry their properties as the widget wire writes them; an escape its payload")
    void stateAndEscapeReadBack() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        WidgetWire change = w.scratch();
        change.key(VGC.LINE_WIDTH);
        change.write(3);
        change.key(VGC.ALPHA);
        change.write((Integer) null);
        change.key(VGC.BUFFER_SCALE);
        change.write(2.0f);
        w.state(GcWire.STATE, change);
        WidgetWire whole = w.scratch();
        whole.key(VGC.LINE_WIDTH);
        whole.write(-1);
        w.state(GcWire.WHOLE, whole);
        byte[] dispose = dev.equo.swt.comm.TaggedValues.encode(java.util.Map.of("fullRepaint", true), null);
        w.escape(CHANNEL + "/gcDispose", dispose, 0, dispose.length);
        assertEntries(w, entry("GC/7", record("_d", 0, "lineWidth", 3, "alpha", null, "bufferScale", 2.0)),
                entry("GC/7", record("_whole", true, "lineWidth", -1)),
                entry("GC/7/gcDispose", record("fullRepaint", true)));
    }

    @Test
    @DisplayName("an op and a state key both sides number go by their numbers, spelling out no name")
    void numberedOpAndKeySpellNothing() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        int start = w.byteSize();
        w.op(line(1, 2, 3, 4));
        WidgetWire change = w.scratch();
        change.key(VGC.LINE_WIDTH);
        change.write(3);
        w.state(GcWire.STATE, change);
        String body = new String(w.buffer(), start, w.byteSize() - start, StandardCharsets.ISO_8859_1);
        assertThat(body).doesNotContain("drawLine").doesNotContain("lineWidth");
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)),
                entry("GC/7", record("_d", 0, "lineWidth", 3)));
    }

    @Test
    @DisplayName("a removed transform goes as null")
    void nullTransformReadsBack() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        WidgetWire change = w.scratch();
        change.key(VGC.TRANSFORM);
        change.object(null);
        w.state(GcWire.STATE, change);
        assertEntries(w, entry("GC/7", record("_d", 0, "transform", null)));
    }

    @Test
    @DisplayName("an argument of any other type travels as the widget wire encodes it")
    void wireArgumentReadsBack() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        w.op(new GcWire.Op() {
            @Override
            public int wireNumber() {
                return CLIP_TO;
            }

            @Override
            public String signature() {
                return "wi";
            }

            @Override
            public void writeBinary(GcWire frame) {
                WidgetWire value = frame.scratch();
                value.write(new org.eclipse.swt.graphics.Rectangle(1, -2, 30, 40));
                frame.w(value);
                frame.i(5);
            }

            @Override
            public String[] wireKinds() {
                return new String[] { "rect" };
            }
        });
        assertEntries(w, entry("GC/7/op" + CLIP_TO, List.of(record("x", 1, "y", -2, "width", 30, "height", 40), 5)));
    }

    @Test
    @DisplayName("a frame with nothing in it is not sent")
    void emptyFrameIsEmpty() {
        GcWire w = new GcWire();
        w.begin(CHANNEL);
        assertThat(w.isEmpty()).isTrue();
        w.op(line(0, 0, 0, 0));
        assertThat(w.isEmpty()).isFalse();
    }
}

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
    private static final int GC = dev.equo.swt.comm.ChannelHeader.requireType("GC");

    /** drawLine's shape: four ints. */
    private static GcWire.Op line(int x1, int y1, int x2, int y2) {
        return new GcWire.Op() {
            @Override
            public String signature() {
                return "iiii";
            }

            @Override
            public int number() {
                return dev.equo.swt.comm.ChannelHeader.requireChannel("drawLineintintintint");
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
            public String signature() {
                return "IIbsfd";
            }

            @Override
            public int number() {
                return dev.equo.swt.comm.ChannelHeader.requireChannel("evolve.test.frameStats");
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
        w.begin(GC, 7);
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
        w.begin(GC, 7);
        w.op(line(-3, 0, 40000, Integer.MAX_VALUE));
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(-3, 0, 40000, Integer.MAX_VALUE)));
    }

    @Test
    @DisplayName("every argument type reads back as the value written")
    void everyArgumentKindReadsBack() {
        GcWire w = new GcWire();
        w.begin(GC, 7);
        w.op(everyKind());
        assertEntries(w, entry("GC/7/evolve.test.frameStats",
                Arrays.asList(List.of(-1, 0, 70000, Integer.MIN_VALUE), null, true, "dé\"jà", 1.5, -0.25)));
    }

    @Test
    @DisplayName("a generated op writes the frame its arguments make, frame after frame")
    void generatedOpWritesWhatItsArgumentsMake() {
        VGC.VGCDrawLineintintintint numbered = new VGC.VGCDrawLineintintintint();
        numbered.x1 = 1;
        numbered.y1 = 2;
        numbered.x2 = 3;
        numbered.y2 = 4;
        assertThat(numbered.number()).isEqualTo(dev.equo.swt.comm.ChannelHeader.number("drawLineintintintint"));
        GcWire byNumber = new GcWire();
        GcWire byHand = new GcWire();
        for (int frame = 0; frame < 2; frame++) {
            byNumber.begin(GC, 7);
            byHand.begin(GC, 7);
            byNumber.op(numbered);
            byNumber.op(everyKind());
            byNumber.op(numbered);
            byHand.op(line(1, 2, 3, 4));
            byHand.op(everyKind());
            byHand.op(line(1, 2, 3, 4));
            assertThat(java.util.Arrays.copyOf(byNumber.buffer(), byNumber.byteSize()))
                    .isEqualTo(java.util.Arrays.copyOf(byHand.buffer(), byHand.byteSize()));
        }
        assertThat(describe(byNumber)).isEqualTo(describe(byHand));
    }

    @Test
    @DisplayName("a repeated op is named once, then referred to by number")
    void repeatedOpIsNamedOnce() {
        GcWire w = new GcWire();
        w.begin(GC, 7);
        w.op(line(1, 2, 3, 4));
        int afterFirst = w.byteSize();
        w.op(line(1, 2, 3, 4));
        assertThat(w.byteSize() - afterFirst).as("a repeat carries no name").isEqualTo(5);
        assertEntries(w, entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)),
                entry("GC/7/drawLineintintintint", List.of(1, 2, 3, 4)));
    }

    @Test
    @DisplayName("a change and a whole state carry their properties as the widget wire writes them; a gcDispose its fields")
    void stateAndEscapeReadBack() {
        GcWire w = new GcWire();
        w.begin(GC, 7);
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
        w.dispose(GcWire.Dispose.ofPaint(true, new org.eclipse.swt.graphics.Rectangle(-1, 2, 30, 40)));
        assertEntries(w, entry("GC/7", record("_d", 0, "lineWidth", 3, "alpha", null, "bufferScale", 2.0)),
                entry("GC/7", record("_whole", true, "lineWidth", -1)),
                entry("GC/7/gcDispose", record("fullRepaint", true, "pixels", false, "retain", false, "ref", 0L,
                        "damage", record("x", -1, "y", 2, "width", 30, "height", 40))));
    }

    @Test
    @DisplayName("an op and a state key both sides number go by their numbers, spelling out no name")
    void numberedOpAndKeySpellNothing() {
        GcWire w = new GcWire();
        w.begin(GC, 7);
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
        w.begin(GC, 7);
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
        w.begin(GC, 7);
        w.op(new GcWire.Op() {
            @Override
            public String signature() {
                return "wi";
            }

            @Override
            public int number() {
                return dev.equo.swt.comm.ChannelHeader.requireChannel("evolve.test.wireStats");
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
        assertEntries(w, entry("GC/7/evolve.test.wireStats", List.of(record("x", 1, "y", -2, "width", 30, "height", 40), 5)));
    }

    @Test
    @DisplayName("a frame with nothing in it is not sent")
    void emptyFrameIsEmpty() {
        GcWire w = new GcWire();
        w.begin(GC, 7);
        assertThat(w.isEmpty()).isTrue();
        w.op(line(0, 0, 0, 0));
        assertThat(w.isEmpty()).isFalse();
    }
}

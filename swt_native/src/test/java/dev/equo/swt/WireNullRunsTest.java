package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A list of objects writes each run of nulls as one marker and its length, so a per-line list with
 * one line set costs a few bytes, not one a line; a lone null keeps its single byte.
 */
class WireNullRunsTest {

    private static final WireObject EMPTY = new WireObject() {
        @Override
        public void writeWireFields(WidgetWire w) {
        }

        @Override
        public int wireTypeId() {
            return 0;
        }
    };

    @Test
    void runsOfNullsReadBackInPlace() {
        WireObject[] list = { EMPTY, null, null, null, EMPTY, null, EMPTY, null, null };
        assertThat(readBack(list)).containsExactly(noFields(), null, null, null, noFields(), null, noFields(), null, null);
    }

    @Test
    void aListOfOnlyNullsReadsBack() {
        assertThat(readBack(new WireObject[1000])).hasSize(1000).containsOnlyNulls();
    }

    @Test
    void aLongListWithOneObjectTakesAFewBytes() {
        WireObject[] lines = new WireObject[6456];
        lines[21] = EMPTY;
        assertThat(write(lines).size()).isLessThan(12);
    }

    @Test
    void aLoneNullTakesOneByte() {
        WidgetWire one = write(new WireObject[] { EMPTY, null, EMPTY });
        WidgetWire none = write(new WireObject[] { EMPTY, EMPTY });
        assertThat(one.size() - none.size()).isEqualTo(1);
    }

    private static java.util.Map<String, Object> noFields() {
        return new java.util.LinkedHashMap<>();
    }

    /** As the generated writers put a list of objects. */
    private static WidgetWire write(WireObject[] a) {
        WidgetWire w = new WidgetWire();
        w.listSize(a.length);
        for (int i = 0; i < a.length; i++) {
            WireObject e = a[i];
            if (e == null) {
                i += w.nulls(a, i) - 1;
                continue;
            }
            w.object(e);
        }
        return w;
    }

    private static List<Object> readBack(WireObject[] a) {
        byte[] bytes = write(a).written();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) WireDump.value(bytes, 0, bytes.length, "object:VLineInfo[]");
        return Arrays.asList(list.toArray());
    }
}

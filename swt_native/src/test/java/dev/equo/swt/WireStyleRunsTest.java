package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A style index travels as runs, each start as the step from the end of the run before, left out
 * when the run touches that one, and a run's name and length as one number; a change to an index,
 * and anything not made of whole runs, as the ints it is. Either reads back as written.
 */
class WireStyleRunsTest {

    @Test
    void aWholeIndexReadsBackAsWritten() {
        int[] index = index(500);
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
    }

    @Test
    void runsThatTouchReadBackAsWritten() {
        int[] index = touching(500);
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
    }

    @Test
    void aRunThatTouchesTheOneBeforeTakesTwoBytes() {
        WidgetWire w = new WidgetWire();
        w.runs(touching(1000));
        WidgetWire header = new WidgetWire();
        header.runs(new int[] { 123456 });
        assertThat(w.size() - header.size()).isLessThanOrEqualTo(2 * 1000 + 2);
    }

    @Test
    void aShortRunTouchingTheOneBeforeTakesOneByte() {
        int[] index = touching(1000);
        for (int i = 1; i < index.length; i += 3) index[i] = (i / 3) % 4;
        WidgetWire w = new WidgetWire();
        w.runs(index);
        WidgetWire header = new WidgetWire();
        header.runs(new int[] { 123456 });
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
        assertThat(w.size() - header.size()).isLessThanOrEqualTo(1000 + 2);
    }

    @Test
    void theLargestPackedNameAndLengthReadBackAsWritten() {
        int last = WidgetWire.MAX_RUN_NAMES - 1;
        int[] index = { 7, last, 0, Integer.MAX_VALUE - 1, 0, Integer.MAX_VALUE - 1, 1 };
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
    }

    @Test
    void namesPastWhatPacksReadBackAsWritten() {
        int[] index = { 7, WidgetWire.MAX_RUN_NAMES, 0, 4, 1, 4, 2 };
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
    }

    @Test
    void extremeNamesAndStepsReadBackAsWritten() {
        int[] index = { 7, Integer.MAX_VALUE, 0, 1, Integer.MIN_VALUE, 5, 2, -1, 5, 3 };
        assertThat(readBack(index)).containsExactlyElementsOf(boxed(index));
    }

    @Test
    void aChangeReadsBackAsWritten() {
        int[] change = { Integer.MIN_VALUE, 987654, 3, 123456, 4, 2, 1, 200012, 6, 2 };
        assertThat(readBack(change)).containsExactlyElementsOf(boxed(change));
    }

    @Test
    void aChangeInsertingWholeRunsReadsBackAsWritten() {
        int[] change = change(500);
        assertThat(readBack(change)).containsExactlyElementsOf(boxed(change));
    }

    @Test
    void aChangeInsertingWholeRunsTakesFarFewerBytesThanItsOffsets() {
        int[] change = change(1000);
        WidgetWire runs = new WidgetWire();
        runs.runs(change);
        WidgetWire ints = new WidgetWire();
        ints.write(change);
        assertThat(runs.size()).isLessThan(ints.size() / 3);
    }

    @Test
    void aListNotMadeOfWholeRunsReadsBackAsWritten() {
        int[] odd = { 5, 1, 2 };
        assertThat(readBack(odd)).containsExactlyElementsOf(boxed(odd));
    }

    @Test
    void aLongDocumentIndexTakesFarFewerBytesThanItsOffsets() {
        int[] index = index(10000);
        WidgetWire runs = new WidgetWire();
        runs.runs(index);
        WidgetWire ints = new WidgetWire();
        ints.write(index);
        assertThat(runs.size()).isLessThan(ints.size() * 65 / 100);
    }

    private static List<Object> readBack(int[] v) {
        WidgetWire w = new WidgetWire();
        w.runs(v);
        byte[] bytes = w.written();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) WireDump.value(bytes, 0, bytes.length, "runs");
        return list;
    }

    /** A palette name, then a run every 12 characters of a long document. */
    private static int[] index(int runs) {
        int[] v = new int[1 + 3 * runs];
        v[0] = 123456;
        for (int i = 0; i < runs; i++) {
            v[1 + 3 * i] = i % 7;
            v[2 + 3 * i] = 200000 + i * 12;
            v[3 + 3 * i] = 5 + i % 4;
        }
        return v;
    }

    /** A palette name, then runs that each start where the one before ends, as a highlighted line's do. */
    private static int[] touching(int runs) {
        int[] v = new int[1 + 3 * runs];
        v[0] = 123456;
        for (int i = 0, start = 0; i < runs; i++) {
            v[1 + 3 * i] = i % 5 - 1;
            v[2 + 3 * i] = start;
            v[3 + 3 * i] = 1 + i % 9;
            start += 1 + i % 9;
        }
        return v;
    }

    /** A change's header, then {@code runs} touching runs it inserts deep in a document. */
    private static int[] change(int runs) {
        int[] touching = touching(runs);
        int[] v = new int[WidgetWire.RUNS_CHANGE_HEADER + 3 * runs];
        v[0] = Integer.MIN_VALUE;
        v[1] = 987654;
        v[2] = 3;
        v[3] = 123456;
        v[4] = 64;
        v[5] = 4425;
        for (int i = 0; i < runs; i++) {
            v[6 + 3 * i] = (i / 3) % 4;
            v[7 + 3 * i] = 200000 + touching[2 + 3 * i];
            v[8 + 3 * i] = touching[3 + 3 * i];
        }
        return v;
    }

    private static List<Object> boxed(int[] v) {
        List<Object> list = new ArrayList<>();
        for (int e : v) list.add(e);
        return list;
    }
}

package dev.equo.swt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.assertj.core.api.Assertions.assertThat;

/** A string a paint draws goes into the frame as it is, with nothing allocated to encode it. */
class GcWireStringAllocationTest {

    private static final int OPS = 20_000;

    private static final int DRAW_TEXT = dev.equo.swt.comm.ChannelHeader.number("drawTextStringintintint");

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static GcWire.Op drawText(String text) {
        return new GcWire.Op() {
            @Override
            public int number() {
                return DRAW_TEXT;
            }

            @Override
            public String signature() {
                return "siii";
            }

            @Override
            public void writeBinary(GcWire w) {
                w.s(text);
                w.i(4);
                w.i(12);
                w.i(0);
            }
        };
    }

    @Test
    @DisplayName("drawText's string, Latin-1 or not, costs the frame no allocation")
    void drawTextAllocatesNothing() {
        for (String text : new String[] {"Package Explorer - src/main/java", "Ünïcödé · 日本語 → ✓"}) {
            GcWire w = new GcWire();
            GcWire.Op op = drawText(text);
            // Grown once to the size the measured run needs, and the op named.
            for (int round = 0; round < 3; round++) {
                w.begin(WireTable.TYPE_GC, 7);
                for (int i = 0; i < OPS; i++) w.op(op);
            }
            w.begin(WireTable.TYPE_GC, 7);
            long before = THREADS.getCurrentThreadAllocatedBytes();
            for (int i = 0; i < OPS; i++) w.op(op);
            long perOp = (THREADS.getCurrentThreadAllocatedBytes() - before) / OPS;
            assertThat(perOp).as("bytes allocated per drawText of \"%s\"", text).isZero();
        }
    }
}

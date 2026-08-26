package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A grid that paints a thousand ops per repaint pays a serialize+send for each one, and the render
 * trails the pointer. The ops of one paint belong to one frame — the Flutter drawer stages them
 * until the GC is disposed, so nothing is rendered before that anyway.
 *
 * Lives in org.eclipse.swt.widgets so the mocked display's package-private
 * {@code sendEvent(EventTable, Event)} can be wired to really dispatch.
 */
// The batching under test lives in shared main-source code (FlutterBridge), identical on every
// backend; one platform pins it. The Linux/Windows embed backends route Canvas construction into
// real GTK/GDI, which cannot run under the mocked headless display.
@DisabledOnOs({ OS.LINUX, OS.WINDOWS })
@ExtendWith(Mocks.class)
class CanvasPaintOpBatchingTest {

    private static final int OPS_PER_PAINT = 200;

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    private Canvas canvas() {
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
                return inv.callRealMethod();
            }
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        pumpAsync();
        return canvas;
    }

    private void pumpAsync() {
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
    }

    /** Every frame the paint put on the wire for its GC, batched or not, in send order. */
    private List<RecordingComm.Frame> gcFrames() {
        return bridge.comm.sent.stream()
                .filter(f -> f.event.startsWith("GC/") || f.event.equals(MessageBatch.EVENT))
                .toList();
    }

    private Canvas paintingCanvas() {
        Canvas canvas = canvas();
        canvas.addPaintListener(e -> {
            for (int i = 0; i < OPS_PER_PAINT; i++) {
                e.gc.drawLine(i, 0, i, 10);
            }
        });
        return canvas;
    }

    private String paintOnce(Canvas canvas) {
        bridge.comm.sent.clear();
        canvas.redraw();
        pumpAsync();
        return gcFrames().stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("one paint costs one op frame, not one per draw call")
    void onePaintSendsOneOpFrame() {
        Canvas canvas = paintingCanvas();
        String wire = paintOnce(canvas);

        assertThat(gcFrames())
                .as("frames for a single %d-op paint", OPS_PER_PAINT)
                .hasSize(1);
        assertThat(countOccurrences(wire, "drawLineintintintint"))
                .as("draw ops that reached the wire")
                .isEqualTo(OPS_PER_PAINT);
    }

    @Test
    @DisplayName("a batched op keeps its own channel, and its place in the run")
    void batchCarriesEveryOpInOrder() {
        Canvas canvas = paintingCanvas();
        String wire = paintOnce(canvas);

        assertThat(wire).as("each op keeps its own channel inside the batch")
                .contains("[\"GC/");
        assertThat(wire.indexOf("\"x1\":0")).as("first op precedes the last")
                .isLessThan(wire.indexOf("\"x1\":" + (OPS_PER_PAINT - 1)));
    }

    @Test
    @DisplayName("the paint still ends with gcDispose, after its ops")
    void gcDisposeStillTerminatesThePaint() {
        Canvas canvas = paintingCanvas();
        String wire = paintOnce(canvas);

        assertThat(wire).as("the paint's wire traffic").contains("gcDispose");
        assertThat(wire.indexOf("drawLineintintintint")).as("ops precede gcDispose")
                .isLessThan(wire.indexOf("gcDispose"));
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
// backend.
@ExtendWith(Mocks.class)
class CanvasPaintOpBatchingTest {

    private static final int OPS_PER_PAINT = 200;

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();
    /** The display mock is shared by every test in the JVM, so its stubs must go quiet after this one. */
    private boolean dispatching;

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
        dispatching = false;
        asyncQueue.clear();
    }

    private Canvas canvas() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        dispatching = true;
        doAnswer(inv -> {
            Event ev = inv.getArgument(1);
            if (dispatching && ev != null && (ev.type == SWT.Paint || ev.type == SWT.Resize)) {
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
        // Counted by payload, as a repeated channel is named once (MessageBatch). An op is its
        // arguments in order: drawLine(i, 0, i, 10) travels as [i,0,i,10].
        assertThat(java.util.regex.Pattern.compile("\\[(\\d+),0,\\1,10]").matcher(wire).results().count())
                .as("draw ops that reached the wire")
                .isEqualTo(OPS_PER_PAINT);
        assertThat(countOccurrences(wire, "drawLineintintintint"))
                .as("and the channel they share is named once, not once per op")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a batched op keeps its own channel, and its place in the run")
    void batchCarriesEveryOpInOrder() {
        Canvas canvas = paintingCanvas();
        String wire = paintOnce(canvas);

        assertThat(wire).as("each op keeps its own channel inside the batch")
                .contains("[\"GC/");
        assertThat(wire.indexOf("[0,0,0,10]")).as("first op precedes the last")
                .isNotNegative()
                .isLessThan(wire.indexOf("[" + (OPS_PER_PAINT - 1) + ",0," + (OPS_PER_PAINT - 1) + ",10]"));
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

    @Test
    @DisplayName("another Display's event-loop flush leaves this thread's buffered ops alone")
    void flushOnAnotherThreadLeavesBufferedOps() throws Exception {
        Canvas canvas = canvas();
        GC gc = new GC(canvas);
        try {
            gc.drawLine(0, 0, 5, 10);
            bridge.comm.sent.clear();

            Throwable[] failure = { null };
            Thread otherDisplay = new Thread(() -> {
                try {
                    FlutterBridge.updateFrame();
                } catch (Throwable t) {
                    failure[0] = t;
                }
            });
            otherDisplay.start();
            otherDisplay.join();

            assertThat(failure[0]).as("the other thread's flush").isNull();
            assertThat(gcFrames())
                    .as("frames another thread sent from a batch this thread is still writing")
                    .isEmpty();

            FlutterBridge.updateFrame();
            assertThat(gcFrames()).as("frames this thread's own flush sends").hasSize(1);
        } finally {
            gc.dispose();
        }
    }

    @Test
    @DisplayName("a GC disposed on another thread still sends its ops before gcDispose")
    void gcHandedToAnotherThreadKeepsItsOrder() throws Exception {
        Canvas canvas = canvas();
        GC gc = new GC(canvas);
        gc.drawLine(0, 0, 5, 10);
        bridge.comm.sent.clear();

        Throwable[] failure = { null };
        Thread worker = new Thread(() -> {
            try {
                gc.dispose();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        worker.start();
        worker.join();
        FlutterBridge.updateFrame();

        assertThat(failure[0]).as("disposing on the other thread").isNull();
        // A frame sent on its own carries its channel as the event, not in the payload.
        String wire = gcFrames().stream().map(f -> f.event + f.json).reduce("", String::concat);
        assertThat(wire).as("the paint's wire traffic").contains("drawLineintintintint", "gcDispose");
        assertThat(wire.indexOf("drawLineintintintint")).as("the op drawn before the hand-over precedes gcDispose")
                .isLessThan(wire.indexOf("gcDispose"));
    }

    @Test
    @DisplayName("a batch that fills up right after a hand-over goes out in order, every op once")
    void capFlushAfterHandOverKeepsEveryOpInOrder() throws Exception {
        // Enough ops to pass FlutterBridge's 1 MB cap on one batch.
        int ops = 100_000;
        Canvas canvas = canvas();
        GC gc = new GC(canvas);
        gc.drawLine(0, 7, 5, 12345);
        bridge.comm.sent.clear();

        Throwable[] failure = { null };
        int[] framesBeforeDispose = { 0 };
        Thread worker = new Thread(() -> {
            try {
                for (int i = 1; i <= ops; i++) gc.drawLine(i, 0, i, 10);
                framesBeforeDispose[0] = gcFrames().size();
                gc.dispose();
            } catch (Throwable t) {
                failure[0] = t;
            }
        });
        worker.start();
        worker.join();

        assertThat(failure[0]).as("drawing on the other thread").isNull();
        assertThat(framesBeforeDispose[0])
                .as("frames sent before gcDispose, the hand-over's then the full batch's: fewer means "
                        + "the scenario never reached the cap right after a hand-over")
                .isGreaterThanOrEqualTo(2);
        List<JsonArray> lines = drawLines();
        assertThat(lines).as("draw ops on the wire").hasSize(ops + 1);
        assertThat(lines.get(0).get(3).getAsInt()).as("the op drawn before the hand-over goes first")
                .isEqualTo(12345);
        List<Integer> x1 = new ArrayList<>();
        for (JsonArray line : lines.subList(1, lines.size())) x1.add(line.get(0).getAsInt());
        assertThat(x1).as("the other thread's ops, each once and in order").isEqualTo(sequence(ops));
        List<String[]> entries = BatchEntries.of(gcFrames());
        assertThat(entries.get(entries.size() - 1)[0]).as("the last frame").endsWith("/gcDispose");
    }

    @Test
    @DisplayName("threads painting while other event loops flush get every op through once, in order")
    void concurrentPaintsAndFlushesLoseNothing() throws Exception {
        int painters = 3;
        int paints = 150;
        int opsPerPaint = 40;
        List<Canvas> canvases = new ArrayList<>();
        for (int t = 0; t < painters; t++) canvases.add(canvas());
        bridge.comm.sent.clear();

        ConcurrentLinkedQueue<Throwable> failures = new ConcurrentLinkedQueue<>();
        AtomicBoolean painting = new AtomicBoolean(true);
        List<Thread> otherLoops = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            otherLoops.add(new Thread(() -> {
                try {
                    while (painting.get()) FlutterBridge.updateFrame();
                } catch (Throwable t) {
                    failures.add(t);
                }
            }));
        }
        List<Thread> painterThreads = new ArrayList<>();
        for (int t = 0; t < painters; t++) {
            Canvas canvas = canvases.get(t);
            // y1 says which painter drew the op.
            int painter = t;
            painterThreads.add(new Thread(() -> {
                try {
                    int seq = 0;
                    for (int p = 0; p < paints; p++) {
                        GC gc = new GC(canvas);
                        for (int k = 0; k < opsPerPaint; k++) {
                            gc.drawLine(++seq, painter, 1, 1);
                            // This thread's own loop turning mid-paint, as a nested event loop does.
                            if (k == opsPerPaint / 2 && p % 3 == 0) FlutterBridge.updateFrame();
                        }
                        gc.dispose();
                    }
                } catch (Throwable e) {
                    failures.add(e);
                }
            }));
        }
        otherLoops.forEach(Thread::start);
        painterThreads.forEach(Thread::start);
        for (Thread t : painterThreads) t.join();
        painting.set(false);
        for (Thread t : otherLoops) t.join();

        assertThat(failures).as("exceptions thrown while painting or flushing").isEmpty();
        Map<Integer, List<Integer>> drawnBy = new TreeMap<>();
        for (JsonArray line : drawLines()) {
            drawnBy.computeIfAbsent(line.get(1).getAsInt(), k -> new ArrayList<>()).add(line.get(0).getAsInt());
        }
        assertThat(drawnBy.keySet()).as("painters whose ops reached the wire").hasSize(painters);
        for (Map.Entry<Integer, List<Integer>> painter : drawnBy.entrySet()) {
            assertThat(painter.getValue()).as("painter %d's ops, each once and in order", painter.getKey())
                    .isEqualTo(sequence(paints * opsPerPaint));
        }
    }

    /**
     * Every drawLine on the wire in send order, as its arguments: drawLine(x1, y1, x2, y2) travels as
     * [x1,y1,x2,y2]. Parsing each frame also fails on a torn one.
     */
    private List<JsonArray> drawLines() {
        List<JsonArray> lines = new ArrayList<>();
        for (String[] entry : BatchEntries.of(gcFrames())) {
            if (entry[0].endsWith("/drawLineintintintint")) {
                lines.add(JsonParser.parseString(entry[1]).getAsJsonArray());
            }
        }
        return lines;
    }

    private static List<Integer> sequence(int count) {
        List<Integer> out = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) out.add(i);
        return out;
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }
}

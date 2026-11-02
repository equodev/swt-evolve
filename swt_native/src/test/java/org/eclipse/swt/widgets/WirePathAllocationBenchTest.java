package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WireObject;
import dev.equo.swt.WireTable;
import dev.equo.swt.WidgetWire;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.ChannelHeader;
import dev.equo.swt.comm.TaggedValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Bytes the Java side allocates per message on each path of the wire, sending and receiving: the
 * whole cost of one message, not the part that grows with its payload. The transport is left out: a
 * sent frame stops where the socket would take it, and a received one starts where the socket hands
 * it over.
 *
 * <p>Measured on the calling thread, where these paths run, as the fewest bytes any of several
 * rounds allocated per message, with a real Display and no mocks: a mocked class is instrumented,
 * and the instrumentation allocates inside the class's own methods even for real instances. Prints
 * rather than asserts: it is the before and after of a change.
 */
@Tag("bench")
class WirePathAllocationBenchTest {

    private static final int WARMUP = 20_000;
    private static final int PER_ROUND = 10_000;
    private static final int ROUNDS = 7;

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    /** A comm that does everything a transport's caller does and drops the bytes at the socket. */
    private static final class NullComm extends AbstractBinaryCommService {
        NullComm() {
            onClientConnected(frame -> {
            });
        }

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
        }

        void receive(byte[] frame, int length) {
            onBinaryMessage(frame, 0, length);
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }
    }

    /** A Display bridge that keeps nothing of what it sends. */
    private static final class QuietBridge extends WebDisplayBridge {
        private final NullComm comm;

        QuietBridge(DartDisplay display, NullComm comm) {
            super(display);
            this.comm = comm;
            clientReady.complete(true);
        }

        @Override
        protected dev.equo.swt.comm.CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }

    /** What stops the Display from starting a real surface while it is made. */
    private static final class Placeholder extends FlutterBridge {
        private final NullComm comm = new NullComm();

        Placeholder() {
            clientReady.complete(true);
        }

        @Override
        protected dev.equo.swt.comm.CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }

    private NullComm comm;
    private Display display;

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
        comm = new NullComm();
        FlutterBridge.set(new Placeholder());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dart = (DartDisplay) display.getImpl();
        QuietBridge bridge = new QuietBridge(dart, comm);
        dart.setBridge(bridge);
        bridge.start(dart);
    }

    @AfterEach
    void tearDown() {
        display.dispose();
    }

    @Test
    void sending() {
        Shell shell = new Shell(display);
        Label label = new Label(shell, SWT.NONE);
        DartWidget impl = (DartWidget) label.getImpl();
        FlutterBridge.update();

        report("send: event, no payload", () -> FlutterBridge.sendEvent(impl, WireTable.CHANNEL_PAINT_PAINT));
        Map<String, Object> args = Map.of("doit", true);
        report("send: widget channel, tagged map", () -> FlutterBridge.send(impl, WireTable.CHANNEL_KEY_VERDICT, args));
        report("send: global channel, tagged map", () -> FlutterBridge.send(comm, WireTable.CHANNEL_SWT_EVOLVE_FOCUS, args));
        WireObject fields = new WireObject() {
            @Override
            public void writeWireFields(WidgetWire w) {
                w.key(WireTable.DISPLAY_ACTIVE_SHELL_ID);
                w.write(7L);
            }

            @Override
            public int wireTypeId() {
                return 0;
            }
        };
        report("send: widget channel, fields", () -> FlutterBridge.sendFields(impl, WireTable.CHANNEL_DROP_NEGOTIATION_RESULT, fields));

        String[] texts = {"one", "two"};
        int[] turn = {0};
        report("update: one label's text changes", () -> {
            label.setText(texts[turn[0]++ & 1]);
            FlutterBridge.update();
        });

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        FlutterBridge.update();
        double paint10 = report("gc: paint of 10 drawLine", () -> paint(canvas, 10), 500, 200);
        double paint110 = report("gc: paint of 110 drawLine", () -> paint(canvas, 110), 500, 200);
        for (int i = 0; i < 2000; i++) paint(canvas, 10);
        // A histogram is itself held while the next is taken, so the growth between two with nothing
        // in between is what measuring adds; it is taken off what the paints add.
        Map<String, long[]> first = histogram();
        Map<String, long[]> before = histogram();
        for (int i = 0; i < 2000; i++) paint(canvas, 10);
        Map<String, long[]> after = histogram();
        long grown = 0;
        java.util.List<String> growers = new java.util.ArrayList<>();
        for (Map.Entry<String, long[]> e : after.entrySet()) {
            long[] was = before.getOrDefault(e.getKey(), new long[2]);
            long[] measuring = first.getOrDefault(e.getKey(), was);
            long bytes = e.getValue()[1] - was[1] - (was[1] - measuring[1]);
            long count = e.getValue()[0] - was[0] - (was[0] - measuring[0]);
            grown += bytes;
            if (bytes > 2000 * 8) growers.add(String.format("%10.1f B/paint %8.2f/paint %s", bytes / 2000.0,
                    count / 2000.0, e.getKey()));
        }
        growers.sort(java.util.Comparator.reverseOrder());
        System.out.printf("MEASURE %-48s %10.1f B/paint%n", "gc: heap still held after a paint", grown / 2000.0);
        for (String g : growers.subList(0, Math.min(15, growers.size()))) System.out.println("  held " + g);
        System.out.printf("MEASURE %-48s %10.1f B/op, %10.1f B/paint besides%n", "gc: split",
                (paint110 - paint10) / 100, paint10 - 10 * (paint110 - paint10) / 100);
    }

    /** Live instances and bytes by class, after a full collection: the JVM's class histogram. */
    private static Map<String, long[]> histogram() {
        try {
            String text = (String) ManagementFactory.getPlatformMBeanServer().invoke(
                    new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand"), "gcClassHistogram",
                    new Object[] {null}, new String[] {String[].class.getName()});
            Map<String, long[]> out = new java.util.HashMap<>();
            for (String line : text.split("\n")) {
                String[] f = line.trim().split("\\s+");
                if (f.length < 4 || !f[0].endsWith(":")) continue;
                out.put(f[3], new long[] {Long.parseLong(f[1]), Long.parseLong(f[2])});
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Every allocation {@code count} runs of {@code op} make, by site; needs -XX:-UseTLAB. */
    static void exactSites(String method, int count, Runnable op) {
        try {
            java.nio.file.Path file = java.nio.file.Files.createTempFile("exact", ".jfr");
            try (jdk.jfr.Recording rec = new jdk.jfr.Recording()) {
                rec.enable("jdk.ObjectAllocationOutsideTLAB").withStackTrace();
                rec.start();
                for (int i = 0; i < count; i++) op.run();
                rec.stop();
                rec.dump(file);
            }
            Map<String, Long> sites = new java.util.HashMap<>();
            long total = 0;
            for (jdk.jfr.consumer.RecordedEvent e : jdk.jfr.consumer.RecordingFile.readAllEvents(file)) {
                jdk.jfr.consumer.RecordedStackTrace st = e.getStackTrace();
                if (st == null) continue;
                java.util.List<jdk.jfr.consumer.RecordedFrame> frames = st.getFrames();
                if (frames.stream().noneMatch(f -> f.getMethod().getName().startsWith(method))) continue;
                long size = e.getLong("allocationSize");
                StringBuilder chain = new StringBuilder(e.getClass("objectClass").getName().replaceAll(".*[.]", "")).append(" @");
                int n = 0;
                for (jdk.jfr.consumer.RecordedFrame f : frames) {
                    String t = f.getMethod().getType().getName();
                    if (n == 0 && (t.startsWith("java.") || t.startsWith("jdk.") || t.startsWith("sun."))) continue;
                    chain.append(n == 0 ? " " : " < ").append(t.replaceAll(".*[.]", "")).append('.').append(f.getMethod().getName()).append(':').append(f.getLineNumber());
                    if (++n == 4) break;
                }
                sites.merge(chain.toString(), size, Long::sum);
                total += size;
            }
            java.nio.file.Files.delete(file);
            System.out.printf("EXACT total %.1f B per call%n", total / (double) count);
            sites.entrySet().stream().sorted((a, b) -> Long.compare(b.getValue(), a.getValue())).limit(80)
                    .forEach(en -> System.out.printf("EXACT %8.1f  %s%n", en.getValue() / (double) count, en.getKey()));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    private static void exactPaint(Canvas canvas) {
        GC gc = new GC(canvas);
        gc.drawLine(1, 0, 1, 10);
        gc.drawLine(2, 0, 2, 10);
        gc.dispose();
    }

    private static void paint(Canvas canvas, int ops) {
        GC gc = new GC(canvas);
        for (int i = 0; i < ops; i++) gc.drawLine(i, 0, i, 10);
        gc.dispose();
    }

    /**
     * Every allocation of a control's paint, by site: run with TLABs off ({@code
     * JAVA_TOOL_OPTIONS=-XX:-UseTLAB}), which makes the JVM record each one.
     */
    @Test
    void paintAllocationSites() {
        Shell shell = new Shell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        FlutterBridge.update();
        for (int i = 0; i < 20000; i++) paint(canvas, 2);
        exactSites("exactPaint", 1000, () -> exactPaint(canvas));
    }

    /** Every allocation of an update that sends one label's new text, by site; see {@link #paintAllocationSites}. */
    @Test
    void updateAllocationSites() {
        Shell shell = new Shell(display);
        Label label = new Label(shell, SWT.NONE);
        FlutterBridge.update();
        String[] texts = {"one", "two"};
        int[] turn = {0};
        Runnable change = () -> {
            label.setText(texts[turn[0]++ & 1]);
            FlutterBridge.update();
        };
        for (int i = 0; i < 20_000; i++) change.run();
        exactSites("lambda$updateAllocationSites$", 1000, change);
    }

    /** Every allocation of receiving a widget event and of sending one, by site; see {@link #paintAllocationSites}. */
    @Test
    void eventAllocationSites() {
        int label = ChannelHeader.requireType("Label");
        int[] got = {0};
        comm.on(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOWN, Event.class, e -> got[0]++);
        Runnable receive = deliver(frame(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOWN, eventPayload()));
        for (int i = 0; i < 20_000; i++) receive.run();
        exactSites("lambda$deliver$", 1000, receive);
        Shell shell = new Shell(display);
        Label widget = new Label(shell, SWT.NONE);
        DartWidget impl = (DartWidget) widget.getImpl();
        FlutterBridge.update();
        Runnable send = () -> FlutterBridge.sendEvent(impl, WireTable.CHANNEL_PAINT_PAINT);
        for (int i = 0; i < 20_000; i++) send.run();
        exactSites("lambda$eventAllocationSites$", 1000, send);
    }

    /** Time per paint, the median of several rounds: what the allocations above cost in time. */
    @Test
    void paintTime() {
        Shell shell = new Shell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 300);
        FlutterBridge.update();
        for (int ops : new int[] {2, 10, 110}) {
            for (int i = 0; i < 50_000; i++) paint(canvas, ops);
            int per = ops > 10 ? 5_000 : 20_000;
            double[] rounds = new double[9];
            for (int r = 0; r < rounds.length; r++) {
                long start = System.nanoTime();
                for (int i = 0; i < per; i++) paint(canvas, ops);
                rounds[r] = (System.nanoTime() - start) / (double) per;
            }
            java.util.Arrays.sort(rounds);
            System.out.printf("TIME gc: paint of %d drawLine %10.1f ns/paint%n", ops, rounds[rounds.length / 2]);
        }
    }

    @Test
    void receiving() {
        int label = ChannelHeader.requireType("Label");
        int[] got = {0};
        comm.on(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOWN, Event.class, e -> got[0]++);
        comm.on(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_UP, byte[].class, b -> got[0]++);
        comm.on(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOUBLE_CLICK, ByteBuffer.class, b -> got[0]++);
        comm.on(0, 0, WireTable.CHANNEL_SWT_EVOLVE_URL_OPEN, String.class, s -> got[0]++);
        comm.on(0, 0, WireTable.CHANNEL_EVOLVE_TEST_QUERY, Object.class, o -> got[0]++);

        byte[] event = frame(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOWN, eventPayload());
        byte[] raw = frame(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_UP, new byte[64]);
        byte[] buffer = frame(label, 1, WireTable.CHANNEL_MOUSE_MOUSE_DOUBLE_CLICK, new byte[64]);
        byte[] text = frame(0, 0, WireTable.CHANNEL_SWT_EVOLVE_URL_OPEN, "https://example.org".getBytes());
        byte[] tagged = frame(0, 0, WireTable.CHANNEL_EVOLVE_TEST_QUERY, TaggedValues.encode(Map.of("queryId", 1, "targetId", 2), null));

        report("receive: event into Event", deliver(event));
        report("receive: raw bytes", deliver(raw));
        report("receive: ByteBuffer", deliver(buffer));
        report("receive: text", deliver(text));
        report("receive: tagged map", deliver(tagged));
        System.out.println("(handled " + got[0] + ")");
    }

    /** Delivers {@code frame} afresh each time: decoding may write over what it was handed. */
    private Runnable deliver(byte[] frame) {
        byte[] work = new byte[frame.length];
        return () -> {
            System.arraycopy(frame, 0, work, 0, frame.length);
            comm.receive(work, frame.length);
        };
    }

    private static byte[] frame(int type, long id, int channel, byte[] payload) {
        byte[] header = ChannelHeader.encode(type, id, channel);
        byte[] frame = java.util.Arrays.copyOf(header, header.length + payload.length);
        System.arraycopy(payload, 0, frame, header.length, payload.length);
        return frame;
    }

    /** A mouse event as the client writes one: x, y, button. */
    private static byte[] eventPayload() {
        return WidgetWire.fieldsOf(new WireObject() {
            @Override
            public void writeWireFields(WidgetWire w) {
                w.key(dev.equo.swt.harness.WireFrames.property("Event.x"));
                w.write(10);
                w.key(dev.equo.swt.harness.WireFrames.property("Event.y"));
                w.write(20);
                w.key(dev.equo.swt.harness.WireFrames.property("Event.button"));
                w.write(1);
            }

            @Override
            public int wireTypeId() {
                return 0;
            }
        });
    }

    private static double report(String label, Runnable op) {
        return report(label, op, WARMUP, PER_ROUND);
    }

    private static double report(String label, Runnable op, int warmup, int perRound) {
        for (int i = 0; i < warmup; i++) op.run();
        long thread = Thread.currentThread().getId();
        double best = Double.MAX_VALUE;
        double[] times = new double[ROUNDS];
        for (int round = 0; round < ROUNDS; round++) {
            long before = THREADS.getThreadAllocatedBytes(thread);
            long start = System.nanoTime();
            for (int i = 0; i < perRound; i++) op.run();
            times[round] = (System.nanoTime() - start) / (double) perRound;
            long after = THREADS.getThreadAllocatedBytes(thread);
            best = Math.min(best, (after - before) / (double) perRound);
        }
        java.util.Arrays.sort(times);
        System.out.printf("MEASURE %-48s %10.1f B/message %8.1f ns%n", label, best, times[ROUNDS / 2]);
        return best;
    }
}

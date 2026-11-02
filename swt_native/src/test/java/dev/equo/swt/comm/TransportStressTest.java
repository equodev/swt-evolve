package dev.equo.swt.comm;

import dev.equo.swt.WireTable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The transport under load from several threads at once, over a real socket: every frame reaches
 * the other side exactly once, whole, and in the order its thread sent it. The send paths reuse
 * their buffers, so a byte the transport read after a send returned would show up here as a frame
 * that is not the one sent.
 *
 * <p>{@code -Dstress.frames=N} sets the frames each thread sends. Nothing on the send side waits
 * for the client, so each thread keeps at most {@code -Dstress.inFlight=N} frames ahead of it: a
 * long run then tests what arrives rather than how much the transport can queue.
 *
 * <p>What the heap holds after a collection may not grow by more than {@code -Dstress.heapLimitMb=N}
 * during a run: a transport that keeps a copy of each frame it has not written yet grows with how
 * far the client is behind. The default leaves room for what the paced run legitimately has in
 * flight: every thread's frames up to its pacing bound. {@code -Dstress.unpacedFrames=N} sets the frames each thread sends
 * when it does not wait for the client at all.
 */
class TransportStressTest {

    private static final int THREADS = 8;
    private static final int FRAMES = Integer.getInteger("stress.frames", 2000);
    /** Sizes the payloads cycle through, past the size a thread keeps a scratch buffer for. */
    private static final int[] SIZES = {0, 1, 13, 255, 4096, 70_000, 33, 1500};
    private static final int IN_FLIGHT = Integer.getInteger("stress.inFlight", 500);
    private static final long HEAP_LIMIT = Long.getLong("stress.heapLimitMb", 64) * 1024 * 1024;
    /** Enough that a client which cannot keep up falls behind by far more than the limit. */
    private static final int UNPACED_FRAMES = Integer.getInteger("stress.unpacedFrames", 20_000);

    private static final com.sun.management.ThreadMXBean THREAD_BEAN =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private BinaryCommService comm;
    private Client client;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (client != null) client.closeBlocking();
        if (comm != null) comm.stop();
    }

    @Test
    @DisplayName("frames sent from many threads at once arrive once each, whole and in their thread's order")
    void framesFromManyThreadsArriveIntact() throws Exception {
        Run run = run(IN_FLIGHT, FRAMES);
        assertThat(run.heap.breach).as("the heap past %d MB after a collection", HEAP_LIMIT >> 20).isNull();
    }

    /**
     * Senders that never wait for the client: the transport can keep only what it is writing, so
     * neither the heap nor what a send allocates grows with the frames it has not written yet.
     */
    @Test
    @Tag("transport-copies")
    @DisplayName("senders that outrun the client keep the heap bounded and allocate nothing per byte sent")
    void sendersThatOutrunTheClientStayBounded() throws Exception {
        Run run = run(0, UNPACED_FRAMES);
        assertThat(run.heap.breach).as("the heap past %d MB after a collection", HEAP_LIMIT >> 20).isNull();
        assertThat(run.allocatedPerByteSent()).as("bytes a send allocates per byte it sends").isLessThan(0.01);
    }

    /** What a run measured. */
    private record Run(HeapWatch heap, long sendAllocated, long bytesSent) {
        double allocatedPerByteSent() {
            return sendAllocated / (double) bytesSent;
        }
    }

    /** {@code frames} from each thread, each at most {@code inFlight} ahead of the client, or unpaced at 0. */
    private Run run(int inFlight, int frames) throws Exception {
        comm = new BinaryCommService();
        client = new Client(new URI("ws://127.0.0.1:" + comm.getPort()), THREADS * frames);
        assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
        waitUntilConnected();

        // What the client sends back is read by the server while it is sending.
        AtomicInteger readBack = new AtomicInteger();
        List<String> readBackErrors = new ArrayList<>();
        int back = THREADS * frames / 4;
        CountDownLatch allReadBack = new CountDownLatch(back);
        comm.onBytes(WireTable.TYPE_LABEL, 0, WireTable.CHANNEL_B, (data, offset, length) -> {
            String error = check(data, offset, offset + length, 0, readBack.get());
            if (error != null) synchronized (readBackErrors) { readBackErrors.add(error); }
            readBack.incrementAndGet();
            allReadBack.countDown();
        });
        // Paced in every run: the test client queues what it sends without bound, and that is not the
        // transport under test.
        Thread backSender = new Thread(() -> {
            for (int seq = 0; seq < back; seq++) {
                long deadline = System.nanoTime() + 30_000_000_000L;
                while (readBack.get() <= seq - IN_FLIGHT && System.nanoTime() < deadline) Thread.onSpinWait();
                byte[] payload = payload(0, seq);
                byte[] frame = new byte[ChannelHeader.MAX_NUMBERED + payload.length];
                int start = ChannelHeader.write(frame, 0, WireTable.TYPE_LABEL, 0, WireTable.CHANNEL_B);
                System.arraycopy(payload, 0, frame, start, payload.length);
                client.send(ByteBuffer.wrap(frame, 0, start + payload.length));
            }
        });

        AtomicLong sendAllocated = new AtomicLong();
        AtomicLong bytesSent = new AtomicLong();
        HeapWatch heap = new HeapWatch(HEAP_LIMIT);
        List<Thread> senders = new ArrayList<>();
        for (int t = 1; t <= THREADS; t++) {
            int thread = t;
            senders.add(new Thread(() -> {
                // One thread lends the transport its own buffer, and spoils it the moment the send returns.
                byte[] lent = new byte[ChannelHeader.MAX_NUMBERED + 80_000];
                long allocated = 0, sent = 0;
                for (int seq = 0; seq < frames && heap.breach == null; seq++) {
                    if (inFlight > 0) client.awaitCaughtUp(thread, seq - inFlight);
                    byte[] payload = payload(thread, seq);
                    long before = THREAD_BEAN.getCurrentThreadAllocatedBytes();
                    if (thread == 1) {
                        int start = ChannelHeader.write(lent, 0, WireTable.TYPE_LABEL, thread, WireTable.CHANNEL_C);
                        System.arraycopy(payload, 0, lent, start, payload.length);
                        comm.sendFrame(lent, 0, start + payload.length);
                        java.util.Arrays.fill(lent, (byte) 0xEE);
                    } else {
                        comm.send(WireTable.TYPE_LABEL, thread, WireTable.CHANNEL_C, payload);
                    }
                    allocated += THREAD_BEAN.getCurrentThreadAllocatedBytes() - before;
                    sent += payload.length;
                }
                sendAllocated.addAndGet(allocated);
                bytesSent.addAndGet(sent);
            }, "sender-" + t));
        }
        backSender.start();
        senders.forEach(Thread::start);
        for (Thread s : senders) s.join(60_000);
        backSender.join(60_000);
        Run run = new Run(heap, sendAllocated.get(), bytesSent.get());
        if (heap.breach != null) {
            heap.close();
            report(inFlight, run);
            return run;
        }

        assertThat(client.all.await(60, TimeUnit.SECONDS)).as("every frame arrived in time").isTrue();
        assertThat(allReadBack.await(60, TimeUnit.SECONDS)).as("every frame the client sent was read").isTrue();
        assertThat(client.errors).as("frames the client received").isEmpty();
        assertThat(readBackErrors).as("frames the server read").isEmpty();
        for (int t = 1; t <= THREADS; t++)
            assertThat(client.next.get(t)).as("frames from thread %d", t).hasValue(frames);
        Thread.sleep(100);
        assertThat(client.received).as("no frame arrived twice").hasValue(THREADS * frames);
        heap.close();
        report(inFlight, run);
        return run;
    }

    private static void report(int inFlight, Run run) {
        System.out.printf("[transport-stress] inFlight=%d: heap after a young collection grew by at most %d MB, garbage included (limit %d)%s;"
                        + " a send allocated %.3f bytes per byte sent (%d MB over %d MB)%n",
                inFlight, run.heap.peakGrowth >> 20, HEAP_LIMIT >> 20, run.heap.breach == null ? "" : ", " + run.heap.breach,
                run.allocatedPerByteSent(), run.sendAllocated >> 20, run.bytesSent >> 20);
    }

    /**
     * How far the heap in use right after each collection has grown since the watch started. Past
     * the limit, a full collection decides: what survives it is held, not merely waiting for an
     * old-generation cycle.
     */
    private static final class HeapWatch implements AutoCloseable {
        private final long limit;
        private final long baseline;
        private final List<Runnable> unsubscribe = new ArrayList<>();
        private final Thread confirmer;
        private volatile boolean confirming, closed;
        volatile long peakGrowth;
        volatile String breach;

        HeapWatch(long limit) {
            this.limit = limit;
            System.gc();
            baseline = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
            confirmer = new Thread(this::confirmLoop, "heap-watch");
            confirmer.setDaemon(true);
            confirmer.start();
            for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
                if (!(gc instanceof NotificationEmitter emitter)) continue;
                javax.management.NotificationListener listener = (n, h) -> afterCollection(n);
                emitter.addNotificationListener(listener, null, null);
                unsubscribe.add(() -> {
                    try {
                        emitter.removeNotificationListener(listener);
                    } catch (javax.management.ListenerNotFoundException ignored) {
                    }
                });
            }
        }

        private void afterCollection(javax.management.Notification n) {
            if (!com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(n.getType())) return;
            var info = com.sun.management.GarbageCollectionNotificationInfo.from((javax.management.openmbean.CompositeData) n.getUserData());
            long used = 0;
            for (MemoryUsage pool : info.getGcInfo().getMemoryUsageAfterGc().values()) used += pool.getUsed();
            long growth = used - baseline;
            if (growth > limit && breach == null) confirming = true;
            else if (!confirming && growth > peakGrowth) peakGrowth = growth;
        }

        private void confirmLoop() {
            while (!closed) {
                if (confirming) {
                    System.gc();
                    long growth = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() - baseline;
                    if (growth > peakGrowth) peakGrowth = growth;
                    if (growth > limit) breach = "grown by " + (growth >> 20) + " MB after a full collection";
                    confirming = false;
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        @Override
        public void close() {
            closed = true;
            unsubscribe.forEach(Runnable::run);
        }
    }

    private void waitUntilConnected() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (!comm.hasClient() && System.currentTimeMillis() < deadline) Thread.sleep(5);
        assertThat(comm.hasClient()).isTrue();
    }

    /** Thread {@code thread}'s frame {@code seq}: its thread, its number, its length, and bytes derived from all three. */
    private static byte[] payload(int thread, int seq) {
        int size = SIZES[(seq + thread) % SIZES.length];
        byte[] p = new byte[9 + size];
        p[0] = (byte) thread;
        ByteBuffer.wrap(p, 1, 8).putInt(seq).putInt(size);
        for (int i = 0; i < size; i++) p[9 + i] = (byte) (seq * 31 + thread * 7 + i);
        return p;
    }

    /** What is wrong with the payload {@code data[from, to)} expected from {@code thread} as frame {@code seq}, or null. */
    private static String check(byte[] data, int from, int to, int thread, int seq) {
        if (to - from < 9) return "short frame of " + (to - from) + " bytes";
        if (data[from] != (byte) thread) return "frame from thread " + data[from] + " where " + thread + " was expected";
        ByteBuffer b = ByteBuffer.wrap(data, from + 1, 8);
        int gotSeq = b.getInt(), size = b.getInt();
        if (gotSeq != seq) return "thread " + thread + ": frame " + gotSeq + " where " + seq + " was next";
        if (to - from != 9 + size) return "thread " + thread + " frame " + seq + ": " + (to - from - 9) + " bytes, not " + size;
        for (int i = 0; i < size; i++) {
            if (data[from + 9 + i] != (byte) (seq * 31 + thread * 7 + i))
                return "thread " + thread + " frame " + seq + ": byte " + i + " changed";
        }
        return null;
    }

    private static final class Client extends WebSocketClient {
        final Map<Integer, AtomicInteger> next = new ConcurrentHashMap<>();
        final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        final AtomicInteger received = new AtomicInteger();
        final CountDownLatch all;

        /** Waits until the client has read frame {@code seq} of {@code thread}, or until the run is failing. */
        void awaitCaughtUp(int thread, int seq) {
            AtomicInteger read = next.computeIfAbsent(thread, t -> new AtomicInteger());
            long deadline = System.nanoTime() + 30_000_000_000L;
            while (read.get() <= seq && errors.isEmpty() && System.nanoTime() < deadline) Thread.onSpinWait();
        }

        Client(URI uri, int expected) {
            super(uri);
            all = new CountDownLatch(expected);
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            byte[] data = new byte[bytes.remaining()];
            bytes.get(data);
            ChannelHeader.Decoded header = ChannelHeader.decode(data, 0, data.length);
            if (header == null) {
                errors.add("a frame with no header it could read");
            } else if (header.channel().endsWith("/c")) {
                int thread = data[header.payloadStart()];
                AtomicInteger expected = next.computeIfAbsent(thread, t -> new AtomicInteger());
                String error = check(data, header.payloadStart(), data.length, thread, expected.get());
                if (error != null) errors.add(error);
                expected.incrementAndGet();
                received.incrementAndGet();
                all.countDown();
            }
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            errors.add("client error: " + ex);
        }
    }
}

package dev.equo.swt.comm;

import org.eclipse.swt.widgets.Event;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How many times an inbound message's bytes are copied by the receiving side before its handler
 * has them.
 *
 * <p>Measured as the allocation that grows with the payload, on the server's WebSocket threads
 * only: the same message at two sizes, the difference in bytes those threads allocated over the
 * difference in payload bytes. The client's own masking copy happens on its threads and is not
 * counted; the server library's unmasked copy of each frame is, and cannot be avoided while
 * Java-WebSocket is the transport.
 */
class ReceiveCopyAllocationTest {

    private static final int PAYLOAD = 64 * 1024;
    private static final int WARMUP = 30;
    private static final int RUNS = 30;

    private static final com.sun.management.ThreadMXBean THREADS =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private BinaryCommService comm;
    private Client client;
    private final Semaphore handled = new Semaphore(0);
    private volatile int lastLength;

    @BeforeEach
    void setUp() throws Exception {
        comm = new BinaryCommService();
        client = new Client(URI.create("ws://localhost:" + comm.getPort()));
        assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.closeBlocking();
        comm.stop();
    }

    @Test
    void rawBytes_copies() throws Exception {
        comm.on("Probe/1/raw", byte[].class, bytes -> {
            lastLength = bytes.length;
            handled.release();
        });
        double copies = copiesPerPayloadByte(size -> frame("Probe/1/raw", new byte[size]));
        assertThat(lastLength).isEqualTo(2 * PAYLOAD);
        System.out.printf("[receive-copies] raw bytes: %.2f%n", copies);
    }

    @Test
    void byteBuffer_copies() throws Exception {
        comm.on("Probe/1/buffer", java.nio.ByteBuffer.class, bytes -> {
            lastLength = bytes.remaining();
            handled.release();
        });
        double copies = copiesPerPayloadByte(size -> frame("Probe/1/buffer", new byte[size]));
        assertThat(lastLength).isEqualTo(2 * PAYLOAD);
        System.out.printf("[receive-copies] byte buffer: %.2f%n", copies);
    }

    @Test
    void typedEvent_copies() throws Exception {
        comm.on("Probe/1/event", Event.class, event -> {
            lastLength = event.text.length();
            handled.release();
        });
        double copies = copiesPerPayloadByte(size -> frame("Probe/1/event",
                ("{\"text\":\"" + "x".repeat(size - 11) + "\"}").getBytes(StandardCharsets.UTF_8)));
        assertThat(lastLength).isEqualTo(2 * PAYLOAD - 11);
        System.out.printf("[receive-copies] typed event: %.2f%n", copies);
    }

    /** Extra bytes the server threads allocated per extra payload byte, between two payload sizes. */
    private double copiesPerPayloadByte(IntFunction<byte[]> frameOfSize) throws Exception {
        long small = fewestAllocated(frameOfSize.apply(PAYLOAD));
        long large = fewestAllocated(frameOfSize.apply(2 * PAYLOAD));
        return (large - small) / (double) PAYLOAD;
    }

    private long fewestAllocated(byte[] frame) throws Exception {
        for (int i = 0; i < WARMUP; i++) deliver(frame);
        long fewest = Long.MAX_VALUE;
        for (int i = 0; i < RUNS; i++) {
            long before = serverThreadsAllocated();
            deliver(frame);
            long allocated = settledServerAllocation() - before;
            if (allocated < fewest) fewest = allocated;
        }
        return fewest;
    }

    private void deliver(byte[] frame) throws Exception {
        client.send(frame);
        assertThat(handled.tryAcquire(5, TimeUnit.SECONDS)).as("the message reached its handler").isTrue();
    }

    /** The server threads' allocation once they have stopped moving: the handler returns before the worker does. */
    private long settledServerAllocation() throws InterruptedException {
        long previous = -1;
        long current = serverThreadsAllocated();
        while (current != previous) {
            Thread.sleep(2);
            previous = current;
            current = serverThreadsAllocated();
        }
        return current;
    }

    private static long serverThreadsAllocated() {
        long total = 0;
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            String name = t.getName();
            if (name.startsWith("WebSocketWorker") || name.startsWith("WebSocketSelector")) {
                long allocated = THREADS.getThreadAllocatedBytes(t.threadId());
                if (allocated > 0) total += allocated;
            }
        }
        return total;
    }

    private static byte[] frame(String event, byte[] payload) {
        byte[] header = CommService.frameHeader(event);
        byte[] frame = java.util.Arrays.copyOf(header, header.length + payload.length);
        System.arraycopy(payload, 0, frame, header.length, payload.length);
        return frame;
    }

    private static final class Client extends WebSocketClient {
        Client(URI uri) {
            super(uri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
        }
    }
}

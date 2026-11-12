package dev.equo.swt.comm;

import dev.equo.swt.Serializer;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default {@link CommService}, backed by the org.java-websocket server. Wire format and all
 * protocol handling live in {@link AbstractBinaryCommService}; this class only wires up the
 * transport. Selected by default (or via {@code -Dcomm.impl=java-websocket}).
 */
public class BinaryCommService extends AbstractBinaryCommService {

    /**
     * Offers permessage-deflate to the client. Off by default: on localhost it only costs CPU, and a
     * remote client's hop is compressed by the gateway in front of it.
     */
    public static final String DEFLATE = "dev.equo.swt.web.deflate";

    private final boolean deflate;
    private final WsServer server;
    private final Set<WebSocket> sessions = ConcurrentHashMap.newKeySet();
    /** Counted down from {@link WsServer#onStart()} once the ephemeral port is actually bound. */
    private final CountDownLatch started = new CountDownLatch(1);

    public BinaryCommService() {
        this(Boolean.getBoolean(DEFLATE));
    }

    public BinaryCommService(boolean deflate) {
        this.deflate = deflate;
        try (dev.equo.swt.BootTrace.Span span = dev.equo.swt.BootTrace.span("WsServer.new")) {
            server = new WsServer(deflate);
        }
        try (dev.equo.swt.BootTrace.Span span = dev.equo.swt.BootTrace.span("WsServer.start")) {
            server.start();
        } catch (Exception e) {
            try {
                server.stop();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to start WebSocket server", e);
        }
    }

    @Override
    public int getPort() {
        // server.start() binds asynchronously on the server thread; the ephemeral port (bound to
        // :0) is only known after onStart(). Block until then so callers never see the unbound 0.
        try {
            if (!started.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("WebSocket server did not bind within 5s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for WebSocket server to bind", e);
        }
        return server.getPort();
    }

    /** The host the server was asked to bind, as given: a literal address or a name. */
    String boundHost() {
        return server.getAddress().getHostString();
    }

    @Override
    protected void broadcast(byte[] frame, int offset, int length) {
        if (deflate && (offset != 0 || length != frame.length)) {
            // The deflate extension compresses the whole array behind a frame's buffer, not only the frame.
            frame = Arrays.copyOfRange(frame, offset, offset + length);
            offset = 0;
        }
        for (WebSocket s : sessions) {
            if (!s.isOpen()) continue;
            try {
                // Java-WebSocket copies the bytes into its own frame before send returns.
                send(s, ByteBuffer.wrap(frame, offset, length));
            } catch (RuntimeException e) {
                // Closed between the check and the send: that client is gone, and the others still get the frame.
                System.err.println(logTag() + " A frame could not reach a client: " + e);
            }
        }
        checkWritesSoon();
    }

    private void send(WebSocket s, ByteBuffer frame) {
        if (!deflate) {
            s.send(frame);
            return;
        }
        // A connection compresses with one Deflater, which is not safe to share between sending threads.
        synchronized (s) {
            s.send(frame);
        }
    }

    /**
     * Java-WebSocket can strand a queued frame until the next send: its selector thread, having
     * drained a connection's queue, sets the key back to OP_READ, and a send that queued a frame and
     * asked for OP_WRITE in between loses that request. A frame queued with no write interest is
     * that state exactly, so it is looked for shortly after every send and the write asked for again.
     */
    private void checkWritesSoon() {
        if (writeCheckPending.compareAndSet(false, true))
            WRITE_CHECKS.schedule(this::resumeStrandedWrites, 5, TimeUnit.MILLISECONDS);
    }

    private void resumeStrandedWrites() {
        writeCheckPending.set(false);
        boolean writing = false;
        for (WebSocket s : sessions) {
            if (!(s instanceof WebSocketImpl) || !s.isOpen()) continue;
            WebSocketImpl conn = (WebSocketImpl) s;
            if (conn.outQueue.isEmpty()) continue;
            SelectionKey key = conn.getSelectionKey();
            if (key == null || !key.isValid()) continue;
            writing = true;
            if ((key.interestOps() & SelectionKey.OP_WRITE) == 0) server.onWriteDemand(conn);
        }
        // Still draining: the selector can drop the interest again when it empties the queue.
        if (writing) checkWritesSoon();
    }

    private final AtomicBoolean writeCheckPending = new AtomicBoolean();

    private static final ScheduledExecutorService WRITE_CHECKS = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "comm-write-check");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println(logTag() + " Stop failed: " + e.getMessage());
        }
    }

    /**
     * Does the JDK work of a server handshake once, on the selector thread before any client
     * connects. The client's first handshake would otherwise pay for it while the window waits on
     * it: {@code Draft_6455} dates its response with a new {@link SimpleDateFormat} (the locale,
     * calendar and time-zone data load on first use) and computes the accept key with SHA-1 (the
     * security providers load on first use). Mirrors {@code Draft_6455} of java-websocket 1.5.7:
     * re-check it when that dependency moves.
     */
    static void warmUpHandshake() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        format.format(Calendar.getInstance().getTime());
        try {
            MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            // The handshake will fail on its own, where it is reported.
        }
    }

    private class WsServer extends WebSocketServer {
        WsServer(boolean deflate) {
            super(new InetSocketAddress(LOOPBACK, 0),
                    deflate ? java.util.Collections.<org.java_websocket.drafts.Draft>singletonList(new Draft_6455(new PerMessageDeflateExtension())) : null);
            setTcpNoDelay(true);
            // The selector + WebSocketWorker decoder threads are non-daemon by default, so a worker
            // that stop() can't unwind (e.g. wedged mid-decode on an active session) keeps the whole
            // JVM alive after the SWT app has closed. Mark them daemon — matching WebFlutterServer's
            // HTTP pool — so they never pin the process; comm.stop() still shuts them down gracefully
            // in the normal close path.
            setDaemon(true);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            sessions.add(conn);
            // Several clients share this comm's connection id, so what may travel as a name rather
            // than a description depends on how many there are -- see Serializer.clientsConnected.
            Serializer.clientsConnected(BinaryCommService.this, sessions.size());
            onClientConnected(frame -> send(conn, ByteBuffer.wrap(frame)));
            checkWritesSoon();
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            sessions.remove(conn);
            Serializer.clientsConnected(BinaryCommService.this, sessions.size());
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer blob) {
            // Java-WebSocket allocates a buffer for each message and never touches it again after
            // this callback, so its array can be handed over as it is.
            if (blob.hasArray() && blob.arrayOffset() == 0 && blob.position() == 0) {
                onBinaryMessage(blob.array(), 0, blob.remaining());
                return;
            }
            byte[] data = new byte[blob.remaining()];
            blob.get(data);
            onBinaryMessage(data, 0, data.length);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Ignore text frames
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            System.err.println(logTag() + " WebSocket error: " + ex.getMessage());
        }

        @Override
        public void onStart() {
            dev.equo.swt.BootTrace.markOnce("comm.bound");
            started.countDown();
            warmUpHandshake();
        }
    }
}

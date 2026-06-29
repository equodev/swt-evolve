package dev.equo.swt.comm;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Default {@link CommService}, backed by the org.java-websocket server. Wire format and all
 * protocol handling live in {@link AbstractBinaryCommService}; this class only wires up the
 * transport. Selected by default (or via {@code -Dcomm.impl=java-websocket}).
 */
public class BinaryCommService extends AbstractBinaryCommService {

    private final WsServer server;
    private final Set<WebSocket> sessions = ConcurrentHashMap.newKeySet();
    /** Counted down from {@link WsServer#onStart()} once the ephemeral port is actually bound. */
    private final CountDownLatch started = new CountDownLatch(1);

    public BinaryCommService() {
        server = new WsServer();
        try {
            server.start();
        } catch (Exception e) {
            try { server.stop(); } catch (Exception ignored) {}
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

    @Override
    protected void broadcast(byte[] frame) {
        for (WebSocket s : sessions) {
            if (s.isOpen()) {
                s.send(frame);
            }
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            System.err.println(logTag() + " Stop failed: " + e.getMessage());
        }
    }

    private class WsServer extends WebSocketServer {
        WsServer() {
            super(new InetSocketAddress("localhost", 0));
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
            onClientConnected(conn::send);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            sessions.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, ByteBuffer blob) {
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
            started.countDown();
        }
    }
}

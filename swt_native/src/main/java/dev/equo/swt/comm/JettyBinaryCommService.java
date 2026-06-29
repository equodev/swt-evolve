package dev.equo.swt.comm;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Alternate {@link CommService} backed by Jetty 12's WebSocket server (core flavor, no servlets).
 * Wire format and all protocol handling live in {@link AbstractBinaryCommService}; this class only
 * wires up the transport. Selected via {@code -Dcomm.impl=jetty}.
 *
 * <p>{@code setAcceptedTcpNoDelay(true)} disables Nagle on accepted connections, matching
 * {@link BinaryCommService}. Jetty 12 also dropped the old {@code BatchMode.AUTO} write
 * aggregation, so each {@link Session#sendBinary} flushes promptly. NOTE: neither change removes
 * Jetty's large-frame penalty — sending a ~250&nbsp;KB message still costs several milliseconds
 * (vs ~140&nbsp;µs on {@code java-websocket}); the cause is deeper in Jetty's write path, so
 * {@code java-websocket} remains the faster default for large payloads.
 */
public class JettyBinaryCommService extends AbstractBinaryCommService {

    private static final long MAX_MSG = 16L * 1024 * 1024;

    private final Server server;
    private final ServerConnector connector;
    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    public JettyBinaryCommService() {
        // Daemon thread pool so the comm never pins the JVM after the app closes (see BinaryCommService).
        QueuedThreadPool pool = new QueuedThreadPool();
        pool.setDaemon(true);
        server = new Server(pool);
        connector = new ServerConnector(server);
        connector.setHost("localhost");
        connector.setPort(0); // ephemeral
        connector.setAcceptedTcpNoDelay(true); // disable Nagle — see class javadoc
        server.addConnector(connector);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        WebSocketUpgradeHandler wsHandler = WebSocketUpgradeHandler.from(server, context, container -> {
            // Default Jetty limits cap binary messages low; our bench payloads can reach
            // hundreds of KB, so push limits well above that and keep messages single-frame.
            container.setMaxBinaryMessageSize(MAX_MSG);
            container.setMaxFrameSize(MAX_MSG);
            container.setIdleTimeout(Duration.ZERO);
            container.addMapping("/", (req, resp, cb) -> new BridgeSocket());
        });
        context.setHandler(wsHandler);
        server.setHandler(context);

        try {
            server.start();
        } catch (Exception e) {
            try { server.stop(); } catch (Exception ignored) {}
            throw new RuntimeException("Failed to start Jetty WebSocket server", e);
        }
    }

    @Override
    public int getPort() {
        return connector.getLocalPort();
    }

    private static final Callback SEND_CALLBACK = new Callback() {
        @Override public void fail(Throwable x) { System.err.println("[JettyBinaryCommService] Send failed: " + x); }
    };

    @Override
    protected void broadcast(byte[] frame) {
        for (Session s : sessions) {
            if (s.isOpen()) {
                s.sendBinary(ByteBuffer.wrap(frame), SEND_CALLBACK);
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

    /**
     * Jetty 12 demand-based endpoint. {@code AutoDemanding} means the framework requests the next
     * message automatically after each callback returns, so we only copy out the bytes (the buffer
     * is reused after we complete the callback) and {@code succeed()}. Must be public for Jetty's
     * MethodHandles-based invocation.
     */
    public class BridgeSocket implements Session.Listener.AutoDemanding {
        private volatile Session session;

        @Override
        public void onWebSocketOpen(Session sess) {
            this.session = sess;
            sessions.add(sess);
            onClientConnected(frame -> sess.sendBinary(ByteBuffer.wrap(frame), Callback.NOOP));
        }

        @Override
        public void onWebSocketBinary(ByteBuffer payload, Callback callback) {
            int len = payload.remaining();
            byte[] data = new byte[len];
            payload.get(data);
            onBinaryMessage(data, 0, len);
            callback.succeed();
        }

        @Override
        public void onWebSocketClose(int statusCode, String reason) {
            if (session != null) sessions.remove(session);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            System.err.println(logTag() + " WebSocket error: " + cause.getMessage());
        }
    }
}

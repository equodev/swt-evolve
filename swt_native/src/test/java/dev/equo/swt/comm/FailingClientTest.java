package dev.equo.swt.comm;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A client that goes away mid-send costs only itself: the others get every frame, and the sender is not stopped. */
class FailingClientTest {

    private BinaryCommService comm;
    private WebSocketClient client;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (client != null) client.closeBlocking();
        if (comm != null) comm.stop();
    }

    @Test
    @DisplayName("a client whose socket closed between the check and the send leaves the others their frames")
    @SuppressWarnings("unchecked")
    void aFailingClientCostsOnlyItself() throws Exception {
        comm = new BinaryCommService();
        int frames = 50;
        CountDownLatch received = new CountDownLatch(frames);
        client = new WebSocketClient(new URI("ws://127.0.0.1:" + comm.getPort())) {
            @Override
            public void onMessage(ByteBuffer bytes) {
                received.countDown();
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
            }
        };
        assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
        long deadline = System.currentTimeMillis() + 5000;
        while (!comm.hasClient() && System.currentTimeMillis() < deadline) Thread.sleep(5);

        // A session that reports itself open and then finds its socket closed, as one does that
        // closes between the two.
        WebSocket gone = mock(WebSocket.class);
        when(gone.isOpen()).thenReturn(true);
        doThrow(new WebsocketNotConnectedException()).when(gone).send(any(ByteBuffer.class));
        java.lang.reflect.Field field = BinaryCommService.class.getDeclaredField("sessions");
        field.setAccessible(true);
        ((Set<WebSocket>) field.get(comm)).add(gone);

        for (int i = 0; i < frames; i++) comm.send("Label/1/c", new byte[] {(byte) i});

        assertThat(received.await(10, TimeUnit.SECONDS)).as("the live client got every frame").isTrue();
    }
}

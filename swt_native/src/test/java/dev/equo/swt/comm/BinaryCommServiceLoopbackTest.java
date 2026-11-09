package dev.equo.swt.comm;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the address the desktop client connects to: {@code comm_ws.dart} dials the literal
 * {@code 127.0.0.1}, so the server has to be listening there whatever {@code localhost} resolves to.
 */
class BinaryCommServiceLoopbackTest {

    @Test
    void bindsTheLiteralLoopbackAddressRatherThanAName() {
        BinaryCommService comm = new BinaryCommService();
        try {
            comm.getPort();
            assertThat(comm.boundHost())
                    .as("a name such as localhost resolves per JVM, and may not be where the client dials")
                    .isEqualTo(AbstractBinaryCommService.LOOPBACK);
        } finally {
            comm.stop();
        }
    }

    @Test
    void servesAClientDialingTheLiteralLoopbackAddress() throws Exception {
        BinaryCommService comm = new BinaryCommService();
        CountDownLatch opened = new CountDownLatch(1);
        WebSocketClient client = new WebSocketClient(URI.create("ws://" + AbstractBinaryCommService.LOOPBACK + ":" + comm.getPort())) {
            @Override public void onOpen(ServerHandshake handshake) { opened.countDown(); }
            @Override public void onMessage(String message) { }
            @Override public void onClose(int code, String reason, boolean remote) { }
            @Override public void onError(Exception ex) { }
        };
        try {
            client.connect();
            assertThat(opened.await(10, TimeUnit.SECONDS)).as("handshake with ws://127.0.0.1").isTrue();
        } finally {
            client.closeBlocking();
            comm.stop();
        }
    }
}

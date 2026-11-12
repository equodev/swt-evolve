package dev.equo.swt.comm;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.DefaultExtension;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.net.SocketFactory;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BinaryCommService#DEFLATE} decides whether the comm socket takes up a client's offer of
 * permessage-deflate: with it, a large compressible frame travels compressed; without it, as it is.
 * Either way a frame lent inside a larger buffer arrives as exactly that frame.
 */
class CommDeflateTest {

    private static final int FRAME = 100 * 1024;

    @ParameterizedTest(name = "{0}, deflate {1}")
    @CsvSource({"java-websocket, true", "java-websocket, false", "jetty, true", "jetty, false"})
    @DisplayName("a large frame travels compressed only when the comm offers permessage-deflate")
    void deflateFollowsTheFlag(String impl, boolean deflate) throws Exception {
        String before = System.getProperty(BinaryCommService.DEFLATE);
        System.setProperty(BinaryCommService.DEFLATE, String.valueOf(deflate));
        AbstractBinaryCommService comm;
        try {
            comm = "jetty".equals(impl) ? new JettyBinaryCommService() : new BinaryCommService();
        } finally {
            if (before == null) System.clearProperty(BinaryCommService.DEFLATE);
            else System.setProperty(BinaryCommService.DEFLATE, before);
        }
        Client client = new Client(comm.getPort());
        try {
            assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
            assertThat(client.opened.await(5, TimeUnit.SECONDS)).isTrue();
            long atOpen = client.socket.read.get();

            byte[] frame = compressibleFrame();
            // Lent inside a larger buffer, as the comm's frame scratch is.
            byte[] lent = new byte[frame.length + 64 * 1024];
            new Random(1).nextBytes(lent);
            System.arraycopy(frame, 0, lent, 7, frame.length);
            comm.sendFrame(lent, 7, frame.length);
            byte[] got = client.next();
            long onSocket = client.socket.read.get() - atOpen;
            assertThat(got).isEqualTo(frame);

            comm.sendFrame(lent, 7, 100);
            assertThat(client.next()).isEqualTo(Arrays.copyOf(frame, 100));
            IExtension negotiated = ((Draft_6455) client.getConnection().getDraft()).getExtension();
            if (deflate) {
                assertThat(negotiated).isInstanceOf(PerMessageDeflateExtension.class);
                assertThat(onSocket).isLessThan(FRAME / 10);
            } else {
                assertThat(negotiated).isExactlyInstanceOf(DefaultExtension.class);
                assertThat(onSocket).isGreaterThanOrEqualTo(FRAME);
            }
        } finally {
            client.closeBlocking();
            comm.stop();
        }
    }

    /** Repeats of a small run of wire-like bytes: a header, a few varints and a short string. */
    private static byte[] compressibleFrame() {
        byte[] run = {0x02, 0x1f, (byte) 0x9a, 0x03, 0x00, 0x05, 'L', 'a', 'b', 'e', 'l', 0x7f, 0x01, (byte) 0x80, 0x02, 0x40};
        byte[] frame = new byte[FRAME];
        for (int i = 0; i < FRAME; i++) frame[i] = (byte) (run[i % run.length] + i / 4096);
        return frame;
    }

    /** A client that offers permessage-deflate and counts the bytes its socket reads. */
    private static final class Client extends WebSocketClient {
        final CountingSocket socket = new CountingSocket();
        final CountDownLatch opened = new CountDownLatch(1);
        final BlockingQueue<Object> messages = new LinkedBlockingQueue<>();

        Client(int port) {
            super(URI.create("ws://localhost:" + port + "/"), new Draft_6455(new PerMessageDeflateExtension()));
            setSocketFactory(new SocketFactory() {
                @Override
                public Socket createSocket() {
                    return socket;
                }

                @Override
                public Socket createSocket(String host, int port) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Socket createSocket(String host, int port, InetAddress local, int localPort) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Socket createSocket(InetAddress host, int port) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Socket createSocket(InetAddress host, int port, InetAddress local, int localPort) {
                    throw new UnsupportedOperationException();
                }
            });
        }

        byte[] next() throws Exception {
            Object m = messages.poll(5, TimeUnit.SECONDS);
            if (m instanceof Exception) throw (Exception) m;
            assertThat(m).as("a frame within 5s").isNotNull();
            ByteBuffer received = (ByteBuffer) m;
            byte[] bytes = new byte[received.remaining()];
            received.get(bytes);
            return bytes;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            opened.countDown();
        }

        @Override
        public void onMessage(String text) {
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            messages.add(bytes);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
        }

        @Override
        public void onError(Exception ex) {
            messages.add(ex);
        }
    }

    private static final class CountingSocket extends Socket {
        final AtomicLong read = new AtomicLong();
        private InputStream in;

        @Override
        public synchronized InputStream getInputStream() throws IOException {
            if (in == null) {
                in = new FilterInputStream(super.getInputStream()) {
                    @Override
                    public int read() throws IOException {
                        int b = super.read();
                        if (b >= 0) read.incrementAndGet();
                        return b;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        int n = super.read(b, off, len);
                        if (n > 0) read.addAndGet(n);
                        return n;
                    }
                };
            }
            return in;
        }
    }
}

package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The theme picker in the main toolbar tells Java what the user picked on
 * {@code swt.evolve.property.set}; Java keeps it and echoes the properties back. Driven over a real
 * comm, with each pick serialized from {@link ConfigFlags}, so the field names are the code's own.
 */
class ThemePickFromFlutterTest {

    private static final String PICK = "swt.evolve.property.set";
    private static final String ECHO = "swt.evolve.properties";
    private static final String[] PROPERTIES = {
            "swt.evolve.force_theme", "swt.evolve.theme_name", "swt.evolve.theme_color"};

    private final Serializer serializer = new Serializer();
    private final Map<String, String> savedProperties = new HashMap<>();
    private final BlockingQueue<ConfigFlags> echoes = new LinkedBlockingQueue<>();
    private ConfigFlags savedFlags;
    private CommService comm;
    private Client client;

    @BeforeEach
    void connect() throws Exception {
        savedFlags = Config.getConfigFlags();
        for (String key : PROPERTIES) {
            savedProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
        Config.setConfigFlags(null);
        comm = FlutterBridge.newComm();
        client = new Client(URI.create("ws://localhost:" + comm.getPort()));
        assertThat(client.connectBlocking(5, TimeUnit.SECONDS)).isTrue();
    }

    @AfterEach
    void restore() throws Exception {
        client.closeBlocking();
        comm.stop();
        savedProperties.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void a_pick_is_kept_and_echoed_back() throws Exception {
        pick(flags -> {
            flags.theme_name = "cursor";
            flags.force_theme = "light";
        });

        ConfigFlags echoed = nextEcho();
        assertThat(echoed.theme_name).isEqualTo("cursor");
        assertThat(echoed.force_theme).isEqualTo("light");
        assertThat(Config.getConfigFlags().theme_name).isEqualTo("cursor");
        assertThat(System.getProperty("swt.evolve.theme_name"))
                .as("what the flags are rebuilt from")
                .isEqualTo("cursor");
    }

    @Test
    void a_pick_that_changes_nothing_is_not_echoed() throws Exception {
        pick(flags -> flags.theme_color = "#112233");
        assertThat(nextEcho().theme_color).isEqualTo("#112233");

        pick(flags -> flags.theme_color = " #112233 ");
        pick(flags -> flags.theme_name = "hb");

        // The comm handles frames in order: had the repeat been echoed, its echo would come first.
        assertThat(nextEcho().theme_name).isEqualTo("hb");
    }

    @Test
    void a_blank_pick_clears_the_field() throws Exception {
        pick(flags -> flags.theme_color = "#112233");
        nextEcho();

        pick(flags -> flags.theme_color = "  ");

        assertThat(nextEcho().theme_color).isNull();
        assertThat(Config.getConfigFlags().theme_color).isNull();
    }

    private void pick(Consumer<ConfigFlags> set) throws Exception {
        ConfigFlags flags = new ConfigFlags();
        set.accept(flags);
        byte[] header = CommService.frameHeader(PICK);
        byte[] payload = serializer.to(flags);
        byte[] frame = Arrays.copyOf(header, header.length + payload.length);
        System.arraycopy(payload, 0, frame, header.length, payload.length);
        client.send(frame);
    }

    private ConfigFlags nextEcho() throws InterruptedException {
        ConfigFlags echoed = echoes.poll(5, TimeUnit.SECONDS);
        assertThat(echoed).as("the properties echoed back to the client").isNotNull();
        return echoed;
    }

    private final class Client extends WebSocketClient {
        Client(URI uri) {
            super(uri);
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
            byte[] frame = new byte[bytes.remaining()];
            bytes.get(frame);
            int nameLength = ((frame[0] & 0xFF) << 8) | (frame[1] & 0xFF);
            int payloadStart = CommService.NAME_LENGTH_BYTES + nameLength;
            String name = new String(frame, CommService.NAME_LENGTH_BYTES, nameLength, StandardCharsets.UTF_8);
            if (!ECHO.equals(name)) return;
            try {
                echoes.add(serializer.from(ConfigFlags.class, Arrays.copyOfRange(frame, payloadStart, frame.length)));
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
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
        }
    }
}

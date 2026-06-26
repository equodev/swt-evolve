package dev.equo.swt.harness;

import dev.equo.swt.comm.CommService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A {@link CommService} test double that captures traffic in both directions without any real
 * transport: outbound frames are recorded in {@link #sent}; inbound handlers registered via
 * {@link #on} are recorded in {@link #handlers} and can be invoked with {@link #fireContaining} to
 * simulate a Dart&rarr;Java event. Used by {@code RadioGroupDeliveryTest} (which inspects outbound
 * frames) and {@code DisplayWakeFlutterTest} (which fires inbound handlers).
 */
public class RecordingComm implements CommService {

    /** A recorded outbound frame: its channel ({@link #event}) and UTF-8 payload ({@link #json}). */
    public static final class Frame {
        public final String event;
        public final String json;

        Frame(String event, String json) {
            this.event = event;
            this.json = json;
        }
    }

    public final List<Frame> sent = new CopyOnWriteArrayList<>();
    public final Map<String, Consumer<?>> handlers = new ConcurrentHashMap<>();

    @Override
    public void send(String eventName) {
        sent.add(new Frame(eventName, ""));
    }

    @Override
    public void send(String eventName, byte[] payload) {
        sent.add(new Frame(eventName, new String(payload, StandardCharsets.UTF_8)));
    }

    @Override
    public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
        handlers.put(eventName, callback);
    }

    @Override
    public void remove(String eventName) {
        handlers.remove(eventName);
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void stop() {
    }

    /** Fire the single recorded inbound handler whose channel name contains {@code needle}. */
    @SuppressWarnings("unchecked")
    public <T> void fireContaining(String needle, T value) {
        String key = handlers.keySet().stream()
                .filter(k -> k.contains(needle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "no handler containing '" + needle + "'; registered: " + handlers.keySet()));
        ((Consumer<T>) handlers.get(key)).accept(value);
    }
}

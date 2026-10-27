package dev.equo.swt.awt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.equo.swt.comm.CommService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An engine with no socket of its own reaches the client only if a registered
 * {@link SwingIslandTransport} takes the Display's comm.
 */
class SwingIslandTransportTest {

    private final CommService displayComm = new NoComm();

    @Test
    @DisplayName("the Display's comm is offered to every transport until one takes it")
    void oneTransportTakesTheComm() {
        List<CommService> offered = new ArrayList<>();
        SwingIslandTransport declines = comm -> {
            offered.add(comm);
            return false;
        };
        SwingIslandTransport takes = comm -> {
            offered.add(comm);
            return true;
        };

        assertThat(SwingIslandHost.carriedBy(List.of(declines, takes), displayComm)).isTrue();
        assertThat(offered).containsExactly(displayComm, displayComm);
    }

    @Test
    @DisplayName("with no transport taking it, the engine's traffic has no way to the client")
    void noTransportTakesTheComm() {
        SwingIslandTransport declines = comm -> false;

        assertThat(SwingIslandHost.carriedBy(List.of(declines), displayComm)).isFalse();
        assertThat(SwingIslandHost.carriedBy(List.of(), displayComm)).isFalse();
    }

    private static final class NoComm implements CommService {
        @Override public void send(String eventName) {}
        @Override public void send(String eventName, byte[] payload) {}
        @Override public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {}
        @Override public void remove(String eventName) {}
        @Override public int getPort() { return 0; }
        @Override public void stop() {}
    }
}

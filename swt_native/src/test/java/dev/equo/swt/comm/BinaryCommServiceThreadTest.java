package dev.equo.swt.comm;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins that {@link BinaryCommService} never leaves a <b>non-daemon</b> thread alive — such a thread
 * keeps the whole JVM running after the SWT app has closed (observed: closing the browser tab on a
 * rich app left the process up even though {@code WebFlutterServer stopped}). The org.java-websocket
 * {@code WebSocketServer} spawns a selector thread plus a pool of {@code WebSocketWorker} decoder
 * threads (the {@code WebSocketWorker-N} seen in the leak's stack trace), all non-daemon by default.
 */
class BinaryCommServiceThreadTest {

    private static Set<Thread> snapshot() {
        return Thread.getAllStackTraces().keySet();
    }

    private static List<Thread> webSocketThreadsNotIn(Set<Thread> before) {
        return snapshot().stream()
                .filter(t -> !before.contains(t))
                .filter(Thread::isAlive)
                .filter(t -> {
                    String n = t.getName();
                    return n.contains("WebSocket") || n.contains("connectionLost");
                })
                .collect(Collectors.toList());
    }

    @Test
    void commServiceThreadsAreDaemon_soTheyDoNotKeepTheJvmAlive() {
        Set<Thread> before = snapshot();
        BinaryCommService comm = new BinaryCommService();
        try {
            comm.getPort(); // blocks until the server thread has actually bound

            List<Thread> ws = webSocketThreadsNotIn(before);
            assertThat(ws).as("the WebSocket server should have started its threads").isNotEmpty();

            List<String> nonDaemon = ws.stream()
                    .filter(t -> !t.isDaemon())
                    .map(Thread::getName)
                    .collect(Collectors.toList());
            assertThat(nonDaemon)
                    .as("WebSocket comm threads must be daemon, else they keep the JVM alive after "
                            + "the app closes; non-daemon threads found: " + nonDaemon)
                    .isEmpty();
        } finally {
            comm.stop();
        }
    }

    @Test
    void afterStop_noLingeringWebSocketThreads() throws InterruptedException {
        Set<Thread> before = snapshot();
        BinaryCommService comm = new BinaryCommService();
        comm.getPort();
        comm.stop();

        // Give the server a moment to unwind its selector/decoder threads.
        long deadline = System.currentTimeMillis() + 2000;
        List<Thread> lingering;
        while (!(lingering = webSocketThreadsNotIn(before).stream()
                .filter(t -> !t.isDaemon())
                .collect(Collectors.toList())).isEmpty()
                && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }

        assertThat(lingering.stream().map(Thread::getName).collect(Collectors.toList()))
                .as("stop() must leave no non-daemon WebSocket threads behind")
                .isEmpty();
    }
}

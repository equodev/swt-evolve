package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpServer;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A RuntimeException thrown by a runnable that {@code syncExec} runs on the UI thread reaches the
 * caller, as with SWT's default runtime exception handler, which only rethrows. Whoever catches it
 * (an e4 event dispatcher that logs it, for one) keeps the application running, so it is not a
 * crash: no dialog, no exit, no report. One nobody catches is a crash when a report URL is set;
 * without one, crash handling does nothing. Runs in a child JVM, because a crash ends the process.
 */
@Tag("native-unit")
public class CaughtSyncExecExceptionNativeTest {

    private static final long TIMEOUT_MS = 30_000;
    private static final String MESSAGE = "thrown inside syncExec";
    private static final String CAUGHT = "CAUGHT_BY_CALLER";
    private static final String ALIVE = "STILL_RUNNING";

    @TempDir
    Path home;

    @Test
    void anExceptionTheCallerCatchesIsNotReportedAndTheApplicationKeepsRunning() throws Exception {
        CompletableFuture<String> received = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            received.complete(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            Process child = startSession("caught", reportUrl(server));
            String output = awaitEnd(child);

            assertThat(child.exitValue()).as("child exit code, output: %s", output).isZero();
            assertThat(output).contains(CAUGHT, ALIVE);
            assertThat(crashLogs()).as("crash reports written").isEmpty();
            assertThat(received).as("report sent").isNotDone();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void withoutAReportUrlNothingIsWritten() throws Exception {
        Process child = startSession("caught");
        String output = awaitEnd(child);

        assertThat(child.exitValue()).as("child exit code, output: %s", output).isZero();
        assertThat(output).contains(CAUGHT, ALIVE);
        assertThat(home.resolve(".equo")).as("crash reporter directory").doesNotExist();
    }

    @Test
    void anExceptionNobodyCatchesIsACrashWhenAReportUrlIsSet() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        Process child = startSession("uncaught", reportUrl(server));
        try {
            String crashLog = awaitCrashLog(child);

            assertThat(crashLog).contains("java.lang.NullPointerException: " + MESSAGE);
            String output = output();
            assertThat(output).doesNotContain(ALIVE);
            if (!child.isAlive()) {
                assertThat(child.exitValue()).as("child exit code, output: %s", output).isNotZero();
            }
        } finally {
            child.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            server.stop(0);
        }
    }

    @Test
    void withoutAReportUrlHandleCrashDoesNothing() throws Exception {
        Process child = startSession("handleCrash");
        String output = awaitEnd(child);

        assertThat(child.exitValue()).as("child exit code, output: %s", output).isZero();
        assertThat(output).contains(ALIVE);
        assertThat(home.resolve(".equo")).as("crash reporter directory").doesNotExist();
    }

    private static String reportUrl(HttpServer server) {
        return "-Ddev.equo.swt.crashReportUrl=http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    private Process startSession(String mode, String... vmArgs) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.add("-Duser.home=" + home);
        command.addAll(List.of(vmArgs));
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(Session.class.getName());
        command.add(mode);
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true)
                .redirectOutput(home.resolve("child.log").toFile());
        builder.environment().remove("DISPLAY");
        builder.environment().remove("WAYLAND_DISPLAY");
        return builder.start();
    }

    /** Waits for the child to end on its own; a crash dialog would hold it until the timeout. */
    private String awaitEnd(Process child) throws Exception {
        boolean ended = child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!ended) child.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
        String output = output();
        assertThat(ended).as("child ended on its own, crash reports %s, output: %s", crashLogs(), output).isTrue();
        return output;
    }

    /** Waits for a crash log that holds the exception; the crash dialog may keep the child running. */
    private String awaitCrashLog(Process child) throws Exception {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            for (Path log : crashLogs()) {
                String content = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
                if (content.contains(MESSAGE)) return content;
            }
            if (!child.isAlive() && crashLogs().isEmpty()) break;
            Thread.sleep(50);
        }
        throw new AssertionError("no crash log holds the exception, crash reports " + crashLogs() + ", output: " + output());
    }

    private String output() throws Exception {
        return new String(Files.readAllBytes(home.resolve("child.log")), StandardCharsets.UTF_8);
    }

    private List<Path> crashLogs() throws Exception {
        Path dir = home.resolve(".equo");
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.getFileName().toString().startsWith("crash-")).toList();
        }
    }

    /**
     * Opens a Display, then by mode: {@code caught} throws inside a UI-thread syncExec and catches it;
     * {@code uncaught} throws there and lets it leave {@code main}; {@code handleCrash} hands an
     * exception to the crash reporter. Then it runs one more syncExec.
     */
    public static final class Session {
        public static void main(String[] args) throws Exception {
            // Config's class initializer runs CrashReporter.init(); a second call would read the
            // marker the first one wrote as an abnormal exit.
            FlutterBridge.set(new NoopBridge());
            Display display = new Display();
            switch (args[0]) {
                case "caught":
                    try {
                        display.syncExec(() -> {
                            throw new NullPointerException(MESSAGE);
                        });
                    } catch (NullPointerException expected) {
                        System.out.println(CAUGHT);
                    }
                    break;
                case "uncaught":
                    display.syncExec(() -> {
                        throw new NullPointerException(MESSAGE);
                    });
                    break;
                case "handleCrash":
                    CrashReporter.handleCrash(new NullPointerException(MESSAGE));
                    break;
                default:
                    throw new IllegalArgumentException(args[0]);
            }
            display.syncExec(() -> System.out.println(ALIVE));
            display.dispose();
            FlutterBridge.set(null);
        }
    }
}

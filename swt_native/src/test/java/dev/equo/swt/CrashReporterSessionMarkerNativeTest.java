package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The crash dialog is shown on the launch after a session whose marker was not left {@code clean}.
 * A session that ends in an orderly way must leave it {@code clean} even when the application never
 * disposed its Display; one that is killed must not. Shutdown hooks are what tells the two apart,
 * so each case runs a child JVM and reads the marker it left behind.
 */
@Tag("native-unit")
public class CrashReporterSessionMarkerNativeTest {

    private static final long TIMEOUT_MS = 30_000;

    @TempDir
    Path home;

    @Test
    void returningFromMainWithoutDisposingTheDisplayIsClean() throws Exception {
        assertThat(runSession("return")).isEqualTo("clean");
    }

    @Test
    void systemExitWithoutDisposingTheDisplayIsClean() throws Exception {
        assertThat(runSession("exit")).isEqualTo("clean");
    }

    @Test
    void aSessionThatIsKilledIsNotClean() throws Exception {
        assertThat(runSession("halt")).isEqualTo("started");
    }

    @Test
    void theLaunchAfterAKilledSessionStillSeesItAsAbnormal() throws Exception {
        runSession("halt");
        assertThat(runSession("return", "-Dprobe.reportPrevious=true"))
                .as("marker after the second session").isEqualTo("clean");
        assertThat(Files.readAllLines(home.resolve("probe.out"), StandardCharsets.UTF_8))
                .contains("PREVIOUS_ABNORMAL=true");
    }

    private String runSession(String ending, String... extraVmArgs) throws Exception {
        Path log = home.resolve("child.log");
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.add("-Duser.home=" + home);
        command.add("-Ddev.equo.swt.crashReportUrl=http://127.0.0.1:9/");
        for (String arg : extraVmArgs) command.add(arg);
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(Session.class.getName());
        command.add(ending);
        Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            child.destroyForcibly();
            throw new AssertionError("child session did not end: " + new String(Files.readAllBytes(log), StandardCharsets.UTF_8));
        }
        String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        assertThat(child.exitValue()).as("child session exit code, output: %s", output).isZero();
        return new String(Files.readAllBytes(home.resolve(".equo").resolve(".clean_shutdown")), StandardCharsets.UTF_8).trim();
    }

    /** Starts the crash reporter, waits until it has marked this session, and ends the way it is told to. */
    public static final class Session {
        public static void main(String[] args) throws Exception {
            CrashReporter.init();
            for (Thread t : Thread.getAllStackTraces().keySet()) {
                if ("crash-reporter-init".equals(t.getName())) t.join(TIMEOUT_MS);
            }
            if (Boolean.getBoolean("probe.reportPrevious")) {
                Files.write(Path.of(System.getProperty("user.home"), "probe.out"),
                        ("PREVIOUS_ABNORMAL=" + CrashReporter.previousSessionAbnormal()).getBytes(StandardCharsets.UTF_8));
            }
            switch (args[0]) {
                case "exit": System.exit(0); break;
                case "halt": Runtime.getRuntime().halt(0); break;
                default: return;
            }
        }
    }
}

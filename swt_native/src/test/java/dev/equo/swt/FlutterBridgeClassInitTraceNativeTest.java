package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The boot trace times {@link FlutterBridge}'s class initialization as a step of its own. A class is
 * initialized once per JVM, so the case runs a child JVM that initializes it between two marks.
 */
@Tag("native-unit")
public class FlutterBridgeClassInitTraceNativeTest {

    private static final long TIMEOUT_MS = 30_000;

    @TempDir
    Path dir;

    @Test
    void theClassInitializationIsRecordedAsAStep() throws Exception {
        List<Map<String, Object>> steps = runChild("-D" + BootTrace.PROPERTY + "=true");

        Map<String, Object> init = step(steps, FlutterBridge.CLASS_INIT_STEP)
                .orElseThrow(() -> new AssertionError("no class init step in " + steps));
        long before = micros(step(steps, Child.BEFORE).get(), "startUs");
        long after = micros(step(steps, Child.AFTER).get(), "startUs");
        assertThat(micros(init, "startUs")).isGreaterThanOrEqualTo(before);
        assertThat(micros(init, "endUs")).isBetween(micros(init, "startUs"), after);
    }

    private static Optional<Map<String, Object>> step(List<Map<String, Object>> steps, String name) {
        return steps.stream().filter(s -> name.equals(s.get("name"))).findFirst();
    }

    private static long micros(Map<String, Object> step, String key) {
        return ((Number) step.get(key)).longValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runChild(String... vmArgs) throws Exception {
        Path out = dir.resolve("child.out");
        List<String> command = new ArrayList<>();
        command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        command.addAll(Arrays.asList(vmArgs));
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(Child.class.getName());
        Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(out.toFile()).start();
        if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            child.destroyForcibly();
            throw new AssertionError("child did not end: " + new String(Files.readAllBytes(out), StandardCharsets.UTF_8));
        }
        String output = new String(Files.readAllBytes(out), StandardCharsets.UTF_8);
        assertThat(child.exitValue()).as("child exit code, output: %s", output).isZero();
        String json = output.substring(output.lastIndexOf(Child.TRACE) + Child.TRACE.length()).trim();
        return new Gson().fromJson(json, List.class);
    }

    /** Initializes {@link FlutterBridge} between two marks and prints the trace. */
    public static final class Child {
        static final String BEFORE = "before class init";
        static final String AFTER = "after class init";
        static final String TRACE = "TRACE:";

        public static void main(String[] args) throws Exception {
            BootTrace.mark(BEFORE);
            Class.forName(FlutterBridge.class.getName(), true, Child.class.getClassLoader());
            BootTrace.mark(AFTER);
            System.out.println(TRACE + BootTrace.toJson());
        }
    }
}

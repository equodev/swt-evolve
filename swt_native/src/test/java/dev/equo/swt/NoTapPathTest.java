package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Production runs with nothing watching the bridge, while every test JVM installs the send
 * observer and the wire counters. The tap is chosen once per JVM, so the path without one runs in a
 * JVM of its own, started without those properties: a widget still goes out, and a client event
 * still reaches its widget.
 */
class NoTapPathTest {

    @Test
    void withNothingWatchingWidgetsGoOutAndClientEventsComeIn() throws Exception {
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        Process process = new ProcessBuilder(java,
                "-cp", System.getProperty("java.class.path"),
                "-Ddev.equo.swt.loadLibrary=false",
                "-XX:+EnableDynamicAgentLoading",
                Run.class.getName())
                .redirectErrorStream(true)
                .start();
        boolean exited = process.waitFor(2, TimeUnit.MINUTES);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!exited) process.destroyForcibly();

        assertThat(exited).as("the run finished").isTrue();
        assertThat(process.exitValue()).as(output).isZero();
        assertThat(output).contains(Run.PASSED);
    }

    /** What the JVM without a tap runs; its exit status is the verdict. */
    static final class Run {
        static final String PASSED = "no-tap path: passed";

        public static void main(String[] args) {
            try {
                check();
                System.out.println(PASSED);
                System.exit(0);
            } catch (Throwable t) {
                t.printStackTrace(System.out);
                System.exit(1);
            }
        }

        private static void check() {
            assertThat(FlutterBridge.TAP).as("nothing watches the bridge").isSameAs(BridgeTap.NONE);
            Config.forceEquo();
            RecordingBridge bridge = new RecordingBridge();
            FlutterBridge.set(bridge);
            Shell shell = Mocks.shell();
            Display display = shell.getDisplay();
            List<Runnable> handed = new ArrayList<>();
            doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));

            Label label = new Label(shell, SWT.NONE);
            label.setText("Hello there");
            Button button = new Button(shell, SWT.PUSH);
            // A new widget goes out inside its parent's description; one the client has is sent by itself.
            FlutterBridge.update();
            label.setData("dev.equo.swt.new", false);
            bridge.dirty((org.eclipse.swt.widgets.DartWidget) label.getImpl());
            FlutterBridge.update().join();

            assertThat(bridge.comm.sent).as("the widgets went out")
                    .anyMatch(f -> f.event.equals("Label/" + label.hashCode()));

            handed.clear();
            bridge.comm.fireContaining("Button/" + button.hashCode() + "/Selection/Selection", new org.eclipse.swt.widgets.Event());
            assertThat(handed).as("the client's selection reached the button").hasSize(1);
        }
    }
}

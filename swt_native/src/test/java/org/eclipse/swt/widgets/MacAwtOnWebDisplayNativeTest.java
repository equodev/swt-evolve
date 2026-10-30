package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.internal.cocoa.NSApplication;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * AWT loaded on the main thread decides from {@code [NSApp isRunning]} whether it embeds in a host
 * application or runs its own event loop there. Native SWT's Display runs NSApp, so an SWT
 * application that loads AWT gets the first; the web Display has to answer the same, or AWT keeps
 * the main thread and the Display thread never gets it back.
 *
 * <p>Each scenario runs in a child JVM started with {@code -XstartOnFirstThread}, so the Display and
 * AWT share the main thread the way an application's do.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacAwtOnWebDisplayNativeTest {

    private static final long TIMEOUT_MS = 20_000;

    private static final String RUNNING = "NSApp is running";

    private static final String AWT_LOADED = "AWT loaded on the main thread";

    private static final String DISPATCHED = "Display thread dispatched after AWT";

    @Test
    void aWebDisplayLeavesNSAppRunning() throws Exception {
        assertThat(runProbe(IsRunningProbe.class))
                .as("the child's output")
                .contains(RUNNING);
    }

    /**
     * Whether a non-running NSApp costs the main thread depends on the JDK's AWT: Java 8's runs its
     * own loop there, Java 21's does not. Making NSApp report running must not cost it either.
     */
    @Test
    void aWebDisplayKeepsTheMainThreadAfterAwtLoadsOnIt() throws Exception {
        assertThat(runProbe(AwtProbe.class))
                .as("the child's output")
                .contains(AWT_LOADED)
                .contains(DISPATCHED);
    }

    /** Runs {@code probe}'s main in a child JVM on a web Display, and returns everything it printed. */
    private static String runProbe(Class<?> probe) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }

        Path log = Files.createTempFile("awt-web-display", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-Djava.awt.headless=false");
            command.add("-Ddev.equo.swt.mode=web");
            command.add("-Dequo.swt.browser=none");
            command.add("-Ddev.equo.swt.crashReport.disabled=true");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(probe.getName());
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS + 30_000, TimeUnit.MILLISECONDS))
                child.destroyForcibly().waitFor();

            return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** Ends the child after twice {@link #TIMEOUT_MS} with {@code message}, whatever the main thread is doing. */
    private static void haltAfterTimeout(String message) {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(2 * TIMEOUT_MS);
            } catch (InterruptedException e) {
                return;
            }
            System.out.println(message);
            System.out.flush();
            Runtime.getRuntime().halt(0);
        });
        watchdog.setDaemon(true);
        watchdog.start();
    }

    public static final class IsRunningProbe {

        public static void main(String[] args) {
            new Display();
            boolean running = NSApplication.sharedApplication().isRunning();
            System.out.println(running ? RUNNING : "NSApp is not running");
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /** What an application that loads AWT on the Display thread does, then its event loop. */
    public static final class AwtProbe {

        private static volatile boolean dispatched;

        public static void main(String[] args) throws Exception {
            haltAfterTimeout("the main thread never came back from AWT");
            Display display = new Display();
            java.awt.Toolkit.getDefaultToolkit();
            // AWT's own startup is asynchronous: a font metric needs its toolkit thread to answer.
            new java.awt.Frame().getFontMetrics(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            System.out.println(AWT_LOADED);
            System.out.flush();

            Thread poster = new Thread(() -> display.asyncExec(() -> dispatched = true));
            poster.setDaemon(true);
            poster.start();
            long deadline = System.currentTimeMillis() + TIMEOUT_MS;
            while (!dispatched && System.currentTimeMillis() < deadline) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            System.out.println(dispatched ? DISPATCHED : "the Display thread never dispatched after AWT");
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }
}

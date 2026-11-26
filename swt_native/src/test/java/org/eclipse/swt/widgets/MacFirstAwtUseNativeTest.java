package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * AWT's first use on macOS hands its AppKit setup to the main thread and waits for it. Under
 * {@code -XstartOnFirstThread} the main thread is the Display thread, so a browser-surface Display
 * that idles in {@code Display.sleep()} has to service the main run loop, as native SWT's does, or
 * the first {@code createGraphics()} on any other thread never returns. Each scenario runs in a
 * child JVM: AWT initializes once per process.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacFirstAwtUseNativeTest {

    private static final long TIMEOUT_MS = 30_000;
    private static final long MAX_WAKE_NANOS = TimeUnit.MILLISECONDS.toNanos(500);
    private static final double MAX_IDLE_CPU_PERCENT = 25;

    @Test
    void firstAwtUseOnAnotherThreadReturnsWhileTheDisplaySleeps() throws Exception {
        assertThat(runProbe(FirstAwtUseProbe.class)).as("the child's output").contains("awt=drawn");
    }

    @Test
    void anAsyncExecFromAnotherThreadEndsTheSleepInTheRunLoop() throws Exception {
        assertThat(runProbe(WakeProbe.class)).as("the child's output").contains("woken=true");
    }

    @Test
    void anIdleDisplayInTheRunLoopKeepsTheProcessNearIdle() throws Exception {
        assertThat(runProbe(IdleCpuProbe.class)).as("the child's output").contains("idle=true");
    }

    /** Runs {@code probe} in a child JVM whose main thread is the AppKit main thread, and returns its output. */
    private static String runProbe(Class<?> probe) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }

        Path log = Files.createTempFile("first-awt-use", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-Djava.awt.headless=false");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(probe.getName());
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                child.destroyForcibly();

            String output = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            System.out.print(output);
            return output;
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /**
     * Sets up a browser-surface Display on the main thread, starts AWT for the first time on another
     * thread, and runs the event loop until that thread returns or 10 seconds pass.
     */
    public static final class FirstAwtUseProbe {

        public static void main(String[] args) {
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
            System.setProperty("dev.equo.swt.desktop", "false");
            FlutterBridge.set(new RecordingBridge());
            Display display = new Display();
            DisplayBridgePlatform.init();

            AtomicReference<String> outcome = new AtomicReference<>();
            Thread awt = new Thread(() -> {
                try {
                    new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).createGraphics().dispose();
                    outcome.set("drawn");
                } catch (Throwable failure) {
                    outcome.set(failure.toString());
                }
                display.wake();
            }, "first-awt-use");
            awt.setDaemon(true);
            awt.start();

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (outcome.get() == null && System.nanoTime() < deadline) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            System.out.println("awt=" + (outcome.get() == null ? "hung" : outcome.get()));
            if (outcome.get() == null) {
                for (StackTraceElement frame : awt.getStackTrace())
                    System.out.println("\tat " + frame);
            }
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /**
     * With a park cap far above the expected latency, each asyncExec posted from another thread while
     * the main-thread Display sleeps has to end that sleep within {@link #MAX_WAKE_NANOS}: the run
     * loop cannot see the wake permit, and a missed wake would last the whole cap.
     */
    public static final class WakeProbe {

        private static final int POSTS = 200;

        public static void main(String[] args) {
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
            System.setProperty("dev.equo.swt.desktop", "false");
            FlutterBridge.set(new RecordingBridge());
            Display display = new Display();
            DisplayBridgePlatform.init();
            DartDisplay.WEB_PARK_CAP_MS = 20_000;
            while (display.readAndDispatch()) {
            }

            long[] latencies = new long[POSTS];
            AtomicInteger done = new AtomicInteger();
            Thread poster = new Thread(() -> {
                for (int i = 0; i < POSTS; i++) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        return;
                    }
                    int post = i;
                    long postedAt = System.nanoTime();
                    display.asyncExec(() -> {
                        latencies[post] = System.nanoTime() - postedAt;
                        done.incrementAndGet();
                    });
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
                    while (done.get() <= post && System.nanoTime() < deadline)
                        Thread.yield();
                }
            }, "poster");
            poster.setDaemon(true);
            poster.start();

            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (done.get() < POSTS && System.nanoTime() < end) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            long[] sorted = Arrays.copyOf(latencies, done.get());
            Arrays.sort(sorted);
            long max = sorted.length == 0 ? Long.MAX_VALUE : sorted[sorted.length - 1];
            System.out.println("woken=" + (sorted.length == POSTS && max < MAX_WAKE_NANOS));
            if (sorted.length > 0)
                System.out.printf("latency ms: median=%.3f p95=%.3f max=%.3f of %d%n", sorted[sorted.length / 2] / 1e6,
                        sorted[sorted.length * 95 / 100] / 1e6, max / 1e6, sorted.length);
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /**
     * After AWT's first use has installed its run-loop sources, a browser-surface Display left idle
     * on the main thread for a few seconds, at the default park cap, has to keep the process near
     * idle: a run loop that returned at once on every tick would hold a core busy.
     */
    public static final class IdleCpuProbe {

        public static void main(String[] args) throws Exception {
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
            System.setProperty("dev.equo.swt.desktop", "false");
            FlutterBridge.set(new RecordingBridge());
            Display display = new Display();
            DisplayBridgePlatform.init();

            AtomicReference<Boolean> drawn = new AtomicReference<>();
            Thread awt = new Thread(() -> {
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).createGraphics().dispose();
                drawn.set(true);
                display.wake();
            }, "first-awt-use");
            awt.setDaemon(true);
            awt.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (drawn.get() == null && System.nanoTime() < deadline) {
                if (!display.readAndDispatch())
                    display.sleep();
            }

            com.sun.management.OperatingSystemMXBean os =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long cpuStart = os.getProcessCpuTime();
            long start = System.nanoTime();
            while (System.nanoTime() - start < TimeUnit.SECONDS.toNanos(5)) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            double cpu = 100.0 * (os.getProcessCpuTime() - cpuStart) / (System.nanoTime() - start);
            System.out.printf("idle cpu %%: %.2f%n", cpu);
            System.out.println("idle=" + (drawn.get() != null && cpu < MAX_IDLE_CPU_PERCENT));
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }
}

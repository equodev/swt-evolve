package org.eclipse.swt.widgets;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.swt.internal.cocoa.NSApplication;
import org.junit.jupiter.api.Assumptions;

/**
 * Runs a probe's {@code main} in a child JVM started with {@code -XstartOnFirstThread} on a web
 * Display, so the Display owns the main thread the way an application's does. What only the main
 * thread gets (Apple Events, AWT's choice of event loop) can only be tested there.
 */
final class MacChildJvm {

    /** How long a probe waits for what it checks; the child is killed well after that. */
    static final long TIMEOUT_MS = 20_000;

    private MacChildJvm() {
    }

    /** Runs {@code probe}'s main with {@code vmArgs}, and returns everything it printed. */
    static String run(Class<?> probe, String... vmArgs) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }

        Path log = Files.createTempFile("mac-child-jvm", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-Ddev.equo.swt.mode=web");
            command.add("-Dequo.swt.browser=none");
            command.add("-Ddev.equo.swt.crashReport.disabled=true");
            for (String vmArg : vmArgs)
                command.add(vmArg);
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

    /**
     * Has another thread {@code asyncExec} on {@code display}, as the comm thread does with every
     * client message, then spins the event loop until that runs or {@link #TIMEOUT_MS} passes.
     *
     * @return whether the posted work ran
     */
    static boolean dispatchesOffThreadWork(Display display) {
        boolean[] dispatched = new boolean[1];
        Thread poster = new Thread(() -> display.asyncExec(() -> dispatched[0] = true));
        poster.setDaemon(true);
        poster.start();
        return spinUntil(display, () -> dispatched[0]);
    }

    /**
     * Spins {@code display}'s event loop until {@code done} or {@link #TIMEOUT_MS} passes.
     *
     * @return whether {@code done} became true
     */
    static boolean spinUntil(Display display, BooleanSupplier done) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!done.getAsBoolean() && System.currentTimeMillis() < deadline) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return done.getAsBoolean();
    }

    /** Prints {@code line} and ends the child at once, whatever its other threads are doing. */
    static void exitWith(String line) {
        System.out.println(line);
        System.out.flush();
        Runtime.getRuntime().halt(0);
    }
}

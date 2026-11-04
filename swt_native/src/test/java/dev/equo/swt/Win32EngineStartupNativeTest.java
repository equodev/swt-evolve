package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ServerSocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * When shells get windows of their own, each window starts an engine of its own, synchronously, on
 * the UI thread, so the start has to be short; Impeller is off for it.
 *
 * <p>The windows are created hidden and never pumped, so nothing is shown. The first window of the
 * process owns the application's exit and posts {@code WM_QUIT} when destroyed; each dispose drains
 * that from this thread's queue so a later test is not handed it.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64",
        disabledReason = "the arm64 Windows engine starts with Impeller even when the runner disables it")
public class Win32EngineStartupNativeTest {

    private static final long BUDGET_MS = 300;

    private String savedMode;

    @BeforeEach
    void multiWindow() {
        savedMode = System.getProperty(WindowPolicy.MODE_PROPERTY);
        System.setProperty(WindowPolicy.MODE_PROPERTY, "all");
    }

    @AfterEach
    void restore() {
        if (savedMode == null) System.clearProperty(WindowPolicy.MODE_PROPERTY);
        else System.setProperty(WindowPolicy.MODE_PROPERTY, savedMode);
    }

    @Test
    void aShellWindowStartsItsEngineInAFewHundredMilliseconds() throws Exception {
        try {
            FlutterLibraryLoader.initialize();
        } catch (FlutterLibraryLoader.LibraryLoaderException e) {
            Assumptions.abort("no Windows runner built here: " + e.getMessage());
        }
        try (ServerSocket nobody = new ServerSocket(0)) {
            // Warm the process-wide state the first engine pays for once (the Dart VM, the library).
            dispose(start(nobody.getLocalPort()));

            long t0 = System.nanoTime();
            long context = start(nobody.getLocalPort());
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000;
            dispose(context);

            assertThat(elapsedMs).as("ms to start one more window's engine").isLessThan(BUDGET_MS);
        }
    }

    private static long start(int port) {
        long context = FlutterNative.initialize(port, 0, 1, "Shell", "light", 0, 0, 400, 300);
        assertThat(context).as("engine started").isNotZero();
        return context;
    }

    private static void dispose(long context) {
        FlutterNative.dispose(context);
        FlutterNative.pumpMessages(Integer.MAX_VALUE);
    }
}

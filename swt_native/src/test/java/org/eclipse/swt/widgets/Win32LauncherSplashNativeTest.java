package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * The Equinox launcher shows its splash in a window of the thread that goes on to run the Display,
 * and publishes its handle in {@value #SPLASH_HANDLE}. A Display rendering in a browser never pumps
 * that thread's window messages, so the splash would stay on the desktop, reported as not responding,
 * until the application took it down.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
public class Win32LauncherSplashNativeTest {

    private static final String SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle";

    private String loadLibrary;

    private long splash;

    @BeforeEach
    void showASplash() {
        // The suite runs with the native library switched off; the launcher's window is native.
        loadLibrary = System.getProperty(DisplayBridgePlatform.LOAD_LIBRARY);
        System.clearProperty(DisplayBridgePlatform.LOAD_LIBRARY);
        splash = OS.CreateWindowEx(0, new TCHAR(0, "STATIC", true), null, OS.WS_POPUP | OS.WS_VISIBLE,
                0, 0, 120, 80, 0, 0, 0, null);
        assertThat(splash).as("a splash window to close").isNotZero();
    }

    @AfterEach
    void restore() {
        System.clearProperty(SPLASH_HANDLE);
        if (loadLibrary != null)
            System.setProperty(DisplayBridgePlatform.LOAD_LIBRARY, loadLibrary);
        if (exists(splash))
            OS.DestroyWindow(splash);
    }

    @Test
    void destroysTheLaunchersSplashFromItsThread() {
        System.setProperty(SPLASH_HANDLE, Long.toString(splash));

        assertThat(DisplayBridgePlatform.dismissLauncherSplash()).as("dismissed").isTrue();
        assertThat(exists(splash)).as("the launcher's splash window").isFalse();
    }

    /** Only the owning thread can destroy a window. */
    @Test
    void leavesTheSplashOfAnotherThreadAlone() throws InterruptedException {
        System.setProperty(SPLASH_HANDLE, Long.toString(splash));
        AtomicBoolean dismissed = new AtomicBoolean(true);

        Thread other = new Thread(() -> dismissed.set(DisplayBridgePlatform.dismissLauncherSplash()));
        other.start();
        other.join();

        assertThat(dismissed.get()).as("dismissed from another thread").isFalse();
        assertThat(OS.IsWindowVisible(splash)).as("the launcher's splash window").isTrue();
    }

    @Test
    void leavesAWindowTheLauncherDoesNotNameAlone() {
        assertThat(DisplayBridgePlatform.dismissLauncherSplash()).as("dismissed").isFalse();
        assertThat(OS.IsWindowVisible(splash)).as("a window that is not the launcher's splash").isTrue();
    }

    private static boolean exists(long hwnd) {
        return hwnd != 0 && OS.GetWindowThreadProcessId(hwnd, new int[1]) != 0;
    }
}

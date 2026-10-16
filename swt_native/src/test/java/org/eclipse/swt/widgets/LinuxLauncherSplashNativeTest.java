package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * On GTK the web Display leaves the Equinox launcher's splash window, published in
 * {@value #SPLASH_HANDLE}, to the launcher, which takes it down once the application runs.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.LINUX)
public class LinuxLauncherSplashNativeTest {

    private static final String SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle";

    private String loadLibrary;

    @BeforeEach
    void switchTheNativeLibraryOn() {
        loadLibrary = System.getProperty(DisplayBridgePlatform.LOAD_LIBRARY);
        System.clearProperty(DisplayBridgePlatform.LOAD_LIBRARY);
    }

    @AfterEach
    void restore() {
        System.clearProperty(SPLASH_HANDLE);
        if (loadLibrary != null)
            System.setProperty(DisplayBridgePlatform.LOAD_LIBRARY, loadLibrary);
    }

    @Test
    void leavesTheLaunchersSplashToTheLauncher() {
        System.setProperty(SPLASH_HANDLE, "1");

        assertThat(DisplayBridgePlatform.dismissLauncherSplash()).as("dismissed").isFalse();
    }
}

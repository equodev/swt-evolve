package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code system_menu_bar} is the single answer to "who draws the Shell's menu bar": the OS, or
 * Flutter inside the window. Both sides read it -- {@code DartMainToolbar} to stop reserving the
 * strip/button space, {@code DecorationsMenuData} to stop drawing them -- so it has to say yes on
 * exactly the surfaces whose window this process owns, and only on macOS.
 */
class ConfigSystemMenuBarTest {

    private ConfigFlags savedFlags;
    private String savedMode;
    private String savedOsName;
    private String savedOverride;

    @BeforeEach
    void captureState() {
        savedFlags = Config.getConfigFlags();
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedOsName = System.getProperty("os.name");
        savedOverride = System.getProperty("dev.equo.swt.systemMenuBar");
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restoreState() {
        restore(ConfigFlags.MODE_PROPERTY, savedMode);
        restore("os.name", savedOsName);
        restore("dev.equo.swt.systemMenuBar", savedOverride);
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private static void surface(String mode, String osName) {
        restore(ConfigFlags.MODE_PROPERTY, mode);
        System.setProperty("os.name", osName);
        System.clearProperty("dev.equo.swt.systemMenuBar");
    }

    @Test
    void a_window_this_process_owns_hands_its_menu_bar_to_the_os() {
        surface(ConfigFlags.MODE_DESKTOP, "Mac OS X");
        assertThat(Config.getConfigFlags().system_menu_bar).isTrue();

        // The Chromium standalone window is this process's too, so the bar above it is ours as well.
        Config.setConfigFlags(null);
        surface(ConfigFlags.MODE_CHROMIUM, "Mac OS X");
        assertThat(Config.getConfigFlags().system_menu_bar).isTrue();
    }

    @Test
    void a_browser_tab_keeps_the_menu_bar_in_the_window() {
        // The system menu bar there belongs to the browser, not to this process.
        surface(null, "Mac OS X");

        assertThat(Config.getConfigFlags().system_menu_bar).isFalse();
    }

    @Test
    void windows_and_linux_keep_the_menu_bar_in_the_window() {
        surface(ConfigFlags.MODE_DESKTOP, "Windows 11");
        assertThat(Config.getConfigFlags().system_menu_bar).isFalse();

        Config.setConfigFlags(null);
        surface(ConfigFlags.MODE_CHROMIUM, "Linux");
        assertThat(Config.getConfigFlags().system_menu_bar).isFalse();
    }

    @Test
    void the_override_decides_either_way() {
        surface(ConfigFlags.MODE_DESKTOP, "Mac OS X");
        System.setProperty("dev.equo.swt.systemMenuBar", "false");
        assertThat(Config.getConfigFlags().system_menu_bar).isFalse();

        Config.setConfigFlags(null);
        surface(null, "Linux");
        System.setProperty("dev.equo.swt.systemMenuBar", "true");
        assertThat(Config.getConfigFlags().system_menu_bar).isTrue();
    }

    @Test
    void the_mode_is_read_at_every_call_because_the_desktop_bridge_sets_it_late() {
        // The flags are computed and cached the first time anything asks for them, which for a
        // desktop run happens before DeskDisplayBridge switches the mode over.
        surface(null, "Mac OS X");
        assertThat(Config.getConfigFlags().system_menu_bar).isFalse();

        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);

        assertThat(Config.getConfigFlags().system_menu_bar).isTrue();
    }
}

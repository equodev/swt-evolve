package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code Display.getMenuBar()} against the native contract: only Cocoa has an application-wide
 * menu bar, and everywhere else it is null. Applications branch on it, and a bar where there is no OS
 * bar to show it in sends them down their macOS path: they build their menus into a bar that has no
 * Shell, and one that checks it is on a Mac before trusting such a menu aborts at startup.
 */
@Tag("flutter-it")
class DisplayMenuBarFlutterTest {

    private static final String SYSTEM_MENU_BAR = "dev.equo.swt.systemMenuBar";

    private Display display;
    private String savedSystemMenuBar;

    @BeforeEach
    void setUp() {
        savedSystemMenuBar = System.getProperty(SYSTEM_MENU_BAR);
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        if (savedSystemMenuBar == null)
            System.clearProperty(SYSTEM_MENU_BAR);
        else
            System.setProperty(SYSTEM_MENU_BAR, savedSystemMenuBar);
    }

    @Test
    void noMenuBarWithoutAnOsMenuBar() {
        System.setProperty(SYSTEM_MENU_BAR, "false");

        assertThat(display.getMenuBar())
                .as("no OS menu bar to show it in, so there is no application menu bar, as on win32/gtk")
                .isNull();
    }

    @Test
    void oneSharedMenuBarWithAnOsMenuBar() {
        System.setProperty(SYSTEM_MENU_BAR, "true");

        Menu bar = display.getMenuBar();
        assertThat(bar).as("the OS menu bar has an application-wide bar behind it, as on Cocoa").isNotNull();
        assertThat(display.getMenuBar()).as("and it is the same one on every call").isSameAs(bar);
    }
}

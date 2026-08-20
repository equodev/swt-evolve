package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * The macOS menu bar belongs to the OS, so the application menu can only be asserted against the
 * live NSApp: without the {@link MacApplicationMenu} call in {@link DisplayBridgePlatform} the bar
 * keeps the bare item Cocoa installs, which has no About and no Settings.
 */
@Tag("native-unit")
@EnabledOnOs(OS.MAC)
public class MacApplicationMenuNativeTest {

    private static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    @Test
    void installsTheStandardApplicationMenu() {
        NSApplication application;
        try {
            application = NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
            return;
        }

        // The suite runs with the native library switched off, which is exactly what init() skips on.
        String loadLibrary = System.getProperty(LOAD_LIBRARY);
        System.clearProperty(LOAD_LIBRARY);
        try {
            DisplayBridgePlatform.init();
        } finally {
            if (loadLibrary != null)
                System.setProperty(LOAD_LIBRARY, loadLibrary);
        }

        NSMenu mainMenu = application.mainMenu();
        assertThat(mainMenu).isNotNull();
        assertThat(mainMenu.numberOfItems()).isGreaterThan(0);

        NSMenu appMenu = mainMenu.itemAtIndex(0).submenu();
        assertThat(appMenu).isNotNull();

        List<Long> tags = new ArrayList<>();
        for (int i = 0; i < appMenu.numberOfItems(); i++) {
            NSMenuItem item = appMenu.itemAtIndex(i);
            if (item.tag() != 0)
                tags.add(item.tag());
        }
        assertThat(tags).containsExactly((long) SWT.ID_ABOUT, (long) SWT.ID_PREFERENCES, (long) SWT.ID_HIDE,
                (long) SWT.ID_HIDE_OTHERS, (long) SWT.ID_SHOW_ALL, (long) SWT.ID_QUIT);
    }
}

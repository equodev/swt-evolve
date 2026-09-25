package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
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

    private static final String SYSTEM_MENU_BAR = "dev.equo.swt.systemMenuBar";

    /**
     * Where no OS menu bar shows it, the client draws the application menu itself -- so the menu is
     * still built, out of the Display's own bar. {@code Display.getMenuBar()} is null on that
     * surface, being the answer to what the OS has, and is not what the menu hangs off.
     */
    @Test
    void buildsTheApplicationMenuWithoutAnOsMenuBar() {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
            return;
        }

        ConfigFlags flags = Config.getConfigFlags();
        String systemMenuBar = System.getProperty(SYSTEM_MENU_BAR);
        System.setProperty(SYSTEM_MENU_BAR, "false");
        Config.setConfigFlags(null);
        FlutterBridge.set(new RecordingBridge());
        Display display = new Display();
        FlutterBridge.set(null);
        try {
            assertThat(display.getMenuBar()).as("no OS menu bar to show an application one in").isNull();

            Menu appMenu = display.getSystemMenu();
            assertThat(appMenu).as("the application menu the client draws").isNotNull();
            assertThat(appMenu.getItemCount()).isGreaterThan(0);
        } finally {
            display.dispose();
            FlutterBridge.set(null);
            if (systemMenuBar == null)
                System.clearProperty(SYSTEM_MENU_BAR);
            else
                System.setProperty(SYSTEM_MENU_BAR, systemMenuBar);
            Config.setConfigFlags(flags);
        }
    }

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

    /**
     * The item titles, read back off the live NSMenu. The {@code SWT_*} keys the menu labels from
     * only entered {@code SWTMessages} in SWT 3.119, so an older build -- and a classpath carrying
     * no bundle at all, which is this suite's -- renders the keys themselves as the item text.
     */
    @Test
    void labelsTheStandardItems() {
        NSApplication application;
        try {
            application = NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
            return;
        }

        String loadLibrary = System.getProperty(LOAD_LIBRARY);
        System.clearProperty(LOAD_LIBRARY);
        try {
            DisplayBridgePlatform.init();
        } finally {
            if (loadLibrary != null)
                System.setProperty(LOAD_LIBRARY, loadLibrary);
        }

        NSMenu appMenu = application.mainMenu().itemAtIndex(0).submenu();
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < appMenu.numberOfItems(); i++) {
            NSMenuItem item = appMenu.itemAtIndex(i);
            if (!item.isSeparatorItem())
                titles.add(item.title().getString());
        }
        assertThat(titles).allSatisfy(title -> assertThat(title).doesNotStartWith("SWT_"));
        assertThat(titles).contains("Preferences...", "Services", "Hide Others", "Show All");
        assertThat(titles.get(0)).startsWith("About ");
    }
}

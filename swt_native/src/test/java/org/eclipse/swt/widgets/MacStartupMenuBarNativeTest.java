package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSString;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * On macOS nothing native is on the way to the first frame: SWT's Cocoa library loads on a thread of
 * its own while the Display is created, and the application menu is installed by the first pass of
 * the event loop. The NSApp main menu is process-wide and cannot be taken back out, so each scenario
 * runs in a child JVM whose NSApp has never had a menu; {@code -XstartOnFirstThread} makes its main
 * thread the Display thread, as in an application.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacStartupMenuBarNativeTest {

    private static final long TIMEOUT_MS = 30_000;

    @Test
    void installsTheApplicationMenuOnTheFirstEventLoopPassNotDuringInit() throws Exception {
        assertThat(runProbe(MenuProbe.class)).as("the child's output")
                .contains("afterInit=false afterFirstPass=true");
    }

    /** In a browser tab the OS menu bar is the browser's: nothing native is built for it. */
    @Test
    void installsNoApplicationMenuForABrowserTab() throws Exception {
        assertThat(runProbe(BrowserTabProbe.class)).as("the child's output")
                .contains("afterFirstPass=false");
    }

    /**
     * The browser surface still opens the OS file and folder choosers, which run NSApp modally. They
     * must not depend on the application menu, which a browser tab no longer builds.
     */
    @Test
    void opensTheNativeFileChooserInABrowserTab() throws Exception {
        assertThat(runProbe(ChooserProbe.class, "-Dprobe.chooser=file")).as("the child's output")
                .contains("menu=false chooser=aborted");
    }

    @Test
    void opensTheNativeFolderChooserInABrowserTab() throws Exception {
        assertThat(runProbe(ChooserProbe.class, "-Dprobe.chooser=directory")).as("the child's output")
                .contains("menu=false chooser=aborted");
    }

    /**
     * A Shell's menu bar can be published before the application menu is in: the workbench sets it
     * while it starts. It has to reach the system bar once the application menu replaces the main menu.
     */
    @Test
    void publishesAShellMenuBarSetBeforeTheApplicationMenuWasInstalled() throws Exception {
        assertThat(runProbe(ShellMenuBarProbe.class)).as("the child's output")
                .contains("beforeFirstPass=[Native] afterFirstPass=[App, File]");
    }

    @Test
    void loadsTheCocoaLibraryOnAThreadOfItsOwn() throws Exception {
        assertThat(runProbe(PreloadProbe.class)).as("the child's output")
                .contains("preloadThread=true usable=true");
    }

    @Test
    void startsNoPreloadWithTheNativeLibraryOff() throws Exception {
        assertThat(runProbe(PreloadProbe.class, "-D" + DisplayBridgePlatform.LOAD_LIBRARY + "=false"))
                .as("the child's output").contains("preloadThread=false");
    }

    /** Runs {@code probe} in a child JVM, and returns everything it printed. */
    private static String runProbe(Class<?> probe, String... jvmArgs) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }

        Path log = Files.createTempFile("startup-menu-bar", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-Djava.awt.headless=true");
            for (String arg : jvmArgs)
                command.add(arg);
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(probe.getName());
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                child.destroyForcibly();

            return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** Whether the main menu carries the application menu, recognized by its About item. */
    static boolean hasApplicationMenu() {
        NSMenu main = NSApplication.sharedApplication().mainMenu();
        if (main == null || main.numberOfItems() == 0)
            return false;
        NSMenu appMenu = main.itemAtIndex(0).submenu();
        if (appMenu == null)
            return false;
        for (int i = 0; i < appMenu.numberOfItems(); i++) {
            if (appMenu.itemAtIndex(i).tag() == SWT.ID_ABOUT)
                return true;
        }
        return false;
    }

    /** The titles of the main menu's top-level items. */
    static List<String> topLevelTitles() {
        List<String> titles = new ArrayList<>();
        NSMenu main = NSApplication.sharedApplication().mainMenu();
        if (main == null)
            return titles;
        for (int i = 0; i < main.numberOfItems(); i++)
            titles.add(main.itemAtIndex(i).title().getString());
        return titles;
    }

    /** Creates the desktop surface's platform state, then runs one event-loop pass. */
    public static final class MenuProbe {

        public static void main(String[] args) {
            ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
            DisplayBridgePlatform.preload();
            DisplayBridgePlatform.init();
            boolean afterInit = hasApplicationMenu();
            DisplayBridgePlatform.syncMenuBar();
            System.out.println("afterInit=" + afterInit + " afterFirstPass=" + hasApplicationMenu());
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /** Creates the browser surface's platform state, then runs one event-loop pass. */
    public static final class BrowserTabProbe {

        public static void main(String[] args) {
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
            DisplayBridgePlatform.preload();
            DisplayBridgePlatform.init();
            DisplayBridgePlatform.syncMenuBar();
            System.out.println("afterFirstPass=" + hasApplicationMenu());
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /**
     * Opens a FileDialog or a DirectoryDialog ({@code -Dprobe.chooser}) the way a browser-surface app
     * does, after the platform state of a browser tab and one event-loop pass. It is aborted from
     * another thread, which {@code -[NSApplication abortModal]} allows, so a chooser that came up
     * returns null; one that returns before any abort never ran modally. One chooser per JVM: an
     * abort sent while no modal session runs ends the next one.
     */
    public static final class ChooserProbe {

        public static void main(String[] args) throws Exception {
            System.clearProperty(ConfigFlags.MODE_PROPERTY);
            System.setProperty("dev.equo.swt.desktop", "false");
            FlutterBridge.set(new RecordingBridge());
            Display display = new Display();
            DisplayBridgePlatform.preload();
            DisplayBridgePlatform.init();
            DisplayBridgePlatform.syncMenuBar();
            Shell shell = new Shell(display);
            String chooser = "file".equals(System.getProperty("probe.chooser"))
                    ? open(() -> new FileDialog(shell, SWT.OPEN).open())
                    : open(() -> new DirectoryDialog(shell).open());
            System.out.println("menu=" + hasApplicationMenu() + " chooser=" + chooser);
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }

        /** Opens the chooser, aborts it until it returns, and reports how it returned. */
        private static String open(java.util.function.Supplier<String> chooser) throws Exception {
            java.util.concurrent.atomic.AtomicBoolean returned = new java.util.concurrent.atomic.AtomicBoolean();
            java.util.concurrent.atomic.AtomicInteger aborts = new java.util.concurrent.atomic.AtomicInteger();
            long app = NSApplication.sharedApplication().id;
            long abortModal = org.eclipse.swt.internal.cocoa.OS.sel_registerName("abortModal");
            Thread abort = new Thread(() -> {
                while (!returned.get()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return;
                    }
                    aborts.incrementAndGet();
                    org.eclipse.swt.internal.cocoa.OS.objc_msgSend(app, abortModal);
                }
            });
            abort.setDaemon(true);
            abort.start();
            String picked = chooser.get();
            returned.set(true);
            if (aborts.get() == 0)
                return "returned before it was aborted";
            return picked == null ? "aborted" : "picked " + picked;
        }
    }

    /**
     * Publishes a Shell's menu bar between Display creation and the first event-loop pass, over the
     * stand-in main menu the native window installs when it finds none.
     */
    public static final class ShellMenuBarProbe {

        public static void main(String[] args) {
            NSMenu standIn = (NSMenu) new NSMenu().alloc();
            standIn.initWithTitle(NSString.string());
            standIn.addItemWithTitle(NSString.stringWith("Native"), 0, NSString.string());
            NSApplication.sharedApplication().setMainMenu(standIn);
            System.setProperty("dev.equo.swt.systemMenuBar", "true");
            DartDisplay.APP_NAME = "App";
            FlutterBridge.set(new RecordingBridge());
            Display display = new Display();
            DisplayBridgePlatform.init();
            Shell shell = new Shell(display);
            Menu bar = new Menu(shell, SWT.BAR);
            MenuItem file = new MenuItem(bar, SWT.CASCADE);
            file.setText("File");
            file.setMenu(new Menu(shell, SWT.DROP_DOWN));
            DisplayBridgePlatform.setMenuBar(bar);
            List<String> before = topLevelTitles();
            DisplayBridgePlatform.syncMenuBar();
            System.out.println("beforeFirstPass=" + before + " afterFirstPass=" + topLevelTitles());
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }

    /** Starts the preload and reports the thread it ran on, and whether Cocoa answers afterwards. */
    public static final class PreloadProbe {

        public static void main(String[] args) throws Exception {
            DisplayBridgePlatform.preload();
            Thread preload = null;
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().equals("SWT Cocoa preload"))
                    preload = thread;
            }
            if (preload == null) {
                System.out.println("preloadThread=false");
            } else {
                preload.join(TIMEOUT_MS);
                boolean usable = NSApplication.sharedApplication() != null;
                System.out.println("preloadThread=true usable=" + usable);
            }
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }
    }
}

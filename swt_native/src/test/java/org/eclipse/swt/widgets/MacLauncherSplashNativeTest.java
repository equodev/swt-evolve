package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSDate;
import org.eclipse.swt.internal.cocoa.NSRect;
import org.eclipse.swt.internal.cocoa.NSRunLoop;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * The Equinox launcher shows its splash in a window of the main thread and publishes its handle in
 * {@value #SPLASH_HANDLE}. A Display rendering in a browser runs no Cocoa event loop on that thread,
 * so the splash would stay on screen until the process quit.
 *
 * <p>Only the main thread may order a window out, and the test worker is not it, so each scenario
 * runs in a child JVM started with {@code -XstartOnFirstThread}. The child asks the window server
 * whether the window is on screen: AppKit reports it hidden as soon as it is ordered out, even when
 * the window server never drops it.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacLauncherSplashNativeTest {

    private static final String SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle";

    private static final long TIMEOUT_MS = 30_000;

    @Test
    void takesTheLaunchersSplashOffScreenFromTheMainThread() throws Exception {
        assertThat(runProbe("main")).as("the child's output").contains("dismissed=true onscreen=false");
    }

    @Test
    void leavesTheSplashOnScreenOffTheMainThread() throws Exception {
        assertThat(runProbe("off-main")).as("the child's output").contains("dismissed=false onscreen=true");
    }

    /** Runs {@link SplashProbe} in a child JVM, and returns everything it printed. */
    private static String runProbe(String thread) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }

        Path log = Files.createTempFile("launcher-splash", ".log");
        try {
            List<String> command = new ArrayList<>();
            command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            command.add("-XstartOnFirstThread");
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(SplashProbe.class.getName());
            command.add(thread);
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS))
                child.destroyForcibly();

            return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        } finally {
            Files.deleteIfExists(log);
        }
    }

    /** Shows a window the way the launcher does, then dismisses it from the thread named in {@code args[0]}. */
    public static final class SplashProbe {

        public static void main(String[] args) throws Exception {
            NSApplication.sharedApplication();
            NSRect bounds = new NSRect();
            bounds.width = 120;
            bounds.height = 80;
            NSWindow window = ((NSWindow) new NSWindow().alloc())
                    .initWithContentRect(bounds, OS.NSWindowStyleMaskBorderless, OS.NSBackingStoreBuffered, false);
            window.orderFront(null);
            // The launcher pumps its splash onto the screen while the framework starts.
            NSRunLoop.currentRunLoop().runMode(OS.NSDefaultRunLoopMode, NSDate.dateWithTimeIntervalSinceNow(0.2));
            long number = window.windowNumber();
            if (!onScreen(number)) {
                System.out.println("the window never reached the screen");
                Runtime.getRuntime().halt(0);
            }
            System.setProperty(SPLASH_HANDLE, Long.toString(window.id));

            boolean[] dismissed = new boolean[1];
            if ("main".equals(args[0])) {
                dismissed[0] = DisplayBridgePlatform.dismissLauncherSplash();
            } else {
                Thread other = new Thread(() -> dismissed[0] = DisplayBridgePlatform.dismissLauncherSplash());
                other.start();
                other.join();
            }
            Thread.sleep(1000);
            System.out.println("dismissed=" + dismissed[0] + " onscreen=" + onScreen(number));
            System.out.flush();
            Runtime.getRuntime().halt(0);
        }

        /** What the window server, not AppKit, says about the window. */
        private static boolean onScreen(long windowNumber) throws Exception {
            String script = "ObjC.import('CoreGraphics');"
                    + "var info = ObjC.castRefToObject($.CGWindowListCopyWindowInfo($.kCGWindowListOptionAll, $.kCGNullWindowID));"
                    + "var on = false;"
                    + "for (var i = 0; i < info.count; i++) {"
                    + "  var w = info.objectAtIndex(i);"
                    + "  if (w.objectForKey('kCGWindowNumber').intValue == " + windowNumber + ")"
                    + "    on = !!(w.objectForKey('kCGWindowIsOnscreen') && w.objectForKey('kCGWindowIsOnscreen').boolValue);"
                    + "}"
                    + "on";
            Process query = new ProcessBuilder("osascript", "-l", "JavaScript", "-e", script).redirectErrorStream(true).start();
            byte[] out = new byte[64];
            int read = query.getInputStream().read(out);
            query.waitFor();
            return read > 0 && new String(out, 0, read, StandardCharsets.UTF_8).trim().equals("true");
        }
    }
}

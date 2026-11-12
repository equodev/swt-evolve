package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSArray;
import org.eclipse.swt.internal.cocoa.NSEvent;
import org.eclipse.swt.internal.cocoa.NSRect;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSView;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * AppKit sends a view controller viewDidLoad and viewWillAppear only when the host executable was
 * linked against the macOS 10.10 SDK or later. Java 8's launcher is built against 10.8, so a desktop
 * window hosted by it must still get a running engine and a vsync source. The probes run in desktop
 * mode under a copy of this JVM's launcher whose load command names an older SDK.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.MAC)
public class MacOldSdkHostNativeTest {

    private static final String OPENED = "first frame fills the window at its requested size";

    private static final String FOLLOWED = "frame repainted at the native resize";

    private static final String IN_CHAIN = "view controller is its view's next responder";

    private static final String SURVIVED = "key equivalents dispatched";

    @Test
    void aDesktopWindowUnderAnOldSdkHostPaintsAtItsSizeAndRepaintsOnResize() throws Exception {
        String output = runUnderOldSdkHost(ResizeProbe.class);
        assertThat(output).as("the child's output").contains(OPENED).contains(FOLLOWED);
    }

    @Test
    void aCommandKeyDownUnderAnOldSdkHostReachesTheEngineAndNeverRecurses() throws Exception {
        String output = runUnderOldSdkHost(KeyEquivalentProbe.class);
        assertThat(output).as("the child's output").contains(IN_CHAIN).contains(SURVIVED);
    }

    private static String runUnderOldSdkHost(Class<?> probe) throws Exception {
        return runUnderOldSdkHost(probe, "10.9");
    }

    /** Runs {@code probe} in desktop mode under {@link #oldSdkLauncher} for {@code sdk} and returns its output. */
    static String runUnderOldSdkHost(Class<?> probe, String sdk) throws Exception {
        try {
            NSApplication.sharedApplication();
        } catch (Throwable notAvailable) {
            Assumptions.abort("no native SWT library on this runner: " + notAvailable);
        }
        Path home = Files.createTempDirectory("old-sdk-jdk");
        try {
            List<String> command = new ArrayList<>();
            command.add(oldSdkLauncher(home, sdk).toString());
            command.add("-XstartOnFirstThread");
            command.add("-Ddev.equo.swt.mode=desktop");
            command.add("-Dequo.swt.browser=none");
            command.add("-Ddev.equo.swt.crashReport.disabled=true");
            String libraryPath = System.getProperty("swt.library.path");
            if (libraryPath != null)
                command.add("-Dswt.library.path=" + libraryPath);
            command.add("-cp");
            command.add(System.getProperty("java.class.path"));
            command.add(probe.getName());
            Path log = home.resolve("child.log");
            Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!child.waitFor(MacChildJvm.TIMEOUT_MS * 2 + 30_000, TimeUnit.MILLISECONDS))
                child.destroyForcibly().waitFor();
            return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
        } finally {
            deleteTree(home);
        }
    }

    /** A copy of this JVM's launcher stamped as built against {@code sdk}, beside links to the rest of the JDK. */
    private static Path oldSdkLauncher(Path home, String sdk) throws Exception {
        Path jdk = Paths.get(System.getProperty("java.home"));
        File[] entries = jdk.toFile().listFiles();
        if (entries != null)
            for (File entry : entries)
                if (!entry.getName().equals("bin"))
                    Files.createSymbolicLink(home.resolve(entry.getName()), entry.toPath());
        Path java = Files.createDirectories(home.resolve("bin")).resolve("java");
        int stamped = new ProcessBuilder("xcrun", "vtool", "-set-build-version", "macos", "11.0", sdk, "-replace",
                "-output", java.toString(), jdk.resolve("bin/java").toString()).inheritIO().start().waitFor();
        Assumptions.assumeTrue(stamped == 0, "vtool is not available");
        int signed = new ProcessBuilder("codesign", "-s", "-", "-f", java.toString()).inheritIO().start().waitFor();
        Assumptions.assumeTrue(signed == 0, "codesign is not available");
        return java;
    }

    private static void deleteTree(Path root) throws Exception {
        try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        }
    }

    /** The probe's desktop window: the only visible one. */
    static NSWindow window() {
        NSArray windows = NSApplication.sharedApplication().windows();
        for (int i = 0; i < windows.count(); i++) {
            NSWindow window = new NSWindow(windows.objectAtIndex(i));
            if (window.isVisible())
                return window;
        }
        return null;
    }

    /**
     * Opens a desktop shell, then resizes its window natively. Each time, the frame the engine last
     * presented must cover the whole window: a button filling the shell paints the same opaque color
     * near its top-left and its bottom-right corner.
     */
    public static final class ResizeProbe {

        public static void main(String[] args) {
            Display display = new Display();
            Shell shell = new Shell(display);
            shell.setLayout(new FillLayout());
            new Button(shell, SWT.PUSH).setText("old SDK host");
            shell.setSize(400, 300);
            shell.open();
            if (!MacChildJvm.spinUntil(display, () -> window() != null))
                MacChildJvm.exitWith("no window");
            NSWindow window = window();
            if (!MacChildJvm.spinUntil(display, () -> fillsWindow(window)))
                MacChildJvm.exitWith("first frame: " + presented(window) + ", shell " + shell.getSize());
            System.out.println(OPENED);

            NSRect frame = window.frame();
            frame.width = 640;
            frame.height = 480;
            window.setFrame(frame, true);
            MacChildJvm.exitWith(MacChildJvm.spinUntil(display, () -> shell.getSize().x == 640 && fillsWindow(window))
                    ? FOLLOWED : "after the resize: " + presented(window) + ", shell " + shell.getSize());
        }

        private static boolean fillsWindow(NSWindow window) {
            long[] sample = sample(window);
            double scale = OS.objc_msgSend_fpret(window.id, OS.sel_registerName("backingScaleFactor"));
            return sample != null && sample[0] == Math.round(window.frame().width * scale) && sample[2] != 0
                    && sample[2] == sample[3];
        }

        private static String presented(NSWindow window) {
            long[] s = sample(window);
            return s == null ? "no frame" : String.format("frame %dx%d, pixels %x %x", s[0], s[1], s[2], s[3]);
        }

        /**
         * Width and height of the presented IOSurface, and its pixels at 25%/30% and 85%/75%; a
         * transparent pixel reads 0 whatever the surface's pixel format.
         */
        private static long[] sample(NSWindow window) {
            long surface = presentedSurface(window);
            if (surface == 0)
                return null;
            long width = OS.objc_msgSend(surface, OS.sel_registerName("width"));
            long height = OS.objc_msgSend(surface, OS.sel_registerName("height"));
            long rowBytes = OS.objc_msgSend(surface, OS.sel_registerName("bytesPerRow"));
            int pixelBytes = (int) Math.min(8, OS.objc_msgSend(surface, OS.sel_registerName("bytesPerElement")));
            long readOnly = 1;
            OS.objc_msgSend(surface, OS.sel_registerName("lockWithOptions:seed:"), readOnly, 0);
            try {
                long base = OS.objc_msgSend(surface, OS.sel_registerName("baseAddress"));
                return new long[] { width, height,
                        pixel(base + height * 30 / 100 * rowBytes + width * 25 / 100 * pixelBytes, pixelBytes),
                        pixel(base + height * 75 / 100 * rowBytes + width * 85 / 100 * pixelBytes, pixelBytes) };
            } finally {
                OS.objc_msgSend(surface, OS.sel_registerName("unlockWithOptions:seed:"), readOnly, 0);
            }
        }

        private static long pixel(long address, int pixelBytes) {
            byte[] bytes = new byte[pixelBytes];
            C.memmove(bytes, address, pixelBytes);
            long value = 0;
            for (byte b : bytes)
                value = value << 8 | (b & 0xff);
            return value;
        }

        /** The IOSurface of the FlutterView's content layer, or 0 before the engine presents a frame. */
        private static long presentedSurface(NSWindow window) {
            NSArray subviews = window.contentView().subviews();
            for (int i = 0; i < subviews.count(); i++) {
                NSView view = new NSView(subviews.objectAtIndex(i));
                if (!view.className().getString().equals("FlutterView"))
                    continue;
                long sublayers = OS.objc_msgSend(OS.objc_msgSend(view.id, OS.sel_registerName("layer")),
                        OS.sel_registerName("sublayers"));
                if (sublayers == 0 || new NSArray(sublayers).count() == 0)
                    return 0;
                long contents = OS.objc_msgSend(new NSArray(sublayers).objectAtIndex(0).id, OS.sel_registerName("contents"));
                return contents != 0 && new NSView(contents).className().getString().contains("IOSurface") ? contents : 0;
            }
            return 0;
        }
    }

    /**
     * Opens a desktop shell, checks that its view controller gets the view's key events, shuts the
     * engine down so it answers a key event at once, and sends the window a Command key-down nothing
     * handles: once as is, once with the controller out of the responder chain, where the Flutter view
     * hands the event straight back to the window. Neither may recurse.
     */
    public static final class KeyEquivalentProbe {

        public static void main(String[] args) throws Exception {
            Display display = new Display();
            Shell shell = new Shell(display);
            shell.setSize(400, 300);
            shell.open();
            if (!MacChildJvm.spinUntil(display, () -> window() != null))
                MacChildJvm.exitWith("no window");
            NSWindow window = window();
            long nextResponder = OS.sel_registerName("nextResponder");
            long controller = OS.objc_msgSend(window.id, OS.sel_registerName("contentViewController"));
            long view = window.contentView().id;
            System.out.println(OS.objc_msgSend(view, nextResponder) == controller ? IN_CHAIN
                    : "the view's next responder is not its controller");
            OS.objc_msgSend(OS.objc_msgSend(controller, OS.sel_registerName("engine")), OS.sel_registerName("shutDownEngine"));
            window.makeKeyAndOrderFront(null);
            NSEvent cmdJ = commandKeyDown(window);
            if (cmdJ.keyCode() != 38 || (cmdJ.modifierFlags() & (1L << 20)) == 0)
                MacChildJvm.exitWith("the key-down is not Cmd-J");
            NSApplication.sharedApplication().sendEvent(cmdJ);
            OS.objc_msgSend(view, OS.sel_registerName("setNextResponder:"), window.id);
            // Where a Command key-down nothing handled comes back up the responder chain.
            OS.objc_msgSend(window.id, OS.sel_registerName("keyDown:"), commandKeyDown(window).id);
            MacChildJvm.exitWith(SURVIVED);
        }

        /**
         * Cmd-J. The factory takes ten arguments, more than any objc_msgSend overload of this shape,
         * so it is called through an NSInvocation, which passes each one as its signature says.
         */
        private static NSEvent commandKeyDown(NSWindow window) {
            NSString j = NSString.stringWith("j");
            long factory = OS.sel_registerName("keyEventWithType:location:modifierFlags:timestamp:windowNumber:"
                    + "context:characters:charactersIgnoringModifiers:isARepeat:keyCode:");
            long signature = OS.objc_msgSend(OS.class_NSEvent, OS.sel_registerName("methodSignatureForSelector:"), factory);
            long invocation = OS.objc_msgSend(OS.objc_getClass("NSInvocation"),
                    OS.sel_registerName("invocationWithMethodSignature:"), signature);
            OS.objc_msgSend(invocation, OS.sel_registerName("setTarget:"), OS.class_NSEvent);
            OS.objc_msgSend(invocation, OS.sel_registerName("setSelector:"), factory);
            long value = C.malloc(16);
            try {
                setArgument(invocation, value, 2, 10 /* NSEventTypeKeyDown */);
                C.memmove(value, new double[] {0, 0}, 16);
                OS.objc_msgSend(invocation, OS.sel_registerName("setArgument:atIndex:"), value, 3);
                setArgument(invocation, value, 4, 1L << 20 /* NSEventModifierFlagCommand */);
                C.memmove(value, new double[] {0}, 8);
                OS.objc_msgSend(invocation, OS.sel_registerName("setArgument:atIndex:"), value, 5);
                setArgument(invocation, value, 6, window.windowNumber());
                setArgument(invocation, value, 7, 0);
                setArgument(invocation, value, 8, j.id);
                setArgument(invocation, value, 9, j.id);
                setArgument(invocation, value, 10, 0 /* isARepeat */);
                setArgument(invocation, value, 11, 38 /* kVK_ANSI_J */);
                OS.objc_msgSend(invocation, OS.sel_registerName("invoke"));
                OS.objc_msgSend(invocation, OS.sel_registerName("getReturnValue:"), value);
                long[] keyDown = new long[1];
                C.memmove(keyDown, value, 8);
                return new NSEvent(keyDown[0]);
            } finally {
                C.free(value);
            }
        }

        /** Sets an integer or object argument; NSInvocation copies as many bytes as the argument's type takes. */
        private static void setArgument(long invocation, long value, int index, long argument) {
            C.memmove(value, new long[] {argument}, 8);
            OS.objc_msgSend(invocation, OS.sel_registerName("setArgument:atIndex:"), value, index);
        }
    }
}

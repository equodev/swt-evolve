package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ServerSocket;

import org.eclipse.swt.internal.win32.MONITORINFO;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.RECT;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * The bounds Java gives a window are its content bounds. A frameless (CSD) window has no OS frame
 * around its content, so moving it to those bounds must not add one: the content would land a
 * caption and a border away from where the shell says it is, and every origin read back from the
 * window would carry that offset into the shell's bounds.
 *
 * <p>The first window of the process owns the application's exit and posts {@code WM_QUIT} when
 * destroyed; the test drains that from this thread's queue so a later test is not handed it.
 */
@Tag("native-unit")
@EnabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
public class Win32WindowBoundsNativeTest {

    @ParameterizedTest(name = "csd={0}")
    @ValueSource(strings = {"overlay", "false"})
    void theContentStartsWhereTheWindowWasMoved(String csd) throws Exception {
        withWindow(csd, context -> {
            long origin = moveContentTo(context, 320, 240);
            assertThat(new int[] {FlutterNative.originX(origin), FlutterNative.originY(origin)})
                    .as("content origin").containsExactly(320, 240);
        });
    }

    /**
     * Native SWT puts a shell's title bar at the y it is given. A framed window's caption sits
     * above its content, so content placed at the top of the screen moves down to keep it there.
     */
    @Test
    void aFramedWindowAtTheTopOfTheScreenKeepsItsWholeCaptionOnIt() throws Exception {
        withWindow("false", context -> {
            int top = FlutterNative.getWorkAreaTop(context, 320, 0, 400, 300);
            assertThat(top).as("work area top").isNotEqualTo(FlutterNative.WORK_AREA_UNKNOWN);

            long moved = moveContentTo(context, 320, top);
            int below = FlutterNative.originY(moved);
            assertThat(FlutterNative.originX(moved)).isEqualTo(320);
            assertThat(below).as("content below the caption").isGreaterThan(top);
            assertThat(FlutterNative.originY(moveContentTo(context, 320, below)))
                    .as("placed where it reads back, it stays").isEqualTo(below);
            assertThat(FlutterNative.originY(moveContentTo(context, 320, below - 1)))
                    .as("a pixel higher puts the caption's top above the work area, so it comes back: "
                            + "the caption starts exactly at the top of the work area")
                    .isEqualTo(below);
        });
    }

    /**
     * Win32 SWT's full screen is a window with no title bar or borders over the whole monitor,
     * taskbar included, where a maximized window only gets the work area. Leaving it puts the
     * frame and the window's place back.
     */
    @ParameterizedTest(name = "csd={0}")
    @ValueSource(strings = {"overlay", "false"})
    void fullScreenCoversTheMonitorAndLeavingItPutsTheWindowBack(String csd) throws Exception {
        withWindow(csd, context -> {
            long hwnd = FlutterNative.getView(context);
            moveContentTo(context, 320, 240);
            int[] placed = windowRect(hwnd);
            int frame = frameStyle(hwnd);
            assertThat(frame).as("frame style of a placed window").isNotZero();

            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);
            assertThat(windowRect(hwnd)).as("full-screen window").containsExactly(monitorRect(hwnd));
            assertThat(frameStyle(hwnd)).as("frame style in full screen").isZero();

            FlutterNative.setState(context, ShellWindow.STATE_NORMAL);
            assertThat(windowRect(hwnd)).as("window after full screen").containsExactly(placed);
            assertThat(frameStyle(hwnd)).as("frame style after full screen").isEqualTo(frame);
        });
    }

    @Test
    void aWindowMaximizedBeforeFullScreenIsMaximizedAfterIt() throws Exception {
        withWindow("overlay", context -> {
            long hwnd = FlutterNative.getView(context);
            FlutterNative.setState(context, ShellWindow.STATE_MAXIMIZED);
            int[] maximized = windowRect(hwnd);

            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);
            assertThat(windowRect(hwnd)).as("full-screen window").containsExactly(monitorRect(hwnd));
            assertThat(OS.IsZoomed(hwnd)).as("maximized in full screen").isFalse();

            FlutterNative.setState(context, ShellWindow.STATE_NORMAL);
            assertThat(OS.IsZoomed(hwnd)).as("maximized after full screen").isTrue();
            assertThat(windowRect(hwnd)).as("window after full screen").containsExactly(maximized);
        });
    }

    @Test
    void aMinimizedFullScreenWindowComesBackFullScreen() throws Exception {
        withWindow("overlay", context -> {
            long hwnd = FlutterNative.getView(context);
            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);
            FlutterNative.setState(context, ShellWindow.STATE_MINIMIZED);
            assertThat(OS.IsIconic(hwnd)).as("minimized").isTrue();

            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);
            assertThat(OS.IsIconic(hwnd)).as("minimized after un-minimizing").isFalse();
            assertThat(windowRect(hwnd)).as("window after un-minimizing").containsExactly(monitorRect(hwnd));
            assertThat(frameStyle(hwnd)).as("frame style after un-minimizing").isZero();
        });
    }

    @Test
    void aMinimizedFullScreenWindowLeavesFullScreen() throws Exception {
        withWindow("overlay", context -> {
            long hwnd = FlutterNative.getView(context);
            moveContentTo(context, 320, 240);
            int[] placed = windowRect(hwnd);
            int frame = frameStyle(hwnd);
            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);
            FlutterNative.setState(context, ShellWindow.STATE_MINIMIZED);

            FlutterNative.setState(context, ShellWindow.STATE_NORMAL);
            assertThat(OS.IsIconic(hwnd)).as("minimized after leaving full screen").isFalse();
            assertThat(windowRect(hwnd)).as("window after leaving full screen").containsExactly(placed);
            assertThat(frameStyle(hwnd)).as("frame style after leaving full screen").isEqualTo(frame);
        });
    }

    @Test
    void maximizingAFullScreenWindowLeavesFullScreen() throws Exception {
        withWindow("overlay", context -> {
            long hwnd = FlutterNative.getView(context);
            int frame = frameStyle(hwnd);
            FlutterNative.setState(context, ShellWindow.STATE_FULLSCREEN);

            FlutterNative.setState(context, ShellWindow.STATE_MAXIMIZED);
            assertThat(OS.IsZoomed(hwnd)).as("maximized").isTrue();
            assertThat(frameStyle(hwnd)).as("frame style when maximized").isEqualTo(frame);
        });
    }

    private static int[] windowRect(long hwnd) {
        RECT rect = new RECT();
        assertThat(OS.GetWindowRect(hwnd, rect)).as("GetWindowRect").isTrue();
        return new int[] {rect.left, rect.top, rect.right, rect.bottom};
    }

    private static int[] monitorRect(long hwnd) {
        MONITORINFO info = new MONITORINFO();
        info.cbSize = MONITORINFO.sizeof;
        assertThat(OS.GetMonitorInfo(OS.MonitorFromWindow(hwnd, OS.MONITOR_DEFAULTTONEAREST), info))
                .as("GetMonitorInfo").isTrue();
        return new int[] {info.rcMonitor_left, info.rcMonitor_top, info.rcMonitor_right, info.rcMonitor_bottom};
    }

    private static int frameStyle(long hwnd) {
        return OS.GetWindowLong(hwnd, OS.GWL_STYLE) & (OS.WS_CAPTION | OS.WS_THICKFRAME);
    }

    private interface WindowCheck {
        void run(long context) throws Exception;
    }

    private static long moveContentTo(long context, int x, int y) {
        FlutterNative.setBounds(context, x, y, 400, 300, x, y, 400, 300);
        return FlutterNative.getOrigin(context);
    }

    private static void withWindow(String csd, WindowCheck check) throws Exception {
        try {
            FlutterLibraryLoader.initialize();
        } catch (FlutterLibraryLoader.LibraryLoaderException e) {
            Assumptions.abort("no Windows runner built here: " + e.getMessage());
        }
        ConfigFlags saved = Config.getConfigFlags();
        ConfigFlags flags = new ConfigFlags();
        flags.csd_placement = csd;
        Config.setConfigFlags(flags);
        // No messages are pumped, so the engine never presents a frame and the window is never shown.
        try (ServerSocket nobody = new ServerSocket(0)) {
            long context = FlutterNative.initialize(nobody.getLocalPort(), 0, 1, "Shell", "light", 0, 0, 400, 300);
            assertThat(context).as("window created").isNotZero();
            try {
                check.run(context);
            } finally {
                FlutterNative.dispose(context);
                FlutterNative.pumpMessages(Integer.MAX_VALUE);
            }
        } finally {
            Config.setConfigFlags(saved);
        }
    }
}

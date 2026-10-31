package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterNative;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;

/**
 * Per-OS native init shared by the two Display surfaces on Windows — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Initializes
 * OLE/COM for the Display thread.
 */
final class DisplayBridgePlatform {

    static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private DisplayBridgePlatform() {
    }

    /** Nothing to load ahead of {@link #init}: it is cheap here. */
    static void preload() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the native call below would throw UnsatisfiedLinkError; pure-web/browser needs no OLE init anyway.
        if (nativeLibraryOff())
            return;
        org.eclipse.swt.internal.win32.OS.OleInitialize(0);
    }

    /**
     * The system browser is opened through {@code ShellExecuteEx} ({@code Program.launch}), which wants
     * COM initialized on the calling thread. {@link #init} initializing it again is harmless.
     */
    static void beforeBrowserLaunch() {
        if (nativeLibraryOff())
            return;
        org.eclipse.swt.internal.win32.OS.OleInitialize(0);
    }

    /** Nothing {@link #init()} set up outlives the Display on this platform. */
    static void release() {
    }

    /** No application menu outside macOS: the platform has no such concept. */
    static Menu systemMenu(Display display) {
        return null;
    }

    /** The menu bar belongs in the window on this platform; only macOS has an OS-owned one. */
    static void setMenuBar(Menu menu) {
    }

    /** Nothing to keep in step: the menu bar is drawn inside the window on this platform. */
    static void syncMenuBar() {
    }

    /** Nothing to bind: a Shell here is drawn by Flutter and has no native view of its own. */
    static void bindWindowView(Control control, long view) {
    }

    /**
     * Destroys the launcher's splash window. It belongs to the Display thread, which never pumps
     * window messages while the Display renders in a browser, so Windows would report the window,
     * and the process with it, as not responding. Only the owning thread can destroy a window.
     *
     * @return whether the window was destroyed
     */
    static boolean dismissLauncherSplash() {
        long hwnd = Long.getLong("org.eclipse.equinox.launcher.splash.handle", 0);
        if (hwnd == 0 || nativeLibraryOff() || OS.GetWindowThreadProcessId(hwnd, new int[1]) != OS.GetCurrentThreadId())
            return false;
        return OS.DestroyWindow(hwnd);
    }

    /** Height of the Windows 11 taskbar at 100%: Evolve's Display has no work area to read it from. */
    private static final int TASKBAR_HEIGHT = 48;

    /** What Win32 takes off the work area height before sizing a default window. */
    private static final int DEFAULT_HEIGHT_INSET = 28;

    /**
     * How much smaller than a top-level window a default dialog is: it starts 100 pixels right of and
     * below its parent and ends at the next cascade slot, 26 pixels on, when the parent sits at its own
     * default slot.
     */
    private static final int DIALOG_CASCADE_INSET = 100 - 26;

    /**
     * The size native SWT creates a shell at when the application gives it none, which Win32 leaves to
     * the system: 3/4 of the work area wide and 3/4 of the work area less {@link #DEFAULT_HEIGHT_INSET}
     * high, the work area being the screen above a {@link #TASKBAR_HEIGHT} taskbar. A dialog is
     * {@link #DIALOG_CASCADE_INSET} smaller each way.
     */
    static Point defaultShellSize(Rectangle screen, DartShell shell) {
        int width = (int) Math.round(screen.width * 0.75);
        int height = (int) Math.round((screen.height - TASKBAR_HEIGHT - DEFAULT_HEIGHT_INSET) * 0.75);
        return shell.parent != null ?new Point(width - DIALOG_CASCADE_INSET, height - DIALOG_CASCADE_INSET) : new Point(width, height);
    }

    /**
     * Where the OS pointer is, read by the runner in the per-monitor logical pixels it places windows
     * in; null without the native library.
     */
    static Point cursorLocation() {
        if (nativeLibraryOff())
            return null;
        long packed = FlutterNative.getCursorLocation();
        if (packed == FlutterNative.CURSOR_UNKNOWN)
            return null;
        return new Point(FlutterNative.originX(packed), FlutterNative.originY(packed));
    }

    /** Moves the OS pointer to a point in {@link #cursorLocation}'s units; whether it moved. */
    static boolean moveCursor(int x, int y) {
        return !nativeLibraryOff() && FlutterNative.setCursorLocation(x, y);
    }

    /** Whether a mouse button is held; false without the native library. */
    static boolean pointerButtonDown() {
        if (nativeLibraryOff())
            return false;
        return OS.GetKeyState(OS.VK_LBUTTON) < 0 || OS.GetKeyState(OS.VK_RBUTTON) < 0 || OS.GetKeyState(OS.VK_MBUTTON) < 0;
    }

    /** Whether the native SWT library is switched off ({@code -Ddev.equo.swt.loadLibrary=false}). */
    private static boolean nativeLibraryOff() {
        return "false".equals(System.getProperty(LOAD_LIBRARY));
    }
}

package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Per-OS native init shared by the two Display surfaces on Linux — the web one ({@link WebDisplayBridge}) and the desktop-native one ({@link DeskDisplayBridge}). Initializes
 * GTK (and its SWT lock functions) once for the Display thread.
 */
final class DisplayBridgePlatform {

    static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private DisplayBridgePlatform() {
    }

    static void init() {
        // When the native SWT library isn't loaded (headless web/test, e.g. -Ddev.equo.swt.loadLibrary=false)
        // the GTK calls below would throw UnsatisfiedLinkError; pure-web/browser needs no GTK init anyway.
        if (nativeLibraryOff())
            return;
        if (!GTKWrapper.IS_GTK4) {
            GTKWrapper.setupThreadLocking();
        }
        boolean init;
        if (GTKWrapper.IS_GTK4) {
            init = GTKWrapper.gtk_init_check();
        } else {
            init = GTKWrapper.gtk_init_check(new long[] { 0 }, null);
        }
        if (!init)
            SWT.error(SWT.ERROR_NO_HANDLES, null, " [gtk_init_check() failed]");
    }

    /** Loads the native library the system browser is opened through ({@code Program.launch}, GIO). */
    static void beforeBrowserLaunch() {
        if (nativeLibraryOff())
            return;
        try {
            Class.forName("org.eclipse.swt.internal.gtk.OS", true, DisplayBridgePlatform.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
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
     * Leaves the launcher's splash window to the launcher: not handled on GTK yet.
     *
     * @return false, nothing is dismissed
     */
    static boolean dismissLauncherSplash() {
        return false;
    }

    /**
     * The size native SWT creates a shell at when the application gives it none: GTK makes the client
     * area of every shell, dialog or not, 5/8 of the monitor each way, truncated, and its bounds add
     * the trim of {@link #defaultTrim}.
     */
    static Point defaultShellSize(Rectangle screen, DartShell shell) {
        Point trim = defaultTrim(shell.getApi().style);
        return new Point(screen.width * 5 / 8 + trim.x, screen.height * 5 / 8 + trim.y);
    }

    /**
     * The trim GTK SWT adds to a shell's client area before the window manager reports the real one
     * (its {@code Display.trimWidths} and {@code trimHeights}, picked by {@code Shell.trimWidth()}).
     */
    private static Point defaultTrim(int style) {
        int onTopResize = SWT.ON_TOP | SWT.RESIZE;
        if ((style & SWT.NO_TRIM) != 0 || (style & onTopResize) == onTopResize) return new Point(0, 0);
        boolean title = (style & (SWT.MIN | SWT.MAX | SWT.TITLE | SWT.MENU)) != 0;
        boolean resize = (style & SWT.RESIZE) != 0, border = (style & SWT.BORDER) != 0;
        if (title) return resize ? new Point(6, 29) : border ? new Point(5, 28) : new Point(0, 23);
        return resize ? new Point(6, 6) : border ? new Point(4, 4) : new Point(0, 0);
    }

    /**
     * Where the OS pointer is, in root-window coordinates. Null without the native library, and on
     * GTK 4 or Wayland, which expose no global pointer position.
     */
    static Point cursorLocation() {
        if (!globalPointer())
            return null;
        int[] x = new int[1], y = new int[1];
        GTKWrapper.gdk_window_get_device_position(GTKWrapper.gdk_get_default_root_window(), pointer(), x, y, null);
        return new Point(x[0], y[0]);
    }

    /** Moves the OS pointer to a root-window coordinate; whether it moved (GTK 4 and Wayland cannot warp it). */
    static boolean moveCursor(int x, int y) {
        if (!globalPointer())
            return false;
        GTKWrapper.gdk_device_warp(pointer(), GTKWrapper.gdk_screen_get_default(), x, y);
        return true;
    }

    /** Whether a mouse button is held; false where {@link #cursorLocation} answers null. */
    static boolean pointerButtonDown() {
        if (!globalPointer())
            return false;
        int[] x = new int[1], y = new int[1], mask = new int[1];
        GTKWrapper.gdk_window_get_device_position(GTKWrapper.gdk_get_default_root_window(), pointer(), x, y, mask);
        return (mask[0] & BUTTON_MASKS) != 0;
    }

    /** GDK_BUTTON1_MASK | GDK_BUTTON2_MASK | GDK_BUTTON3_MASK, which move from OS to GDK across SWT releases. */
    private static final int BUTTON_MASKS = 0x100 | 0x200 | 0x400;

    private static long pointer() {
        return GTKWrapper.gdk_get_pointer(GTKWrapper.gdk_display_get_default());
    }

    private static boolean globalPointer() {
        return !nativeLibraryOff() && !GTKWrapper.IS_GTK4 && GTKWrapper.isX11();
    }

    /** Whether the native SWT library is switched off ({@code -Ddev.equo.swt.loadLibrary=false}). */
    private static boolean nativeLibraryOff() {
        return "false".equals(System.getProperty(LOAD_LIBRARY));
    }
}

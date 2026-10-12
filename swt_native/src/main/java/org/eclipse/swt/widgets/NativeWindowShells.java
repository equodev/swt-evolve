package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;

/**
 * The shells made with {@code Shell.internal_new(display, handle)} around a window someone else
 * created: the Eclipse launcher's splash is the one that matters. That window is on screen already,
 * drawn by its owner, so on the desktop such a shell must not be drawn inside the Display's window
 * as well, nor stand in for the application's main shell.
 */
public final class NativeWindowShells {

    private NativeWindowShells() {
    }

    /** Whether {@code shell} wraps a native window that its owner draws. */
    public static boolean wraps(Shell shell) {
        return shell != null && (shell.state & DartWidget.FOREIGN_HANDLE) != 0;
    }

    /**
     * Whether {@code shell} is drawn by the window it wraps rather than inside the Display's window:
     * on the desktop, where that window is on the user's screen. A browser user never sees it, so
     * there the shell is drawn as any other.
     */
    public static boolean drawnByItsOwnWindow(Shell shell) {
        return ConfigFlags.isDesktopMode() && wraps(shell);
    }
}

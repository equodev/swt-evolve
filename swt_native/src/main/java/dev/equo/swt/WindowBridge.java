package dev.equo.swt;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartControl;

/**
 * Optional capability a {@link FlutterBridge} may implement when it is backed by a
 * real OS window — e.g. the Equo Chromium standalone window hosting the Flutter web app.
 * <p>
 * The web bridge ({@code WebDisplayBridge}) implements this so that window operations on
 * the main {@code DartShell} (maximize/minimize/fullscreen/title) can be forwarded to the
 * hosting Chromium window. Bridges that have no OS window (normal browsers, native desktop)
 * simply do not implement it, so the generated shell code is a no-op there.
 * <p>
 * Geometry ({@code setBounds}) is intentionally not part of this interface: it already flows
 * through {@link FlutterBridge#setBounds(DartControl, org.eclipse.swt.graphics.Rectangle)}.
 */
public interface WindowBridge {

    void setWindowMaximized(DartControl control, boolean maximized);

    void setWindowMinimized(DartControl control, boolean minimized);

    void setWindowFullScreen(DartControl control, boolean fullScreen);

    void setWindowTitle(DartControl control, String title);

    /**
     * Whether this shell is the one filling the bridge's own window, rather than a shell drawn
     * inside it with chrome of its own.
     *
     * <p>Asked rather than guessed from geometry: the answer decides whether a coordinate walk adds
     * a title-bar inset, and a shell's bounds cannot tell the two apart once a window is free to sit
     * anywhere on screen.
     */
    default boolean rendersAsMainWindow(Object shell) {
        return false;
    }

    /**
     * Whether this shell is the root of a native window, drawn without a Flutter title bar: the shell
     * filling the bridge's own window, or one in a window of its own.
     */
    default boolean rootsAWindow(Object shell) {
        return rendersAsMainWindow(shell);
    }

    /**
     * The native top-level window this shell is drawn into, or 0 when there is none.
     *
     * <p>A window the application opens on its own, such as a Swing dialog from embedded content,
     * takes it as its owner: that ownership is what keeps it above the application window.
     */
    default long nativeWindowHandle(Object shell) {
        return 0;
    }

    /**
     * Identifies the window this shell is drawn into: the shell rooting a window of its own that is
     * {@code shell} or its nearest ancestor, or null for the bridge's own window. An ancestor hidden
     * with its window, or whose window is going away, does not count. Two shells with the same answer
     * are drawn by the same client.
     */
    default Object windowOf(Object shell) {
        return null;
    }

    /**
     * The rectangle, in display coordinates, of {@code shell}'s window of its own: an OS window, the strip
     * its client draws above the shell included, or a browser's page. Null for a shell without one, or
     * whose window is gone; {@link #windowOf} names the window such a shell is drawn into.
     */
    Rectangle ownWindowBounds(Object shell);

    /** Runs {@code listener} on the UI thread after a shell got a window of its own, or stopped having one. */
    void addShellWindowListener(Runnable listener);

    void removeShellWindowListener(Runnable listener);

    /** The display's shells in the order the clients draw them, the topmost last. */
    Object[] shellsInDrawOrder();
}

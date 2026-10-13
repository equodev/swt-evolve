package dev.equo.swt;

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
     * The native top-level window this shell is drawn into, or 0 when there is none.
     *
     * <p>A window the application opens on its own, such as a Swing dialog from embedded content,
     * takes it as its owner: that ownership is what keeps it above the application window.
     */
    default long nativeWindowHandle(Object shell) {
        return 0;
    }
}

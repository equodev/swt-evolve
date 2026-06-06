package dev.equo.swt;

import org.eclipse.swt.widgets.DartControl;

/**
 * Optional capability a {@link FlutterBridge} may implement when it is backed by a
 * real OS window — e.g. the Equo Chromium standalone window hosting the Flutter web app.
 * <p>
 * The web bridge ({@code SwtFlutterBridgeWeb}) implements this so that window operations on
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
}

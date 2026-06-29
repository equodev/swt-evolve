package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterNative;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Native-window surface for a Display-level bridge ("desktop-native", 100% Flutter): the whole
 * Dart-backed SWT tree is rendered by one Flutter view hosted in a native top-level window
 * (NSWindow / Win32 FlutterWindow / GtkWindow) instead of a browser. Nothing from the original
 * native SWT is involved — the Display and all its widgets are Dart Java classes, exactly as in the
 * web case; only the transport differs.
 *
 * <p>It reuses everything {@link DisplayBridge} provides (one comm per Display, the
 * {@code Display/{id}/ClientReady} handshake, serialization, shell tracking) and swaps the
 * transport: rather than starting a {@code WebFlutterServer} and opening a browser, it asks the
 * native bridge to create the window and host a Flutter view that connects back over the comm port.
 *
 * <p>OS-agnostic: the per-OS native specifics (creating the window, running the event loop) live in
 * {@code flutter_bridge.swift} / {@code flutter_bridge.cpp} / {@code flutter_bridge.cc}. The per-OS
 * Java-side native init (shared with the web surface) is done once via {@link DisplayBridgePlatform}, whose
 * concrete lives in each web-OS source set.
 */
public class DeskDisplayBridge extends DisplayBridge {

    /** Native window/controller handle returned by {@link dev.equo.swt.FlutterNative#initialize}. */
    private long windowContext;

    /**
     * True once the user closed the native window (pump reported it gone). We stop pumping/waiting on
     * it, but deliberately keep {@link #windowContext} non-zero so {@link #destroyDisplay()} still
     * calls {@link FlutterNative#dispose} to shut the Flutter engine down cleanly. Skipping that
     * (the previous behaviour) left the engine running and crashed the process on teardown with
     * "mutex lock failed" (SIGABRT) on the platforms whose engine shutdown lives in dispose().
     */
    private boolean windowClosed;

    DeskDisplayBridge(DartDisplay display) {
        super(display);
        // The native window is the client; never fall back to a browser/chromium launch.
        System.setProperty("dev.equo.swt.mode", "desktop");
        DisplayBridgePlatform.init();
    }

    /** Creates the native window for the given Display and wires the comm handshake. */
    @Override
    protected void start(DartDisplay display) {
        registerDisplayClientReady(display);

        long displayId = display.getApi().hashCode();
        int port = comm().getPort();
        String theme = Display.isSystemDarkTheme() ? "dark" : "light";

        // Paint the Display with the real widget background (matching the embedded path); passing 0
        // here would make the Flutter theme render a pure-black Display background.
        Color bg = display.getApi().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        int backgroundColor = (bg.getRed() << 16) | (bg.getGreen() << 8) | bg.getBlue();

        // The default Display bounds (1920x1080) is just a placeholder; create the window at a sane
        // size and let the ClientReady handshake correct Display bounds to the real viewport size.
        Rectangle b = display.bounds;
        int width = (b != null && b.width > 0 && b.width <= 2560) ? b.width : 1280;
        int height = (b != null && b.height > 0 && b.height <= 1600) ? b.height : 800;

        windowContext = FlutterNative.initialize(
                port, 0, displayId, "Display", theme, backgroundColor, backgroundColor, width, height);
    }

    /**
     * Pumps the native window's event loop. Driven from {@code DartDisplay.readAndDispatch()}. When
     * the native side reports the window was closed, dispose the SWT side to match (close the
     * top-level shells) so snippet event loops shut down cleanly instead of spinning with no window.
     */
    /**
     * The native window is pull-driven: its OS event loop only advances while {@code DartDisplay.sleep()}
     * blocks in {@link #sleep(int)} ({@code FlutterNative.waitEvents}) and {@link #onUpdate()} pumps it.
     * So the idle wait must go through the bridge rather than parking on the wake permit.
     */
    @Override
    public boolean needsPump() {
        return true;
    }

    @Override
    public void onUpdate() {
        if (windowContext == 0 || windowClosed) {
            return;
        }
        int status = FlutterNative.pump(windowContext);
        if (status < 0) {
            onWindowClosed();
        }
    }

    /**
     * Idle wait: block inside the native (Cocoa/GTK/Win32) event loop instead of {@code Thread.sleep}.
     * Keeps the SWT main thread parked in the OS event loop, so a click, key, or window event wakes
     * it immediately (no up-to-16ms input lag) and live window drag/resize stays smooth. The deadline
     * still bounds the wait so Java-side timers (caret blink, etc.) keep ticking when idle.
     */
    @Override
    public void sleep(int millis) throws InterruptedException {
        if (windowContext != 0 && !windowClosed) {
            FlutterNative.waitEvents(windowContext, millis);
        } else {
            Thread.sleep(millis);
        }
    }

    private void onWindowClosed() {
        // Avoid re-entrancy: once we start closing, stop pumping the (now gone) window. Keep
        // windowContext non-zero so destroyDisplay() still shuts the engine down (see windowClosed).
        if (windowClosed) {
            return;
        }
        windowClosed = true;
        DartDisplay display = forDisplay;
        if (display == null) {
            return;
        }
        Display api = display.getApi();
        if (api == null || api.isDisposed()) {
            return;
        }
        api.asyncExec(() -> {
            if (api.isDisposed()) {
                return;
            }
            for (Shell shell : display._shells()) {
                if (shell != null && !shell.isDisposed() && shell.getParent() == null) {
                    shell.close();
                }
            }
        });
    }

    @Override
    public void destroyDisplay() {
        if (windowContext != 0) {
            FlutterNative.dispose(windowContext);
            windowContext = 0;
        }
        super.destroyDisplay();
    }

    /**
     * Desk semantics: the Display is the <em>monitor</em>, governed independently of the window. The
     * reported {@code monitor} drives {@code display.bounds} (so {@code Display}/{@code Monitor} report
     * the real screen), while the reported viewport drives the main <em>shell</em> only — the two are
     * never conflated (that conflation is the resize loop). The shell resize is flagged so it is not
     * echoed back to the OS window. Returns whether anything changed (a no-op repeat must not re-push).
     */
    @Override
    protected boolean applyClientViewport(DartDisplay display, Rectangle viewport, Rectangle monitor, boolean isFirst) {
        boolean changed = false;

        if (monitor != null && !display.bounds.equals(monitor)) {
            display.bounds = monitor; // the Display is the screen, not the window
            changed = true;
        }

        Shell main = mainShell(display);
        if (main != null) {
            Rectangle current = main.getBounds();
            boolean shellMatches = current != null && current.x == 0 && current.y == 0
                    && current.width == viewport.width && current.height == viewport.height;
            if (!shellMatches) {
                applyClientBounds(() -> resizeMainShells(display, viewport));
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Desk: the shell is independent of the monitor-sized Display, so (unlike the web surface) don't
     * grow it to display bounds on show — the main shell is sized to the window viewport by
     * {@link #applyClientViewport}. Just lay out and push.
     */
    @Override
    public void setVisible(DartControl control, boolean visible) {
        if (control instanceof DartShell dartShell) {
            if (visible) {
                ((Shell) dartShell.getApi()).layout(true, true);
            }
            sendDisplayUpdate(forDisplay);
        }
    }

    /** Whether a live native top-level window backs this Display (false in headless tests, and once
     *  the user has closed the window — so window ops aren't forwarded to a closing/gone window). */
    protected boolean hasNativeWindow() {
        return windowContext != 0 && !windowClosed;
    }

    /** Pushes a genuine, user-/app-driven geometry to the native window. Seam for tests to observe. */
    protected void forwardWindowBounds(Rectangle bounds) {
        FlutterNative.setBounds(windowContext, bounds.x, bounds.y, bounds.width, bounds.height,
                bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * True when window operations on this control should drive the native top-level window: only for
     * the main top-level shell (never dialogs/tool/modal shells), mirroring the chromium case.
     */
    private boolean isMainWindow(DartControl control) {
        return hasNativeWindow()
                && control instanceof DartShell dartShell
                && shouldTrackDisplayBounds((Shell) dartShell.getApi());
    }

    @Override
    public void setWindowTitle(DartControl control, String title) {
        if (isMainWindow(control)) {
            FlutterNative.setTitle(windowContext, title == null ? "" : title);
        }
    }

    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
        if (!isMainWindow(control) || bounds == null) {
            return;
        }
        // Applying a client-reported resize (the main shell being slaved to the window viewport by
        // applyClientViewport()/setVisible()): the window already has this size, so echoing it back
        // fights the window manager and oscillates — the resize loop. Only genuine app-driven geometry
        // reaches the OS window.
        if (applyingClientBounds) {
            return;
        }
        Shell shell = (Shell) ((DartShell) control).getApi();
        if (shell.getMaximized() || shell.getFullScreen()) {
            return;
        }
        forwardWindowBounds(bounds);
    }

    @Override
    public void setWindowMaximized(DartControl control, boolean maximized) {
        if (isMainWindow(control)) {
            FlutterNative.setState(windowContext, maximized ? 1 : 0);
        }
    }

    @Override
    public void setWindowMinimized(DartControl control, boolean minimized) {
        if (isMainWindow(control)) {
            FlutterNative.setState(windowContext, minimized ? 2 : 0);
        }
    }

    @Override
    public void setWindowFullScreen(DartControl control, boolean fullScreen) {
        if (isMainWindow(control)) {
            FlutterNative.setState(windowContext, fullScreen ? 3 : 0);
        }
    }
}

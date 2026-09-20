package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterNative;
import dev.equo.swt.ShellWindow;
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

    /**
     * True only while the shell close that {@link #onWindowCloseRequested} asked for is running.
     * The window follows an <em>accepted close gesture</em>, never the mere absence of shells: an
     * application that disposes a splash before opening its real shell leaves no top-level shell for
     * an instant, and tearing the window down there strands the app with no window at all.
     */
    private boolean closingOnRequest;

    DeskDisplayBridge(DartDisplay display) {
        super(display);
        // The native window is the client; never fall back to a browser/chromium launch.
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        DisplayBridgePlatform.init();
    }

    /** Creates the native window for the given Display and wires the comm handshake. */
    @Override
    protected void start(DartDisplay display) {
        registerDisplayClientReady(display);
        registerDisplayKeyEvents(display);

        long displayId = display.getApi().hashCode();

        // The default Display bounds (1920x1080) is just a placeholder; create the window at a sane
        // size and let the ClientReady handshake correct Display bounds to the real viewport size.
        Rectangle b = display.bounds;
        int width = (b != null && b.width > 0 && b.width <= 2560) ? b.width : 1280;
        int height = (b != null && b.height > 0 && b.height <= 1600) ? b.height : 800;

        windowContext = openNativeWindow(displayId, "Display", width, height);
    }

    /** Opens one native top-level window hosting a Flutter client rooted at {@code widgetName/id}.
     *  Seam: tests stand a window in without JNI. */
    protected long openNativeWindow(long widgetId, String widgetName, int width, int height) {
        // Paint the window with the real widget background (matching the embedded path); passing 0
        // here would make the Flutter theme render a pure-black background.
        Color bg = forDisplay.getApi().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        int backgroundColor = (bg.getRed() << 16) | (bg.getGreen() << 8) | bg.getBlue();
        String theme = Display.isSystemDarkTheme() ? "dark" : "light";
        return FlutterNative.initialize(comm().getPort(), 0, widgetId, widgetName, theme,
                backgroundColor, backgroundColor, width, height);
    }

    /**
     * The native window is pull-driven: its OS event loop only advances while {@code DartDisplay.sleep()}
     * blocks in {@link #sleep(int)} ({@code FlutterNative.waitEvents}) and {@link #onUpdate()} pumps it.
     * So the idle wait must go through the bridge rather than parking on the wake permit.
     */
    @Override
    public boolean needsPump() {
        return true;
    }

    /**
     * Pumps the native window's event loop. Driven from {@code DartDisplay.readAndDispatch()}. The
     * window reports two different things: a close the user <em>asked</em> for, which SWT still gets to
     * refuse ({@link #onWindowCloseRequested}), and a window that is already gone, which it can only
     * be told about ({@link #onWindowClosed}).
     */
    @Override
    public void onUpdate() {
        if (!hasNativeWindow()) {
            return;
        }
        super.onUpdate();
        if (boundShell == null || boundShell.isDisposed())
            bindWindowView();
        applyWindowOrigin(mainShell(forDisplay), mainWindowOrigin());

        int status = pumpWindow();
        if (status == FlutterNative.PUMP_CLOSE_REQUESTED) {
            onWindowCloseRequested();
        } else if (status < 0) {
            onWindowClosed();
        }
        pumpShellWindows();
    }

    // ---- one native window per detached shell (see WindowPolicy) ----------------------------------

    /** The detached-shell windows, pumped alongside the Display's own. */
    private final java.util.List<NativeShellWindow> shellWindowsToPump = new java.util.ArrayList<>();

    @Override
    protected boolean keepsMainShellOrigin() {
        // The window has a real place on screen, and applyWindowOrigin keeps the shell told of it.
        return true;
    }

    @Override
    protected boolean supportsShellWindows() {
        return hasNativeWindow();
    }

    @Override
    protected ShellWindow createShellWindow(Shell shell) {
        Rectangle bounds = shell.getBounds();
        int width = bounds.width > 0 ? bounds.width : 640;
        int height = bounds.height > 0 ? bounds.height : 480;
        long context = openNativeWindow(shell.hashCode(), "Shell", width, height);
        if (context == 0) return null;
        NativeShellWindow window = new NativeShellWindow(shell, context);
        shellWindowsToPump.add(window);
        // A detached shell opens where the application put it; the window is created at a size only,
        // so the position has to be pushed once or every window would stack at the OS default spot.
        // A shell still at the origin was never placed — the window is then cascaded instead, the way
        // a window manager places one, because the alternative is every unplaced window landing in
        // exactly the same centred spot, which looks like opening one did nothing at all.
        Rectangle placed = (bounds.x != 0 || bounds.y != 0) ? bounds : cascade(bounds);
        window.setBounds(placed);
        shell.setLocation(placed.x, placed.y);
        String title = shell.getText();
        if (title != null && !title.isEmpty()) window.setTitle(title);
        bindShellWindowView(shell, context);
        return window;
    }

    // Every native call a detached window makes goes through one of these, so a test can stand a
    // window in behind a fake handle without any of them reaching JNI.

    /** Hands the window's view to its shell, so an application reaching for a handle finds one. */
    protected void bindShellWindowView(Shell shell, long context) {
        DisplayBridgePlatform.bindWindowView(shell, FlutterNative.getView(context));
    }

    protected int pumpShellWindow(long context) {
        return FlutterNative.pump(context);
    }

    protected void disposeShellWindow(long context) {
        FlutterNative.dispose(context);
    }

    protected void setShellWindowTitle(long context, String title) {
        FlutterNative.setTitle(context, title);
    }

    protected void setShellWindowBounds(long context, Rectangle b) {
        // Same rule as the Display's own window: a shell that has not been laid out yet carries a
        // placeholder size, and a window shrunk to a pixel never reports a viewport worth having, so
        // nothing ever corrects it. Move it, but keep the size it already has.
        if (b.width <= 1 || b.height <= 1) {
            FlutterNative.setBounds(context, b.x, b.y, 0, 0, b.x, b.y, 0, 0);
            return;
        }
        FlutterNative.setBounds(context, b.x, b.y, b.width, b.height, b.x, b.y, b.width, b.height);
    }

    protected void setShellWindowState(long context, int state) {
        FlutterNative.setState(context, state);
    }

    /** Test seam: shows or hides a detached window without taking it down. */
    protected void setShellWindowVisible(long context, boolean visible) {
        FlutterNative.setVisible(context, visible);
    }

    /** Where the Display's own window sits, or null when there is none (headless tests). */
    protected org.eclipse.swt.graphics.Point mainWindowOrigin() {
        return hasNativeWindow() ? shellWindowOrigin(windowContext) : null;
    }

    /** Test seam: where a detached window's content sits on screen, or null when it cannot be told. */
    protected org.eclipse.swt.graphics.Point shellWindowOrigin(long context) {
        long packed = FlutterNative.getOrigin(context);
        if (packed == FlutterNative.ORIGIN_UNKNOWN) return null;
        return new org.eclipse.swt.graphics.Point(
                FlutterNative.originX(packed), FlutterNative.originY(packed));
    }



    /** How many unplaced windows have been cascaded so far; see {@link #cascade}. */
    private int cascaded;

    /** Step between cascaded windows, and how many before starting over near the origin again. */
    private static final int CASCADE_STEP = 28;
    private static final int CASCADE_WRAP = 8;

    /** A staggered position for a window the application never placed, near the Display's own. */
    private Rectangle cascade(Rectangle size) {
        int step = (cascaded++ % CASCADE_WRAP) * CASCADE_STEP;
        Rectangle origin = forDisplay == null ? null : mainShellBoundsOrNull();
        int x = (origin == null ? 80 : origin.x + 60) + step;
        int y = (origin == null ? 80 : origin.y + 60) + step;
        return new Rectangle(x, y, size.width, size.height);
    }

    private Rectangle mainShellBoundsOrNull() {
        Shell main = mainShell(forDisplay);
        return (main == null || main.isDisposed()) ? null : main.getBounds();
    }

    /** Round-robin cursor over {@link #shellWindowsToPump}; see {@link #pumpShellWindows}. */
    private int nextShellWindowToPump;

    /**
     * Advances <em>one</em> detached shell's window per pass, in turn.
     *
     * <p>A pump is not per-window work: it drains the process-wide event queue and then spins the
     * main run loop for a couple of milliseconds so the engines — which share the platform thread —
     * get time to build a frame. One call gives every window that time. Calling it once per window
     * drains an already-empty queue again and pays the spin again, so the cost of an event-loop turn
     * grew with the number of open windows and the whole application slowed down as more were
     * opened. Taking them in turn keeps that cost flat.
     *
     * <p>What <em>is</em> per-window is the close status, and that is the only reason each window
     * still has to be asked at all. Learning it a few passes late is not observable: the loop turns
     * continuously, and a close gesture is answered on the next pass either way.
     */
    private void pumpShellWindows() {
        shellWindowsToPump.removeIf(window -> !window.isAlive());
        if (shellWindowsToPump.isEmpty()) {
            nextShellWindowToPump = 0;
            return;
        }
        if (nextShellWindowToPump >= shellWindowsToPump.size()) nextShellWindowToPump = 0;
        NativeShellWindow window = shellWindowsToPump.get(nextShellWindowToPump++);

        // One window per pass, the same budget the pump already works to: a window the user moved
        // reports its new origin on the next turn, the same freshness a close gets.
        applyWindowOrigin(window.shell, window.origin());

        int status = window.pump();
        if (status == FlutterNative.PUMP_CLOSE_REQUESTED) {
            closeShellFromWindow(window.shell);
        } else if (status < 0) {
            // The window is already gone. Report it as such and let the registry react, which is
            // the one place that knows what losing a window means for the shell that was in it.
            window.markGone();
            shellWindowsToPump.remove(window);
            if (forDisplay != null) sendDisplayUpdate(forDisplay);
        }
    }

    /**
     * The user asked a detached shell's window to close and the runner vetoed the OS teardown, so
     * that window is still up. Same contract as the Display's own window, one shell out:
     * {@code SWT.Close} while there is still something to render into, and the window follows only
     * if the shell is actually disposed.
     */
    private void closeShellFromWindow(Shell shell) {
        Display api = forDisplay == null ? null : forDisplay.getApi();
        if (api == null || api.isDisposed()) return;
        api.asyncExec(() -> {
            if (api.isDisposed() || shell.isDisposed()) return;
            shell.close();
        });
    }

    /** Test seam: opens a window without JNI. */
    protected NativeShellWindow newShellWindow(Shell shell, long context) {
        return new NativeShellWindow(shell, context);
    }

    /** A detached shell's native top-level window. */
    protected class NativeShellWindow implements ShellWindow {
        final Shell shell;
        private long context;
        private boolean gone;
        /** A window is created showing, and is only told when that changes. */
        private boolean shown = true;

        NativeShellWindow(Shell shell, long context) {
            this.shell = shell;
            this.context = context;
        }

        int pump() {
            return pumpShellWindow(context);
        }

        void markGone() {
            gone = true;
        }

        @Override
        public boolean isAlive() {
            return context != 0 && !gone;
        }

        @Override
        public void setTitle(String title) {
            if (isAlive()) setShellWindowTitle(context, title);
        }

        @Override
        public void setBounds(Rectangle bounds) {
            if (isAlive()) setShellWindowBounds(context, bounds);
        }

        @Override
        public void setState(int state) {
            if (isAlive()) setShellWindowState(context, state);
        }

        @Override
        public org.eclipse.swt.graphics.Point origin() {
            return isAlive() ? shellWindowOrigin(context) : null;
        }

        @Override
        public void setVisible(boolean visible) {
            // Asked on every pass, so only a change crosses JNI.
            if (visible == shown || !isAlive()) return;
            shown = visible;
            setShellWindowVisible(context, visible);
        }

        @Override
        public void close() {
            if (context == 0) return;
            // Dispose whether or not the OS already took the window down: that shuts the window's
            // own Flutter engine, which is what a bare close leaves running.
            disposeShellWindow(context);
            context = 0;
            shellWindowsToPump.remove(this);
        }
    }

    /** Pumps the native window once. Seam for tests to script what the window reports. */
    protected int pumpWindow() {
        return FlutterNative.pump(windowContext);
    }

    /**
     * Idle wait: block inside the native (Cocoa/GTK/Win32) event loop instead of {@code Thread.sleep}.
     * Keeps the SWT main thread parked in the OS event loop, so a click, key, or window event wakes
     * it immediately (no up-to-16ms input lag) and live window drag/resize stays smooth. The deadline
     * still bounds the wait so Java-side timers (caret blink, etc.) keep ticking when idle.
     */
    @Override
    public void sleep(int millis) throws InterruptedException {
        if (hasNativeWindow()) {
            FlutterNative.waitEvents(windowContext, millis);
        } else {
            Thread.sleep(millis);
        }
    }

    /**
     * The user asked the OS window to close (title-bar X, Alt+F4, Cmd+W) and the runner vetoed the OS
     * teardown, so the window is <em>still up</em>. Answer with SWT's contract on the main shell:
     * {@code Shell.close()} fires {@code SWT.Close} while there is still a window to render into, so an
     * application can put up "Save changes before exiting?" and keep everything alive with
     * {@code doit = false}. The window follows the gesture, not the shell count — it is torn down from
     * {@link #destroy} only if this close is accepted and disposes the shell.
     */
    private void onWindowCloseRequested() {
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
            Shell main = mainShell(display);
            if (main == null || main.isDisposed()) {
                return;
            }
            closingOnRequest = true;
            try {
                main.close();
            } finally {
                closingOnRequest = false;
            }
        });
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

    /**
     * Takes the window down once the close the user asked for has actually been accepted — the shell
     * is disposed and no other top-level shell is left to host. This is the only route by which a
     * user-driven close destroys the window: the gesture itself is just a request
     * ({@link #onWindowCloseRequested}), and a vetoed one must leave the window standing.
     *
     * <p>A shell disposed <em>outside</em> that flow never takes the window with it, however briefly
     * it leaves the Display with no top-level shell — a splash disposed before the real shell opens
     * is the case that matters, and destroying the window there leaves the application running with
     * no window it can ever show.
     */
    @Override
    public void destroy(DartWidget control) {
        super.destroy(control);
        if (closingOnRequest && control instanceof DartShell && hasNativeWindow()
                && forDisplay != null && mainShell(forDisplay) == null) {
            disposeNativeWindow();
        }
    }

    @Override
    public void destroyDisplay() {
        disposeNativeWindow();
        super.destroyDisplay();
    }

    /** Shuts the native window and its Flutter engine down. Seam for tests to observe the teardown. */
    protected void disposeNativeWindow() {
        if (windowContext != 0) {
            FlutterNative.dispose(windowContext);
            windowContext = 0;
        }
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
            // A window not sized yet reports a 1x1 viewport, and taking it as authoritative collapses
            // the shell to match -- after which nothing reports anything else. Until the window has a
            // real viewport the shell is the source of truth, so push its geometry out.
            if (viewport.width <= 1 || viewport.height <= 1) {
                if (current != null && current.width > 1 && current.height > 1) {
                    forwardWindowBounds(current);
                    changed = true;
                }
                return changed;
            }
            // Size only: where the shell is, is the window's business (applyWindowOrigin), not the
            // viewport's. Requiring (0,0) here treated any real position as "needs re-pinning".
            boolean shellMatches = current != null
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
        if (control instanceof DartShell) {
            if (visible) {
                ((Shell) ((DartShell) control).getApi()).layout(true, true);
            }
            sendDisplayUpdate(forDisplay);
        }
    }

    /** Whether a live native top-level window backs this Display (false in headless tests, and once
     *  the user has closed the window — so window ops aren't forwarded to a closing/gone window). */
    protected boolean hasNativeWindow() {
        return hasWindowSurface() && !windowClosed;
    }

    /** Whether a native window surface was created at all. Seam: tests stand one in without JNI. */
    protected boolean hasWindowSurface() {
        return windowContext != 0;
    }

    /** Pushes a genuine, user-/app-driven geometry to the native window. Seam for tests to observe. */
    protected void forwardWindowBounds(Rectangle bounds) {
        // A shell not laid out yet carries a placeholder size. A window collapsed to a pixel reports
        // no viewport, so the handshake that would correct it never runs: keep the size it has.
        if (bounds.width <= 1 || bounds.height <= 1) return;
        FlutterNative.setBounds(windowContext, bounds.x, bounds.y, bounds.width, bounds.height,
                bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * True when window operations on this control should drive the native top-level window: only for
     * the main top-level shell (never dialogs/tool/modal shells), mirroring the chromium case.
     */
    private boolean isMainWindow(DartControl control) {
        return hasNativeWindow()
                && control instanceof DartShell
                && isMainShell(forDisplay, (Shell) ((DartShell) control).getApi());
    }

    /** The Shell already given the window's view, so this is done once and not on every pass. */
    private Shell boundShell;

    /**
     * Hands the native window's view to the Shell filling it. Nothing on this backend does so
     * otherwise -- a Dart-backed Control has no native handle -- and an application reaching for it
     * gets an empty one, which silently swallows whatever it asks the window to do.
     */
    private void bindWindowView() {
        if (windowContext == 0 || windowClosed)
            return;
        Shell shell = mainShell(forDisplay);
        if (shell == null || shell.isDisposed())
            return;
        boundShell = shell;
        DisplayBridgePlatform.bindWindowView(shell, FlutterNative.getView(windowContext));
    }

    @Override
    public void setWindowTitle(DartControl control, String title) {
        if (isMainWindow(control)) {
            FlutterNative.setTitle(windowContext, title == null ? "" : title);
            return;
        }
        super.setWindowTitle(control, title);
    }

    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
        if (!isMainWindow(control)) {
            super.setBounds(control, bounds);
            return;
        }
        if (bounds == null) {
            return;
        }
        // Geometry that came FROM the window must not be commanded back onto it -- that round trip is
        // the resize loop. The client is still told: it is what positions a popup.
        if (applyingClientBounds) {
            super.setBounds(control, bounds);
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
            return;
        }
        super.setWindowMaximized(control, maximized);
    }

    @Override
    public void setWindowMinimized(DartControl control, boolean minimized) {
        if (isMainWindow(control)) {
            FlutterNative.setState(windowContext, minimized ? 2 : 0);
            return;
        }
        super.setWindowMinimized(control, minimized);
    }

    @Override
    public void setWindowFullScreen(DartControl control, boolean fullScreen) {
        if (isMainWindow(control)) {
            FlutterNative.setState(windowContext, fullScreen ? 3 : 0);
            return;
        }
        super.setWindowFullScreen(control, fullScreen);
    }
}

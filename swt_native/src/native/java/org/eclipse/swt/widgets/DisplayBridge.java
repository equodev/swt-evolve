package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.WindowBridge;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Base for every <em>Display-level</em> bridge: the variants where the entire Dart-backed SWT tree
 * (DartDisplay, DartShell, …) is rendered by a single Flutter surface that talks back over one
 * comm per {@link Display}, rather than one embedded Flutter view per native widget.
 *
 * <p>It owns everything the surfaces share — the per-Display comm, the {@code Display/{id}/ClientReady}
 * handshake, serialization to Flutter, shell tracking, focus, and the {@link #of}/{@link #initForDisplay}
 * dispatch. The two concrete surfaces differ only in <em>where</em> the Flutter view lives and how its
 * event loop is driven:
 * <ul>
 *   <li>{@link WebDisplayBridge} — a browser (or Chromium standalone) over a {@code WebFlutterServer}.</li>
 *   <li>{@link DeskDisplayBridge} — one native top-level window.</li>
 * </ul>
 * Those differences are the {@code abstract}/overridable hooks below: {@link #start},
 * {@link #onUpdate}, {@link #sleep}, {@link #destroyDisplay}, and the {@link WindowBridge} window ops.
 */
public abstract class DisplayBridge extends FlutterBridge implements WindowBridge {

    protected DartDisplay forDisplay;
    private DartControl focused = null;
    /** One comm (and one surface) per Display, unlike the embedded bridge's shared static comm. */
    private CommService comm;

    /**
     * Set while applying a client-reported viewport to the shells. The resulting {@code shell.setBounds()}
     * must then NOT be echoed back to the OS window as a resize command — that round-trip (window resize
     * &rarr; ClientReady &rarr; shell resize &rarr; window resize) is the desktop/Chromium resize loop.
     * Single-threaded: only ever read/written on the Display's UI thread.
     */
    protected boolean applyingClientBounds;

    protected DisplayBridge(DartDisplay display) {
        this.forDisplay = display;
        System.setProperty("dev.equo.swt.desktop", "false");
    }

    @Override
    protected CommService comm() {
        if (comm == null) {
            comm = newComm();
        }
        return comm;
    }

    /** True when the tree should be rendered in a native top-level window instead of a browser. */
    static boolean isDesktopMode() {
        return ConfigFlags.isDesktopMode();
    }

    /**
     * Creates and starts the Display-level bridge. Called once during {@code Display.init()}. Picks
     * the native-window surface under {@code -Ddev.equo.swt.mode=desktop}, otherwise the web surface.
     * Both concrete classes are resolved per-OS from the compiled source set.
     */
    public static DisplayBridge initForDisplay(DartDisplay display) {
        // A test/bench harness may inject its own bridge (FlutterBridge.set) before the Display is
        // created; it then owns the comm/server/browser and every widget already routes through it.
        // Creating a per-Display bridge here would double-boot a second server. Skip it; leaving
        // displayBridge null is safe — all its uses are null-guarded.
        if (FlutterBridge.injected() != null) return null;
        DisplayBridge bridge = isDesktopMode()
                ? new DeskDisplayBridge(display)
                : new WebDisplayBridge(display);
        display.setBridge(bridge);
        bridge.start(display);
        return bridge;
    }

    /** Starts the surface for the Display (web server + browser, or native window). */
    protected abstract void start(DartDisplay display);

    /**
     * Returns the display-level bridge for the given widget's display. Called from
     * {@code DartWidget.register()} for every control; also registers top-level shells with the display.
     */
    public static FlutterBridge of(DartWidget widget) {
        // A test/bench harness may inject a global bridge (FlutterBridge.set) that owns the comm;
        // every widget routes through it instead of the per-Display bridge.
        FlutterBridge injected = injected();
        if (injected != null) return injected;
        if (widget instanceof DartControl dartControl) {
            Display display = dartControl._display();
            if (display != null) {
                DartDisplay dartDisplay = (DartDisplay) display.getImpl();
                if (widget instanceof DartShell dartShell) {
                    dartDisplay.addShell((Shell) dartShell.getApi());
                    dartShell.bounds = dartDisplay.bounds;
                }
                return dartDisplay.displayBridge;
            }
        }
        return null;
    }

    /**
     * Register a raw string-channel handler on this Display's comm. Used by widget-less surfaces
     * (Dialog/MessageBox) that have no bridge of their own but belong to a Display.
     */
    public void onChannel(String channel, java.util.function.Consumer<String> cb) {
        comm().on(channel, String.class, cb);
    }

    /** Remove a handler registered via {@link #onChannel}. */
    public void offChannel(String channel) {
        comm().remove(channel);
    }

    /**
     * Wires the {@code Display/{id}/ClientReady} handler that fires when the Flutter client (browser
     * or native window) has rendered the Display: syncs the Display bounds to the reported viewport
     * size, completes {@link #clientReady}, pushes the swt.evolve properties and the first update.
     */
    protected void registerDisplayClientReady(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        // Completing clientReady + pushing swt.evolve.properties on the first ClientReady is handled
        // by the shared FlutterBridge.onClientReady template; here we only do the Display-specific
        // work: sync Display bounds to the reported viewport and push the (first/next) update.
        onClientReady("Display/" + displayId + "/ClientReady", ClientReadyPayload.class, (p, first) -> {
            if (p == null) return;
            // A client that reconnects reporting p.isFirst=true — a fresh Flutter instance re-established
            // the socket (e.g. a browser refresh) but is NOT the bridge's first ClientReady — needs the
            // swt.evolve properties re-pushed, or its theme/config init is lost on refresh.
            if (!first && p.isFirst) broadcastSwtEvolveProperties();

            // A reconnecting client (e.g. a browser refresh re-establishing the socket and re-sending
            // ClientReady) cancels any pending tab-close — see WebDisplayBridge.onDisplayClientReady.
            onDisplayClientReady(first);

            Display api = display.getApi();
            Runnable apply = () -> {
                boolean changed = applyClientViewport(display,
                        new Rectangle(0, 0, p.width, p.height), monitorOf(p), p.isFirst);
                // Push on the very first ClientReady (it bootstraps the Flutter tree); otherwise only
                // when something actually changed, so a repeated identical viewport doesn't feed a loop.
                if (changed || first) sendDisplayUpdate(display);
            };
            // applyClientViewport touches shells (setBounds/layout) so it must run on the Display thread.
            // In production this handler runs on the comm (WebSocket) thread: a syncExec would block it
            // until the Display thread services the runnable, but the idle E4 workbench parks the Display
            // thread inside the native event pump, so it would block forever (a leaked comm thread, and
            // the shell never sized to the viewport). Post async instead — the buffered pre-connect
            // Display state has already given Flutter its first frame, so the loop is live and drains it
            // promptly. When ALREADY on the Display thread (unit tests drive ClientReady inline), run it
            // synchronously so the resize is observable immediately.
            if (api.getThread() == Thread.currentThread()) apply.run();
            else api.asyncExec(apply);
        });
    }

    /**
     * Hook fired on every {@code Display/{id}/ClientReady}, before the viewport sync. No-op here; the
     * web surface overrides it to cancel a deferred tab-close when a refreshed client reconnects.
     * {@code first} is whether this was the first (bridge-completing) ClientReady.
     */
    protected void onDisplayClientReady(boolean first) {}

    /** The reported monitor rectangle (origin 0,0), or {@code null} when the client didn't report one. */
    private static Rectangle monitorOf(ClientReadyPayload p) {
        return (p.displayWidth > 0 && p.displayHeight > 0)
                ? new Rectangle(0, 0, p.displayWidth, p.displayHeight)
                : null;
    }

    /**
     * Apply a client-reported geometry. Web surface (the default here): the browser viewport
     * <em>is</em> the Display, so sync {@code display.bounds} to the viewport and slave the
     * display-tracking main shell(s) to it (the reported {@code monitor} is ignored — on web the
     * viewport is the monitor). The shell resize is flagged (see {@link #applyingClientBounds}) so it
     * is not echoed back to the window. Returns whether anything changed — a no-op repeat must not
     * re-push, or it would feed the resize loop. The desktop surface overrides this (there the Display
     * is the {@code monitor}, governed independently of the window — see {@code DeskDisplayBridge}).
     */
    protected boolean applyClientViewport(DartDisplay display, Rectangle viewport, Rectangle monitor, boolean isFirst) {
        if (!isFirst && display.bounds.equals(viewport)) {
            return false; // idempotent: same viewport as before — no churn, no echo
        }
        display.bounds = viewport;
        applyClientBounds(() -> resizeMainShells(display, viewport));
        return true;
    }

    /** Runs {@code apply} (which resizes shells) with the window-echo suppressed. */
    protected void applyClientBounds(Runnable apply) {
        boolean previous = applyingClientBounds;
        applyingClientBounds = true;
        try {
            apply.run();
        } finally {
            applyingClientBounds = previous;
        }
    }

    public void sendDisplayUpdate(DartDisplay display) {
        // NOTE: no `clientReady` gate here. A Display update produced before the Flutter client has
        // connected (e.g. the E4 workbench shows its top-level Shell during startup, long before the
        // native window's Flutter engine connects) must still be serialized and sent — the comm layer
        // BUFFERS pre-connect frames and flushes them on connect (AbstractBinaryCommService). Skipping
        // it here left the client with no shells until a post-connect re-push, but that re-push runs on
        // the comm thread and needs the UI thread, which the idle E4 workbench traps inside the native
        // event pump — so the window stayed blank. Buffering the pre-connect state avoids that race and
        // gives Flutter content the moment it connects (which also keeps its runloop live).
        try {
            VDisplay vd = VDisplay.of(display);
            serializeAndSend("Display/" + vd.id, vd);
            FlutterBridge.displayBootstrapped = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Slave every display-tracking main shell to {@code viewport}. Caller wraps this in
     *  {@link #applyClientBounds} so the resulting setBounds isn't echoed to the window. */
    protected void resizeMainShells(DartDisplay display, Rectangle viewport) {
        for (Shell shell : display._shells()) {
            if (shell == null || shell.isDisposed()) {
                continue;
            }
            if (!shouldTrackDisplayBounds(shell)) {
                continue;
            }
            Rectangle shellBounds = shell.getBounds();
            boolean atOrigin =
                    shellBounds != null
                            && shellBounds.x == 0
                            && shellBounds.y == 0
                            && shellBounds.width > 0
                            && shellBounds.height > 0;
            if (atOrigin || shell.getMaximized() || shell.getFullScreen()) {
                shell.setBounds(0, 0, viewport.width, viewport.height);
            }
        }
    }

    /** The first top-level, display-tracking shell (the one slaved to the window viewport), or null. */
    protected Shell mainShell(DartDisplay display) {
        for (Shell shell : display._shells()) {
            if (shell != null && !shell.isDisposed() && shouldTrackDisplayBounds(shell)) {
                return shell;
            }
        }
        return null;
    }

    protected boolean shouldTrackDisplayBounds(Shell shell) {
        if (shell.getParent() != null) {
            return false;
        }
        String text = shell.getText();
        if (text != null && text.contains("limbo")) {
            return false;
        }
        int style = shell.getStyle();
        int modalMask = org.eclipse.swt.SWT.PRIMARY_MODAL
                | org.eclipse.swt.SWT.APPLICATION_MODAL
                | org.eclipse.swt.SWT.SYSTEM_MODAL;
        if ((style & modalMask) != 0) {
            return false;
        }
        return (style & org.eclipse.swt.SWT.TOOL) == 0;
    }

    public String registerBrowserHtml(long browserId, String html) {
        return "";
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
        // Bridge is initialized at Display level in initForDisplay().
    }

    /**
     * Drives the surface's event loop once per {@code DartDisplay.readAndDispatch()}. No-op by
     * default; overridden by the surfaces that own an event loop (Chromium / native window).
     */
    public void onUpdate() {
    }

    /**
     * Whether {@code DartDisplay.sleep()} must wait through {@link #sleep(int)} (capped, pump-driven)
     * rather than parking indefinitely on the wake permit. False by default (pure web is fully
     * event-driven); the surfaces that own a pull-driven event loop override it — the Chromium
     * standalone window ({@link WebDisplayBridge}) and the desktop-native window
     * ({@link DeskDisplayBridge}, which blocks in the OS event loop).
     */
    public boolean needsPump() {
        return false;
    }

    /**
     * Idle wait between event-loop turns, called from {@code DartDisplay.sleep()} when {@link
     * #needsPump()} is true. Parks the thread by default; the native-window surface overrides this to
     * block inside the OS event loop so input/window events wake it immediately (see
     * {@link DeskDisplayBridge}).
     */
    public void sleep(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Override
    public void destroy(DartWidget control) {
        if (control instanceof DartShell dartShell && forDisplay != null) {
            forDisplay.removeShell((Shell) dartShell.getApi());
            sendDisplayUpdate(forDisplay);
        }
    }

    /** Tears down the Display's surface and comm. Subclasses override to add surface-specific cleanup. */
    public void destroyDisplay() {
        if (comm != null) {
            comm.stop();
            comm = null;
        }
    }

    @Override
    public Object container(DartComposite parent) {
        return null;
    }

    @Override
    public boolean setFocus(DartControl widget) {
        focused = widget;
        return true;
    }

    @Override
    public boolean hasFocus(DartControl widget) {
        return widget == focused;
    }

    @Override
    public void setVisible(DartControl control, boolean visible) {
        if (control instanceof DartShell dartShell) {
            Shell shell = (Shell) dartShell.getApi();
            if (visible && forDisplay != null) {
                if (shouldTrackDisplayBounds(shell)) {
                    Rectangle displayBounds = forDisplay.bounds;
                    Rectangle shellBounds = shell.getBounds();
                    if (shellBounds != null
                            && shellBounds.x == 0
                            && shellBounds.y == 0
                            && shellBounds.width < displayBounds.width
                            && shellBounds.height < displayBounds.height) {
                        applyClientBounds(() -> shell.setBounds(0, 0, displayBounds.width, displayBounds.height));
                    }
                }
            }
            if (visible) {
                shell.layout(true, true);
            }
            sendDisplayUpdate(forDisplay);
        }
    }

    @Override
    public void reparent(DartControl control, Composite parent) {
    }

    @Override
    public void setZOrder(DartControl control, Control sibling, boolean above) {
    }

    // Window ops (WindowBridge): no-op by default; the surfaces that own a real OS window override.
    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
    }

    @Override
    public void setWindowMaximized(DartControl control, boolean maximized) {
    }

    @Override
    public void setWindowMinimized(DartControl control, boolean minimized) {
    }

    @Override
    public void setWindowFullScreen(DartControl control, boolean fullScreen) {
    }

    @Override
    public void setWindowTitle(DartControl control, String title) {
    }
}

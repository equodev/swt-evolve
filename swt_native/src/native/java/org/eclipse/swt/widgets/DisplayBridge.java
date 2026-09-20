package org.eclipse.swt.widgets;

import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.ShellWindow;
import dev.equo.swt.WindowBridge;
import dev.equo.swt.WindowPolicy;
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

    /** Display-wide: a focus request can name a control the client has not built yet. */
    private static final String FOCUS_CHANNEL = "swt.evolve.focus";

    protected DartDisplay forDisplay;
    private DartControl focused = null;
    /** Last control the client was asked to focus, so one focus move is not sent twice: a single
     *  {@code Control.forceFocus()} reaches {@link #setFocus} again through the shell's restoreFocus. */
    private DartControl focusRequested = null;
    /** One comm (and one surface) per Display, unlike the embedded bridge's shared static comm. */
    private CommService comm;

    /**
     * Set while applying a client-reported viewport to the shells. The resulting {@code shell.setBounds()}
     * must then NOT be echoed back to the OS window as a resize command — that round-trip (window resize
     * &rarr; ClientReady &rarr; shell resize &rarr; window resize) is the desktop/Chromium resize loop.
     * Single-threaded: only ever read/written on the Display's UI thread.
     */
    protected boolean applyingClientBounds;

    /**
     * Shells hosted in a window of their own, in open order. Empty under the default single-window
     * policy, which is what keeps every question below off the normal path.
     */
    private final java.util.LinkedHashMap<Shell, ShellWindow> shellWindows =
            new java.util.LinkedHashMap<>();

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
        FlutterBridge.setDisplayGcCommResolver(DisplayBridge::sharedCommFor);
        return bridge;
    }

    // comm() is safe to hand out before the client connects — the comm layer buffers pre-connect
    // sends and flushes them on connect (see sendDisplayUpdate); the caller's own onReady() gates
    // anything that actually needs the client to be listening.
    private static CommService sharedCommFor(Display display) {
        if (!(display.getImpl() instanceof DartDisplay)) {
            return null;
        }
        DartDisplay dd = (DartDisplay) display.getImpl();
        if (dd.displayBridge == null) {
            return null;
        }
        DisplayBridge db = dd.displayBridge;
        return !display.isDisposed() ? db.comm() : null;
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
        if (widget instanceof DartControl) {
            DartControl dartControl = (DartControl) widget;
            Display display = dartControl._display();
            if (display != null) {
                DartDisplay dartDisplay = (DartDisplay) display.getImpl();
                if (widget instanceof DartShell) {
                    DartShell dartShell = (DartShell) widget;
                    dartDisplay.addShell((Shell) dartShell.getApi());
                    if (injected == null) {
                        if (dartShell.parent == null) {
                            dartShell.bounds = dartDisplay.bounds;
                        } else if (dartShell.bounds.width == 0 && dartShell.bounds.height == 0) {
                            Rectangle db = dartDisplay.bounds;
                            int w = Math.max(400, db.width * 3 / 5);
                            int h = Math.max(300, db.height * 3 / 5);
                            dartShell.bounds = new Rectangle(dartShell.bounds.x, dartShell.bounds.y, w, h);
                        }
                    }
                }
                if (injected != null) return injected;
                return dartDisplay.displayBridge;
            }
        }
        if (injected != null) return injected;
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
                display.applyClientDeviceZoom(p.zoom);
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
     * Wires the top-level key channel for the whole-tree model. Flutter captures every keystroke
     * once at the Display root (see {@code display_evolve.dart}) and posts it here; we route it to
     * the currently focused control so it flows through the normal SWT dispatch: the Display filter
     * chain (e.g. Eclipse's command key bindings, installed via {@code Display.addFilter(SWT.KeyDown,
     * …)}) and then the focused control's own KeyDown/KeyUp listeners. This is the single generic
     * path that replaces the per-control forwarders while a Display is rendered.
     *
     * <p>A key arriving here was forwarded by nothing else — the client suppresses this channel for
     * exactly the keystrokes a focused editor forwards on its own (see {@code key_forwarding.dart})
     * — so every arrival is dispatched, whatever the focused control's type.
     */
    protected void registerDisplayKeyEvents(DartDisplay display) {
        long displayId = display.getApi().hashCode();
        comm().on("Display/" + displayId + "/Key/KeyDown", Event.class,
                ev -> routeKeyToFocused(display, org.eclipse.swt.SWT.KeyDown, ev));
        comm().on("Display/" + displayId + "/Key/KeyUp", Event.class,
                ev -> routeKeyToFocused(display, org.eclipse.swt.SWT.KeyUp, ev));
    }

    private void routeKeyToFocused(DartDisplay display, int type, Event ev) {
        Display api = display.getApi();
        if (api == null || api.isDisposed())
            return;
        // Arrives on the comm thread; hop to the Display thread like every other inbound event.
        api.asyncExec(() -> {
            if (api.isDisposed())
                return;
            Control focus = api.getFocusControl();
            if (focus == null || focus.isDisposed() || !(focus.getImpl() instanceof DartControl))
                return;
            DartControl dc = (DartControl) focus.getImpl();
            if (type == org.eclipse.swt.SWT.KeyDown) {
                boolean vetoable = focus.isListening(org.eclipse.swt.SWT.KeyDown);
                ev.doit = true;
                ControlHelper.sendDisplayRoutedKeyDown(dc, ev);
                if (vetoable) {
                    dev.equo.swt.FlutterBridge.send(dc, "key/verdict",
                            dev.equo.swt.Java8.map("doit", ev.doit));
                }
                // Surface the traversal (Tab/arrows/Esc/Enter/Page) as SWT.Traverse too, so a Display
                // Traverse filter (Eclipse command bindings) and TraverseListeners see it.
                boolean traverseDoit = ControlHelper.sendFlutterTraverse(dc, ev);
                // The client traverses at Display level, so the verdict goes there, not to the control.
                if (ControlHelper.isGatedTraversal(ev)) {
                    sendDisplayGate(display, "traverse/verdict", "doit", traverseDoit);
                }
            } else {
                dc.sendEvent(org.eclipse.swt.SWT.KeyUp, ev);
            }
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

    /**
     * The Display's entry in the shared dirty set. Equal by (bridge, display) so repeated
     * enrolments of the same Display collapse to one entry, which is what lets several state
     * changes in a turn produce one frame once these stop being flushed on the spot.
     */
    private static final class DisplayFlush implements FlutterBridge.DirtyState {

        private final DisplayBridge bridge;
        private final DartDisplay display;

        DisplayFlush(DisplayBridge bridge, DartDisplay display) {
            this.bridge = bridge;
            this.display = display;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DisplayFlush)) return false;
            DisplayFlush other = (DisplayFlush) o;
            return bridge == other.bridge && display == other.display;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(bridge) * 31 + System.identityHashCode(display);
        }

        @Override
        public boolean isStale() {
            Display api = display.getApi();
            return api == null || api.isDisposed();
        }

        @Override
        public void flush() {
            bridge.pushDisplayState(display);
        }
    }

    /**
     * Marks the Display as having state to send, then sends it.
     *
     * <p>The immediate flush keeps every caller's timing exactly as it was; the enrolment is the
     * point, putting the Display in the same dirty set as everything else so it inherits the
     * per-property delivery being built there rather than needing its own copy of it. Dropping the
     * flush is what later lets a turn's worth of changes coalesce into one frame — a separate
     * change, because it moves when a client learns, and some callers push and then block.
     */
    public void sendDisplayUpdate(DartDisplay display) {
        FlutterBridge.dirty(new DisplayFlush(this, display));
        FlutterBridge.flushDirtyStates();
    }

    void pushDisplayState(DartDisplay display) {
        // NOTE: no `clientReady` gate here. A Display update produced before the Flutter client has
        // connected (e.g. the E4 workbench shows its top-level Shell during startup, long before the
        // native window's Flutter engine connects) must still be serialized and sent — the comm layer
        // BUFFERS pre-connect frames and flushes them on connect (AbstractBinaryCommService). Skipping
        // it here left the client with no shells until a post-connect re-push, but that re-push runs on
        // the comm thread and needs the UI thread, which the idle E4 workbench traps inside the native
        // event pump — so the window stayed blank. Buffering the pre-connect state avoids that race and
        // gives Flutter content the moment it connects (which also keeps its runloop live).
        try {
            // Before the value is built: what it names as windowed has to be the windows that exist.
            syncShellWindows();
            VDisplay vd = VDisplay.of(display);
            vd.activeShellId = publishedActiveShell = activeShellId();
            vd.mainShellId = publishedMainShell = mainShellId(display);
            vd.windowedShellIds = windowedShellIds();
            serializeAndSend("Display/" + vd.id, vd);
            syncShellPopups();
            FlutterBridge.displayBootstrapped = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Slave the main shell to {@code viewport} (wrapped by the caller in {@link #applyClientBounds}
     *  so the setBounds isn't echoed to the window). Slaved regardless of origin — the Eclipse
     *  workbench opens non-origin, which an earlier atOrigin-only guard skipped. Only touch it when
     *  the geometry differs, so a no-op repeat doesn't feed the resize loop. */
    protected void resizeMainShells(DartDisplay display, Rectangle viewport) {
        Shell shell = mainShell(display);
        if (shell == null || shell.isDisposed()) {
            return;
        }
        Rectangle shellBounds = shell.getBounds();
        int x = keepsMainShellOrigin() && shellBounds != null ? shellBounds.x : 0;
        int y = keepsMainShellOrigin() && shellBounds != null ? shellBounds.y : 0;
        boolean matchesViewport =
                shellBounds != null
                        && shellBounds.width == viewport.width
                        && shellBounds.height == viewport.height;
        if (!matchesViewport) {
            shell.setBounds(x, y, viewport.width, viewport.height);
        }
    }

    /** Class name of the Eclipse e4 workbench's main-window layout, checked by name only (no hard
     *  dependency on the e4 workbench jar). Also used by {@code dev.equo.swt.Config}. */
    private static final String E4_MAIN_SHELL_LAYOUT =
            "org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout";

    /** The top-level, display-tracking shell that drives (and is slaved to) the viewport, or null.
     *  Prefers a shell laid out with {@link #E4_MAIN_SHELL_LAYOUT} over construction order; falls
     *  back to the first trackable shell when no such shell exists yet. */
    protected Shell mainShell(DartDisplay display) {
        Shell firstTrackable = null;
        for (Shell shell : display._shells()) {
            if (shell == null || shell.isDisposed() || !shouldTrackDisplayBounds(shell)) {
                continue;
            }
            // A shell already hosted in a window of its own drives that window, never this one.
            // Asked as a fact rather than through the policy, which would ask back who is main.
            if (shellWindows.containsKey(shell)) {
                continue;
            }
            Layout layout = shell.getLayout();
            if (layout != null && E4_MAIN_SHELL_LAYOUT.equals(layout.getClass().getName())) {
                return shell;
            }
            if (firstTrackable == null) {
                firstTrackable = shell;
            }
        }
        return firstTrackable;
    }

    /** Whether {@code shell} is the one {@link #mainShell} returns — the single shell that drives
     *  (and is slaved to) the native/host window. */
    protected boolean isMainShell(DartDisplay display, Shell shell) {
        return shell != null && !shell.isDisposed() && shell == mainShell(display);
    }

    /** {@link #mainShell} as the client sees it: its id, or 0 when it is not among the shells
     *  {@link VDisplay#of} sends (only visible shells are serialized, so an invisible one would
     *  name a shell the client does not have). */
    private long mainShellId(DartDisplay display) {
        Shell shell = mainShell(display);
        return (shell != null && !shell.isDisposed() && shell.getVisible()) ? shell.hashCode() : 0;
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
        if ((style & org.eclipse.swt.SWT.TOOL) != 0) {
            return false;
        }
        // Plain browser only: a lone shell with no resize trim (e.g. a fixed-size login/splash
        // screen) isn't meant to track the browser viewport -- it renders at its own declared
        // size via the floating shell chrome instead of being stretched to fill the page.
        // Desktop and the Chromium standalone surface always have a real OS/CEF window framing
        // the app, so every shell there still fills it regardless of trim.
        if (!ConfigFlags.isDesktopMode() && !ConfigFlags.isChromiumMode()
                && (style & org.eclipse.swt.SWT.RESIZE) == 0) {
            return false;
        }
        return true;
    }

    // ---- shells hosted in a window of their own (see WindowPolicy) --------------------------------

    /** Whether this surface can host a shell in a window of its own. */
    protected boolean supportsShellWindows() {
        return false;
    }

    /** Creates the window hosting {@code shell}, or null when the surface could not. */
    protected ShellWindow createShellWindow(Shell shell) {
        return null;
    }

    /** Whether {@code shell} is one {@link WindowPolicy} puts in a window of its own. */
    protected boolean ownsWindow(Shell shell) {
        return WindowPolicy.ownsWindow(shell, isMainShell(forDisplay, shell));
    }

    /** The window hosting {@code control}'s shell, or null when it is drawn inside the Display's. */
    protected ShellWindow shellWindowFor(DartControl control) {
        if (!(control instanceof DartShell dartShell)) return null;
        return shellWindows.get((Shell) dartShell.getApi());
    }

    /** Test seam: the shells currently hosted in a window of their own, in open order. */
    protected java.util.Set<Shell> windowedShells() {
        return java.util.Collections.unmodifiableSet(shellWindows.keySet());
    }

    /**
     * Brings the set of open windows in line with what the policy now says, opening one for every
     * eligible visible shell and closing the ones whose shell is gone, hidden or no longer eligible.
     *
     * <p>Driven from {@link #pushDisplayState}, so the ids the client is told about are the windows
     * that actually exist: a shell the client leaves out of its own stack must have a window to be
     * drawn in, or it would be rendered nowhere at all.
     */
    private void syncShellWindows() {
        if (forDisplay == null) return;
        // Nothing can be eligible and nothing is open: the overwhelmingly common case, skipped whole.
        if (shellWindows.isEmpty() && (!supportsShellWindows() || WindowPolicy.isSingleWindow())) return;

        shellWindows.entrySet().removeIf(entry -> {
            Shell shell = entry.getKey();
            ShellWindow window = entry.getValue();
            if (!window.isAlive()) {
                onShellWindowLost(shell);
                return true;
            }
            if (shell.isDisposed() || !ownsWindow(shell)) {
                closeShellWindow(shell, window);
                return true;
            }
            // Hidden is not gone. A toolkit hides and re-shows a shell while it lays out -- the
            // Eclipse workbench does it to a detached part -- and taking the window down for it
            // rebuilds the window on the next pass, since a window closed here is not recorded as
            // lost. That is an endless create/destroy cycle, one new engine per turn.
            window.setVisible(shell.getVisible());
            return false;
        });

        if (!supportsShellWindows()) return;
        for (Shell shell : forDisplay._shells()) {
            if (shell == null || shell.isDisposed() || !shell.getVisible()) continue;
            if (shellWindows.containsKey(shell) || lostWindows.contains(shell)) continue;
            if (!ownsWindow(shell)) continue;
            ShellWindow window = createShellWindow(shell);
            if (window == null) continue;
            shellWindows.put(shell, window);
            registerShellClientReady(shell);
        }
    }

    /**
     * Teaches {@code shell} where its window actually is.
     *
     * <p>Applied under {@link #applyClientBounds} and only on a change, which is what keeps it from
     * being echoed straight back to the OS as a move -- the resize loop with a different axis.
     */
    protected void applyWindowOrigin(Shell shell, org.eclipse.swt.graphics.Point origin) {
        if (origin == null || shell == null || shell.isDisposed()) return;
        Rectangle bounds = shell.getBounds();
        if (bounds == null || (bounds.x == origin.x && bounds.y == origin.y)) return;
        applyClientBounds(() -> shell.setBounds(origin.x, origin.y, bounds.width, bounds.height));
    }

    /**
     * The shell last published as the main one. Read rather than recomputed: working it out walks
     * every shell and asks each for its layout, and this is reached from inside that very walk
     * (a coordinate conversion during serialization), where the answer would be half-formed -- which
     * is how the workbench shell came to be treated as detached and the application drew nothing.
     */
    private volatile long publishedMainShell;

    @Override
    public boolean rendersAsMainWindow(Object shell) {
        return shell instanceof Shell s && !s.isDisposed()
                && s.hashCode() == publishedMainShell && !isShellWindowed(s);
    }

    /**
     * Whether the main shell keeps its own origin when it is slaved to the viewport.
     *
     * <p>Only where a window has a position of its own to keep. A page has none -- it <em>is</em> the
     * viewport, and its main shell belongs at (0,0) -- while a desktop window sits wherever the OS
     * put it, and pinning its shell to the origin makes every screen coordinate derived from it (a
     * popup's location, a drag's drop target) wrong by exactly that distance.
     */
    protected boolean keepsMainShellOrigin() {
        return false;
    }

    /** Whether {@code shell} is drawn in a window of its own rather than inside the Display's. */
    boolean isShellWindowed(Shell shell) {
        return shellWindows.containsKey(shell);
    }

    /**
     * The shell a popup menu was opened over, or null when it cannot be told.
     *
     * <p>The control the menu was opened for, not {@link Menu#getParent()}: a popup's parent is the
     * Decorations it was constructed against, which for a menu shared between shells is not the one
     * it is currently showing over.
     */
    static Shell popupShell(Menu menu) {
        if (menu == null || menu.isDisposed() || !(menu.getImpl() instanceof DartMenu impl)) return null;
        Control owner = impl.findOwnerControl();
        if (owner != null && !owner.isDisposed()) return owner.getShell();
        Decorations parent = menu.getParent();
        return parent == null || parent.isDisposed() ? null : parent.getShell();
    }

    /** The popups a windowed shell has to draw itself, in Display order, or an empty array. */
    private Menu[] popupsFor(Shell shell) {
        Menu[] all = forDisplay == null ? null : forDisplay.popups;
        if (all == null) return new Menu[0];
        java.util.ArrayList<Menu> mine = new java.util.ArrayList<>();
        for (Menu menu : all) {
            if (menu != null && !menu.isDisposed() && popupShell(menu) == shell) mine.add(menu);
        }
        return mine.toArray(Menu[]::new);
    }

    /** What each windowed shell was last told its popups were, so a settled set is not re-sent. */
    private final java.util.Map<Shell, java.util.List<Long>> sentShellPopups =
            new java.util.IdentityHashMap<>();

    /**
     * Hands every windowed shell the popups opened over it, on its own channel.
     *
     * <p>A popup is drawn by the client that owns the window it was opened over, and the shell's
     * client cannot learn of one any other way: popups hang off the Display, not off the shell's
     * widget tree, so nothing carries them into the shell's own frame.
     */
    private void syncShellPopups() {
        if (shellWindows.isEmpty() && sentShellPopups.isEmpty()) return;
        sentShellPopups.keySet().removeIf(shell -> !shellWindows.containsKey(shell));
        for (Shell shell : shellWindows.keySet()) {
            if (shell.isDisposed()) continue;
            Menu[] popups = popupsFor(shell);
            java.util.List<Long> ids = new java.util.ArrayList<>(popups.length);
            for (Menu menu : popups) ids.add((long) menu.hashCode());
            // Sent on every Display push otherwise, which is every frame — and a popup that is
            // still up is still in the list.
            if (ids.equals(sentShellPopups.get(shell))) continue;
            sentShellPopups.put(shell, ids);
            VShellPopups payload = new VShellPopups();
            payload.shellId = shell.hashCode();
            payload.popups = popups;
            try {
                // Described in full, not referenced: a popup the Display's client was already sent
                // counts as delivered, and this client — which has never seen it — cannot resolve a
                // reference to something it was never given.
                for (Menu menu : popups) {
                    if (menu.getImpl() instanceof DartMenu impl) {
                        dev.equo.swt.Serializer.forgetDelivery(impl.getValue());
                    }
                }
                serializeAndSend("Shell/" + payload.shellId + "/Popups", payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Shells whose window went away on its own. Kept so the pass below does not hand the shell a
     * fresh window each time — reopening a window the user just closed is the one reaction that is
     * certainly wrong.
     */
    private final java.util.Set<Shell> lostWindows =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    /**
     * The window hosting {@code shell} is gone without this side asking — the user closed it, or the
     * OS took it down. The shell has nowhere left to be drawn, so it goes with it. Posted rather than
     * run here: this is reached from a serialization flush, which is no place to dispose a widget.
     */
    private void onShellWindowLost(Shell shell) {
        if (shell.isDisposed() || !lostWindows.add(shell)) return;
        Display api = forDisplay == null ? null : forDisplay.getApi();
        if (api == null || api.isDisposed()) return;
        api.asyncExec(() -> {
            if (shell.isDisposed()) return;
            shell.close();
            // A veto has nothing left to render into, so it cannot be honoured here.
            if (!shell.isDisposed()) shell.dispose();
        });
    }

    private void closeShellWindow(Shell shell, ShellWindow window) {
        if (!shell.isDisposed()) offChannel(shellClientReadyChannel(shell));
        window.close();
    }

    /** Takes down the window hosting {@code shell}, if it has one. */
    private void closeShellWindow(Shell shell) {
        ShellWindow window = shellWindows.remove(shell);
        if (window != null && window.isAlive()) closeShellWindow(shell, window);
        lostWindows.remove(shell);
    }

    private static String shellClientReadyChannel(Shell shell) {
        return "Shell/" + shell.hashCode() + "/ClientReady";
    }

    /**
     * Wires the handshake for a shell rendered in its own window: that window reports its viewport,
     * and the shell is slaved to it exactly as the main shell is slaved to the Display's window.
     * The resize is flagged so it is not echoed back as a window resize — the same loop, one window out.
     */
    private void registerShellClientReady(Shell shell) {
        Display api = forDisplay.getApi();
        comm().on(shellClientReadyChannel(shell), ClientReadyPayload.class, p -> {
            if (p == null) return;
            Runnable apply = () -> {
                if (shell.isDisposed()) return;
                // A window of its own is a client of its own, and it has been sent nothing: it needs
                // the same configuration bootstrap the Display's client gets on its first
                // handshake, or it renders the whole shell against default theme and flags.
                broadcastSwtEvolveProperties();
                sendShellState(shell);
                if (p.width <= 0 || p.height <= 0) return;
                Rectangle current = shell.getBounds();
                if (current.width == p.width && current.height == p.height) return;
                applyClientBounds(() -> shell.setBounds(current.x, current.y, p.width, p.height));
            };
            if (api.isDisposed()) return;
            if (api.getThread() == Thread.currentThread()) apply.run();
            else api.asyncExec(apply);
        });
    }

    /**
     * Describes {@code shell} in full on its own channel, for the client that draws it in a window
     * of its own.
     *
     * <p>That client is rooted at the shell and never listens on {@code Display/{id}}, so the
     * Display frame — the only thing a shell is normally delivered inside — reaches it and is
     * dropped. Nor does asking for a refresh help: a shell delivered only inside the Display has
     * never been sent on its own channel, so the flush still counts it as new and holds it back for
     * an ancestor to carry, and the ancestor is the one frame this client ignores. Left to either of
     * those, a detached window renders nothing at all.
     *
     * <p>Delivery is forgotten first so this is a description rather than a name: whatever the shell
     * was sent before went to the client drawing the Display, and means nothing to this one.
     */
    private void sendShellState(Shell shell) {
        if (!(shell.getImpl() instanceof DartShell impl)) return;
        try {
            dev.equo.swt.Serializer.forgetDelivery(impl.getValue());
            // The api widget, not its value: that is what carries the id and the type name into the
            // frame, and a client whose only knowledge of this shell is the frame cannot decode one
            // without them. The ordinary flush sends the same thing for the same reason.
            serializeAndSend("Shell/" + shell.hashCode(), shell);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The shells the client must leave out of the Display's own window because each is drawn in a
     * window of its own. Null when there are none, so the common frame is unchanged.
     */
    private long[] windowedShellIds() {
        if (shellWindows.isEmpty()) return null;
        long[] ids = new long[shellWindows.size()];
        int n = 0;
        for (Shell shell : shellWindows.keySet()) {
            if (!shell.isDisposed() && shell.getVisible()) ids[n++] = shell.hashCode();
        }
        if (n == 0) return null;
        return n == ids.length ? ids : java.util.Arrays.copyOf(ids, n);
    }

    /** Takes every extra window down. Called with the Display, before the surface itself goes. */
    protected void closeShellWindows() {
        for (java.util.Map.Entry<Shell, ShellWindow> entry : shellWindows.entrySet()) {
            if (entry.getValue().isAlive()) closeShellWindow(entry.getKey(), entry.getValue());
        }
        shellWindows.clear();
    }

    public String registerBrowserHtml(long browserId, String html) {
        return "";
    }

    @Override
    public void initFlutterView(Composite parent, DartControl control) {
        // Bridge is initialized at Display level in initForDisplay().
    }

    /**
     * Drives the surface's event loop once per {@code DartDisplay.readAndDispatch()}. The surfaces
     * that own an event loop (Chromium / native window) override this and pump it; both call up,
     * because the work below belongs to every surface that owns a window.
     */
    public void onUpdate() {
        // Where the menu bar belongs to the OS (macOS) it is outside the Flutter tree, so nothing
        // else carries a change to it: adding a menu is as invisible as removing one. Rate-limited,
        // and a no-op wherever the menu bar belongs in the window -- see MacMenuBar#sync.
        DisplayBridgePlatform.syncMenuBar();
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
            Shell shell = (Shell) dartShell.getApi();
            forDisplay.removeShell(shell);
            // Reached while the shell is being released, before isDisposed() flips: the sync inside
            // the push below would still find it visible and eligible, and leave its window standing
            // with nothing in it. This is the point that knows the shell is going.
            closeShellWindow(shell);
            sendDisplayUpdate(forDisplay);
        }
    }

    /** Tears down the Display's surface and comm. Subclasses override to add surface-specific cleanup. */
    public void destroyDisplay() {
        closeShellWindows();
        if (comm != null) {
            comm.stop();
            comm = null;
        }
    }

    @Override
    public Object container(DartComposite parent) {
        return null;
    }

    /** Last {@link #activeShellId()} the client was told about, so a re-push only happens on a change. */
    private long publishedActiveShell;

    /**
     * The shell owning the focus control, or 0 when nothing holds focus. This is
     * {@code Display.getActiveShell()} without its "otherwise the newest visible shell" fallback:
     * that fallback would name a shell nobody activated, which is exactly the case this value exists
     * to rule out.
     */
    private long activeShellId() {
        Control focus = getFocused();
        if (focus == null || focus.isDisposed())
            return 0;
        Shell shell = focus.getShell();
        return (shell != null && !shell.isDisposed() && shell.getVisible()) ? shell.hashCode() : 0;
    }

    /**
     * Re-push the Display state when focus moves to another shell. A shell only takes keyboard focus
     * on the client once it is the active shell, and {@code Shell.open()} focuses into the new shell
     * <em>after</em> the display update that introduced it — without this the client would never hear
     * that the shell became active.
     */
    private void publishActiveShell() {
        if (forDisplay == null || activeShellId() == publishedActiveShell)
            return;
        sendDisplayUpdate(forDisplay);
        publishMenuBar();
    }

    /**
     * Hands the newly active shell's menu bar to the platform. {@code Shell.setMenuBar} only reaches
     * the Display while that shell is already the active one, which it is not yet when an
     * application builds its bar before {@code open()} — so on a platform whose menu bar is owned by
     * the OS (macOS) that first bar would never be published without this.
     *
     * <p>A shell with no menu bar of its own — every dialog — publishes nothing rather than a null:
     * on macOS the menu bar is the application's, not the window's, so it has to outlive whatever
     * dialog is in front of it.
     */
    private void publishMenuBar() {
        Shell active = forDisplay.getApi().getActiveShell();
        if (active == null || active.isDisposed())
            return;
        Menu bar = active.getMenuBar();
        if (bar != null && !bar.isDisposed())
            forDisplay.setMenuBar(bar);
    }

    /**
     * Publishes one boolean on {@code Display/{id}/<gate>}. The id must be the <em>API</em> Display's
     * hashCode: a DartDisplay is a DartDevice, so {@code FlutterBridge.id} falls through to the impl
     * object's hashCode and would address a channel the client never listens on.
     */
    private void sendDisplayGate(DartDisplay display, String gate, String key, boolean value) {
        if (display == null || display.getApi() == null)
            return;
        try {
            serializeAndSend("Display/" + display.getApi().hashCode() + "/" + gate,
                    dev.equo.swt.Java8.map(key, value));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /** Gates armed per focus. Arming keeps the round trip off the path where no veto is possible. */
    private static final class FocusGate {
        private final String channel;
        private final int eventType;
        private final Class<? extends DartControl> impl;

        FocusGate(String channel, int eventType, Class<? extends DartControl> impl) {
            this.channel = channel;
            this.eventType = eventType;
            this.impl = impl;
        }

        String channel() { return channel; }

        int eventType() { return eventType; }

        Class<? extends DartControl> impl() { return impl; }
    }

    private static final java.util.List<FocusGate> FOCUS_GATES = dev.equo.swt.Java8.list(
            new FocusGate("key", org.eclipse.swt.SWT.KeyDown, DartControl.class),
            new FocusGate("modify", org.eclipse.swt.SWT.Verify, DartText.class));

    @Override
    public boolean setFocus(DartControl widget) {
        focused = widget;
        Control api = widget == null ? null : widget.getApi();
        boolean live = api != null && !api.isDisposed();
        if (live) {
            for (FocusGate gate : FOCUS_GATES) {
                if (gate.impl().isInstance(widget) && api.isListening(gate.eventType())) {
                    dev.equo.swt.FlutterBridge.send(widget, gate.channel() + "/vetoable",
                            dev.equo.swt.Java8.map("value", true));
                }
            }
        }
        // Display-scoped, so it never hears the focus change: publish on every one, false included.
        if (forDisplay != null) {
            sendDisplayGate(forDisplay, "traverse/vetoable", "value",
                    live && api.isListening(org.eclipse.swt.SWT.Traverse));
        }
        publishActiveShell();
        requestClientFocus(widget);
        return true;
    }

    /**
     * Moves the client's keyboard focus onto {@code widget}. Tracking the focus holder in Java is not
     * enough: the render side owns the real focus, so a control Java focuses on its own -- a JFace cell
     * editor activating on Add, a part activation -- would take no keystroke until the user clicked it.
     *
     * <p>The request goes out on one Display-wide channel rather than the control's own, and is queued
     * behind any pending widget flush. A control created in this same event-loop pass has not been sent
     * yet, and only exists client-side a frame after its parent's state push, so nothing addressed to it
     * directly could be received. The client holds the request until a control claims it.
     *
     * <p>A focus that started on the client comes back through here too (the FocusIn handler tracks
     * it the same way); the cell-editor guard below is what keeps that from re-pushing anything.
     */
    private void requestClientFocus(DartControl widget) {
        Control api = widget == null ? null : widget.getApi();
        if (api == null || api.isDisposed())
            return;
        if (!isCellEditorControl(api))
            return;
        if (widget == focusRequested)
            return;
        focusRequested = widget;
        long id = FlutterBridge.id(widget);
        FlutterBridge.update().whenComplete((result, error) -> {
            try {
                serializeAndSend(FOCUS_CHANNEL, dev.equo.swt.Java8.map("id", id));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Whether {@code control} is the control a {@link org.eclipse.swt.custom.TableEditor} or
     * {@link org.eclipse.swt.custom.TreeEditor} just placed over a cell.
     *
     * <p>Moving the client's focus is deliberately limited to that case. Pushing it for every control
     * Java focuses is measurably unsafe today: the Eclipse IDE New Project wizard, driven through the
     * e2e suite, goes from 10/10 green to 5 failures with the field's keystrokes landing out of order,
     * and the cause is the focus move itself rather than the transport (a run with the transport intact
     * and only the move disabled is green). A cell editor is the case the render side genuinely cannot
     * resolve on its own -- it is created, placed and focused inside one event-loop pass, with no user
     * gesture anywhere -- so it is the one that is honoured until the general case is understood.
     */
    private static boolean isCellEditorControl(Control control) {
        Composite parent = control.getParent();
        if (parent instanceof Table && ((Table) parent).getImpl() instanceof DartTable) {
            DartTable dartTable = (DartTable) ((Table) parent).getImpl();
            for (org.eclipse.swt.custom.TableEditor editor : dartTable._editors()) {
                if (editor != null && editor.getEditor() == control)
                    return true;
            }
        }
        if (parent instanceof Tree && ((Tree) parent).getImpl() instanceof DartTree) {
            DartTree dartTree = (DartTree) ((Tree) parent).getImpl();
            for (org.eclipse.swt.custom.TreeEditor editor : dartTree._editors()) {
                if (editor != null && editor.getEditor() == control)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasFocus(DartControl widget) {
        return widget == focused;
    }

    /** The control that currently holds focus (Java-tracked; there is no OS focus on the web). */
    public org.eclipse.swt.widgets.Control getFocused() {
        return (focused != null && !focused.getApi().isDisposed()) ? focused.getApi() : null;
    }

    /** Clear the tracked focus if it points at the given control (used on focus loss/dispose). */
    public void clearFocus(DartControl widget) {
        if (focused == widget) {
            focused = null;
            publishActiveShell();
        }
        // The client moved focus off it, so focusing it again later is a real move to send.
        if (focusRequested == widget) {
            focusRequested = null;
        }
    }

    @Override
    public void setVisible(DartControl control, boolean visible) {
        if (control instanceof DartShell) {
            Shell shell = (Shell) ((DartShell) control).getApi();
            if (visible && forDisplay != null) {
                if (isMainShell(forDisplay, shell)) {
                    // Fill the viewport on show even if the shell opened at a stale, non-origin
                    // geometry persisted from a previous run (a maximized browser fires no resize).
                    applyClientBounds(() -> resizeMainShells(forDisplay, forDisplay.bounds));
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

    // Window ops (WindowBridge). A shell in a window of its own is driven through that window on
    // every surface, so it is handled here; the Display's own window is the surface's business, and
    // the surfaces that have one override these and fall back to super for the rest.
    @Override
    public void setBounds(DartControl control, Rectangle bounds) {
        // Applying a client-reported resize: the window already has this geometry, so echoing it
        // back fights the window manager -- the same loop the main shell avoids.
        if (applyingClientBounds || bounds == null) return;
        ShellWindow window = shellWindowFor(control);
        if (window != null) window.setBounds(bounds);
    }

    @Override
    public void setWindowMaximized(DartControl control, boolean maximized) {
        setShellWindowState(control, maximized, ShellWindow.STATE_MAXIMIZED);
    }

    @Override
    public void setWindowMinimized(DartControl control, boolean minimized) {
        setShellWindowState(control, minimized, ShellWindow.STATE_MINIMIZED);
    }

    @Override
    public void setWindowFullScreen(DartControl control, boolean fullScreen) {
        setShellWindowState(control, fullScreen, ShellWindow.STATE_FULLSCREEN);
    }

    private void setShellWindowState(DartControl control, boolean on, int state) {
        ShellWindow window = shellWindowFor(control);
        if (window != null) window.setState(on ? state : ShellWindow.STATE_NORMAL);
    }

    @Override
    public void setWindowTitle(DartControl control, String title) {
        ShellWindow window = shellWindowFor(control);
        if (window != null) window.setTitle(title == null ? "" : title);
    }
}

package dev.equo.swt.awt;

import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartSwingIsland;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SwingIsland;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.comm.CommService;

/**
 * {@code SWT_AWT.new_Frame} while swing-evolve's engine owns the JVM's AWT
 * ({@link Config#hasSwingEngine}).
 *
 * <p>The engine gives every AWT window a fake peer and a {@code windowId}, and mirrors its content
 * as Flutter widgets to whoever mounts a {@code SwingMirror} for that id. So the frame is a plain
 * undecorated {@link Frame}, and a {@link SwingIsland} filling the EMBEDDED composite is told the id
 * and the engine's comm port so its Flutter region can mount the mirror. A negative port means the
 * engine has no socket of its own and its traffic rides this Display's connection, through a
 * {@link SwingIslandTransport}. Nothing is blitted, and
 * none of {@link EvolveSwingHost}'s toolkit set-up applies: with fake peers AWT makes no AppKit
 * call.
 *
 * <p>Every other window the engine shows is a request to the mirror bundle on the client, which
 * may route it to a Shell of its own ({@link #SURFACE_EVENT}): Java builds that Shell, holding a
 * {@link SwingIsland} bound to the engine's existing window, and leaves its close and its teardown
 * to that bundle. A modal window's Shell is {@code APPLICATION_MODAL}, and stays on top of any modal
 * Shell opened after it (see {@link #listen}). Such a Shell stays in the Display's window whatever
 * {@link WindowPolicy} says: a Shell in a window of its own is drawn by another Flutter client,
 * which has no mirror of the engine's window to show. An application {@link WindowPolicy.Resolver} still outranks that.
 *
 * <p>Reaches the engine by reflection only. {@code swt_native} compiles against nothing of
 * swing-evolve, which a host puts on its classpath (and its {@code -javaagent}) at runtime.
 */
public final class SwingIslandHost {

    private SwingIslandHost() {}

    /** The key stock {@code SWT_AWT.getFrame(parent)} reads. */
    private static final String EMBEDDED_FRAME_KEY = "org.eclipse.swt.awt.SWT_AWT.embeddedFrame";

    /** The event the region asks on and is answered on: {@code SwingIsland/<id>/swingIsland}. */
    static final String ISLAND_EVENT = "swingIsland";

    /**
     * The mirror bundle asks for a Shell for one of the engine's windows: its windowId, the engine's
     * port, its kind ({@code frame}, {@code dialog} or {@code window}), title, resizability,
     * modality, the nearest island or Shell'd window in its owner chain ({@code hostOwnerId}, 0 for none), Java's
     * x/y, its size, and its offset from that owner ({@code dx}/{@code dy}) when known.
     */
    static final String SURFACE_EVENT = "swingIsland.surface";

    /** The mirror bundle tears a Shell down: {@code {windowId}}. */
    static final String DISPOSE_EVENT = "swingIsland.dispose";

    /** Java resized the window, and its Shell follows: {@code {windowId, w, h}}. */
    static final String RESIZE_EVENT = "swingIsland.resize";

    /** Java retitled the window, and its Shell follows: {@code {windowId, title}}. */
    static final String TITLE_EVENT = "swingIsland.title";

    /** The Shell built for a window is open: {@code {windowId}}, to the bundle. */
    static final String OPENED_EVENT = "swingIsland.opened";

    /** The person closed a Shell, which Java's window decides: {@code {windowId}}, to the bundle. */
    static final String CLOSE_EVENT = "swingIsland.close";

    /**
     * Java's answer to a Shell request for a window that already has a live island:
     * {@code {windowId}}, to the bundle.
     *
     * <p><b>Why silence was the wrong answer, measured.</b> Refusing to build the Shell and saying
     * nothing looked safe — the client gives every request a deadline and declines an unanswered
     * one, the same as for a Shell Java cannot build for any other reason. But declining is not
     * "nothing happens": a declined window is drawn <em>in-view</em>, inside the box of whichever
     * island is live. So the worst possible answer for a window that Java knows is an island was
     * the one silence produced.
     *
     * <p>Reproduced in a snippet with four islands in two tab stacks: only the visible tab's region
     * exists on the client, so three windows never register as islands, their Shell requests are
     * refused here, and five seconds later all three are drawn inside the one visible island —
     * three views stacked in one pane, each answering input, all moving together. The same shape
     * had been measured on an application as "the same view drawn in two places", and
     * the number of refusals there varied per boot because it depends on how many regions register
     * before the client's own two-second wait runs out.
     */
    static final String IS_ISLAND_EVENT = "swingIsland.island";

    /** The Display's islands and Shell'd windows, by windowId. */
    private static final String ISLANDS_KEY = SwingIslandHost.class.getName() + ".islands";

    /** The Display's Shells built for the mirror bundle, by windowId. */
    private static final String SURFACES_KEY = SwingIslandHost.class.getName() + ".surfaces";

    /** Set on a Display whose comm answers the bundle's Shell requests. */
    private static final String LISTENING_KEY = SwingIslandHost.class.getName() + ".listening";

    public static Frame newFrame(Composite parent) {
        if (parent == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        parent.setLayout(new FillLayout());
        SwingIsland child = new SwingIsland(parent, SWT.NONE);
        if (!(child.getImpl() instanceof DartSwingIsland)) {
            child.dispose();
            throw new IllegalStateException("swing-evolve's engine owns AWT but " + parent
                    + " is not a Dart widget, so it cannot host a Swing island");
        }
        parent.layout(true);

        // Shown on the calling (SWT) thread, where the blit path built its frame too. It opens at
        // the island's current size, 0x0 before layout: the mirror reports its real box on its
        // first layout and Java adopts it.
        Frame frame = new Frame();
        frame.setUndecorated(true);
        EvolveSwingHost.sizeFrame(frame, child.getClientArea());
        frame.setVisible(true);

        CommService comm = FlutterBridge.commFor(child.getImpl());
        int[] window = describe(frame, comm);
        listen(parent.getDisplay(), comm);
        mount(child, window[0], window[1], false);

        // The child goes with the EMBEDDED composite, so this covers the parent's dispose too.
        child.addListener(SWT.Dispose, e -> {
            frame.dispose();
            dropFromAwtShutdown(frame);
        });
        parent.setData(EMBEDDED_FRAME_KEY, frame);
        return frame;
    }

    /**
     * Drops a disposed island frame from {@code sun.awt.AWTAutoShutdown}'s peer map.
     *
     * <p>swing-evolve's engine gives every AWT window a fake peer and registers it with
     * AWTAutoShutdown — so the JVM stays alive while a window is shown — but never unregisters it,
     * on hide or on dispose. A disposed frame's stale registration keeps the peer map non-empty, so
     * the non-daemon AWT event thread never auto-shuts-down and the JVM does not exit once the
     * Display is gone. Remove the entry here, the way the engine's peer would.
     *
     * <p>Reflective because AWTAutoShutdown's {@code getPeer}/{@code unregisterPeer} are
     * package-private; the engine's own launch flags open {@code sun.awt}. Fails closed: if it
     * cannot reach them the JVM lingers exactly as it did before, no worse.
     */
    private static void dropFromAwtShutdown(Frame frame) {
        try {
            Class<?> autoShutdown = Class.forName("sun.awt.AWTAutoShutdown");
            Object instance = autoShutdown.getMethod("getInstance").invoke(null);
            Method getPeer = autoShutdown.getDeclaredMethod("getPeer", Object.class);
            getPeer.setAccessible(true);
            Object peer = getPeer.invoke(instance, frame);
            if (peer == null) return;
            Method unregisterPeer = autoShutdown.getDeclaredMethod("unregisterPeer", Object.class, Object.class);
            unregisterPeer.setAccessible(true);
            unregisterPeer.invoke(instance, frame, peer);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unreachable) {
            // Without the hook AWT shuts down (or not) exactly as it would have.
        }
    }

    /**
     * Tells {@code child}'s region which window it mirrors: {@code windowId} on the engine at
     * {@code port}, and with {@code surface} that the window has a Shell of its own rather than
     * being an island.
     */
    static void mount(SwingIsland child, int windowId, int port, boolean surface) {
        Map<Integer, SwingIsland> islands = registry(child.getDisplay(), ISLANDS_KEY);
        if (!mountable(islands, child, windowId, surface)) {
            return;
        }
        DartSwingIsland island = (DartSwingIsland) child.getImpl();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("windowId", windowId);
        info.put("port", port);
        if (surface) info.put("surface", true);
        // The region asks when it mounts (it may not exist yet, and a push nobody listens to is
        // dropped) and is answered on its own channel; the push covers a region already there.
        FlutterBridge.onPayload(island, ISLAND_EVENT, p -> {
            FlutterBridge.send(island, ISLAND_EVENT, info);
            sayWhereItIs(child, windowId);
        });
        FlutterBridge.send(island, ISLAND_EVENT, info);

        islands.put(windowId, child);
        child.addListener(SWT.Dispose, e -> islands.remove(windowId, child));
        say("island " + idOf(child) + " mounts window " + windowId
                + (surface ? " as a Shell" : ""));
    }

    /**
     * Whether this island may be told it mirrors {@code windowId} — the two ways that answer can be
     * wrong, both of which used to pass silently.
     *
     * <p><b>A windowId of 0 is not a window.</b> {@code WindowIds.of} answers 0, never a valid id,
     * for a window swing-evolve's toolkit has not peered, and {@link #describe} passed whatever it
     * got straight through. A region told 0 asks the engine to mirror a window that does not exist.
     *
     * <p><b>And two live islands may not share one window.</b> The registry below is keyed by
     * windowId, so a second mount used to overwrite the first entry while <em>both</em> regions had
     * already been sent the same id — and both then drew the same view, each inside its own pane.
     * Measured on an application with three editor parts, each hosting an island: three
     * {@code SwingIsland} widgets, two registered windows, and one view rendered live in two panes
     * at once — the application's own toolbar buttons still driving the right view while its
     * pixels appeared in someone else's pane.
     *
     * <p>Refusing the second is safe by the router's own rule rather than by hope: a surface is
     * requested only for a window that is <em>not</em> an island ({@code _IslandRouter.answer}
     * answers {@code island} and asks for nothing when it is), so a window with a live island never
     * legitimately acquires a second one.
     */
    private static boolean mountable(Map<Integer, SwingIsland> islands, SwingIsland child,
            int windowId, boolean surface) {
        if (windowId <= 0) {
            say("refusing to mount island " + idOf(child) + " on windowId " + windowId
                    + ": swing-evolve's engine has not peered that window, so there is nothing "
                    + "to mirror");
            return false;
        }
        SwingIsland held = islands.get(windowId);
        if (held != null && held != child && !held.isDisposed()) {
            say("window " + windowId + " is already mirrored by live island " + idOf(held)
                    + "; refusing to mount it on island " + idOf(child) + ", asked for as "
                    + (surface ? "a Shell the client requested" : "an embedded island")
                    + ", which would draw the same view in two places");
            return false;
        }
        return true;
    }

    /**
     * Whether a Shell may be built for {@code windowId}, asked <em>before</em> anything is built.
     *
     * <p>{@link #mount} refuses a window that already has a live island, and refusing it there was
     * half a fix: the Shell, its {@link SwingIsland} and its region had already been built,
     * parented and opened, and the region was then never told which window it mirrors. Measured on
     * the application this was found on: one
     * {@code No handler for: SwingIsland/<id>/swingIsland} per refused island, naming exactly the
     * ids the refusal lines named, with nothing else in either log mentioning them — a region on
     * the client asking on a channel the engine will never answer. The refusal belongs here, where
     * nothing has been built yet.
     *
     * <p>Nothing is sent back, and that is the client's own design rather than an omission: it gave
     * the request a deadline ({@code shellOpenWait}), and an unanswered one is declined and its
     * Shell asked for by id — the same path as a Shell Java could not build for any other reason.
     */
    private static boolean buildable(Map<Integer, SwingIsland> islands, int windowId,
            CommService comm) {
        if (windowId <= 0) {
            // Nothing to say back: there is no island either, so the client declining it and
            // drawing it in-view is the best available answer rather than the wrong one.
            say("refusing to build a Shell for windowId " + windowId
                    + ": swing-evolve's engine has not peered that window, so there is nothing "
                    + "to mirror");
            return false;
        }
        SwingIsland held = islands.get(windowId);
        if (held != null && !held.isDisposed()) {
            say("window " + windowId + " is already mirrored by live island " + idOf(held)
                    + "; refusing to build the Shell the client asked for, and telling it the "
                    + "window is an island so it is not drawn inside another one");
            FlutterBridge.send(comm, IS_ISLAND_EVENT, Map.of("windowId", windowId));
            return false;
        }
        return true;
    }

    /**
     * The island's widget id — the same number the client's channel is named after, so a line here
     * and a {@code SwingIsland/<id>} on that side are about the same widget.
     *
     * <p>Without it the census says how many islands mounted and which windows they took, and
     * cannot say <em>which island</em> took which — which is the question left when the counts are
     * right and the panes are still crossed.
     */
    private static int idOf(SwingIsland island) {
        return island == null ? 0 : island.hashCode();
    }

    /**
     * Says which island answered for which window, and where that island is on screen.
     *
     * <p>A census of mounts says how many islands there are and which windows they took. It cannot
     * say which <em>pane</em> each one is in, which is the question left when the counts are right
     * and the views are still drawn in the wrong places — and no amount of client log answers it
     * either, because the client knows a region's box and not the window behind it. Java knows
     * both. The island's display bounds here are in the same coordinates a person reads off the
     * screen, so an island, a window and a pane become one line.
     *
     * <p>On the asking side rather than at mount: at mount the island has not been laid out and its
     * box is the 1x1 the frame opened at. Deferred onto the Display, because the ask arrives on the
     * comm's thread and a widget's bounds may only be read on the thread that owns it.
     */
    private static void sayWhereItIs(SwingIsland island, int windowId) {
        Display display = island.getDisplay();
        onDisplay(display, () -> {
            if (island.isDisposed()) return;
            Rectangle box = island.getBounds();
            Point at = island.getParent() == null ? new Point(box.x, box.y)
                    : island.getParent().toDisplay(box.x, box.y);
            // A region asks as soon as it mounts, which is before the layout that gives it a box,
            // so the first answer for a window is routinely 0x0 at wherever its unlaid-out parent
            // happens to sit. Saying so costs a clause and stops a reader concluding that the
            // window has no pane at all -- which is exactly what two readers did conclude, from
            // the same 0x0 line, before a later answer for the same window landed on a real pane.
            String laid = box.width <= 0 || box.height <= 0
                    ? " -- not laid out yet, so this is not where it will be"
                    : "";
            say("island " + idOf(island) + " answers for window " + windowId + ", at " + at.x + ","
                    + at.y + " " + box.width + "x" + box.height + " on screen, inside "
                    + island.getParent() + laid);
        });
    }

    /** Said out loud, because every one of these is a window appearing or failing to. */
    private static void say(String what) {
        System.err.println("[swing-island] " + what);
    }

    /**
     * Answers the mirror bundle's Shell requests on {@code comm}, once per Display.
     *
     * <p>Also raises a modal window's Shell back over a modal Shell opened after it: the newest modal
     * Shell is drawn on top with the client's modal scrim under it, and would cover a window that
     * AWT, not SWT, blocks.
     */
    static void listen(Display display, CommService comm) {
        if (display.getData(LISTENING_KEY) != null) return;
        display.setData(LISTENING_KEY, Boolean.TRUE);
        display.addFilter(SWT.Show, e -> {
            if (e.widget instanceof Shell && isModal((Shell) e.widget)) {
                Shell shown = (Shell) e.widget;
                // Deferred until open() has activated and focused it.
                onDisplay(display, () -> raiseModalSurfaceOver(display, shown));
            }
        });
        comm.on(SURFACE_EVENT, Object.class, p -> {
            if (p instanceof Map) onDisplay(display, () -> openSurface(display, comm, (Map<?, ?>) p));
        });
        comm.on(DISPOSE_EVENT, Object.class, p -> {
            if (p instanceof Map) onDisplay(display, () -> {
                Shell shell = surfaceShell(display, intOf((Map<?, ?>) p, "windowId"));
                if (shell != null) shell.dispose();
            });
        });
        comm.on(RESIZE_EVENT, Object.class, p -> {
            if (p instanceof Map) onDisplay(display, () -> {
                Map<?, ?> resize = (Map<?, ?>) p;
                Shell shell = surfaceShell(display, intOf(resize, "windowId"));
                if (shell != null) setClientSize(shell, intOf(resize, "w"), intOf(resize, "h"));
            });
        });
        comm.on(TITLE_EVENT, Object.class, p -> {
            if (p instanceof Map) onDisplay(display, () -> {
                Map<?, ?> title = (Map<?, ?>) p;
                Shell shell = surfaceShell(display, intOf(title, "windowId"));
                if (shell != null) shell.setText(textOf(title, "title"));
            });
        });
    }

    private static boolean isModal(Shell shell) {
        return (shell.getStyle() & (SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL)) != 0;
    }

    /** Raises the newest open modal Shell built for a window, which {@code shown}, just opened, covers. */
    private static void raiseModalSurfaceOver(Display display, Shell shown) {
        if (shown.isDisposed() || !shown.isVisible()) return;
        Map<Integer, Shell> surfaces = registry(display, SURFACES_KEY);
        if (surfaces.containsValue(shown)) return;
        Shell covered = null;
        for (Shell shell : display.getShells()) {
            if (surfaces.containsValue(shell) && !shell.isDisposed() && shell.isVisible() && isModal(shell)
                    && !isDescendant(shown, shell)) {
                covered = shell;
            }
        }
        if (covered != null) covered.setActive();
    }

    private static boolean isDescendant(Shell shell, Shell ancestor) {
        for (Composite c = shell.getParent(); c != null; c = c.getParent()) {
            if (c == ancestor) return true;
        }
        return false;
    }

    private static void setClientSize(Shell shell, int width, int height) {
        Rectangle area = shell.getClientArea();
        if (area.width == width && area.height == height) return;
        Rectangle trim = shell.computeTrim(0, 0, width, height);
        shell.setSize(trim.width, trim.height);
    }

    /** The Shell built for {@code windowId}, or null. */
    static Shell surfaceShell(Display display, int windowId) {
        Map<Integer, Shell> surfaces = registry(display, SURFACES_KEY);
        Shell shell = surfaces.get(windowId);
        return shell == null || shell.isDisposed() ? null : shell;
    }

    private static void onDisplay(Display display, Runnable task) {
        if (!display.isDisposed()) display.asyncExec(() -> {
            if (!display.isDisposed()) task.run();
        });
    }

    private static void openSurface(Display display, CommService comm, Map<?, ?> request) {
        int windowId = intOf(request, "windowId");
        Shell previous = surfaceShell(display, windowId);
        if (previous != null) previous.dispose();

        Map<Integer, SwingIsland> islands = registry(display, ISLANDS_KEY);
        if (!buildable(islands, windowId, comm)) return;
        SwingIsland owner = islands.get(intOf(request, "hostOwnerId"));
        if (owner != null && owner.isDisposed()) owner = null;
        Shell parent = owner != null ? owner.getShell() : display.getActiveShell();
        String kind = String.valueOf(request.get("kind"));
        boolean resizable = Boolean.TRUE.equals(request.get("resizable"));
        Shell shell;
        if ("frame".equals(kind)) {
            shell = new Shell(display, resizable ? SWT.SHELL_TRIM : SWT.SHELL_TRIM & ~(SWT.RESIZE | SWT.MAX));
        } else {
            // Swing's document and toolkit modality collapse to APPLICATION_MODAL.
            int style = "dialog".equals(kind)
                    ? SWT.DIALOG_TRIM | (resizable ? SWT.RESIZE : 0)
                            | (Boolean.TRUE.equals(request.get("modal")) ? SWT.APPLICATION_MODAL : 0)
                    : SWT.NO_TRIM;
            shell = parent != null ? new Shell(parent, style) : new Shell(display, style);
        }
        shell.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
        shell.setText(textOf(request, "title"));
        shell.setLayout(new FillLayout());
        SwingIsland island = new SwingIsland(shell, SWT.NONE);

        setClientSize(shell, intOf(request, "w"), intOf(request, "h"));
        shell.setLocation(locationOf(display, request, kind, owner, parent, shell.getSize()));

        Map<Integer, Shell> surfaces = registry(display, SURFACES_KEY);
        surfaces.put(windowId, shell);
        shell.addListener(SWT.Dispose, e -> surfaces.remove(windowId, shell));
        // Java's window decides its own close: a DO_NOTHING_ON_CLOSE one stays, a validating one
        // may refuse. Only the mirror bundle's teardown, once Java has hidden it, disposes the Shell.
        shell.addListener(SWT.Close, e -> {
            e.doit = false;
            FlutterBridge.send(comm, CLOSE_EVENT, Map.of("windowId", windowId));
        });
        mount(island, windowId, intOf(request, "port"), true);
        shell.layout(true);
        shell.open();
        FlutterBridge.send(comm, OPENED_EVENT, Map.of("windowId", windowId));
    }

    /**
     * A top-level frame keeps its own position, which is in the Evolve view's coordinates; a window
     * whose offset from its island or Shell'd owner is known keeps that offset; any other is
     * centred on its parent.
     */
    private static Point locationOf(Display display, Map<?, ?> request, String kind, SwingIsland owner,
            Shell parent, Point size) {
        if ("frame".equals(kind)) return viewToDisplay(display, intOf(request, "x"), intOf(request, "y"));
        if (owner != null && request.get("dx") instanceof Number && request.get("dy") instanceof Number) {
            return owner.toDisplay(intOf(request, "dx"), intOf(request, "dy"));
        }
        if (parent == null) return viewToDisplay(display, intOf(request, "x"), intOf(request, "y"));
        Rectangle bounds = parent.getBounds();
        return new Point(bounds.x + (bounds.width - size.x) / 2, bounds.y + (bounds.height - size.y) / 2);
    }

    /**
     * The Display location of a point at ({@code x}, {@code y}) in the Evolve view. The view is the
     * window that draws inline Shells; a floating Shell's bounds are read as a screen position from
     * which the client subtracts that window's origin (its {@code WindowOriginScope}). That origin is
     * the main Shell's bounds, which is the Display's (0, 0) on web and the window's screen position on
     * desktop; reading the main Shell's own location gives the exact value the client subtracts, so the
     * two cancel and the Shell lands at ({@code x}, {@code y}) inside the view. No main Shell (e.g. a
     * headless test) leaves the request's coordinates as-is.
     */
    private static Point viewToDisplay(Display display, int x, int y) {
        Shell main = mainShell(display);
        if (main == null) return new Point(x, y);
        Point origin = main.getLocation();
        return new Point(origin.x + x, origin.y + y);
    }

    /** The Shell the client draws its viewport as, as the bridge that owns it reports it, or null. */
    private static Shell mainShell(Display display) {
        for (Shell shell : display.getShells()) {
            if (shell.isDisposed() || !(shell.getImpl() instanceof DartControl)) continue;
            // The widget's own bridge in production, the injected one under test -- the order commFor uses.
            FlutterBridge bridge = ((DartControl) shell.getImpl()).getBridge();
            if (bridge == null) bridge = FlutterBridge.injected();
            if (bridge != null && Boolean.TRUE.equals(bridge.hostsAsMainShell(shell))) return shell;
        }
        return null;
    }

    private static String textOf(Map<?, ?> request, String key) {
        Object value = request.get(key);
        return value == null ? "" : value.toString();
    }

    private static int intOf(Map<?, ?> request, String key) {
        Object value = request.get(key);
        return value instanceof Number ? (int) Math.round(((Number) value).doubleValue()) : 0;
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<Integer, T> registry(Display display, String key) {
        Object registry = display.getData(key);
        if (registry == null) {
            registry = new HashMap<Integer, T>();
            display.setData(key, registry);
        }
        return (Map<Integer, T>) registry;
    }

    /** The frame's {@code windowId} and the engine's comm port, which is all the region needs. */
    private static int[] describe(Frame frame, CommService displayComm) {
        EngineCalls calls = EngineCalls.INSTANCE;
        if (calls.unavailable != null) throw new IllegalStateException(calls.unavailable, calls.cause);
        try {
            int windowId = (Integer) calls.windowIdOf.invoke(null, frame);
            Object engine = calls.start.invoke(null);
            Object comm = calls.comm.invoke(engine);
            int port = (Integer) calls.getPort.invoke(comm);
            if (port < 0 && !carriedBy(ServiceLoader.load(SwingIslandTransport.class,
                    SwingIslandHost.class.getClassLoader()), displayComm)) {
                throw new IllegalStateException("swing-evolve's engine has no socket of its own and no "
                        + SwingIslandTransport.class.getName() + " carries its traffic over this Display's connection");
            }
            return new int[] {windowId, port};
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(NOT_CALLABLE, e);
        }
    }

    /** Whether one of {@code transports} takes {@code displayComm} for the engine's traffic. */
    static boolean carriedBy(Iterable<SwingIslandTransport> transports, CommService displayComm) {
        for (SwingIslandTransport transport : transports) {
            if (transport.attach(displayComm)) return true;
        }
        return false;
    }

    private static final String NOT_CALLABLE = "swing-evolve's engine is present but not callable";

    /** The engine's entry points, looked up once, on the first frame. */
    private static final class EngineCalls {
        static final EngineCalls INSTANCE = new EngineCalls();

        Method windowIdOf, start, comm, getPort;
        /** Why the engine cannot be called, or null. */
        String unavailable;
        Throwable cause;

        private EngineCalls() {
            Class<?> engineClass = Config.swingEngineClass("dev.equo.swing.engine.Engine");
            Class<?> commClass = Config.swingEngineClass("dev.equo.swing.bridge.comm.CommService");
            if (engineClass == null || commClass == null) {
                unavailable = "swing-evolve's engine classes are not loadable";
                return;
            }
            try {
                windowIdOf = engineClass.getMethod("windowIdOf", Window.class);
                start = engineClass.getMethod("start");
                comm = engineClass.getMethod("comm");
                getPort = commClass.getMethod("getPort");
            } catch (ReflectiveOperationException e) {
                unavailable = NOT_CALLABLE;
                cause = e;
            }
        }
    }
}

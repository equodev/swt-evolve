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
        // the island's current size, 1x1 before layout: the mirror reports its real box on its
        // first layout and Java adopts it.
        Frame frame = new Frame();
        frame.setUndecorated(true);
        Rectangle area = child.getClientArea();
        frame.setSize(Math.max(1, area.width), Math.max(1, area.height));
        frame.setVisible(true);

        CommService comm = FlutterBridge.commFor(child.getImpl());
        int[] window = describe(frame, comm);
        listen(parent.getDisplay(), comm);
        mount(child, window[0], window[1], false);

        // The child goes with the EMBEDDED composite, so this covers the parent's dispose too.
        child.addListener(SWT.Dispose, e -> frame.dispose());
        parent.setData(EMBEDDED_FRAME_KEY, frame);
        return frame;
    }

    /**
     * Tells {@code child}'s region which window it mirrors: {@code windowId} on the engine at
     * {@code port}, and with {@code surface} that the window has a Shell of its own rather than
     * being an island.
     */
    static void mount(SwingIsland child, int windowId, int port, boolean surface) {
        DartSwingIsland island = (DartSwingIsland) child.getImpl();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("windowId", windowId);
        info.put("port", port);
        if (surface) info.put("surface", true);
        // The region asks when it mounts (it may not exist yet, and a push nobody listens to is
        // dropped) and is answered on its own channel; the push covers a region already there.
        FlutterBridge.onPayload(island, ISLAND_EVENT, p -> FlutterBridge.send(island, ISLAND_EVENT, info));
        FlutterBridge.send(island, ISLAND_EVENT, info);

        Map<Integer, SwingIsland> islands = registry(child.getDisplay(), ISLANDS_KEY);
        islands.put(windowId, child);
        child.addListener(SWT.Dispose, e -> islands.remove(windowId, child));
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

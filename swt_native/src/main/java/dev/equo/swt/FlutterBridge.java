package dev.equo.swt;

import dev.equo.swt.comm.BinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.JettyBinaryCommService;
import dev.equo.swt.spi.FlutterBridgeSpi;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.equo.swt.Config.getConfigFlags;

public abstract class FlutterBridge {
    private static final String DEV_EQU_SWT_NEW = "dev.equo.swt.new";
    protected static final Serializer serializer = new Serializer();
    private static final Set<Object> dirty = new HashSet<>();
    private static FlutterBridge bridge;
    private static boolean keepClient = false;

    /**
     * True once Flutter has the widget tree (first ClientReady / Display update). Until then,
     * blocking round-trips (e.g. {@code GCHelper.callOnDisplayBytes}) have no listener on the far side and
     * would just time out, so callers return their fallback immediately instead of dead-waiting.
     */
    public static volatile boolean displayBootstrapped = false;

    /** Registered only from {@code DisplayBridge}; stays unset in embed builds. */
    public interface DisplayGcCommResolver {
        CommService resolve(Display display);
    }

    private static volatile DisplayGcCommResolver displayGcCommResolver;

    public static void setDisplayGcCommResolver(DisplayGcCommResolver resolver) {
        displayGcCommResolver = resolver;
    }

    /** The Display's shared GC comm, or {@code null} in embed mode or once the Display is gone. */
    public static CommService resolveDisplayGcComm(Display display) {
        DisplayGcCommResolver resolver = displayGcCommResolver;
        return (resolver != null && display != null) ? resolver.resolve(display) : null;
    }

    /**
     * Desktop / default comm. Lazily created on first use and shared by every desktop bridge — there
     * is one Flutter engine per JVM there. {@code null} until first needed, and never created on web
     * (where {@link #comm()} is overridden per Display), so the unused desktop server isn't started.
     */
    private static volatile CommService desktopComm;

    /**
     * Creates a fresh comm (transport chosen by {@code -Dcomm.impl}) and wires the inbound
     * property-set channel to it. Each comm is independent, so web Displays each get their own.
     */
    protected static CommService newComm() {
        String impl = System.getProperty("comm.impl", "java-websocket");
        CommService comm = "jetty".equals(impl) ? new JettyBinaryCommService() : new BinaryCommService();
        comm.on("swt.evolve.property.set", ConfigFlags.class, parsed -> handlePropertySetFromFlutter(comm, parsed));
        return comm;
    }

    /** The shared desktop comm, created (and started) on first access. */
    protected static CommService desktopComm() {
        CommService c = desktopComm;
        if (c == null) {
            synchronized (FlutterBridge.class) {
                c = desktopComm;
                if (c == null) {
                    c = newComm();
                    desktopComm = c;
                }
            }
        }
        return c;
    }

    /**
     * The comm this bridge talks through. Desktop bridges share {@link #desktopComm()}; the web
     * bridge overrides this to return a comm created once per {@link Display}.
     */
    protected CommService comm() {
        return desktopComm();
    }

    /**
     * Resolves the comm a widget/resource should talk through. The comm is owned by the Display, so
     * on web every widget under a Display resolves (via its display bridge) to that Display's comm,
     * while desktop widgets share {@link #desktopComm()}. Tries, in order: the widget's own bridge,
     * the globally-injected bridge (used by tests, where the per-widget bridge may be a stub whose
     * {@code comm()} is null), then the desktop comm.
     */
    public static CommService commFor(Object w) {
        CommService c = commOf(getBridge(w));
        if (c == null) c = commOf(bridge);
        return c != null ? c : desktopComm();
    }

    private static CommService commOf(FlutterBridge b) {
        return b != null ? b.comm() : null;
    }

    private static void handlePropertySetFromFlutter(CommService comm, ConfigFlags parsed) {
        if (parsed == null) return;
        boolean changed = false;
        ConfigFlags current = getConfigFlags();
        changed |= applyStringField("force_theme", current.force_theme, parsed.force_theme, v -> current.force_theme = v);
        changed |= applyStringField("theme_name", current.theme_name, parsed.theme_name, v -> current.theme_name = v);
        changed |= applyStringField("theme_color", current.theme_color, parsed.theme_color, v -> current.theme_color = v);
        if (changed) {
            // Echo the updated properties back on the comm they arrived on.
            try {
                serializeAndSend(comm, "swt.evolve.properties", getConfigFlags());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean applyStringField(String name, String oldVal, String newRaw, Consumer<String> setter) {
        if (newRaw == null) return false;
        String trimmed = newRaw.trim();
        String newVal = trimmed.isEmpty() ? null : trimmed;
        if (Objects.equals(oldVal, newVal)) return false;
        System.setProperty("swt.evolve." + name, trimmed);
        setter.accept(newVal);
        return true;
    }

    protected final CompletableFuture<Boolean> clientReady = new CompletableFuture<>();

    public static void disposeClient() {
        // Desktop only: stop the shared comm if it was ever created. On web each Display stops its
        // own comm in WebDisplayBridge.destroyDisplay(), and desktopComm stays null here.
        if (!keepClient && desktopComm != null)
            desktopComm.stop();
        keepClient = false;
    }

    public static void disposeDisplayAndContinue(Display display) {
        keepClient = true;
        display.dispose();
    }

    protected FlutterBridge() {
    }

    protected DartWidget forWidget() {
        return null;
    }

    static Set<Object> filterWidgetsWithDirtyAncestors(Set<Object> dirtySet) {
        Set<Object> filtered = new HashSet<>();

        for (Object widget : dirtySet) {
            if (isFlutterRoot(widget) || isShell(widget) || !hasAncestorInSet(widget, dirtySet)) {
                filtered.add(widget);
            }
        }

        return filtered;
    }

    private static boolean isShell(Object widget) {
        return widget instanceof DartControl d && d.getApi() instanceof Shell;
    }

    private static boolean isFlutterRoot(Object widget) {
        if (widget instanceof DartControl d) {
            FlutterBridge bridge = d.getBridge();
            return bridge != null && bridge.forWidget() == widget;
        }
        return false;
    }

    static boolean hasAncestorInSet(Object widget, Set<Object> dirtySet) {
        Object parent = getParent(widget);

        while (parent != null) {
            if (dirtySet.contains(parent)) {
                return true;
            }
            parent = getParent(parent);
        }

        return false;
    }

    static Object getParent(Object obj) {
        if (obj instanceof DartControl && !((DartControl) obj).isDisposed()) {
            Composite parent = ((DartControl) obj).getParent();
            return (parent != null && parent.getImpl() instanceof DartWidget) ? parent.getImpl() : null;
        } else {
            return null;
        }
    }

    public static CompletableFuture<Void> update() {
        if (dirty.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Set<Object> dirtySnapshot;
        synchronized (dirty) {
            dirtySnapshot = new HashSet<>(dirty);
        }
        Set<Object> filteredDirty = filterWidgetsWithDirtyAncestors(dirtySnapshot);

        for (Object widget : dirtySnapshot) {
            if (!filteredDirty.contains(widget)) {
                setNotNew(widget);
            }
        }

        for (Object widget : filteredDirty) {
            if (isDisposed(widget)) continue;
            Runnable send = () -> {
                try {
                    if (isDisposed(widget)) return; // widget may have been disposed while waiting for clientReady
                    boolean isHidden = (widget instanceof org.eclipse.swt.widgets.DartControl dc) && !dc.getVisible();
                    if (!isNew(widget) || widget instanceof DartToolTip || widget instanceof DartMenu || isHidden) { // send with the parent
                        setNotNew(widget);
                        synchronized (dirty) { // undirty if it was dirtied while waiting foe clientReady
                            dirty.remove(widget);
                        }
                        String event = event(widget);
                        try {
                            serializeAndSend(commFor(widget), event, getApi(widget));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        setNotNew(widget);
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            };
            CompletableFuture<Void> future = getBridge(widget).clientReady.thenRun(() -> runOnDisplayThread(widget, send));
            futures.add(future);
        }
        synchronized (dirty) {
            dirty.clear();
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static boolean isNew(Object widget) {
        if (widget instanceof DartWidget)
            return ((DartWidget) widget).getData(DEV_EQU_SWT_NEW) == null;
        return false;
    }

    private static boolean isDisposed(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).isDisposed();
        if (w instanceof DartResource) return ((DartResource) w).isDisposed();
        return true;
    }

    private static FlutterBridge getBridge(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getBridge();
        if (w instanceof DartResource) return ((DartResource) w).getBridge();
        return null;
    }

    private static Display getDisplay(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getDisplay();
        if (w instanceof DartGC) return ((DartGC) w).getDisplay();
        return null;
    }

    /**
     * Runs {@code task} on {@code widget}'s Display thread. {@link #clientReady} is completed from
     * {@link #onClientReady}, which runs on the comm (network) thread — a callback chained onto it
     * (e.g. {@link #update()}'s per-widget send) that touches widget state (getters like
     * {@code Control#isEnabled()}) must hop back to the Display thread first, or it throws
     * {@code SWTException: Invalid thread access}. No-op hop when already on the right thread or
     * when no Display can be resolved (unchanged, synchronous behavior in both cases).
     */
    private static void runOnDisplayThread(Object widget, Runnable task) {
        Display display = getDisplay(widget);
        if (display == null || display.isDisposed() || display.getThread() == Thread.currentThread()) {
            task.run();
            return;
        }
        display.asyncExec(task);
    }

    private static Object getApi(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getApi();
        if (w instanceof DartResource) return ((DartResource) w).getApi();
        return null;
    }

    /** Serializes through this bridge's own {@link #comm()} — the form instance callers use. */
    protected void serializeAndSend(String eventName, Object args) throws IOException {
        serializeAndSend(comm(), eventName, args);
    }

    private static void serializeAndSend(CommService comm, String eventName, Object args) throws IOException {
        byte[] bytes = serializer.to(args);
        DebugLog.logSend(eventName, bytes);
        comm.send(eventName, bytes);
    }

    private static void setNotNew(Object control) {
        if (control instanceof DartWidget)
            ((DartWidget) control).setData(DEV_EQU_SWT_NEW, false);
    }

    public static void set(FlutterBridge staticBridge) {
        bridge = staticBridge;
    }

    /**
     * The globally-injected bridge, or {@code null} in production. A test/bench harness injects one
     * (via {@link #set}) before any Display is created; when present it owns the comm + client and
     * every widget routes through it (see {@link #of}), so per-Display bridges must not be created.
     */
    public static FlutterBridge injected() {
        return bridge;
    }

    public static void on(DartWidget widget, String listener, String event, Consumer<Event> cb) {
        String eventName = event(widget, listener, event);
        commFor(widget).on(eventName, Event.class, ev -> {
            if (widget.isDisposed()) return;
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                System.out.println(eventName + ", event:" + ev);
            cb.accept(ev);
        });
    }

    public static void onPayload(Object widget, String event, Consumer<byte[]> cb) {
        String eventName = eventName(widget, event);
        commFor(widget).on(eventName, byte[].class, p -> {
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                System.out.println(eventName + ", payload:" + (p == null ? "null" : p.length + "B"));
            cb.accept(p);
        });
    }

    public static <T> void onPayload(Object widget, String event, Class<T> cls, Consumer<T> cb) {
        String eventName = eventName(widget, event);
        commFor(widget).on(eventName, cls, p -> {
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                System.out.println(eventName + ", payload:" + p);
            cb.accept(p);
        });
    }

    public static void removeEvent(Object widget, String event) {
        String eventName = eventName(widget, event);
        commFor(widget).remove(eventName);
    }

    public static void sendEvent(Object widget, String event) {
        String name = eventName(widget, event);
        commFor(widget).send(name);
    }

    public static void send(DartResource resource, String event, Object args) {
        CommService comm = commFor(resource);
        if (getBridge(resource) instanceof GCImageDrawer drawer) {
            // Serialize eagerly (captures current GC state: colors, font, etc.) then
            // queue the send so it is dispatched only after Flutter's GCDrawer.standalone
            // has registered its listeners — fixing the macOS race condition where ops
            // arrive before _registerOps() runs.
            try {
                String stateEventName = null;
                byte[] stateBytes = null;
                synchronized (dirty) {
                    if (dirty.remove(resource)) {
                        stateEventName = event(resource);
                        stateBytes = serializer.to(getApi(resource));
                    }
                }
                byte[] opBytes = serializer.to(args);

                final String finalStateEvent = stateEventName;
                final byte[] finalStateBytes = stateBytes;
                final String opEvent = eventName(resource, event);
                final byte[] finalOpBytes = opBytes;

                drawer.queueOp(() -> {
                    if (finalStateEvent != null) {
                        comm.send(finalStateEvent, finalStateBytes);
                    }
                    comm.send(opEvent, finalOpBytes);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (dirty.contains(resource)) {
            update().whenComplete((r, a) -> {
                try {
                    serializeAndSend(comm, eventName(resource, event), args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try {
                serializeAndSend(comm, eventName(resource, event), args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void send(DartWidget resource, String event, Object args) {
        CommService comm = commFor(resource);
        if (dirty.contains(resource)) {
            update().whenComplete((r, a) -> {
                try {
                    serializeAndSend(comm, eventName(resource, event), args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try {
                serializeAndSend(comm, eventName(resource, event), args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The one ClientReady handler, shared by the embedded ({@link #onReady}) and Display-level
     * ({@code DisplayBridge.registerDisplayClientReady}) bridges — every ClientReady comes from the
     * same Flutter {@code main.dart}, for a widget or a Display, with some payload. The first time the
     * client signals ready on {@code channel}, mark {@link #clientReady} complete and push the
     * swt.evolve properties (Flutter can only receive them once ready). {@code each} then runs with
     * the payload for surface-specific work (resolve the ready payload, hot-reload re-render, sync
     * Display bounds, push the first/next update); its {@code Boolean} arg is whether this was the
     * first (completing) ClientReady.
     */
    protected <P> void onClientReady(String channel, Class<P> type, java.util.function.BiConsumer<P, Boolean> each) {
        comm().on(channel, type, p -> {
            boolean first = !clientReady.isDone();
            if (first) {
                System.out.println("ClientReady " + channel);
                clientReady.complete(true);
                displayBootstrapped = true;
                sendSwtEvolveProperties();
            }
            if (each != null) each.accept(p, first);
        });
    }

    protected <P> CompletableFuture<P> onReady(Object control, Class<P> payloadClass) {
        setNotNew(control);
        dirty(control);
        CompletableFuture<P> readyPayload = (payloadClass != null) ? new CompletableFuture<>() : null;
        onClientReady(event(control, "ClientReady"), payloadClass, (p, first) -> {
            if (first) {
                if (readyPayload != null) readyPayload.complete(p);
            } else { // hot reload
                dirty(control);
                update();
            }
        });
        return readyPayload;
    }

    private void dirty(Object obj) {
        if (obj instanceof DartControl c)
            dirty(c);
        if (obj instanceof DartResource r)
            dirty(r);
    }

    public void dirty(DartResource resource) {
        if (resource == null)
            return;
        synchronized (dirty) {
            dirty.add(resource);
        }
        wakeForDirty();
    }

    public void dirty(DartWidget widget) {
        if (widget == null)
            return;
        synchronized (dirty) {
            dirty.add(widget);
        }
        wakeForDirty();
    }

    /**
     * Invoked right after a widget/resource is marked dirty. The dirty set is flushed to Dart only at
     * the top of the next {@code readAndDispatch()}, so on a platform whose UI thread parks while idle
     * (web {@code DartDisplay.sleep()}), a dirty produced off the UI thread would otherwise wait for
     * the {@code sleep()} safety-net cap (~50ms) before reaching Dart. The web bridge overrides this to
     * wake its Display so the flush is prompt; a dirty on the UI thread releases a permit that the same
     * thread's next {@code sleep()} drains, so it costs nothing there. No-op where the event loop
     * already flushes itself (desktop natives).
     */
    protected void wakeForDirty() {
    }

    // Package-private methods for testing
    static void clearDirty() {
        synchronized (dirty) {
            dirty.clear();
        }
    }

    static boolean isDirty(Object widget) {
        synchronized (dirty) {
            return dirty.contains(widget);
        }
    }
    
    /** Whether any widget/resource is awaiting a flush to Dart — a pending-work condition for sleep(). */
    public boolean hasDirty() {
        synchronized (dirty) {
            return !dirty.isEmpty();
        }
    }

    public static String widgetName(Object w) {
        if (w instanceof DartWidget) {
            return w.getClass().getSimpleName().substring(4);
        }
        if (w instanceof DartResource) {
            return w.getClass().getSimpleName().substring(4);
        }
        return w.getClass().getSimpleName();
    }

    public static String eventName(Object w, String event) {
        return widgetName(w) + "/" + id(w) + "/" + event;
    }

    public static String event(Object w, String... events) {
        String ev = widgetName(w) + "/" + id(w);
        if (events.length > 0)
            ev += "/" + String.join("/", events);
        return ev;
    }

    public abstract void initFlutterView(Composite parent, DartControl control);

    public void destroy(DartWidget control) {
        comm().remove(event(control,"ClientReady"));
    }

    public void setBounds(DartControl control, Rectangle bounds) {
    }

    public void setVisible(DartControl control, boolean visible) {
    }

    public void setZOrder(DartControl control, Control sibling, boolean above) {
    }

    public boolean setFocus(DartControl control) {
        return false;
    }

    public boolean hasFocus(DartControl control) {
        return false;
    }

    public Object container(DartComposite parent) {
        return null;
    }

    public Point getWindowOrigin(DartControl control) {
        return new Point(0, 0);
    }

    public void setCursor(DartControl control, long cursor) {
    }

    public void reparent(DartControl control, Composite parent) {
    }

    public static long id(Object w) {
        if (w instanceof DartWidget)
            return ((DartWidget) w).getApi().hashCode();
        if (w instanceof DartResource) {
            return ((DartResource) w).getApi().hashCode();
        }
        return w.hashCode();
    }

    static long id(Widget w) {
        return w.hashCode();
    }

    protected void broadcastSwtEvolveProperties() {
        try {
            serializeAndSend("swt.evolve.properties", getConfigFlags());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendSwtEvolveProperties() {
        System.out.println("will send: " + getConfigFlags());
        broadcastSwtEvolveProperties();
    }

}


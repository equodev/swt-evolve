package dev.equo.swt;

import dev.equo.swt.comm.BinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.JettyBinaryCommService;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.spi.FlutterBridgeSpi;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
     * channels to it. Each comm is independent, so web Displays each get their own.
     */
    protected static CommService newComm() {
        String impl = System.getProperty("comm.impl", "java-websocket");
        CommService comm = "jetty".equals(impl) ? new JettyBinaryCommService() : new BinaryCommService();
        comm.on("swt.evolve.property.set", ConfigFlags.class, parsed -> handlePropertySetFromFlutter(comm, parsed));
        comm.on("swt.evolve.url.open", Object.class, FlutterBridge::handleUrlOpenFromFlutter);
        comm.on(WIDGET_REFRESH_CHANNEL, String.class, FlutterBridge::handleWidgetRefresh);
        // Debug-only: a reflective "run this on the UI thread" primitive (open a dialog by id, etc.)
        // that reaches surfaces the Flutter action layer can't drive — e.g. a native-menu-gated
        // Preferences dialog. Gated so it never registers in production. See TestUiRunner.
        if (Config.isDebug())
            comm.on(TestUiRunner.CHANNEL, Object.class, m -> TestUiRunner.handle(comm, m));
        return comm;
    }

    /**
     * Channel Flutter sends "re-serialize widget <id>" requests on. Flutter asks for one when it
     * finds a payload that was buffered while the widget was unmounted: such a payload is
     * ambiguous — it can be older than the state the widget just mounted with (a stale snapshot
     * from before a SashForm reveal) or newer (a dialog Shell's content sent right after
     * the Display embed that mounted it) — so instead of applying it, Flutter drops it and asks
     * for the live state, which is authoritative either way.
     */
    public static final String WIDGET_REFRESH_CHANNEL = "swt.evolve.widget.refresh";

    /** Widgets/resources by {@link #id}, so a refresh request can find its target. */
    private static final java.util.concurrent.ConcurrentHashMap<Long, java.lang.ref.WeakReference<Object>> widgetsById =
            new java.util.concurrent.ConcurrentHashMap<>();

    static void handleWidgetRefresh(String idText) {
        if (idText == null) return;
        long id;
        try {
            id = Long.parseLong(idText.trim());
        } catch (NumberFormatException e) {
            return;
        }
        java.lang.ref.WeakReference<Object> ref = widgetsById.get(id);
        Object w = ref != null ? ref.get() : null;
        if (w == null || isDisposed(w)) return;
        FlutterBridge bridge = getBridge(w);
        if (bridge == null) return;
        // dirty() is safe off the display thread; the next dispatch flushes the fresh state.
        if (w instanceof DartWidget widget) bridge.dirty(widget);
        else if (w instanceof DartResource resource) bridge.dirty(resource);
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

    static void handleUrlOpenFromFlutter(Object payload) {
        String url = payload instanceof Map ? urlOf((Map<?, ?>) payload) : null;
        if (url == null) {
            System.out.println("[url.open] refused: " + payload);
            return;
        }
        if (!org.eclipse.swt.program.Program.launch(url)) {
            System.out.println("[url.open] could not open: " + url);
        }
    }

    /**
     * The URL in a {@code swt.evolve.url.open} payload, or null when it carries none or one the OS
     * must not be handed. Only http(s) passes: this is Flutter-supplied input and the OS handler is
     * ShellExecute on Windows, which would equally run an executable path or a custom scheme. The
     * check belongs here rather than in {@code Program.launch}, which legitimately opens local files.
     */
    static String urlOf(Map<?, ?> payload) {
        Object value = payload.get("url");
        if (!(value instanceof String)) return null;
        String url = ((String) value).trim();
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") ? url : null;
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
            // Stop at the widget's own Shell: a Shell serializes its own subtree but not the Shells
            // it opens (VShell.getShells() reads DartShell.shells, which nothing on the Java->Dart
            // side ever assigns, so it is always null). A dirty ancestor above this Shell therefore
            // cannot carry the widget, and treating it as a carrier drains the widget from the dirty
            // set unsent, losing the change for good.
            if (isShell(parent)) {
                return false;
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

    /** Smallest gap between two pushes of the same widget: a frame, as a repaint would coalesce to. */
    private static final long PUSH_INTERVAL_NANOS = 16_000_000L;

    private static final Map<Object, Long> lastPushNanos =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /**
     * Table only: a held-back push arrives a turn later, which breaks code that changes a widget,
     * pumps the loop and reads the result at once. Table's payload carries every row, so it is the
     * one widget where the saving is worth that.
     */
    private static boolean coalescible(Object widget) {
        return widget instanceof DartControl control
                && control.getApi() instanceof org.eclipse.swt.widgets.Table;
    }

    private static boolean pushDue(Object widget, long now) {
        if (!coalescible(widget)) return true;
        Long last = lastPushNanos.get(widget);
        return last == null || now - last >= PUSH_INTERVAL_NANOS;
    }

    /** The event loop's flush: coalescible widgets are held to one push per frame. */
    public static CompletableFuture<Void> updateFrame() {
        return update(true);
    }

    /** Flushes everything now: callers that block on the result need the state out on this call. */
    public static CompletableFuture<Void> update() {
        return update(false);
    }

    private static CompletableFuture<Void> update(boolean coalesce) {
        flushOpBatches();
        if (dirty.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Drain, don't copy-then-clear: clearing after the sends discarded every mark made while
        // this flush ran, and nothing re-marks them, so that state never reached Dart at all.
        // Anything dirtied from here on belongs to the next flush.
        Set<Object> dirtySnapshot;
        synchronized (dirty) {
            dirtySnapshot = new HashSet<>(dirty);
            dirty.clear();
        }
        Set<Object> filteredDirty = filterWidgetsWithDirtyAncestors(dirtySnapshot);

        for (Object widget : dirtySnapshot) {
            if (!filteredDirty.contains(widget)) {
                setNotNew(widget);
            }
        }

        long now = System.nanoTime();
        for (Object widget : filteredDirty) {
            if (isDisposed(widget)) continue;
            // No bridge (Display already gone) -> nothing to send; skip to avoid NPE below.
            if (getBridge(widget) == null) continue;
            if (coalesce && !pushDue(widget, now)) {
                synchronized (dirty) {
                    dirty.add(widget);
                }
                continue;
            }
            if (coalescible(widget)) lastPushNanos.put(widget, now);
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
            if (widget.isDisposed()) {
                DebugLog.checkpoint(eventName, "skipped: widget disposed");
                return;
            }
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                DebugLog.logRecv(eventName, ev);
            // A message whose body is absent deserializes to null, and handlers read the Event
            // straight away — DartText's DefaultSelection does `e.detail == SWT.ICON_CANCEL`, so a
            // bodyless Enter in a Text threw NullPointerException out of an asyncExec, which e4
            // turns into a modal "Internal Error" dialog that blocks the whole workbench. Six
            // handlers across five widgets dereference the event this way; guarding here fixes the
            // class rather than the instance. An empty Event carries detail == 0, which is what a
            // "no detail" event means to every one of them. Logged, not swallowed: a null body is
            // still worth seeing when reading a trace.
            if (ev == null) {
                DebugLog.checkpoint(eventName, "empty body: substituting a blank Event");
                cb.accept(new Event());
                return;
            }
            cb.accept(ev);
        });
    }

    public static void onPayload(Object widget, String event, Consumer<byte[]> cb) {
        String eventName = eventName(widget, event);
        commFor(widget).on(eventName, byte[].class, p -> {
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                DebugLog.logRecvPayload(eventName, p == null ? "null" : p.length + "B");
            cb.accept(p);
        });
    }

    public static <T> void onPayload(Object widget, String event, Class<T> cls, Consumer<T> cb) {
        String eventName = eventName(widget, event);
        commFor(widget).on(eventName, cls, p -> {
            if (!eventName.contains("MouseMove") || getConfigFlags().print_move)
                DebugLog.logRecvPayload(eventName, String.valueOf(p));
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
        if (resource instanceof DartGC gc) {
            bufferOp(comm, gc, event, args);
            return;
        }
        if (dirty.contains(resource)) {
            CompletableFuture<Void> deferred = update().whenComplete((r, a) -> {
                try {
                    serializeAndSend(comm, eventName(resource, event), args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            trackDeferredSend(deferred);
        } else {
            try {
                serializeAndSend(comm, eventName(resource, event), args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** A GC's ops, held to one frame per paint. Flutter paints nothing until gcDispose anyway. */
    private static final Map<DartGC, MessageBatch> opBatches =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    /** Cap on what one batch holds, so a GC that is never disposed cannot grow it without end. */
    private static final int MAX_BATCH_BYTES = 1 << 20;

    private static void bufferOp(CommService comm, DartGC gc, String event, Object args) {
        MessageBatch batch = opBatches.computeIfAbsent(gc, g -> new MessageBatch());
        try {
            // The GC's state has to precede the op drawn with it, and on the same frame.
            synchronized (dirty) {
                if (dirty.remove(gc)) addToBatch(batch, event(gc), serializer.to(getApi(gc)));
            }
            addToBatch(batch, eventName(gc, event), serializer.to(args));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (GC_DISPOSE.equals(event) || batch.byteSize() >= MAX_BATCH_BYTES) flushOpBatch(gc);
    }

    /** Terminates a paint: Flutter commits the staged ops when it arrives. */
    private static final String GC_DISPOSE = "gcDispose";

    private static void addToBatch(MessageBatch batch, String event, byte[] bytes) {
        DebugLog.logSend(event, bytes);
        batch.add(event, bytes);
    }

    /** Puts a GC's buffered ops on the wire, for a caller about to block on an answer to one. */
    public static void flushOps(Object resource) {
        if (resource instanceof DartGC gc) flushOpBatch(gc);
    }

    private static void flushOpBatch(DartGC gc) {
        MessageBatch batch = opBatches.remove(gc);
        if (batch != null && !batch.isEmpty()) commFor(gc).send(batch);
    }

    /** Nothing may stay buffered across an event-loop turn, whatever disposed the GC or didn't. */
    private static void flushOpBatches() {
        if (opBatches.isEmpty()) return;
        List<DartGC> open;
        synchronized (opBatches) {
            open = new ArrayList<>(opBatches.keySet());
        }
        for (DartGC gc : open) flushOpBatch(gc);
    }

    // A send through the "dirty" branch above defers the actual wire send to an arbitrary
    // later point. An unbuffered, immediate send issued afterwards (e.g. releasing a remote
    // image cache entry a still-deferred draw references) can then physically overtake it on
    // the wire. Track every deferred send so such a caller can wait for the backlog to drain.
    private static final java.util.Set<CompletableFuture<?>> pendingDeferredSends =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static void trackDeferredSend(CompletableFuture<Void> future) {
        pendingDeferredSends.add(future);
        future.whenComplete((r, e) -> pendingDeferredSends.remove(future));
    }

    /**
     * Resolves once every {@link #send(DartResource, String, Object)} deferred at the moment of
     * this call has actually gone out. Callers that must not let a message overtake an
     * already-queued deferred send (e.g. releasing a remote image cache entry a pending draw might
     * still reference) should send after this completes rather than immediately.
     */
    public static CompletableFuture<Void> awaitPendingDeferredSends() {
        return CompletableFuture.allOf(pendingDeferredSends.toArray(new CompletableFuture[0]));
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
        registerForRefresh(resource);
        synchronized (dirty) {
            dirty.add(resource);
        }
        wakeForDirty();
    }

    public void dirty(DartWidget widget) {
        if (widget == null)
            return;
        registerForRefresh(widget);
        synchronized (dirty) {
            dirty.add(widget);
        }
        wakeForDirty();
    }

    /**
     * A dirty() during construction can run before the api peer is wired, when the widget has no
     * id yet — skip it; the next dirty() (any later state change or send) registers it.
     */
    private static void registerForRefresh(Object w) {
        Object api = getApi(w);
        if (api != null)
            widgetsById.put((long) api.hashCode(), new java.lang.ref.WeakReference<>(w));
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
    
    /**
     * Whether anything is awaiting a flush <em>now</em> — a pending-work condition for sleep(). A
     * held-back widget must not count, or the loop spins instead of parking for the rest of the
     * frame, and it is the parking that coalesces the pushes.
     */
    public boolean hasDirty() {
        long now = System.nanoTime();
        synchronized (dirty) {
            for (Object widget : dirty) {
                if (pushDue(widget, now)) return true;
            }
            return false;
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

    /** Clears the tracked focus holder if it is {@code control}. No-op unless a Display-level bridge
     *  tracks focus (see the whole-tree surface); embedded/other bridges have nothing to clear. */
    public void clearFocus(DartControl control) {
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


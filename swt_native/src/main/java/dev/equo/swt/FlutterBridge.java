package dev.equo.swt;

import dev.equo.swt.comm.AbstractBinaryCommService;
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

    /**
     * Re-describes the widget with this id to the client in full, as {@link #WIDGET_REFRESH_CHANNEL}
     * does on request. Called where <em>this</em> side knows a client holds nothing for it: a shell
     * just given a window of its own is rendered by a fresh client rooted at that shell, which has
     * never been sent it.
     */
    public static void resendWidget(long id) {
        handleWidgetRefresh(Long.toString(id));
    }

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
        // The request says this client holds nothing for the widget, so what it has been sent
        // before is worth nothing to it: forget that, or the answer is a description of what
        // changed since a state it does not have.
        // dirty() is safe off the display thread; the next dispatch flushes the fresh state.
        if (w instanceof DartWidget) {
            DartWidget widget = (DartWidget) w;
            Serializer.forgetDelivery(widget.getValue());
            bridge.dirty(widget);
        } else if (w instanceof DartResource) {
            DartResource resource = (DartResource) w;
            resource.getValue().sent(0, 0L);
            bridge.dirty(resource);
        }
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

    /**
     * The widgets that go on their own channel, out of everything that is dirty.
     *
     * <p>A widget is left out only when an ancestor is going to be sent in a form that contains
     * it: whole, or as an update that names its child list. Sending both would then be the same
     * state twice. An update naming anything else carries nothing about what is beneath it, so a
     * descendant left out on its account would simply never arrive.
     *
     * <p>That is also where the saving is. Two changes at different depths of one subtree used to
     * force the ancestor to go whole to carry the deeper one, dragging every node in between along
     * in full. Now each travels as itself, the nodes in between are not sent at all, and the batch
     * puts them in one message anyway.
     */
    static Set<Object> filterWidgetsWithDirtyAncestors(Set<Object> dirtySet) {
        Set<Object> filtered = new HashSet<>();

        for (Object widget : dirtySet) {
            if (isFlutterRoot(widget) || isShell(widget) || !hasAncestorCarrying(widget, dirtySet)) {
                filtered.add(widget);
            }
        }

        return filtered;
    }

    /**
     * The order one flush's frames go out in: the display first, then parents before children.
     *
     * <p>The set they come from is a hash set, so without this they leave in identity-hash order -
     * arbitrary, and different between runs of the same code. The frames of one flush travel as one
     * message and are applied in the order they were written, so that hash order was the delivery
     * order.
     *
     * <p>What has to come first is the display: its frame carries the shell list, which is how the
     * far side learns a shell exists at all, and sent after the widgets beneath those shells it
     * arrives too late for them to be placed.
     *
     * <p>Only that, and deliberately. Ordering the widgets among themselves by depth as well would
     * be the complete answer, but it costs a walk up the parent chain per widget - and a parent's
     * getter is guarded, so the walk is far from free: measured over a workbench-sized flush it was
     * a third again of what the whole flush costs, on work that runs every turn of the event loop.
     * The children whose parent is carrying them have already been dropped from this set by
     * {@link #filterWidgetsWithDirtyAncestors}, so what is left is mostly siblings, where the order
     * between them decides nothing.
     */
    private static java.util.List<Object> flushOrder(Set<Object> flush) {
        java.util.List<Object> ordered = new ArrayList<>(flush.size());
        for (Object o : flush) {
            if (o instanceof DirtyState) ordered.add(o);
        }
        for (Object o : flush) {
            if (!(o instanceof DirtyState)) ordered.add(o);
        }
        return ordered;
    }

    /**
     * Whether some ancestor of [widget] is going to be sent in a form that contains it.
     *
     * <p>The search stops at the widget's own Shell: a Shell's payload holds its own subtree and
     * nothing of another's, so a control in a dialog is not carried by the main window however
     * dirty that window is — and the main window repaints constantly, so it almost always is.
     */
    private static boolean hasAncestorCarrying(Object widget, Set<Object> dirtySet) {
        for (Object parent = getParent(widget); parent != null; parent = getParent(parent)) {
            if (dirtySet.contains(parent) && carriesItsChildren(parent)) return true;
            if (isShell(parent)) return false;
        }
        return false;
    }

    /**
     * Whether what this widget is about to send contains the widgets beneath it.
     *
     * <p>Two ways it can. Sent whole, it is every property including the children. Sent as an
     * update that names {@code children}, it carries that list — and a child in it that has changed
     * is written out in full, so the subtree comes with it.
     *
     * <p>An update that names anything else contains nothing but the properties it names. A
     * descendant left out on the strength of one of those would never arrive, and one left out on
     * the strength of one of these would arrive twice.
     */
    private static boolean carriesItsChildren(Object widget) {
        if (!(widget instanceof DartWidget)) return true;
        DartWidget w = (DartWidget) widget;
        // Asked of the client this widget is written for. The filter runs before the walk that
        // names it, so there is no addressee in scope to inherit - and reading it as "no client"
        // would answer that every widget is sent whole, which drops the children of one that is
        // not.
        if (!Serializer.canDiff(w, connectionOf(widget))) return true;
        return w.getValue().changedKeys().contains("children");
    }

    private static boolean isShell(Object widget) {
        return widget instanceof DartControl && ((DartControl) widget).getApi() instanceof Shell;
    }

    private static boolean isFlutterRoot(Object widget) {
        if (widget instanceof DartControl) { DartControl d = (DartControl) widget;
            FlutterBridge bridge = d.getBridge();
            return bridge != null && bridge.forWidget() == widget;
        }
        return false;
    }

    /**
     * The widgets that are being sent on behalf of a descendant as well as themselves.
     *
     * <p>A dirty widget with a dirty ancestor is not sent on its own channel: the ancestor's
     * payload contains it, so sending both would be the same state twice. That holds only while a
     * payload is the whole widget. An update describing just the ancestor's own changed properties
     * carries nothing of its children, so a child dropped in its favour would simply never be sent
     * — a layout pass would move five hundred children and report only the parent's own bounds.
     *
     * <p>These therefore go out whole. Naming the descendants instead is the shape the subtree work
     * takes, and until then this is the line between "smaller" and "wrong".
     */
    private static Set<Object> ancestorsCarryingOthers(Set<Object> allDirty, Set<Object> beingSent) {
        Set<Object> carrying = new HashSet<>();
        for (Object widget : allDirty) {
            if (beingSent.contains(widget)) continue;
            for (Object parent = getParent(widget); parent != null; parent = getParent(parent)) {
                if (beingSent.contains(parent)) {
                    carrying.add(parent);
                    break;
                }
            }
        }
        return carrying;
    }

    /**
     * Every widget a change has to travel through: the dirty ones, and every ancestor of them.
     *
     * <p>A dirty widget whose ancestor is also being sent is not sent on its own channel — the
     * ancestor's payload contains it. That payload is the only copy of the change, so nothing on
     * the way down to it may be written as a name rather than a description: an unchanged composite
     * between the two is unchanged and still load-bearing.
     */
    private static Set<VWidget> pathsToDirty(Set<Object> dirtySet) {
        Set<VWidget> required = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Object widget : dirtySet) {
            for (Object node = widget; node != null; node = getParent(node)) {
                if (node instanceof DartWidget) required.add(((DartWidget) node).getValue());
            }
        }
        return required;
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
        return widget instanceof DartControl
                && ((DartControl) widget).getApi() instanceof org.eclipse.swt.widgets.Table;
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
        Set<Object> carryingDescendants = ancestorsCarryingOthers(dirtySnapshot, filteredDirty);
        Serializer.describeInFull(pathsToDirty(dirtySnapshot));

        for (Object widget : dirtySnapshot) {
            if (!filteredDirty.contains(widget)) {
                setNotNew(widget);
            }
        }

        long now = System.nanoTime();
        // Everything this flush sends belongs to one moment, so it travels as one message. Only the
        // sends made inline are caught: a widget still waiting on clientReady sends when it can.
        Map<CommService, MessageBatch> batching = new java.util.LinkedHashMap<>();
        flushSends.set(batching);
        try {
        for (Object widget : flushOrder(filteredDirty)) {
            // Non-widget state (the Display) sends itself, and does so without waiting on
            // clientReady: its frame is what gives a connecting client its first content, and the
            // comm buffers it until the socket opens. Gating it here is what once left a workbench
            // showing an empty window — the re-push needed the UI thread, which was parked in the
            // native event pump.
            if (widget instanceof DirtyState) {
                DirtyState state = (DirtyState) widget;
                if (!state.isStale()) state.flush();
                continue;
            }
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
                    boolean isHidden = (widget instanceof org.eclipse.swt.widgets.DartControl)
                            && !((org.eclipse.swt.widgets.DartControl) widget).getVisible();
                    if (!isNew(widget) || widget instanceof DartToolTip || widget instanceof DartMenu || isHidden) { // send with the parent
                        setNotNew(widget);
                        synchronized (dirty) { // undirty if it was dirtied while waiting foe clientReady
                            dirty.remove(widget);
                        }
                        String event = event(widget);
                        CommService comm = commFor(widget);
                        try {
                            // Written for one client, so say which before writing: whether a nested
                            // widget can travel as a name depends on whether that client holds it.
                            Serializer.targeting(comm == null ? 0 : comm.connectionId(), () -> {
                                try {
                                    // A widget already sent whole can be described by what changed
                                    // since. Decided here rather than inside the writer because it
                                    // is a property of the frame: a widget nested in an ancestor's
                                    // payload is still written whole, since the far side has
                                    // nothing to merge a nested change into yet.
                                    if (widget instanceof DartWidget && Serializer.canDiff((DartWidget) widget)
                                            && !carryingDescendants.contains(widget)) {
                                        byte[] header = CommService.frameHeader(event);
                                        serializer.toDiff(header, (DartWidget) widget, (buffer, length) ->
                                                sendBytes(comm, event, buffer, header.length, length));
                                    } else if (widget instanceof DartGC) {
                                        byte[] header = CommService.frameHeader(event);
                                        writeGCState(comm, (DartGC) widget, header, (buffer, length) ->
                                                sendBytes(comm, event, buffer, header.length, length));
                                    } else
                                        serializeAndSend(comm, event, getApi(widget));
                                } catch (IOException e) {
                                    throw new java.io.UncheckedIOException(e);
                                }
                            });
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
        } finally {
            Serializer.describeNormally();
            flushSends.remove();
            for (Map.Entry<CommService, MessageBatch> entry : batching.entrySet()) {
                entry.getKey().send(entry.getValue());
                releaseBatch(entry.getValue());
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Whether the client has never been told about this widget.
     *
     * <p>New is about the client's knowledge, so the write stamp is what answers it: a widget that
     * has been written has been delivered, whether on its own channel or inside an ancestor's
     * payload. The flag alone did not say that — it is only set for widgets this flush considered,
     * so a child created quietly and delivered inside its parent stayed "new" for good, and every
     * later update of its own was dropped here as one the client could not place.
     */
    private static boolean isNew(Object widget) {
        if (widget instanceof DartWidget) {
            DartWidget w = (DartWidget) widget;
            return w.getData(DEV_EQU_SWT_NEW) == null
                    && w.getValue().sentSeq(connectionOf(widget)) == 0;
        }
        return false;
    }

    /** The client {@code widget} is written for, or 0 when it has no comm to be written to. */
    private static int connectionOf(Object widget) {
        CommService comm = commFor(widget);
        return comm == null ? 0 : comm.connectionId();
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

    /**
     * State that belongs in the dirty set but is not a widget — today only the {@code Display},
     * whose value has its own channel and no place in the widget tree.
     *
     * <p>It joins the same set so that "what still has to be sent" has one answer rather than two,
     * and so the per-property work that will apply to widgets applies to it by construction instead
     * of needing a parallel implementation.
     */
    public interface DirtyState {
        /** True once it can no longer be sent — dropped from the set rather than flushed. */
        boolean isStale();

        void flush();
    }

    /** Enrols non-widget state for the next flush. */
    public static void dirty(DirtyState state) {
        if (state == null) return;
        synchronized (dirty) {
            dirty.add(state);
        }
    }

    /**
     * Flushes enrolled {@link DirtyState} only, leaving widgets to their normal flush.
     *
     * <p>Exists so enrolling the Display does not also change when widgets are sent. Its callers
     * push immediately today; when Display frames are allowed to coalesce into the ordinary flush,
     * those calls go away and nothing else has to move.
     */
    public static void flushDirtyStates() {
        List<DirtyState> due = new ArrayList<>();
        synchronized (dirty) {
            dirty.removeIf(entry -> {
                if (!(entry instanceof DirtyState)) return false;
                due.add((DirtyState) entry);
                return true;
            });
        }
        for (DirtyState state : due) {
            if (!state.isStale()) state.flush();
        }
    }

    /**
     * Sees every outbound frame as it is sent. Unset in production; a verification harness installs
     * one to check what the widget tree actually puts on the wire against what changed in Java —
     * a question no assertion inside the harness can answer, because only this point sees every
     * frame as it is sent.
     */
    public interface SendObserver {
        void onSend(String eventName, byte[] payload);
    }

    private static volatile SendObserver sendObserver;

    /** Installs (or, with null, removes) the outbound-frame observer. */
    public static void setSendObserver(SendObserver observer) {
        sendObserver = observer;
    }

    private static void serializeAndSend(CommService comm, String eventName, Object args) throws IOException {
        // Named here as well as at the flush, so a send from anywhere else - an event reply, a
        // pre-connect push, a draw op - is credited to the client it actually goes to.
        if (Serializer.targetConnection() != 0) {
            serializeFrameAndSend(comm, eventName, args);
            return;
        }
        java.io.IOException[] failed = new java.io.IOException[1];
        Serializer.targeting(comm == null ? 0 : comm.connectionId(), () -> {
            try {
                serializeFrameAndSend(comm, eventName, args);
            } catch (IOException e) {
                failed[0] = e;
            }
        });
        if (failed[0] != null) throw failed[0];
    }

    /** Serializes {@code args} behind its frame header, so the frame is sent from where it was written. */
    private static void serializeFrameAndSend(CommService comm, String eventName, Object args) throws IOException {
        byte[] header = CommService.frameHeader(eventName);
        serializer.to(header, args, (buffer, length) -> sendBytes(comm, eventName, buffer, header.length, length));
    }

    /**
     * The tail of a send. {@code buffer[0, length)} is a whole frame whose payload starts at
     * {@code payloadStart}, lent by the serializer: anything that keeps it past this call copies it.
     */
    private static void sendBytes(CommService comm, String eventName, byte[] buffer, int payloadStart, int length) {
        // The bytes are going out, so what was written counts as delivered. Serializing on its own
        // does not: a widget can be written to be read rather than sent.
        Serializer.markDelivered();
        int payloadLength = length - payloadStart;
        DebugLog.logSend(eventName, buffer, payloadStart, payloadLength);
        SendObserver observer = sendObserver;
        if (observer != null) observer.onSend(eventName, java.util.Arrays.copyOfRange(buffer, payloadStart, length));
        Map<CommService, MessageBatch> open = flushSends.get();
        if (open != null) {
            MessageBatch batch = open.computeIfAbsent(comm, c -> borrowBatch());
            batch.add(eventName, buffer, payloadStart, payloadLength);
            // A flush big enough to be worth splitting: one unbounded message would stall the
            // client's decode as surely as the frames it replaced stalled its event loop.
            if (batch.byteSize() >= MAX_BATCH_BYTES) {
                comm.send(batch);
                batch.clear();
            }
            return;
        }
        comm.sendFrame(buffer, 0, length);
    }

    /**
     * What one flush has produced so far, per comm, while that flush is running.
     *
     * <p>A layout pass touches every child of a composite, and each of them is a frame. Sending
     * them one at a time is one socket message, one handler dispatch and one rebuild each, for
     * changes that all belong to the same moment, so they travel as one {@link MessageBatch}.
     *
     * <p>Null outside a flush, which is what makes a send from anywhere else — an event reply, a
     * draw op, a pre-connect push finishing on another thread — go straight out as it always did.
     * Thread-local for the same reason: only the sends this flush makes inline belong to it.
     */
    private static final ThreadLocal<Map<CommService, MessageBatch>> flushSends = new ThreadLocal<>();

    /** Batches already grown to a flush's size, reused so their buffer is not regrown every turn. */
    private static final ThreadLocal<java.util.ArrayDeque<MessageBatch>> spareBatches =
            ThreadLocal.withInitial(java.util.ArrayDeque::new);

    /** Spares kept per thread; a batch released on a thread that never borrows must not pile up there. */
    private static final int MAX_SPARE_BATCHES = 4;

    private static MessageBatch borrowBatch() {
        MessageBatch batch = spareBatches.get().pollFirst();
        return batch != null ? batch : new MessageBatch();
    }

    private static void releaseBatch(MessageBatch batch) {
        batch.clear();
        java.util.ArrayDeque<MessageBatch> spares = spareBatches.get();
        // One outsized frame must not pin its buffer for the life of the thread.
        if (spares.size() < MAX_SPARE_BATCHES && batch.capacity() <= 2 * MAX_BATCH_BYTES) spares.addFirst(batch);
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
        if (getBridge(resource) instanceof GCImageDrawer) { GCImageDrawer drawer = (GCImageDrawer) getBridge(resource);
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
        if (resource instanceof DartGC) { DartGC gc = (DartGC) resource;
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
        MessageBatch batch = opBatches.computeIfAbsent(gc, g -> borrowBatch());
        try {
            // The GC's state has to precede the op drawn with it, and on the same frame.
            synchronized (dirty) {
                if (dirty.remove(gc)) {
                    String stateEvent = event(gc);
                    writeGCState(comm, gc, NO_PREFIX, (buffer, length) -> {
                        DebugLog.logSend(stateEvent, buffer, 0, length);
                        batch.add(stateEvent, buffer, 0, length);
                    });
                }
            }
            addToBatch(batch, eventName(gc, event), args);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (GC_DISPOSE.equals(event) || batch.byteSize() >= MAX_BATCH_BYTES) flushOpBatch(gc);
    }

    private static final byte[] NO_PREFIX = new byte[0];

    /**
     * The stamp of the GC state each drawable's channel was last given. Every GC of a control writes
     * to the control's one channel, so a GC may send only what changed while no other GC of that
     * control has written since.
     */
    private static final Map<Object, Long> gcStateSeq = new java.util.WeakHashMap<>();

    /**
     * Writes a GC's state as what changed since its channel last got it, or whole when the channel
     * holds something else: a GC that has not written yet, or another GC of the same control.
     */
    private static void writeGCState(CommService comm, DartGC gc, byte[] prefix, Serializer.Lent sink) {
        VResource value = gc.getValue();
        int connection = comm == null ? 0 : comm.connectionId();
        // Id 0 is shared by every GC on a drawable that is neither a control nor an image.
        Object channel = id(gc) == 0 ? null : gc._drawable();
        synchronized (gcStateSeq) {
            Long held = channel == null ? null : gcStateSeq.get(channel);
            long base = value.sentSeq(connection);
            boolean diff = Serializer.mayDiffResources() && base != 0 && held != null && held == base
                    && value.anyDirty();
            long seq = diff ? serializer.toDiff(prefix, gc, base, sink) : serializer.toStamped(prefix, gc, sink);
            value.sent(connection, seq);
            if (channel != null) gcStateSeq.put(channel, seq);
        }
    }

    /** Terminates a paint: Flutter commits the staged ops when it arrives. */
    private static final String GC_DISPOSE = "gcDispose";

    private static void addToBatch(MessageBatch batch, String event, Object args) throws IOException {
        serializer.to(args, (buffer, length) -> {
            DebugLog.logSend(event, buffer, 0, length);
            batch.add(event, buffer, 0, length);
        });
    }

    /** Puts a GC's buffered ops on the wire, for a caller about to block on an answer to one. */
    public static void flushOps(Object resource) {
        if (!(resource instanceof DartGC)) return;
        DartGC gc = (DartGC) resource;
        flushOpBatch(gc);
        // An Image-backed GC buffers its ops until the drawer is started, and only a caller
        // wanting an answer starts it.
        FlutterBridge gcBridge = getBridge(gc);
        if (gcBridge instanceof GCImageDrawer) ((GCImageDrawer) gcBridge).startForPendingReply();
    }

    private static void flushOpBatch(DartGC gc) {
        MessageBatch batch = opBatches.remove(gc);
        if (batch == null) return;
        if (!batch.isEmpty()) commFor(gc).send(batch);
        releaseBatch(batch);
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

    /**
     * Sends without waiting for a pending update to flush first.
     *
     * {@link #send(DartWidget, String, Object)} defers behind {@link #update()} when the widget is
     * dirty, which is right for state that must not overtake the snapshot it belongs to. It is wrong
     * for a message announcing that a blocking event loop has started — {@code Tracker.open()} —
     * because the loop is what stops that update from ever completing, so the message would never
     * leave and the loop would never be told to end.
     */
    public static void sendNow(DartWidget resource, String event, Object args) {
        try {
            serializeAndSend(commFor(resource), eventName(resource, event), args);
        } catch (IOException e) {
            e.printStackTrace();
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
            // A (re)attaching client cannot be assumed to hold anything already sent.
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
        if (obj instanceof DartControl)
            dirty(((DartControl) obj));
        if (obj instanceof DartResource)
            dirty(((DartResource) obj));
    }

    /** The widget whose state the client itself is currently reporting, if any. */
    private static final ThreadLocal<DartWidget> dirtySuppressedFor = new ThreadLocal<>();

    /**
     * Applies a change that originated on the client without echoing {@code widget}'s state back to
     * it. Wrap only the sync itself: anything the change triggers that the client does <em>not</em>
     * already know about still has to go out.
     */
    public static void withoutDirty(DartWidget widget, Runnable body) {
        DartWidget previous = dirtySuppressedFor.get();
        dirtySuppressedFor.set(widget);
        try {
            body.run();
        } finally {
            dirtySuppressedFor.set(previous);
        }
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
        if (widget == dirtySuppressedFor.get())
            return;
        registerForRefresh(widget);
        synchronized (dirty) {
            dirty.add(widget);
        }
        wakeForDirty();
    }

    /**
     * Records a widget that has just been delivered, so a later request for it can be answered.
     *
     * <p>Everything the far side may be asked to resolve by name has been through here: a name is
     * written only for a widget that was delivered, and delivery is what this records. Scheduling
     * alone used to be the only way in, which left every widget delivered only inside an ancestor
     * unanswerable — it would be named, asked for, and never heard about again.
     */
    static void registerDelivered(Object impl) {
        if (impl != null) registerForRefresh(impl);
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
            return Config.presentedName((DartWidget) w, w.getClass().getSimpleName().substring(4));
        }
        if (w instanceof DartResource) {
            return w.getClass().getSimpleName().substring(4);
        }
        return w.getClass().getSimpleName();
    }

    static {
        AbstractBinaryCommService.setAlternateName(FlutterBridge::alternateAddress);
    }

    /**
     * A plain Composite is named Composite when its handlers are registered at construction and
     * MainComposite once it becomes the main sash area ({@link Config#presentedName}), and it can
     * go back. A message addressed under either name belongs to the same widget.
     */
    static String alternateAddress(String eventName) {
        if (eventName.startsWith("MainComposite/")) return "Composite/" + eventName.substring("MainComposite/".length());
        if (eventName.startsWith("Composite/")) return "MainComposite/" + eventName.substring("Composite/".length());
        return null;
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


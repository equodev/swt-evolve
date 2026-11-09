package dev.equo.swt;

import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.BinaryCommService;
import dev.equo.swt.comm.ChannelHeader;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.JettyBinaryCommService;
import dev.equo.swt.comm.MessageBatch;
import org.eclipse.swt.graphics.GCHelper;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.equo.swt.Config.getConfigFlags;

public abstract class FlutterBridge {
    private static final String DEV_EQU_SWT_NEW = "dev.equo.swt.new";
    protected static final Serializer serializer = new Serializer();
    /**
     * What the next flush sends besides widgets, in the order it was dirtied: resources and other
     * state. Also the lock for everything queued, {@link #dirtyWidgets} included.
     */
    private static final Set<Object> dirty = new java.util.LinkedHashSet<>();

    /**
     * Widgets the next flush sends, in the order they were first dirtied. A widget knows it is here
     * ({@link Serializer.SentHashes#queued}), so dirtying it again costs no lookup, and the flush
     * takes the list whole rather than copying a set. A widget taken off is only unmarked: the
     * flush skips an entry whose widget is no longer marked.
     */
    private static ArrayList<DartWidget> dirtyWidgets = new ArrayList<>();
    /** The list the flush before handed back, emptied, for the next to fill. */
    private static ArrayList<DartWidget> spareDirtyWidgets = new ArrayList<>();
    /** How many widgets in {@link #dirtyWidgets} are still marked. */
    private static int queuedWidgets;

    /** Queues {@code widget} for the next flush; the caller holds {@link #dirty}. */
    private static void queue(DartWidget widget) {
        VWidget value = widget.getValue();
        if (value == null) {
            dirty.add(widget);
            return;
        }
        Serializer.SentHashes memo = Serializer.sentHashes(value);
        if (memo.queued) return;
        memo.queued = true;
        dirtyWidgets.add(widget);
        queuedWidgets++;
    }

    /** Takes {@code widget} off the next flush, whichever way it was queued; the caller holds {@link #dirty}. */
    private static boolean unqueue(Object widget) {
        if (widget instanceof DartWidget) {
            VWidget value = ((DartWidget) widget).getValue();
            Serializer.SentHashes memo = value == null ? null : (Serializer.SentHashes) value.wireMemo;
            if (memo != null && memo.queued) {
                memo.queued = false;
                queuedWidgets--;
                return true;
            }
        }
        return !dirty.isEmpty() && dirty.remove(widget);
    }

    /**
     * Whether {@code widget} may be queued, read without the lock: false only when it is not, as far as
     * this thread has seen, for a caller that then takes the lock to be sure.
     */
    private static boolean mayBeQueued(Object widget) {
        if (!dirty.isEmpty()) return true;
        if (!(widget instanceof DartWidget)) return false;
        VWidget value = ((DartWidget) widget).getValue();
        Serializer.SentHashes memo = value == null ? null : (Serializer.SentHashes) value.wireMemo;
        return memo != null && memo.queued;
    }

    /** Whether {@code widget} is queued for the next flush; the caller holds {@link #dirty}. */
    private static boolean isQueued(Object widget) {
        if (widget instanceof DartWidget) {
            VWidget value = ((DartWidget) widget).getValue();
            Serializer.SentHashes memo = value == null ? null : (Serializer.SentHashes) value.wireMemo;
            if (memo != null && memo.queued) return true;
        }
        return dirty.contains(widget);
    }

    /** Whether nothing is queued; the caller holds {@link #dirty}. */
    private static boolean nothingQueued() {
        return queuedWidgets == 0 && dirty.isEmpty();
    }
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
        return wire("jetty".equals(impl) ? new JettyBinaryCommService() : new BinaryCommService());
    }

    private static CommService wire(CommService comm) {
        comm.on(0, 0, WireTable.CHANNEL_SWT_EVOLVE_PROPERTY_SET, ConfigFlags.class, parsed -> handlePropertySetFromFlutter(comm, parsed));
        comm.on(0, 0, WireTable.CHANNEL_SWT_EVOLVE_URL_OPEN, String.class, FlutterBridge::handleUrlOpenFromFlutter);
        comm.on(0, 0, WireTable.CHANNEL_SWT_EVOLVE_WIDGET_REFRESH, String.class, FlutterBridge::handleWidgetRefresh);
        TAP.wired(comm);
        if (ECHO_TIMING)
            comm.on(0, 0, WireTable.CHANNEL_SWT_EVOLVE_TIMING_PING, Object.class, m -> echoTiming(comm, m));
        return comm;
    }

    /** Echoes timing pings so a lockstep drag's round trip splits into legs. Off by default: a message per move. */
    private static final boolean ECHO_TIMING = Boolean.getBoolean("dev.equo.swt.echoTiming");

    static final String ECHO_CHANNEL = "swt.evolve.timing.ping";

    private static void echoTiming(CommService comm, Object ping) {
        long recvNs = System.nanoTime();
        if (!(ping instanceof Map)) return;
        Object seq = ((Map<?, ?>) ping).get("seq");
        Object sentAt = ((Map<?, ?>) ping).get("t0");
        // Answered before anything is queued: the transport floor, apart from the event-loop hand-off.
        try {
            serializeAndSend(comm, WireTable.CHANNEL_SWT_EVOLVE_TIMING_BARE, Java8.map("seq", seq));
        } catch (IOException e) {
            // Diagnostic only.
        }
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        if (display == null || display.isDisposed()) return;
        // Queued behind this move's work, so the span is Java's handling of the move.
        display.asyncExec(() -> {
            try {
                serializeAndSend(comm, WireTable.CHANNEL_SWT_EVOLVE_TIMING_PONG, Java8.map(
                        "seq", seq,
                        "t0", sentAt,
                        "javaMs", (System.nanoTime() - recvNs) / 1_000_000.0));
            } catch (IOException e) {
                // Diagnostic only.
            }
        });
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

    /** Flutter sets properties the user changed (the theme picker); Java echoes them on {@link #PROPERTIES_CHANNEL}. */
    static final String PROPERTY_SET_CHANNEL = "swt.evolve.property.set";
    /** Java pushes the properties on it whenever a client needs them: when it connects, and after a property set. */
    static final String PROPERTIES_CHANNEL = "swt.evolve.properties";

    /** Widgets/resources by {@link #id}, so a refresh request can find its target. */
    private static final dev.equo.swt.comm.LongTable<java.lang.ref.WeakReference<Object>> widgetsById =
            new dev.equo.swt.comm.LongTable<>();

    /**
     * Re-describes the widget with this id to the client in full, as {@link #WIDGET_REFRESH_CHANNEL}
     * does on request. Called where <em>this</em> side knows a client holds nothing for it: a shell
     * just given a window of its own is rendered by a fresh client rooted at that shell, which has
     * never been sent it.
     */
    public static void resendWidget(long id) {
        handleWidgetRefresh(Long.toString(id));
    }

    /** Whether the table refresh requests are looked up in holds an entry for this {@link #id}. */
    public static boolean holdsId(long id) {
        return widgetsById.containsKey(id);
    }

    /** The live widget or resource with this {@link #id}, or null when there is none. */
    public static Object findById(long id) {
        java.lang.ref.WeakReference<Object> ref = widgetsById.get(id);
        Object w = ref != null ? ref.get() : null;
        return w == null || isDisposed(w) ? null : w;
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
        // changed since a state it does not have. Nor the images it names, which a request also
        // stands for: the client asks for the widget when one of them never reached it. On the
        // Display thread, which writes the same records of what was sent.
        runOnDisplayThread(w, () -> {
            if (isDisposed(w)) return;
            if (w instanceof DartWidget) {
                DartWidget widget = (DartWidget) w;
                Serializer.forgetDelivery(widget.getValue());
                Serializer.forgetResourcesNamedBy(widget.getValue());
                bridge.dirty(widget);
            } else if (w instanceof DartResource) {
                DartResource resource = (DartResource) w;
                resource.getValue().sent(0, 0L);
                bridge.dirty(resource);
            }
        });
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

    static void handleUrlOpenFromFlutter(String payload) {
        String url = payload == null ? null : urlOf(payload);
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
    static String urlOf(String payload) {
        String url = payload.trim();
        String lower = url.toLowerCase(java.util.Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://") ? url : null;
    }

    static void handlePropertySetFromFlutter(CommService comm, ConfigFlags parsed) {
        if (parsed == null) return;
        boolean changed = false;
        ConfigFlags current = getConfigFlags();
        changed |= applyStringField("force_theme", current.force_theme, parsed.force_theme, v -> current.force_theme = v);
        changed |= applyStringField("theme_name", current.theme_name, parsed.theme_name, v -> current.theme_name = v);
        changed |= applyStringField("theme_color", current.theme_color, parsed.theme_color, v -> current.theme_color = v);
        if (changed) {
            // Echo the updated properties back on the comm they arrived on.
            comm.send(0, 0, WireTable.CHANNEL_SWT_EVOLVE_PROPERTIES, WidgetWire.fieldsOf(getConfigFlags()));
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
     * Drains {@code flush} into the order its frames go out in: the display first, then parents
     * before children. The frames of one flush travel as one message and are applied in the order
     * they were written.
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
    private static void flushOrder(java.util.List<Object> ordered) {
        // Widgets wait in their own queue; this set holds the rest, and is usually empty.
        if (!dirty.isEmpty()) {
            for (Object o : dirty) {
                if (o instanceof DirtyState) ordered.add(o);
            }
            for (Object o : dirty) {
                if (!(o instanceof DirtyState)) ordered.add(o);
            }
            dirty.clear();
        }
        ArrayList<DartWidget> widgets = dirtyWidgets;
        dirtyWidgets = spareDirtyWidgets;
        for (int i = 0, n = widgets.size(); i < n; i++) {
            DartWidget widget = widgets.get(i);
            Serializer.SentHashes memo = (Serializer.SentHashes) widget.getValue().wireMemo;
            if (!memo.queued) continue;
            memo.queued = false;
            ordered.add(widget);
        }
        widgets.clear();
        spareDirtyWidgets = widgets;
        queuedWidgets = 0;
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
        // A descendant with its own frame travels as a name, not in full.
        if (describesItsOwnChange(widget)) return false;
        // Asked of the client this widget is written for. The filter runs before the walk that
        // names it, so there is no addressee in scope to inherit - and reading it as "no client"
        // would answer that every widget is sent whole, which drops the children of one that is
        // not.
        if (!Serializer.canDiff(w, connectionOf(widget))) return true;
        return w.getValue().changedKeys().contains(org.eclipse.swt.widgets.VComposite.CHILDREN);
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
    /** Why a widget is about to travel whole; for the debug log only. */
    private static String wholeSendReason(Object widget, CommService comm, Set<Object> carrying) {
        if (!(widget instanceof DartWidget)) return "not a widget";
        DartWidget w = (DartWidget) widget;
        VWidget value = w.getValue();
        if (value == null) return "no value yet";
        if (value.sentSeq(comm == null ? 0 : comm.connectionId()) == 0) return "never delivered";
        if (!value.anyDirty()) return "nothing named";
        if (carrying.contains(widget)) return "carrying a descendant";
        return "diff disabled";
    }

    /** The values of the widgets in {@code widgets}, for the serializer to recognise by identity. */
    private static Set<VWidget> valuesOf(Set<Object> widgets) {
        Set<VWidget> values = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Object widget : widgets) {
            if (widget instanceof DartWidget) {
                VWidget value = ((DartWidget) widget).getValue();
                if (value != null) values.add(value);
            }
        }
        return values;
    }

    /** False for a widget the client has never been given: there is nothing for a change to be relative to. */
    private static boolean describesItsOwnChange(Object widget) {
        return widget instanceof DartWidget
                && Serializer.canDiff((DartWidget) widget, connectionOf(widget));
    }

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

    /** What a flush returns once everything it had is on its way: shared, as nothing completes it. */
    private static final CompletableFuture<Void> SENT = CompletableFuture.completedFuture(null);

    private static CompletableFuture<Void> update(boolean coalesce) {
        flushOpBatches();
        if (nothingQueued()) {
            return SENT;
        }

        long flushStart = TAP.flushStarted();
        List<CompletableFuture<Void>> futures = null;

        // Drain, don't copy-then-clear: clearing after the sends discarded every mark made while
        // this flush ran, and nothing re-marks them, so that state never reached Dart at all.
        // Anything dirtied from here on belongs to the next flush.
        // Every widget gets a record of its own and names its children by id, so nothing is carried
        // inside an ancestor and there is no ancestry to work out.
        FlushState flushState = FLUSH.get();
        Flush running = flushState.open();
        ArrayList<Object> flush = running.widgets;
        synchronized (dirty) {
            flushOrder(flush);
            lastQueued = null;
        }
        // A Paint the flush released has drawn by now: its ops go out ahead of this flush's records.
        if (org.eclipse.swt.widgets.PaintPacing.flushing(flush)) flushOpBatches();

        long now = System.nanoTime();
        // Everything this flush sends belongs to one moment, so it travels as one message. Only the
        // sends made inline are caught: a widget still waiting on clientReady sends when it can.
        // A flush started inside another, while one of its records is written, hands the outer one
        // back as it found it: the outer frame is still open, and sending it early would send it twice.
        Flush outer = flushState.current;
        flushState.current = running;
        // The widgets of one flush nearly always share a display, whose thread is asked once.
        Display lastDisplay = null;
        boolean lastInline = false;
        try {
        for (int i = 0; i < flush.size(); i++) {
            Object widget = flush.get(i);
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
                    if (widget instanceof DartWidget) queue((DartWidget) widget);
                    else dirty.add(widget);
                }
                continue;
            }
            if (coalescible(widget)) lastPushNanos.put(widget, now);
            CompletableFuture<Boolean> ready = getBridge(widget).clientReady;
            if (ready.isDone() && !ready.isCompletedExceptionally()) {
                // What thenRun would do on a completed future, without one per widget to wait on.
                Display display = getDisplay(widget);
                if (display != lastDisplay) {
                    lastDisplay = display;
                    lastInline = runsInline(display);
                }
                if (lastInline) {
                    sendWidget(widget);
                } else {
                    if (futures == null) futures = new ArrayList<>();
                    futures.add(sentOnDisplayThread(display, widget));
                }
            } else {
                if (futures == null) futures = new ArrayList<>();
                futures.add(ready.thenCompose(r -> {
                    Display display = getDisplay(widget);
                    if (runsInline(display)) {
                        sendWidget(widget);
                        return SENT;
                    }
                    return sentOnDisplayThread(display, widget);
                }));
            }
        }
        } finally {
            // A send made from here on goes out on its own, as one made outside any flush does, or into
            // the flush this one started inside.
            flushState.current = outer;
            long sendStart = TAP.flushSending(flushStart);
            // Each frame goes out whatever happened to the one before: a comm that fails (its client
            // gone mid-send) must not keep another comm's frames, already counted as delivered, from
            // their client. The first failure is reported once every frame has been tried.
            RuntimeException failed = null;
            for (int i = 0; i < running.batches.size(); i++) {
                MessageBatch batch = running.batches.get(i);
                try {
                    running.batchComms.get(i).send(batch);
                } catch (RuntimeException e) {
                    if (failed == null) failed = e;
                } finally {
                    releaseBatch(batch);
                }
            }
            for (int i = 0; i < running.tapes.size(); i++) {
                try {
                    sendTape(running.tapeComms.get(i), running.tapes.get(i));
                } catch (RuntimeException e) {
                    if (failed == null) failed = e;
                }
            }
            flushState.close(running);
            TAP.flushEnded(flushStart, sendStart);
            if (failed != null) throw failed;
        }
        if (futures == null) return SENT;
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /** Sends {@code widget}'s record, for a flush: on the widget's display thread. */
    private static void sendWidget(Object widget) {
            try {
                if (isDisposed(widget)) return; // widget may have been disposed while waiting for clientReady
                boolean isHidden = (widget instanceof org.eclipse.swt.widgets.DartControl)
                        && !((org.eclipse.swt.widgets.DartControl) widget).getVisible();
                if (!isNew(widget) || widget instanceof DartToolTip || widget instanceof DartMenu || isHidden) { // send with the parent
                    setNotNew(widget);
                    // Undirty if it was dirtied while waiting for clientReady. A widget the flush has
                    // just taken is not queued again, so it is not locked for.
                    if (mayBeQueued(widget)) {
                        synchronized (dirty) {
                            unqueue(widget);
                            lastQueued = null;
                        }
                    }
                    CommService comm = commFor(widget);
                    try {
                        // Written for one client, so say which before writing: whether a nested
                        // widget can travel as a name depends on whether that client holds it.
                        int outer = Serializer.enterTarget(comm == null ? 0 : comm.connectionId());
                        try {
                            if (widget instanceof DartWidget) {
                                describe(comm, (DartWidget) widget);
                            } else if (widget instanceof DartImage) {
                                // A changed image is described again, as an image record.
                                Serializer.Tape tape = openTape(comm);
                                if (tape != null) {
                                    tape.describeImage(((DartImage) widget).getApi());
                                    closeTape(comm, tape);
                                }
                            }
                        } finally {
                            Serializer.leaveTarget(outer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    setNotNew(widget);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
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
            VWidget value = w.getValue();
            if (value != null && value.wireMemo != null && ((Serializer.SentHashes) value.wireMemo).notNew) return false;
            return w.getData(DEV_EQU_SWT_NEW) == null
                    && value.sentSeq(connectionOf(widget)) == 0;
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
    /**
     * Sends {@code widget} on {@code display}'s thread; what is returned completes once it is sent, so
     * a message about the widget chained on it cannot reach the client first.
     */
    private static CompletableFuture<Void> sentOnDisplayThread(Display display, Object widget) {
        CompletableFuture<Void> sent = new CompletableFuture<>();
        display.asyncExec(() -> {
            try {
                sendWidget(widget);
            } finally {
                sent.complete(null);
            }
        });
        return sent;
    }

    private static void runOnDisplayThread(Object widget, Runnable task) {
        Display display = getDisplay(widget);
        if (runsInline(display)) {
            task.run();
            return;
        }
        display.asyncExec(task);
    }

    /** Whether a task for {@code display} runs right here: no display to run it, or this is its thread. */
    private static boolean runsInline(Display display) {
        return display == null || display.isDisposed() || display.getThread() == Thread.currentThread();
    }

    private static Object getApi(Object w) {
        if (w instanceof DartWidget) return ((DartWidget) w).getApi();
        if (w instanceof DartResource) return ((DartResource) w).getApi();
        return null;
    }

    /**
     * Serializes through this bridge's own {@link #comm()} — the form instance callers use — on the
     * channel numbered {@code channel} of the widget of type {@code type} numbered {@code id}, or on
     * the global channel when {@code type} is 0.
     */
    protected void serializeAndSend(int type, long id, int channel, Object args) throws IOException {
        serializeAndSend(comm(), type, id, channel, args);
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

    /**
     * Installs (or, with null, removes) the outbound-frame observer. Only where sends are observed
     * at all, {@code -Ddev.equo.swt.observeSends=true}: elsewhere nothing reads it.
     */
    public static void setSendObserver(SendObserver observer) {
        ObservedSends.set(observer);
    }

    /**
     * What watches this bridge's traffic, chosen once from system properties (see {@link BridgeTap}).
     * Final, so with nothing watching each call to it compiles to nothing.
     */
    static final BridgeTap TAP = BridgeTap.select();

    private static void serializeAndSend(CommService comm, int channel, Object args) throws IOException {
        serializeAndSend(comm, 0, 0, channel, args);
    }

    /**
     * Sends {@code args} as tagged values on the channel numbered {@code channel} of the widget of type
     * {@code type} numbered {@code id}, or on the global channel when {@code type} is 0.
     */
    private static void serializeAndSend(CommService comm, int type, long id, int channel, Object args) throws IOException {
        if (!dev.equo.swt.comm.TaggedValues.isPlain(args)) throw notPlain(args, ChannelHeader.name(channel));
        boolean entered = enterTarget(comm);
        try {
            sendLent(comm, dev.equo.swt.comm.TaggedValues.lend(type, id, channel, args));
        } finally {
            if (entered) Serializer.exitConnection();
        }
    }

    /**
     * Sends {@code args} on {@code w}'s channel {@code event}, the frame header written from the
     * numbers both sides know the widget's type and the channel by: no channel name is built.
     */


    /**
     * Credits what is written from here on to {@code comm}'s client, unless a caller already named one:
     * a send from anywhere else - an event reply, a pre-connect push, a draw op - goes to that client.
     */
    private static boolean enterTarget(CommService comm) {
        if (Serializer.targetConnection() != 0 || comm == null) return false;
        Serializer.enterConnection(comm.connectionId());
        return true;
    }

    /** Sends the lent frame {@code t} and gives it back. */
    private static void sendLent(CommService comm, dev.equo.swt.comm.TaggedValues t) {
        try {
            sendBytes(comm, t.buffer(), t.payloadStart(), t.size());
        } finally {
            t.release();
        }
    }

    /** Args that are not maps, lists and scalars have a binary form of their own, and go through it. */
    private static IllegalArgumentException notPlain(Object args, String eventName) {
        return new IllegalArgumentException("no tagged form for " + args.getClass().getName() + " on " + eventName);
    }

    /** The name of the channel the frame {@code frame[0, payloadStart)} opens with, for a log. */
    static String channelOf(byte[] frame, int payloadStart) {
        ChannelHeader.Decoded header = ChannelHeader.decode(frame, 0, payloadStart);
        return header == null ? "?" : header.channel();
    }


    /**
     * Runs {@code write} against {@code comm}'s binary frame: the running flush's, or, when no flush is
     * running, one of its own sent as soon as {@code write} returns.
     */
    public static void writeTape(CommService comm, java.util.function.Consumer<Serializer.Tape> write) {
        Serializer.Tape tape = openTape(comm);
        if (tape == null) return;
        write.accept(tape);
        closeTape(comm, tape);
    }

    /** {@code comm}'s tape: the running flush's, or one of its own for {@link #closeTape} to send. */
    private static Serializer.Tape openTape(CommService comm) {
        if (comm == null) return null;
        Flush running = FLUSH.get().current;
        return running == null ? Serializer.Tape.borrow(comm.connectionId()) : running.tapeFor(comm);
    }

    /** Ends what {@link #openTape} began: a tape no flush holds is sent now. */
    private static void closeTape(CommService comm, Serializer.Tape tape) {
        Flush running = FLUSH.get().current;
        if (running == null) sendTape(comm, tape);
    }

    private static void describe(CommService comm, DartWidget widget) {
        Serializer.Tape tape = openTape(comm);
        if (tape == null) return;
        tape.describe(widget);
        closeTape(comm, tape);
    }

    private static void sendTape(CommService comm, Serializer.Tape tape) {
        try {
            sendFrameOf(comm, tape);
        } finally {
            tape.release();
        }
    }

    private static void sendFrameOf(CommService comm, Serializer.Tape tape) {
        if (tape.isEmpty()) return;
        TAP.widgetFrame(tape.frame());
        tape.sendTo(comm);
    }

    /**
     * The tail of a send. {@code buffer[0, length)} is a whole frame whose payload starts at
     * {@code payloadStart}, lent by the serializer: anything that keeps it past this call copies it.
     */
    private static void sendBytes(CommService comm, byte[] buffer, int payloadStart, int length) {
        // The bytes are going out, so what was written counts as delivered. Serializing on its own
        // does not: a widget can be written to be read rather than sent.
        Serializer.markDelivered();
        TAP.sent(buffer, payloadStart, length);
        Flush running = FLUSH.get().current;
        if (running != null) {
            MessageBatch batch = running.batchFor(comm);
            batch.addFrame(buffer, payloadStart, length);
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
     * The flush running on a thread: what its inline sends join, one message per comm, and the frame
     * each comm's records go into.
     *
     * <p>None outside a flush, which is what makes a send from anywhere else — an event reply, a
     * draw op, a pre-connect push finishing on another thread — go straight out as it always did.
     * Per thread for the same reason: only the sends this flush makes inline belong to it. Reused
     * from flush to flush, so a flush allocates none of it.
     */
    private static final class Flush {
        final ArrayList<Object> widgets = new ArrayList<>();
        final ArrayList<CommService> batchComms = new ArrayList<>();
        final ArrayList<MessageBatch> batches = new ArrayList<>();
        final ArrayList<CommService> tapeComms = new ArrayList<>();
        final ArrayList<Serializer.Tape> tapes = new ArrayList<>();

        /** A flush writes for one comm or two, so they are looked up in order. */
        MessageBatch batchFor(CommService comm) {
            for (int i = 0; i < batchComms.size(); i++) {
                if (batchComms.get(i) == comm) return batches.get(i);
            }
            MessageBatch batch = borrowBatch();
            batchComms.add(comm);
            batches.add(batch);
            return batch;
        }

        Serializer.Tape tapeFor(CommService comm) {
            for (int i = 0; i < tapeComms.size(); i++) {
                if (tapeComms.get(i) == comm) return tapes.get(i);
            }
            Serializer.Tape tape = Serializer.Tape.borrow(comm.connectionId());
            tapeComms.add(comm);
            tapes.add(tape);
            return tape;
        }

        void clear() {
            widgets.clear();
            batchComms.clear();
            batches.clear();
            tapeComms.clear();
            tapes.clear();
        }
    }

    /** A thread's running flush, and the ones it can reuse: a flush can start inside another. */
    private static final class FlushState {
        Flush current;
        private final java.util.ArrayDeque<Flush> spare = new java.util.ArrayDeque<>();

        Flush open() {
            Flush flush = spare.pollFirst();
            return flush != null ? flush : new Flush();
        }

        void close(Flush flush) {
            flush.clear();
            spare.addFirst(flush);
        }
    }

    private static final ThreadLocal<FlushState> FLUSH = ThreadLocal.withInitial(FlushState::new);


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
        if (control instanceof DartWidget) {
            VWidget value = ((DartWidget) control).getValue();
            if (value != null) Serializer.sentHashes(value).notNew = true;
            else ((DartWidget) control).setData(DEV_EQU_SWT_NEW, false);
        }
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

    /** Registered only from {@code DisplayBridge}; stays unset in embed builds, whose events carry the OS's times. */
    public interface EventClock {
        /** Stamps an event arriving from {@code display}'s client. */
        void stamp(Display display);
    }

    private static volatile EventClock eventClock;

    public static void setEventClock(EventClock clock) {
        eventClock = clock;
    }

    private static void stampEventTime(DartWidget widget) {
        EventClock clock = eventClock;
        if (clock != null) clock.stamp(getDisplay(widget));
    }

    /**
     * Work held for a later step of the same client gesture, which runs it inline: a gesture the
     * client reports as several messages natively happens in one turn of the event loop, but each
     * message is queued on its own, and other events can be queued between them.
     */
    private final java.util.Queue<Runnable> held = new java.util.concurrent.ConcurrentLinkedQueue<>();

    /** Holds {@code work} on {@code widget}'s bridge until {@link #runHeld} is called there; without a bridge, posts it. */
    public static void hold(DartWidget widget, Runnable work) {
        FlutterBridge bridge = widget.getBridge();
        if (bridge == null) widget.getDisplay().asyncExec(work);
        else bridge.held.add(work);
    }

    /** Runs, on the calling thread and in the order they were held, the work held on {@code widget}'s bridge. */
    public static void runHeld(DartWidget widget) {
        FlutterBridge bridge = widget.getBridge();
        if (bridge == null) return;
        Runnable work;
        while ((work = bridge.held.poll()) != null) work.run();
    }

    /** Registers {@code cb} for the events {@code widget}'s channel numbered {@code channel} carries. */
    public static void on(DartWidget widget, int channel, Consumer<Event> cb) {
        String name = ChannelHeader.name(channel);
        boolean key = name.startsWith("Key/");
        commFor(widget).on(typeOf(widget), id(widget), channel, Event.class, ev -> {
            if (widget.isDisposed()) {
                TAP.skipped(widget, name, "skipped: widget disposed");
                return;
            }
            stampEventTime(widget);
            TAP.received(widget, name, ev);
            // A message whose body is absent deserializes to null, and handlers read the Event
            // straight away — DartText's DefaultSelection does `e.detail == SWT.ICON_CANCEL`, so a
            // bodyless Enter in a Text threw NullPointerException out of an asyncExec, which e4
            // turns into a modal "Internal Error" dialog that blocks the whole workbench. Six
            // handlers across five widgets dereference the event this way; guarding here fixes the
            // class rather than the instance. An empty Event carries detail == 0, which is what a
            // "no detail" event means to every one of them. Logged, not swallowed: a null body is
            // still worth seeing when reading a trace.
            if (ev == null) {
                TAP.skipped(widget, name, "empty body: substituting a blank Event");
                cb.accept(new Event());
                return;
            }
            if (key) ControlHelper.applyControlCharacter(ev);
            cb.accept(ev);
        });
    }

    /**
     * Registers {@code dispatch} for every channel of {@code widget} named in {@code events}
     * ("Listener/Event"), as one handler for all of them, delivering each as {@link #on} does.
     */
    public static void onClientEvents(DartWidget widget, String[] events) {
        CommService comm = commFor(widget);
        int type = typeOf(widget);
        if (comm.onType(type, Event.class, new ClientEventsByType(comm))) {
            // Found by its id from now on: a widget is entered when first sent, but one that takes
            // the client's events need never be sent itself (a DropTarget is drawn by its control).
            registerForRefresh(widget);
            return;
        }
        comm.onOwner(type, id(widget), CLIENT_EVENTS.get(widget.getClass()).channels, Event.class,
                (channel, ev) -> deliverClientEvent(widget, channel, ev));
    }

    /**
     * The client events of every widget on one comm, found by the id their channel names: a widget
     * known to the client was sent, and so can be found by its id (see {@link #registerForRefresh}).
     * One that has been disposed is not found, and its events are dropped. The ids are looked up
     * across the JVM, so a widget reached through another comm is not this one's: its client cannot
     * send events to another session's widgets.
     */
    private static final class ClientEventsByType implements CommService.TypeCallback<Event> {
        private final CommService comm;

        ClientEventsByType(CommService comm) {
            this.comm = comm;
        }

        @Override
        public Object target(String type, long id, int channel, String rest) {
            Object found = findById(id);
            if (!(found instanceof DartWidget)) return null;
            DartWidget widget = (DartWidget) found;
            if (commFor(widget) != comm) return null;
            String name = widgetName(widget);
            if (!name.equals(type) && !type.equals(alternateTypeName(name))) return null;
            ClientEvents events = CLIENT_EVENTS.get(widget.getClass());
            return events != null && events.has(channel) ? widget : null;
        }

        @Override
        public void accept(Object widget, String rest, Event ev) {
            deliverClientEvent((DartWidget) widget, rest, ev);
        }
    }

    private static String alternateTypeName(String name) {
        if (name.equals("MainComposite")) return "Composite";
        if (name.equals("Composite")) return "MainComposite";
        return null;
    }

    private static void deliverClientEvent(DartWidget widget, String channel, Event ev) {
        if (widget.isDisposed()) {
            TAP.skipped(widget, channel, "skipped: widget disposed");
            return;
        }
        stampEventTime(widget);
        TAP.received(widget, channel, ev);
        if (ev == null) {
            TAP.skipped(widget, channel, "empty body: substituting a blank Event");
            widget.clientEvent(channel, new Event());
            return;
        }
        if (channel.startsWith("Key/")) ControlHelper.applyControlCharacter(ev);
        widget.clientEvent(channel, ev);
    }

    private static final Map<Class<?>, ClientEvents> CLIENT_EVENTS = new ConcurrentHashMap<>();

    /** The events a widget class takes from the client: by name, and by the channel numbers they have. */
    private static final class ClientEvents {
        final String[] names;
        final int[] channels;
        final boolean[] numbers;

        ClientEvents(String[] names) {
            this.names = names;
            channels = new int[names.length];
            int max = 0;
            for (int i = 0; i < names.length; i++) {
                channels[i] = ChannelHeader.requireChannel(names[i]);
                max = Math.max(max, channels[i]);
            }
            numbers = new boolean[max + 1];
            for (int channel : channels) numbers[channel] = true;
        }

        boolean has(int channel) {
            return channel > 0 && channel < numbers.length && numbers[channel];
        }
    }

    /** The events a widget class takes from the client, listed once per class. */
    public static String[] clientEvents(Class<?> type, java.util.function.Supplier<String[]> list) {
        return CLIENT_EVENTS.computeIfAbsent(type, t -> new ClientEvents(list.get())).names;
    }

    /** {@code inherited} followed by {@code own}. */
    public static String[] joinEvents(String[] inherited, String... own) {
        String[] all = java.util.Arrays.copyOf(inherited, inherited.length + own.length);
        System.arraycopy(own, 0, all, inherited.length, own.length);
        return all;
    }

    /** Registers {@code cb} for the raw payloads on {@code widget}'s channel numbered {@code channel}. */
    public static void onPayload(Object widget, int channel, Consumer<byte[]> cb) {
        onPayload(widget, channel, byte[].class, cb);
    }

    /** Registers {@code cb} for the payloads on {@code widget}'s channel numbered {@code channel}, read as {@code cls}. */
    public static <T> void onPayload(Object widget, int channel, Class<T> cls, Consumer<T> cb) {
        commFor(widget).on(typeOf(widget), id(widget), channel, cls, p -> {
            TAP.receivedPayload(ChannelHeader.name(channel), p);
            cb.accept(p);
        });
    }

    public static void removeEvent(Object widget, int channel) {
        commFor(widget).remove(typeOf(widget), id(widget), channel);
    }

    /** Sends a message with no payload on {@code widget}'s channel numbered {@code channel}. */
    public static void sendEvent(Object widget, int channel) {
        commFor(widget).send(typeOf(widget), id(widget), channel, null);
    }

    /** The frame header of {@code w}'s channel numbered {@code channel}. */
    static byte[] header(Object w, int channel) {
        return ChannelHeader.encode(typeOf(w), id(w), channel);
    }

    /** Sends {@code args} on {@code resource}'s channel numbered {@code channel}: a GC's, buffered into its paint's frame. */
    public static void send(DartResource resource, int channel, Object args) {
        CommService comm = commFor(resource);
        if (resource instanceof DartGC) { DartGC gc = (DartGC) resource;
            // Flutter never learns a silently disposed GC exists: its drawing would be sent on its
            // drawable's channel and never committed.
            if (gc.silentDispose) return;
            bufferOp(comm, gc, channel, args);
            return;
        }
        if (dirty.contains(resource)) {
            CompletableFuture<Void> deferred = update().whenComplete((r, a) -> {
                try {
                    serializeAndSend(comm, typeOf(resource), id(resource), channel, args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            trackDeferredSend(deferred);
        } else {
            try {
                serializeAndSend(comm, typeOf(resource), id(resource), channel, args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** The number a GC draws as: one drawing into an Image, its drawer's, which outlives any one GC. */
    private static long gcId(DartGC gc) {
        FlutterBridge bridge = getBridge(gc);
        return bridge instanceof GCImageDrawer ? ((GCImageDrawer) bridge).channelId() : id(gc);
    }

    /** Cap on what one batch holds, so a GC that is never disposed cannot grow it without end. */
    private static final int MAX_BATCH_BYTES = 1 << 20;

    /**
     * One GC's paint from its first change or op to its {@code gcDispose}: the frame its ops go
     * into, and what it has drawn and restated so far. Kept in the GC value's {@code wireMemo}, and
     * reused for another GC once the paint ends, so a paint allocates none of it.
     */
    private static final class GcPaint implements WidgetWire.Names {
        DartGC gc;
        int type;
        long id;
        /** The drawable whose overlay the ops draw on; null for an image GC, whose drawer starts blank. */
        Object target;
        /** What the client was last shown of {@link #target}. */
        Shown shown;
        /** The frame being filled; null until the next op once one is sent. */
        GcWire batch;
        /** The ops drawn so far, as a running hash. */
        long drawn;
        /** Whether the paint restated the GC's state. */
        boolean staged;
        /** Whether the GC's state changed since it was last staged. */
        boolean stateDirty;
        /** Resources the buffered ops wrote, credited only once the batch is sent. */
        final ArrayList<Object> written = new ArrayList<>();
        /** What was last sent of each property of the GC's state. */
        final Serializer.SentHashes sent = new Serializer.SentHashes(32);
        /** An image GC's own record of what it staged: its drawer starts blank with each GC. */
        final Shown own = new Shown();

        void start(DartGC gc) {
            this.gc = gc;
            batch = null;
            drawn = EMPTY_CYCLE;
            staged = false;
            stateDirty = false;
            written.clear();
            sent.clear();
            own.clear();
        }

        /** Where the ops go: worked out again at each frame's first op, as a GC learns its owner-drawn item late. */
        void aim() {
            type = typeOf(gc);
            id = gcId(gc);
            target = getBridge(gc) instanceof GCImageDrawer ? null : GCHelper.stateKeyOf(gc);
            shown = target == null ? null : shownOf(target);
        }

        /** What the client holds of the GC's state: its drawable's, or for an image GC its own. */
        Shown stateShown() {
            return target == null ? own : shown;
        }

        @Override
        public void widget(Object widget) {
        }

        /**
         * Sends what the client lacks of an image the frame names, ahead of the frame: the frame names
         * it by id. An image drawer's frames wait in its queue, so the image waits there too.
         */
        @Override
        public void image(org.eclipse.swt.graphics.Image image) {
            CommService comm = commFor(gc);
            if (comm == null) return;
            Serializer.Tape tape = new Serializer.Tape(comm.connectionId());
            tape.describeImage(image);
            if (tape.isEmpty()) {
                tape.release();
                return;
            }
            Thread borrower = Thread.currentThread();
            Runnable send = () -> {
                tape.sendTo(comm);
                // Back into the pool of the thread that took it; one a drawer's queue runs elsewhere
                // would sit in that thread's pool, which never takes from it.
                if (Thread.currentThread() == borrower) tape.release();
            };
            // The client is told the image anew because what it shows changed. The frame names it
            // by id all the same, so a paint that only blits it is the same bytes as the last one:
            // without this, it would be dropped as a repeat and the client would keep the old image.
            drawn = (drawn ^ IMAGES_DESCRIBED.incrementAndGet()) * 0x100000001b3L;
            FlutterBridge bridge = getBridge(gc);
            if (bridge instanceof GCImageDrawer) ((GCImageDrawer) bridge).queueOp(send);
            else send.run();
        }
    }

    /** Counts the images described to a client, so each makes the paint naming it unlike any other. */
    private static final java.util.concurrent.atomic.AtomicLong IMAGES_DESCRIBED =
            new java.util.concurrent.atomic.AtomicLong();

    /** What the client was last shown of a drawable, kept while the drawable lives. */
    private static final class Shown {
        /** The client and the hash of the paint last sent, when {@link #painted}. */
        boolean painted;
        int paintConnection;
        long paintDrawn;
        /** The client and the hash of the whole GC state last staged, when {@link #stated}. */
        boolean stated;
        int stateConnection;
        long stateHash;
        /**
         * The stamp of the GC state the drawable's channel was last given, 0 for none. Every GC of a
         * control writes to the control's one channel, so a GC may send only what changed while no
         * other GC of that control has written since.
         */
        long stateSeq;

        void clear() {
            painted = false;
            stated = false;
            stateSeq = 0;
        }
    }

    /** Per drawable, what its client was last shown. */
    private static final Map<Object, Shown> shownByDrawable = new java.util.WeakHashMap<>();

    private static Shown shownOf(Object drawable) {
        synchronized (shownByDrawable) {
            Shown shown = shownByDrawable.get(drawable);
            if (shown == null) shownByDrawable.put(drawable, shown = new Shown());
            return shown;
        }
    }

    /** Paints ended on this thread, ready for its next GC. */
    private static final ThreadLocal<java.util.ArrayDeque<GcPaint>> sparePaints =
            ThreadLocal.withInitial(java.util.ArrayDeque::new);

    private static final int MAX_SPARE_PAINTS = 8;

    /** {@code gc}'s paint, started at its first change or op. */
    private static GcPaint paintOf(DartGC gc) {
        VResource value = gc.getValue();
        GcPaint paint = (GcPaint) value.wireMemo;
        if (paint != null) return paint;
        paint = sparePaints.get().pollFirst();
        if (paint == null) paint = new GcPaint();
        paint.start(gc);
        value.wireMemo = paint;
        return paint;
    }

    /** Ends {@code paint}: nothing of its GC is held past it, so neither the GC nor its Image is kept from collection. */
    private static void endPaint(GcPaint paint) {
        paint.gc.getValue().wireMemo = null;
        paint.gc = null;
        paint.target = null;
        paint.shown = null;
        paint.batch = null;
        paint.written.clear();
        java.util.ArrayDeque<GcPaint> spares = sparePaints.get();
        if (spares.size() < MAX_SPARE_PAINTS) spares.addFirst(paint);
    }

    /** Paints with a frame open, so a flush or an image drawer can find them. */
    private static final ArrayList<GcPaint> openPaints = new ArrayList<>();

    /** The GC's state changed: it is staged ahead of the GC's next op. */
    private static void gcStateChanged(DartGC gc) {
        synchronized (dirty) {
            paintOf(gc).stateDirty = true;
        }
    }

    private static void bufferOp(CommService comm, DartGC gc, int channel, Object args) {
        buffer(comm, gc, channel, args, false, null);
    }

    /** {@link #bufferOp}, with what a {@code gcDispose} that ends a control's paint repaints. */
    private static void buffer(CommService comm, DartGC gc, int channel, Object args,
            boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage) {
        GcPaint paint = paintOf(gc);
        GcWire batch = openBatch(paint);
        int connection = comm == null ? 0 : comm.connectionId();
        int outer = Serializer.enterTarget(connection);
        try {
            // The GC's state has to precede the op drawn with it, and on the same frame.
            synchronized (dirty) {
                if (paint.stateDirty) {
                    paint.stateDirty = false;
                    if (stageGcState(paint, batch, connection)) paint.staged = true;
                }
            }
            addOp(paint, batch, channel, args, fullRepaint, damage);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        } finally {
            Serializer.takeWrittenResources(paint.written);
            Serializer.leaveTarget(outer);
        }
        if (channel != WireTable.CHANNEL_GC_DISPOSE) {
            if (batch.byteSize() >= MAX_BATCH_BYTES) flushOpBatch(paint);
            return;
        }
        endCycle(paint, connection);
    }

    /** The frame {@code paint}'s next op goes into, opened at its first op or after a send. */
    private static GcWire openBatch(GcPaint paint) {
        GcWire batch = paint.batch;
        if (batch != null && batch.writer() != Thread.currentThread()) {
            // The GC changed threads: what the previous one drew goes first, then this one owns it.
            flushOpBatch(paint);
            batch = null;
        }
        if (batch == null) {
            paint.aim();
            batch = paint.batch = borrowWire(paint.type, paint.id, paint);
            synchronized (openPaints) {
                openPaints.add(paint);
            }
        }
        return batch;
    }

    /**
     * The paint's {@code gcDispose} is buffered: sends it, unless it repaints what the client
     * already shows, and ends the paint.
     */
    private static void endCycle(GcPaint paint, int connection) {
        Shown shown = paint.shown;
        // A restated description always goes (the next clear clears to it); otherwise a repaint of
        // what is already shown is dropped.
        boolean repeat = !paint.staged && shown != null && shown.painted
                && shown.paintConnection == connection && shown.paintDrawn == paint.drawn;
        if (DROP_REPEATED_REPAINTS && repeat) {
            // Nothing in this paint reaches the client, so nothing it wrote counts as held.
            paint.written.clear();
            GcWire dropped = takeBatch(paint);
            if (dropped != null) releaseWire(dropped);
            org.eclipse.swt.widgets.PaintPacing.unchanged(paint.target);
        } else {
            if (shown != null) {
                shown.painted = true;
                shown.paintConnection = connection;
                shown.paintDrawn = paint.drawn;
            }
            flushOpBatch(paint);
        }
        endPaint(paint);
    }

    /** Ends a control's paint: its {@code gcDispose}, with what it repaints. */
    public static void endPaint(DartGC gc, boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage) {
        // Flutter never learns a silently disposed GC exists: its drawing would be sent on its
        // drawable's channel and never committed.
        if (gc.silentDispose) return;
        buffer(commFor(gc), gc, WireTable.CHANNEL_GC_DISPOSE, ENDING_PAINT, fullRepaint, damage);
    }

    /** What {@link #endPaint(DartGC, boolean, org.eclipse.swt.graphics.Rectangle)} buffers: the dispose its arguments describe. */
    private static final Object ENDING_PAINT = new Object();

    /**
     * Stages a paint's GC state: as a change when this GC was the last to describe itself on the
     * channel, else whole ({@link #addGcStateIfChanged}). Either way the channel then holds this
     * GC's whole state, which is what the next change is relative to.
     */
    private static boolean stageGcState(GcPaint paint, GcWire batch, int connection) {
        DartGC gc = paint.gc;
        VResource value = gc.getValue();
        Shown drawable = id(gc) == 0 ? null : shownOf(GCHelper.stateKeyOf(gc));
        synchronized (shownByDrawable) {
            long held = drawable == null ? 0 : drawable.stateSeq;
            long base = value.sentSeq(connection);
            if (Serializer.mayDiffResources() && base != 0 && held == base && value.anyDirty()) {
                long seq = serializer.toDiffBinary(gc, base, batch, paint.sent);
                value.sent(connection, seq);
                drawable.stateSeq = seq;
                // The channel holds a state no whole description was hashed from.
                paint.stateShown().stated = false;
                return seq != base;
            }
            boolean staged = addGcStateIfChanged(paint, batch, connection);
            // Staged or already held, the channel now holds this GC's state.
            long seq = Serializer.nextWriteStamp();
            value.sent(connection, seq);
            if (drawable != null) drawable.stateSeq = seq;
            return staged;
        }
    }

    /**
     * Stages {@code gc}'s whole state, unless the client already holds that very description: pooled
     * GCs keep restating the same one.
     *
     * @return whether anything was staged.
     */
    private static boolean addGcStateIfChanged(GcPaint paint, GcWire batch, int connection) {
        WidgetWire props = serializer.wholeState(paint.gc, batch, paint.sent);
        long hash = props.hash(0, props.size());
        Shown seen = paint.stateShown();
        if (seen.stated && seen.stateConnection == connection && seen.stateHash == hash) return false;
        seen.stated = true;
        seen.stateConnection = connection;
        seen.stateHash = hash;
        batch.state(GcWire.WHOLE, props);
        return true;
    }

    /** Bisect switch: {@code -Ddev.equo.swt.gc.dropBlank=false} also sends repaints of what is already shown. */
    private static final boolean DROP_REPEATED_REPAINTS =
            !"false".equals(System.getProperty("dev.equo.swt.gc.dropBlank"));

    /** For a paint the client asked for: what it shows is no longer what was last sent. */
    public static void forgetWhatIsShown(Object drawable) {
        if (drawable == null) return;
        synchronized (shownByDrawable) {
            Shown shown = shownByDrawable.get(drawable);
            if (shown != null) {
                shown.painted = false;
                shown.stated = false;
            }
        }
    }

    /** Appends an op to a GC's frame, folding its record into the paint's running hash. */
    private static void addOp(GcPaint paint, GcWire batch, int channel, Object args,
            boolean fullRepaint, org.eclipse.swt.graphics.Rectangle damage) {
        int from = batch.byteSize();
        if (args instanceof GcWire.Op) {
            batch.op((GcWire.Op) args);
        } else if (args == ENDING_PAINT) {
            batch.dispose(fullRepaint, damage, 0, false, false);
        } else if (args instanceof GcWire.Dispose) {
            batch.dispose((GcWire.Dispose) args);
        } else {
            throw new IllegalArgumentException("no wire form for " + (args == null ? "nothing" : args.getClass().getName())
                    + " on the GC channel " + ChannelHeader.name(channel));
        }
        paint.drawn = fnv(paint.drawn, batch.buffer(), from, batch.byteSize() - from);
    }

    /** FNV offset basis: the hash of a paint that has buffered nothing. */
    private static final long EMPTY_CYCLE = 0xcbf29ce484222325L;

    private static long fnv(long hash, byte[] bytes, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            hash = (hash ^ (bytes[i] & 0xFF)) * 0x100000001b3L;
        }
        return hash;
    }

    /** An image GC's paint is ended by its drawer, not by a {@code gcDispose}, so the drawer flushes it. */
    public static void flushBufferedOps(GCImageDrawer drawer) {
        GcPaint paint = paintBuffering(drawer);
        if (paint != null) flushOpBatch(paint);
    }

    /**
     * Sends an image GC's ops with the {@code gcDispose} that ends them as their last record.
     *
     * @return false when the GC buffered no ops for it to join.
     */
    public static boolean flushBufferedOpsWith(GCImageDrawer drawer, GcWire.Dispose dispose) {
        GcPaint paint = paintBuffering(drawer);
        if (paint == null) return false;
        paint.batch.dispose(dispose);
        flushOpBatch(paint);
        return true;
    }

    /** Drops an image GC's buffered ops: its drawing was abandoned and will never be rendered. */
    public static void discardBufferedOps(GCImageDrawer drawer) {
        GcPaint paint = paintBuffering(drawer);
        if (paint == null) return;
        // Whatever these ops wrote never travels, so nothing in them is credited as held.
        paint.written.clear();
        GcWire batch = takeBatch(paint);
        if (batch != null) releaseWire(batch);
    }

    /** The paint whose ops {@code drawer} will draw, if one has any buffered. */
    private static GcPaint paintBuffering(GCImageDrawer drawer) {
        synchronized (openPaints) {
            for (int i = 0; i < openPaints.size(); i++) {
                GcPaint paint = openPaints.get(i);
                if (getBridge(paint.gc) == drawer) return paint;
            }
        }
        return null;
    }

    /** Puts a GC's buffered ops on the wire, for a caller about to block on an answer to one. */
    public static void flushOps(Object resource) {
        if (!(resource instanceof DartGC)) return;
        DartGC gc = (DartGC) resource;
        GcPaint paint = (GcPaint) gc.getValue().wireMemo;
        if (paint != null) flushOpBatch(paint);
        // An Image-backed GC buffers its ops until the drawer is started, and only a caller
        // wanting an answer starts it.
        FlutterBridge gcBridge = getBridge(gc);
        if (gcBridge instanceof GCImageDrawer) ((GCImageDrawer) gcBridge).startForPendingReply();
    }

    /** Takes {@code paint}'s frame out of it, so no later op adds to it. */
    private static GcWire takeBatch(GcPaint paint) {
        GcWire batch;
        synchronized (openPaints) {
            batch = paint.batch;
            if (batch == null) return null;
            paint.batch = null;
            openPaints.remove(paint);
        }
        batch.own(null);
        return batch;
    }

    private static void flushOpBatch(GcPaint paint) {
        DartGC gc = paint.gc;
        GcWire batch = takeBatch(paint);
        if (batch == null) {
            paint.written.clear();
            return;
        }
        if (batch.isEmpty()) {
            paint.written.clear();
            releaseWire(batch);
            return;
        }
        TAP.gcFrame(batch);
        CommService comm = commFor(gc);
        int connection = comm == null ? 0 : comm.connectionId();
        FlutterBridge bridge = getBridge(gc);
        if (bridge instanceof GCImageDrawer) {
            // Through the drawer's queue: the ops must not reach Flutter before GCDrawer.standalone
            // registers its listeners. What they carry counts as held once they are sent: a drawer
            // given up on drops its queue, and an image credited then would never be sent again.
            java.util.List<Object> written = paint.written.isEmpty()
                    ? java.util.Collections.emptyList() : new ArrayList<>(paint.written);
            paint.written.clear();
            Thread borrower = Thread.currentThread();
            ((GCImageDrawer) bridge).queueOp(() -> {
                batch.sendTo(comm);
                Serializer.creditResources(written, connection);
                // As the tape of an image's record: given back only on the thread that took it.
                if (Thread.currentThread() == borrower) releaseWire(batch);
            });
            return;
        }
        batch.sendTo(comm);
        // The bytes are on their way, so what they carried counts as held from here on.
        Serializer.creditResources(paint.written, connection);
        paint.written.clear();
        releaseWire(batch);
    }

    private static final ThreadLocal<java.util.ArrayDeque<GcWire>> spareWires =
            ThreadLocal.withInitial(java.util.ArrayDeque::new);

    private static GcWire borrowWire(int type, long id, GcPaint paint) {
        GcWire wire = spareWires.get().pollFirst();
        if (wire == null) wire = new GcWire();
        wire.begin(type, id);
        wire.own(paint.gc);
        wire.images(paint);
        return wire;
    }

    private static void releaseWire(GcWire wire) {
        java.util.ArrayDeque<GcWire> spares = spareWires.get();
        // One outsized frame must not pin its buffer for the life of the thread.
        if (spares.size() < MAX_SPARE_BATCHES && wire.buffer().length <= 2 * MAX_BATCH_BYTES) spares.addFirst(wire);
    }

    /** Nothing this thread buffered may outlast its event-loop turn, whatever disposed the GC or didn't. */
    static void flushOpBatches() {
        Thread current = Thread.currentThread();
        while (true) {
            GcPaint mine = null;
            synchronized (openPaints) {
                for (int i = 0; i < openPaints.size(); i++) {
                    GcPaint paint = openPaints.get(i);
                    if (paint.batch.writer() == current) {
                        mine = paint;
                        break;
                    }
                }
            }
            if (mine == null) return;
            flushOpBatch(mine);
        }
    }

    // A send through the "dirty" branch above defers the actual wire send to an arbitrary
    // later point. An unbuffered, immediate send issued afterwards (e.g. releasing a remote
    // image cache entry a still-deferred draw references) can then physically overtake it on
    // the wire. Track every deferred send so such a caller can wait for the backlog to drain.
    private static final java.util.Set<CompletableFuture<?>> pendingDeferredSends =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    @SuppressWarnings("CollectionUndefinedEquality") // a future is equal only to itself, which is what this set tracks
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
    public static void sendNow(DartWidget resource, int channel, Object args) {
        try {
            serializeAndSend(commFor(resource), typeOf(resource), id(resource), channel, args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Sends {@code args} on {@code comm} on the global channel numbered {@code channel} ({@link WireTable}). */
    public static void send(CommService comm, int channel, Object args) {
        try {
            serializeAndSend(comm, channel, args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends {@code fields} on {@code widget}'s channel numbered {@code channel} as the binary wire writes an
     * object's fields, which the client reads straight into its value class: what an event carries
     * when an answer is one, with no map of names to build or read.
     */
    public static void sendFields(DartWidget widget, int channel, WireObject fields) {
        CommService comm = commFor(widget);
        if (comm == null) return;
        WidgetWire w = WidgetWire.borrow();
        try {
            w.reset();
            int payloadStart = w.header(typeOf(widget), id(widget), channel);
            w.writeFields(fields);
            sendBytes(comm, w.buffer(), payloadStart, w.size());
        } finally {
            WidgetWire.giveBack(w);
        }
    }

    /** Sends {@code args} on {@code resource}'s channel numbered {@code channel}, after the update it is queued for. */
    public static void send(DartWidget resource, int channel, Object args) {
        if (drawnByParent(resource))
            return;
        int type = typeOf(resource);
        long id = id(resource);
        CommService comm = commFor(resource);
        boolean queued;
        synchronized (dirty) {
            queued = isQueued(resource);
        }
        if (queued) {
            update().whenComplete((r, a) -> {
                try {
                    serializeAndSend(comm, type, id, channel, args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try {
                serializeAndSend(comm, type, id, channel, args);
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
    protected <P> void onClientReady(int ownerType, long ownerId, Class<P> type, java.util.function.BiConsumer<P, Boolean> each) {
        comm().on(ownerType, ownerId, WireTable.CHANNEL_CLIENT_READY, type, p -> {
            if (p instanceof org.eclipse.swt.widgets.ClientReadyPayload) {
                ((org.eclipse.swt.widgets.ClientReadyPayload) p).requireSameWireTable();
            }
            // A (re)attaching client cannot be assumed to hold anything already sent.
            boolean first = !clientReady.isDone();
            if (first) {
                if (BootTrace.enabled()) BootTrace.mark("clientReady " + ChannelHeader.typeName(ownerType) + "/" + ownerId);
                System.out.println("ClientReady " + ChannelHeader.typeName(ownerType) + "/" + ownerId);
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
        onClientReady(typeOf(control), id(control), payloadClass, (p, first) -> {
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

    /**
     * The resource last added to {@link #dirty}, while it is still there: a GC paint marks its state
     * once per setter, and the lock is only needed to queue it the first time. Set and cleared only
     * under the lock, and cleared by every removal, so when it names a resource, that resource is
     * queued.
     */
    private static volatile Object lastQueued;

    /**
     * {@link #lastQueued}'s hash when it was added. A GC's hash can change once it is set up, and the
     * set holds it under the one it had: under a new hash it has to be added again to be found.
     */
    private static volatile int lastQueuedHash;

    public void dirty(DartResource resource) {
        if (resource == null)
            return;
        if (resource instanceof DartGC) {
            gcStateChanged((DartGC) resource);
            return;
        }
        if (lastQueued == resource && resource.hashCode() == lastQueuedHash) {
            wakeForDirty();
            return;
        }
        registerForRefresh(resource);
        synchronized (dirty) {
            dirty.add(resource);
            lastQueuedHash = resource.hashCode();
            lastQueued = resource;
        }
        wakeForDirty();
    }

    public void dirty(DartWidget widget) {
        if (widget == null || drawnByParent(widget))
            return;
        registerForRefresh(widget);
        synchronized (dirty) {
            queue(widget);
        }
        wakeForDirty();
    }

    /** A control its parent draws itself, such as a CCombo's field, has no client-side peer: nothing is sent for it. */
    private static boolean drawnByParent(DartWidget widget) {
        if (!(widget instanceof DartControl))
            return false;
        Composite parent = ((DartControl) widget)._parent();
        return parent != null && parent.getImpl() instanceof DartComposite
                && ((DartComposite) parent.getImpl())._drawsItself(((DartControl) widget).getApi());
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
        // A control's GC shares the control's id; recording it would shadow the control.
        if (w instanceof DartGC) return;
        // Every change marks its widget, and an entry stays until the widget is disposed: a widget
        // remembers it has one rather than looking it up each time.
        VWidget value = w instanceof DartWidget ? ((DartWidget) w).getValue() : null;
        Serializer.SentHashes mark = value != null ? Serializer.sentHashes(value) : null;
        if (mark != null && mark.registered) return;
        Object api = getApi(w);
        if (api == null) return;
        long id = api.hashCode();
        java.lang.ref.WeakReference<Object> held = widgetsById.get(id);
        if (held == null || held.get() != w) widgetsById.put(id, new java.lang.ref.WeakReference<>(w));
        if (mark != null) mark.registered = true;
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
            flushOrder(new ArrayList<>());
            lastQueued = null;
        }
    }

    static boolean isDirty(Object widget) {
        synchronized (dirty) {
            return isQueued(widget);
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
            for (int i = 0, n = dirtyWidgets.size(); i < n; i++) {
                DartWidget widget = dirtyWidgets.get(i);
                if (((Serializer.SentHashes) widget.getValue().wireMemo).queued && pushDue(widget, now)) return true;
            }
            return false;
        }
    }

    public static String widgetName(Object w) {
        if (w instanceof DartWidget) return Config.presentedName((DartWidget) w, CLASS_NAMES.get(w.getClass()));
        return CLASS_NAMES.get(w.getClass());
    }

    /** The number of the type {@code w} is presented as: its class's, unless it is presented as another. */
    public static int typeOf(Object w) {
        String own = CLASS_NAMES.get(w.getClass());
        if (w instanceof DartWidget) {
            String presented = Config.presentedName((DartWidget) w, own);
            if (presented != own) return ChannelHeader.requireType(presented);
        }
        return CLASS_TYPES.get(w.getClass());
    }

    /** Each class's type number, by the name {@link #CLASS_NAMES} gives it. */
    private static final ClassValue<Integer> CLASS_TYPES = new ClassValue<Integer>() {
        @Override
        protected Integer computeValue(Class<?> type) {
            return ChannelHeader.requireType(CLASS_NAMES.get(type));
        }
    };

    /** What a class's instances are named on the wire: a Dart or Swt implementation by its widget's name. */
    private static final ClassValue<String> CLASS_NAMES = new ClassValue<String>() {
        @Override
        protected String computeValue(Class<?> type) {
            String simple = type.getSimpleName();
            boolean impl = DartWidget.class.isAssignableFrom(type) || DartResource.class.isAssignableFrom(type);
            return (impl ? simple.substring(4) : simple).intern();
        }
    };

    static {
        AbstractBinaryCommService.setAlternateTypes("MainComposite", "Composite");
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
        CommService comm = comm();
        if (comm == null) return;
        String name = widgetName(control);
        long id = id(control);
        comm.removeOwner(ChannelHeader.requireType(name), id);
        String alternate = alternateTypeName(name);
        if (alternate != null) comm.removeOwner(ChannelHeader.requireType(alternate), id);
    }

    /**
     * Drops what a disposed {@code widget} left registered: every handler it registered, under
     * either of its names, and its entry in the table refresh requests are looked up in. The comm is
     * found as {@link #commFor} found it rather than through the widget's bridge: an Item has none,
     * and its handlers would otherwise keep it, and all it refers to, reachable.
     */
    public static void releaseRegistrations(DartWidget widget) {
        Object api = widget.getApi();
        if (api != null) {
            // Ids are identity hash codes, so another widget may hold this one's id by now.
            widgetsById.compute(api.hashCode(), ref -> {
                Object held = ref == null ? null : ref.get();
                return held == null || held == widget || held == api ? null : ref;
            });
        }
        CommService comm = commFor(widget);
        if (comm == null) return;
        String name = widgetName(widget);
        long id = id(widget);
        comm.removeOwner(ChannelHeader.requireType(name), id);
        String alternate = alternateTypeName(name);
        if (alternate != null) comm.removeOwner(ChannelHeader.requireType(alternate), id);
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

    /**
     * Makes {@code control} its shell's active control, announcing SWT.Activate/Deactivate with
     * {@code detail}, when the shell tracks that itself. False when it does not -- a native shell --
     * and the caller has to announce the change on its own.
     */
    public boolean setActiveControl(DartControl control, int detail) {
        return false;
    }

    /** Moves the client's keyboard focus onto {@code control}, which Java has just focused itself. */
    public void focusClient(DartControl control) {
    }

    /** The client moved its focus to {@code control}: tracked like {@link #setFocus}, without echoing it back. */
    public void clientFocused(DartControl control) {
        setFocus(control);
    }

    /**
     * Whether a client report that it focused {@code control} is older than a focus request Java has
     * made since, so acting on it -- activating the control's part included -- would undo that request.
     */
    public boolean staleFocusReport(DartControl control) {
        return false;
    }

    public boolean hasFocus(DartControl control) {
        return false;
    }

    /**
     * Makes {@code control} the active control of its shell, sending Deactivate to the chain that was active
     * and Activate, carrying {@code detail}, to the new one. False when this bridge leaves activation to its
     * caller.
     */
    public boolean activate(DartControl control, int detail) {
        return false;
    }

    /** Clears the tracked focus holder if it is {@code control}. No-op unless a Display-level bridge
     *  tracks focus (see the whole-tree surface); embedded/other bridges have nothing to clear. */
    public void clearFocus(DartControl control) {
    }

    public Object container(DartComposite parent) {
        return null;
    }

    /**
     * Whether the window hosting {@code shell} currently has the keyboard focus the OS grants.
     *
     * <p>True by default: a surface with no window of its own -- a page in a browser tab, an
     * embedded view inside a native parent -- has no separate notion of being the active window, and
     * the shell's own visibility is the best answer available. A surface that owns a top-level window
     * overrides this, because there "visible" and "active" are different facts and a Display that
     * conflates them reports an active shell while the user is in another application.
     */
    public boolean hostsActiveWindow(Object shell) {
        return true;
    }

    /** Screen position of the window content that hosts {@code control}: what a point at (0, 0) of its
     *  top-level shell maps to in display coordinates. */
    public Point getWindowOrigin(DartControl control) {
        return new Point(0, 0);
    }

    /**
     * Whether {@code shell} is the top-level shell this bridge shows as its host window, drawn without
     * a Flutter title bar; {@code null} when the bridge does not host shells and the caller must decide
     * from the shell alone.
     */
    public Boolean hostsAsMainShell(Shell shell) {
        return null;
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
        CommService comm = comm();
        if (comm != null) comm.send(0, 0, WireTable.CHANNEL_SWT_EVOLVE_PROPERTIES, WidgetWire.fieldsOf(getConfigFlags()));
    }

    protected void sendSwtEvolveProperties() {
        // Only under the debug switch: rendering the flags is slow on first use, and this runs on the
        // comm thread before the first ClientReady is posted to the UI thread.
        if (Config.debug) System.out.println("will send: " + getConfigFlags());
        broadcastSwtEvolveProperties();
    }

}


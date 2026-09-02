package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import java.util.Map;
import java.util.WeakHashMap;

public class ControlHelper {

    // True while a Flutter-originated KeyDown is being dispatched. Widgets that edit content on
    // KeyDown (StyledText.handleKey) consult it to skip re-applying what the Flutter editor
    // already inserted; Java-simulated keys (upstream tests, apps) keep the full SWT behavior.
    private static final ThreadLocal<Boolean> FLUTTER_KEY = ThreadLocal.withInitial(() -> false);

    public static int inPaintDepth;

    // Area each control still owes a Paint for, unioned as redraw() calls arrive and consumed by
    // the paint that follows. Absent, or a null value, means the whole client area.
    private static final Map<DartControl, Rectangle> pendingDamage = new WeakHashMap<>();

    // Area the Paint being dispatched is scoped to, null when it covers the whole client area. A GC
    // opened during that dispatch carries it to Flutter, which composites within it.
    private static Rectangle paintDamage;

    /** The damage the in-flight Paint is scoped to, or null outside one / for a full-area Paint. */
    public static Rectangle currentPaintDamage() {
        return paintDamage;
    }

    public static void sendFlutterKeyDown(DartWidget widget, Event event) {
        FLUTTER_KEY.set(true);
        try {
            widget.sendEvent(SWT.KeyDown, event);
        } finally {
            FLUTTER_KEY.set(false);
        }
        translateTraversal(widget, event);
    }

    /**
     * Dart controls have no OS window proc, so nothing turns an incoming KeyDown into an
     * {@link SWT#Traverse} event the way {@code SwtControl.translateTraversal} does natively.
     * Popups and dialogs (e.g. the Command Palette) close on Escape via a Traverse listener on
     * their Shell, which never fires without this translation. Mirror the native ancestor walk by
     * delegating to {@code Control.traverse(int, Event)}, which sends SWT.Traverse up the parent
     * chain to the Shell.
     */
    private static void translateTraversal(DartWidget widget, Event keyEvent) {
        if (!(widget instanceof DartControl))
            return;
        DartControl origin = (DartControl) widget;
        if (origin.isDisposed())
            return;
        int detail;
        switch (keyEvent.keyCode) {
            case SWT.ESC:
                detail = SWT.TRAVERSE_ESCAPE;
                break;
            default:
                return;
        }
        Control start = resolveTraverseStart(origin);
        if (start == null || start.isDisposed() || !(start.getImpl() instanceof DartControl))
            return;
        Event event = new Event();
        event.character = keyEvent.character;
        event.keyCode = keyEvent.keyCode;
        event.keyLocation = keyEvent.keyLocation;
        event.stateMask = keyEvent.stateMask;
        event.doit = true;
        ((DartControl) start.getImpl()).traverse(detail, event);
    }

    /**
     * Native SWT starts a traversal at {@code display.getFocusControl()}, not necessarily the widget
     * that observed the key. When the key was forwarded by an actual control that control is already
     * the right start. But a shell-level Escape (the shell's Flutter focus scope caught the key
     * because no child holds Flutter focus) must start at the control the app's Traverse listener
     * sits on — the focused control — or the walk fires only on the shell and misses the listener.
     * {@code getFocusControl()} is Java-side tracked (see {@code DisplayBridge}), so it is set even
     * though no Flutter FocusIn reached Java.
     */
    private static Control resolveTraverseStart(DartControl origin) {
        Control self = origin.getApi();
        if (!(self instanceof Shell))
            return self;
        Shell shell = (Shell) self;
        Control fc = shell.getDisplay().getFocusControl();
        if (fc != null && !fc.isDisposed() && fc.getShell() == shell && fc.getImpl() instanceof DartControl)
            return fc;
        return self;
    }

    public static boolean isFlutterOriginatedKey() {
        return FLUTTER_KEY.get();
    }

    /**
     * Derives and fires an {@link SWT#Traverse} event from a Flutter-originated KeyDown, matching
     * native SWT's key&rarr;traversal mapping (Tab/Shift+Tab, arrows, Enter, Ctrl+PageUp/Down) and the
     * Alt+letter mnemonic. This is the whole-tree analogue of the platform's {@code translateTraversal}:
     * it lets a Display Traverse filter (e.g. Eclipse's command key bindings, hooked via
     * {@code Display.addFilter(SWT.Traverse, …)}) and any {@code TraverseListener} see traversal keys.
     * Focus movement itself stays with Flutter — this only surfaces the event; it does not run SWT's
     * traversal (unlike {@link DartControl#traverse(int, char, int, int, int, boolean)}).
     *
     * <p>Escape is intentionally excluded: {@link #translateTraversal} already emits it (via the full
     * {@code Control.traverse}, so popups/dialogs still close) from {@link #sendFlutterKeyDown}, which
     * runs first in every path. Handling it here too would fire {@code SWT.Traverse} twice.
     */
    public static boolean sendFlutterTraverse(DartWidget widget, Event keyEvent) {
        // A KeyDown listener can dispose the control the key was routed to — Close All Editors
        // disposes the focused editor — and the caller's disposal guard ran before that dispatch.
        if (widget.isDisposed())
            return true;
        int detail = traverseDetail(keyEvent.keyCode, keyEvent.stateMask);
        boolean mnemonic = detail == SWT.TRAVERSE_NONE && isMnemonicTrigger(keyEvent);
        if (mnemonic)
            detail = SWT.TRAVERSE_MNEMONIC;
        if (detail == SWT.TRAVERSE_NONE)
            return true;
        Event e = new Event();
        e.character = keyEvent.character;
        e.keyCode = keyEvent.keyCode;
        e.stateMask = keyEvent.stateMask;
        e.detail = detail;
        // Arrow traversal is off by default (the focused control consumes arrows); the rest default
        // to performing the traversal — the same doit defaults as Control.traverse(...).
        e.doit = detail != SWT.TRAVERSE_ARROW_NEXT && detail != SWT.TRAVERSE_ARROW_PREVIOUS;
        widget.sendEvent(SWT.Traverse, e);
        // An Alt+letter mnemonic that a Traverse listener/filter didn't veto activates the matching
        // widget (Button click, Label→next focus, Group→first child, TabItem select) in the shell.
        if (mnemonic && e.doit && widget instanceof DartControl dc && !dc.getApi().isDisposed()) {
            MnemonicHelper.dispatch(dc.getApi(), (char) keyEvent.keyCode);
        }
        return e.doit;
    }

    /** Answers the client's held context menu after the MenuDetect listeners have run. */
    public static void sendMenuDetectVerdict(DartWidget widget, Event e) {
        dev.equo.swt.FlutterBridge.send(widget, "menu/verdict", java.util.Map.of("doit", e.doit));
    }

    /**
     * Whether the client withholds its own traversal for this key. Only Tab: it is the only key
     * Flutter traverses on by itself. Java must publish a verdict for exactly these keys — the
     * client matches by order, so an unpaired verdict would resolve the wrong proposal.
     */
    public static boolean isGatedTraversal(Event keyEvent) {
        return keyEvent.keyCode == SWT.TAB;
    }

    /** True when the key event is an {@code Alt+<letter/digit>} mnemonic trigger (no Ctrl/Command). */
    private static boolean isMnemonicTrigger(Event ev) {
        if ((ev.stateMask & SWT.ALT) == 0)
            return false;
        if ((ev.stateMask & (SWT.CTRL | SWT.COMMAND)) != 0)
            return false;
        int key = ev.keyCode;
        return key > 0 && key <= 0xFFFF && (key & SWT.KEYCODE_BIT) == 0
                && Character.isLetterOrDigit((char) key);
    }

    private static int traverseDetail(int keyCode, int stateMask) {
        switch (keyCode) {
            // Escape is handled by translateTraversal() (from sendFlutterKeyDown, which runs first),
            // so it is deliberately not mapped here — see sendFlutterTraverse's javadoc.
            case SWT.CR:
                return SWT.TRAVERSE_RETURN;
            case SWT.ARROW_DOWN:
            case SWT.ARROW_RIGHT:
                return SWT.TRAVERSE_ARROW_NEXT;
            case SWT.ARROW_UP:
            case SWT.ARROW_LEFT:
                return SWT.TRAVERSE_ARROW_PREVIOUS;
            case SWT.TAB:
                return (stateMask & SWT.SHIFT) != 0 ? SWT.TRAVERSE_TAB_PREVIOUS : SWT.TRAVERSE_TAB_NEXT;
            case SWT.PAGE_DOWN:
                return (stateMask & SWT.CTRL) != 0 ? SWT.TRAVERSE_PAGE_NEXT : SWT.TRAVERSE_NONE;
            case SWT.PAGE_UP:
                return (stateMask & SWT.CTRL) != 0 ? SWT.TRAVERSE_PAGE_PREVIOUS : SWT.TRAVERSE_NONE;
            default:
                return SWT.TRAVERSE_NONE;
        }
    }

    private static Control walkToSwtAncestor(Control control, int[] offset) {
        Control current = control;
        while (current != null && (current.getImpl() instanceof DartControl)) {
            Rectangle bounds = current.getBounds();
            offset[0] += bounds.x;
            offset[1] += bounds.y;
            if (current instanceof Shell) {
                int st = current.getStyle();
                boolean showsTitleBar = (st & SWT.NO_TRIM) == 0
                    && ((st & SWT.TITLE) != 0 || (st & SWT.CLOSE) != 0);
                if (showsTitleBar && !isMainShell((Shell) current)) {
                    offset[1] += (st & SWT.TOOL) != 0 ? 22 : 30;
                }
                return null;
            }
            current = current.getParent();
        }
        return current;
    }

    private static boolean isMainShell(Shell shell) {
        Rectangle b = shell.getBounds();
        if (b.x != 0 || b.y != 0)
            return false;
        int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;
        if ((shell.getStyle() & modal) != 0)
            return false;
        if (b.width == 1024 && b.height == 768)
            return true;
        Rectangle view = shell.getMonitor().getClientArea();
        return b.width >= Math.round(view.width * 0.8f)
            && b.height >= Math.round(view.height * 0.8f);
    }

    static Point toDisplay(DartControl dartControl, int x, int y) {
        int[] offset = new int[2];
        Control ancestor = walkToSwtAncestor(dartControl.getApi(), offset);
        if (ancestor != null) {
            return ancestor.toDisplay(x + offset[0], y + offset[1]);
        }
        return new Point(x + offset[0], y + offset[1]);
    }

    static Point toControl(DartControl dartControl, int x, int y) {
        int[] offset = new int[2];
        Control ancestor = walkToSwtAncestor(dartControl.getApi(), offset);
        if (ancestor != null) {
            Point result = ancestor.toControl(x, y);
            return new Point(result.x - offset[0], result.y - offset[1]);
        }
        return dartControl.display.map(null, dartControl.getApi(), x, y);
    }

    // Its own latch, not drawCount: that is SWT's setRedraw counter, so a queued paint that moved it
    // made the control look to everything else as though drawing had been turned off.
    private static final Set<DartControl> paintQueued =
        Collections.newSetFromMap(new WeakHashMap<>());

    static void paint(DartControl c, Event e) {
        if (c.drawCount > 0 || paintQueued.contains(c)) return;
        damageAll(c);
        firePaint(c);
    }

    /**
     * Declares that what the control paints is out of date. Reachable from the custom-widget
     * package, unlike Widget.hooks(int).
     */
    public static void markDamaged(DartControl c) {
        if (c.hooks(SWT.Paint))
            paint(c);
    }

    /** As {@link #markDamaged(DartControl)}, for an invalidation that named the area it dirtied. */
    public static void markDamaged(DartControl c, int x, int y, int width, int height) {
        if (c.hooks(SWT.Paint))
            paint(c, x, y, width, height);
    }

    public static void paint(DartControl c) {
        damageAll(c);
        schedulePaint(c);
    }

    /**
     * A {@code redraw(x, y, width, height, all)} whose rectangle the following Paint must carry.
     * Repeated invalidations before that Paint runs are unioned, the way SWT merges paint requests.
     */
    public static void paint(DartControl c, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        Rectangle damage = new Rectangle(x, y, width, height);
        synchronized (pendingDamage) {
            if (!pendingDamage.containsKey(c)) {
                pendingDamage.put(c, damage);
            } else {
                Rectangle pending = pendingDamage.get(c);
                // null is the whole client area, which already contains anything added to it.
                if (pending != null) pending.add(damage);
            }
        }
        schedulePaint(c);
    }

    private static void damageAll(DartControl c) {
        synchronized (pendingDamage) {
            pendingDamage.put(c, null);
        }
    }

    /** The area to paint, clamped to {@code bounds}, or null when nothing is left to paint. */
    private static Rectangle takeDamage(DartControl c, Rectangle bounds) {
        Rectangle damage;
        synchronized (pendingDamage) {
            if (!pendingDamage.containsKey(c)) return bounds;
            damage = pendingDamage.remove(c);
        }
        if (damage == null) return bounds;
        damage.intersect(bounds);
        return damage.isEmpty() ? null : damage;
    }

    private static void schedulePaint(DartControl c) {
        if (c.drawCount > 0 || !paintQueued.add(c))
            return;
        c.getDisplay().asyncExec(() -> {
            paintQueued.remove(c);
            // Drawing may have been turned off in between; setRedraw(true) delivers the Paint then.
            if (c.drawCount > 0)
                return;
            firePaint(c);
            c.dirty();
        });
    }

    private static void firePaint(DartControl c) {
        if (c.isDisposed()) return;
        Composite parent = c.getParent();
        while (parent != null) {
            if (parent.isDisposed())
                return;
            parent = parent.getParent();
        }
        Rectangle bounds = c.getBounds();
        // Native SWT never delivers a Paint event for an empty area. Some controls
        // (e.g. FormText) allocate a back-buffer Image of the paint size and would
        // throw ERROR_INVALID_ARGUMENT (ImageData with width/height <= 0) when asked
        // to paint a 0-sized region. This happens here because a Paint can be
        // dispatched re-entrantly while bounds are still 0 (e.g. event loop pumped
        // from DartImage.getImageData during serialization).
        if (bounds.width <= 0 || bounds.height <= 0)
            return;
        Rectangle clientArea = new Rectangle(0, 0, bounds.width, bounds.height);
        Rectangle damage = takeDamage(c, clientArea);
        if (damage == null)
            return;
        boolean scoped = !damage.equals(clientArea);
        // Mark that a paint is in progress. sendEvent(SWT.Paint) runs the paint handler
        // synchronously, and that handler (plus the GC dispose below) can pump the SWT event
        // loop via DartImage.getImageData. While inPaintDepth > 0, DartDisplay.runDeferredEvents
        // leaves queued input events untouched, so a click can't be dispatched re-entrantly
        // against widgets that are mid-teardown. Balanced in a finally; nested paints just
        // increment further and unwind cleanly.
        inPaintDepth++;
        try {
            // draw2d drives its own painting from the LightweightSystem, so a FigureCanvas must not
            // get this synthetic full-area Paint. Without draw2d on the classpath, nothing to skip.
            try {
                if (!Class.forName("org.eclipse.draw2d.FigureCanvas").isInstance(c.getApi())) {
                    sendPaint(c, damage, scoped);
                }
            } catch (ClassNotFoundException ex) {
                sendPaint(c, damage, scoped);
            }
        } finally {
            inPaintDepth--;
        }
    }

    private static void sendPaint(DartControl c, Rectangle damage, boolean scoped) {
        Event event = new Event();
        event.x = damage.x;
        event.y = damage.y;
        event.width = damage.width;
        event.height = damage.height;
        Rectangle outer = paintDamage;
        paintDamage = scoped ? damage : null;
        try {
            event.gc = new GC(c.getApi());
            // SWT confines a paint handler to the damaged area by clipping the GC, and compositions
            // read that clip back to confine their own children to it.
            if (scoped) event.gc.setClipping(damage);
            c.sendEvent(SWT.Paint, event);
            event.gc.dispose();
        } finally {
            paintDamage = outer;
        }
    }

    public static void setEnabled(DartControl c, boolean enabled) {
        boolean newValue = enabled;
        if (!java.util.Objects.equals(c.enabled, newValue)) {
            c.dirty();
        }
        c.checkWidget();
        if (((c.getApi().state & DartWidget.DISABLED) == 0) == enabled)
            return;
        Control control = null;
        boolean fixFocus = false;
        if (!enabled) {
//            if (((SwtDisplay) c.display.getImpl()).focusEvent != SWT.FocusOut) {
//                control = c.display.getFocusControl();
//                fixFocus = c.isFocusAncestor(control);
//            }
        }
        if (enabled) {
            c.getApi().state &= ~DartWidget.DISABLED;
        } else {
            c.getApi().state |= DartWidget.DISABLED;
        }
        c.enabled = newValue;
        c.enableWidget(enabled);
        if (fixFocus)
            c.fixFocus(control);
    }

    /**
     * Takes the focus and announces the activation that goes with it, so a control that focuses
     * itself programmatically still reaches an embedding workbench's active-part tracking — there is
     * no OS focus here to generate it. Deliberately does NOT send SWT.FocusIn: a control that
     * re-focuses one of its own children from its own FocusIn handler (CCombo does) would re-enter
     * that handler unbounded, since the bridge's focus holder is what breaks the cycle and the plain
     * FlutterBridge does not track one.
     */
    public static boolean takeFocus(DartControl control) {
        boolean result = control.getBridge().setFocus(control);
        if (control.isDisposed())
            return result;
        sendActivateToAncestors(control);
        return result;
    }

    /**
     * Announce that the user activated [control], by sending SWT.Activate up its parent chain.
     *
     * There is no OS focus machinery behind this backend, so nothing generates the activation an
     * embedding workbench listens for. Eclipse's part renderer, for one, binds its activation
     * listener to the *client Composite* of a part rather than to the control the user actually
     * clicked, and only reacts when that widget carries the part's own model data — so the event
     * has to travel up from the focused control for the enclosing part to become the active one.
     * Without it a view keeps drawing and reporting selection while the workbench still considers
     * a different part active, and every action bound to it quietly does nothing.
     *
     * Ancestors that are not part containers simply have no listener and ignore the event.
     */
    public static void sendActivateToAncestors(DartControl control) {
        if (control == null || control.getApi() == null || control.getApi().isDisposed())
            return;
        for (Widget widget = control.getApi(); widget != null; ) {
            if (widget.isDisposed())
                return;
            // A Dart control can sit inside a natively-backed ancestor; that one already gets its
            // activation from the OS, so walk past it rather than assuming the whole chain is ours.
            if (widget.getImpl() instanceof DartWidget impl)
                impl.sendEvent(SWT.Activate);
            if (widget instanceof Shell)
                return;
            widget = (widget instanceof Control c) ? c.getParent() : null;
        }
    }
}

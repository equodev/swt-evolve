package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ControlHelper {

    public static int inPaintDepth;

    // Area each control still owes a Paint for, unioned as redraw() calls arrive and consumed by
    // the paint that follows. Absent, or a null value, means the whole client area.
    private static final Map<DartControl, Rectangle> pendingDamage = new WeakHashMap<>();

    // The control each Shell last announced as activated, so the next announcement can end it.
    private static final Map<Shell, Control> lastActivated =
            Collections.synchronizedMap(new WeakHashMap<>());

    // Area the Paint being dispatched is scoped to, null when it covers the whole client area. A GC
    // opened during that dispatch carries it to Flutter, which composites within it.
    private static Rectangle paintDamage;

    /** The damage the in-flight Paint is scoped to, or null outside one / for a full-area Paint. */
    public static Rectangle currentPaintDamage() {
        return paintDamage;
    }

    public static void sendFlutterKeyDown(DartWidget widget, Event event) {
        widget.sendEvent(SWT.KeyDown, event);
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

    /**
     * The character GTK and Win32 report for a Ctrl chord: a letter or {@code @}..{@code _} becomes
     * its ASCII control character (Ctrl+C is 3). Cocoa reports the key's own character. The browser
     * reports the latter everywhere, so this follows the platform the application runs on.
     */
    public static void applyControlCharacter(Event key) {
        if ((key.stateMask & SWT.CTRL) == 0 || key.character > 0x7F || "cocoa".equals(SWT.getPlatform()))
            return;
        int c = key.character;
        if ('a' <= c && c <= 'z')
            c -= 'a' - 'A';
        if ('@' <= c && c <= '_')
            key.character = (char) (c - '@');
    }

    /** What {@link #routeKeyDown} did with a key. */
    public static final class RoutedKey {
        /** Whether the KeyDown was delivered, and its doit after the listeners ran. */
        public final boolean delivered, keyDoit;
        /** The Traverse's doit after the listeners ran, or false when the key traverses nothing. */
        public final boolean traverseDoit;

        RoutedKey(boolean delivered, boolean keyDoit, boolean traverseDoit) {
            this.delivered = delivered;
            this.keyDoit = keyDoit;
            this.traverseDoit = traverseDoit;
        }
    }

    /**
     * Offers a traversal key as {@link SWT#Traverse} before the KeyDown, as every platform's
     * {@code translateTraversal} does. The client performs a Tab traversal itself.
     */
    public static RoutedKey routeKeyDown(DartControl control, Event key) {
        key.doit = true;
        int detail = traverseDetail(key.keyCode, key.stateMask);
        boolean mnemonic = detail == SWT.TRAVERSE_NONE && isMnemonicTrigger(key);
        if (mnemonic)
            detail = SWT.TRAVERSE_MNEMONIC;
        boolean traversed = false;
        boolean traverseDoit = false;
        if (key.keyCode == SWT.ESC) {
            traversed = escapeTraversal(control, key);
        } else if (detail != SWT.TRAVERSE_NONE) {
            Event e = new Event();
            e.character = key.character;
            e.keyCode = key.keyCode;
            e.stateMask = key.stateMask;
            e.detail = detail;
            e.doit = traversesByDefault(control, detail);
            control.sendEvent(SWT.Traverse, e);
            if (control.isDisposed())
                return new RoutedKey(false, false, false);
            traverseDoit = e.doit;
            if (e.doit) {
                switch (e.detail) {
                    // A listener that consumed the key clears the detail; Control.traverse counts it as done.
                    case SWT.TRAVERSE_NONE:
                    case SWT.TRAVERSE_TAB_NEXT:
                    case SWT.TRAVERSE_TAB_PREVIOUS:
                        traversed = true;
                        break;
                    case SWT.TRAVERSE_MNEMONIC:
                        traversed = MnemonicHelper.dispatch(control.getApi(), (char) key.keyCode);
                        break;
                    default:
                        break;
                }
            }
        }
        if (traversed || control.isDisposed())
            return new RoutedKey(false, false, traverseDoit);
        control.sendEvent(SWT.KeyDown, key);
        return new RoutedKey(true, key.doit, traverseDoit);
    }

    /**
     * Mirrors {@code SwtControl/SwtComposite.traversalCode}: arrows stay in the control, and a
     * key-listening Canvas keeps every key unless a Traverse listener lets it go.
     */
    private static boolean traversesByDefault(DartControl control, int detail) {
        if (detail == SWT.TRAVERSE_ARROW_NEXT || detail == SWT.TRAVERSE_ARROW_PREVIOUS)
            return false;
        if (control.getApi() instanceof Canvas) {
            if ((control.getApi().getStyle() & SWT.NO_FOCUS) != 0)
                return false;
            if (control.hooks(SWT.KeyDown) || control.hooks(SWT.KeyUp))
                return false;
        }
        return true;
    }

    public static boolean isModifierKey(int keyCode) {
        return keyCode == SWT.SHIFT || keyCode == SWT.CTRL || keyCode == SWT.ALT || keyCode == SWT.COMMAND;
    }

    /** Escape runs the whole traversal, so a dialog closes on it. */
    private static boolean escapeTraversal(DartControl origin, Event keyEvent) {
        Control start = resolveTraverseStart(origin);
        if (start == null || start.isDisposed() || !(start.getImpl() instanceof DartControl))
            return false;
        Event event = new Event();
        event.character = keyEvent.character;
        event.keyCode = keyEvent.keyCode;
        event.keyLocation = keyEvent.keyLocation;
        event.stateMask = keyEvent.stateMask;
        event.doit = true;
        return ((DartControl) start.getImpl()).traverse(SWT.TRAVERSE_ESCAPE, event);
    }

    /**
     * {@code count} is lines, positive up; a pixel scroll arrives in {@code detail}, positive down,
     * and moves the bar by those pixels. Listeners always see {@code detail} as SCROLL_LINE.
     */
    public static void handleMouseWheel(DartControl control, Event e) {
        int pixels = e.detail;
        e.detail = SWT.SCROLL_LINE;
        e.doit = true;
        control.sendEvent(SWT.MouseWheel, e);
        if (!e.doit || control.isDisposed() || !(control.getApi() instanceof Scrollable))
            return;
        ScrollBar bar = ((Scrollable) control.getApi()).getVerticalBar();
        if (bar == null || !bar.getEnabled())
            return;
        int max = bar.getMaximum() - bar.getThumb();
        int step = pixels != 0 ? pixels : -e.count * bar.getIncrement();
        int selection = Math.max(bar.getMinimum(), Math.min(max, bar.getSelection() + step));
        if (selection == bar.getSelection())
            return;
        bar.setSelection(selection);
        Event scrolled = new Event();
        scrolled.detail = step < 0 ? SWT.ARROW_UP : SWT.ARROW_DOWN;
        bar.notifyListeners(SWT.Selection, scrolled);
    }

    /** Answers the client's held context menu after the MenuDetect listeners have run. */
    public static void sendMenuDetectVerdict(DartWidget widget, Event e) {
        dev.equo.swt.FlutterBridge.send(widget, "menu/verdict", dev.equo.swt.Java8.map("doit", e.doit));
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
            // Escape runs the whole traversal instead; see escapeTraversal.
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
            // Plain cast, not a pattern: this source set is also compiled at -source 8 and -source 11
            // for the older SWT versions, where a binding instanceof is a compile error.
            if (current instanceof Shell) {
                Shell shell = (Shell) current;
                DartControl impl = (DartControl) current.getImpl();
                FlutterBridge bridge = impl.getBridge();
                // The bridge knows which shell it shows as its window (no Flutter title bar) and where
                // that window's content sits on screen; the geometry guess is only for a bridge that
                // does not host shells.
                Boolean hosted = bridge == null ? null : bridge.hostsAsMainShell(shell);
                boolean main = hosted != null ? hosted : isMainShell(shell);
                int st = current.getStyle();
                boolean showsTitleBar = (st & SWT.NO_TRIM) == 0
                    && ((st & SWT.TITLE) != 0 || (st & SWT.CLOSE) != 0);
                if (showsTitleBar && !main) {
                    offset[1] += (st & SWT.TOOL) != 0 ? 22 : 30;
                }
                if (bridge != null) {
                    Point origin = bridge.getWindowOrigin(impl);
                    offset[0] += origin.x;
                    offset[1] += origin.y;
                }
                return null;
            }
            Composite parent = current.getParent();
            // Only a Group, and only the vertical trim. Sizes.getClientArea(DartGroup) reports the
            // client origin as (GROUP_BORDER, 0) whenever the Group has a title, so the layout
            // already gave the child the horizontal inset in its bounds, while the title height the
            // Group draws above its content is reported by nobody -- without it every popup a JFace
            // viewer opens inside a Group came up a title-height too high.
            //
            // Every other container lays its children out in its own coordinates, so their bounds
            // already carry whatever the container draws above them: a CTabFolder's page sits at
            // y = the tab strip's height, and computeTrim reports that same height again. Applying
            // this there adds the strip a second time, once per folder in the chain, and everything
            // mapped through it lands that much too low.
            if (parent instanceof Group) {
                offset[1] -= parent.computeTrim(0, 0, 0, 0).y;
            }
            current = parent;
        }
        return current;
    }

    private static boolean isMainShell(Shell shell) {
        // The bridge knows which shell fills its window; ask it rather than inferring. The guesses
        // below cannot tell a main shell from a floating one once a window may sit anywhere on
        // screen, and getting it wrong adds a title-bar inset to every coordinate inside the shell.
        if (shell.getImpl() instanceof DartControl) {
            dev.equo.swt.FlutterBridge bridge = ((DartControl) shell.getImpl()).getBridge();
            if (bridge instanceof dev.equo.swt.WindowBridge) {
                return ((dev.equo.swt.WindowBridge) bridge).rendersAsMainWindow(shell);
            }
        }
        Rectangle b = shell.getBounds();
        if (b.x != 0 || b.y != 0)
            return false;
        int modal = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL | SWT.PRIMARY_MODAL;
        if ((shell.getStyle() & modal) != 0)
            return false;
        if (b.width == 1024 && b.height == 768)
            return true;
        Rectangle view = screenToMeasureAgainst(shell);
        if (view == null)
            return false;
        return b.width >= Math.round(view.width * 0.8f)
            && b.height >= Math.round(view.height * 0.8f);
    }

    /**
     * The screen area a shell's size is compared against, or null when there is none to compare with.
     * <p>
     * Shell.getMonitor() is the right question but not always an answered one: a shell that has not
     * been placed on a monitor yet has none to name, while the Display already knows of one. Reading
     * it unguarded made toDisplay throw from inside the walk, which surfaced as a
     * NullPointerException on an unrelated caller rather than as a missing monitor.
     */
    private static Rectangle screenToMeasureAgainst(Shell shell) {
        Monitor monitor = shell.getMonitor();
        if (monitor == null) {
            Display display = shell.getDisplay();
            Monitor[] monitors = display == null ? null : display.getMonitors();
            if (monitors != null && monitors.length > 0)
                monitor = monitors[0];
        }
        return monitor == null ? null : monitor.getClientArea();
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
        // The client asks for this paint from every Control it mounts, having no way to know which
        // ones paint themselves. Answering one that listens for nothing still builds a GC and
        // disposes it -- a whole VGC push per control, for no drawing.
        if (!c.hooks(SWT.Paint)) return;
        // The client only asks when its view is stale, so the bridge's record of it is out of date.
        dev.equo.swt.FlutterBridge.forgetWhatIsShown(c.getApi());
        if (c.drawCount > 0 || paintQueued.contains(c)) return;
        damageAll(c);
        firePaint(c);
    }

    /**
     * Declares that what the control paints is out of date. Reachable from the custom-widget
     * package, unlike Widget.hooks(int).
     */
    public static void markDamaged(DartControl c) {
        if (c.hooks(SWT.Paint)) {
            paint(c);
        }
        markOwnerDrawnCellsDamaged(c);
    }

    /** As {@link #markDamaged(DartControl)}, for an invalidation that named the area it dirtied. */
    public static void markDamaged(DartControl c, int x, int y, int width, int height) {
        if (c.hooks(SWT.Paint)) {
            paint(c, x, y, width, height);
        }
        markOwnerDrawnCellsDamaged(c);
    }

    /**
     * A control that draws its own cells derives their content by running the application's
     * {@code SWT.PaintItem} listener, so a repaint is the only announcement that the derived content
     * changed — nothing else names those properties, and an update carries only what it names.
     *
     * <p>Area-scoped invalidations name their rows too: the cost is one dirty flag per existing row,
     * and narrowing it to the rows the rectangle covers would mean mapping pixels back to rows here.
     */
    private static void markOwnerDrawnCellsDamaged(DartControl c) {
        markListenerProvidedStyles(c);
        if (!c.hooks(SWT.PaintItem)) {
            return;
        }
        if (c instanceof DartTable) {
            TableHelper.nameOwnerDrawnCells((DartTable) c);
        } else if (c instanceof DartTree) {
            TreeHelper.nameOwnerDrawnCells((DartTree) c);
        }
    }

    /** Listener-provided StyledText styles are derived while painting, so only a repaint announces them. */
    private static void markListenerProvidedStyles(DartControl c) {
        if (!(c.getApi() instanceof org.eclipse.swt.custom.StyledText))
            return;
        if (!c.hooks(org.eclipse.swt.custom.ST.LineGetStyle)
                && !c.hooks(org.eclipse.swt.custom.ST.LineGetBackground)
                // A band of styles around the viewport changes on scroll even when the styles do not.
                && !stylesAreBanded(c))
            return;
        c.getValue().markDirty(org.eclipse.swt.custom.VStyledText.RENDERER);
        c.getValue().markDirty(org.eclipse.swt.custom.VStyledText.STYLES);
        // The names are names in that palette, so they are never left behind by it.
        c.getValue().markDirty(org.eclipse.swt.custom.VStyledText.STYLE_INDEX);
    }

    private static boolean stylesAreBanded(DartControl c) {
        return org.eclipse.swt.custom.StyledTextHelper.stylesAreBanded(
                (org.eclipse.swt.custom.StyledText) c.getApi());
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
            // A FigureCanvas takes this Paint like any other control. draw2d answers it by painting
            // its whole figure tree straight into the event's GC (DeferredUpdateManager#paint), and
            // that is the only paint path guaranteed to run once the Flutter client is connected:
            // the LightweightSystem's own update loop can complete entirely before that, and its
            // output goes through an off-screen Image blit whose pixels the client is not yet there
            // to render. Skipping the Paint here left such a canvas showing whatever that early
            // blit deposited -- for a fresh buffer, an opaque black rectangle.
            sendPaint(c, damage, scoped);
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
        // Compared against the state bit, which is what getEnabled() - and so the wire - reports.
        // The `enabled` field is a different thing and the two can disagree, in which case a gate
        // on the field says "unchanged" while the payload changes. That was harmless while every
        // update carried the whole widget and simply re-sent the right value; once an update
        // carries only what changed, it means the property stops being sent at all.
        boolean wasEnabled = (c.getApi().state & DartWidget.DISABLED) == 0;
        if (wasEnabled != enabled) {
            c.getValue().markDirty(VControl.ENABLED);
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
        c.enabled = enabled;
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
        sendActivateToAncestors(control, SWT.None);
    }

    /**
     * [detail] carries what caused the activation, the way Shell.setActiveControl(control, type)
     * stamps it natively. A listener is allowed to react only to a user click — Eclipse's stack
     * renderer activates a tab's part solely for SWT.MouseDown — so a click reported by the render
     * side has to say so, or it is indistinguishable from a programmatic focus change and ignored.
     */
    public static void sendActivateToAncestors(DartControl control, int detail) {
        if (control == null || control.getApi() == null || control.getApi().isDisposed())
            return;
        Control now = control.getApi();
        Shell shell = now.getShell();
        if (shell == null || shell.isDisposed())
            return;
        Control previous = lastActivated.put(shell, now);
        // An open shell is the active control of itself until something inside it takes over -- what
        // Shell.setActiveControl(this) establishes natively when the shell activates. Treating an
        // untracked shell as inactive instead makes the first focus inside it announce the shell too.
        if (previous == null || previous.isDisposed())
            previous = shell;
        // Activation is a change, not a restatement: re-focusing the control that already holds focus
        // announces nothing, which is also what keeps two focus callbacks for one focus change from
        // announcing it twice.
        if (previous == now)
            return;
        sendDeactivateToFormerChain(previous, now, detail);
        for (Widget widget = now; widget != null; ) {
            if (widget.isDisposed())
                return;
            // Stop at the first ancestor the two chains share: an ancestor that was already active is
            // not activated again. Walking to the Shell regardless announces it on every focus change
            // inside it, which the native never does.
            if (isAncestorOfOrSame(widget, previous))
                return;
            // A Dart control can sit inside a natively-backed ancestor; that one already gets its
            // activation from the OS, so walk past it rather than assuming the whole chain is ours.
            if (widget.getImpl() instanceof DartWidget) { DartWidget impl = (DartWidget) widget.getImpl();
                Event event = new Event();
                event.detail = detail;
                impl.sendEvent(SWT.Activate, event);
            }
            if (widget instanceof Shell)
                return;
            widget = (widget instanceof Control) ? ((Control) widget).getParent() : null;
        }
    }

    /**
     * Send SWT.Deactivate to the chain that was active until now, up to the first ancestor the new
     * control shares with it. Natively that is the Shell's own enter/leave bookkeeping, driven by
     * an OS focus event this backend has no equivalent of, so the walk above only ever announces
     * the arrival -- nothing announces the departure, and every chain the user has focused stays
     * marked active. A CTabFolder is where that shows: it keeps painting itself as the focused
     * stack, so with several stacks open none of them is distinguishable from the active one.
     *
     * The previous control is tracked by the caller rather than read from Shell.lastActive because
     * the Shell may be a native one (the embedded backend has no Dart Shell at all), and because the
     * walk above, not setActiveControl, owns the Activate half.
     */
    private static void sendDeactivateToFormerChain(Control previous, Control now, int detail) {
        for (Widget widget = previous; widget != null; ) {
            if (widget.isDisposed() || isAncestorOfOrSame(widget, now))
                return;
            if (widget.getImpl() instanceof DartWidget) {
                DartWidget impl = (DartWidget) widget.getImpl();
                Event event = new Event();
                event.detail = detail;
                impl.sendEvent(SWT.Deactivate, event);
            }
            if (widget instanceof Shell)
                return;
            widget = (widget instanceof Control) ? ((Control) widget).getParent() : null;
        }
    }

    private static boolean isAncestorOfOrSame(Widget candidate, Control control) {
        for (Control c = control; c != null; c = c.getParent())
            if (c == candidate)
                return true;
        return false;
    }

}

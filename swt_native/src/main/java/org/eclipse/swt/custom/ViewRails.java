package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.WidgetWire;
import dev.equo.swt.WireTable;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.size.ViewRailsSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartSideBar;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The view stacks of the e4 main window, shown as icon rails on its left and right trim instead of
 * as tab strips: every view of the current perspective has an icon there, and a view stack draws
 * only its selected view under a header.
 *
 * <p>Sent as the main shell's {@code ViewRails} holder record. It carries each view stack's
 * CTabFolder by reference, because a minimized stack's folder sits in e4's limbo shell and nothing
 * else would carry it to the client; where each stack's icons go; the trim composites that draw
 * the rails; and the e4 TrimStacks the rails stand in for, which the client leaves undrawn.
 *
 * <p>Read from the e4 model by reflection: e4 is not a dependency. With the {@code view_rails} flag
 * off there is no record, and every part stack keeps its tabs.
 */
public final class ViewRails implements FlutterBridge.DirtyState {

    /** Where a stack's icons go: which rail, and which end of it. */
    public static final int LEFT_TOP = 0, LEFT_BOTTOM = 1, RIGHT_TOP = 2, RIGHT_BOTTOM = 3;

    /** Set on a place when the stack is minimized: its icons are listed, its folder is not shown. */
    public static final int MINIMIZED = 4;

    /** Whether {@code place}, minimized or not, is on the right rail. */
    static boolean isRight(int place) {
        return (place & ~MINIMIZED) >= RIGHT_TOP;
    }

    /** Whether {@code place}, minimized or not, is a rail's bottom end. */
    static boolean isBottom(int place) {
        return (place & 1) != 0;
    }

    static int place(boolean right, boolean bottom) {
        return (right ? RIGHT_TOP : LEFT_TOP) + (bottom ? 1 : 0);
    }

    /**
     * What a {@code ViewRails/show} event asks for, in its detail: a press on a view's button, a
     * view's menu (on its stack's folder) or a rail's (on the rail).
     */
    static final int PRESS = 1, VIEW_MENU = 3, RAIL_MENU = 4, MOVE = 5;

    /** The rails' sizes, each with its width in {@link ViewRailsSizes}. */
    public static final int SMALL = 0, MEDIUM = 1, LARGE = 2;

    private static final int[] WIRE_KEYS = {
            WireTable.VIEW_RAILS_STACKS, WireTable.VIEW_RAILS_PLACES, WireTable.VIEW_RAILS_RAILS,
            WireTable.VIEW_RAILS_RAIL_SIDES, WireTable.VIEW_RAILS_TRIMS, WireTable.VIEW_RAILS_SIZE,
    };

    /** The display data key a session's rails are kept under. */
    private static final String DATA_KEY = ViewRails.class.getName();

    private final Display display;
    final Model model = new Model();
    private final ViewRailsMenus menus = new ViewRailsMenus(this);

    /** The rails' size for this session: {@code ConfigFlags.view_rails_size} to start with, then the rail menu's. */
    private int size = initialSize();
    private final Serializer.HolderSent sent = new Serializer.HolderSent();

    /** The folders whose {@code ViewRails/show} channel is listened to. */
    private final Map<DartCTabFolder, Boolean> listening = new WeakHashMap<>();

    /** The listed stacks whose view toolbar was kept from wrapping, to get it back when unlisted. */
    private final Map<DartCTabFolder, Boolean> unwrapped = new WeakHashMap<>();

    /** The record last written, read by the side bars when they size themselves. */
    volatile Record current = Record.empty();

    private ViewRails(Display display) {
        this.display = display;
    }

    /**
     * Asks the next flush to check {@code widget}, a view stack's folder or a side bar: the rails are
     * worked out again only when it changed in a way the record reads, and most of its changes do not.
     * Nothing is read here: the widget may still be under construction.
     */
    public static void touched(DartWidget widget) {
        if (!Config.getConfigFlags().view_rails) return;
        Widget api = widget.getApi();
        Display display = api == null ? null : api.getDisplay();
        if (display == null || display.isDisposed()) return;
        if (display.getThread() != Thread.currentThread()) {
            display.asyncExec(() -> {
                if (!display.isDisposed()) of(display).toCheck(api);
            });
            return;
        }
        of(display).toCheck(api);
    }

    /** What the record reads of each folder and side bar, as last seen. */
    private final Map<Widget, List<Object>> seen = new WeakHashMap<>();

    /** The folders and side bars touched since the last flush, checked by it. */
    private final Map<Widget, Boolean> touched = new WeakHashMap<>();

    /** Whether the next flush that can work the rails out must, whatever the touched widgets show. */
    private boolean stale = true;

    private void toCheck(Widget widget) {
        touched.put(widget, Boolean.TRUE);
        FlutterBridge.dirty(this);
    }

    /** Asks for the rails to be worked out again at the next flush, whatever changed. */
    private void refresh() {
        stale = true;
        FlutterBridge.dirty(this);
    }

    /** Compares the touched widgets with what they were; marks the rails stale when one changed. */
    private void checkTouched() {
        for (Widget widget : new ArrayList<>(touched.keySet())) {
            if (widget.isDisposed()) {
                stale |= seen.remove(widget) != null;
                continue;
            }
            List<Object> now = shape(widget);
            List<Object> before = seen.put(widget, now);
            if (before == null) widget.addListener(SWT.Dispose, e -> {
                seen.remove(widget);
                refresh();
            });
            stale |= !now.equals(before);
        }
        touched.clear();
    }

    /**
     * What the record reads of {@code widget}: a folder is listed while it has tabs, minimized when
     * hidden or moved to another shell, and placed by where its model part sits, which its parent and
     * bounds follow; a side bar draws a rail when shown, and holds the minimized stacks' trim bars.
     */
    static List<Object> shape(Widget widget) {
        if (widget instanceof CTabFolder) {
            CTabFolder folder = (CTabFolder) widget;
            return java.util.Arrays.asList(folder.getVisible(), folder.getShell(), folder.getParent(),
                    folder.getBounds(), folder.getItemCount() > 0);
        }
        if (widget instanceof Composite && ((Composite) widget).getImpl() instanceof DartSideBar) {
            Composite bar = (Composite) widget;
            return java.util.Arrays.asList(bar.getVisible(), bar.getChildren().length);
        }
        return Collections.emptyList();
    }

    /**
     * The width the rail on {@code sideBar} needs: its size's width with icons to draw, 0 for a rail
     * with none, and -1 when the side bar draws no rail and sizes itself as a trim bar.
     */
    public static int railWidth(DartSideBar sideBar) {
        Widget api = sideBar.getApi();
        if (api == null || !Config.getConfigFlags().view_rails) return -1;
        Object held = api.getDisplay().getData(DATA_KEY);
        if (!(held instanceof ViewRails)) return -1;
        ViewRails rails = (ViewRails) held;
        if (!rails.current.rails.contains(api)) return -1;
        return rails.current.hostsAny(api) ? ViewRailsSizes.width(rails.current.size) : 0;
    }

    /**
     * The header controls of a listed view stack whose full width does not fit the folder: each at its
     * preferred width, packed from the folder's outer edge, so the row runs past the inner one (to the
     * left for a stack on the right rail, to the right for one on the left). Run once the folder has
     * placed them in {@code rects}, which it keeps for its tabs' room and which stay as they are;
     * controls wrapped onto another row are left alone.
     */
    static void layoutHeaderControls(DartCTabFolder folder, Rectangle[] rects, boolean[] overflow) {
        Rectangle[] wide = headerControlBounds(folder, rects, overflow);
        if (wide == null) return;
        // A control resized while this is clear lays the folder out again, and that again widens it.
        boolean ignoring = folder.ignoreResize;
        folder.ignoreResize = true;
        try {
            for (int i = 0; i < wide.length; i++) {
                Control control = folder.controls[i];
                if (wide[i] == null || control.isDisposed()) continue;
                Rectangle now = control.getBounds();
                control.setBounds(wide[i].x, now.y, wide[i].width, now.height);
            }
        } finally {
            folder.ignoreResize = ignoring;
        }
    }

    /** The widened bounds of each header control, null where it stays; null when they all fit. */
    static Rectangle[] headerControlBounds(DartCTabFolder folder, Rectangle[] rects, boolean[] overflow) {
        if (!Config.getConfigFlags().view_rails || folder.controls == null) return null;
        CTabFolder api = folder.getApi();
        Object held = api.getDisplay().getData(DATA_KEY);
        if (!(held instanceof ViewRails)) return null;
        int place = ((ViewRails) held).current.placeOf(api);
        if (place < 0) return null;
        Integer[] row = headerRow(folder.controls, rects, overflow);
        int[] widths = new int[row.length];
        int total = 0;
        for (int i = 0; i < row.length; i++) {
            Control control = folder.controls[row[i]];
            widths[i] = Math.max(rects[row[i]].width, control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
            total += widths[i];
        }
        int width = api.getSize().x;
        if (row.length == 0 || total <= width) return null;
        Rectangle[] wide = new Rectangle[rects.length];
        int x = isRight(place) ? width - total : 0;
        for (int i = 0; i < row.length; i++) {
            Rectangle r = rects[row[i]];
            wide[row[i]] = new Rectangle(x, r.y, widths[i], r.height);
            x += widths[i];
        }
        return wide;
    }

    /** The indices of the visible controls on the header's row, left to right. */
    private static Integer[] headerRow(Control[] controls, Rectangle[] rects, boolean[] overflow) {
        List<Integer> row = new ArrayList<>();
        for (int i = 0; i < controls.length && i < rects.length; i++) {
            if (controls[i] == null || controls[i].isDisposed() || !controls[i].getVisible()) continue;
            if ((overflow != null && i < overflow.length && overflow[i]) || rects[i].width <= 0) continue;
            row.add(i);
        }
        row.sort((a, b) -> Integer.compare(rects[a].x, rects[b].x));
        return row.toArray(new Integer[0]);
    }

    private static int initialSize() {
        String size = Config.getConfigFlags().view_rails_size;
        if ("small".equalsIgnoreCase(size)) return SMALL;
        if ("large".equalsIgnoreCase(size)) return LARGE;
        return MEDIUM;
    }

    int size() {
        return size;
    }

    /** Changes the rails' size for the session, from the rail menu. */
    void resize(int size) {
        if (size < SMALL || size > LARGE || size == this.size) return;
        this.size = size;
        refresh();
    }

    /** The display's rails, kept as its data: they are the session's, as the display is. On its thread. */
    private static ViewRails of(Display display) {
        Object held = display.getData(DATA_KEY);
        if (held instanceof ViewRails) return (ViewRails) held;
        ViewRails rails = new ViewRails(display);
        display.setData(DATA_KEY, rails);
        return rails;
    }

    @Override
    public boolean isStale() {
        return display.isDisposed();
    }

    @Override
    public void flush() {
        if (display.getThread() != Thread.currentThread()) {
            display.asyncExec(() -> FlutterBridge.dirty(this));
            return;
        }
        checkTouched();
        if (!stale) return;
        Shell shell = mainShell();
        if (shell == null || !(shell.getImpl() instanceof DartWidget)) return;
        stale = false;
        Record record = compute(shell);
        Record before = current;
        current = record;
        for (CTabFolder folder : record.stacks) listen(folder);
        for (Control rail : record.rails) listenRail(rail);
        keepToolbarsInHeaders(record.stacks);
        CommService comm = FlutterBridge.commFor(shell.getImpl());
        FlutterBridge.writeTape(comm, tape -> tape.describeHolder("ViewRails", FlutterBridge.id(shell.getImpl()),
                WIRE_KEYS, record::writeWire, sent));
        // A rail that starts or stops hosting icons changes width.
        if (before.railWidthsDiffer(record)) shell.layout(null, SWT.ALL | SWT.CHANGED | SWT.DEFER);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ViewRails && ((ViewRails) o).display == display;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(display);
    }

    private Shell mainShell() {
        for (Shell shell : display.getShells()) {
            if (!shell.isDisposed() && Config.isMainShellLayout(shell.getLayout())) return shell;
        }
        return null;
    }

    private Record compute(Shell shell) {
        Object window = shell.getData(MODEL_ELEMENT);
        if (window == null) return Record.empty();
        Rectangle[] area = new Rectangle[1];
        Map<Object, Rectangle> stackRects = viewStacks(model, window, area);
        if (stackRects.isEmpty()) return Record.empty();
        Control left = rail(shell, SWT.LEFT), right = rail(shell, SWT.RIGHT);
        if (left == null || right == null) ensureRails(shell);
        if (left == null && right == null) return Record.empty();

        Rectangle reference = area[0];
        Map<String, Control> trimsById = model.trimStacks(shell);
        List<Control> trims = new ArrayList<>();
        List<CTabFolder> stacks = new ArrayList<>();
        List<Long> places = new ArrayList<>();
        for (Map.Entry<Object, Rectangle> entry : stackRects.entrySet()) {
            Object element = entry.getKey();
            Object widget = model.call(element, "getWidget");
            if (!(widget instanceof CTabFolder)) continue;
            CTabFolder folder = (CTabFolder) widget;
            if (folder.isDisposed() || !(folder.getImpl() instanceof DartCTabFolder) || folder.getItemCount() == 0) continue;
            boolean minimized = !Boolean.TRUE.equals(model.call(element, "isVisible")) || folder.getShell() != shell;
            int place = hostedPlace(placeOf(entry.getValue(), reference), left != null, right != null);
            stacks.add(folder);
            places.add((long) (place | (minimized ? MINIMIZED : 0)));
            Control trim = trimsById.get(String.valueOf(model.call(element, "getElementId")));
            if (trim != null) trims.add(trim);
        }
        List<Control> rails = new ArrayList<>();
        List<Long> sides = new ArrayList<>();
        if (left != null) { rails.add(left); sides.add((long) SWT.LEFT); }
        if (right != null) { rails.add(right); sides.add((long) SWT.RIGHT); }
        return new Record(stacks, places, rails, sides, trims, size);
    }

    /**
     * The view stacks of {@code window}, each with its rectangle in the model's layout, and in
     * {@code area} the editor area's. None when the window has no editor area: the rails place views
     * around it, and an application without one keeps its own layout.
     */
    static Map<Object, Rectangle> viewStacks(Model model, Object window, Rectangle[] area) {
        Map<Object, Rectangle> stacks = new java.util.LinkedHashMap<>();
        model.collect(window, new Rectangle(0, 0, MODEL_SPAN, MODEL_SPAN), stacks, area);
        if (area[0] == null) stacks.clear();
        return stacks;
    }

    /** The side of the square the model's layout is worked out in. */
    static final int MODEL_SPAN = 10000;

    /** A side's trim composite when it can draw a rail. */
    private static Control rail(Shell shell, int side) {
        Control trim = Config.trimAt(shell, side);
        return trim != null && !trim.isDisposed() && trim.getImpl() instanceof DartSideBar ? trim : null;
    }

    /** A place on a rail that exists: the other side's when this side has no trim. */
    static int hostedPlace(int place, boolean left, boolean right) {
        return place(isRight(place) ? right : !left, isBottom(place));
    }

    /**
     * The place a stack goes, by where it sits against {@code reference}: right when it starts past
     * the reference's middle, bottom when its top is below the reference's middle. A stack under the
     * editor area that spans it goes to the left rail's bottom end.
     */
    static int placeOf(Rectangle stack, Rectangle reference) {
        int midX = reference.x + reference.width / 2, midY = reference.y + reference.height / 2;
        boolean right = stack.x >= midX;
        boolean bottom = stack.y >= midY;
        return place(right, bottom);
    }

    /**
     * Gives the window a left and a right trim composite when it lacks one, through the layout's own
     * {@code getTrimComposite}, as e4 asks for one when it renders a trim bar. e4 disposes a side's
     * composite once its trim bar has nothing left in it, which would take the rail with it. Later,
     * and left first: a trim composite under construction is told apart by the sides already built.
     */
    private void ensureRails(Shell shell) {
        display.asyncExec(() -> {
            if (shell.isDisposed()) return;
            Object layout = shell.getLayout();
            if (!Config.isMainShellLayout((org.eclipse.swt.widgets.Layout) layout)) return;
            boolean created = false;
            for (int side : new int[] {SWT.LEFT, SWT.RIGHT}) {
                if (Config.trimAt(shell, side) != null) continue;
                try {
                    layout.getClass().getMethod("getTrimComposite", Composite.class, int.class).invoke(layout, shell, side);
                    created = true;
                } catch (ReflectiveOperationException | RuntimeException e) {
                    model.report(layout, "getTrimComposite", e);
                    return;
                }
            }
            if (created) {
                shell.layout(true);
                refresh();
            }
        });
    }

    /**
     * A listed stack's view toolbar stays in its header row, drawn on hover over the title, rather
     * than wrapping onto a row of its own: e4 lets it wrap ({@code SWT.RIGHT | SWT.WRAP}) and the
     * header keeps every tab in the strip Java lays out, so it would always wrap. A stack that
     * leaves the rails gets its wrap back.
     */
    private void keepToolbarsInHeaders(List<CTabFolder> stacks) {
        java.util.Set<DartCTabFolder> listed = Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (CTabFolder folder : stacks) {
            DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
            listed.add(impl);
            if (impl.topRight != null && !impl.topRight.isDisposed() && (impl.topRightAlignment & SWT.WRAP) != 0) {
                folder.setTopRight(impl.topRight, impl.topRightAlignment & ~SWT.WRAP);
                unwrapped.put(impl, Boolean.TRUE);
            }
        }
        for (DartCTabFolder impl : new ArrayList<>(unwrapped.keySet())) {
            if (listed.contains(impl)) continue;
            unwrapped.remove(impl);
            if (!impl.isDisposed() && impl.topRight != null && !impl.topRight.isDisposed()
                    && impl.topRightAlignment == SWT.RIGHT) {
                impl.getApi().setTopRight(impl.topRight, SWT.RIGHT | SWT.WRAP);
            }
        }
    }

    private void listen(CTabFolder folder) {
        DartCTabFolder impl = (DartCTabFolder) folder.getImpl();
        if (listening.put(impl, Boolean.TRUE) != null) return;
        FlutterBridge.on(impl, WireTable.CHANNEL_VIEW_RAILS_SHOW, e -> display.asyncExec(() -> {
            if (e.detail == VIEW_MENU) menus.viewMenu(impl, e.index, e.x, e.y);
            else if (e.detail == PRESS) press(impl, e.index);
            else if (e.detail == MOVE) menus.drop(impl, e.index, e.start, e.x, e.end);
        }));
    }

    /** A rail's own menu, asked for on the rail: {@code index} 0 for its top end, 1 for its bottom. */
    private void listenRail(Control rail) {
        if (!(rail.getImpl() instanceof DartWidget)) return;
        DartWidget impl = (DartWidget) rail.getImpl();
        if (listeningRails.put(impl, Boolean.TRUE) != null) return;
        FlutterBridge.on(impl, WireTable.CHANNEL_VIEW_RAILS_SHOW, e -> display.asyncExec(() -> {
            if (e.detail == RAIL_MENU && !rail.isDisposed()) menus.railMenu(rail, e.index, e.x, e.y);
        }));
    }

    /** The rails whose {@code ViewRails/show} channel is listened to, for their own menu. */
    private final Map<DartWidget, Boolean> listeningRails = new WeakHashMap<>();

    /** The main window's shell, when it has one. */
    Shell shell() {
        return mainShell();
    }

    /**
     * A view's button pressed: on the view being worked in, it minimizes the view's stack, as its
     * minimize button does; on any other, it shows the view. Decided here, from the workbench's
     * state, rather than from what the client last drew.
     */
    void press(DartCTabFolder folder, int index) {
        if (folder.isDisposed()) return;
        if (!minimized(folder) && folder.getApi().getSelectionIndex() == index && folder._highlight()) {
            CTabFolderHelper.handleMinimize(folder, new Event());
            return;
        }
        show(folder, index);
    }

    /**
     * Brings the stack back if it is minimized, selects item {@code index} and activates its view, as a
     * click on its tab does.
     */
    void show(DartCTabFolder folder, int index) {
        if (folder.isDisposed()) return;
        if (minimized(folder)) CTabFolderHelper.handleRestore(folder, new Event());
        if (folder.isDisposed() || index < 0 || index >= folder.getItemCount()) return;
        folder.setSelection(index, true);
        CTabItem item = folder.getSelection();
        Control content = item == null || item.isDisposed() ? null : item.getControl();
        if (content != null && !content.isDisposed()) content.setFocus();
        else folder.getApi().forceFocus();
        // A tab already selected raises no Selection: the workbench activates its part on the
        // Activate a mouse-down on the folder sends.
        if (!folder.isDisposed()) {
            Event activate = new Event();
            activate.detail = SWT.MouseDown;
            folder.getApi().notifyListeners(SWT.Activate, activate);
        }
    }

    boolean minimized(DartCTabFolder folder) {
        Object element = folder.getApi().getData(MODEL_ELEMENT);
        return element != null && !Boolean.TRUE.equals(model.call(element, "isVisible"));
    }

    static final String MODEL_ELEMENT = "modelElement";

    /**
     * The e4 model, read by reflection and matched by interface name. Per session, with the methods
     * and interfaces it has looked up.
     */
    static final class Model {
        private final Map<Class<?>, java.util.Set<String>> interfaces = new java.util.HashMap<>();
        private final Map<Class<?>, Map<String, java.util.Optional<Method>>> methods = new java.util.HashMap<>();
        private final java.util.Set<String> reported = new java.util.HashSet<>();

        /**
         * The view stacks of the window's current perspective, in model order, each with the part of
         * {@code rect} the model's sash weights give it, and the editor area's. Worked out from the model
         * rather than from the widgets' bounds, so a stack keeps its place while it is minimized, and
         * while another is maximized over everything. Editor stacks (anything inside the area) keep their
         * tabs and are not listed; neither are detached windows, which are not the window's children.
         */
        void collect(Object element, Rectangle rect, Map<Object, Rectangle> stacks, Rectangle[] area) {
            if (is(element, "MArea")) {
                area[0] = rect;
                return;
            }
            if (is(element, "MPlaceholder")) {
                Object ref = call(element, "getRef");
                if (ref != null && is(ref, "MArea")) area[0] = rect;
                return;
            }
            if (is(element, "MPartStack")) {
                if (!hasTag(element, "EditorStack")) stacks.put(element, rect);
                return;
            }
            if (is(element, "MPerspectiveStack")) {
                Object selected = call(element, "getSelectedElement");
                if (selected != null) collect(selected, rect, stacks, area);
                return;
            }
            Object children = call(element, "getChildren");
            if (!(children instanceof List)) return;
            List<Object> rendered = new ArrayList<>();
            for (Object child : (List<?>) children) {
                if (Boolean.TRUE.equals(call(child, "isToBeRendered"))) rendered.add(child);
            }
            if (!is(element, "MPartSashContainer")) {
                for (Object child : rendered) collect(child, rect, stacks, area);
                return;
            }
            boolean horizontal = Boolean.TRUE.equals(call(element, "isHorizontal"));
            double total = 0;
            double[] weights = new double[rendered.size()];
            for (int i = 0; i < weights.length; i++) total += weights[i] = weight(rendered.get(i));
            double at = 0;
            for (int i = 0; i < weights.length; i++) {
                double share = total > 0 ? weights[i] / total : 1.0 / weights.length;
                Rectangle part = horizontal
                        ? new Rectangle(rect.x + (int) (at * rect.width), rect.y, (int) (share * rect.width), rect.height)
                        : new Rectangle(rect.x, rect.y + (int) (at * rect.height), rect.width, (int) (share * rect.height));
                at += share;
                collect(rendered.get(i), part, stacks, area);
            }
        }

        /** A sash child's weight: its container data, a number, as e4 reads it. */
        private double weight(Object element) {
            Object data = call(element, "getContainerData");
            if (data == null) return 100;
            try {
                return Math.max(0, Double.parseDouble(data.toString().trim()));
            } catch (NumberFormatException e) {
                return 100;
            }
        }

        /**
         * The composites of e4's TrimStacks (the trim icons of a minimized stack), by the element id of
         * the stack each one stands for: a TrimStack's id is the stack's element id followed by the
         * perspective's in parentheses. The rails stand in only for the view stacks' ones: the editor
         * area's stays.
         */
        Map<String, Control> trimStacks(Shell shell) {
            Map<String, Control> trims = new java.util.HashMap<>();
            for (int side : new int[] {SWT.LEFT, SWT.RIGHT, SWT.BOTTOM}) {
                Control bar = Config.trimAt(shell, side);
                if (!(bar instanceof Composite) || bar.isDisposed()) continue;
                for (Control child : ((Composite) bar).getChildren()) {
                    Object element = child.getData(MODEL_ELEMENT);
                    if (element == null || !hasTag(element, "TrimStack")) continue;
                    Object id = call(element, "getElementId");
                    if (id == null) continue;
                    String stackId = id.toString();
                    int paren = stackId.indexOf('(');
                    trims.put(paren > 0 ? stackId.substring(0, paren) : stackId, child);
                }
            }
            return trims;
        }

        boolean hasTag(Object element, String tag) {
            Object tags = call(element, "getTags");
            return tags instanceof List && ((List<?>) tags).contains(tag);
        }

        /** Whether {@code element} implements the e4 model interface named {@code simpleName}. */
        boolean is(Object element, String simpleName) {
            return interfaces.computeIfAbsent(element.getClass(), Model::interfaceNames).contains(simpleName);
        }

        private static java.util.Set<String> interfaceNames(Class<?> type) {
            java.util.Set<String> names = new java.util.HashSet<>();
            for (Class<?> c = type; c != null; c = c.getSuperclass()) addInterfaces(c, names);
            return names;
        }

        private static void addInterfaces(Class<?> c, java.util.Set<String> names) {
            for (Class<?> i : c.getInterfaces()) {
                if (names.add(i.getSimpleName())) addInterfaces(i, names);
            }
        }

        /** The no-argument public method {@code name} of {@code target}, invoked; null when it has none. */
        Object call(Object target, String name) {
            java.util.Optional<Method> method = methods.computeIfAbsent(target.getClass(), c -> new java.util.HashMap<>())
                    .computeIfAbsent(name, n -> {
                        try {
                            return java.util.Optional.of(target.getClass().getMethod(n));
                        } catch (NoSuchMethodException e) {
                            return java.util.Optional.empty();
                        }
                    });
            if (!method.isPresent()) return null;
            try {
                return method.get().invoke(target);
            } catch (ReflectiveOperationException | RuntimeException e) {
                report(target, name, e);
                return null;
            }
        }

        /**
         * {@code target}'s method {@code name} taking {@code args}, invoked: a public one, or one its
         * class or a superclass declares (a renderer's own). Throws when there is none or it fails.
         */
        Object invoke(Object target, String name, Object... args) throws ReflectiveOperationException {
            try {
                Method method = find(target.getClass(), name, args);
                if (method == null) throw new NoSuchMethodException(target.getClass().getName() + "." + name);
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (ReflectiveOperationException | RuntimeException e) {
                report(target, name, e);
                throw e;
            }
        }

        /**
         * Prints a failed e4 call once per method name: the rails reach e4 by reflection, so a
         * signature e4 changed would otherwise leave a rail or a menu item silently doing nothing.
         */
        void report(Object target, String name, Throwable failure) {
            String key = target.getClass().getName() + "." + name;
            if (!reported.add(key)) return;
            Throwable cause = failure instanceof java.lang.reflect.InvocationTargetException && failure.getCause() != null
                    ? failure.getCause() : failure;
            System.err.println("[ViewRails] " + key + " failed: " + cause);
        }

        private static Method find(Class<?> type, String name, Object[] args) {
            for (Method m : type.getMethods()) if (accepts(m, name, args)) return m;
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) if (accepts(m, name, args)) return m;
            }
            return null;
        }

        private static boolean accepts(Method m, String name, Object[] args) {
            if (!m.getName().equals(name) || m.getParameterCount() != args.length) return false;
            Class<?>[] types = m.getParameterTypes();
            for (int i = 0; i < args.length; i++) {
                Class<?> t = types[i].isPrimitive() ? boxed(types[i]) : types[i];
                if (args[i] != null && !t.isInstance(args[i])) return false;
            }
            return true;
        }

        private static Class<?> boxed(Class<?> primitive) {
            if (primitive == int.class) return Integer.class;
            if (primitive == float.class) return Float.class;
            if (primitive == boolean.class) return Boolean.class;
            if (primitive == long.class) return Long.class;
            if (primitive == double.class) return Double.class;
            return primitive;
        }

        /** The placeholder of the editor area in the window's current perspective, or the area itself. */
        Object editorArea(Object element) {
            if (is(element, "MArea")) return element;
            if (is(element, "MPlaceholder")) {
                Object ref = call(element, "getRef");
                return ref != null && is(ref, "MArea") ? element : null;
            }
            if (is(element, "MPerspectiveStack")) {
                Object selected = call(element, "getSelectedElement");
                return selected == null ? null : editorArea(selected);
            }
            Object children = call(element, "getChildren");
            if (!(children instanceof List)) return null;
            for (Object child : (List<?>) children) {
                Object found = editorArea(child);
                if (found != null) return found;
            }
            return null;
        }

        /** The window's perspective stack, the first one under {@code element}; null for none. */
        Object perspectiveStack(Object element) {
            if (is(element, "MPerspectiveStack")) return element;
            Object children = call(element, "getChildren");
            if (!(children instanceof List)) return null;
            for (Object child : (List<?>) children) {
                Object found = perspectiveStack(child);
                if (found != null) return found;
            }
            return null;
        }
    }

    /** What one record says. */
    static final class Record {
        static Record empty() {
            return new Record(Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), MEDIUM);
        }

        final List<CTabFolder> stacks;
        final List<Long> places;
        final List<Control> rails;
        final List<Long> railSides;
        final List<Control> trims;
        final int size;

        Record(List<CTabFolder> stacks, List<Long> places, List<Control> rails, List<Long> railSides,
               List<Control> trims, int size) {
            this.stacks = stacks;
            this.places = places;
            this.rails = rails;
            this.railSides = railSides;
            this.trims = trims;
            this.size = size;
        }

        /** The place of {@code folder}'s icons, minimized flag left out; -1 when it is not listed. */
        int placeOf(CTabFolder folder) {
            int index = stacks.indexOf(folder);
            return index < 0 ? -1 : (int) (places.get(index) & ~MINIMIZED);
        }

        /** The first listed stack at {@code place}, other than {@code except}; null for none. */
        CTabFolder stackAt(int place, CTabFolder except) {
            for (int i = 0; i < stacks.size(); i++) {
                if (stacks.get(i) != except && (places.get(i) & ~MINIMIZED) == place) return stacks.get(i);
            }
            return null;
        }

        /** The listed stack the client knows by {@code id}, or null. */
        CTabFolder stackById(long id) {
            for (CTabFolder stack : stacks) {
                if (!stack.isDisposed() && FlutterBridge.id(stack.getImpl()) == id) return stack;
            }
            return null;
        }

        /** The side ({@code SWT.LEFT} or {@code SWT.RIGHT}) of the rail {@code rail}; 0 when not a rail. */
        int sideOf(Control rail) {
            int index = rails.indexOf(rail);
            return index < 0 ? 0 : railSides.get(index).intValue();
        }

        /** The rail drawn on {@code side}; null for none. */
        Control railOn(int side) {
            int index = railSides.indexOf((long) side);
            return index < 0 ? null : rails.get(index);
        }

        /** Whether the rail {@code trim} has any icon to draw. */
        boolean hostsAny(Widget trim) {
            int index = rails.indexOf(trim);
            if (index < 0) return false;
            boolean right = railSides.get(index) == SWT.RIGHT;
            for (long place : places) {
                if (isRight((int) place) == right) return true;
            }
            return false;
        }

        boolean railWidthsDiffer(Record other) {
            java.util.Set<Control> all = new java.util.HashSet<>(rails);
            all.addAll(other.rails);
            if (size != other.size) return true;
            for (Control rail : all) if (hostsAny(rail) != other.hostsAny(rail)) return true;
            return false;
        }

        boolean writeWire(WidgetWire w, int key) {
            switch (key) {
                case WireTable.VIEW_RAILS_STACKS:
                    w.refs(stacks.toArray(new Widget[0]));
                    return true;
                case WireTable.VIEW_RAILS_PLACES:
                    w.write(byId(stacks, places));
                    return true;
                case WireTable.VIEW_RAILS_RAILS:
                    w.refs(rails.toArray(new Widget[0]));
                    return true;
                case WireTable.VIEW_RAILS_RAIL_SIDES:
                    w.write(byId(rails, railSides));
                    return true;
                case WireTable.VIEW_RAILS_TRIMS:
                    w.refs(trims.toArray(new Widget[0]));
                    return true;
                case WireTable.VIEW_RAILS_SIZE:
                    w.write((long) size);
                    return true;
            }
            return false;
        }

        /**
         * Each widget's id followed by its value. Not a list beside the references: the client leaves
         * out a reference it cannot resolve yet, which would shift every value after it.
         */
        private static long[] byId(List<? extends Widget> widgets, List<Long> values) {
            long[] out = new long[widgets.size() * 2];
            for (int i = 0; i < widgets.size(); i++) {
                out[2 * i] = FlutterBridge.id(widgets.get(i).getImpl());
                out[2 * i + 1] = values.get(i);
            }
            return out;
        }
    }
}

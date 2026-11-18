package dev.equo.swt;

import dev.equo.swt.delivery.Canon;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.VWidget;
import org.eclipse.swt.widgets.Widget;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Calls every public setter of a widget with values its getters return and records each mark that
 * names a property whose wire value stayed as it was, on any widget. A mark tells the client its copy
 * is out of date; a property the wire compares anyway ({@link VWidget#wireCompared}) is left out,
 * since its mark is dropped before it is written.
 */
final class SetAgainSweep {

    /** What the sweep found for one widget. */
    static final class Result {
        /** "setter(args)" to the properties it marked while their wire value stayed as it was. */
        final Map<String, Set<String>> marked = new TreeMap<>();
        /** The widget's own wire properties some setter changed and marked, with the setters that did. */
        final Map<String, Set<String>> reached = new TreeMap<>();
        /** "setter(args)" to the properties of the widget swept it changed on the wire without marking. */
        final Map<String, Set<String>> unmarked = new TreeMap<>();
        /** Setters no getter pairs with, which the sweep cannot call. */
        final Set<String> unpaired = new TreeSet<>();
    }

    private final Set<DartWidget> seen = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final Result result = new Result();
    /** The shell the widget swept is on: what an earlier test left queued elsewhere is not this sweep's. */
    private final org.eclipse.swt.widgets.Shell shell;
    /** Whether each widget queued so far is on [shell], asked once per widget. */
    private final Map<DartWidget, Boolean> onShell = new IdentityHashMap<>();

    private SetAgainSweep(org.eclipse.swt.widgets.Shell shell) {
        this.shell = shell;
    }

    /**
     * Sweeps a widget [make] builds: every setter given its getter's value as built; then, setter by
     * setter, the value a twin holds once [fill] has filled it, and that value again.
     */
    static Result run(Supplier<? extends Widget> make, Consumer<Widget> fill) {
        Widget w = make.get();
        SetAgainSweep sweep = new SetAgainSweep(shellOf(w));
        Widget twin = make.get();
        fill.accept(twin);
        List<Method> setters = setters(w.getClass());
        for (Method setter : setters) {
            if (w.isDisposed()) break;
            sweep.call(w, setter, w, "as built ");
        }
        // As built, then filled: filled, it has the text, items and columns a selection, a top index or
        // a sort column needs; as built, nothing refuses a change the fill rules out (block selection
        // under word wrap).
        sweep.changes(w, twin, setters);
        if (!w.isDisposed()) {
            fill.accept(w);
            // The fill's own changes are not the next setter's.
            if (w.getImpl() instanceof DartWidget) sweep.described.put((DartWidget) w.getImpl(), describe((DartWidget) w.getImpl()));
        }
        sweep.changes(w, twin, setters);
        sweep.drain();
        return sweep.result;
    }

    /** Each setter given another value, the twin's or one of the widget's own, and then that value again. */
    private void changes(Widget w, Widget twin, List<Method> setters) {
        for (Method setter : setters) {
            if (w.isDisposed() || twin.isDisposed()) return;
            call(w, setter, twin, "changed ");
            call(w, setter, w, "");
            for (Object[] args : alternatives(w, setter)) {
                if (w.isDisposed()) return;
                invoke(w, setter, args, "changed ");
                call(w, setter, w, "");
            }
        }
    }

    /** SWT's constants an int property takes: alignments, directions, positions, orientations. */
    private static final int[] CONSTANTS = {
        0, 1, 2, 3, org.eclipse.swt.SWT.LEFT, org.eclipse.swt.SWT.RIGHT, org.eclipse.swt.SWT.CENTER,
        org.eclipse.swt.SWT.UP, org.eclipse.swt.SWT.DOWN, org.eclipse.swt.SWT.TOP, org.eclipse.swt.SWT.BOTTOM,
        org.eclipse.swt.SWT.LEFT_TO_RIGHT, org.eclipse.swt.SWT.RIGHT_TO_LEFT, org.eclipse.swt.SWT.HORIZONTAL,
        org.eclipse.swt.SWT.VERTICAL, org.eclipse.swt.SWT.INHERIT_DEFAULT, org.eclipse.swt.SWT.INHERIT_FORCE,
        org.eclipse.swt.SWT.DRAW_DELIMITER, 40,
    };

    /**
     * Further argument lists for a one-argument setter of [w]: SWT's constants for an int, a cursor,
     * and a widget built on [w]'s own tree for a widget-typed one (a menu, a child control, a caret,
     * one of its own columns or items).
     */
    private List<Object[]> alternatives(Widget w, Method setter) {
        List<Object[]> out = new ArrayList<>();
        Class<?>[] p = setter.getParameterTypes();
        // Each value, then the same again: a flag with no getter to pair (a ViewForm's border) is swept too.
        if (p.length == 1 && p[0] == boolean.class) {
            for (boolean b : new boolean[] {true, true, false, false}) out.add(new Object[] {b});
            return out;
        }
        // A location or a size given as ints, which a setter with no getter to pair (a Menu's) takes.
        if (p.length > 1 && allInt(p)) {
            for (int base : new int[] {10, 30}) {
                Object[] args = new Object[p.length];
                for (int i = 0; i < p.length; i++) args[i] = base + i;
                out.add(args);
            }
            return out;
        }
        // Several values of other kinds (a CLabel's gradient, a CTabFolder's top-right control): one of each.
        if (p.length > 1) {
            // Plain numbers, then SWT's position constants for the ints (setTopRight's alignment).
            for (int position : new int[] {0, org.eclipse.swt.SWT.RIGHT, org.eclipse.swt.SWT.FILL}) {
                Object[] args = new Object[p.length];
                for (int i = 0; i < p.length; i++) {
                    args[i] = position != 0 && p[i] == int.class ? position : candidate(w, p[i], i);
                    if (args[i] == null) return out;
                }
                out.add(args);
            }
            return out;
        }
        Class<?> t = p[0];
        try {
            if (t == int.class) {
                // A style constant is a high bit, which a count or a size would take as millions.
                boolean takesStyle = setter.getName().matches(".*(Orientation|Alignment|Direction|Position|Mode)");
                for (int c : CONSTANTS) if (takesStyle || c <= 40) out.add(new Object[] {c});
            } else if (t == String.class) {
                // A setter with no getter to pair (a Browser's text) is swept with these alone.
                out.add(new Object[] {"a"});
                out.add(new Object[] {"https://example.org/"});
            } else if (t == org.eclipse.swt.graphics.Image.class) {
                // Pixels of their own, so the image travels under a name of its own.
                out.add(new Object[] {new org.eclipse.swt.graphics.Image(w.getDisplay(), 3, 5)});
            } else if (t == org.eclipse.swt.graphics.Region.class) {
                org.eclipse.swt.graphics.Region region = new org.eclipse.swt.graphics.Region(w.getDisplay());
                region.add(new Rectangle(1, 2, 7, 9));
                out.add(new Object[] {region});
            } else if (t == org.eclipse.swt.graphics.Cursor.class) {
                out.add(new Object[] {new org.eclipse.swt.graphics.Cursor(w.getDisplay(), org.eclipse.swt.SWT.CURSOR_HAND)});
            } else if (Widget.class.isAssignableFrom(t)) {
                for (Widget made : ownWidgets(w, t)) out.add(new Object[] {made});
            }
        } catch (RuntimeException e) {
            // No value of that type can be built here; the reach report says what that leaves.
        }
        return out;
    }

    /** A value of [type] for the [i]-th argument of a setter of [w], or null when none is at hand. */
    private static Object candidate(Widget w, Class<?> type, int i) {
        org.eclipse.swt.graphics.Device device = w.getDisplay();
        if (type == int.class) return 50 + i;
        if (type == boolean.class) return Boolean.TRUE;
        if (type == String.class) return "a";
        if (type == Point.class) return new Point(20, 20);
        if (type == org.eclipse.swt.graphics.Color.class) return new org.eclipse.swt.graphics.Color(device, 10, 20, 30);
        if (type == int[].class) return new int[] {60};
        if (type == Point[].class) return new Point[] {new Point(40, 20)};
        if (type == org.eclipse.swt.graphics.Color[].class)
            return new org.eclipse.swt.graphics.Color[] {
                new org.eclipse.swt.graphics.Color(device, 10, 20, 30), new org.eclipse.swt.graphics.Color(device, 200, 100, 0)};
        if (Widget.class.isAssignableFrom(type)) {
            List<Widget> own = ownWidgets(w, type);
            return own.isEmpty() ? null : own.get(0);
        }
        return null;
    }

    /** Widgets of [type] that belong with [w]: built on its shell or as its children, or its own members. */
    private static List<Widget> ownWidgets(Widget w, Class<?> type) {
        List<Widget> out = new ArrayList<>();
        org.eclipse.swt.widgets.Control control = w instanceof org.eclipse.swt.widgets.Control
            ? (org.eclipse.swt.widgets.Control) w : null;
        org.eclipse.swt.widgets.Composite host = w instanceof org.eclipse.swt.widgets.Composite
            ? (org.eclipse.swt.widgets.Composite) w : control != null ? control.getParent() : parentOf(w);
        if (type == org.eclipse.swt.widgets.Menu.class) {
            org.eclipse.swt.widgets.Shell shell = host != null ? host.getShell() : shellOf(w);
            if (shell != null) {
                out.add(new org.eclipse.swt.widgets.Menu(shell, org.eclipse.swt.SWT.POP_UP));
                out.add(new org.eclipse.swt.widgets.Menu(shell, org.eclipse.swt.SWT.DROP_DOWN));
                out.add(new org.eclipse.swt.widgets.Menu(shell, org.eclipse.swt.SWT.BAR));
            }
        } else if (type == org.eclipse.swt.widgets.Caret.class && w instanceof org.eclipse.swt.widgets.Canvas) {
            out.add(new org.eclipse.swt.widgets.Caret((org.eclipse.swt.widgets.Canvas) w, org.eclipse.swt.SWT.NONE));
        } else if (org.eclipse.swt.widgets.Control.class.isAssignableFrom(type) && host != null) {
            out.add(new org.eclipse.swt.widgets.Button(host, org.eclipse.swt.SWT.PUSH));
        }
        // An item of the widget's own (a sort column, a tab to select), built as its constructor builds one.
        if (org.eclipse.swt.widgets.Item.class.isAssignableFrom(type)) {
            for (java.lang.reflect.Constructor<?> c : type.getConstructors()) {
                Class<?>[] cp = c.getParameterTypes();
                if (cp.length == 2 && cp[0].isInstance(w) && cp[1] == int.class) {
                    try {
                        // Two: a folder selects the first item it is given, so only the second is a change.
                        out.add((Widget) c.newInstance(w, org.eclipse.swt.SWT.NONE));
                        out.add((Widget) c.newInstance(w, org.eclipse.swt.SWT.NONE));
                    } catch (ReflectiveOperationException | RuntimeException ignored) {
                        // Not an item this widget can hold.
                    }
                }
            }
        }
        for (String members : new String[] {"getColumns", "getItems"}) {
            try {
                Object[] own = (Object[]) w.getClass().getMethod(members).invoke(w);
                for (Object o : own) if (type.isInstance(o)) out.add((Widget) o);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Not a widget with members of that kind.
            }
        }
        return out;
    }

    /**
     * The display mock is shared by the whole JVM and Mockito keeps every call made on it; a sweep
     * makes millions, which outgrow the test heap unless they are dropped as it goes.
     */
    private static void forgetDisplayCalls(Widget w) {
        org.eclipse.swt.widgets.Display display = w.isDisposed() ? null : w.getDisplay();
        if (display == null || !org.mockito.Mockito.mockingDetails(display).isMock()) return;
        org.mockito.Mockito.clearInvocations(display);
        if (org.mockito.Mockito.mockingDetails(display.getImpl()).isMock()) org.mockito.Mockito.clearInvocations(display.getImpl());
    }

    /** The shell of a widget whose parent is not a composite (a menu item's is its menu), or null. */
    private static org.eclipse.swt.widgets.Shell shellOf(Widget w) {
        for (Object o = w; o != null; ) {
            try {
                Object shell = o.getClass().getMethod("getShell").invoke(o);
                if (shell instanceof org.eclipse.swt.widgets.Shell) return (org.eclipse.swt.widgets.Shell) shell;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Asked of its parent instead.
            }
            try {
                o = o.getClass().getMethod("getParent").invoke(o);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return null;
            }
        }
        return null;
    }

    /** The composite an item belongs to, which hosts what is built for it. */
    private static org.eclipse.swt.widgets.Composite parentOf(Widget w) {
        try {
            Object parent = w.getClass().getMethod("getParent").invoke(w);
            return parent instanceof org.eclipse.swt.widgets.Composite ? (org.eclipse.swt.widgets.Composite) parent : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Calls [setter] on [w] with what [source] holds, or, for a change call, something else when the
     * two hold the same; a change call records what it reached.
     */
    private void call(Widget w, Method setter, Widget source, String phase) {
        boolean change = !phase.isEmpty() && source != w;
        List<Object[]> calls;
        try {
            calls = currentArgs(source, setter);
            if (change && calls != null) {
                List<Object[]> held = currentArgs(w, setter);
                for (int i = 0; i < calls.size(); i++) {
                    Object[] args = calls.get(i);
                    if (held != null && i < held.size() && java.util.Arrays.deepEquals(args, held.get(i)))
                        args[args.length - 1] = other(args[args.length - 1]);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            calls = null;
        }
        if (calls == null || calls.isEmpty()) {
            result.unpaired.add(signature(setter));
            return;
        }
        for (Object[] args : calls) {
            if (w.isDisposed()) return;
            // The twin's caret, menu or item belongs to the twin: handing it to the widget swept would
            // make the two share it, which no application does.
            if (change && java.util.Arrays.stream(args).anyMatch(a -> a instanceof Widget || a instanceof Widget[]))
                continue;
            invoke(w, setter, args, phase);
        }
    }

    /**
     * Calls [setter] on [w] with [args] and records each mark whose property reads as it did, on any
     * widget; a change call ([phase] not empty, the widget's own value not given back) also records
     * what it reached.
     */
    private void invoke(Widget w, Method setter, Object[] args, String phase) {
        forgetDisplayCalls(w);
        boolean change = !phase.isEmpty();
        // What changed outside the calls swept (the fill, a widget built for an argument) is marked too.
        for (DartWidget widget : marks().keySet()) described.put(widget, describe(widget));
        drain();
        describeNew();
        DartWidget self = w.getImpl() instanceof DartWidget ? (DartWidget) w.getImpl() : null;
        if (self != null && !described.containsKey(self)) {
            // Describing it the first time can mark it, which is not the call's doing.
            seen.add(self);
            described.put(self, describe(self));
            drain();
        }
        Map<String, Object> before = self == null ? null : described.get(self);
        boolean threw = false;
        try {
            setter.invoke(w, args);
        } catch (ReflectiveOperationException | RuntimeException e) {
            threw = true;
        }
        Map<DartWidget, Map<String, Object>> after = new IdentityHashMap<>();
        if (self != null) after.put(self, describe(self));
        if (!threw && before != null) unmarked(self, before, after.get(self), signature(setter), phase);
        if (self != null) described.put(self, after.get(self));
        for (Map.Entry<DartWidget, Set<String>> e : marks().entrySet()) {
            DartWidget widget = e.getKey();
            Map<String, Object> was = widget == self ? before : described.get(widget);
            Map<String, Object> now = after.containsKey(widget) ? after.get(widget) : describe(widget);
            described.put(widget, now);
            if (threw) continue;
            for (String name : e.getValue()) {
                boolean same = was != null && now != null
                        && java.util.Objects.equals(Canon.canon(was.get(name)), Canon.canon(now.get(name)));
                if (same) {
                    String where = widget.getApi() == w ? "" : widget.getApi().getClass().getSimpleName() + ".";
                    result.marked.computeIfAbsent(phase + signature(setter), k -> new TreeSet<>()).add(where + name);
                } else if (change && widget.getApi() == w) {
                    result.reached.computeIfAbsent(name, k -> new TreeSet<>()).add(signature(setter));
                }
            }
        }
    }

    /**
     * Records each property of [self] whose wire value differs from [before] with no mark on it,
     * compared ones included: an unmarked property is never written, so the client keeps the old value.
     */
    private void unmarked(DartWidget self, Map<String, Object> before, Map<String, Object> now, String setter, String phase) {
        if (now == null) return;
        VWidget v = self.getValue();
        Set<String> names = new TreeSet<>();
        ChangedKeys changed = v.changedKeys();
        for (int i = 0; i < changed.size(); i++) names.add(v.wireName(changed.get(i)));
        Set<String> keys = new TreeSet<>(before.keySet());
        keys.addAll(now.keySet());
        for (String name : keys) {
            if (names.contains(name)) continue;
            if (java.util.Objects.equals(Canon.canon(before.get(name)), Canon.canon(now.get(name)))) continue;
            result.unmarked.computeIfAbsent(phase + setter, k -> new TreeSet<>()).add(name);
        }
    }

    /**
     * Each widget's whole record as of the last call that marked it. Once the marks are drained a
     * record changes only where something marks it, so a call re-describes just the widgets it marked.
     */
    private final Map<DartWidget, Map<String, Object>> described = new IdentityHashMap<>();

    /** Describes the widgets marked for the first time since the last call. */
    private void describeNew() {
        boolean any = false;
        for (DartWidget widget : new ArrayList<>(seen)) {
            if (described.containsKey(widget)) continue;
            described.put(widget, describe(widget));
            any = true;
        }
        if (any) drain();
    }

    /**
     * [widget]'s whole record as it travels, or null when it cannot be described. An image or a region
     * stays the name it travels under rather than what it resolves to: a new image with the same
     * pixels is another image to the client.
     */
    private static Map<String, Object> describe(DartWidget widget) {
        Object api = widget.getApi();
        if (!(api instanceof Widget) || ((Widget) api).isDisposed() || widget.getValue() == null) return null;
        try {
            Serializer.Tape tape = new Serializer.Tape(0);
            tape.describe(widget);
            Serializer.discardWritten();
            String id = "/" + api.hashCode();
            for (WireFrames.Record record : WireFrames.records(tape.frame().payload())) {
                if (record.channel().endsWith(id)) return Canon.canon(record.state());
            }
            return null;
        } catch (RuntimeException | AssertionError e) {
            return null;
        }
    }

    /** The names of every wire property of [value]. */
    static Set<String> wireNames(VWidget value) {
        Set<String> names = new TreeSet<>();
        for (int key : value.wireKeys()) {
            String name = value.wireName(key);
            if (name != null) names.add(name);
        }
        return names;
    }

    /** Public instance setters of [type] and its supertypes, one per signature. */
    private static List<Method> setters(Class<?> type) {
        Map<String, Method> found = new TreeMap<>();
        for (Method m : type.getMethods()) {
            // A Browser's setText and setUrl answer whether they took the value.
            if (Modifier.isStatic(m.getModifiers()) || (m.getReturnType() != void.class && m.getReturnType() != boolean.class)) continue;
            if (!m.getName().startsWith("set") || m.getName().length() < 4 || m.getParameterCount() == 0) continue;
            if (m.getName().equals("setImpl")) continue;
            found.putIfAbsent(signature(m), m);
        }
        return new ArrayList<>(found.values());
    }

    private static String signature(Method m) {
        StringBuilder s = new StringBuilder(m.getName()).append('(');
        Class<?>[] p = m.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) s.append(',');
            s.append(p[i].getSimpleName());
        }
        return s.append(')').toString();
    }

    /**
     * The argument lists that give [setter] what [w] holds now: the getter's value for a one-argument
     * setter, a Point or Rectangle spread over int arguments, and every index of an indexed setter.
     * Null when no getter pairs with it.
     */
    private static List<Object[]> currentArgs(Widget w, Method setter) throws ReflectiveOperationException {
        String suffix = setter.getName().substring(3);
        Class<?>[] p = setter.getParameterTypes();
        List<Object[]> calls = new ArrayList<>();
        Method get = getter(w.getClass(), suffix);
        if (p.length == 1 && get != null && box(p[0]).isAssignableFrom(box(get.getReturnType()))) {
            calls.add(new Object[] {get.invoke(w)});
            return calls;
        }
        if (allInt(p) && get != null && p.length == 2 && get.getReturnType() == Point.class) {
            Point pt = (Point) get.invoke(w);
            if (pt != null) calls.add(new Object[] {pt.x, pt.y});
            return calls;
        }
        if (allInt(p) && get != null && p.length == 4 && get.getReturnType() == Rectangle.class) {
            Rectangle r = (Rectangle) get.invoke(w);
            if (r != null) calls.add(new Object[] {r.x, r.y, r.width, r.height});
            return calls;
        }
        if (p.length == 2 && p[0] == int.class) {
            Method indexed = indexedGetter(w.getClass(), suffix);
            if (indexed != null && box(p[1]).isAssignableFrom(box(indexed.getReturnType()))) {
                int count = count(w);
                for (int i = 0; i < count; i++) {
                    try {
                        calls.add(new Object[] {i, indexed.invoke(w, i)});
                    } catch (InvocationTargetException e) {
                        break;
                    }
                }
                return calls;
            }
        }
        return null;
    }

    /** A value of [v]'s type other than [v], where one is at hand; else [v]. */
    private static Object other(Object v) {
        if (v instanceof Boolean) return !(Boolean) v;
        if (v instanceof Integer) return (Integer) v + 1;
        if (v instanceof String) return v + "x";
        if (v instanceof Character) return (char) ((Character) v + 1);
        return v;
    }

    private static boolean allInt(Class<?>[] p) {
        for (Class<?> c : p) if (c != int.class) return false;
        return p.length > 0;
    }

    private static Method getter(Class<?> type, String suffix) {
        for (String prefix : new String[] {"get", "is"}) {
            try {
                Method m = type.getMethod(prefix + suffix);
                if (m.getReturnType() != void.class) return m;
            } catch (NoSuchMethodException ignored) {
                // Tried under the other prefix.
            }
        }
        return null;
    }

    private static Method indexedGetter(Class<?> type, String suffix) {
        try {
            return type.getMethod("get" + suffix, int.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /** How many indexes an indexed setter of [w] takes: its items, else its columns, else one. */
    private static int count(Widget w) {
        for (String name : new String[] {"getItemCount", "getColumnCount"}) {
            try {
                int n = (Integer) w.getClass().getMethod(name).invoke(w);
                if (n > 0) return Math.min(n, 4);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Not a widget with that count.
            }
        }
        return 1;
    }

    private static Class<?> box(Class<?> c) {
        if (!c.isPrimitive()) return c;
        if (c == int.class) return Integer.class;
        if (c == boolean.class) return Boolean.class;
        if (c == long.class) return Long.class;
        if (c == char.class) return Character.class;
        if (c == double.class) return Double.class;
        if (c == float.class) return Float.class;
        if (c == byte.class) return Byte.class;
        if (c == short.class) return Short.class;
        return c;
    }

    /** The bridge's queue of widgets the next flush sends, which is where a mark on any widget lands. */
    @SuppressWarnings("unchecked")
    /** Adds the widgets queued on [shell] to [seen]. */
    private void track() {
        for (DartWidget widget : queued()) {
            if (onShell.computeIfAbsent(widget, k -> {
                Object api = k.getApi();
                return api instanceof Widget && !((Widget) api).isDisposed() && shellOf((Widget) api) == shell;
            })) seen.add(widget);
        }
    }

    private static List<DartWidget> queued() {
        try {
            Field f = FlutterBridge.class.getDeclaredField("dirtyWidgets");
            f.setAccessible(true);
            return new ArrayList<>((List<DartWidget>) f.get(null));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Forgets every outstanding mark, so what follows is the next call's alone. */
    private void drain() {
        track();
        for (DartWidget widget : seen) {
            VWidget v = widget.getValue();
            if (v != null) v.clearDirty();
        }
        FlutterBridge.clearDirty();
    }

    /** The marks outstanding that the wire does not compare, by widget and name. */
    private Map<DartWidget, Set<String>> marks() {
        track();
        Map<DartWidget, Set<String>> out = new IdentityHashMap<>();
        for (DartWidget widget : seen) {
            VWidget v = widget.getValue();
            if (v == null) continue;
            ChangedKeys changed = v.changedKeys();
            for (int i = 0; i < changed.size(); i++) {
                int key = changed.get(i);
                if (v.wireCompared(key) || !written(v, key)) continue;
                out.computeIfAbsent(widget, k -> new TreeSet<>()).add(v.wireName(key));
            }
        }
        return out;
    }

    /**
     * Whether a change record of [v] writes [key]: a property the client of that type never reads (a
     * CTabFolder's hasOwnBackground, which Control's background setter names for every control) is
     * dropped as it is written, so its mark sends nothing.
     */
    static boolean written(VWidget v, int key) {
        WidgetWire w = WidgetWire.borrow();
        try {
            return v.writeWire(w, key);
        } catch (RuntimeException e) {
            return true;
        } finally {
            WidgetWire.giveBack(w);
        }
    }
}

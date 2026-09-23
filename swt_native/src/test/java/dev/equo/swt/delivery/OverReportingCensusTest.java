package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

/**
 * Counts the setters that name a property nothing shows changed.
 *
 * <p>{@link FlagsMatchChangesTest} asserts the two agree for a handful of hand-written cases. This
 * sweeps every single-argument setter it can call on a spread of widgets and reports the ones that
 * disagree, so the size of the problem is a number rather than an impression. It reports; it does
 * not fail, because over-reporting costs bytes rather than correctness — an update that names a
 * property nothing changed is wasteful, not wrong.
 *
 * <p>The two sides are independent by construction: one is the widget's own record, filled by its
 * setters, the other is derived from the serialized state before and after, knowing nothing about
 * flags.
 */
@ExtendWith(Mocks.class)
class OverReportingCensusTest {

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        FlutterBridge.setSendObserver(null);
    }

    private record Subject(String name, BiFunction<Composite, Integer, Widget> create, int style) {
    }

    private static final List<Subject> SUBJECTS = List.of(
            new Subject("Label", (p, s) -> new Label(p, s), SWT.NONE),
            new Subject("Button", (p, s) -> new Button(p, s), SWT.CHECK),
            new Subject("Text", (p, s) -> new Text(p, s), SWT.NONE),
            new Subject("Combo", (p, s) -> new Combo(p, s), SWT.NONE),
            new Subject("Spinner", (p, s) -> new Spinner(p, s), SWT.NONE),
            new Subject("Scale", (p, s) -> new Scale(p, s), SWT.NONE),
            new Subject("ProgressBar", (p, s) -> new ProgressBar(p, s), SWT.NONE),
            new Subject("List", (p, s) -> new org.eclipse.swt.widgets.List(p, s), SWT.NONE),
            new Subject("Group", (p, s) -> new Group(p, s), SWT.NONE),
            new Subject("Composite", (p, s) -> new Composite(p, s), SWT.NONE),
            new Subject("Canvas", (p, s) -> new Canvas(p, s), SWT.NONE),
            new Subject("Table", (p, s) -> new Table(p, s), SWT.NONE),
            new Subject("Tree", (p, s) -> new Tree(p, s), SWT.NONE),
            new Subject("ToolBar", (p, s) -> new ToolBar(p, s), SWT.NONE),
            new Subject("Sash", (p, s) -> new Sash(p, s), SWT.NONE),
            new Subject("Link", (p, s) -> new Link(p, s), SWT.NONE));

    /** Setters whose whole job is to not change the state when the value is already what it is. */
    private static final Set<String> SKIP = Set.of(
            "setData", "setLayout", "setLayoutData", "setMenu", "setParent", "setRegion",
            "setCursor", "setFont", "setBackground", "setForeground", "setBackgroundImage",
            "setLayoutDeferred", "setRedraw", "setFocus", "setCapture", "setImage", "setImages");

    @Test
    void reportSettersThatNamePropertiesNothingChanged() {
        var overReported = new TreeMap<String, Set<String>>();
        var silent = new TreeMap<String, Set<String>>();
        int checked = 0;

        for (Subject subject : SUBJECTS) {
            for (Method setter : settersOf(subject)) {
                Object argument = cannedArgument(setter);
                if (argument == null) continue;

                Shell shell = Mocks.shell();
                Composite parent = new Composite(shell, SWT.NONE);
                Widget widget = subject.create().apply(parent, subject.style());
                try {
                    settle(parent);
                    clearFlags(widget);

                    String before = Canon.canon(serialize(widget));
                    try {
                        setter.invoke(widget, argument);
                    } catch (Throwable ignored) {
                        // A setter that rejects the canned value tells us nothing either way.
                        continue;
                    }
                    Set<String> flagged = new LinkedHashSet<>(flags(widget));
                    Set<String> derived = DeliveryAudit.changedKeys(before, Canon.canon(serialize(widget)));
                    checked++;

                    var extra = new LinkedHashSet<>(flagged);
                    extra.removeAll(derived);
                    if (!extra.isEmpty())
                        overReported.computeIfAbsent(subject.name() + "." + setter.getName(),
                                k -> new LinkedHashSet<>()).addAll(extra);

                    var missing = new LinkedHashSet<>(derived);
                    missing.removeAll(flagged);
                    if (!missing.isEmpty())
                        silent.computeIfAbsent(subject.name() + "." + setter.getName(),
                                k -> new LinkedHashSet<>()).addAll(missing);
                } finally {
                    // One sweep builds a few hundred widgets. Left alive they outlast the test and
                    // become whatever the next one in the JVM has to work around.
                    dispose(parent);
                }
            }
        }

        System.out.println("CENSUS setters checked: " + checked);
        System.out.println("CENSUS over-reporting setters: " + overReported.size());
        overReported.forEach((where, keys) -> System.out.println("CENSUS  over  " + where + " -> " + keys));
        System.out.println("CENSUS unrecorded setters: " + silent.size());
        silent.forEach((where, keys) -> System.out.println("CENSUS  silent " + where + " -> " + keys));
    }

    private static void dispose(Composite parent) {
        try {
            if (!parent.isDisposed()) parent.dispose();
        } catch (Throwable ignored) {
            // A widget that will not dispose is not what is being measured here.
        }
    }

    private static List<Method> settersOf(Subject subject) {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Class<?> type = subject.create().apply(parent, subject.style()).getClass();
        dispose(parent);
        var methods = new ArrayList<Method>();
        for (Method m : type.getMethods()) {
            if (!m.getName().startsWith("set") || m.getParameterCount() != 1) continue;
            if (SKIP.contains(m.getName())) continue;
            methods.add(m);
        }
        methods.sort(Comparator.comparing(Method::getName).thenComparing(m -> m.getParameterTypes()[0].getName()));
        return methods;
    }

    /** A value the setter will accept and that differs from a freshly built widget's state. */
    private static Object cannedArgument(Method setter) {
        Class<?> type = setter.getParameterTypes()[0];
        if (type == boolean.class) return Boolean.TRUE;
        if (type == int.class) return 3;
        if (type == char.class) return '*';
        if (type == String.class) return "census";
        if (type == String[].class) return new String[] { "census" };
        return null;
    }

    private String serialize(Widget widget) {
        try {
            return new String(new Serializer().to(widget), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new AssertionError("could not serialize " + widget, e);
        }
    }

    private static Set<String> flags(Widget widget) {
        return ((DartWidget) widget.getImpl()).getValue().changedKeys();
    }

    private static void clearFlags(Widget widget) {
        ((DartWidget) widget.getImpl()).getValue().clearDirty();
    }

    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (Control child : composite.getChildren()) markSent(child);
    }
}

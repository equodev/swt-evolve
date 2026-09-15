package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.Serializer;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the flags against reality: for a change made through the public API, what the setter says
 * changed must be what actually changed.
 *
 * <p>This is the check the whole per-property design rests on, so it is deliberately built from two
 * independent sources. One side is the widget's own record, filled by its setters. The other is
 * derived by {@link DeliveryAudit} from the payloads themselves, knowing nothing about flags. A
 * setter that forgets to flag, or flags something it did not change, disagrees with the payloads and
 * fails here — which no assertion written in terms of the flags could ever do.
 */
@ExtendWith(Mocks.class)
class FlagsMatchChangesTest {

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

    /** A widget, a property to change on it, and the change itself. */
    private record Case(String name, BiFunction<Composite, Integer, Widget> create, int style,
                        Consumer<Widget> change) {
    }

    private static final List<Case> CASES = List.of(
            new Case("Label.text", (p, s) -> new Label(p, s), SWT.NONE,
                    w -> ((Label) w).setText("changed")),
            new Case("Label.toolTipText", (p, s) -> new Label(p, s), SWT.NONE,
                    w -> ((Label) w).setToolTipText("tip")),
            new Case("Button.text", (p, s) -> new Button(p, s), SWT.PUSH,
                    w -> ((Button) w).setText("changed")),
            new Case("Button.selection", (p, s) -> new Button(p, s), SWT.CHECK,
                    w -> ((Button) w).setSelection(true)),
            new Case("Text.text", (p, s) -> new Text(p, s), SWT.NONE,
                    w -> ((Text) w).setText("changed")),
            new Case("Scale.selection", (p, s) -> new Scale(p, s), SWT.NONE,
                    w -> ((Scale) w).setSelection(7)),
            new Case("Spinner.selection", (p, s) -> new Spinner(p, s), SWT.NONE,
                    w -> ((Spinner) w).setSelection(3)),
            new Case("ProgressBar.selection", (p, s) -> new ProgressBar(p, s), SWT.NONE,
                    w -> ((ProgressBar) w).setSelection(42)),
            new Case("Combo.text", (p, s) -> new Combo(p, s), SWT.NONE,
                    w -> ((Combo) w).setText("changed")),
            new Case("Control.enabled", (p, s) -> new Label(p, s), SWT.NONE,
                    w -> ((Label) w).setEnabled(false)),
            new Case("Control.visible", (p, s) -> new Label(p, s), SWT.NONE,
                    w -> ((Label) w).setVisible(false)));

    @TestFactory
    List<DynamicTest> flagsMatchWhatChanged() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Case c : CASES) {
            tests.add(DynamicTest.dynamicTest(c.name(), () -> check(c)));
        }
        return tests;
    }

    private void check(Case c) {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Widget widget = c.create().apply(parent, c.style());
        settle(parent);
        clearFlags(widget);

        // The two states, taken from the widget itself rather than from frames. Comparing states
        // directly means a change that turns out to be a no-op - a value clamped straight back to
        // what it already was - reads as "nothing changed" on both sides instead of as a failure.
        String before = Canon.canon(serialize(widget));
        c.change().accept(widget);
        // Read the flags before serializing again: writing a widget whole is what marks it as no
        // longer outstanding, so the second serialize clears the very thing being measured.
        Set<String> flagged = new LinkedHashSet<>(flags(widget));
        String after = Canon.canon(serialize(widget));

        Set<String> derived = DeliveryAudit.changedKeys(before, after);

        assertThat(flagged)
                .as("what the setter said changed, against what the state shows changed")
                .isEqualTo(derived);
    }

    private String serialize(Widget widget) {
        try {
            return new String(new Serializer().to(widget), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new AssertionError("could not serialize " + widget, e);
        }
    }

    // ---- harness ----

    private static Set<String> flags(Widget widget) {
        return ((DartWidget) widget.getImpl()).getValue().changedKeys();
    }

    private static void clearFlags(Widget widget) {
        ((DartWidget) widget.getImpl()).getValue().clearDirty();
    }

    private static String channelOf(Widget widget) {
        String name = widget.getClass().getSimpleName();
        return name + "/" + widget.hashCode();
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

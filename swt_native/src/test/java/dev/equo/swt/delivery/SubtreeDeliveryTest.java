package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What travels when changes at two depths of one subtree land in the same flush.
 *
 * <p>The shape: a root A holding a composite B and a label C, with a label D under B. A change to A
 * and a change to D, in one event-loop turn.
 *
 * <p>An ancestor's payload contains its descendants only when it is the whole widget. An update
 * naming what changed about the ancestor says nothing about anything under it, so a change deeper
 * down travels on its own channel and the nodes in between are not sent at all - they have not
 * changed, and nothing needs them to carry anything.
 *
 * <p>When the ancestor really is going whole - it is new, or nothing about it is named - the old
 * reasoning holds and the descendant does travel inside it. Then everything between the two is
 * load-bearing: an unchanged composite on that path is still unchanged, and naming it instead of
 * describing it would cut the path and lose the change silently.
 */
@ExtendWith(Mocks.class)
class SubtreeDeliveryTest {

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
    }

    @Test
    @DisplayName("two changes at different depths travel as two updates in one message")
    void rootAndGrandchildInOneFlush() {
        Shell shell = Mocks.shell();
        Composite a = new Composite(shell, SWT.NONE);
        Composite b = new Composite(a, SWT.NONE);
        Label c = new Label(a, SWT.NONE);
        Label d = new Label(b, SWT.NONE);
        c.setText("C");
        d.setText("D");
        settle(a);

        a.setToolTipText("A changed");
        d.setText("D changed");
        FlutterBridge.update();

        assertThat(bridge.comm.wireFrames()).as("one flush, one message").isEqualTo(1L);
        assertThat(recordOn(a))
                .as("each change described as itself")
                .containsEntry("_d", java.util.List.of("toolTipText"));
        assertThat(recordOn(d))
                .containsEntry("_d", java.util.List.of("text"))
                .containsEntry("text", "D changed");
        assertThat(channels())
                .as("B changed nothing and carries nothing, so it is not sent at all - which is "
                        + "the whole saving: an unchanged node between two changed ones costs "
                        + "nothing rather than its entire state")
                .doesNotContain("Composite/" + b.hashCode())
                .as("and neither is the unchanged sibling")
                .doesNotContain("Label/" + c.hashCode());
    }

    @Test
    @DisplayName("a change under an ancestor that is going whole travels as itself, the rest is named")
    void grandchildUnderAWholeAncestor() {
        Shell shell = Mocks.shell();
        Composite a = new Composite(shell, SWT.NONE);
        Composite b = new Composite(a, SWT.NONE);
        Label c = new Label(a, SWT.NONE);
        Label d = new Label(b, SWT.NONE);
        c.setText("C");
        d.setText("D");
        settle(a);

        // Scheduled with nothing named is what a whole send looks like: there is no update to
        // write, so the widget goes in full - and then it really does contain what is beneath it.
        bridge.dirty((DartWidget) a.getImpl());
        d.setText("D changed");
        FlutterBridge.update();

        assertThat(bridge.comm.wireFrames()).isEqualTo(1L);
        assertThat(recordOn(a)).as("A goes whole, naming its children").containsKey("children");
        assertThat(recordOn(d)).as("the deeper change travels as itself").containsEntry("text", "D changed");
        assertThat(channels())
                .as("B and C changed nothing and the client holds them, so they are only named")
                .doesNotContain("Composite/" + b.hashCode(), "Label/" + c.hashCode());
    }

    @Test
    @DisplayName("the same two changes in separate flushes travel as themselves")
    void rootAndGrandchildSeparately() {
        Shell shell = Mocks.shell();
        Composite a = new Composite(shell, SWT.NONE);
        Composite b = new Composite(a, SWT.NONE);
        Label d = new Label(b, SWT.NONE);
        d.setText("D");
        settle(a);

        a.setToolTipText("A changed");
        FlutterBridge.update();
        assertThat(bridge.comm.sent).hasSize(1);
        assertThat(recordOn(a))
                .as("nothing under A is involved, so A describes only what moved")
                .containsEntry("_d", java.util.List.of("toolTipText"));
        assertThat(WireValues.containsText(recordOn(a), "children"))
                .as("nothing under A is involved, so A describes only what moved")
                .isFalse();
        bridge.comm.sent.clear();

        d.setText("D changed");
        FlutterBridge.update();
        assertThat(bridge.comm.sent).hasSize(1);
        assertThat(bridge.comm.sent.get(0).event).isEqualTo("Label/" + d.hashCode());
        assertThat(bridge.comm.sent.get(0).state())
                .as("no dirty ancestor to be folded into, so it goes as itself")
                .containsEntry("_d", java.util.List.of("text"))
                .containsEntry("text", "D changed");
    }

    // ---- harness ----

    /** The record sent for {@code widget}. */
    private java.util.Map<String, Object> recordOn(Widget widget) {
        String channel = widget.getClass().getSimpleName() + "/" + widget.hashCode();
        return bridge.comm.sent.stream().filter(f -> f.event.equals(channel)).map(RecordingComm.Frame::state).findFirst()
                .orElseThrow(() -> new AssertionError("nothing sent on " + channel));
    }

    private java.util.List<String> channels() {
        return bridge.comm.sent.stream().map(f -> f.event).toList();
    }

    private void settle(Widget widget) {
        // Building the tree leaves its host Shell dirty, and this backend has the host Dart-backed
        // too: a widget with a dirty ancestor is expected to travel inside it, so its own frame is
        // suppressed and nothing is ever stamped delivered. Drain that, then stamp the subtree.
        FlutterBridge.update();
        markKnown(widget);
        bridge.dirty((DartWidget) widget.getImpl());
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }

    private void markKnown(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (!(widget instanceof Composite composite)) return;
        Widget[] children = composite.getChildren();
        if (children == null) return;
        for (Widget child : children) markKnown(child);
    }
}

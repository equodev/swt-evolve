package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
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

        assertThat(bridge.comm.sent).as("one flush, one message").hasSize(1);
        String payload = bridge.comm.sent.get(0).json;

        assertThat(payload)
                .as("each change described as itself, on its own channel")
                .contains("\"Composite/" + a.hashCode() + "\"")
                .contains("\"_d\":[\"toolTipText\"]")
                .contains("\"Label/" + d.hashCode() + "\"")
                .contains("\"_d\":[\"text\"]")
                .contains("\"D changed\"");
        assertThat(payload)
                .as("B changed nothing and carries nothing, so it is not sent at all - which is "
                        + "the whole saving: an unchanged node between two changed ones costs "
                        + "nothing rather than its entire state")
                .doesNotContain("\"id\":" + b.hashCode());
        assertThat(payload)
                .as("and neither is the unchanged sibling")
                .doesNotContain("\"id\":" + c.hashCode());
    }

    @Test
    @DisplayName("a change under an ancestor that is going whole still travels inside it")
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

        assertThat(bridge.comm.sent).hasSize(1);
        String payload = bridge.comm.sent.get(0).json;
        assertThat(payload)
                .as("the deeper change has no other way to arrive")
                .contains("\"D changed\"");
        assertThat(sliceFor(payload, b))
                .as("B changed nothing and is the path D's change travels down")
                .doesNotContain("\"_r\"");
        assertThat(sliceFor(payload, c))
                .as("C changed nothing and nothing travels through it, so it is named")
                .contains("\"_r\":1");
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
        assertThat(bridge.comm.sent.get(0).json)
                .as("nothing under A is involved, so A describes only what moved")
                .contains("\"_d\":[\"toolTipText\"]")
                .doesNotContain("children");
        bridge.comm.sent.clear();

        d.setText("D changed");
        FlutterBridge.update();
        assertThat(bridge.comm.sent).hasSize(1);
        assertThat(bridge.comm.sent.get(0).event).isEqualTo("Label/" + d.hashCode());
        assertThat(bridge.comm.sent.get(0).json)
                .as("no dirty ancestor to be folded into, so it goes as itself")
                .contains("\"_d\":[\"text\"]")
                .contains("\"D changed\"");
    }

    // ---- harness ----

    /** The part of a payload describing one widget: from its id up to the next one's. */
    private String sliceFor(String payload, Widget widget) {
        String marker = "\"id\":" + widget.hashCode();
        int start = payload.indexOf(marker);
        assertThat(start).as("%s is not in the payload at all", widget).isNotNegative();
        int next = payload.indexOf("\"id\":", start + marker.length());
        return payload.substring(start, next < 0 ? payload.length() : next);
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

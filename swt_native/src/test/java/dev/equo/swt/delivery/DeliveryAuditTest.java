package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the audit seam works before anything depends on it: that the observer sees real frames,
 * that it is inert when nobody installs one, and that the changed set it derives by comparing
 * states matches what was actually changed.
 *
 * <p>The last test records how today's full-send delivery behaves, which is the thing the audit
 * exists to measure a change against.
 */
@ExtendWith(Mocks.class)
class DeliveryAuditTest {

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

    @Test
    @DisplayName("the observer sees the frames the widget tree sends")
    void observesFrames() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("before");
        settle(parent);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            label.setText("after");
            FlutterBridge.update();

            assertThat(audit.framesOn("Label/")).hasSize(1);
            assertThat(audit.frames().get(0).json()).contains("\"text\":\"after\"");
        }
    }

    @Test
    @DisplayName("no observer, no observation - the hook is inert while unset")
    void inertWhenUnset() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        settle(parent);

        DeliveryAudit audit = new DeliveryAudit(); // built, deliberately not installed
        label.setText("after");
        FlutterBridge.update();

        assertThat(audit.frames())
                .as("an uninstalled observer must see nothing, so production carries no cost")
                .isEmpty();
        assertThat(bridge.comm.sent)
                .as("and the frame still went out")
                .isNotEmpty();
    }

    @Test
    @DisplayName("the derived changed set names the property that actually changed")
    void derivesTheChangedProperty() {
        // Deriving what changed by comparing two payloads only works while a payload is a whole
        // state. Once updates carry only what the sender chose to name, an observer of the wire has
        // nothing independent left to compare against - the frame *is* the claim. Verifying
        // completeness under partial delivery needs the full state sent alongside for comparison,
        // which is a separate piece of work; until then this measures the mode it can measure.
        org.junit.jupiter.api.Assumptions.assumeFalse(Serializer.diffEnabled,
                "derivation from consecutive payloads requires whole payloads");

        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("before");
        settle(parent);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // First observed frame establishes the snapshot; the second is the one under test.
            label.setText("first");
            FlutterBridge.update();
            label.setText("second");
            FlutterBridge.update();

            String channel = "Label/" + label.hashCode();
            assertThat(audit.lastChangedKeysOn(channel))
                    .as("derived by comparing the two payloads, with no help from the dirty bits - "
                            + "which is what makes it able to catch a dirty bit that was never set")
                    .isEqualTo(Set.of("text"));
        }
    }

    @Test
    @DisplayName("re-setting the same value sends nothing")
    void sameValueSendsNothing() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("stable");
        settle(parent);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            label.setText("changed");
            FlutterBridge.update();
            label.setText("changed"); // same value again
            FlutterBridge.update();

            assertThat(audit.framesOn("Label/"))
                    .as("a setter that changes nothing must put nothing on the wire. Asserted "
                            + "because it is a property to preserve, not to achieve: making "
                            + "updates incremental must not turn a no-op setter into a frame "
                            + "carrying an empty change set")
                    .hasSize(1);
        }
    }

    @Test
    @DisplayName("every frame after the first carries a real change")
    void framesCarryChanges() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("one");
        settle(parent);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            for (String text : new String[]{"two", "three", "four"}) {
                label.setText(text);
                FlutterBridge.update();
            }

            // The generalised form of the test above, and the shape the audit will take on real
            // workloads: rather than naming a case, walk what was sent and ask of each frame
            // whether it needed sending.
            for (int i = 1; i < audit.frames().size(); i++) {
                assertThat(audit.changedKeysOf(i))
                        .as("frame %d on %s repeated the previous state", i, audit.frames().get(i).channel())
                        .isNotEmpty();
            }
            assertThat(audit.frames()).hasSize(3);
        }
    }

    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (var child : composite.getChildren()) markSent(child);
    }
}

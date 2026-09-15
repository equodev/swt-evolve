package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartLabel;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.VLabel;
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
 * The record of what changed since a widget was last sent — the thing an update will be built from
 * once updates stop carrying everything.
 *
 * <p>Nothing writes to it yet; setters start flagging in the next step. What is pinned here is the
 * contract those setters will rely on, and the property that makes it safe to add now: the record
 * is bookkeeping, and bookkeeping must not reach the wire.
 */
@ExtendWith(Mocks.class)
class ChangeTrackingTest {

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
    @DisplayName("flagging a property records it and schedules the widget")
    void markDirtyRecordsAndSchedules() {
        Label label = newLabel();
        VLabel value = value(label);

        value.markDirty("text");

        assertThat(value.changedKeys()).containsExactly("text");
        assertThat(value.anyDirty()).isTrue();

        FlutterBridge.update();
        assertThat(bridge.comm.sent)
                .as("a property flagged on a widget nobody scheduled would never be sent; flagging "
                        + "and scheduling have to happen together or not at all")
                .anyMatch(f -> f.event.equals("Label/" + label.hashCode()));
    }

    @Test
    @DisplayName("the record keeps every property that changed, in order")
    void recordsEveryChange() {
        VLabel value = value(newLabel());

        value.markDirty("text");
        value.markDirty("toolTipText");
        value.markDirty("text");

        assertThat(value.changedKeys())
                .as("a property changed twice between sends is still one property to send")
                .containsExactly("text", "toolTipText");
    }

    @Test
    @DisplayName("clearing leaves nothing behind")
    void clearingEmptiesTheRecord() {
        VLabel value = value(newLabel());
        value.markDirty("text");

        value.clearDirty();

        assertThat(value.changedKeys()).isEmpty();
        assertThat(value.anyDirty()).isFalse();
    }

    @Test
    @DisplayName("the record never reaches the wire")
    void recordIsNotSerialized() {
        Label label = newLabel();
        label.setText("visible");
        value(label).markDirty("text");
        FlutterBridge.update();

        assertThat(bridge.comm.sent).isNotEmpty();
        String json = bridge.comm.sent.get(bridge.comm.sent.size() - 1).json;
        assertThat(json)
                .as("this is bookkeeping about the wire, not content for it - and it is named like a "
                        + "property, so a codec that bound it would ship it to every client")
                .doesNotContain("changed")
                .doesNotContain("anyDirty");
        assertThat(json).contains("\"text\":\"visible\"");
    }

    private Label newLabel() {
        Shell shell = Mocks.swtShell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        settle(parent);
        // What is under test is what markDirty records, not what building a widget happens to leave
        // flagged - and that differs by platform, since each one's construction runs its own
        // setters. Starting from an empty record is what makes the assertions mean one thing.
        value(label).clearDirty();
        return label;
    }

    private static VLabel value(Label label) {
        return ((DartLabel) label.getImpl()).getValue();
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

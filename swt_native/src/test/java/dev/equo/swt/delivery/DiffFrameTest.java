package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartLabel;
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

import java.util.List;
import java.util.Map;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a widget's change record looks like on the wire, and what it costs.
 *
 * <p>The tape is driven directly rather than through a flushed send, so each record can be read on
 * its own: what it carries, what it is relative to, and whether there is one at all.
 */
@ExtendWith(Mocks.class)
class DiffFrameTest {

    /**
     * The client every record in this test is written for. Delivery is recorded against a client:
     * what one client holds says nothing about what another does.
     */
    private static final int CLIENT = 1;

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
    @DisplayName("a change record carries the changed property, and says what it is relative to")
    void diffCarriesOnlyTheChange() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        long sentAt = sendWhole(label);

        label.setText("after");
        Map<String, Object> diff = WireFrames.record(impl, CLIENT);

        assertThatWire(diff).node("_d").isEqualTo(List.of("text"));
        assertThatWire(diff).containsEntry("text", "after");
        assertThat(((Number) diff.get("_b")).longValue())
                .as("the state it was computed from, so the far side can check it fits what it holds")
                .isEqualTo(sentAt);
        for (String other : List.of("bounds", "background", "orientation")) {
            assertThat(WireValues.mentions(diff, other))
                    .as("nothing else the widget happens to hold - that is the entire point: " + other)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("a change record is a fraction of the whole state")
    void diffIsSmallerThanTheWhole() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("after");
        int diff = WireFrames.record(impl, CLIENT).size();
        Serializer.forgetDelivery(impl.getValue());
        int whole = WireFrames.record(impl, CLIENT).size();

        assertThat(diff)
                .as("one property of a Label, against the %d entries of all of them", whole)
                .isLessThan(whole);
    }

    @Test
    @DisplayName("the next change is measured from the one before it, not from the first")
    void baseAdvancesWithEachUpdate() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("first");
        Map<String, Object> firstDiff = WireFrames.record(impl, CLIENT);
        long firstSeq = ((Number) firstDiff.get("_s")).longValue();

        label.setText("second");
        Map<String, Object> secondDiff = WireFrames.record(impl, CLIENT);

        assertThat(((Number) secondDiff.get("_b")).longValue())
                .as("a chain: each change fits the state the one before it produced")
                .isEqualTo(firstSeq);
    }

    @Test
    @DisplayName("a change sent leaves nothing outstanding")
    void deliveringClearsTheRecord() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("after");
        assertThat(impl.getValue().anyDirty()).isTrue();
        WireFrames.record(impl, CLIENT);
        assertThat(impl.getValue().anyDirty())
                .as("what has been sent is no longer outstanding; leaving it would put the same "
                        + "property in every later change forever")
                .isFalse();
    }

    @Test
    @DisplayName("a widget never sent whole cannot be described by a change")
    void unsentWidgetCannotDiff() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();

        label.setText("after");

        assertThat(Serializer.canDiff(impl, CLIENT))
                .as("there is no state on the far side for a change to be relative to")
                .isFalse();
    }

    @Test
    @DisplayName("a widget with nothing outstanding has no change to send")
    void unchangedWidgetCannotDiff() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        assertThat(Serializer.canDiff(impl, CLIENT)).isFalse();
    }

    @Test
    @DisplayName("a change back to what a change sent, after a whole description, is sent")
    void changeBackAfterAWholeDescriptionIsSent() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("A");
        WireFrames.record(impl, CLIENT);
        // Sent whole, so the client now holds "B".
        label.setText("B");
        sendWhole(label);

        label.setText("A");
        Map<String, Object> diff = WireFrames.record(impl, CLIENT);

        assertThat(diff)
                .as("the client holds B; skipping A as a repeat of the last change left a resized "
                        + "tree drawn at the size the whole description carried")
                .isNotNull();
        assertThatWire(diff).containsEntry("text", "A");
    }

    @Test
    @DisplayName("a property changed back to what the client holds is not sent at all")
    void aChangeThatIsNotADifferenceIsDropped() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);
        label.setText("settled");
        assertThat(WireFrames.record(impl, CLIENT)).isNotNull();

        // As a layout pass does to bounds: dirty, but equal to what was sent by the flush.
        label.setText("mid");
        label.setText("settled");

        assertThat(WireFrames.record(impl, CLIENT))
                .as("nothing the client does not hold, so no record")
                .isNull();
        assertThat(impl.getValue().anyDirty())
                .as("and it stops being dirty, or it is reconsidered on every later flush")
                .isFalse();
    }

    @Test
    @DisplayName("a real change is still sent")
    void aRealDifferenceStillTravels() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("mid");
        label.setText("after");

        Map<String, Object> diff = WireFrames.record(impl, CLIENT);
        assertThatWire(diff).node("_d").isEqualTo(List.of("text"));
        assertThatWire(diff).containsEntry("text", "after");
    }

    @Test
    @DisplayName("a dropped change leaves the write stamp where it was")
    void droppingAnUpdateDoesNotMoveTheBase() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);
        label.setText("settled");
        WireFrames.record(impl, CLIENT);
        long sentAt = impl.getValue().sentSeq(CLIENT);

        label.setText("mid");
        label.setText("settled");
        assertThat(WireFrames.record(impl, CLIENT)).isNull();

        label.setText("after");
        assertThat(((Number) WireFrames.record(impl, CLIENT).get("_b")).longValue())
                .as("the next real change is still measured from the last record the client received")
                .isEqualTo(sentAt);
    }

    // ---- harness ----

    /** Sends the widget whole to the client and returns the stamp that state carries. */
    private long sendWhole(Label label) {
        DartLabel impl = (DartLabel) label.getImpl();
        Serializer.forgetDelivery(impl.getValue());
        WireFrames.record(impl, CLIENT);
        return impl.getValue().sentSeq(CLIENT);
    }

    private Label newLabel() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("before");
        settle(parent);
        return label;
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

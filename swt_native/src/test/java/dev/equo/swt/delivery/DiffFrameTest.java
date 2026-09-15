package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
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

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a partial update looks like on the wire, and what it costs.
 *
 * <p>The writer is exercised directly rather than through a flushed send, because whether a given
 * flush takes the partial path depends on a system property read once at class-load - which a test
 * cannot flip. What matters here is the shape of the bytes and the size of them; which path a flush
 * chooses is decided by {@link Serializer#canDiff} and asserted separately.
 */
@ExtendWith(Mocks.class)
class DiffFrameTest {

    /**
     * The client every frame in this test is written for. Delivery is recorded against a client, so
     * a test that sends has to say which one; a walk that names nobody credits nobody, which is what
     * keeps a serialize done only to read a state from telling the next update it can be relative to
     * something that was never sent.
     */
    private static final int CLIENT = 1;

    @org.junit.jupiter.api.BeforeEach
    void addressFramesToTheClient() {
        Serializer.enterConnection(CLIENT);
    }

    @org.junit.jupiter.api.AfterEach
    void stopAddressingFrames() {
        Serializer.exitConnection();
    }

    private RecordingBridge bridge;
    private final Serializer serializer = new Serializer();

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
    @DisplayName("a partial update carries the changed property, and says what it is relative to")
    void diffCarriesOnlyTheChange() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        long sentAt = sendWhole(label);

        label.setText("after");
        String diff = new String(serializer.toDiff(impl), StandardCharsets.UTF_8);

        assertThat(diff).contains("\"_d\":[\"text\"]");
        assertThat(diff).contains("\"text\":\"after\"");
        assertThat(diff)
                .as("the state it was computed from, so the far side can check it fits what it holds")
                .contains("\"_b\":" + sentAt);
        assertThat(diff)
                .as("nothing else the widget happens to hold - that is the entire point")
                .doesNotContain("\"bounds\"")
                .doesNotContain("\"background\"")
                .doesNotContain("\"orientation\"");
    }

    @Test
    @DisplayName("a partial update is a fraction of the whole state")
    void diffIsSmallerThanTheWhole() throws Exception {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("after");
        int diffBytes = serializer.toDiff(impl).length;
        int wholeBytes = serializer.to(label).length;

        // Half rather than a third, and the ratio is the weaker half of what is being checked. A
        // Label carries little state now that the properties nothing reads are gone, so most of a
        // one-property update is the identity and the protocol keys, and the ratio narrows as the
        // whole state shrinks - it says less the better delivery gets. What the update must not
        // contain is asserted above, where it does not move with the size of the widget.
        assertThat(diffBytes)
                .as("one property of a Label, against the %d bytes of all of them", wholeBytes)
                .isLessThan(wholeBytes / 2);
    }

    @Test
    @DisplayName("the next update is measured from the one before it, not from the first")
    void baseAdvancesWithEachUpdate() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("first");
        String firstDiff = new String(serializer.toDiff(impl), StandardCharsets.UTF_8);
        Serializer.markDelivered();
        long firstSeq = longAfter(firstDiff, "\"_s\":");

        label.setText("second");
        String secondDiff = new String(serializer.toDiff(impl), StandardCharsets.UTF_8);

        assertThat(secondDiff)
                .as("a chain: each update fits the state the one before it produced")
                .contains("\"_b\":" + firstSeq);
    }

    @Test
    @DisplayName("delivering clears what was outstanding")
    void deliveringClearsTheRecord() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        label.setText("after");
        assertThat(impl.getValue().anyDirty()).isTrue();
        serializer.toDiff(impl);
        assertThat(impl.getValue().anyDirty())
                .as("writing is not delivering: until the bytes are on their way the widget still "
                        + "owes the far side that property")
                .isTrue();

        Serializer.markDelivered();
        assertThat(impl.getValue().anyDirty())
                .as("what has been sent is no longer outstanding; leaving it would put the same "
                        + "property in every later update forever")
                .isFalse();
    }

    @Test
    @DisplayName("a widget never sent whole cannot be described by a change")
    void unsentWidgetCannotDiff() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();

        label.setText("after");

        assertThat(Serializer.canDiff(impl))
                .as("there is no state on the far side for a change to be relative to")
                .isFalse();
    }

    @Test
    @DisplayName("a widget with nothing outstanding has no change to send")
    void unchangedWidgetCannotDiff() {
        Label label = newLabel();
        DartLabel impl = (DartLabel) label.getImpl();
        sendWhole(label);

        assertThat(Serializer.canDiff(impl)).isFalse();
    }

    // ---- harness ----

    /** Sends the widget whole and returns the stamp that state carries. */
    private long sendWhole(Label label) {
        DartLabel impl = (DartLabel) label.getImpl();
        try {
            serializer.to(label);
            Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        return impl.getValue().sentSeq(CLIENT);
    }

    private static long longAfter(String json, String key) {
        int at = json.indexOf(key) + key.length();
        int end = at;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        return Long.parseLong(json.substring(at, end));
    }

    private Label newLabel() {
        Shell shell = Mocks.swtShell();
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

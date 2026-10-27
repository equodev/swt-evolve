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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Whether the updates that were sent add up to the state they were describing.
 *
 * <p>Once an update carries only what the sender named, an observer of the wire cannot check that
 * claim against the wire: the frame <em>is</em> the claim. So this rebuilds what a client would be
 * holding from the frames alone, and holds it against Java's own state — which the frames had no
 * part in producing. A property that changed and was never named is missing from one side and
 * present in the other, which is exactly the failure the flags cannot report about themselves.
 *
 * <p>Reading Java's state has to be free of consequences for this to work at all: serializing a
 * widget notes what it wrote but does not mark it delivered, so looking does not change what the
 * next update will be relative to.
 */
@ExtendWith(Mocks.class)
class ConvergenceAuditTest {

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
        FlutterBridge.setSendObserver(null);
    }

    @Test
    @DisplayName("a sequence of updates adds up to the state it describes")
    void updatesAddUp() {
        Label label = newLabel();
        String channel = "Label/" + label.hashCode();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            deliverWhole(label);
            for (String text : new String[]{"one", "two", "three"}) {
                label.setText(text);
                FlutterBridge.update();
            }

            audit.assertConverged(channel, truth(label));
        }
    }

    @Test
    @DisplayName("a property that changed without being named is caught")
    void unnamedChangeIsCaught() {
        // Only meaningful while an update carries what it names. Sending every widget whole carries
        // the change whether or not anything recorded it, so there is nothing here to miss - which
        // is exactly why that mode is the way out if partial updates ever have to be turned off.
        assumeTrue(Serializer.diffEnabled, "updates carry only what they name");
        Label label = newLabel();
        String channel = "Label/" + label.hashCode();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            deliverWhole(label);

            // A change made the way a mutation site that forgets to flag would make it: the text
            // moves, nothing records it, and a different property carries the update. The flags are
            // consistent with themselves - they simply never mention the text.
            label.setText("changed behind the audit's back");
            ((DartLabel) label.getImpl()).getValue().clearDirty();
            ((DartLabel) label.getImpl()).getValue().markDirty("toolTipText");
            FlutterBridge.update();

            assertThatThrownBy(() -> audit.assertConverged(channel, truth(label)))
                    .as("the reconstruction still has the old text while Java has the new one, and "
                            + "no inspection of the dirty bits could have said so")
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("does not add up")
                    .hasMessageContaining("text");
        }
    }

    @Test
    @DisplayName("a widget with nothing named falls back to sending itself whole")
    void nothingNamedSendsWhole() {
        Label label = newLabel();
        String channel = "Label/" + label.hashCode();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            deliverWhole(label);

            // Scheduled with an empty record - which is what a forgotten flag looks like when the
            // widget is still scheduled by something else. There is nothing to describe, so the
            // whole widget goes instead of an update that would say nothing.
            label.setText("recovered anyway");
            ((DartLabel) label.getImpl()).getValue().clearDirty();
            FlutterBridge.update();

            audit.assertConverged(channel, truth(label));
        }
    }

    @Test
    @DisplayName("clearing a property back to its default is carried, not dropped")
    void clearedPropertyIsCarried() {
        Label label = newLabel();
        String channel = "Label/" + label.hashCode();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            label.setToolTipText("a tip");
            deliverWhole(label);

            label.setToolTipText(null);
            FlutterBridge.update();

            audit.assertConverged(channel, truth(label));
        }
    }

    // ---- harness ----

    /** Java's own state for the widget, read without marking anything delivered. */
    private String truth(Widget widget) {
        try {
            String json = new String(serializer.to(widget), StandardCharsets.UTF_8);
            Serializer.discardWritten();
            return json;
        } catch (java.io.IOException e) {
            throw new AssertionError("could not read the state of " + widget, e);
        }
    }

    /** Puts the widget on the wire whole, so later updates have something to be relative to. */
    private void deliverWhole(Label label) {
        bridge.dirty((DartLabel) label.getImpl());
        FlutterBridge.update();
    }

    private Label newLabel() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        label.setText("before");
        FlutterBridge.update();
        markSent(parent);
        bridge.comm.sent.clear();
        return label;
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (var child : composite.getChildren()) markSent(child);
    }
}

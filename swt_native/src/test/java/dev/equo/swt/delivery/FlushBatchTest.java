package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * One flush is one message.
 *
 * <p>Everything a flush sends belongs to the same moment — a layout pass over a composite touches
 * every child of it — and sending them one at a time costs the client a socket frame, a decode, a
 * dispatch and a rebuild for each. A flush is one binary frame of records, one per widget.
 */
@ExtendWith(Mocks.class)
class FlushBatchTest {

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
    @DisplayName("a flush with several things to say sends one message")
    void manyFramesTravelTogether() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        List<Label> rows = new ArrayList<>();
        for (int i = 0; i < 8; i++) rows.add(new Label(parent, SWT.NONE));
        settle(parent);

        for (int i = 0; i < rows.size(); i++) rows.get(i).setText("row " + i);
        FlutterBridge.update();

        assertThat(bridge.comm.wireFrames())
                .as("eight changes in one moment, one message")
                .isEqualTo(1L);
        for (int i = 0; i < rows.size(); i++) {
            String channel = "Label/" + rows.get(i).hashCode();
            String text = "row " + i;
            assertThat(bridge.comm.sent)
                    .as("every change is in it, as the record of the widget it belongs to")
                    .anyMatch(f -> f.event.equals(channel)
                            && !WireValues.find(f.value, m -> text.equals(m.get("text"))).isEmpty());
        }
    }

    @Test
    @DisplayName("a flush with one thing to say sends one record")
    void oneFrameTravelsAlone() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        Label only = new Label(parent, SWT.NONE);
        settle(parent);

        only.setText("alone");
        FlutterBridge.update();

        assertThat(bridge.comm.wireFrames()).isEqualTo(1L);
        assertThat(bridge.comm.sent).hasSize(1);
        assertThat(bridge.comm.sent.get(0).event).isEqualTo("Label/" + only.hashCode());
    }

    @Test
    @DisplayName("nothing to say sends nothing")
    void anEmptyFlushSendsNothing() {
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        new Label(parent, SWT.NONE);
        settle(parent);

        FlutterBridge.update();

        assertThat(bridge.comm.sent).isEmpty();
    }

    // ---- harness ----

    /**
     * Gets the tree onto the wire and clears the record, so what a test measures is only what its
     * own change produced. The tree is declared known first: a widget the client has never seen is
     * not sent on its own channel, it is expected to arrive inside its parent.
     */
    private void settle(Widget widget) {
        markKnown(widget);
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }

    private void markKnown(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite)
            for (Widget child : composite.getChildren()) markKnown(child);
    }
}

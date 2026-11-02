package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ControlHelper;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A full repaint identical to the last one is dropped, but a differing paint or one the client
 * asked for must still be sent.
 */
@Tag("native-unit")
class GCRepeatedRepaintNativeTest {

    private RecordingBridge bridge;
    private Canvas canvas;
    private Display display;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** Simulates a SWT.Paint cycle: a Paint listener cannot run in this harness. */
    private void repaint(int fillWidth) {
        ControlHelper.inPaintDepth++;
        try {
            GC gc = new GC(canvas);
            if (fillWidth > 0) gc.fillRectangle(0, 0, fillWidth, 100);
            gc.dispose();
        } finally {
            ControlHelper.inPaintDepth--;
        }
    }

    /** The entries sent since the last drain that carry anything. */
    private List<GcFrames.Entry> drain() {
        List<GcFrames.Entry> wire = BatchEntries.of(bridge.comm.sent).stream().filter(e -> e.value() != null).toList();
        bridge.comm.sent.clear();
        return wire;
    }

    private static boolean fills(List<GcFrames.Entry> wire) {
        return wire.stream().anyMatch(e -> e.channel().contains("fillRectangle"));
    }

    @Test
    @DisplayName("a repaint that draws what the last one drew is not sent")
    void repeatedPaintIsDropped() {
        repaint(200);
        assertThat(fills(drain())).as("the first paint is what puts the fill on the control")
                .isTrue();

        repaint(200);

        assertThat(drain()).as("the control already shows this fill").isEmpty();
    }

    @Test
    @DisplayName("a repaint that draws something else is sent")
    void changedPaintIsSent() {
        repaint(200);
        drain();

        repaint(120);

        assertThat(fills(drain())).isTrue();
    }

    @Test
    @DisplayName("the first paint that draws nothing is sent, and the next is not")
    void firstBlankClearsAndTheRestAreDropped() {
        repaint(200);
        drain();

        repaint(0);
        assertThat(drain()).as("a full repaint drawing nothing is what clears the control")
                .isNotEmpty();

        repaint(0);
        assertThat(drain()).as("the control is already clear").isEmpty();
    }

    @Test
    @DisplayName("a repaint whose GC description changed is sent even when its ops repeat")
    void restatedDescriptionIsSent() {
        repaint(200);
        drain();

        ControlHelper.inPaintDepth++;
        try {
            GC gc = new GC(canvas);
            // Not a system colour: the test Display answers every one with the same value.
            gc.setBackground(new Color(display, 7, 8, 9));
            gc.fillRectangle(0, 0, 200, 100);
            gc.dispose();
        } finally {
            ControlHelper.inPaintDepth--;
        }

        assertThat(fills(drain()))
                .as("the colour the next clear will clear to is part of what the client holds")
                .isTrue();
    }

    @Test
    @DisplayName("a paint the client asked for is sent even when it repeats")
    void paintTheClientAskedForIsSent() {
        repaint(200);
        drain();

        FlutterBridge.forgetWhatIsShown(canvas);
        repaint(200);

        assertThat(fills(drain())).as("the client asked because what it shows is not what was last sent")
                .isTrue();
    }
}

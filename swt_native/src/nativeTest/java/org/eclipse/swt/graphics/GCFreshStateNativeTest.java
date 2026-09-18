package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every GC on a Canvas shares one channel, so Flutter keeps the last state it was sent. A new GC
 * starts from its own defaults, and has to say so before its first op, or that op is drawn with
 * whatever the previous paint left behind.
 */
@Tag("native-unit")
class GCFreshStateNativeTest {

    private RecordingBridge bridge;
    private Canvas canvas;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Display display = DartMocks.dartDisplay();
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

    /** A paint that leaves every piece of GC state away from its default. */
    private void paintWithEverythingChanged() {
        GC gc = new GC(canvas);
        gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        gc.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_GREEN));
        gc.setAlpha(40);
        gc.setClipping(0, 0, 10, 10);
        Transform transform = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(transform);
        transform.dispose();
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();
        bridge.comm.sent.clear();
    }

    /** What the next paint sends ahead of its first op, or null when it sends no state. */
    private String stateBeforeFirstOp() {
        String wire = bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
        int op = wire.indexOf("GC/" + canvas.hashCode() + "/fillRectangleintintintint");
        assertThat(op).as("sanity: the paint's fill reached the wire").isNotNegative();
        String before = wire.substring(0, op);
        return before.contains("\"alpha\"") ? before : null;
    }

    @Test
    @DisplayName("a GC that sets nothing still resets what the previous paint left")
    void untouchedGcSendsItsDefaults() {
        paintWithEverythingChanged();

        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();

        String state = stateBeforeFirstOp();
        assertThat(state).as("the fill must not be drawn with the previous paint's state").isNotNull();
        // The canvas's own background: a control GC starts from the control it was opened
        // on, so the literal that used to stand here only matched while the GC ignored it.
        Color canvasBg = canvas.getBackground();
        assertThat(state).contains(String.format("\"background\":{\"alpha\":255,\"blue\":%d,\"green\":%d,\"red\":%d}",
                canvasBg.getBlue(), canvasBg.getGreen(), canvasBg.getRed()));
        assertThat(state).contains("\"foreground\":{\"alpha\":255,\"blue\":0,\"green\":0,\"red\":0}");
        assertThat(state).contains("\"alpha\":255");
        assertThat(state).doesNotContain("\"clipping\":").doesNotContain("\"transform\"");
    }

    @Test
    @DisplayName("setting a colour equal to the GC's default still reaches Flutter")
    void colourEqualToTheDefaultIsSent() {
        paintWithEverythingChanged();

        GC gc = new GC(canvas);
        gc.setBackground(new Color(255, 255, 255));
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();

        String state = stateBeforeFirstOp();
        assertThat(state).as("a clear in the default colour must not reuse the previous background").isNotNull();
        assertThat(state).contains("\"background\":{\"alpha\":255,\"blue\":255,\"green\":255,\"red\":255}");
    }
}

package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
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

import java.util.ArrayList;
import java.util.List;

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
    private List<Object> stateBeforeFirstOp() {
        List<GcFrames.Entry> entries = BatchEntries.of(bridge.comm.sent);
        String fill = "GC/" + canvas.hashCode() + "/fillRectangleintintintint";
        int op = -1;
        for (int i = 0; i < entries.size() && op < 0; i++) if (entries.get(i).channel().contains(fill)) op = i;
        assertThat(op).as("sanity: the paint's fill reached the wire").isNotNegative();
        List<Object> before = new ArrayList<>();
        for (GcFrames.Entry entry : entries.subList(0, op)) before.add(entry.value());
        boolean state = !WireValues.find(before, m -> Boolean.TRUE.equals(m.get("_whole"))
                || (m.get("_d") instanceof Number && ((Number) m.get("_d")).intValue() == 0)).isEmpty();
        return state ? before : null;
    }

    /** Whether some record in {@code state} has {@code key} equal to {@code value}. */
    private static boolean carries(List<Object> state, String key, Object value) {
        return !WireValues.find(state, m -> m.containsKey(key) && WireValues.same(m.get(key), value)).isEmpty();
    }

    @Test
    @DisplayName("a GC that sets nothing still resets what the previous paint left")
    void untouchedGcSendsItsDefaults() {
        paintWithEverythingChanged();

        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();

        List<Object> state = stateBeforeFirstOp();
        assertThat(state).as("the fill must not be drawn with the previous paint's state").isNotNull();
        // A control GC starts from the control's background, and from the foreground the
        // application set on it -- none here, so black, whatever the colour scheme's text is.
        Color canvasBg = canvas.getBackground();
        assertThat(carries(state, "background", String.format("#ff%02x%02x%02x",
                canvasBg.getRed(), canvasBg.getGreen(), canvasBg.getBlue()))).isTrue();
        assertThat(carries(state, "foreground", "#ff000000")).isTrue();
        // A whole description replaces what the client holds, so an alpha left out is the default.
        assertThat(carries(state, "alpha", 40)).isFalse();
        assertThat(WireValues.mentions(state, "clipping")).isFalse();
        assertThat(WireValues.mentions(state, "transform")).isFalse();
    }

    @Test
    @DisplayName("a GC starts from the foreground the application set on its control")
    void gcStartsFromTheControlsOwnForeground() {
        canvas.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_RED));
        GC gc = new GC(canvas);
        assertThat(gc.getForeground().getRGB()).isEqualTo(canvas.getForeground().getRGB());
        gc.dispose();
    }

    @Test
    @DisplayName("setting a colour equal to the GC's default still reaches Flutter")
    void colourEqualToTheDefaultIsSent() {
        paintWithEverythingChanged();

        GC gc = new GC(canvas);
        gc.setBackground(new Color(canvas.getDisplay(), 255, 255, 255));
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();

        List<Object> state = stateBeforeFirstOp();
        assertThat(state).as("a clear in the default colour must not reuse the previous background").isNotNull();
        assertThat(carries(state, "background", "#ffffffff")).isTrue();
    }
}

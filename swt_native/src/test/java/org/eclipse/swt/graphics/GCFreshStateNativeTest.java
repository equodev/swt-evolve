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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every GC on a Canvas shares one channel, so Flutter keeps the last state it was sent. A new GC
 * starts from its own defaults, and has to say what of them differs from that state before its
 * first op, or that op is drawn with whatever the previous paint left behind.
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

    /** Where the paint after {@link #paintWithEverythingChanged} starts in what was sent. */
    private int nextPaint;

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
        nextPaint = bridge.comm.sent.size();
    }

    /**
     * The state the client's channel holds when the next paint's first op is drawn: a whole record
     * replaces it, a change merges into it. Null when that paint sends no state ahead of the op.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> stateAtFirstOp() {
        String channel = "GC/" + canvas.hashCode();
        String fill = channel + "/fillRectangleintintintint";
        Map<String, Object> held = new HashMap<>();
        for (GcFrames.Entry entry : BatchEntries.of(bridge.comm.sent.subList(0, nextPaint))) {
            if (channel.equals(entry.channel())) apply(held, (Map<String, Object>) entry.value());
        }
        boolean stated = false;
        boolean drawn = false;
        for (GcFrames.Entry entry : BatchEntries.of(bridge.comm.sent.subList(nextPaint, bridge.comm.sent.size()))) {
            if (entry.channel().contains(fill)) {
                drawn = true;
                break;
            }
            if (channel.equals(entry.channel())) {
                apply(held, (Map<String, Object>) entry.value());
                stated = true;
            }
        }
        assertThat(drawn).as("sanity: the paint's fill reached the wire").isTrue();
        return stated ? held : null;
    }

    private static void apply(Map<String, Object> held, Map<String, Object> record) {
        if (Boolean.TRUE.equals(record.get("_whole"))) held.clear();
        held.putAll(record);
        held.remove("_whole");
        held.remove("_d");
    }

    @Test
    @DisplayName("a GC that sets nothing still resets what the previous paint left")
    void untouchedGcSendsItsDefaults() {
        paintWithEverythingChanged();

        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 200, 100);
        gc.dispose();

        Map<String, Object> state = stateAtFirstOp();
        assertThat(state).as("the fill must not be drawn with the previous paint's state").isNotNull();
        // A control GC starts from the control's background, and from the foreground the
        // application set on it -- none here, so black, whatever the colour scheme's text is.
        Color canvasBg = canvas.getBackground();
        assertThat(state).containsEntry("background", String.format("#ff%02x%02x%02x",
                canvasBg.getRed(), canvasBg.getGreen(), canvasBg.getBlue()));
        assertThat(state).containsEntry("foreground", "#ff000000");
        assertThat(WireValues.same(state.get("alpha"), 40)).isFalse();
        assertThat(state.get("clipping")).isNull();
        assertThat(state.get("transform")).isNull();
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

        Map<String, Object> state = stateAtFirstOp();
        assertThat(state).as("a clear in the default colour must not reuse the previous background").isNotNull();
        assertThat(state).containsEntry("background", "#ffffffff");
    }
}

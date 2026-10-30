package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingComm;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A GC's state goes whole the first time and as what changed afterwards, as long as the channel it
 * shares with every other GC of the control still holds what this GC last sent.
 */
@Tag("native-unit")
class GCStateDiffNativeTest {

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
        bridge.comm.sent.clear();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** The GC state frames sent on the canvas's channel, batched or not, in order. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> stateFrames() {
        String channel = "GC/" + canvas.hashCode();
        List<Map<String, Object>> frames = new ArrayList<>();
        for (GcFrames.Entry entry : dev.equo.swt.harness.BatchEntries.of(bridge.comm.sent)) {
            if (channel.equals(entry.channel())) frames.add((Map<String, Object>) entry.value());
        }
        return frames;
    }

    private static void mentions(Map<String, Object> frame, String... keys) {
        for (String key : keys) assertThat(WireValues.mentions(frame, key)).as("%s in %s", key, frame).isTrue();
    }

    private static void omits(Map<String, Object> frame, String... keys) {
        for (String key : keys) assertThat(WireValues.mentions(frame, key)).as("%s in %s", key, frame).isFalse();
    }

    private static void isAChange(Map<String, Object> frame) {
        assertThat(WireValues.same(frame.get("_d"), 0)).as("a change: %s", frame).isTrue();
    }

    @Test
    @DisplayName("a change inside a paint sends only the property that changed")
    void changeInsidePaintIsADiff() {
        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 10, 10);
        gc.setClipping(0, 0, 5, 5);
        gc.fillRectangle(0, 0, 10, 10);
        gc.dispose();

        List<Map<String, Object>> frames = stateFrames();
        assertThat(frames).hasSize(2);
        omits(frames.get(0), "_d");
        assertThat(frames.get(0)).containsEntry("_whole", true);
        mentions(frames.get(0), "background");
        // A change names neither the GC, its properties nor the state it follows: the channel says
        // which GC and delivers in order, and the drawer merges whatever keys arrive.
        isAChange(frames.get(1));
        assertThat(frames.get(1).get("clipping")).isInstanceOf(Map.class);
        omits(frames.get(1), "_b", "_s", "swt", "id", "background", "foreground");
    }

    @Test
    @DisplayName("a colour travels as its ARGB word, described whole or changed inside a paint")
    void changedColourIsPacked() {
        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 10, 10);
        gc.setForeground(new Color(canvas.getDisplay(), 0x12, 0x34, 0x56));
        gc.drawLine(0, 0, 10, 10);
        gc.dispose();

        List<Map<String, Object>> frames = stateFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(0)).containsEntry("foreground", "#ff000000");
        assertThat(frames.get(1)).containsEntry("foreground", "#ff123456");
    }

    @Test
    @DisplayName("a colour change does not resend the pattern it clears when there was none")
    void colourChangeWithoutPatternSendsOnlyTheColour() {
        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 10, 10);
        gc.setForeground(new Color(canvas.getDisplay(), 0x12, 0x34, 0x56));
        gc.setBackground(new Color(canvas.getDisplay(), 0x65, 0x43, 0x21));
        gc.drawLine(0, 0, 10, 10);
        gc.dispose();

        List<Map<String, Object>> frames = stateFrames();
        assertThat(frames).hasSize(2);
        mentions(frames.get(1), "foreground", "background");
        assertThat(WireValues.containsText(frames.get(1), "Pattern")).as("%s", frames.get(1)).isFalse();
    }

    @Test
    @DisplayName("a colour change after a pattern tells the client the pattern is gone")
    void colourChangeAfterPatternClearsIt() {
        Display display = canvas.getDisplay();
        Pattern pattern = new Pattern(display, 0, 0, 10, 10,
                new Color(display, 255, 0, 0), new Color(display, 0, 0, 255));
        GC gc = new GC(canvas);
        gc.setForegroundPattern(pattern);
        gc.setBackgroundPattern(pattern);
        gc.fillRectangle(0, 0, 10, 10);
        gc.setForeground(new Color(display, 0x12, 0x34, 0x56));
        gc.setBackground(new Color(display, 0x65, 0x43, 0x21));
        gc.drawLine(0, 0, 10, 10);
        gc.dispose();

        List<Map<String, Object>> frames = stateFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(0).get("foregroundPattern")).isInstanceOf(Map.class);
        assertThat(frames.get(0).get("backgroundPattern")).isInstanceOf(Map.class);
        assertThat(frames.get(1)).containsEntry("foregroundPattern", null).containsEntry("backgroundPattern", null);
    }

    @Test
    @DisplayName("a new GC on the same control starts whole")
    void newGcStartsWhole() {
        GC first = new GC(canvas);
        first.fillRectangle(0, 0, 10, 10);
        first.dispose();
        bridge.comm.sent.clear();

        // A description the client already holds is not sent again, so this one has to differ. Not a
        // system colour: the mocked display answers every one with the canvas's own foreground.
        GC second = new GC(canvas);
        second.setForeground(new Color(canvas.getDisplay(), 255, 0, 0));
        second.fillRectangle(0, 0, 10, 10);
        second.dispose();

        assertThat(stateFrames()).hasSize(1);
        omits(stateFrames().get(0), "_d");
        mentions(stateFrames().get(0), "background");
    }

    @Test
    @DisplayName("a GC whose channel another GC wrote to since sends its state whole")
    void interleavedGcsSendWhole() {
        GC outer = new GC(canvas);
        outer.fillRectangle(0, 0, 10, 10);
        GC inner = new GC(canvas);
        inner.fillRectangle(0, 0, 10, 10);
        inner.dispose();
        bridge.comm.sent.clear();

        outer.setClipping(0, 0, 5, 5);
        outer.fillRectangle(0, 0, 10, 10);
        outer.dispose();

        List<Map<String, Object>> frames = stateFrames();
        omits(frames.get(frames.size() - 1), "_d");
        mentions(frames.get(frames.size() - 1), "clipping", "background");
    }

    @Test
    @DisplayName("replacing a rectangle clip with a region names the region's rectangles")
    void regionClipNamesItsRectangles() {
        GC gc = new GC(canvas);
        gc.setClipping(0, 0, 5, 5);
        gc.fillRectangle(0, 0, 10, 10);
        Region region = new Region(canvas.getDisplay());
        region.add(1, 2, 3, 4);
        gc.setClipping(region);
        gc.fillRectangle(0, 0, 10, 10);
        region.dispose();
        gc.dispose();

        List<Map<String, Object>> frames = stateFrames();
        assertThat(frames).hasSize(2);
        mentions(frames.get(1), "_d", "clippingRects");
    }
}

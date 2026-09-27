package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
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
    private List<String> stateFrames() {
        String channel = "GC/" + canvas.hashCode();
        List<String> frames = new ArrayList<>();
        for (String[] entry : dev.equo.swt.harness.BatchEntries.of(bridge.comm.sent)) {
            if (channel.equals(entry[0])) frames.add(entry[1]);
        }
        return frames;
    }

    @Test
    @DisplayName("a change inside a paint sends only the property that changed")
    void changeInsidePaintIsADiff() {
        GC gc = new GC(canvas);
        gc.fillRectangle(0, 0, 10, 10);
        gc.setClipping(0, 0, 5, 5);
        gc.fillRectangle(0, 0, 10, 10);
        gc.dispose();

        List<String> frames = stateFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(0)).doesNotContain("\"_d\"").contains("\"_s\"").contains("\"background\"");
        assertThat(frames.get(1)).contains("\"_d\":[").contains("\"clipping\":{").contains("\"_b\"")
                .doesNotContain("\"background\"").doesNotContain("\"foreground\"");
    }

    @Test
    @DisplayName("a new GC on the same control starts whole")
    void newGcStartsWhole() {
        GC first = new GC(canvas);
        first.fillRectangle(0, 0, 10, 10);
        first.dispose();
        bridge.comm.sent.clear();

        // A description the client already holds is not sent again, so this one has to differ.
        GC second = new GC(canvas);
        second.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_RED));
        second.fillRectangle(0, 0, 10, 10);
        second.dispose();

        assertThat(stateFrames()).hasSize(1);
        assertThat(stateFrames().get(0)).doesNotContain("\"_d\"").contains("\"background\"");
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

        List<String> frames = stateFrames();
        assertThat(frames.get(frames.size() - 1)).doesNotContain("\"_d\"").contains("\"clipping\"")
                .contains("\"background\"");
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

        List<String> frames = stateFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(1)).contains("\"_d\"").contains("\"clippingRects\"");
    }
}

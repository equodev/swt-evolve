package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.VControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A control set to a {@link Region} is clipped to it on the Flutter side, so the region has to reach
 * the wire as its rectangles. Upstream keeps a region in an OS handle and exposes only its bounds,
 * and bounds are not the shape: an application builds an outline by adding a rectangle and
 * subtracting the middle, which is how the Eclipse workbench frames the drop feedback for a dragged
 * view. A region that arrives without its rectangles covers nothing and hides the control instead.
 */
@Tag("native-unit")
class RegionRectsNativeTest {

    private RecordingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** A Dart-backed control, the way an application reaches one. */
    private Canvas freshCanvas() {
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        return canvas;
    }

    /** What the control hands the serializer: the value object Flutter is built from. */
    private static int[] serializedRects(Canvas canvas) {
        Region region = ((VControl) ((DartControl) canvas.getImpl()).getValue()).getRegion();
        return ((DartRegion) region.getImpl()).getValue().getRects();
    }

    @Test
    @DisplayName("the region is in the JSON the control is rebuilt from")
    void regionReachesTheWire() throws Exception {
        Canvas canvas = freshCanvas();
        Region region = new Region(canvas.getDisplay());
        region.add(10, 20, 100, 50);
        canvas.setRegion(region);

        String json = new String(
                new dev.equo.swt.Serializer().to(((DartControl) canvas.getImpl()).getValue()),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(json).as("the value object carries it, but only the wire reaches Flutter")
                .contains("\"region\"").contains("\"rects\"");
    }

    @Test
    @DisplayName("a control's region travels as its rectangles")
    void regionCarriesItsRectangles() {
        Canvas canvas = freshCanvas();
        Region region = new Region(canvas.getDisplay());
        region.add(10, 20, 100, 50);

        canvas.setRegion(region);

        assertThat(serializedRects(canvas)).as("the shape, not just that a region exists")
                .containsExactly(10, 20, 100, 50);
    }

    @Test
    @DisplayName("an outline keeps the bars the subtract left behind")
    void anOutlineKeepsItsBars() {
        // The workbench's drop feedback: a rectangle with its middle taken out. Sending the bounds
        // would describe a filled sheet, which is what covers the application instead of framing it.
        Canvas canvas = freshCanvas();
        Region region = new Region(canvas.getDisplay());
        region.add(0, 0, 100, 100);
        region.subtract(2, 2, 96, 96);

        canvas.setRegion(region);

        assertThat(region.getBounds()).as("bounds alone still describe the whole area")
                .isEqualTo(new Rectangle(0, 0, 100, 100));
        assertThat(serializedRects(canvas).length / 4)
                .as("every bar the subtract left, not one rectangle")
                .isGreaterThan(1);
        assertThat(region.contains(50, 50))
                .as("the middle is outside the region -- the shape is a frame")
                .isFalse();
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Transform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * {@code drawBackground} is how a custom-painted control fills part of a GC with its own background,
 * colour or tiled image. Eclipse Forms' {@code Section} paints its title-bar gradient this way: the
 * gradient is the section's background image, and the title bar is filled from it.
 */
@Tag("native-unit")
class CanvasDrawBackgroundNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Canvas canvas;

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /**
     * The traffic of a simulated SWT.Paint cycle that calls drawBackground, read after disposing the
     * GC: a paint's ops are batched until then. A Paint listener cannot run in this harness.
     */
    private String drawBackground(int x, int y, int width, int height) {
        bridge.comm.sent.clear();
        ControlHelper.inPaintDepth++;
        try {
            GC gc = new GC(canvas);
            canvas.drawBackground(gc, x, y, width, height);
            gc.dispose();
        } finally {
            ControlHelper.inPaintDepth--;
        }
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("drawBackground fills the rectangle with the control's background colour")
    void fillsWithTheBackgroundColor() {
        canvas.setBackground(new Color(display, 7, 8, 9));

        String wire = drawBackground(0, 0, 50, 20);

        assertThat(wire).contains("fillRectangleintintintint\",[0,0,50,20]")
                .contains("\"background\":{\"a\":255,\"b\":9,\"g\":8,\"r\":7}");
    }

    @Test
    @DisplayName("drawBackground tiles the control's background image over the rectangle")
    void fillsWithTheBackgroundImage() {
        canvas.setBackgroundImage(new Image(display, 1, 20));

        String wire = drawBackground(0, 0, 50, 20);

        assertThat(wire).contains("fillRectangleintintintint\",[0,0,50,20]")
                .contains("\"backgroundPattern\":{");
    }

    @Test
    @DisplayName("drawBackground leaves the GC's own background colour and pattern as it found them")
    void restoresTheGcBackground() {
        canvas.setBackground(new Color(display, 7, 8, 9));
        GC gc = new GC(canvas);
        Color before = new Color(display, 1, 2, 3);
        gc.setBackground(before);
        Pattern pattern = new Pattern(display, new Image(display, 2, 2));
        gc.setBackgroundPattern(pattern);

        canvas.drawBackground(gc, 0, 0, 10, 10);

        assertThat(gc.getBackground()).isEqualTo(before);
        assertThat(gc.getBackgroundPattern()).isSameAs(pattern);
        gc.dispose();
    }

    @Test
    @DisplayName("drawBackground shifts the image tile by the offset and gives the GC its transform back")
    void offsetsTheImageAndRestoresTheTransform() {
        canvas.setBackgroundImage(new Image(display, 1, 20));
        bridge.comm.sent.clear();
        float[] before = new float[6];
        float[] after = new float[6];
        ControlHelper.inPaintDepth++;
        try {
            GC gc = new GC(canvas);
            Transform transform = new Transform(display);
            transform.translate(2, 4);
            gc.setTransform(transform);
            gc.getTransform(transform);
            transform.getElements(before);

            canvas.drawBackground(gc, 0, 0, 50, 20, 5, 3);

            gc.getTransform(transform);
            transform.getElements(after);
            transform.dispose();
            gc.dispose();
        } finally {
            ControlHelper.inPaintDepth--;
        }
        String wire = bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);

        assertThat(wire).contains("fillRectangleintintintint\",[5,3,50,20]")
                .contains("\"elements\":[1.0,0.0,0.0,1.0,-3.0,1.0]");
        assertThat(after).containsExactly(before);
    }
}

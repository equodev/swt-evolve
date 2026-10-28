package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Every platform holds a clip in device space: a clip set under a transform stays where it was set
 * when the transform changes afterwards, whichever overload set it, and it is reported back through
 * the transform in force when it is read. An empty clip is a clip, the one that admits nothing.
 * And a GC is only had from a live control.
 */
@Tag("native-unit")
class GCClipUnderTransformNativeTest {

    private RecordingBridge bridge;
    private Canvas canvas;
    private GC gc;

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
        canvas.setBounds(0, 0, 200, 200);
        gc = new GC(canvas);
    }

    @AfterEach
    void tearDown() {
        if (!gc.isDisposed())
            gc.dispose();
        FlutterBridge.set(null);
    }

    private void translate(float dx, float dy) {
        Transform transform = new Transform(gc.getDevice(), 1, 0, 0, 1, dx, dy);
        gc.setTransform(transform);
        transform.dispose();
    }

    @Test
    @DisplayName("a region clip set under a transform stays where it was set")
    void regionClipIsHeldInDeviceSpace() {
        translate(10, 10);
        Region region = new Region(gc.getDevice());
        region.add(20, 20, 30, 30);
        gc.setClipping(region);
        region.dispose();

        assertThat(gc.getClipping()).isEqualTo(new Rectangle(20, 20, 30, 30));
        Region read = new Region(gc.getDevice());
        gc.getClipping(read);
        assertThat(read.getBounds()).isEqualTo(new Rectangle(20, 20, 30, 30));
        read.dispose();

        gc.setTransform(null);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(30, 30, 30, 30));
    }

    @Test
    @DisplayName("a path clip set under a transform stays where it was set")
    void pathClipIsHeldInDeviceSpace() {
        Transform scale = new Transform(gc.getDevice(), 2, 0, 0, 2, 0, 0);
        gc.setTransform(scale);
        scale.dispose();
        Path path = new Path(gc.getDevice());
        path.addRectangle(5, 5, 10, 10);
        gc.setClipping(path);
        path.dispose();

        assertThat(gc.getClipping()).isEqualTo(new Rectangle(5, 5, 10, 10));
        gc.setTransform(null);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(10, 10, 20, 20));
    }

    @Test
    @DisplayName("an empty path is a clip that admits nothing")
    void emptyPathIsAClip() {
        Path empty = new Path(gc.getDevice());
        gc.setClipping(empty);
        empty.dispose();

        assertThat(gc.isClipped()).isTrue();
        assertThat(gc.getClipping().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("a disposed control refuses a GC as a disposed widget, not with a NullPointerException")
    void disposedControlRefusesAGc() {
        gc.dispose();
        canvas.dispose();

        assertThatThrownBy(() -> new GC(canvas))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
    }
}

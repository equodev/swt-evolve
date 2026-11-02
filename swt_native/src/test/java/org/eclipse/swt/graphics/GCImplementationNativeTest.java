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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/** What {@link DartGC} does when it is driven directly rather than through the dual-run API tests. */
@Tag("native-unit")
class GCImplementationNativeTest {

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
        bridge.comm.sent.clear();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartGC impl(GC gc) {
        return (DartGC) gc.getImpl();
    }

    private String wire() {
        return String.join(" ", dev.equo.swt.harness.GcFrames.channels(bridge.comm.sent));
    }

    /** A drawable that is neither a control nor an image nor a device. */
    private Drawable foreignDrawable() {
        return new Drawable() {
            @Override
            public long internal_new_GC(GCData data) {
                data.device = display;
                data.font = display.getSystemFont();
                return 0;
            }

            @Override
            public void internal_dispose_GC(long handle, GCData data) {
            }

            // Absent before 3.104, abstract on 3.105 and default on the newest: an implementation
            // without @Override compiles against all of them.
            public boolean isAutoScalable() {
                return true;
            }
        };
    }

    @Test
    @DisplayName("a GC made without a drawable is Dart-backed, disposed and at SWT's defaults")
    void gcWithoutADrawable() {
        GC gc = new GC();

        assertThat(gc.getImpl()).isInstanceOf(DartGC.class);
        assertThat(impl(gc).getApi()).isSameAs(gc);
        assertThat(gc.isDisposed()).isTrue();
        assertThat(impl(gc)._alpha()).isEqualTo(255);
        assertThat(impl(gc)._antialias()).isEqualTo(SWT.DEFAULT);
        assertThat(impl(gc)._interpolation()).isEqualTo(SWT.DEFAULT);
        assertThat(impl(gc)._textAntialias()).isEqualTo(SWT.DEFAULT);
        assertThat(impl(gc)._clippingRects()).isEmpty();
        assertThat(impl(gc).wireBufferScale()).isEqualTo(1f);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartGC impl = new DartGC((GC) null);

        GC api = impl.getApi();

        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("an owner-draw capture takes every image and text draw instead of the wire")
    void captureTakesTheDraws() {
        Image image = new Image(display, 8, 8);
        GC gc = new GC(canvas);
        List<Image> images = new ArrayList<>();
        impl(gc).imageCapture = images::add;

        gc.drawImage(image, 0, 0);
        gc.drawImage(image, 0, 0, 4, 4, 0, 0, 4, 4);
        gc.drawText("ignored while only images are captured", 0, 0);
        List<String> texts = new ArrayList<>();
        impl(gc).textCapture = texts::add;
        gc.drawText("captured", 0, 0);
        gc.dispose();

        assertThat(images).containsExactly(image, image);
        assertThat(texts).containsExactly("captured");
        assertThat(wire()).doesNotContain("drawImage").doesNotContain("drawText");
        image.dispose();
    }

    @Test
    @DisplayName("a disposed GC puts no dash offset on the wire")
    void disposedGcHasNoDashOffset() {
        GC gc = new GC(canvas);
        gc.setLineAttributes(new LineAttributes(1, SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_CUSTOM, new float[] {2, 2}, 3, 10));
        assertThat(impl(gc).wireLineDashOffset()).isEqualTo(3f);

        gc.dispose();

        assertThat(impl(gc).wireLineDashOffset()).isZero();
    }

    @Test
    @DisplayName("a clip from a path with text is reported as the text and the shape, and a rectangle replaces it")
    void textClipThenRectangle() {
        GC gc = new GC(canvas);
        Font font = new Font(display, new FontData("Helvetica", 12, SWT.NORMAL));
        Path path = new Path(display);
        path.addString("A", 0, 0, font);
        path.addRectangle(50, 50, 10, 10);

        gc.setClipping(path);
        path.dispose();

        assertThat(gc.isClipped()).isTrue();
        assertThat(gc.getClipping().contains(55, 55)).as("the shape is inside the clip").isTrue();
        assertThat(impl(gc).wireClippingText()).isNotNull();
        gc.setClipping(1, 1, 5, 5);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(1, 1, 5, 5));
        assertThat(impl(gc).wireClippingText()).isNull();
        Path text = new Path(display);
        text.addString("A", 0, 0, font);
        gc.setClipping(text);
        text.dispose();
        assertThat(gc.isClipped()).isTrue();
        assertThat(impl(gc).wireClippingText()).isNotNull();
        gc.dispose();
        font.dispose();
    }

    @Test
    @DisplayName("a path that is only a close is an empty clip")
    void closeOnlyPathIsAnEmptyClip() {
        GC gc = new GC(canvas);
        Path path = new Path(display);
        path.close();

        gc.setClipping(path);
        path.dispose();

        assertThat(gc.isClipped()).isTrue();
        assertThat(gc.getClipping().isEmpty()).isTrue();
        gc.dispose();
    }

    @Test
    @DisplayName("a GC that renders nothing of an image's has no snapshot to wait for")
    void snapshotWithoutAnImageRender() {
        GC controlGc = new GC(canvas);
        impl(controlGc).requestRenderSnapshotAndWait();
        controlGc.dispose();

        Image image = new Image(display, 4, 4);
        GC imageGc = new GC(image);
        imageGc.dispose();
        impl(imageGc).requestRenderSnapshotAndWait();
        image.dispose();

        assertThat(controlGc.isDisposed()).isTrue();
        assertThat(imageGc.isDisposed()).isTrue();
    }

    @Test
    @DisplayName("a GC on a drawable SWT does not know reports an unbounded clip and copies nothing")
    void gcOnAForeignDrawable() {
        GC gc = new GC(foreignDrawable());
        Image target = new Image(display, 4, 4);
        bridge.comm.sent.clear();

        assertThat(gc.getClipping()).isEqualTo(new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        gc.setClipping(1, 2, 3, 4);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(1, 2, 3, 4));
        gc.copyArea(target, 0, 0);

        assertThat(wire()).doesNotContain("copyArea");
        gc.dispose();
        target.dispose();
    }
}

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
 * A clip is a shape, not only a rectangle: {@code setClipping(Path)} and {@code setClipping(Region)}
 * have to narrow what is drawn afterwards the way the rectangle overload does. On this backend a
 * Path and a Region are Dart-backed too, so the clip can travel as its exact shape — the rectangles
 * a Region is the union of, and the curve geometry of a Path. Without that, everything drawn after
 * the call covers the whole canvas: a shape the wire cannot carry reads as no clip at all.
 */
@Tag("native-unit")
class GCClipShapeNativeTest {

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

    /** A GC on a Dart-backed Canvas — the drawable an application paints through. */
    private GC freshGc() {
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private String wireAfterDisposing(GC gc) {
        gc.drawLine(0, 0, 10, 10);
        gc.dispose();
        String wire = bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
        assertThat(wire).as("sanity: the GC's ops reached the wire").contains("drawLine");
        return wire;
    }

    private Path rectanglePath(GC gc, int x, int y, int width, int height) {
        Path path = new Path(gc.getDevice());
        path.addRectangle(x, y, width, height);
        return path;
    }

    @Test
    @DisplayName("clipping to a path narrows the reported clipping rectangle to the path bounds")
    void pathClippingReportsPathBounds() {
        GC gc = freshGc();
        Path path = rectanglePath(gc, 10, 20, 30, 40);
        gc.setClipping(path);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(10, 20, 30, 40));
        path.dispose();
        gc.dispose();
    }

    @Test
    @DisplayName("clipping to a region narrows the reported clipping rectangle to the region bounds")
    void regionClippingReportsRegionBounds() {
        GC gc = freshGc();
        Region region = new Region(gc.getDevice());
        region.add(new Rectangle(10, 20, 30, 40));
        gc.setClipping(region);
        assertThat(gc.getClipping()).isEqualTo(new Rectangle(10, 20, 30, 40));
        region.dispose();
        gc.dispose();
    }

    @Test
    @DisplayName("a path clip travels as geometry, not only as its bounding box")
    void pathGeometryReachesTheWire() {
        GC gc = freshGc();
        Path path = rectanglePath(gc, 10, 20, 30, 40);
        gc.setClipping(path);
        path.dispose();
        assertThat(wireAfterDisposing(gc))
                .contains("\"points\":[10.0,20.0,40.0,20.0,40.0,60.0,10.0,60.0]");
    }

    @Test
    @DisplayName("unsetting the clipping drops a shape clip set earlier")
    void unsettingTheClippingDropsTheShape() {
        GC gc = freshGc();
        Path path = rectanglePath(gc, 10, 20, 30, 40);
        gc.setClipping(path);
        path.dispose();
        gc.setClipping((Rectangle) null);
        assertThat(wireAfterDisposing(gc))
                .as("a shape clip left behind would keep narrowing everything drawn after the "
                        + "application restored the full clip")
                .doesNotContain("clippingPath");
    }

    @Test
    @DisplayName("a region clip travels as every rectangle it is made of")
    void regionRectanglesReachTheWire() {
        GC gc = freshGc();
        Region region = new Region(gc.getDevice());
        region.add(new Rectangle(10, 20, 30, 40));
        region.add(new Rectangle(60, 20, 30, 40));
        gc.setClipping(region);
        region.dispose();
        assertThat(wireAfterDisposing(gc))
                .as("the gap between the two rectangles is the whole point of a region clip; a "
                        + "bounding box would let drawing through it")
                .contains("\"clippingRects\":[10,20,30,40,60,20,30,40]");
    }

    @Test
    @DisplayName("a curved clip path travels as curves, not as its bounding box")
    void arcPathReachesTheWireAsCurves() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.addArc(0, 0, 100, 100, 0, 360);
        gc.setClipping(path);
        path.dispose();
        // Move, four cubics, close — base64, the way a byte[] travels: [1, 4, 4, 4, 4, 5].
        assertThat(wireAfterDisposing(gc))
                .as("a full circle is four cubic segments; a clip that arrived as line segments "
                        + "would have squared off the shape")
                .contains("\"clippingPath\"")
                .contains("\"types\":\"AQQEBAQF\"");
    }

    @Test
    @DisplayName("a clip path built from a string is left unclipped rather than clipping to nothing")
    void glyphOutlinePathDoesNotBlankTheDrawing() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.addString("SWT", 10, 10, gc.getFont());
        gc.setClipping(path);
        path.dispose();
        assertThat(wireAfterDisposing(gc))
                .as("glyph outlines have no representation on this backend; clipping to the empty "
                        + "shape they leave behind would blank everything drawn afterwards")
                .doesNotContain("clippingPath");
    }
}

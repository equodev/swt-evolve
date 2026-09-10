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
 * A Dart-backed Path has no OS handle to hold the geometry, so what an application builds with
 * moveTo/lineTo/cubicTo/addArc only survives if those calls are recorded into PathData, and only
 * reaches Flutter if drawPath/fillPath carry that data as a draw op.
 */
@Tag("native-unit")
class GCPathNativeTest {

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
        canvas.setBounds(0, 0, 400, 400);
        canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private String wire() {
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("moveTo/lineTo/close are recorded as path data")
    void lineSegmentsAreRecorded() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.moveTo(10, 20);
        path.lineTo(30, 40);
        path.close();

        PathData data = path.getPathData();
        assertThat(data.types).containsExactly(
                (byte) SWT.PATH_MOVE_TO, (byte) SWT.PATH_LINE_TO, (byte) SWT.PATH_CLOSE);
        assertThat(data.points).containsExactly(10f, 20f, 30f, 40f);
    }

    @Test
    @DisplayName("a curve on a fresh path starts at the origin, as the native backends do")
    void cubicOnEmptyPathIsRecorded() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.cubicTo(-150, 100, 150, 200, 0, 300);

        PathData data = path.getPathData();
        assertThat(data.types).containsExactly((byte) SWT.PATH_MOVE_TO, (byte) SWT.PATH_CUBIC_TO);
        assertThat(data.points).containsExactly(0f, 0f, -150f, 100f, 150f, 200f, 0f, 300f);
    }

    @Test
    @DisplayName("addArc over a full turn is a closed sub path of its own")
    void fullArcIsRecorded() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.addArc(0, 0, 100, 50, 0, 360);

        PathData data = path.getPathData();
        assertThat(data.types)
                .startsWith((byte) SWT.PATH_MOVE_TO)
                .endsWith((byte) SWT.PATH_CLOSE)
                .contains((byte) SWT.PATH_CUBIC_TO);
        assertThat(data.points).as("the arc starts at 3 o'clock on the ellipse")
                .startsWith(100f, 25f);
    }

    @Test
    @DisplayName("consecutive full arcs are separate sub paths, so an even-odd fill leaves rings")
    void consecutiveFullArcsEachStartTheirOwnSubPath() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.addArc(0, 0, 100, 100, 0, 360);
        path.addArc(20, 20, 60, 60, 0, 360);

        PathData data = path.getPathData();
        long moveTos = 0;
        for (byte type : data.types) if (type == SWT.PATH_MOVE_TO) moveTos++;
        assertThat(moveTos).as("one moveTo per ellipse, never a line joining them").isEqualTo(2);
    }

    @Test
    @DisplayName("drawPath carries the geometry to Flutter")
    void drawPathReachesTheWire() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.moveTo(10, 20);
        path.lineTo(30, 40);
        gc.drawPath(path);
        gc.dispose();

        assertThat(wire()).as("the op itself").contains("drawPathPath");
        assertThat(wire()).as("the path's geometry")
                .contains("\"points\":[10.0,20.0,30.0,40.0]");
    }

    @Test
    @DisplayName("fillPath carries the geometry to Flutter")
    void fillPathReachesTheWire() {
        GC gc = freshGc();
        Path path = new Path(gc.getDevice());
        path.addRectangle(0, 0, 10, 20);
        gc.fillPath(path);
        gc.dispose();

        assertThat(wire()).as("the op itself").contains("fillPathPath");
        assertThat(wire()).as("the rectangle's geometry")
                .contains("\"points\":[0.0,0.0,10.0,0.0,10.0,20.0,0.0,20.0]");
    }
}

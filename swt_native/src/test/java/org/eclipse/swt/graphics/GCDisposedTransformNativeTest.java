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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * The GC owns its copy of the matrix. SWT lets an application dispose a {@link Transform} as soon as
 * {@code GC#setTransform} returns, because a native GC copies it at set time; this backend
 * serializes the GC's state lazily, at the next draw, so reading the caller's object then throws
 * {@code ERROR_GRAPHIC_DISPOSED} and the throw escapes as far as the display loop. That copy is also
 * what {@code getTransform} has to answer with, for the read-modify-restore idiom painters use
 * around a rotation.
 */
@Tag("native-unit")
class GCDisposedTransformNativeTest {

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
    private String wire() {
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
    }

    @Test
    @DisplayName("a Transform disposed right after setTransform still reaches Flutter")
    void matrixSurvivesCallerDispose() {
        GC gc = freshGc();
        Transform transform = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(transform);
        transform.dispose();

        assertThatCode(() -> {
            gc.drawLine(0, 0, 10, 10);
            gc.dispose();
        }).as("disposing the Transform is legal SWT — drawing after it must not throw")
                .doesNotThrowAnyException();

        assertThat(wire()).as("sanity: the GC's ops reached the wire").contains("drawLine");
        assertThat(wire()).as("the matrix set before the dispose must still travel")
                .contains("\"transform\":{\"elements\":[2.0,0.0,0.0,2.0,5.0,7.0]}");
    }

    @Test
    @DisplayName("a later setTransform overrides the matrix an earlier disposed one left")
    void latestMatrixWins() {
        GC gc = freshGc();
        Transform first = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(first);
        first.dispose();
        Transform second = new Transform(gc.getDevice(), 3, 0, 0, 3, 1, 1);
        gc.setTransform(second);
        second.dispose();

        gc.drawLine(0, 0, 10, 10);
        gc.dispose();

        assertThat(wire()).contains("\"transform\":{\"elements\":[3.0,0.0,0.0,3.0,1.0,1.0]}");
        assertThat(wire()).doesNotContain("[2.0,0.0,0.0,2.0,5.0,7.0]");
    }

    @Test
    @DisplayName("setTransform(null) clears the matrix a disposed Transform left behind")
    void nullClearsTheMatrix() {
        GC gc = freshGc();
        Transform transform = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(transform);
        transform.dispose();
        gc.setTransform(null);

        gc.drawLine(0, 0, 10, 10);
        gc.dispose();

        assertThat(wire()).doesNotContain("\"transform\"");
    }

    @Test
    @DisplayName("getTransform answers with the matrix currently set")
    void getTransformAnswersTheCurrentMatrix() {
        GC gc = freshGc();
        Transform set = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(set);
        set.dispose();

        Transform read = new Transform(gc.getDevice());
        gc.getTransform(read);
        float[] elements = new float[6];
        read.getElements(elements);

        assertThat(elements).as("a painter reading the GC's transform must see what it set")
                .containsExactly(2, 0, 0, 2, 5, 7);
    }

    @Test
    @DisplayName("getTransform answers identity while no transform is set")
    void getTransformAnswersIdentityWhenUnset() {
        GC gc = freshGc();
        Transform read = new Transform(gc.getDevice(), 3, 3, 3, 3, 3, 3);
        gc.getTransform(read);
        float[] elements = new float[6];
        read.getElements(elements);

        assertThat(elements).containsExactly(1, 0, 0, 1, 0, 0);
    }

    @Test
    @DisplayName("the read-modify-restore idiom leaves the GC where it started")
    void readModifyRestoreRoundTrips() {
        GC gc = freshGc();
        Transform outer = new Transform(gc.getDevice(), 2, 0, 0, 2, 5, 7);
        gc.setTransform(outer);
        outer.dispose();

        // What VerticalTextPainter-style code does: save, rotate, draw, put it back.
        Transform saved = new Transform(gc.getDevice());
        gc.getTransform(saved);
        Transform rotated = new Transform(gc.getDevice());
        gc.getTransform(rotated);
        rotated.rotate(-90f);
        gc.setTransform(rotated);
        rotated.dispose();
        gc.setTransform(saved);
        saved.dispose();

        Transform read = new Transform(gc.getDevice());
        gc.getTransform(read);
        float[] elements = new float[6];
        read.getElements(elements);

        assertThat(elements).as("restoring must return the outer transform, not identity")
                .containsExactly(2, 0, 0, 2, 5, 7);
    }
}

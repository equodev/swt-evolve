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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A Dart-backed Pattern has no native brush, so the GC state it rides in is the only way Flutter
 * learns what to paint with.
 */
@Tag("native-unit")
class GCPatternNativeTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** A GC on a Dart-backed Canvas, and a Display the graphics resources are Dart-backed on. */
    private GC freshGc() {
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 400);
        bridge.comm.sent.clear();
        return new GC(canvas);
    }

    /** The GC's traffic, read after disposing it: a paint's ops are batched until then. */
    private List<Object> wire() {
        return BatchEntries.of(bridge.comm.sent).stream().map(GcFrames.Entry::value).toList();
    }

    /** Whether any record on {@code wire} holds {@code expected} under {@code key}. */
    private static boolean carries(List<Object> wire, String key, Object expected) {
        return !WireValues.find(wire, m -> m.containsKey(key) && WireValues.same(m.get(key), expected)).isEmpty();
    }

    /**
     * No GC here: any op or flush would serialize the image, and that encodes a PNG through
     * ImageLoader, which needs the native SWT library this suite runs without.
     */
    @Test
    @DisplayName("an image pattern models the image it was built from")
    void imagePatternCarriesItsImage() {
        display = DartMocks.dartDisplay();
        Image image = new Image(display, 4, 4);
        assertThat(image.getImpl()).isInstanceOf(DartImage.class);

        Pattern pattern = new Pattern(display, image);

        assertThat(((DartPattern) pattern.getImpl()).getValue().getImage()).isSameAs(image);
        assertThat(bridge.comm.sent).as("nothing serialized, so no encode was reached").isEmpty();
    }

    @Test
    @DisplayName("a gradient pattern carries its end points and colors")
    void gradientPatternCarriesItsGeometry() {
        GC gc = freshGc();
        Color red = new Color(display, 255, 0, 0);
        Color blue = new Color(display, 0, 0, 255);
        Pattern pattern = new Pattern(display, 1, 2, 30, 40, red, blue);
        gc.setBackgroundPattern(pattern);
        gc.fillRectangle(0, 0, 40, 40);
        gc.dispose();

        List<Object> wire = wire();
        assertThat(carries(wire, "color1", "#ffff0000")).as("color1").isTrue();
        assertThat(carries(wire, "startX", 1.0)).as("startX").isTrue();
        assertThat(carries(wire, "startY", 2.0)).as("startY").isTrue();
        assertThat(carries(wire, "endX", 30.0)).as("endX").isTrue();
        assertThat(carries(wire, "endY", 40.0)).as("endY").isTrue();
    }
}

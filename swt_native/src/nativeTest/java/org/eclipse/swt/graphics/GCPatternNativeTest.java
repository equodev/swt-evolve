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
    private String wire() {
        return bridge.comm.sent.stream().map(f -> f.json).reduce("", String::concat);
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
        Color red = new Color(255, 0, 0);
        Color blue = new Color(0, 0, 255);
        Pattern pattern = new Pattern(display, 1, 2, 30, 40, red, blue);
        gc.setBackgroundPattern(pattern);
        gc.fillRectangle(0, 0, 40, 40);
        gc.dispose();

        assertThat(wire())
                .contains("\"color1\":{\"alpha\":255,\"blue\":0,\"green\":0,\"red\":255}")
                .contains("\"startX\":1.0").contains("\"startY\":2.0")
                .contains("\"endX\":30.0").contains("\"endY\":40.0");
    }
}

package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every drawImage names its image to the client, and what the client lacks of it is described ahead of
 * the frame. Once the client holds the image there is nothing to describe, so drawing it again and
 * again (icons in a paint) must not make a description's tape, an 8 KB buffer and its maps, each time.
 * <p>
 * Checked by the tape's identity rather than by bytes allocated: a mocked Resource has Mockito advise
 * every {@code getImpl()} in the JVM, and what that allocates depends on the SWT release and the JIT.
 */
@Tag("native-unit")
class GCDrawImageAllocationNativeTest {

    private Display display;
    private Canvas canvas;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        Shell shell = DartMocks.dartShell(display);
        canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 200);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private void draw(Image image, int times) {
        GC gc = new GC(canvas);
        for (int i = 0; i < times; i++) gc.drawImage(image, i % 100, i % 50);
        gc.dispose();
    }

    @Test
    void drawingAnImageTheClientHoldsDescribesItOnTheThreadsSpareTape() {
        Image image = new Image(display, new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        draw(image, 1);

        Serializer.Tape spare = Serializer.Tape.borrow(0);
        spare.release();
        draw(image, 200);
        Serializer.Tape next = Serializer.Tape.borrow(0);
        next.release();

        assertThat(next).as("the tape the draws handed back").isSameAs(spare);
        image.dispose();
    }

    @Test
    void aDrawImageFillsTheThreadsOpAndLetsGoOfItsImage() {
        Image image = new Image(display, new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        GC gc = new GC(canvas);
        gc.drawImage(image, 37, 21);
        gc.drawImage(image, 1, 2, 8, 9, 40, 50, 16, 18);
        gc.dispose();

        VGC.VGCDrawImageImageintint at = VGC.VGCDrawImageImageintint.reused();
        assertThat(new int[] {at.x, at.y}).as("the thread's drawImage(Image, x, y)").containsExactly(37, 21);
        assertThat(at.image).as("the image it drew, once written").isNull();
        VGC.VGCDrawImageImageintintintintintintintint scaled = VGC.VGCDrawImageImageintintintintintintintint.reused();
        assertThat(new int[] {scaled.srcX, scaled.srcY, scaled.srcWidth, scaled.srcHeight,
                scaled.destX, scaled.destY, scaled.destWidth, scaled.destHeight})
                .as("the thread's scaled drawImage").containsExactly(1, 2, 8, 9, 40, 50, 16, 18);
        assertThat(scaled.image).as("the image it drew, once written").isNull();
        image.dispose();
    }
}

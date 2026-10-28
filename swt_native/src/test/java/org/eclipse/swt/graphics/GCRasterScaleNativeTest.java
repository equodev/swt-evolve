package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
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

import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * At a raster scale above 1 an off-screen image holds more pixels than the application's coordinates:
 * it is drawn in its own units, and what crosses to Flutter is its pixels. A blit of it, a GC on it
 * and a copy into it all translate between the two.
 */
@Tag("native-unit")
class GCRasterScaleNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private Canvas canvas;
    private String autoScale;

    @BeforeEach
    void setUp() {
        autoScale = System.clearProperty("swt.autoScale");
        Config.setClientDeviceZoom(200);
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 100, 100);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        Config.setClientDeviceZoom(100);
        if (autoScale != null)
            System.setProperty("swt.autoScale", autoScale);
    }

    private String wire() {
        return bridge.comm.sent.stream().map(f -> f.json).collect(Collectors.joining());
    }

    @Test
    @DisplayName("an off-screen image holds its pixels at the raster scale and answers in its own units")
    void scratchBufferAtTheRasterScale() {
        Image image = new Image(display, 10, 10);
        DartImage impl = (DartImage) image.getImpl();

        assertThat(impl._logicalWidth()).isEqualTo(10);
        assertThat(impl._wireWidth()).isEqualTo(20);
        assertThat(image.getBounds()).isEqualTo(new Rectangle(0, 0, 10, 10));
        assertThat(image.getImageData(200).width).isEqualTo(20);
        GC gc = new GC(image);
        assertThat(((DartGC) gc.getImpl()).wireBufferScale()).isEqualTo(2f);
        gc.dispose();
        image.dispose();
    }

    @Test
    @DisplayName("a blit of an off-screen image sends its pixels into the area the application named")
    void blitSendsThePixels() {
        Image image = new Image(display, 10, 10);
        GC gc = new GC(canvas);
        bridge.comm.sent.clear();

        gc.drawImage(image, 5, 5);
        gc.drawImage(image, 0, 0, 5, 5, 0, 0, 5, 5);
        gc.dispose();

        assertThat(wire()).contains(",0,0,20,20,5,5,10,10]").contains(",0,0,10,10,0,0,5,5]");
        image.dispose();
    }

    @Test
    @DisplayName("a copy into an off-screen image lands at its pixels")
    void copyIntoTheBuffer() throws Exception {
        Image image = new Image(display, 10, 10);
        GC gc = new GC(canvas);
        ImageData red = new ImageData(10, 10, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        for (int y = 0; y < 10; y++)
            for (int x = 0; x < 10; x++)
                red.setPixel(x, y, 0xFF0000);
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] {red};
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        loader.save(png, SWT.IMAGE_PNG);
        Thread answer = new Thread(() -> {
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline) {
                try {
                    bridge.comm.fireContaining("copyAreaImageintintResponse", png.toByteArray());
                    return;
                } catch (IllegalStateException notYet) {
                    Thread.onSpinWait();
                }
            }
        });
        answer.start();

        gc.copyArea(image, 0, 0);
        answer.join();

        ImageData pixels = image.getImageData(200);
        assertThat(pixels.width).isEqualTo(20);
        assertThat(pixels.palette.getRGB(pixels.getPixel(15, 15))).isEqualTo(new RGB(255, 0, 0));
        gc.dispose();
        image.dispose();
    }
}

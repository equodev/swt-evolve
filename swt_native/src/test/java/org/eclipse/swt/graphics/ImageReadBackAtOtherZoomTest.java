package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.DPIUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.eclipse.swt.widgets.Mocks.device;
import static org.mockito.Mockito.when;

/**
 * An Image drawn by a GC is held at the raster scale, so reading it at 100 % has to scale it.
 * With smooth scaling (what win32 uses under monitor-specific scaling) DPIUtil scales by drawing
 * into another Image and reading that one back at 100 % -- which must not need scaling in turn.
 */
class ImageReadBackAtOtherZoomTest extends SerializeTestBase {

    private boolean bootstrapped;
    private String savedMode;
    private String savedAutoScale;
    private Field scaleMethod;
    private Object savedScaleMethod;
    private final RenderingComm comm = new RenderingComm();

    @BeforeEach
    void drawAtOneAndAHalfWithSmoothScaling() throws Exception {
        bootstrapped = FlutterBridge.displayBootstrapped;
        FlutterBridge.displayBootstrapped = true;
        FlutterBridge.setDisplayGcCommResolver(display -> comm);

        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        savedAutoScale = System.getProperty("swt.autoScale");
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        System.clearProperty("swt.autoScale");
        Config.setClientDeviceZoom(150);
        // A real Display is auto-scalable; the mock answers false, and DPIUtil then skips scaling.
        when(device().isAutoScalable()).thenReturn(true);

        Field field;
        try {
            field = DPIUtil.class.getDeclaredField("autoScaleMethod");
        } catch (NoSuchFieldException e) {
            field = null;
        }
        assumeTrue(field != null, "this SWT release has no smooth image scaling");
        // DPIUtil reads swt.autoScale.method only in its static initializer, so the mode is set here.
        scaleMethod = field;
        scaleMethod.setAccessible(true);
        savedScaleMethod = scaleMethod.get(null);
        setScaleMethod("SMOOTH");
    }

    @AfterEach
    void restore() throws Exception {
        if (scaleMethod != null) scaleMethod.set(null, savedScaleMethod);
        when(device().isAutoScalable()).thenReturn(false);
        Config.setClientDeviceZoom(100);
        restoreProperty(ConfigFlags.MODE_PROPERTY, savedMode);
        restoreProperty("swt.autoScale", savedAutoScale);
        FlutterBridge.setDisplayGcCommResolver(null);
        FlutterBridge.displayBootstrapped = bootstrapped;
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    @Test
    void an_image_drawn_at_the_raster_scale_reads_back_at_other_zooms() {
        assertThat(Config.rasterScale()).isEqualTo(1.5);
        Image image = drawnImage();
        try {
            ImageData atRaster = image.getImageData(150);
            ImageData at100 = image.getImageData(100);
            ImageData at200 = image.getImageData(200);

            assertThat(atRaster.width).as("held at the raster scale").isEqualTo(30);
            assertThat(at100.width).isEqualTo(20);
            assertThat(at100.height).isEqualTo(10);
            assertThat(at200.width).isEqualTo(40);
            assertThat(at200.height).isEqualTo(20);
            assertThat(at100.palette.getRGB(at100.getPixel(5, 5))).as("the rendered pixels").isEqualTo(RenderingComm.RENDERED);
        } finally {
            image.dispose();
        }
    }

    @Test
    void nearest_scaling_reads_a_raster_buffer_back_without_another_render() throws Exception {
        setScaleMethod("NEAREST");
        Image image = drawnImage();
        try {
            long renders = comm.renders();
            ImageData at100 = image.getImageData(100);

            assertThat(at100.width).isEqualTo(20);
            assertThat(at100.palette.getRGB(at100.getPixel(5, 5))).isEqualTo(RenderingComm.RENDERED);
            assertThat(comm.renders()).as("GC renders after the read-back").isEqualTo(renders);
        } finally {
            image.dispose();
        }
    }

    private static Image drawnImage() {
        Image image = new Image(device(), 20, 10);
        GC gc = new GC(image);
        gc.fillRectangle(0, 0, 20, 10);
        gc.dispose();
        return image;
    }

    private void setScaleMethod(String name) throws IllegalAccessException {
        for (Object constant : scaleMethod.getType().getEnumConstants()) {
            if (((Enum<?>) constant).name().equals(name)) scaleMethod.set(null, constant);
        }
    }

    /**
     * Stands in for the render side: answers a pixel read with a PNG of {@link #RENDERED} the size of
     * the buffer the last drawn image was described with. Each read here follows its own image's
     * draw, so the last description is the one being read.
     */
    private static final class RenderingComm extends RecordingComm {
        static final RGB RENDERED = new RGB(0x33, 0x66, 0xCC);
        private static final Pattern WIDTH = Pattern.compile("\"width\":(\\d+)");
        private static final Pattern HEIGHT = Pattern.compile("\"height\":(\\d+)");
        private int[] lastDescribed;

        long renders() {
            return sent.stream().filter(f -> f.event.endsWith("/imageInit")).count();
        }

        @Override
        public void send(String eventName, byte[] payload) {
            super.send(eventName, payload);
            if (eventName.endsWith("/imageInit")) {
                String text = new String(payload, StandardCharsets.UTF_8);
                lastDescribed = new int[] {lastInt(WIDTH, text), lastInt(HEIGHT, text)};
            } else if (eventName.equals("Image/requestPixels")) {
                long ref = ByteBuffer.wrap(payload).getLong();
                answer("Image/" + ref + "/pixelsResult", png(lastDescribed));
            }
        }

        private static int lastInt(Pattern pattern, String text) {
            Matcher m = pattern.matcher(text);
            int value = -1;
            while (m.find()) value = Integer.parseInt(m.group(1));
            return value;
        }

        private static byte[] png(int[] size) {
            if (size == null) return new byte[0];
            PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
            ImageData data = new ImageData(size[0], size[1], 32, palette);
            int pixel = palette.getPixel(RENDERED);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) data.setPixel(x, y, pixel);
            }
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] {data};
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            loader.save(out, SWT.IMAGE_PNG);
            return out.toByteArray();
        }

        @SuppressWarnings("unchecked")
        private void answer(String event, Object payload) {
            Consumer<Object> handler = (Consumer<Object>) handlers.get(event);
            if (handler != null) handler.accept(payload);
        }
    }
}

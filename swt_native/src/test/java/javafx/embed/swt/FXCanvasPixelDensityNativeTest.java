package javafx.embed.swt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.equo.swt.AutoScalePolicy;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.RecordingBridge;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * FXCanvas rasterises its JavaFX scene into an image and blits it into a canvas measured in points.
 * The render side composites that blit at the display's pixel density, so the raster has to carry
 * that density: one taken at the point size is magnified there, and small text and shape edges come
 * out soft and stair-stepped.
 *
 * <p>The sizes are a reported case: a 373x573-point canvas on a 125% display, in a browser tab. A
 * desktop window that rounds 125% down to 100% draws its points 1:1 on the monitor's pixels, and the
 * raster stays at the point size there.</p>
 */
@Tag("native-unit")
// OpenJFX publishes no win-aarch64 jars in any version (nor linux-aarch64 for 21.x).
@DisabledOnOs(value = { OS.LINUX, OS.WINDOWS }, architectures = "aarch64")
class FXCanvasPixelDensityNativeTest {

    private static final int CANVAS_W = 373;
    private static final int CANVAS_H = 573;
    private static final int SWATCH = 0xFF1E90FF;
    private static final int FILL = 0xFFFFFFFF;

    private RecordingBridge bridge;
    private FXCanvas canvas;
    private int deviceZoomBefore;
    private String modeBefore;
    private String autoScaleBefore;

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.vsync", "false");
        Platform.setImplicitExit(false);
        CountDownLatch up = new CountDownLatch(1);
        try {
            Platform.startup(up::countDown);
        } catch (IllegalStateException alreadyStarted) {
            up.countDown();
        }
        assertThat(up.await(20, TimeUnit.SECONDS)).as("JavaFX toolkit started").isTrue();
    }

    @BeforeEach
    void setUp() {
        deviceZoomBefore = DPIUtil.getDeviceZoom();
        modeBefore = System.getProperty(ConfigFlags.MODE_PROPERTY);
        autoScaleBefore = System.getProperty("swt.autoScale");
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        System.clearProperty("swt.autoScale");
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        Display display = DartMocks.dartDisplay();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Shell shell = DartMocks.dartShell(display);
        canvas = new FXCanvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, CANVAS_W, CANVAS_H);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        canvas.setScene(null);
        onFxThread(() -> { });
        DPIUtil.setDeviceZoom(deviceZoomBefore);
        Config.setClientDeviceZoom(100);
        restoreProperty(ConfigFlags.MODE_PROPERTY, modeBefore);
        restoreProperty("swt.autoScale", autoScaleBefore);
        FlutterBridge.set(null);
    }

    private static void restoreProperty(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    private static void displayAt(int zoom) {
        DPIUtil.setDeviceZoom(zoom);
        Config.setClientDeviceZoom(zoom);
    }

    @Test
    @DisplayName("on a 125% display the scene is rasterised at 125% of the canvas")
    void aScaledDisplayRasterisesAtItsPixelDensity() throws InterruptedException {
        displayAt(125);

        int[] blit = renderAndBlit();

        assertThat(blit).as("frame drawn into the canvas: src w,h -> dest w,h")
                .containsExactly(466, 716, CANVAS_W, CANVAS_H);
        // The swatch is centred and half the canvas: x 116.5..349.5 once scaled, not 93..280.
        assertThat(frameArgb(233, 358)).as("swatch centre").isEqualTo(SWATCH);
        assertThat(frameArgb(345, 358)).as("inside the swatch's scaled right edge").isEqualTo(SWATCH);
        assertThat(frameArgb(355, 358)).as("past the swatch's scaled right edge").isEqualTo(FILL);
        assertThat(frameArgb(463, 713)).as("scene fill in the far corner").isEqualTo(FILL);
    }

    @Test
    @DisplayName("a desktop window that rounds 125% down to 100% keeps the raster at the canvas size")
    void aDesktopWindowAtAnIntegerZoomRasterisesAtThePointSize() throws InterruptedException {
        ConfigFlags.setMode(ConfigFlags.MODE_DESKTOP);
        int[][] blit = new int[1][];
        AutoScalePolicy.runWith("integer", () -> {
            displayAt(125);
            try {
                blit[0] = renderAndBlit();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });

        assertThat(blit[0]).containsExactly(CANVAS_W, CANVAS_H, CANVAS_W, CANVAS_H);
    }

    @Test
    @DisplayName("on an unscaled display the scene is rasterised at the canvas size")
    void anUnscaledDisplayRasterisesAtThePointSize() throws InterruptedException {
        displayAt(100);

        int[] blit = renderAndBlit();

        assertThat(blit).containsExactly(CANVAS_W, CANVAS_H, CANVAS_W, CANVAS_H);
    }

    /**
     * Shows a scene, lets the first paint size the scene and render it on the FX thread, then paints
     * again and returns the blit: the frame's width and height, and the canvas rectangle it lands in.
     */
    private int[] renderAndBlit() throws InterruptedException {
        Rectangle swatch = new Rectangle(CANVAS_W / 2.0, CANVAS_H / 2.0, Color.web("#1e90ff"));
        canvas.setScene(new Scene(new StackPane(swatch), CANVAS_W, CANVAS_H, Color.WHITE));
        onFxThread(() -> { });
        paint();
        onFxThread(() -> { });
        bridge.comm.sent.clear();
        paint();
        List<int[]> blits = blits();
        assertThat(blits).as("drawImage calls of the second paint").hasSize(1);
        return blits.get(0);
    }

    private void paint() {
        GC gc = new GC(canvas);
        try {
            Event e = new Event();
            e.gc = gc;
            e.width = CANVAS_W;
            e.height = CANVAS_H;
            e.widget = canvas;
            e.display = canvas.getDisplay();
            e.type = SWT.Paint;
            // This harness does not dispatch events to listeners, so they are called directly.
            for (Listener listener : canvas.getListeners(SWT.Paint)) {
                listener.handleEvent(e);
            }
        } finally {
            gc.dispose();
        }
    }

    private int frameArgb(int x, int y) {
        synchronized (canvas) {
            return canvas.framePixels[y * canvas.frameWidth + x];
        }
    }

    /** srcWidth, srcHeight, destWidth, destHeight of every recorded nine-argument drawImage. */
    private List<int[]> blits() {
        List<int[]> blits = new ArrayList<>();
        for (String[] entry : BatchEntries.of(bridge.comm.sent)) {
            if (!entry[0].startsWith("GC/") || !entry[0].contains("/drawImage")) {
                continue;
            }
            JsonElement body = JsonParser.parseString(entry[1]);
            JsonArray args = body.isJsonArray() ? body.getAsJsonArray() : null;
            if (args == null || args.size() != 9) {
                continue;
            }
            blits.add(new int[] {
                    args.get(3).getAsInt(), args.get(4).getAsInt(),
                    args.get(7).getAsInt(), args.get(8).getAsInt() });
        }
        return blits;
    }

    private static void onFxThread(Runnable work) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                work.run();
            } finally {
                done.countDown();
            }
        });
        assertThat(done.await(20, TimeUnit.SECONDS)).as("FX thread drained").isTrue();
    }
}

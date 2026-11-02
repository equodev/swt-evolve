package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

/**
 * A framed trim toolbar is a Canvas that hosts the toolbar and paints a drag handle in the strip
 * beside it. Its paint listener blits a frame image piece by piece, and when the theme supplies
 * only the handle the frame image is null and every piece has an empty source: those blits must
 * return before the image is checked, and the handle blit after them must reach Flutter.
 */
class FramedToolBarHandlePaintNativeTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeEach
    void setUp() {
        Config.forceEquo();
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    private String wire() {
        return String.join(" ", GcFrames.channels(bridge.comm.sent));
    }

    @Test
    @DisplayName("the handle blit of a frame with no frame image reaches Flutter")
    void handleBlitReachesFlutter() {
        Shell shell = new Shell(display);
        shell.setSize(400, 200);
        Image handle = new Image(display, new ImageData(5, 20, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        Canvas frame = new Canvas(shell, SWT.NONE);
        ToolBar bar = new ToolBar(shell, SWT.FLAT);
        new ToolItem(bar, SWT.PUSH).setText("A");
        frame.addPaintListener(e -> {
            Image frameImage = null;
            Point inner = bar.getSize();
            e.gc.drawImage(frameImage, 0, 0, 0, 0, 0, 0, 0, 0);
            e.gc.drawImage(frameImage, 0, 0, 0, 0, 0, 0, inner.x + 5, 0);
            e.gc.drawImage(frameImage, 0, 0, 0, 0, 0, 0, 0, inner.y);
            Rectangle b = handle.getBounds();
            e.gc.drawImage(handle, 0, 0, b.width, b.height, 0, (inner.y - b.height) / 2, b.width, b.height);
        });
        bar.setParent(frame);
        bar.pack(true);
        bar.setLocation(5, 0);
        frame.setSize(5 + bar.getSize().x, bar.getSize().y);
        while (display.readAndDispatch()) {
        }
        FlutterBridge.update();
        bridge.comm.sent.clear();

        ControlHelper.paint((DartControl) frame.getImpl(), null);
        while (display.readAndDispatch()) {
        }
        FlutterBridge.update();

        String wire = wire();
        int dy = (bar.getSize().y - 20) / 2;
        assertThat(wire).as("the paint ran and its GC was disposed").contains("/gcDispose");
        List<List<?>> blits = GcFrames.argsOf(bridge.comm.sent, "drawImageImageintintintintintintintint");
        assertThat(blits).as("exactly one blit: the handle").hasSize(1);
        assertThat(WireValues.same(blits.get(0).subList(1, 9), List.of(0, 0, 5, 20, 0, dy, 5, 20)))
                .as("the handle at its own size in the strip").isTrue();
        handle.dispose();
    }
}

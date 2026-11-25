package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A workbench's trim toolbars are framed by Canvases that all paint the one shared drag-handle
 * image. Each frame's paint must put its own blit of the handle on the wire.
 */
class TrimHandleGripPaintNativeTest {

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

    private Canvas frame(Composite trim, Image handle, int x) {
        Canvas frame = new Canvas(trim, SWT.NONE);
        ToolBar bar = new ToolBar(frame, SWT.FLAT);
        new ToolItem(bar, SWT.PUSH).setText("A");
        frame.addPaintListener(e -> {
            Point inner = bar.getSize();
            e.gc.drawImage(handle, 0, 0, 5, 20, 0, (inner.y - 20) / 2, 5, 20);
        });
        bar.pack(true);
        bar.setLocation(5, 0);
        frame.setBounds(x, 0, 5 + bar.getSize().x, bar.getSize().y);
        return frame;
    }

    private void settle() {
        while (display.readAndDispatch()) {
        }
        FlutterBridge.update();
    }

    @Test
    @DisplayName("every frame sharing the handle image blits it")
    void everyFrameBlitsTheSharedHandle() {
        Shell shell = new Shell(display);
        shell.setSize(600, 200);
        Composite trim = new Composite(shell, SWT.NONE);
        trim.setBounds(0, 0, 600, 40);
        Image handle = new Image(display, new ImageData(5, 20, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF)));
        List<Canvas> frames = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            frames.add(frame(trim, handle, i * 100));
        settle();
        bridge.comm.sent.clear();

        for (Canvas frame : frames)
            ControlHelper.paint((DartControl) frame.getImpl(), null);
        settle();

        List<List<?>> blits = GcFrames.argsOf(bridge.comm.sent, "drawImageImageintintintintintintintint");
        assertThat(blits).as("one handle blit per frame").hasSize(frames.size());
        handle.dispose();
    }
}

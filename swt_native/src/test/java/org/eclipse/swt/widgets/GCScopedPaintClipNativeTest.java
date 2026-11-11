package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A paint confined to a damaged area hands out a GC clipped to it, as the platforms do below SWT: a clip
 * the handler resets, or widens, still stays inside the damage, and is not one the application set. A GC on
 * the display has the display's area to draw in, which Cocoa reports as empty.
 */
class GCScopedPaintClipNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        Config.forceEquo();
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
        Config.defaultToEclipse();
    }

    @Test
    @DisplayName("a clip reset inside a scoped paint stays inside the damage, and is not reported as a clip")
    void clipResetInsideAScopedPaint() {
        Shell shell = new Shell(display);
        shell.setSize(300, 200);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 200, 100);
        while (display.readAndDispatch()) {
        }
        List<Rectangle> clips = new ArrayList<>();
        List<Boolean> clipped = new ArrayList<>();
        canvas.addPaintListener(e -> {
            e.gc.setClipping((Rectangle) null);
            clips.add(e.gc.getClipping());
            clipped.add(e.gc.isClipped());
            e.gc.setClipping(0, 0, 500, 500);
            clips.add(e.gc.getClipping());
            e.gc.setClipping(3, 4, 2, 2);
            clipped.add(e.gc.isClipped());
        });

        ControlHelper.paint((DartControl) canvas.getImpl(), 2, 3, 10, 10);
        while (display.readAndDispatch()) {
        }

        assertThat(clips).containsExactly(new Rectangle(2, 3, 10, 10), new Rectangle(2, 3, 10, 10));
        assertThat(clipped).as("a clip inside the damage is one the application set").containsExactly(false, true);
    }

    @Test
    @DisplayName("a wait for the render side pumps the real display, and its deadline wakes it")
    void waitsPumpTheDisplay() {
        String mode = dev.equo.swt.ConfigFlags.mode();
        dev.equo.swt.ConfigFlags.setMode(dev.equo.swt.ConfigFlags.MODE_DESKTOP);
        try {
            boolean[] ran = new boolean[1];
            display.asyncExec(() -> ran[0] = true);
            assertThat(org.eclipse.swt.graphics.GCHelper.fetchRemotePixels(display, new dev.equo.swt.harness.RecordingComm(), 21, 50)).isNull();
            assertThat(ran[0]).as("pending work ran while waiting").isTrue();
            Canvas canvas = new Canvas(new Shell(display), SWT.NONE);
            display.asyncExec(() -> ran[0] = false);
            org.eclipse.swt.graphics.GCHelper.callOnDisplayBytes(canvas.getImpl(), dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT, dev.equo.swt.WireTable.CHANNEL_GET_ITEM_HEIGHT_RESPONSE, null, bytes -> {
            }, 50);
            assertThat(ran[0]).isFalse();
            org.eclipse.swt.graphics.Image image = new org.eclipse.swt.graphics.Image(display, 2, 2);
            java.util.concurrent.CompletableFuture<Void> render = new java.util.concurrent.CompletableFuture<>();
            ((org.eclipse.swt.graphics.DartImage) image.getImpl())._pendingRenderFuture(render);
            // The render's answer arrives on the comm thread; the read runs no application code
            // while it waits, so an asyncExec would only complete it at the wait's deadline.
            java.util.concurrent.CompletableFuture.runAsync(() -> render.complete(null),
                    java.util.concurrent.CompletableFuture.delayedExecutor(50, java.util.concurrent.TimeUnit.MILLISECONDS));
            long start = System.nanoTime();
            assertThat(image.getImageData().width).as("a read waits for the render").isEqualTo(2);
            assertThat(render).as("the read returned once the render completed").isDone();
            assertThat(System.nanoTime() - start).as("and not at its deadline")
                    .isLessThan(java.util.concurrent.TimeUnit.SECONDS.toNanos(10));
            image.dispose();
        } finally {
            if (mode == null)
                System.clearProperty(dev.equo.swt.ConfigFlags.MODE_PROPERTY);
            else
                dev.equo.swt.ConfigFlags.setMode(mode);
        }
    }

    @Test
    @DisplayName("a GC on the display reports the display's area, as on GTK and Win32, and takes a clip inside it")
    void displayGcReportsTheDisplayArea() {
        GC gc = new GC(display);
        Rectangle area = display.getBounds();

        assertThat(gc.getClipping()).isEqualTo(area);
        gc.setClipping(1, 2, 3, 4);
        assertThat(gc.getClipping()).isEqualTo(area.intersection(new Rectangle(1, 2, 3, 4)));
        gc.dispose();
    }
}

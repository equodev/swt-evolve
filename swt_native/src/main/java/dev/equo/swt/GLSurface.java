package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;

/**
 * The OpenGL context behind a Flutter-drawn {@link GLCanvas}. The application draws into a native
 * context sized to the canvas; each {@code swapBuffers()} reads the frame back and the canvas paints
 * it, so the client shows it like any other drawing. Where the platform has no OpenGL the canvas
 * keeps no context and draws nothing.
 */
public final class GLSurface {

    private final GLCanvas canvas;
    private long handle;
    private int width = 1;
    private int height = 1;
    private int[] frame;
    private int frameWidth;
    private int frameHeight;
    // A frame waits for the paint its redraw asked for; later swaps replace it instead of queueing more.
    private boolean framePending;

    /** {@code shared} is the surface of {@code data.shareContext}, or null. */
    public GLSurface(GLCanvas canvas, GLData data, GLSurface shared) {
        this.canvas = canvas;
        if (!GLNative.isAvailable())
            return;
        long created;
        try {
            created = GLNative.create(glData(data), shared == null ? 0 : shared.handle, width, height);
        } catch (LinkageError e) {
            // A bridge built before GL contexts existed: behave as a platform without them.
            return;
        }
        if (created < 0) {
            GLNative.logNoContext(created);
            return;
        }
        if (created == 0)
            SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
        handle = created;
        canvas.addListener(SWT.Resize, e -> resize());
        canvas.addListener(SWT.Paint, e -> paint(e.gc));
        canvas.addListener(SWT.Dispose, e -> dispose());
        resize();
    }

    private static int[] glData(GLData data) {
        int[] values = new int[GLNative.GL_DATA_LENGTH];
        values[GLNative.DOUBLE_BUFFER] = data.doubleBuffer ? 1 : 0;
        values[GLNative.STEREO] = data.stereo ? 1 : 0;
        values[GLNative.RED_SIZE] = data.redSize;
        values[GLNative.GREEN_SIZE] = data.greenSize;
        values[GLNative.BLUE_SIZE] = data.blueSize;
        values[GLNative.ALPHA_SIZE] = data.alphaSize;
        values[GLNative.DEPTH_SIZE] = data.depthSize;
        values[GLNative.STENCIL_SIZE] = data.stencilSize;
        values[GLNative.ACCUM_RED_SIZE] = data.accumRedSize;
        values[GLNative.ACCUM_GREEN_SIZE] = data.accumGreenSize;
        values[GLNative.ACCUM_BLUE_SIZE] = data.accumBlueSize;
        values[GLNative.ACCUM_ALPHA_SIZE] = data.accumAlphaSize;
        values[GLNative.SAMPLE_BUFFERS] = data.sampleBuffers;
        values[GLNative.SAMPLES] = data.samples;
        return values;
    }

    public void setCurrent() {
        if (handle == 0)
            return;
        resize();
        GLNative.makeCurrent(handle);
    }

    public boolean isCurrent() {
        return handle != 0 && GLNative.isCurrent(handle);
    }

    public void swapBuffers() {
        if (handle == 0)
            return;
        int w = width, h = height;
        if (frame == null || frame.length != w * h)
            frame = new int[w * h];
        if (!GLNative.swap(handle, frame, w, h))
            return;
        frameWidth = w;
        frameHeight = h;
        if (!framePending) {
            framePending = true;
            canvas.redraw();
        }
    }

    /** Whether a swapped frame still waits for the Paint that shows it. */
    public boolean framePending() {
        return framePending;
    }

    private void resize() {
        if (handle == 0 || canvas.isDisposed())
            return;
        Rectangle area = canvas.getClientArea();
        int w = Math.max(1, area.width), h = Math.max(1, area.height);
        if (w == width && h == height)
            return;
        if (!GLNative.setSize(handle, w, h))
            return;
        width = w;
        height = h;
    }

    /** The last swapped frame, top row first, or null before the first swap. */
    public ImageData frameData() {
        if (frame == null || frameWidth == 0)
            return null;
        ImageData imageData = new ImageData(frameWidth, frameHeight, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        imageData.setPixels(0, 0, frameWidth * frameHeight, frame, 0);
        return imageData;
    }

    private void paint(GC gc) {
        framePending = false;
        ImageData imageData = frameData();
        if (imageData == null)
            return;
        Image image = new Image(canvas.getDisplay(), imageData);
        try {
            gc.drawImage(image, 0, 0);
        } finally {
            image.dispose();
        }
    }

    private void dispose() {
        if (handle == 0)
            return;
        GLNative.destroy(handle);
        handle = 0;
        frame = null;
    }
}

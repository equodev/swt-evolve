package org.eclipse.swt.opengl;

import dev.equo.swt.GLSurface;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.assertj.core.api.Assertions.assertThat;

/** What an application draws into a GLCanvas and swaps is what the client shows for that canvas. */
@Tag("flutter-it")
class GLCanvasRenderFlutterTest {

    static {
        GLTestContexts.init();
    }

    private static final int W = 80;
    private static final int H = 60;

    private final WebDisplayHarness flutter = new WebDisplayHarness();

    @AfterEach
    void tearDown() {
        try {
            GLTestContexts.clear();
        } finally {
            flutter.teardown();
        }
    }

    @Test
    @Timeout(60)
    void theClientShowsWhatWasDrawnAndSwapped() {
        Display display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setBounds(0, 0, 200, 150);
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        GLCanvas canvas = new GLCanvas(shell, SWT.NONE, data);
        canvas.setBounds(10, 10, W, H);
        shell.open();
        flutter.settle();

        canvas.setCurrent();
        assertThat(canvas.isCurrent()).as("isCurrent() after setCurrent()").isTrue();
        GL.createCapabilities();
        GL11.glViewport(0, 0, W, H);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, H / 2, W, H - H / 2);
        GL11.glClearColor(1f, 0f, 0f, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glScissor(0, 0, W, H / 2);
        GL11.glClearColor(0f, 0f, 1f, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        canvas.swapBuffers();
        // The swap's frame reaches the client with the Paint it asks for, which paint pacing may hold
        // until the client answers for the canvas's previous Paint.
        GLSurface surface = ((DartGLCanvas) canvas.getImpl()).glSurface;
        while (surface.framePending())
            if (!display.readAndDispatch())
                display.sleep();
        flutter.settle();

        ImageData shown = shown(display, canvas);
        assertThat(rgb(shown, W / 2, H / 4)).as("the canvas's top half as the client shows it, RGB")
                .containsExactly(255, 0, 0);
        assertThat(rgb(shown, W / 2, 3 * H / 4)).as("the canvas's bottom half as the client shows it, RGB")
                .containsExactly(0, 0, 255);
    }

    @Test
    @Timeout(60)
    void disposingTheCanvasReleasesItsContext() {
        Display display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setBounds(0, 0, 200, 150);
        GLCanvas canvas = new GLCanvas(shell, SWT.NONE, new GLData());
        canvas.setBounds(10, 10, W, H);
        shell.open();
        flutter.settle();
        canvas.setCurrent();
        assertThat(canvas.isCurrent()).as("isCurrent() after setCurrent()").isTrue();
        GLSurface surface = ((DartGLCanvas) canvas.getImpl()).glSurface;

        canvas.dispose();

        assertThat(surface.isCurrent()).as("the disposed canvas's surface still holds a context").isFalse();
        assertThat(GLTestContexts.current()).as("the thread's current OpenGL context").isZero();
    }

    private static int[] rgb(ImageData pixels, int x, int y) {
        RGB rgb = pixels.palette.getRGB(pixels.getPixel(x, y));
        return new int[] {rgb.red, rgb.green, rgb.blue};
    }

    private static ImageData shown(Display display, GLCanvas canvas) {
        Image shown = new Image(display, W, H);
        GC gc = new GC(canvas);
        try {
            gc.copyArea(shown, 0, 0);
            return shown.getImageData();
        } finally {
            gc.dispose();
            shown.dispose();
        }
    }
}

package org.eclipse.swt.opengl;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.GLSurface;
import dev.equo.swt.harness.RecordingBridge;
import java.nio.ByteBuffer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.lwjgl.BufferUtils;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.lwjgl.opengl.EXTFramebufferBlit;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A GLCanvas gives the application an OpenGL context to draw into: {@code setCurrent()} makes it
 * current on the calling thread, and what is drawn there lands in a framebuffer the context can read
 * back.
 */
@Tag("native-unit")
class GLCanvasContextNativeTest {

    private static final int W = 64;
    private static final int H = 48;

    private Shell shell;
    private GLCanvas canvas;

    @BeforeEach
    void setUp() {
        GLTestContexts.init();
        FlutterBridge.set(new RecordingBridge());
        Display display = DartMocks.dartDisplay();
        shell = DartMocks.dartShell(display);
        canvas = newCanvas(null);
        canvas.setBounds(0, 0, W, H);
    }

    private GLCanvas newCanvas(GLCanvas share) {
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        data.shareContext = share;
        return new GLCanvas(shell, SWT.NONE, data);
    }

    private static GLSurface surfaceOf(GLCanvas canvas) {
        return GLTestContexts.surfaceOf(canvas);
    }

    private static void clear(int x, int y, int w, int h, float r, float g, float b) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
        GL11.glClearColor(r, g, b, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private static int[] rgb(ImageData frame, int x, int y) {
        RGB rgb = frame.palette.getRGB(frame.getPixel(x, y));
        return new int[] {rgb.red, rgb.green, rgb.blue};
    }

    @AfterEach
    void tearDown() {
        GLTestContexts.clear();
        FlutterBridge.set(null);
    }

    @Test
    void setCurrentMakesTheCanvasContextCurrent() {
        canvas.setCurrent();

        assertThat(canvas.isCurrent()).as("isCurrent() after setCurrent()").isTrue();
        assertThat(GLTestContexts.current()).as("the thread's current OpenGL context").isNotZero();
    }

    @Test
    void aClearDrawnAfterSetCurrentIsReadBackFromTheContext() {
        canvas.setCurrent();
        assertThat(canvas.isCurrent()).as("isCurrent() after setCurrent()").isTrue();
        GL.createCapabilities();

        GL11.glViewport(0, 0, W, H);
        GL11.glClearColor(1f, 0f, 0f, 1f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        GL11.glReadPixels(W / 2, H / 2, 1, 1, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixel);

        assertThat(GL11.glGetError()).as("glGetError after clearing and reading back").isEqualTo(GL11.GL_NO_ERROR);
        assertThat(new int[] {pixel.get(0) & 0xFF, pixel.get(1) & 0xFF, pixel.get(2) & 0xFF})
                .as("the pixel at the canvas centre, RGB").containsExactly(255, 0, 0);
    }

    @Test
    void theSwappedFrameIsTopRowFirstAndLeavesTheApplicationsReadStateAlone() {
        canvas.setCurrent();
        GL.createCapabilities();
        // The application's own framebuffer, bound for reading, and a pack alignment the swap must not use.
        int fbo = EXTFramebufferObject.glGenFramebuffersEXT();
        int colour = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, colour);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL11.GL_RGBA8, W, H);
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, colour);
        clear(0, 0, W, H, 1f, 1f, 1f);
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferBlit.GL_DRAW_FRAMEBUFFER_EXT, 0);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glViewport(0, 0, W, H);
        clear(0, H / 2, W, H - H / 2, 1f, 0f, 0f);
        clear(0, 0, W, H / 2, 0f, 0f, 1f);

        canvas.swapBuffers();

        ImageData frame = surfaceOf(canvas).frameData();
        assertThat(frame).as("the swapped frame").isNotNull();
        assertThat(rgb(frame, W / 2, 0)).as("the frame's top row, RGB").containsExactly(255, 0, 0);
        assertThat(rgb(frame, W / 2, H - 1)).as("the frame's bottom row, RGB").containsExactly(0, 0, 255);
        assertThat(GL11.glGetError()).as("glGetError after the swap").isEqualTo(GL11.GL_NO_ERROR);
        assertThat(GL11.glGetInteger(EXTFramebufferBlit.GL_READ_FRAMEBUFFER_BINDING_EXT))
                .as("the application's read framebuffer after the swap").isEqualTo(fbo);
        assertThat(GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT)).as("GL_PACK_ALIGNMENT after the swap").isEqualTo(1);
    }

    @Test
    void afterAResizeTheFrameHasTheNewSize() {
        canvas.setBounds(0, 0, W / 2, H / 3);
        canvas.setCurrent();
        GL.createCapabilities();
        GL11.glViewport(0, 0, W / 2, H / 3);
        clear(0, 0, W / 2, H / 3, 0f, 1f, 0f);

        canvas.swapBuffers();

        ImageData frame = surfaceOf(canvas).frameData();
        assertThat(new int[] {frame.width, frame.height}).as("the frame's size").containsExactly(W / 2, H / 3);
        assertThat(rgb(frame, W / 2 - 1, H / 3 - 1)).as("the frame's bottom-right pixel, RGB").containsExactly(0, 255, 0);
        assertThat(GL11.glGetError()).as("glGetError after the swap").isEqualTo(GL11.GL_NO_ERROR);
    }

    @Test
    void aSharedContextSeesTheTexturesOfTheOther() {
        canvas.setCurrent();
        GL.createCapabilities();
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        ByteBuffer green = BufferUtils.createByteBuffer(4).put(new byte[] {0, (byte) 255, 0, (byte) 255});
        green.flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 1, 1, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, green);
        GL11.glFlush();

        GLCanvas second = newCanvas(canvas);
        second.setBounds(0, 0, W, H);
        second.setCurrent();
        assertThat(second.isCurrent()).as("the second canvas is current").isTrue();
        GL.createCapabilities();
        ByteBuffer read = BufferUtils.createByteBuffer(4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, read);

        assertThat(GL11.glIsTexture(texture)).as("the first canvas's texture, seen from the second").isTrue();
        assertThat(new int[] {read.get(0) & 0xFF, read.get(1) & 0xFF, read.get(2) & 0xFF})
                .as("the shared texture's pixel, RGB").containsExactly(0, 255, 0);
    }
}

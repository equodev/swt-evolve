package dev.equo.opengl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * A GLCanvas animated the way a 3D view drives one: each tick makes the canvas current, clears it,
 * draws a rotating triangle with LWJGL and swaps. The triangle must turn smoothly and follow the
 * canvas when the shell is resized. Where the machine has no OpenGL (no driver, no X display) the
 * canvas has no context and the snippet says so and exits.
 *
 * <pre>
 * ./gradlew :swt-evolve:examples:runWebExample -PmainClass=dev.equo.opengl.GLCanvasSnippet
 * </pre>
 */
public class GLCanvasSnippet {

    private static final int FRAME_MS = 16;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GLCanvas");
        shell.setLayout(new FillLayout());
        GLData data = new GLData();
        data.doubleBuffer = true;
        data.depthSize = 24;
        GLCanvas canvas = new GLCanvas(shell, SWT.NONE, data);
        canvas.setCurrent();
        if (!canvas.isCurrent()) {
            System.out.println("GLCanvas has no OpenGL context on this machine.");
            display.dispose();
            return;
        }
        GL.createCapabilities();

        float[] angle = {0};
        Runnable frame = new Runnable() {
            @Override
            public void run() {
                if (canvas.isDisposed())
                    return;
                Rectangle area = canvas.getClientArea();
                canvas.setCurrent();
                GL11.glViewport(0, 0, area.width, area.height);
                GL11.glClearColor(0.1f, 0.2f, 0.3f, 1f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
                GL11.glRotatef(angle[0], 0f, 0f, 1f);
                GL11.glBegin(GL11.GL_TRIANGLES);
                GL11.glColor3f(1f, 0f, 0f);
                GL11.glVertex2f(0f, 0.8f);
                GL11.glColor3f(0f, 1f, 0f);
                GL11.glVertex2f(-0.7f, -0.6f);
                GL11.glColor3f(0f, 0f, 1f);
                GL11.glVertex2f(0.7f, -0.6f);
                GL11.glEnd();
                canvas.swapBuffers();
                angle[0] = (angle[0] + 2f) % 360f;
                display.timerExec(FRAME_MS, this);
            }
        };

        shell.setSize(480, 360);
        shell.open();
        display.timerExec(FRAME_MS, frame);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}

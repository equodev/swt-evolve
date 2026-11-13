package org.eclipse.swt.opengl;

import dev.equo.swt.GLNative;
import dev.equo.swt.GLSurface;
import org.lwjgl.opengl.CGL;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLX;
import org.lwjgl.opengl.WGL;
import org.lwjgl.system.Platform;

/** The calling thread's OpenGL context, read through the platform's own API rather than the bridge. */
final class GLTestContexts {

    static {
        // The suites run with dev.equo.swt.loadLibrary=false; GL contexts need the native bridge.
        System.setProperty("dev.equo.swt.gl.loadLibrary", "true");
    }

    private GLTestContexts() {
    }

    /** Loads this class, and with it the property above, before the first GLCanvas is created. */
    static void init() {
    }

    static long current() {
        switch (Platform.get()) {
            case MACOSX:
                return CGL.CGLGetCurrentContext();
            case WINDOWS:
                return WGL.wglGetCurrentContext();
            default:
                return GLX.glXGetCurrentContext();
        }
    }

    static void clear() {
        GL.setCapabilities(null);
        if (GLNative.isAvailable())
            GLNative.makeCurrent(0);
    }

    static GLSurface surfaceOf(GLCanvas canvas) {
        return ((DartGLCanvas) canvas.getImpl()).glSurface;
    }
}

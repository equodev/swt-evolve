package dev.equo.swt;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JNI boundary for {@code GLCanvas} contexts, implemented by each platform's native bridge
 * (NSOpenGL on macOS, GLX on Linux, WGL on Windows). Each handle owns an OpenGL context drawing into
 * an off-screen drawable, so the application's GL code gets a default framebuffer while the client
 * paints the frames read back by {@link #swap}.
 */
public final class GLNative {

    /** What {@link #create} returns when the main thread didn't create the drawable in time (macOS). */
    public static final long MAIN_THREAD_TIMEOUT = -1;
    /** What {@link #create} returns when this machine has no OpenGL to create a context with. */
    public static final long NO_GL = -2;

    // The GLData fields, in the order create() takes them; each bridge maps them the way SWT's own
    // GLCanvas does on that platform.
    public static final int DOUBLE_BUFFER = 0;
    public static final int STEREO = 1;
    public static final int RED_SIZE = 2;
    public static final int GREEN_SIZE = 3;
    public static final int BLUE_SIZE = 4;
    public static final int ALPHA_SIZE = 5;
    public static final int DEPTH_SIZE = 6;
    public static final int STENCIL_SIZE = 7;
    public static final int ACCUM_RED_SIZE = 8;
    public static final int ACCUM_GREEN_SIZE = 9;
    public static final int ACCUM_BLUE_SIZE = 10;
    public static final int ACCUM_ALPHA_SIZE = 11;
    public static final int SAMPLE_BUFFERS = 12;
    public static final int SAMPLES = 13;
    public static final int GL_DATA_LENGTH = 14;

    private static final boolean AVAILABLE = load();
    private static final AtomicBoolean TIMEOUT_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean NO_GL_LOGGED = new AtomicBoolean();

    private GLNative() {
    }

    private static boolean load() {
        // Like the rest of the bridge, honour loadLibrary=false (headless web suites); GL tests opt back in
        // with dev.equo.swt.gl.loadLibrary=true.
        if ("false".equals(System.getProperty("dev.equo.swt.loadLibrary"))
                && !Boolean.getBoolean("dev.equo.swt.gl.loadLibrary"))
            return false;
        try {
            FlutterLibraryLoader.initialize();
            return true;
        } catch (Throwable e) {
            log().log(Level.WARNING, "GLCanvas: the native bridge is not available, nothing will be drawn", e);
            return false;
        }
    }

    private static Logger log() {
        return Logger.getLogger(GLNative.class.getName());
    }

    /** Logs, once per process, why a GLCanvas got no context: {@code code} is what {@link #create} returned. */
    public static void logNoContext(long code) {
        if (code == MAIN_THREAD_TIMEOUT && TIMEOUT_LOGGED.compareAndSet(false, true))
            log().warning("GLCanvas: the main thread did not create the OpenGL drawable in time, "
                    + "the canvas will draw nothing (is the main thread running its event loop?)");
        else if (code == NO_GL && NO_GL_LOGGED.compareAndSet(false, true))
            log().warning("GLCanvas: no OpenGL is available here (on Linux GLX needs an X display), "
                    + "the canvas will draw nothing");
    }

    /** Whether the native bridge is loaded, so GL contexts can be asked for. */
    public static boolean isAvailable() {
        return AVAILABLE;
    }

    /**
     * Creates a context for {@code glData} ({@link #GL_DATA_LENGTH} values indexed by the constants
     * above), sharing objects with {@code share} when it is not 0. Returns 0 when no pixel format
     * matches, {@link #MAIN_THREAD_TIMEOUT} or {@link #NO_GL} when no context could be made at all.
     */
    public static long create(int[] glData, long share, int width, int height) {
        return Create(glData, share, width, height);
    }

    /** Returns false when the drawable could not be resized. */
    public static boolean setSize(long handle, int width, int height) {
        return SetSize(handle, width, height);
    }

    /** Makes the context current on the calling thread; 0 clears the thread's context. */
    public static void makeCurrent(long handle) {
        MakeCurrent(handle);
    }

    public static boolean isCurrent(long handle) {
        return IsCurrent(handle);
    }

    /**
     * Reads the frame about to be swapped into {@code pixels} ({@code width * height} 0xAARRGGBB
     * values, top row first) and swaps. Returns whether {@code pixels} was filled.
     */
    public static boolean swap(long handle, int[] pixels, int width, int height) {
        return Swap(handle, pixels, width, height);
    }

    public static void destroy(long handle) {
        Destroy(handle);
    }

    private static native long Create(int[] glData, long share, int width, int height);

    private static native boolean SetSize(long handle, int width, int height);

    private static native void MakeCurrent(long handle);

    private static native boolean IsCurrent(long handle);

    private static native boolean Swap(long handle, int[] pixels, int width, int height);

    private static native void Destroy(long handle);
}

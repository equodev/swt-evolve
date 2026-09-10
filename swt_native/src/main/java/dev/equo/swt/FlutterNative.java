package dev.equo.swt;

/**
 * The JNI boundary to the Flutter native bridge ({@code flutter_bridge.swift} /
 * {@code flutter_bridge.cpp} / {@code flutter_bridge.cc}), plus loading of that native library.
 *
 * <p>It is a plain static utility — <em>not</em> a base class — so both kinds of bridge can use it
 * by composition: {@link org.eclipse.swt.widgets.EmbeddedBridge} (one embedded Flutter view per
 * native SWT widget) and {@code DeskDisplayBridge} (one native top-level window hosting the whole
 * Dart Display). A "surface" is whichever the native side created; the same {@link #dispose},
 * {@link #setBounds}, {@link #pump} and friends operate on either, dispatching on its kind natively.
 */
public final class FlutterNative {

    static {
        if (!"false".equals(System.getProperty("dev.equo.swt.loadLibrary")))
            FlutterLibraryLoader.initialize();
    }

    private FlutterNative() {
    }

    /**
     * {@link #pump} status: the user asked the window to close (title-bar X, Alt+F4, Cmd+W) and the
     * runner <em>vetoed</em> the OS teardown, so the window is still up. The bridge must answer with
     * SWT's close contract — {@code SWT.Close}, then dispose only if no listener set
     * {@code doit = false}. Reported once per gesture. Distinct from a negative status meaning the
     * window is already gone, which admits no veto.
     */
    public static final int PUMP_CLOSE_REQUESTED = -2;

    /**
     * Creates a Flutter surface and returns its native handle.
     * <ul>
     *   <li>{@code width > 0 && height > 0} → a top-level <b>window</b> hosting the whole Display
     *       (desktop-native). {@code parent} is ignored; {@code widgetId} is the Display id.</li>
     *   <li>otherwise → an <b>embedded</b> view inside {@code parent} (or an offscreen view if
     *       {@code parent == 0}, the limbo case).</li>
     * </ul>
     */
    public static long initialize(int port, long parent, long widgetId, String widgetName,
                                  String theme, int backgroundColor, int parentBackgroundColor,
                                  int width, int height) {
        // A frameless/undecorated window is only usable when Flutter draws the replacement
        // chrome, so the native side must not strip the OS one when CSD is off -- that would
        // leave a window with no title bar, no buttons and nothing to drag.
        boolean csdEnabled = !"false".equals(Config.getConfigFlags().csd_placement);
        return Initialize(port, parent, widgetId, widgetName, theme,
                backgroundColor, parentBackgroundColor, width, height, csdEnabled);
    }

    /** Native view handle of a surface: the embedded view, or a window's content view. */
    public static long getView(long context) {
        return GetView(context);
    }

    /** Disposes a surface — an embedded view or a top-level window. */
    public static void dispose(long context) {
        Dispose(context);
    }

    /**
     * Sets a surface's bounds. For an <b>embedded</b> surface {@code (x,y,w,h)} is the container and
     * {@code (vx,vy,vw,vh)} the hosted view (they differ only for CTabFolder). For a <b>window</b>
     * surface {@code (x,y,w,h)} is the window geometry and the view rect is ignored.
     */
    public static void setBounds(long context, int x, int y, int w, int h,
                                 int vx, int vy, int vw, int vh) {
        SetBounds(context, x, y, w, h, vx, vy, vw, vh);
    }

    /** Pumps up to {@code maxMessages} from the native event loop (headless measurement path). */
    public static int pumpMessages(int maxMessages) {
        return PumpMessages(maxMessages);
    }

    /**
     * Pumps a window surface's event loop once (driven from the SWT event loop). Returns
     * {@link #PUMP_CLOSE_REQUESTED} when the user asked to close and the window is still up, and any
     * other negative value once the window is gone, so the bridge can shut the SWT side down.
     */
    public static int pump(long context) {
        return Pump(context);
    }

    /**
     * Blocks in a window surface's event loop until an event is available or up to {@code millis}
     * milliseconds elapse, without consuming it (the next {@link #pump} dispatches it). Lets the SWT
     * idle wait park inside the OS event loop so input/window events wake it instantly.
     */
    public static void waitEvents(long context, int millis) {
        WaitEvents(context, millis);
    }

    /** Sets a window surface's title. No-op for an embedded surface. */
    public static void setTitle(long context, String title) {
        SetTitle(context, title);
    }

    /** Window state: {@code 0} = restore/normal, {@code 1} = maximized, {@code 2} = minimized, {@code 3} = fullscreen. */
    public static void setState(long context, int state) {
        SetState(context, state);
    }

    /**
     * Points the native bridge at an external app-bundle directory (the EWT-owned combined
     * bundle: {@code <dir>/lib/libapp.so} + {@code <dir>/data/flutter_assets}). Must be
     * called before the first {@link #initialize}. Passing null/absent restores the default
     * (bridge-relative) lookup used by standalone Evolve.
     */
    public static void setBundleDir(String bundleDir) {
        SetBundleDir(bundleDir);
    }

    private static native void SetBundleDir(String bundleDir);

    // ---- JNI peers (symbols exported by flutter_bridge.swift / .cpp / .cc) ----

    private static native long Initialize(int port, long parent, long widgetId, String widgetName,
                                           String theme, int backgroundColor, int parentBackgroundColor,
                                           int width, int height, boolean csdEnabled);

    private static native long GetView(long context);

    private static native void Dispose(long context);

    private static native void SetBounds(long context, int x, int y, int w, int h,
                                          int vx, int vy, int vw, int vh);

    private static native int PumpMessages(int maxMessages);

    private static native int Pump(long context);

    private static native void WaitEvents(long context, int millis);

    private static native void SetTitle(long context, String title);

    private static native void SetState(long context, int state);
}

package javafx.embed.swt;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;

/**
 * SWT Evolve reimplementation of {@code javafx.embed.swt.FXCanvas} — the
 * SWT&lt;-&gt;JavaFX bridge.
 *
 * <p>Upstream OpenJFX's {@code FXCanvas} hosts a JavaFX {@link Scene} inside a
 * native SWT control: it drives the JavaFX <em>embedded-scene</em> contract
 * ({@link HostInterface} / {@link EmbeddedSceneInterface}), pulls the rendered
 * pixels, and blits them onto the SWT control via an SWT {@code PaintListener} +
 * {@code GC.drawImage}. Input is forwarded by reading SWT mouse/key events and
 * calling {@code scenePeer.mouseEvent(...)} / {@code keyEvent(...)}.</p>
 *
 * <p>SWT Evolve renders SWT through Flutter rather than native widgets, but it
 * faithfully implements the SWT API surface this class relies on:
 * {@code addPaintListener} delivers a {@code GC} whose {@code drawImage} is piped
 * to the Flutter canvas overlay, and Flutter pointer/key events are delivered
 * back as SWT mouse/key events. So the upstream rendering/input strategy works
 * unchanged here — the JavaFX scene is rendered off-screen and its pixels appear
 * on the Evolve canvas.</p>
 *
 * <h2>How this differs from upstream</h2>
 * <ul>
 *   <li><b>No window, whichever Glass backend.</b> Nothing here ever opens one: the
 *       scene is rasterised into an image by {@link Scene#snapshot}, never onto a
 *       screen, and all this canvas takes from Glass is the JavaFX thread and its
 *       pulses. So the host's own backend (Win/Gtk) serves, and Prism picks its own
 *       pipeline. Monocle/Headless is the <em>exception</em>, not the default — it does
 *       not ship with {@code javafx.graphics} and is absent from most host
 *       applications. It is asked for only where the host's backend cannot work at
 *       all: macOS, whose Glass seizes the main thread SWT runs its loop on, and a
 *       machine with no display server. See {@code needsMonocle()}.</li>
 *   <li><b>snapshot() rendering.</b> Under headless Monocle the embedded scene's
 *       {@code repaint()}/{@code getPixels()} pulse does not deliver frames, so we
 *       render with the synchronous {@link Scene#snapshot} API instead and cache
 *       the latest frame. The {@link EmbeddedWindow}/scene is still used for
 *       <em>input</em> forwarding and lifecycle. A snapshot is requested after
 *       scene set, resize, and every forwarded input event; when it completes the
 *       canvas is redrawn and {@link #paintControl} blits the cached frame.</li>
 * </ul>
 *
 * <p>v1 scope: a single interactive scene with pointer + key forwarding, resize
 * and lifecycle. HiDPI/popups/IME/DnD/animation-pumping are follow-ups.</p>
 */
public class FXCanvas extends Canvas {

    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean UNAVAILABLE_WARNED = new AtomicBoolean(false);

    // Created on demand, NOT in a field initializer. Instantiating it defines HostContainer,
    // which implements com.sun.javafx.embed.HostInterface — and a host that loads JavaFX into
    // its own ModuleLayer (e(fx)clipse does) never exports com.sun.javafx.* to this unnamed
    // module, so that definition throws IllegalAccessError. Eagerly, that error escaped from
    // `new FXCanvas(...)` and took down the host application's startup; an optional bridge must
    // not be able to do that. Deferred and guarded, an unreachable JavaFX degrades this canvas
    // to a blank one instead. See fxHostUnavailable / ensureHost().
    private HostContainer hostContainer;

    // Set once we know JavaFX can't be driven from here. Everything that would touch the FX
    // toolkit checks it, so a degraded canvas never throws.
    private boolean fxHostUnavailable;

    private EmbeddedWindow stage;
    private volatile Scene scene;

    private volatile EmbeddedStageInterface stagePeer;
    private volatile EmbeddedSceneInterface scenePeer;

    // Latest rendered frame (ARGB, row-major), produced on the FX thread by a
    // snapshot and consumed on the SWT thread by paintControl. Guarded by 'this'.
    private int[] framePixels;
    private int frameWidth;
    private int frameHeight;

    private int pWidth;
    private int pHeight;

    private final AtomicBoolean snapshotPending = new AtomicBoolean(false);

    // SWT buttons currently held down (bitmask of SWT.BUTTON1/2/3), tracked across
    // MouseDown/MouseUp so a MouseMove can be classified as a drag — Evolve omits the
    // button bits from a move's stateMask. SWT-thread only.
    private int buttonsDown;

    // Whether the embedded JavaFX stage has been activated (focused). The scene's focus
    // owner (e.g. a TextField) only shows a caret / accepts typing while the stage is
    // focused. Activated once the canvas size is known. FX-thread only.
    private boolean stageActivated;

    // Repaints driven by JavaFX's own animation pulses (e.g. a TextField caret blink), so
    // animations run without needing a mouse/key event. The listener snapshots SYNCHRONOUSLY
    // (never via Platform.runLater) — a runLater would itself trigger another pulse and spin a
    // self-sustaining loop even when idle. With no active animation no pulse fires, so this
    // stays idle. FX-thread only.
    private final Runnable pulseListener = this::onScenePulse;
    private Scene pulseScene;
    private long lastPulseSnapshotNanos;
    private boolean inSnapshot;

    // Cap pulse-driven snapshots (~30 fps). An active animation pulses at ~60 fps, but a caret
    // blink / typical transition needs far less and each snapshot is a full software render.
    private static final long PULSE_MIN_INTERVAL_NANOS = 33_000_000L;

    public FXCanvas(Composite parent, int style) {
        super(parent, style);
        // Stays in the constructor: paintControl posts to the FX thread, and a paint can
        // arrive before the first setScene(), so the toolkit has to be up by then. Guarded
        // because JavaFX may be absent entirely (the javafx.* imports are optional).
        try {
            startToolkit();
        } catch (Throwable t) {
            fxHostUnavailable = true;
            warnUnavailable(t);
        }

        addPaintListener(this::paintControl);
        addListener(SWT.Dispose, e -> dispose0());

        // Input forwarding — Evolve delivers these SWT events from Flutter.
        addListener(SWT.MouseDown, e -> {
            MouseEvent me = new MouseEvent(e);
            buttonsDown |= swtButtonMask(me.button);
            // Take SWT keyboard focus so KeyDown/KeyUp are delivered here and can be
            // forwarded to the focused JavaFX node (e.g. a TextField).
            forceFocus();
            sendMouseEventToFX(me, AbstractEvents.MOUSEEVENT_PRESSED);
            requestSnapshot();
        });
        addListener(SWT.MouseUp, e -> {
            MouseEvent me = new MouseEvent(e);
            buttonsDown &= ~swtButtonMask(me.button);
            sendMouseEventToFX(me, AbstractEvents.MOUSEEVENT_RELEASED);
            requestSnapshot();
        });
        addListener(SWT.MouseMove, e -> {
            // Evolve does not carry the pressed-button bits in a MouseMove's stateMask, so a
            // drag would otherwise look like a plain move and controls like Slider/ScrollBar
            // wouldn't track. Classify from the button state we track on MouseDown/MouseUp.
            MouseEvent me = new MouseEvent(e);
            sendMouseEventToFX(me, buttonsDown != 0
                    ? AbstractEvents.MOUSEEVENT_DRAGGED : AbstractEvents.MOUSEEVENT_MOVED);
            requestSnapshot();
        });
        addListener(SWT.MouseEnter, e -> { sendMouseEventToFX(new MouseEvent(e), AbstractEvents.MOUSEEVENT_ENTERED); requestSnapshot(); });
        addListener(SWT.MouseExit, e -> { sendMouseEventToFX(new MouseEvent(e), AbstractEvents.MOUSEEVENT_EXITED); requestSnapshot(); });
        addListener(SWT.MouseDoubleClick, e -> { sendMouseEventToFX(new MouseEvent(e), AbstractEvents.MOUSEEVENT_PRESSED); requestSnapshot(); });
        addListener(SWT.KeyDown, e -> { sendKeyEventToFX(new KeyEvent(e), SWT.KeyDown); requestSnapshot(); });
        addListener(SWT.KeyUp, e -> { sendKeyEventToFX(new KeyEvent(e), SWT.KeyUp); requestSnapshot(); });
    }

    // Canvas/Composite already allow subclassing; keep explicit for clarity.
    @Override
    protected void checkSubclass() {
    }

    /**
     * Sets the JavaFX scene hosted by this canvas. Mirrors upstream
     * {@code FXCanvas.setScene}: the embedded stage is created lazily on the FX
     * application thread and bound to this host.
     */
    public void setScene(final Scene newScene) {
        checkWidget();
        this.scene = newScene;
        if (!ensureHost()) {
            // Degraded: the scene is kept so getScene() stays coherent, but nothing is
            // handed to JavaFX and the canvas renders nothing.
            return;
        }
        Platform.runLater(() -> {
            if (pulseScene != null) {
                pulseScene.removePostLayoutPulseListener(pulseListener);
                pulseScene = null;
            }
            if (stage == null && newScene != null) {
                stage = new EmbeddedWindow(hostContainer);
                stage.show();
            }
            if (stage != null) {
                stage.setScene(newScene);
            }
            if (stage != null && newScene == null) {
                stage.hide();
                stage = null;
            }
            if (newScene != null) {
                // Repaint on the scene's own animation pulses (caret blink, transitions).
                newScene.addPostLayoutPulseListener(pulseListener);
                pulseScene = newScene;
                snapshotNow();
            }
        });
    }

    public Scene getScene() {
        checkWidget();
        return scene;
    }

    /**
     * Requests a fresh render of the hosted scene. Call this after changing the
     * scene from <em>outside</em> the canvas's own input path — e.g. an SWT control
     * (on the SWT thread) or app code mutating a JavaFX node — so the change appears
     * without needing a mouse/key event over the canvas.
     *
     * <p>This canvas renders headlessly and only re-snapshots on forwarded input
     * (and scene-set/resize); under the headless toolkit there is no reliable
     * per-frame "scene changed" callback, and snapshotting on every JavaFX pulse
     * spins a render loop even when idle. So programmatic edits explicitly ask for a
     * frame here. Coalesced and thread-safe; the actual render runs on the FX thread.
     * Redundant calls are cheap — an unchanged frame is rendered but not repainted.</p>
     *
     * <p>JavaFX animations (e.g. a focused TextField's caret blink) repaint on their own via
     * the scene's animation pulses — see {@link #onScenePulse}; this is only for changes that
     * don't go through input or an animation.</p>
     */
    public void refresh() {
        requestSnapshot();
    }

    /**
     * Post-layout pulse callback. JavaFX fires pulses while a scene animates (caret blink,
     * transitions); repaint on those — throttled to ~30 fps — so animations are visible without
     * a mouse/key event. Snapshots synchronously: routing through {@link #requestSnapshot} (a
     * {@code Platform.runLater}) would re-trigger a pulse and spin a self-sustaining loop even
     * when idle. When nothing animates, no pulse fires and this never runs.
     */
    private void onScenePulse() {
        long now = System.nanoTime();
        if (now - lastPulseSnapshotNanos < PULSE_MIN_INTERVAL_NANOS) {
            return;
        }
        lastPulseSnapshotNanos = now;
        snapshotNow();
    }

    // ---- JavaFX toolkit bootstrap (headless / offscreen) -------------------

    /**
     * Creates the {@link HostContainer} on first use, and reports whether this canvas can
     * drive JavaFX at all.
     *
     * <p>This is the single place {@code HostContainer} is instantiated, which matters: that
     * instantiation is what forces the JVM to define a class implementing
     * {@code com.sun.javafx.embed.HostInterface}. When the host application loads JavaFX into
     * its own {@code ModuleLayer} — e(fx)clipse's {@code FXClassLoader} does — {@code
     * javafx.graphics} exports its {@code com.sun.javafx.*} packages to that layer only, never
     * to this bundle's unnamed module, and the definition fails with {@link IllegalAccessError}.
     * Command-line {@code --add-exports} does not help: it only applies to the boot layer.</p>
     *
     * <p>Rather than let that escape and abort the host application's startup, we record it and
     * degrade: the canvas stays blank and every FX-touching path becomes a no-op.</p>
     *
     * @return {@code true} if the JavaFX embedding internals are reachable
     */
    private boolean ensureHost() {
        if (hostContainer != null) {
            return true;
        }
        if (fxHostUnavailable) {
            return false;
        }
        openFxInternals();
        try {
            hostContainer = new HostContainer();
            return true;
        } catch (Throwable t) {
            fxHostUnavailable = true;
            warnUnavailable(t);
            return false;
        }
    }

    /** Packages {@code HostContainer} and the embedded-stage plumbing need to reach. */
    private static final String[] FX_EMBED_PACKAGES = {
        "com.sun.javafx.embed",
        "com.sun.javafx.cursor",
        "com.sun.javafx.stage",
        "com.sun.javafx.application",
        "com.sun.javafx.tk",
    };

    private static final java.util.concurrent.atomic.AtomicBoolean FX_OPEN_TRIED =
            new java.util.concurrent.atomic.AtomicBoolean();

    /**
     * Adds the embedding packages of the host's {@code javafx.graphics} to this bundle's module, so
     * that defining {@code HostContainer implements HostInterface} passes the JVM's access check.
     *
     * <p>Command-line {@code --add-exports} cannot do this: it is applied to the boot layer, while a
     * host like e(fx)clipse builds its own {@code ModuleLayer} at runtime. Mutating the {@code Module}
     * object works whatever layer it lives in — but {@code Module.addExports} is caller-sensitive, so
     * we go through {@code implAddExports}, which needs {@code java.lang} opened to us. That one flag
     * <em>does</em> work from the command line, because {@code java.base} is always in the boot
     * layer:</p>
     *
     * <pre>--add-opens=java.base/java.lang=ALL-UNNAMED</pre>
     *
     * <p>Without it this is a no-op and the canvas degrades to blank exactly as before.</p>
     */
    private static void openFxInternals() {
        if (!FX_OPEN_TRIED.compareAndSet(false, true)) {
            return;
        }
        try {
            Module fx = Platform.class.getModule();
            Module self = FXCanvas.class.getModule();
            // JavaFX on the classpath lands in the unnamed module, where no access control applies.
            if (!fx.isNamed()) {
                return;
            }
            java.lang.reflect.Method implAddExports =
                    Module.class.getDeclaredMethod("implAddExports", String.class, Module.class);
            implAddExports.setAccessible(true);
            for (String pkg : FX_EMBED_PACKAGES) {
                if (fx.getPackages().contains(pkg) && !fx.isExported(pkg, self)) {
                    implAddExports.invoke(fx, pkg, self);
                }
            }
        } catch (Throwable t) {
            // Left closed — ensureHost() will report the blank canvas with the real cause.
            System.err.println("[FXCanvas] could not open JavaFX internals to this bundle "
                    + "(add --add-opens=java.base/java.lang=ALL-UNNAMED to enable): " + t);
        }
    }

    /** Explains the blank canvas once per JVM — otherwise it is undiagnosable. */
    private static void warnUnavailable(Throwable cause) {
        if (UNAVAILABLE_WARNED.compareAndSet(false, true)) {
            System.err.println("[FXCanvas] JavaFX embedding disabled — this canvas will stay blank. "
                    + "The JavaFX internals (com.sun.javafx.*) are not reachable from this bundle, "
                    + "which happens when the host application loads JavaFX into its own ModuleLayer. "
                    + "Cause: " + cause);
        }
    }

    private static void startToolkit() {
        if (!TOOLKIT_STARTED.compareAndSet(false, true)) {
            return;
        }
        // The host's own Glass backend is the normal path. Nothing here ever opens a
        // window: the scene is rasterised into an image by snapshot(), never onto a
        // screen, so Win/Mac/Gtk all serve — all this canvas takes from Glass is the
        // JavaFX thread and its pulses.
        //
        // Monocle is the exception, for a machine with no display at all to talk to.
        // It does not ship with javafx.graphics — only a separate artifact carries it —
        // so it is absent from practically every host application, and naming it
        // unconditionally made Glass resolve com.sun.glass.ui.monocle.MonoclePlatformFactory,
        // fail, and hand back a null factory, leaving every canvas in the product blank.
        //
        // Anywhere this reads the machine wrong, -Dglass.platform on the command line still
        // wins — every write below is set-if-absent.
        if (needsMonocle() && monocleAvailable()) {
            setIfAbsent("glass.platform", "Monocle");
            setIfAbsent("monocle.platform", "Headless");
            // Nothing is going to hand a headless Glass a GPU, so the software pipeline is
            // the only one that can serve. With a real backend, let Prism choose (d3d/es2,
            // falling back to sw by itself where there is no usable device).
            setIfAbsent("prism.order", "sw");
        }
        setIfAbsent("prism.vsync", "false");
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException alreadyStarted) {
            // Toolkit already running — fine.
        }
    }

    /**
     * Whether the host's own Glass backend cannot drive this canvas, so the headless one is
     * the only candidate left.
     *
     * <p><b>macOS.</b> Glass there adopts the <em>main</em> thread as the JavaFX application
     * thread — the same thread SWT runs its event loop on — and offers no alternative. That
     * loop is pure Java: {@code DartDisplay.readAndDispatch()} runs timers, paints and async
     * messages and then parks in {@code sleep()} on a semaphore, so it never services the
     * native run loop Glass posts to and no {@code Platform.runLater} ever runs. Measured on a
     * live application: the main thread renamed "JavaFX Application Thread" and parked in
     * {@code DartDisplay.sleep}, with not a single frame drawn. It is the same dead end that
     * {@code javafx.embed.isEventThread=true} leads to, reached without asking for it.</p>
     *
     * <p><b>No display server.</b> A CI worker has nothing for a native backend to talk to.</p>
     */
    private static boolean needsMonocle() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        if (os.contains("mac")) {
            return true;
        }
        if (os.startsWith("windows")) {
            return false;
        }
        // Linux: the host Glass backend is Gtk, and Gtk's <clinit> matches the SWT-GTK version.
        // Evolve renders via Flutter and never initialises native GTK, so SWT reports GTK major
        // version 0 and GtkApplication throws "SWT-GTK uses unsupported major GTK version 0".
        // The Gtk backend can therefore never drive this canvas under Evolve — display or not —
        // so Monocle (headless, offscreen snapshot) is the only viable backend on Linux.
        return true;
    }

    /**
     * Whether this JavaFX carries the headless Monocle Glass platform.
     *
     * <p>Probed through the loader that defined {@code javafx.application.Platform}, because
     * that is the loader Glass itself resolves the platform factory with — under OSGi, or when
     * the host loads JavaFX into its own {@code ModuleLayer}, it is not this bundle's.</p>
     */
    private static boolean monocleAvailable() {
        try {
            Class.forName("com.sun.glass.ui.monocle.MonoclePlatformFactory", false,
                    Platform.class.getClassLoader());
            return true;
        } catch (Throwable notThere) {
            return false;
        }
    }

    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    // ---- Rendering: snapshot on FX thread, blit on SWT thread --------------

    /** Coalesced request to (re)render the scene on the FX thread. */
    private void requestSnapshot() {
        if (scene == null || fxHostUnavailable) {
            return;
        }
        if (snapshotPending.compareAndSet(false, true)) {
            Platform.runLater(() -> {
                snapshotPending.set(false);
                snapshotNow();
            });
        }
    }

    /** Renders the current scene synchronously. Must run on the FX thread. */
    private void snapshotNow() {
        Scene s = scene;
        if (s == null) {
            return;
        }
        int w = pWidth, h = pHeight;
        if (w <= 0 || h <= 0) {
            return;
        }
        if (inSnapshot) {
            // Re-entrancy guard: snapshot() can itself fire a pulse, which would re-enter via
            // onScenePulse(). Bail rather than nest a render inside a render.
            return;
        }
        inSnapshot = true;
        try {
            // Force a CSS + layout pass at the current size before rendering. The
            // embedded scene's setSize() is asynchronous, so relying on it alone
            // can snapshot a stale (e.g. collapsed) layout; resizing the root and
            // applying CSS/layout here makes every frame correctly laid out.
            javafx.scene.Parent root = s.getRoot();
            if (root != null) {
                root.resize(w, h);
                root.applyCss();
                root.layout();
            }
            WritableImage img = new WritableImage(w, h);
            s.snapshot(img);
            int[] px = new int[w * h];
            img.getPixelReader().getPixels(0, 0, w, h,
                    PixelFormat.getIntArgbInstance(), px, 0, w);
            // Only repaint when the frame actually changed. Repainting on every
            // render would spam the SWT canvas with Paint events (and re-blits)
            // even when nothing on screen differs.
            boolean changed;
            synchronized (this) {
                changed = frameWidth != w || frameHeight != h
                        || framePixels == null || !java.util.Arrays.equals(framePixels, px);
                if (changed) {
                    framePixels = px;
                    frameWidth = w;
                    frameHeight = h;
                }
            }
            if (!changed) {
                return;
            }
            debugFrame(px, w, h);
        } catch (Throwable t) {
            // A failed render should never take down the host app.
            System.err.println("[FXCanvas] snapshot failed: " + t);
            return;
        } finally {
            inSnapshot = false;
        }
        Display display = getDisplaySafe();
        if (display != null) {
            display.asyncExec(() -> {
                if (!isDisposed()) {
                    redraw();
                }
            });
        }
    }

    private void paintControl(org.eclipse.swt.events.PaintEvent pe) {
        Rectangle area = getClientArea();
        int w = area.width;
        int h = area.height;
        if (w <= 0 || h <= 0) {
            return;
        }
        if (w != pWidth || h != pHeight) {
            pWidth = w;
            pHeight = h;
            final int fw = w, fh = h;
            // Degraded: no toolkit to post to, and nothing to resize.
            if (fxHostUnavailable) {
                return;
            }
            Platform.runLater(() -> {
                if (stagePeer != null) {
                    stagePeer.setSize(fw, fh);
                    // setEmbeddedStage usually runs before the first paint (size still 0) and
                    // skips activation; activate now that the size is known, otherwise the scene
                    // stays unfocused — no caret and no typing in a TextField.
                    if (!stageActivated) {
                        stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
                        stageActivated = true;
                    }
                }
                if (scenePeer != null) scenePeer.setSize(fw, fh);
                snapshotNow();
            });
        }

        final int[] px;
        final int pw, ph;
        synchronized (this) {
            px = framePixels;
            pw = frameWidth;
            ph = frameHeight;
        }
        if (px == null || pw <= 0 || ph <= 0) {
            return;
        }

        // snapshot() returns ARGB; render as opaque RGB (alpha dropped).
        PaletteData palette = new PaletteData(0x00ff0000, 0x0000ff00, 0x000000ff);
        ImageData imageData = new ImageData(pw, ph, 32, palette);
        imageData.setPixels(0, 0, pw * ph, px, 0);

        Image image = new Image(getDisplay(), imageData);
        try {
            pe.gc.drawImage(image, 0, 0, pw, ph, 0, 0, pWidth, pHeight);
            if (DEBUG && drawCount++ < 3) {
                System.err.println("[FXCanvas] drawImage frame " + pw + "x" + ph + " -> " + pWidth + "x" + pHeight);
            }
        } finally {
            image.dispose();
        }
    }

    // ---- Debug instrumentation (enable with -Dfxcanvas.debug=true) ----------

    private static final boolean DEBUG = "true".equals(System.getProperty("fxcanvas.debug"));
    private int drawCount;
    private int debugFrameCount;

    private void debugFrame(int[] argb, int w, int h) {
        if (!DEBUG || debugFrameCount++ > 8) {
            return;
        }
        try {
            java.awt.image.BufferedImage bi =
                    new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
            bi.setRGB(0, 0, w, h, argb, 0, w);
            String path = System.getProperty("fxcanvas.debug.out",
                    System.getProperty("java.io.tmpdir") + "/fxcanvas_frame.png");
            javax.imageio.ImageIO.write(bi, "png", new java.io.File(path));
            System.err.println("[FXCanvas] wrote first in-process frame " + w + "x" + h + " to " + path);
        } catch (Throwable t) {
            System.err.println("[FXCanvas] debugFrame failed: " + t);
        }
    }

    private Display getDisplaySafe() {
        try {
            Display d = getDisplay();
            return (d == null || d.isDisposed()) ? null : d;
        } catch (Throwable t) {
            return null;
        }
    }

    // ---- Input translation --------------------------------------------------

    private void sendMouseEventToFX(MouseEvent me, int embedMouseType) {
        EmbeddedSceneInterface peer = scenePeer;
        if (peer == null) {
            return;
        }
        Point abs = toDisplay(me.x, me.y);
        int stateMask = me.stateMask;
        // Button-down state comes from buttonsDown (updated on MouseDown/MouseUp before this
        // call), not me.stateMask, which Evolve doesn't populate during a drag/move.
        boolean primaryDown = (buttonsDown & SWT.BUTTON1) != 0;
        boolean middleDown = (buttonsDown & SWT.BUTTON2) != 0;
        boolean secondaryDown = (buttonsDown & SWT.BUTTON3) != 0;
        int button = embedButton(me.button, embedMouseType);
        boolean shift = (stateMask & SWT.SHIFT) != 0;
        boolean control = (stateMask & SWT.CONTROL) != 0;
        boolean alt = (stateMask & SWT.ALT) != 0;
        boolean meta = (stateMask & SWT.COMMAND) != 0;
        peer.mouseEvent(embedMouseType, button,
                primaryDown, middleDown, secondaryDown,
                false, false,
                me.x, me.y, abs.x, abs.y,
                shift, control, alt, meta,
                secondaryDown);
    }

    private static int swtButtonMask(int swtButton) {
        switch (swtButton) {
            case 1: return SWT.BUTTON1;
            case 2: return SWT.BUTTON2;
            case 3: return SWT.BUTTON3;
            default: return 0;
        }
    }

    private static int embedButton(int swtButton, int type) {
        if (type == AbstractEvents.MOUSEEVENT_MOVED || type == AbstractEvents.MOUSEEVENT_DRAGGED
                || type == AbstractEvents.MOUSEEVENT_ENTERED || type == AbstractEvents.MOUSEEVENT_EXITED) {
            return AbstractEvents.MOUSEEVENT_NONE_BUTTON;
        }
        switch (swtButton) {
            case 1: return AbstractEvents.MOUSEEVENT_PRIMARY_BUTTON;
            case 2: return AbstractEvents.MOUSEEVENT_MIDDLE_BUTTON;
            case 3: return AbstractEvents.MOUSEEVENT_SECONDARY_BUTTON;
            default: return AbstractEvents.MOUSEEVENT_NONE_BUTTON;
        }
    }

    private void sendKeyEventToFX(KeyEvent e, int swtType) {
        EmbeddedSceneInterface peer = scenePeer;
        if (peer == null) {
            return;
        }
        int modifiers = embedModifiers(e.stateMask);
        int embedType = swtType == SWT.KeyDown
                ? AbstractEvents.KEYEVENT_PRESSED
                : AbstractEvents.KEYEVENT_RELEASED;
        // Best-effort: precise SWT->FX key-code mapping is a follow-up. The TYPED
        // event below carries the actual character, which is what text input needs.
        peer.keyEvent(embedType, e.keyCode, new char[0], modifiers);
        if (swtType == SWT.KeyDown && e.character != '\0') {
            peer.keyEvent(AbstractEvents.KEYEVENT_TYPED, e.keyCode, new char[]{e.character}, modifiers);
        }
    }

    private static int embedModifiers(int stateMask) {
        int m = 0;
        if ((stateMask & SWT.SHIFT) != 0) m |= AbstractEvents.MODIFIER_SHIFT;
        if ((stateMask & SWT.CONTROL) != 0) m |= AbstractEvents.MODIFIER_CONTROL;
        if ((stateMask & SWT.ALT) != 0) m |= AbstractEvents.MODIFIER_ALT;
        if ((stateMask & SWT.COMMAND) != 0) m |= AbstractEvents.MODIFIER_META;
        return m;
    }

    // ---- Lifecycle ----------------------------------------------------------

    private void dispose0() {
        final EmbeddedWindow s = stage;
        final Scene ps = pulseScene;
        stage = null;
        pulseScene = null;
        scene = null;
        scenePeer = null;
        stagePeer = null;
        if (s != null || ps != null) {
            Platform.runLater(() -> {
                try {
                    if (ps != null) ps.removePostLayoutPulseListener(pulseListener);
                    if (s != null) s.hide();
                } catch (Throwable ignored) {
                }
            });
        }
    }

    // ---- HostInterface ------------------------------------------------------

    private final class HostContainer implements HostInterface {

        @Override
        public void setEmbeddedStage(EmbeddedStageInterface embeddedStage) {
            stagePeer = embeddedStage;
            if (embeddedStage == null) {
                stageActivated = false;
                return;
            }
            if (pWidth > 0 && pHeight > 0) {
                embeddedStage.setSize(pWidth, pHeight);
                embeddedStage.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
                stageActivated = true;
            }
        }

        @Override
        public void setEmbeddedScene(EmbeddedSceneInterface embeddedScene) {
            scenePeer = embeddedScene;
            if (embeddedScene == null) {
                return;
            }
            embeddedScene.setPixelScaleFactors(1.0f, 1.0f);
            if (pWidth > 0 && pHeight > 0) {
                embeddedScene.setSize(pWidth, pHeight);
            }
        }

        @Override
        public boolean requestFocus() {
            Display display = getDisplaySafe();
            if (display == null) {
                return false;
            }
            display.asyncExec(() -> {
                if (!isDisposed()) {
                    forceFocus();
                }
            });
            return true;
        }

        @Override
        public boolean traverseFocusOut(boolean forward) {
            return true;
        }

        @Override
        public void setPreferredSize(int width, int height) {
        }

        @Override
        public void repaint() {
            requestSnapshot();
        }

        @Override
        public void setEnabled(boolean enabled) {
        }

        @Override
        public void setCursor(CursorFrame cursorFrame) {
        }

        @Override
        public boolean grabFocus() {
            return true;
        }

        @Override
        public void ungrabFocus() {
        }
    }
}

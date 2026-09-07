package dev.equo.swt.awt;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.PaintEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;

import sun.swing.JLightweightFrame;
import sun.swing.LightweightContent;

/**
 * SWT Evolve host for embedded Swing content — the AWT/Swing mirror of
 * {@code javafx.embed.swt.FXCanvas}.
 *
 * <p>Stock {@code SWT_AWT.new_Frame(Composite)} reparents a heavyweight AWT
 * {@code Frame} into the SWT control's native window handle. Under Evolve there
 * is no native SWT widget and no handle to reparent into, so this host renders
 * the Swing tree <em>off-screen</em> instead: it drives a
 * {@link JLightweightFrame} through the {@link LightweightContent} contract (the
 * same mechanism JavaFX's {@code SwingNode} uses), pulls the rendered pixels out
 * of the frame's shared {@code int[]} buffer, and blits them onto a Flutter-backed
 * SWT {@link Canvas} via {@code addPaintListener} + {@code GC.drawImage} — exactly
 * the path FXCanvas uses for JavaFX.</p>
 *
 * <p>Because {@code JLightweightFrame extends java.awt.Frame}, the object returned
 * from {@link #newFrame(Composite)} <em>is</em> a {@code Frame}; unmodified app
 * code keeps calling {@code frame.add(swingPanel)} and it just works.</p>
 *
 * <h2>Threading</h2>
 * The stock bridge deadlocks under Evolve because it busy-waits on the SWT main
 * thread while AWT tries to create the frame — on macOS the frame's construction
 * marshals to the AppKit main thread, which the busy-wait is starving. Here the
 * frame is built on the EDT while the SWT thread keeps <em>pumping</em> its event
 * loop, so AWT's main-thread work is serviced and construction completes.
 */
public final class EvolveSwingHost {

    private EvolveSwingHost() {}

    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);

    /**
     * One paint-and-push callback per active embedding, keyed by its content root.
     * {@link JLightweightFrame}'s own repaint cycle only reacts to the lightweight tree, so a
     * heavyweight descendant redrawing itself has no other path back into it.
     */
    private static final Map<Container, Runnable> HEAVYWEIGHT_REPAINT_HOOKS = new ConcurrentHashMap<>();

    private static final AtomicBoolean OUTSIDE_CLICK_FILTER_INSTALLED = new AtomicBoolean(false);

    /**
     * Evolve reimplementation of {@code SWT_AWT.new_Frame}. Returns a Swing
     * {@link Frame} whose content is rendered off-screen and presented on the
     * Flutter surface backing {@code parent}.
     */
    public static Frame newFrame(final Composite parent) {
        if (parent == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        startToolkit();
        installOutsideClickRepaintFilter(parent.getDisplay());

        // A single full-bleed Canvas child is the paint surface (Evolve pipes its
        // PaintListener + GC.drawImage to Flutter). FillLayout keeps it sized to
        // the EMBEDDED composite.
        parent.setLayout(new FillLayout());
        final Canvas canvas = new Canvas(parent, SWT.NO_BACKGROUND);

        final Host host = new Host(canvas);

        // Construct the off-screen frame on the CURRENT (SWT main) thread rather than
        // the EDT. On macOS the frame's construction makes AppKit main-thread calls
        // (e.g. getScreenInsets); done on the EDT they marshal to the main thread,
        // which under Evolve/desk is trapped in the blocking Flutter pump → deadlock.
        // Constructing here runs those calls inline. Software Swing painting afterwards
        // happens on the EDT (Java2D, no main thread needed).
        final JLightweightFrame frame = new JLightweightFrame();
        frame.setContent(host.content);
        host.bind(frame);

        // Initial size + realize so the first frame renders. Pre-open the canvas may
        // report 0×0; the Resize listener re-sizes the frame once it's laid out.
        Rectangle area = canvas.getClientArea();
        frame.setSize(Math.max(1, area.width), Math.max(1, area.height));
        frame.setVisible(true);
        return frame;
    }

    /**
     * Repaints every active embedding on any SWT click, installed once per process.
     *
     * <p>A control outside the embed can change an embedded view's model without going through
     * the AWT/Swing input bridge at all — a zoom toolbar button that is an ordinary SWT
     * {@code Canvas} has no way to reach into the off-screen frame and ask it to repaint, so the
     * change stays applied but invisible. Filtering {@code SWT.Selection} would be the tighter
     * net, but a widget fires it by invoking its own listener array directly, which never reaches
     * a {@code Display} filter; mouse events do.
     */
    private static void installOutsideClickRepaintFilter(Display display) {
        if (!OUTSIDE_CLICK_FILTER_INSTALLED.compareAndSet(false, true)) return;
        Listener repaintAllEmbeds = ev -> {
            for (Runnable forceRepaint : HEAVYWEIGHT_REPAINT_HOOKS.values()) {
                scheduleStaggeredRepaints(forceRepaint, 100, 300, 700, 1500);
            }
        };
        display.addFilter(SWT.MouseDown, repaintAllEmbeds);
        display.addFilter(SWT.MouseUp, repaintAllEmbeds);
    }

    /**
     * Fires {@code forceRepaint} a few times, staggered, instead of once immediately after some
     * change (an initial paint, a resize, an input-driven update). A hosted app's own rendering
     * is often asynchronous — it updates its model synchronously but recomputes what it actually
     * paints (e.g. a cached composite image) on its own schedule after that — so a single
     * immediate repaint reliably captures the PREVIOUS state instead of the new one (observed
     * live: a wheel zoom rendered exactly one notch behind). Spread a few attempts over a short
     * window instead of guessing one exact delay or polling indefinitely.
     */
    static void scheduleStaggeredRepaints(Runnable forceRepaint, int... delaysMs) {
        for (int delayMs : delaysMs) {
            javax.swing.Timer t = new javax.swing.Timer(delayMs, ev -> forceRepaint.run());
            t.setRepeats(false);
            t.start();
        }
    }

    private static void startToolkit() {
        if (!TOOLKIT_STARTED.compareAndSet(false, true)) return;
        // Non-headless AWT (Swing must actually render), but as a background agent:
        // no Dock icon / app menu, so bringing up AppKit alongside Flutter is quiet.
        setIfAbsent("java.awt.headless", "false");
        setIfAbsent("apple.awt.UIElement", "true");
        // Keep menus/tooltips/popups inside the off-screen frame (lightweight) rather
        // than spawning real OS windows over the Flutter surface.
        javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(true);
        Toolkit.getDefaultToolkit();
        installEvolveDispatcher();
        installHeavyweightRepaintListener();
        installDispatcherReinstateListener();
    }

    private static final ThreadLocal<Boolean> BLITTING = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Map<Container, Long> LAST_REPAINT = new ConcurrentHashMap<>();
    private static final java.util.Set<Container> REPAINT_PENDING = ConcurrentHashMap.newKeySet();
    private static final int MIN_REPAINT_INTERVAL_MS = 16;

    /**
     * Runs {@code forceRepaint} at most once per {@link #MIN_REPAINT_INTERVAL_MS}. A repaint here
     * costs a full-frame copy plus a full paint of the heavyweight subtree, so driving it straight
     * off every {@link PaintEvent} both duplicates work within a single frame and — for any
     * component that repaints itself while painting — feeds itself without bound.
     */
    /** Drops every reference this class holds to a disposed embedding's component tree. */
    private static void forgetEmbed(Container root) {
        HEAVYWEIGHT_REPAINT_HOOKS.remove(root);
        REPAINT_PENDING.remove(root);
        LAST_REPAINT.remove(root);
    }

    private static void coalesceRepaint(Container root, Runnable forceRepaint) {
        if (!REPAINT_PENDING.add(root)) return;
        long since = System.currentTimeMillis() - LAST_REPAINT.getOrDefault(root, 0L);
        int delay = (int) Math.max(1, MIN_REPAINT_INTERVAL_MS - since);
        javax.swing.Timer timer = new javax.swing.Timer(delay, ev -> {
            REPAINT_PENDING.remove(root);
            LAST_REPAINT.put(root, System.currentTimeMillis());
            BLITTING.set(Boolean.TRUE);
            try {
                forceRepaint.run();
            } finally {
                BLITTING.set(Boolean.FALSE);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Forces an embedding's paint-and-push whenever a heavyweight descendant repaints itself.
     *
     * <p>Such a component posts a {@link PaintEvent} exactly as it would under a real native peer
     * — that part of AWT neither knows nor cares that there is no visible window behind it — so a
     * global listener sees it without needing the hosted application's cooperation. Events raised
     * by our own paint pass are skipped: reacting to them would re-trigger the pass that produced
     * them, without bound.
     */
    private static void installHeavyweightRepaintListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof PaintEvent)) return;
            if (Boolean.TRUE.equals(BLITTING.get())) return;
            Component source = ((PaintEvent) event).getComponent();
            for (Map.Entry<Container, Runnable> entry : HEAVYWEIGHT_REPAINT_HOOKS.entrySet()) {
                Container root = entry.getKey();
                if (root == source || root.isAncestorOf(source)) {
                    coalesceRepaint(root, entry.getValue());
                    return;
                }
            }
        }, AWTEvent.PAINT_EVENT_MASK);
    }

    /**
     * Reinstates this host's dispatcher when a hosted window is disposed — where it has been
     * observed to go missing. Repairing at that moment rather than on the next input keeps the
     * gap from ever being observable.
     */
    private static void installDispatcherReinstateListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event.getID() == java.awt.event.WindowEvent.WINDOW_CLOSED) {
                ensureDispatcherInstalled();
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
    }

    /**
     * Puts this host's dispatcher back on the AWT event queue if the queue has stopped holding it.
     *
     * <p>The queue holds exactly one forwarding dispatcher, and a host application may run its own
     * AWT/SWT bridge competing for that slot — one seen in the field installs {@code null} there
     * when it leaves single-UI-thread mode, while our embeds are still live. The queue then keeps
     * draining events but dispatches them past this host, so the embedded Swing tree silently
     * stops updating and stops taking input. Nothing throws: {@code EventQueue.isDispatchThread()}
     * just falls back to comparing threads and answers false, which is what this detects — the
     * queue answering something other than what our own dispatcher would answer.
     *
     * <p>Fails closed like the initial install: an unavailable interop API leaves AWT dispatching
     * however it would by default.
     */
    static void ensureDispatcherInstalled() {
        try {
            EvolveDispatcherWrapper wrapper = new EvolveDispatcherWrapper(Display.getDefault());
            if (wrapper.isDispatchThread() == EventQueue.isDispatchThread()) return;
            jdk.swing.interop.DispatcherWrapper.setFwDispatcher(
                    Toolkit.getDefaultToolkit().getSystemEventQueue(), wrapper);
        } catch (Throwable notInstallable) {
        }
    }

    /**
     * Installs {@link EvolveDispatcherWrapper} on the system {@link EventQueue} — see the
     * class-level "AWT/Swing dispatch" section for why. Requires
     * {@code --add-exports jdk.unsupported.desktop/jdk.swing.interop=ALL-UNNAMED} on the
     * actual runtime JVM command line, not just at compile time; if that export is missing
     * this fails closed (logged, caught) and dispatch falls back to whatever the host's own
     * JavaFX/AWT bridge does by default.
     *
     * <p>Installs the first time any Evolve Swing embedding happens in the process. A host
     * that constructs its own {@code JFXPanel} (unrelated to Evolve) before that point installs
     * its own dispatcher first, and that specific panel's handshake is not protected by this —
     * only dispatch from this point forward is.
     */
    private static void installEvolveDispatcher() {
        try {
            Display display = Display.getDefault();
            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
            jdk.swing.interop.DispatcherWrapper.setFwDispatcher(eq, new EvolveDispatcherWrapper(display));
        } catch (Throwable ignored) {
            // Falls back to default AWT/JavaFX dispatch behavior.
        }
    }

    // Package-private (not private) so EvolveDispatcherWrapperTest can construct and exercise
    // these directly instead of only through the full JavaFX/AWT dispatch-merge trigger.
    static final class EvolveDispatcherWrapper extends jdk.swing.interop.DispatcherWrapper {
        private final Display display;

        EvolveDispatcherWrapper(Display display) {
            this.display = display;
        }

        @Override
        public boolean isDispatchThread() {
            return !display.isDisposed() && display.getThread() == Thread.currentThread();
        }

        @Override
        public void scheduleDispatch(Runnable runnable) {
            if (display.isDisposed()) return;
            try {
                display.asyncExec(runnable);
            } catch (NullPointerException notYetInitialized) {
                // installEvolveDispatcher() can capture a Display before its Synchronizer is set
                // up -- JavaFX/AWT startup can reach the EDT before the SWT Display finishes its
                // own construction, so isDisposed() (false) doesn't catch this window. Retry on
                // the next EDT turn instead of losing the task.
                EventQueue.invokeLater(() -> scheduleDispatch(runnable));
            }
        }

        @Override
        public java.awt.SecondaryLoop createSecondaryLoop() {
            return new EvolveSecondaryLoop(display);
        }
    }

    /**
     * Pumps the SWT {@link Display}'s own event loop while "nested", in place of the native
     * toolkit's own nested-event-loop mechanism (which a merged single-UI-thread host can enter
     * and never return from, since nothing here pumps its native message loop). {@link #enter()}
     * runs on whatever thread requests the nested loop — always the {@code Display}'s own thread
     * in practice, since {@link EvolveDispatcherWrapper#createSecondaryLoop()} is only reached via
     * code paths gated on {@link EvolveDispatcherWrapper#isDispatchThread()}. {@link #exit()} is
     * called from elsewhere (typically a JavaFX-internal thread, once its own startup finishes)
     * to release it.
     */
    static final class EvolveSecondaryLoop implements java.awt.SecondaryLoop {
        private final Display display;
        private volatile boolean running;

        EvolveSecondaryLoop(Display display) {
            this.display = display;
        }

        @Override
        public boolean enter() {
            if (running) return false;
            running = true;
            while (running && !display.isDisposed()) {
                if (!display.readAndDispatch()) display.sleep();
            }
            return true;
        }

        @Override
        public boolean exit() {
            if (!running) return false;
            running = false;
            if (!display.isDisposed()) display.wake();
            return true;
        }
    }

    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) System.setProperty(key, value);
    }

    // ---- Host: LightweightContent + SWT canvas wiring ----------------------------

    private static final class Host {
        private final Canvas canvas;
        // Cached at construction so the EDT never calls canvas.getDisplay() (which throws once the
        // canvas is disposed during shutdown). Display.isDisposed()/asyncExec are thread-safe.
        private final Display display;
        private final JPanel contentRoot = new JPanel(new BorderLayout());
        private final EvolveContent content = new EvolveContent();
        private volatile JLightweightFrame frame;
        // Guards the container-listener reentrancy while we relocate app-added children.
        private boolean relocating;

        // Latest rendered frame (ARGB), produced on the EDT and consumed on the SWT
        // thread by the paint listener. Guarded by 'this'.
        private int[] framePixels;
        private int frameWidth;
        private int frameHeight;
        // Coalesces repaints: many EDT frames (e.g. a blinking caret) collapse to one SWT redraw.
        private final AtomicBoolean redrawPending = new AtomicBoolean();

        Host(Canvas canvas) {
            this.canvas = canvas;
            this.display = canvas.getDisplay();
        }

        /**
         * Wires the frame to its canvas: paint, resize, dispose and input.
         *
         * <p>Three of those also force a paint-and-push, because a heavyweight descendant has no
         * equivalent of a real peer's {@code WM_PAINT}: once here (its first paint would otherwise
         * never happen), on resize (its cached content stays stale until it recomputes for the new
         * size), and per interaction from {@link AwtInput}. Each is staggered rather than
         * immediate — the hosted application recomputes what it paints on its own schedule, so a
         * single immediate push captures the previous frame.
         *
         * <p>{@code forceRepaint} is registered globally too, so a heavyweight's own
         * {@code repaint()} reaches the screen; {@link AwtInput}'s drag timer covers the remaining
         * case, immediate-mode {@code getGraphics()} drawing, which posts no {@link PaintEvent} at
         * all.
         */
        void bind(JLightweightFrame frame) {
            this.frame = frame;
            interceptAdds(frame);
            canvas.addPaintListener(this::paint);
            Runnable forceRepaint = () -> content.imageUpdated(0, 0, 0, 0);
            HEAVYWEIGHT_REPAINT_HOOKS.put(contentRoot, forceRepaint);
            AwtInput.attach(canvas, frame, contentRoot, forceRepaint);
            canvas.addListener(SWT.Resize, e -> {
                Rectangle a = canvas.getClientArea();
                final int w = Math.max(1, a.width), h = Math.max(1, a.height);
                EventQueue.invokeLater(() -> frame.setSize(w, h));
                scheduleStaggeredRepaints(forceRepaint, 200, 600, 1500);
            });
            canvas.addListener(SWT.Dispose, e -> {
                forgetEmbed(contentRoot);
                disposeFrame(frame);
            });
            scheduleStaggeredRepaints(forceRepaint, 300, 900, 2000);
        }

        /**
         * App code adds Swing content with {@code frame.add(panel)}, but a raw
         * {@code java.awt.Frame} would place it in {@code BorderLayout.CENTER},
         * displacing the frame's own root pane and rendering nothing. Relocate any
         * app-added child into {@link #contentRoot} (which the frame already paints
         * off-screen), restoring the root pane.
         */
        private void interceptAdds(JLightweightFrame frame) {
            frame.addContainerListener(new ContainerListener() {
                @Override
                public void componentAdded(ContainerEvent e) {
                    if (relocating) return;
                    final Component child = e.getChild();
                    if (child == frame.getRootPane()) return;
                    relocating = true;
                    EventQueue.invokeLater(() -> {
                        try {
                            frame.remove(child);
                            frame.add(frame.getRootPane(), BorderLayout.CENTER);
                            contentRoot.add(child);
                            contentRoot.revalidate();
                            contentRoot.repaint();
                            frame.revalidate();
                        } finally {
                            relocating = false;
                        }
                    });
                }

                @Override
                public void componentRemoved(ContainerEvent e) {}
            });
        }

        /**
         * Tears down the off-screen frame when the canvas is disposed, on the <em>SWT thread</em>
         * (not the EDT). Frame teardown makes a main-thread AppKit call (input-method locale during
         * {@code removeNotify}); done here it runs inline (we are the main thread, just like frame
         * construction in {@link #newFrame}), whereas on the EDT it would marshal to the main thread —
         * which on desk has stopped pumping by shutdown and would hang forever. Making the components
         * undisplayable also stops a focused text field's caret-blink Timer, letting the EDT idle so
         * AWT auto-shuts-down (otherwise the non-daemon EDT keeps the JVM alive after the app closes).
         */
        private void disposeFrame(JLightweightFrame frame) {
            try {
                frame.setVisible(false);
                frame.dispose();
            } catch (Throwable ignored) {}
        }

        private void paint(org.eclipse.swt.events.PaintEvent pe) {
            final int[] px;
            final int pw, ph;
            synchronized (this) {
                px = framePixels;
                pw = frameWidth;
                ph = frameHeight;
            }
            if (px == null || pw <= 0 || ph <= 0) return;

            // The off-screen frame's buffer is INT_ARGB_PRE and can leave regions the Swing content
            // didn't cover transparent (notably on Windows, where a resize leaves the reallocated
            // buffer's uncovered area unpainted). Composite over the control's background so those
            // areas show the widget background — a naive alpha-drop would render them black.
            final int bg = backgroundRgb();
            final int[] out = new int[pw * ph];
            for (int i = 0; i < out.length; i++) {
                int argb = px[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0xFF) {
                    out[i] = argb & 0xFFFFFF;
                } else if (a == 0) {
                    out[i] = bg;
                } else {
                    // Premultiplied source over opaque bg: out = src_pre + bg * (255 - a) / 255.
                    int inv = 255 - a;
                    int r = ((argb >> 16) & 0xFF) + (((bg >> 16) & 0xFF) * inv) / 255;
                    int g = ((argb >> 8) & 0xFF) + (((bg >> 8) & 0xFF) * inv) / 255;
                    int b = (argb & 0xFF) + ((bg & 0xFF) * inv) / 255;
                    out[i] = (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255);
                }
            }

            PaletteData palette = new PaletteData(0x00ff0000, 0x0000ff00, 0x000000ff);
            ImageData imageData = new ImageData(pw, ph, 32, palette);
            imageData.setPixels(0, 0, pw * ph, out, 0);
            Image image = new Image(display, imageData);
            try {
                pe.gc.drawImage(image, 0, 0);
            } finally {
                image.dispose();
            }
        }

        /** The control background (0xRRGGBB) to composite uncovered frame pixels over; white fallback. */
        private int backgroundRgb() {
            try {
                org.eclipse.swt.graphics.Color c = canvas.getBackground();
                if (c != null && !c.isDisposed()) {
                    return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
                }
            } catch (Throwable ignored) {}
            return 0xFFFFFF;
        }

        private void frameProduced(int[] argb, int w, int h) {
            synchronized (this) {
                framePixels = argb;
                frameWidth = w;
                frameHeight = h;
            }
            // Repaint the SWT canvas on its own thread (mirrors FXCanvas). The off-screen Swing
            // repaint that produced this frame ran on the EDT; hop back to the SWT thread to blit.
            // Coalesce so a burst of EDT frames doesn't queue a redraw each; skip once the app is
            // tearing down (the canvas/display may be disposed on the SWT side while frames arrive).
            if (display.isDisposed()) return;
            if (redrawPending.compareAndSet(false, true)) {
                try {
                    display.asyncExec(() -> {
                        redrawPending.set(false);
                        if (!canvas.isDisposed()) canvas.redraw();
                    });
                } catch (org.eclipse.swt.SWTException disposed) {
                    redrawPending.set(false); // display disposed between the check and asyncExec
                }
            }
        }

        /** LightweightContent implementation bound to this host. */
        private final class EvolveContent implements LightweightContent {
            // Shared pixel buffer from JLightweightFrame (INT_ARGB_PRE), its stride and size.
            private int[] buffer;
            private int stride;
            private int bufWidth;
            private int bufHeight;
            private final Object lock = new Object();

            @Override
            public JComponent getComponent() {
                return contentRoot;
            }

            @Override public void paintLock() {}
            @Override public void paintUnlock() {}

            @Override
            public void imageBufferReset(int[] data, int x, int y, int width, int height,
                                         int linestride, double scaleX, double scaleY) {
                synchronized (lock) {
                    buffer = data;
                    stride = linestride;
                    bufWidth = width;
                    bufHeight = height;
                }
            }

            @Override
            public void imageReshaped(int x, int y, int width, int height) {
                synchronized (lock) {
                    bufWidth = width;
                    bufHeight = height;
                }
            }

            @Override
            public void imageUpdated(int dirtyX, int dirtyY, int dirtyWidth, int dirtyHeight) {
                final int[] snapshot;
                final int w, h;
                synchronized (lock) {
                    if (buffer == null || bufWidth <= 0 || bufHeight <= 0) return;
                    w = bufWidth;
                    h = bufHeight;
                    // Copy the whole logical frame out of the shared buffer (dropping any
                    // padding the stride carries) so the SWT thread reads a stable image.
                    snapshot = new int[w * h];
                    for (int row = 0; row < h; row++) {
                        System.arraycopy(buffer, row * stride, snapshot, row * w, w);
                    }
                }
                paintHeavyweightDescendants(contentRoot, 0, 0, snapshot, w, h);
                frameProduced(snapshot, w, h);
            }

            /**
             * {@code JLightweightFrame}'s own paint cycle only reaches lightweight
             * descendants (the same {@code GraphicsCallback} filter stock AWT's
             * {@code Container.paint()} uses) — a heavyweight child (e.g. a hosted app's
             * {@code JApplet}) is meant to be painted by its own native peer responding to
             * {@code WM_PAINT}, which never happens here since this frame has no on-screen
             * native window. Paint it ourselves and composite the result directly into the
             * snapshot before it goes out, so a heavyweight subtree isn't silently blank.
             * Runs on the EDT (this method is itself part of JLightweightFrame's own
             * Swing-driven paint dispatch), same thread {@link Component#paint} expects.
             */
            private void paintHeavyweightDescendants(Container root, int offsetX, int offsetY,
                    int[] snapshot, int w, int h) {
                for (Component child : root.getComponents()) {
                    int cx = offsetX + child.getX();
                    int cy = offsetY + child.getY();
                    if (!child.isShowing()) continue;
                    if (!child.isLightweight()) {
                        blitHeavyweight(child, cx, cy, snapshot, w, h);
                    } else if (child instanceof Container) {
                        paintHeavyweightDescendants((Container) child, cx, cy, snapshot, w, h);
                    }
                }
            }

            /** Paints one heavyweight component off-screen and composites it (premultiplied) into snapshot. */
            private void blitHeavyweight(Component heavy, int destX, int destY, int[] snapshot, int w, int h) {
                int cw = heavy.getWidth(), ch = heavy.getHeight();
                if (cw <= 0 || ch <= 0) return;
                BufferedImage img = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                try {
                    heavy.paint(g2);
                } catch (Throwable t) {
                    return; // A component that can't paint off-cycle leaves that region as JLightweightFrame left it.
                } finally {
                    g2.dispose();
                }
                int[] src = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                for (int row = 0; row < ch; row++) {
                    int py = destY + row;
                    if (py < 0 || py >= h) continue;
                    int rowBase = row * cw;
                    int destRowBase = py * w;
                    for (int col = 0; col < cw; col++) {
                        int px = destX + col;
                        if (px < 0 || px >= w) continue;
                        int argb = src[rowBase + col];
                        int a = (argb >>> 24) & 0xFF;
                        if (a == 0) continue;
                        int r = (((argb >> 16) & 0xFF) * a) / 255;
                        int g = (((argb >> 8) & 0xFF) * a) / 255;
                        int b = ((argb & 0xFF) * a) / 255;
                        snapshot[destRowBase + px] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                }
            }

            @Override public void focusGrabbed() {}
            @Override public void focusUngrabbed() {}
            @Override public void preferredSizeChanged(int width, int height) {}
            @Override public void maximumSizeChanged(int width, int height) {}
            @Override public void minimumSizeChanged(int width, int height) {}
        }
    }
}

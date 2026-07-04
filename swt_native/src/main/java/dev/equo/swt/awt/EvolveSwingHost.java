package dev.equo.swt.awt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
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
     * Evolve reimplementation of {@code SWT_AWT.new_Frame}. Returns a Swing
     * {@link Frame} whose content is rendered off-screen and presented on the
     * Flutter surface backing {@code parent}.
     */
    public static Frame newFrame(final Composite parent) {
        if (parent == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
        startToolkit();

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

        void bind(JLightweightFrame frame) {
            this.frame = frame;
            interceptAdds(frame);
            canvas.addPaintListener(this::paint);
            canvas.addListener(SWT.Resize, e -> {
                Rectangle a = canvas.getClientArea();
                final int w = Math.max(1, a.width), h = Math.max(1, a.height);
                EventQueue.invokeLater(() -> frame.setSize(w, h));
            });
            canvas.addListener(SWT.Dispose, e -> disposeFrame(frame));
            AwtInput.attach(canvas, frame);
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
                frameProduced(snapshot, w, h);
            }

            @Override public void focusGrabbed() {}
            @Override public void focusUngrabbed() {}
            @Override public void preferredSizeChanged(int width, int height) {}
            @Override public void maximumSizeChanged(int width, int height) {}
            @Override public void minimumSizeChanged(int width, int height) {}
        }
    }
}

package dev.equo.swing;

import java.awt.Component;
import java.awt.Image;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A surface producer with no GPU behind it, shaped like the one a field deployment ships.
 *
 * <p>It implements nothing of swing-evolve's: a deployment's producer is compiled against its own
 * graphics library, outside the engine's build, and is named by {@code
 * dev.equo.swing.nativeSurfaceProducer} and adapted by method name. Keeping that here is not
 * tidiness — a producer that implemented the interface directly would be a different thing from
 * the one in the field, and the adaptation is part of what is under test.
 *
 * <p>What it reproduces, and nothing more: a surface that <b>opens</b> and a seam that <b>fires
 * late</b>. {@code open} answers a handle that is not zero and is not a texture; {@code transfer}
 * succeeds without moving a pixel. So a run of this snippet cannot show a 3D scene composited from
 * a texture — no client can sample a handle that stands for nothing. What it can show, and what
 * the field measurement was, is whether a frame for that surface ever reaches the wire at all. The
 * trace line is the instrument: {@code N surface frames}, which stayed at zero for the life of the
 * process.
 *
 * <h2>The knobs</h2>
 * <ul>
 *   <li>{@code -Dgl.seamSilentMs} (on {@link GlLikeCanvas}, default 15000) — how long the canvas
 *       renders without the seam firing. The measured application's was about that.
 *   <li>{@code -Dgl.seamThread} — {@code own} (default) runs the engine's render on a thread of
 *       this producer's, which is where a real GL canvas's post-render listeners run; {@code edt}
 *       runs it inline, on whatever thread rendered.
 * </ul>
 */
public final class GlLikeProducer {

    private static final AtomicLong HANDLES = new AtomicLong(0x4000_0000L);

    private final AtomicInteger transfers = new AtomicInteger();

    private volatile long handle;

    private volatile Component rendering;

    private volatile String lastError = "no GPU here, by construction";

    /** Answers a handle that is not zero, because every caller treats zero as the failure. */
    public long open(int width, int height) {
        handle = HANDLES.incrementAndGet();
        System.out.println("[gl-producer] opened " + width + "x" + height
                + ", handle 0x" + Long.toHexString(handle));
        return handle;
    }

    /** Succeeds without moving a pixel: there is no texture to move one into. */
    public int transfer() {
        int n = transfers.incrementAndGet();
        if (n <= 3 || n % 50 == 0) {
            System.out.println("[gl-producer] transfer #" + n);
        }
        return 0;
    }

    public String lastError() {
        return lastError;
    }

    public void close() {
        System.out.println("[gl-producer] closed handle 0x" + Long.toHexString(handle)
                + " after " + transfers.get() + " transfers");
        handle = 0;
    }

    /**
     * Takes the canvas's own render seam, the way a producer that owns the GL context does.
     *
     * <p>Answering {@code false} here is what sends the engine down the paint-and-snapshot path
     * instead, so a canvas this producer cannot reach must say so rather than quietly taking a
     * seam that will never fire.
     */
    public boolean onRendered(Component component, Runnable afterRender) {
        GlLikeCanvas canvas = canvasOf(component);
        if (canvas == null) {
            lastError = "nothing in " + component.getClass().getName() + " renders";
            return false;
        }
        rendering = canvas;
        boolean ownThread = !"edt".equalsIgnoreCase(System.getProperty("gl.seamThread", "own"));
        canvas.addPostRenderListener(() -> {
            if (!ownThread) {
                afterRender.run();
                return;
            }
            Thread render = new Thread(() -> {
                try {
                    afterRender.run();
                } catch (Throwable thrown) {
                    // The engine's render, on the thread a producer offers it. A throw here would
                    // otherwise die with the thread and leave a blank view and a silent log.
                    System.err.println("[gl-producer] the engine's render threw:");
                    thrown.printStackTrace();
                }
            }, "gl-like-render");
            render.setDaemon(true);
            render.start();
        });
        System.out.println("[gl-producer] took the seam on " + canvas.getClass().getSimpleName()
                + ", firing on " + (ownThread ? "its own render thread" : "the painting thread"));
        return true;
    }

    /**
     * The application's own screenshot path, which is what the image route asks for. It answers a
     * real picture, so a run pinned to {@code image} shows a live cube — the control the texture
     * route is read against.
     */
    public Image snapshot(Component component, int width, int height) {
        GlLikeCanvas canvas = canvasOf(component);
        return canvas == null ? null : canvas.getOffScreenImage(width, height);
    }

    private static GlLikeCanvas canvasOf(Component component) {
        if (component instanceof GlLikeCanvas canvas) {
            return canvas;
        }
        if (component instanceof GlLikeCanvasPanel panel) {
            return panel.canvas();
        }
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                GlLikeCanvas found = canvasOf(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}

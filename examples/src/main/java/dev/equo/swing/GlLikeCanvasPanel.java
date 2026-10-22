package dev.equo.swing;

import javax.swing.JPanel;

import java.awt.BorderLayout;

/**
 * The panel a {@link GlLikeCanvas} sits in, as a named class rather than a bare {@code JPanel}.
 *
 * <p>It has a name because a deployment points {@code dev.equo.swing.nativeSurfaceCarry} at it.
 * What moves into the host window is this panel, not the canvas: an application that inspects its
 * canvas's parent — for layout, for a mouse listener, for its own bookkeeping — finds the parent it
 * put there, and the stand-in the client mounts the surface over is left in this panel's place.
 *
 * <p>Two components where one would do is the whole point of the arrangement, and it is the shape
 * that had never been exercised outside a field deployment.
 */
public class GlLikeCanvasPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final GlLikeCanvas canvas;

    public GlLikeCanvasPanel(String name) {
        super(new BorderLayout());
        canvas = new GlLikeCanvas(name);
        add(canvas, BorderLayout.CENTER);
    }

    /** The canvas that renders, which is not this panel. */
    public GlLikeCanvas canvas() {
        return canvas;
    }
}

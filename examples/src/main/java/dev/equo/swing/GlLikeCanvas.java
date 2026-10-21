package dev.equo.swing;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

/**
 * A heavyweight {@link Canvas} shaped like the 3D view in the application this was written
 * against — <b>without a GL driver</b>, and the distinction is worth stating plainly.
 *
 * <p><b>What our side of that application actually sees is not "OpenGL".</b> It is a heavyweight
 * AWT canvas with a real platform peer, whose renderer is <em>expose-driven</em> (it draws in
 * {@code paint}, with no animator anywhere in the JVM), whose pixels Java2D cannot read, and which
 * offers an off-screen image of itself. Every one of those is reproduced here. What is not
 * reproduced is GL itself: context ownership, {@code swapBuffers}, the post-render seam, and the
 * 0x0 reads through JAWT. Those belong to a different investigation and a real driver; conflating
 * the two would make this snippet claim more than it measures.
 *
 * <p>Measured on the application, and why each piece below is shaped the way it is:
 * <ul>
 *   <li>a canvas inside a realized but never-shown window gets <b>no</b> paint on a resize and
 *       none on realization — so an expose-driven renderer never draws a frame unless something
 *       asks it to. {@code NativeSurfaceHost} is what asks, and this canvas answers the same way.</li>
 *   <li>the route that carried its pixels asked for {@code getOffScreenImage()} — the application's
 *       own screenshot path — so both overloads are here, sized and unsized, with the sized one
 *       taking precedence the way the producer calls it.</li>
 *   <li>pointer events reach it only because the stand-in forwards them; a drag that rotates and a
 *       wheel that zooms are what made that measurable, so both are wired here.</li>
 * </ul>
 */
public class GlLikeCanvas extends Canvas {

    private final String name;
    private double yaw = 0.6;
    private double pitch = 0.4;
    private double scale = 1.0;
    private int renders;
    private int lastX;
    private int lastY;
    /** Which gizmo axis the pointer is over (0..2), or -1. Drawn, and drawn silently. */
    private int hoveredAxis = -1;
    /** The axis last clicked, which snaps the camera to it. */
    private int snappedAxis = -1;

    public GlLikeCanvas(String name) {
        this.name = name;
        setBackground(new Color(0x12, 0x14, 0x1A));
        setPreferredSize(new Dimension(520, 420));
        Spin spin = new Spin();
        addMouseListener(spin);
        addMouseMotionListener(spin);
        addMouseWheelListener(spin);
    }

    /** The last render. {@link #paint} composites this; only a render replaces it. */
    private BufferedImage buffer;

    /**
     * An expose. It composites the last render, and renders only when there is none to composite or
     * the extent changed — the lazy-buffer shape, which is what an application with a render target
     * does rather than re-rendering for every exposure.
     *
     * <p><b>Why this shape and not a render per paint.</b> The first version of this rendered on
     * every {@code paint}, and measured on this machine the cube then followed a drag perfectly
     * (192 renders in one gesture) while the application it stands for did not — its 3D view kept
     * its picture through every gesture and came right only on a window resize. So the application
     * is not this shape, and a snippet that renders from {@code paint} cannot reproduce it.
     *
     * <p><b>It is not the application's shape either, and that was measured afterwards.</b> Reading
     * that application's own bytecode, its canvas's {@code update} is five bytes —
     * {@code invokevirtual paint} — so the render is not there. What gates its re-render is an
     * override of {@code repaint()} clearing a backing store that its paint otherwise re-blits,
     * with a size change as the only other invalidator. This class stands for the engine-visible
     * half of that: a redraw that does not reach the component's own {@code repaint()} leaves the
     * last render on screen, and a resize dislodges it.
     *
     * <p>With the render in {@link #update} the reproduction is exact, because the engine's choice
     * of method is the whole defect: AWT delivers a {@code repaint()} to {@code update} and an
     * expose to {@code paint} (measured on JDK 21 — after show paint=1 update=0, after
     * {@code repaint()} paint=0 update=1). An engine that calls {@code paint} for both composites a
     * buffer nothing re-rendered, so the gesture is lost; a resize changes the extent, which is the
     * one thing {@code paint} re-renders for, which is why a resize "fixes" it.
     */
    @Override
    public void paint(Graphics g) {
        if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight()) {
            buffer = render(getWidth(), getHeight());
        }
        g.drawImage(buffer, 0, 0, null);
        System.out.println("[gl] " + name + " composited render #" + renders + " at "
                + getWidth() + "x" + getHeight());
    }

    /**
     * A redraw the application asked for, which is where the render lives. No background clear: the
     * render covers every pixel, as a GL swap would.
     */
    @Override
    public void update(Graphics g) {
        buffer = render(getWidth(), getHeight());
        g.drawImage(buffer, 0, 0, null);
        System.out.println("[gl] " + name + " rendered #" + renders + " at "
                + getWidth() + "x" + getHeight());
    }

    /** The application's own screenshot path, sized — the overload the image route calls first. */
    public BufferedImage getOffScreenImage(int width, int height) {
        return render(Math.max(1, width), Math.max(1, height));
    }

    /** And unsized, which takes the canvas's own extent. */
    public BufferedImage getOffScreenImage() {
        return render(Math.max(1, getWidth()), Math.max(1, getHeight()));
    }

    private BufferedImage render(int w, int h) {
        renders++;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x12, 0x14, 0x1A));
            g2.fillRect(0, 0, w, h);
            drawCube(g2, w, h);
            drawGizmo(g2, w, h);
            g2.setColor(Color.WHITE);
            g2.drawString(name + "  render #" + renders + "  " + w + "x" + h, 10, 18);
        } finally {
            g2.dispose();
        }
        firePostRender();
        return image;
    }

    /**
     * The seam a surface producer takes: the moment this canvas has just rendered, which on a real
     * GL canvas is also the moment its context is current. A producer registers here instead of
     * waiting for a paint, because the renders this canvas does for a wheel or a drag never reach
     * {@code paint} at all.
     *
     * <p><b>The silent window is the defect being reproduced.</b> An application measured in the
     * field renders for fifteen seconds without the seam firing once — its second, off-screen
     * paint branch never calls its own post-render listeners — and only then starts firing. A
     * driver that gives up inside that window is left with a surface nobody carries.
     * {@code -Dgl.seamSilentMs} sets it; 0 fires from the first render, which is the control.
     */
    public void addPostRenderListener(Runnable listener) {
        if (seamOpensAt == 0L) {
            seamOpensAt = System.currentTimeMillis()
                    + Long.getLong("gl.seamSilentMs", 15_000L);
        }
        postRender.add(listener);
    }

    private final java.util.List<Runnable> postRender =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    private volatile long seamOpensAt;

    private void firePostRender() {
        if (postRender.isEmpty() || System.currentTimeMillis() < seamOpensAt) {
            return;
        }
        for (Runnable listener : postRender) {
            listener.run();
        }
    }

    /** A wireframe cube, so a drag that rotates it is visible without a driver. */
    private void drawCube(Graphics2D g2, int w, int h) {
        double[][] corners = {
            {-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1},
            {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1},
        };
        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7},
        };
        int size = (int) (Math.min(w, h) * 0.3 * scale);
        int[] px = new int[corners.length];
        int[] py = new int[corners.length];
        for (int i = 0; i < corners.length; i++) {
            double[] p = rotate(corners[i]);
            px[i] = (int) (w / 2.0 + p[0] * size);
            py[i] = (int) (h / 2.0 + p[1] * size);
        }
        g2.setColor(new Color(0x60, 0xC0, 0xF0));
        g2.setStroke(new java.awt.BasicStroke(2f));
        for (int[] e : edges) {
            g2.drawLine(px[e[0]], py[e[0]], px[e[1]], py[e[1]]);
        }
    }

    /** The orientation gizmo, which is the part that survived in the application's blank pane. */
    private void drawGizmo(Graphics2D g2, int w, int h) {
        int cx = w - 48;
        int cy = 48;
        g2.setStroke(new java.awt.BasicStroke(2f));
        double[][] axes = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        Color[] colours = {new Color(0xE0, 0x50, 0x40), new Color(0x50, 0xD0, 0x60),
            new Color(0x50, 0x90, 0xF0)};
        for (int i = 0; i < 3; i++) {
            double[] p = rotate(axes[i]);
            boolean lit = i == hoveredAxis;
            g2.setStroke(new java.awt.BasicStroke(lit ? 5f : 2f));
            g2.setColor(lit ? colours[i].brighter() : colours[i]);
            g2.drawLine(cx, cy, cx + (int) (p[0] * 26), cy + (int) (p[1] * 26));
            if (i == snappedAxis) {
                g2.fillOval(cx + (int) (p[0] * 26) - 4, cy + (int) (p[1] * 26) - 4, 8, 8);
            }
        }
    }

    private double[] rotate(double[] p) {
        double x = p[0] * Math.cos(yaw) - p[2] * Math.sin(yaw);
        double z = p[0] * Math.sin(yaw) + p[2] * Math.cos(yaw);
        double y = p[1] * Math.cos(pitch) - z * Math.sin(pitch);
        return new double[] {x, y};
    }

    /** A drag rotates and the wheel zooms: the two gestures the forwarding path was proven with. */
    private final class Spin extends MouseAdapter implements MouseWheelListener {
        /**
         * The gizmo lights under the pointer and snaps the camera when clicked — and does both
         * <b>silently</b>, by the same path everything else here uses.
         *
         * <p>It is the loudest form of {@code PF-44} case (b): a cube that does not rotate while
         * you drag is odd, but an affordance that does not light under the pointer reads as a dead
         * control. It is also a probe of its own — an application whose gizmo <em>does</em> light
         * has some path that cooperates with Swing after all, and that changes what the fix has to
         * reach.
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            int over = axisUnder(e.getX(), e.getY());
            if (over == hoveredAxis) {
                return;
            }
            hoveredAxis = over;
            if (Boolean.getBoolean("gl.gesturesRepaint")) {
                repaint();
                return;
            }
            buffer = render(Math.max(1, getWidth()), Math.max(1, getHeight()));
            System.out.println("[gl] " + name + " gizmo hover " + (over < 0 ? "off" : "axis " + over)
                    + ", rendered #" + renders + " — AWT was not told, so the highlight will not"
                    + " show until something else repaints this canvas");
        }

        /** Which gizmo axis the point is near, or -1. The gizmo sits 48 px in from the top-right. */
        private int axisUnder(int x, int y) {
            int cx = getWidth() - 48;
            int cy = 48;
            double[][] axes = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
            for (int i = 0; i < 3; i++) {
                double[] p = rotate(axes[i]);
                int ex = cx + (int) (p[0] * 26);
                int ey = cy + (int) (p[1] * 26);
                if (java.awt.geom.Line2D.ptSegDist(cx, cy, ex, ey, x, y) <= 6) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            int axis = axisUnder(e.getX(), e.getY());
            if (axis < 0) {
                return;
            }
            snappedAxis = axis;
            yaw = axis == 0 ? 0 : axis == 1 ? Math.PI / 2 : Math.PI / 4;
            pitch = axis == 1 ? Math.PI / 2 : 0.2;
            if (Boolean.getBoolean("gl.gesturesRepaint")) {
                repaint();
                return;
            }
            buffer = render(Math.max(1, getWidth()), Math.max(1, getHeight()));
            System.out.println("[gl] " + name + " gizmo CLICKED axis " + axis + ", rendered #"
                    + renders + " — AWT was not told, so the camera has moved and the screen has"
                    + " not");
        }

        /**
         * A rotation, and silent for the same reason the zoom is: on the application measured
         * here neither gesture updates the view until something else repaints it. Reported
         * verbatim — "the cube does not react until I update something, like going to another tab
         * or a resize" — of the drag as much as of the wheel, so a snippet whose drag repaints
         * would be reproducing a cooperating application rather than that one.
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            yaw += (e.getX() - lastX) * 0.01;
            pitch += (e.getY() - lastY) * 0.01;
            lastX = e.getX();
            lastY = e.getY();
            if (Boolean.getBoolean("gl.gesturesRepaint")) {
                repaint();
                return;
            }
            buffer = render(Math.max(1, getWidth()), Math.max(1, getHeight()));
            System.out.println("[gl] " + name + " rotated and rendered #" + renders
                    + " — AWT was not told, so nothing will show it until something else repaints"
                    + " this canvas");
        }

        /**
         * A zoom that changes what the view would draw and <b>tells AWT nothing</b>, which is
         * {@code PF-44} case (b) and what the application measured here does.
         *
         * <p>Its renderer is its own: the gesture moves the camera and the next render will use
         * it, and nothing in that path goes through {@code repaint()}. On a real heavyweight
         * canvas that costs nothing, because the native peer's own loop draws anyway. Here a leaf
         * is lightweight, so the only drawing that is ever mirrored is what a paint records — and
         * a paint nobody asks for never happens.
         *
         * <p>Measured on that application: a zoom produced 4 frames carrying an op-list in 215 s
         * against 6 in a 215 s idle control — below the floor, so nothing — while a window resize
         * produced 13, every frame carrying one, plus the line that separates the two gestures,
         * {@code [NativeSurface] ViewCanvas: image frames at 357x291}. The view does not update
         * until something else repaints it: a resize, or leaving the tab and coming back.
         *
         * <p>{@code -Dgl.gesturesRepaint=true} is the control — the same gestures with the
         * {@code repaint()} an application that cooperates with Swing would make.
         */
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            scale = Math.max(0.2, Math.min(3.0, scale - e.getWheelRotation() * 0.08));
            if (Boolean.getBoolean("gl.gesturesRepaint")) {
                repaint();
                return;
            }
            // The render their canvas would do on its own thread, into its own buffer: the model
            // moved and the pixels follow, with AWT none the wiser.
            buffer = render(Math.max(1, getWidth()), Math.max(1, getHeight()));
            System.out.println("[gl] " + name + " zoomed to " + String.format("%.2f", scale)
                    + " and rendered #" + renders + " — AWT was not told, so nothing will show it"
                    + " until something else repaints this canvas");
        }
    }
}

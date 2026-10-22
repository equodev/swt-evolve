package dev.equo.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * A view shaped like the map in the application this was written against: a dark ground, a grid
 * with labelled lines, coloured polylines over it, and a scale bar in a corner — all ordinary
 * Java2D, which is what that view turned out to be.
 *
 * <p><b>Why ordinary Java2D is the faithful choice here, and not a shortcut.</b> Measured on the
 * application: the map has no native-surface entry at all. Only its 3D view is adopted into a host
 * window; the map's pixels travel as op-lists like any other Swing painting. So a panel that
 * paints this way exercises exactly the path the map's pixels take, and a defect that loses the
 * map is a defect in that path.
 *
 * <p><b>And the rubber band is the other half, deliberately.</b> The application's black ghosts
 * were reported on its rubber band and its line tool, and its drawing idiom was measured on the
 * wire as "0 drawing ops baked, 5 in XOR" — ten times, unanimous. So the band here is drawn the
 * same way the application draws it: {@link java.awt.Component#getGraphics()} outside the paint
 * cycle, in {@link Graphics2D#setXORMode XOR}, erased by drawing it again. A band that repainted
 * the component instead would be a different mechanism and would not reproduce anything.
 *
 * <p>Drag with the left button for a band; the line tool is the right button, which leaves its
 * line behind so a ghost has something to be a ghost of.
 */
public class MapLikeView extends JPanel {

    /**
     * The canvas ground.
     *
     * <p>Lighter than the application's on purpose, and it is not cosmetic. An XOR stroke the
     * client fails to cancel paints <b>opaque black</b> on the overlay layer — so on a ground that
     * is itself near-black the ghosts are perfectly real and perfectly invisible, which is exactly
     * how this reproduction read for three runs: no band, no preview, no residue, and the drawing
     * arriving at the client all along. The application's own ground is dark too, but its map has
     * content for the black to land on.
     *
     * <p>{@code -Dground=dark} restores the near-black one, for comparing against a screenshot.
     */
    private static final Color GROUND = "dark".equalsIgnoreCase(System.getProperty("ground", "mid"))
            ? new Color(0x10, 0x10, 0x14) : new Color(0x4A, 0x52, 0x60);
    private static final Color GRID = new Color(0x30, 0x34, 0x40);
    private static final Color LABEL = new Color(0xB0, 0xB8, 0xC8);
    private static final Color[] TRACKS = {
        new Color(0xE0, 0x60, 0x40), new Color(0x50, 0xC0, 0x90), new Color(0xF0, 0xC0, 0x40),
    };

    private final String name;
    private final int seed;
    private int paints;

    /**
     * The colour the band is XOR'd against.
     *
     * <p>Not a detail, and the reason the first version of this view reproduced nothing. XOR
     * erases exactly when the mode's colour is the one actually under the ink: drawing the same
     * shape twice then cancels. An application that passes a fixed colour instead — white, or its
     * own background constant — erases exactly only where the ground happens to match, and leaves
     * the difference everywhere else. On a dark canvas that difference reads as black smears,
     * which is what the ghosts were reported as.
     *
     * <p><b>Black by default, and that is the faithful choice rather than an arbitrary one.</b>
     * The client composites XOR as {@code |dst - (src ^ argb)|}, which equals Java's
     * {@code src ^ dst ^ argb} exactly when {@code src ^ argb} is white — the case an application
     * is in whenever it wants a stroke visible over anything, which is what a rubber band is for.
     * White ink against a black XOR colour is that case. An earlier version of this view used the
     * canvas ground as the XOR colour, which is outside it, and the strokes came out ocre: a real
     * difference, correctly rendered, between two idioms — and read for a while as a defect.
     *
     * <p>{@code -Dxor=ground} picks the unfaithful one back up, for seeing that difference.
     */
    private static final Color XOR_AGAINST =
            "ground".equalsIgnoreCase(System.getProperty("xor", "black")) ? GROUND : Color.BLACK;

    /** The band's current rectangle while a drag is in flight, in this component's coordinates. */
    private Rectangle band;
    private Point bandFrom;

    /** Lines the right button left behind, so the ghosts have something to haunt. */
    private final java.util.List<int[]> lines = new java.util.ArrayList<>();
    private Point lineFrom;

    /**
     * The line tool's live preview: where the line would land, following the pointer.
     *
     * <p>This is the idiom the ghosts were reported on, and it is not the same as drawing once on
     * release. The application's tool takes a first click and then <em>follows the mouse</em>,
     * drawing a preview at each position and erasing the one before it — a draw and an erase per
     * mouse event, dozens a second, all of them immediate-mode XOR with no repaint behind them.
     * That is what gives an unmatched erase somewhere to go wrong.
     */
    private Point linePreviewTo;

    /**
     * A status line that follows the pointer, like the one the application has along its bottom
     * edge — and the reason the first two versions of this view reproduced nothing.
     *
     * <p><b>It is not decoration, it is the mechanism.</b> Immediate-mode drawing accumulates in a
     * list that ships whole each frame, so an XOR erase cancels against its own stroke riding with
     * it. The engine drops that list for any window that was really repainted in the same flush,
     * because a real paint erases immediate drawing in Swing. A status line that updates on every
     * mouse move repaints the window on every move — so the list is dropped mid-gesture, and the
     * next XOR erase arrives with nothing underneath to cancel. It draws instead of erasing, and
     * that stroke is a ghost.
     *
     * <p>Reported on the application, and each of the four readings follows from this: the
     * crosshair alone leaves them (it is immediate drawing like any other), they pile up through a
     * gesture, a resize or a zoom clears them all at once (both are real repaints, which drop the
     * list), and leaving the canvas freezes them without clearing (no more moves, so no more
     * unmatched erases, and no repaint either).
     *
     * <p>{@code -Dstatus=off} takes it away, which is the control: the same gestures, nothing
     * repainting underneath them.
     */
    private final boolean statusFollowsPointer =
            !"off".equalsIgnoreCase(System.getProperty("status", "on"));

    /**
     * Where the status goes — <b>a component of its own, and that is the whole point.</b>
     *
     * <p>The first version repainted this view itself, which reproduced the opposite symptom: the
     * band never appeared during a drag at all. Correctly so, and it is Swing's doing rather than
     * ours — a real paint on the component that holds immediate drawing erases that drawing, so
     * there was never anything to accumulate.
     *
     * <p>The application's status line is elsewhere in the window. Repainting it leaves the
     * canvas's pixels alone, so the band stays on screen — while the engine drops the window's
     * accumulated overlay anyway, because it drops per WINDOW and Swing erases per COMPONENT.
     * That asymmetry is the thing under test: after the drop, the next XOR erase has nothing
     * beneath it to cancel against, so it draws, and that stroke is a ghost.
     */
    private java.util.function.Consumer<String> statusSink = text -> { };

    /** Where the status line is, so the view can update it without repainting itself. */
    public void statusGoesTo(java.util.function.Consumer<String> sink) {
        this.statusSink = sink;
    }

    public MapLikeView(String name, int seed) {
        this.name = name;
        this.seed = seed;
        setBackground(GROUND);
        setPreferredSize(new Dimension(600, 420));
        Tool tool = new Tool();
        addMouseListener(tool);
        addMouseMotionListener(tool);
        if (Boolean.getBoolean("map.scriptedBandDrag")) {
            scriptBandDrag(tool);
        }
    }

    /**
     * A band drag driven from here instead of from a hand, for the one case a hand found and a
     * test had not: dragging so the pointer leaves the bottom of the canvas and passes over the
     * status strip leaves a band behind per move, and only a window resize clears them.
     *
     * <p>It runs the real handlers with real events, so it goes through {@code getGraphics}, the
     * XOR erase and the status sink exactly as the pointer does — the only thing it replaces is
     * who moves the mouse. The last few moves are deliberately below {@code getHeight()}, which is
     * where the hand was when the residue appeared.
     *
     * <pre>./gradlew :examples:runExample -PmainClass=… -Dmap.scriptedBandDrag=true</pre>
     */
    private void scriptBandDrag(Tool tool) {
        // Fast, because the speed is the point: a hand produces dozens of moves a second and the
        // EDT coalesces several into one flush, which is where the erase and the stroke it erases
        // arrive together. A leisurely script never coalesces and so never reaches that case —
        // measured, a 600 ms script leaves exactly one band standing while a hand leaves several.
        javax.swing.Timer timer = new javax.swing.Timer(
                Integer.getInteger("map.scriptedBandDragMs", 16), null);
        timer.addActionListener(new java.awt.event.ActionListener() {
            private int step = -1;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent ignored) {
                if (getWidth() <= 0) {
                    return; // not laid out yet
                }
                step++;
                int x = 60 + step * 6;
                // Down the canvas and then past its bottom edge, which is the gesture reported.
                int y = 60 + step * 12;
                if (step == 0) {
                    tool.mousePressed(event(java.awt.event.MouseEvent.MOUSE_PRESSED, 60, 60, 1));
                    System.out.println("[map] scripted band: pressed at 60,60 in a "
                            + getWidth() + "x" + getHeight() + " canvas");
                    return;
                }
                if (y > getHeight() + 120 || step > 60) {
                    tool.mouseReleased(event(java.awt.event.MouseEvent.MOUSE_RELEASED, x, y, 1));
                    System.out.println("[map] scripted band: released after " + step + " moves");
                    timer.stop();
                    return;
                }
                tool.mouseDragged(event(java.awt.event.MouseEvent.MOUSE_DRAGGED, x, y, 0));
                System.out.println("[map] scripted band: move " + step + " to " + x + "," + y
                        + (y > getHeight() ? "  (past the bottom edge)" : ""));
            }

            private MouseEvent event(int id, int x, int y, int clicks) {
                return new MouseEvent(MapLikeView.this, id, System.currentTimeMillis(),
                        java.awt.event.InputEvent.BUTTON1_DOWN_MASK, x, y, clicks, false,
                        MouseEvent.BUTTON1);
            }
        });
        timer.setInitialDelay(3000);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paints++;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(GROUND);
            g2.fillRect(0, 0, w, h);
            drawGrid(g2, w, h);
            drawTracks(g2, w, h);
            drawLines(g2);
            drawScaleBar(g2, h);
            g2.setColor(LABEL);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g2.drawString(name + "  ·  painted " + paints + "x at " + w + "x" + h, 12, 20);

        } finally {
            g2.dispose();
        }
        System.out.println("[map] " + name + " painted " + paints + " time(s) at "
                + getWidth() + "x" + getHeight());
    }

    private void drawGrid(Graphics2D g2, int w, int h) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        for (int x = 60; x < w; x += 60) {
            g2.setColor(GRID);
            g2.drawLine(x, 0, x, h);
            g2.setColor(LABEL);
            g2.drawString("IL " + (2000 + x), x + 3, 12);
        }
        for (int y = 60; y < h; y += 60) {
            g2.setColor(GRID);
            g2.drawLine(0, y, w, y);
            g2.setColor(LABEL);
            g2.drawString("XL " + (1000 + y), 3, y - 3);
        }
    }

    private void drawTracks(Graphics2D g2, int w, int h) {
        g2.setStroke(new BasicStroke(2f));
        for (int t = 0; t < TRACKS.length; t++) {
            g2.setColor(TRACKS[t]);
            int steps = 24;
            int[] xs = new int[steps];
            int[] ys = new int[steps];
            for (int i = 0; i < steps; i++) {
                double u = i / (double) (steps - 1);
                xs[i] = (int) (u * (w - 40)) + 20;
                ys[i] = (int) (h / 2.0
                        + Math.sin(u * 6 + t + seed) * (h / 5.0)
                        + Math.cos(u * 11 + seed) * (h / 18.0));
            }
            g2.drawPolyline(xs, ys, steps);
        }
    }

    private void drawLines(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        for (int[] l : lines) {
            g2.drawLine(l[0], l[1], l[2], l[3]);
        }
    }

    private void drawScaleBar(Graphics2D g2, int h) {
        int y = h - 26;
        g2.setColor(new Color(0xE0, 0x50, 0x40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(24, y, 184, y);
        g2.drawLine(24, y - 5, 24, y + 5);
        g2.drawLine(184, y - 5, 184, y + 5);
        g2.setColor(LABEL);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.drawString("1,500 feet", 70, y - 8);
        g2.drawString("400 meters", 68, y + 16);
    }

    /**
     * The band and the line, drawn the way the application draws them: straight onto the
     * component's own {@code Graphics}, outside any paint, in XOR, and erased by repeating the
     * same call. Nothing here repaints the component, which is the point.
     */
    private final class Tool extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            System.out.println("[map] " + name + " PRESS at " + e.getX() + "," + e.getY()
                    + " button " + e.getButton());
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (lineFrom == null) {
                    lineFrom = e.getPoint();   // armed: the preview now follows the pointer
                    linePreviewTo = null;
                } else {
                    // Commit the way a tool does: the line becomes the view's, and the only thing
                    // the tool does about the preview still on screen is repaint the region it
                    // occupied. It does not XOR-erase it — a real paint over those pixels is what
                    // erases immediate drawing in Swing, and relying on that is the idiom.
                    //
                    // The region, not the whole view, because that is the difference that matters
                    // here: a host that reads "part of the canvas was repainted" as "all of it"
                    // drops drawing the paint never covered, and one that reads it as "none of it"
                    // leaves a residue nothing clears until a window resize. Both were measured,
                    // the second from an application's line tool, and both are why this commits
                    // through a damaged rectangle rather than through repaint().
                    java.awt.Rectangle damage =
                            new java.awt.Rectangle(lineFrom.x, lineFrom.y, 0, 0);
                    damage.add(linePreviewTo != null ? linePreviewTo : e.getPoint());
                    damage.add(e.getPoint());
                    damage.grow(4, 4); // the stroke is drawn centred on the line
                    lines.add(new int[] {lineFrom.x, lineFrom.y, e.getX(), e.getY()});
                    lineFrom = null;
                    linePreviewTo = null;
                    repaint(damage.x, damage.y, damage.width, damage.height);
                    System.out.println("[map] " + name + " committed a line, repainting " + damage);
                }
                return;
            }
            bandFrom = e.getPoint();
            band = null;
        }

        /** The status line follows the pointer, which repaints the window on every move. */
        @Override
        public void mouseMoved(MouseEvent e) {
            if (statusFollowsPointer) {
                statusSink.accept("IL " + (2000 + e.getX()) + "   XL " + (1000 + e.getY()));
            }
            if (lineFrom == null) {
                return;
            }
            // The preview: erase the last one, draw the new one, both in XOR, neither repainting.
            Graphics2D g2 = (Graphics2D) getGraphics();
            if (g2 == null) {
                return;
            }
            try {
                g2.setXORMode(XOR_AGAINST);
                g2.setColor(Color.WHITE);
                if (linePreviewTo != null) {
                    g2.drawLine(lineFrom.x, lineFrom.y, linePreviewTo.x, linePreviewTo.y);
                }
                linePreviewTo = e.getPoint();
                g2.drawLine(lineFrom.x, lineFrom.y, linePreviewTo.x, linePreviewTo.y);
            } finally {
                g2.dispose();
            }
        }

        private int dragsSeen;

        @Override
        public void mouseDragged(MouseEvent e) {
            // What Java actually receives, which is the question a ghost turned into: a band left
            // on screen is a band whose erase never came, and an erase that never came is a move
            // or a release the engine did not deliver.
            dragsSeen++;
            if (statusFollowsPointer) {
                statusSink.accept("IL " + (2000 + e.getX()) + "   XL " + (1000 + e.getY()));
            }
            if (bandFrom == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) getGraphics();
            if (g2 == null) {
                // Said out loud, because silently skipping a move leaves `band` where it was and
                // nothing erases it: the band freezes exactly there and stays. A frozen band is
                // what a residue looks like from the outside, and the two were confused for a day.
                System.out.println("[map] " + name + " drag #" + dragsSeen
                        + ": getGraphics() returned NULL — the move is skipped and the band is"
                        + " left at " + band);
                return;
            }
            try {
                g2.setXORMode(XOR_AGAINST);
                g2.setColor(Color.WHITE);
                if (band != null) {
                    g2.drawRect(band.x, band.y, band.width, band.height);
                }
                band = rectOf(bandFrom, e.getPoint());
                g2.drawRect(band.x, band.y, band.width, band.height);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            System.out.println("[map] " + name + " RELEASE at " + e.getX() + "," + e.getY()
                    + " after " + dragsSeen + " drag(s)");
            dragsSeen = 0;
            if (band != null) {
                Graphics2D g2 = (Graphics2D) getGraphics();
                if (g2 != null) {
                    try {
                        g2.setXORMode(XOR_AGAINST);
                        g2.setColor(Color.WHITE);
                        g2.drawRect(band.x, band.y, band.width, band.height);
                    } finally {
                        g2.dispose();
                    }
                }
                System.out.println("[map] " + name + " band released at " + band);
            }
            band = null;
            bandFrom = null;
        }

        private Rectangle rectOf(Point a, Point b) {
            return new Rectangle(Math.min(a.x, b.x), Math.min(a.y, b.y),
                    Math.abs(a.x - b.x), Math.abs(a.y - b.y));
        }
    }
}

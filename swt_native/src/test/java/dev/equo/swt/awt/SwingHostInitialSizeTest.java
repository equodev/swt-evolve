package dev.equo.swt.awt;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The frame is created before the window opens, while the canvas still reports a 0x0 client area.
 * A {@code JSplitPane} keeps recomputing its divider from its children's preferred sizes until it
 * has been painted once, so if it is painted at a placeholder size its top component stays squeezed
 * when the frame grows.
 *
 * <p>A panel stands in for the frame: constructing an AWT frame needs a display, and CI runs
 * headless. {@link #resizeAndPaint} mirrors {@code JLightweightFrame.reshape}, which allocates no
 * buffer, and so paints nothing, at 0x0. The tree is never shown, so it is laid out and painted on
 * the test thread: on macOS this suite gives the main thread to Cocoa and the AWT event queue never
 * runs.
 */
class SwingHostInitialSizeTest {

    private static final int VIEWPORT_HEIGHT = 100;
    private static final int INNER_DIVIDER = 200;
    private static final int OUTER_DIVIDER = 600;
    private static final int APP_DIVIDER = 180;
    private static final int TOP_MINIMUM_HEIGHT = 62;

    @Test
    void anIslandFrameKeepsAnEmptyTablesPreferredHeightAboveTheDivider() {
        JSplitPane split = splitWithEmptyTable();
        JPanel frame = new JPanel(new BorderLayout());
        frame.add(split);

        EvolveSwingHost.sizeFrame(frame, new Rectangle(0, 0, 0, 0));
        layOutAndPaint(frame);
        EvolveSwingHost.sizeFrame(frame, new Rectangle(0, 0, 420, 468));
        layOutAndPaint(frame);

        assertTableAreaKept(split);
    }

    @Test
    void aBlittedFrameKeepsAnEmptyTablesPreferredHeightAboveTheDivider() {
        JSplitPane split = splitWithEmptyTable();
        EvolveSwingHost.ContentRoot root = new EvolveSwingHost.ContentRoot(() -> {});
        root.add(split);
        JPanel frame = new JPanel(new BorderLayout());
        frame.add(root);

        EvolveSwingHost.sizeBlitFrame(frame, root, new Rectangle(0, 0, 0, 0));
        layOutAndPaint(frame);
        EvolveSwingHost.sizeBlitFrame(frame, root, new Rectangle(0, 0, 420, 468));
        layOutAndPaint(frame);

        assertTableAreaKept(split);
    }

    @Test
    void contentThatRepaintsFromItselfKeepsItsDividersWhenAddedBeforeTheFrameHasASize() {
        JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JLabel("Selection"), new JLabel("Units"));
        inner.setDividerLocation(INNER_DIVIDER);
        JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inner, new JPanel());
        outer.setDividerLocation(OUTER_DIVIDER);
        JPanel applet = new JPanel(new BorderLayout());
        applet.add(outer);
        EvolveSwingHost.ContentRoot root = new EvolveSwingHost.ContentRoot(() -> {});
        JPanel frame = new JPanel(new BorderLayout());
        frame.add(root);

        EvolveSwingHost.sizeBlitFrame(frame, root, new Rectangle(0, 0, 0, 0));
        root.host(applet);
        paintFromItselfIfInTree(applet, frame);
        EvolveSwingHost.sizeBlitFrame(frame, root, new Rectangle(0, 0, 384, 820));
        layOutAndPaint(frame);

        int units = inner.getBottomComponent().getHeight();
        int expected = OUTER_DIVIDER - INNER_DIVIDER - 2 * outer.getDividerSize();
        assertTrue(units >= expected, "middle pane " + units + " px high, expected at least " + expected);
    }

    @Test
    void contentPlacedAtTheFirstRealSizeKeepsTheDividerTheAppSet() {
        Embedding e = new Embedding();

        EvolveSwingHost.sizeBlitFrame(e.frame, e.root, new Rectangle(0, 0, 0, 0));
        e.frame.validate();
        e.root.host(e.applet);
        EvolveSwingHost.sizeBlitFrame(e.frame, e.root, new Rectangle(0, 0, 568, 729));
        e.frame.validateFromRootPaneAndPaint();
        layOutAndPaint(e.frame);

        assertDividerKept(e.split);
    }

    @Test
    void contentLaidOutInThePlaceholderFrameBeforeItWasRelocatedKeepsTheDividerTheAppSet() {
        Embedding e = new Embedding();

        EvolveSwingHost.sizeBlitFrame(e.frame, e.root, new Rectangle(0, 0, 0, 0));
        e.frame.validate();
        // The application added the applet to the frame itself, which laid it out before the host
        // moved it into the content root.
        e.applet.setBounds(0, 0, 0, 1);
        layOut(e.applet);
        e.root.host(e.applet);
        EvolveSwingHost.sizeBlitFrame(e.frame, e.root, new Rectangle(0, 0, 590, 1493));
        e.frame.validateFromRootPaneAndPaint();
        layOutAndPaint(e.frame);
        EvolveSwingHost.sizeBlitFrame(e.frame, e.root, new Rectangle(0, 0, 568, 729));
        layOutAndPaint(e.frame);

        assertDividerKept(e.split);
    }

    @Test
    void placedContentKeepsTheDividerTheAppSetWhileTheCanvasIsEmpty() {
        Embedding e = new Embedding();

        resizeCanvas(e.frame, e.root, 0, 0);
        e.frame.validate();
        e.root.host(e.applet);
        resizeCanvas(e.frame, e.root, 568, 729);
        resizeCanvas(e.frame, e.root, 0, 0);
        resizeCanvas(e.frame, e.root, 568, 729);

        assertDividerKept(e.split);
    }

    /** What the canvas's Resize listener runs, then what Swing does with what it revalidates. */
    private static void resizeCanvas(LightweightFrame frame, EvolveSwingHost.ContentRoot root, int width, int height) {
        Rectangle area = new Rectangle(0, 0, width, height);
        EvolveSwingHost.sizeBlitFrame(frame, root, area);
        EvolveSwingHost.layOutBlitContent(frame.rootPane, root, area);
        frame.validateFromRootPaneAndPaint();
    }

    /**
     * An applet is its own repaint root: Swing paints it at its own bounds, with nothing above it
     * consulted, as soon as it is part of a displayable tree. Here that tree is the frame's.
     */
    private static void paintFromItselfIfInTree(JPanel applet, JPanel frame) {
        if (!SwingUtilities.isDescendingFrom(applet, frame)) return;
        applet.setSize(applet.getPreferredSize());
        layOut(applet);
        paintInto(applet);
    }

    private static JSplitPane splitWithEmptyTable() {
        JTable table = new JTable(new DefaultTableModel(new String[] {"Name", "Type", "Age", "Update Date"}, 0));
        table.setPreferredScrollableViewportSize(new Dimension(400, VIEWPORT_HEIGHT));
        return new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JLabel("Details"));
    }

    /**
     * A selection table whose panel can't go below a header and one row, above a tall details area,
     * in a split with no border, so that its children fill it.
     */
    private static JSplitPane splitWithMinimumTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.add(new JScrollPane(new JTable(new DefaultTableModel(new String[] {"Name", "Type"}, 0))));
        top.setMinimumSize(new Dimension(266, TOP_MINIMUM_HEIGHT));
        JPanel details = new JPanel();
        details.setPreferredSize(new Dimension(400, 600));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, details);
        split.setBorder(null);
        split.setDividerLocation(APP_DIVIDER);
        return split;
    }

    private static void assertDividerKept(JSplitPane split) {
        int top = split.getTopComponent().getHeight();
        assertTrue(top >= APP_DIVIDER, "top component " + top + " px high (divider at " + split.getDividerLocation()
                + "), expected the " + APP_DIVIDER + " px the divider was set to");
    }

    /** An applet holding {@link #splitWithMinimumTop}, not yet placed, and the frame it goes into. */
    private static final class Embedding {
        final JSplitPane split = splitWithMinimumTop();
        final JPanel applet = new JPanel(new BorderLayout());
        final EvolveSwingHost.ContentRoot root = new EvolveSwingHost.ContentRoot(() -> {});
        final LightweightFrame frame = new LightweightFrame(root);

        Embedding() {
            applet.add(split);
        }
    }

    /**
     * Stands for {@code JLightweightFrame}: its root pane fills it, and it has a peer, so {@link
     * #validate} lays its tree out.
     */
    private static final class LightweightFrame extends JPanel {
        private final JPanel rootPane = new JPanel(new BorderLayout());

        LightweightFrame(EvolveSwingHost.ContentRoot root) {
            super(new BorderLayout());
            rootPane.add(root);
            add(rootPane);
        }

        @Override
        public void validate() {
            layOut(this);
        }

        /**
         * What {@code RepaintManager} does with content that revalidates and repaints itself: lay
         * out from the root pane, its validate root, at the bounds the frame last gave it, then
         * paint.
         */
        void validateFromRootPaneAndPaint() {
            layOut(rootPane);
            paintInto(this);
        }
    }

    private static void assertTableAreaKept(JSplitPane split) {
        int top = split.getTopComponent().getHeight();
        assertTrue(top >= VIEWPORT_HEIGHT,
                "table area " + top + " px high, expected at least its " + VIEWPORT_HEIGHT + " px viewport");
    }

    /** What {@code JLightweightFrame} does with a new size: lay out, then paint only if it allocated a buffer. */
    private static void layOutAndPaint(JPanel frame) {
        layOut(frame);
        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) return;
        paintInto(frame);
    }

    private static void paintInto(JComponent c) {
        BufferedImage buffer = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        try {
            c.paint(g);
        } finally {
            g.dispose();
        }
    }

    /** {@code validate()} skips a tree that has no peer, so run each layout manager directly. */
    private static void layOut(Container c) {
        c.doLayout();
        for (Component child : c.getComponents()) {
            if (child instanceof Container) layOut((Container) child);
        }
    }
}

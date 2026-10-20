package dev.equo.swt.awt;

import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The frame is created before the window opens, while the canvas still reports a 0x0 client area.
 * A {@code JSplitPane} keeps recomputing its divider from its children's preferred sizes until it
 * has been painted once. The frame paints whenever it has a positive size, so if it starts at any
 * positive size the split pane is painted with its top component squeezed and keeps it that way
 * when the frame grows.
 *
 * <p>A panel stands in for the frame: constructing an AWT frame needs a display, and CI runs
 * headless. What is under test is {@link EvolveSwingHost#sizeFrame}, the one place both
 * {@code newFrame} and the canvas Resize listener size the frame; {@link #resizeAndPaint} mirrors
 * {@code JLightweightFrame.reshape}, which allocates no buffer, and so paints nothing, at 0x0. The tree is
 * never shown, so it is laid out and painted on the test thread: on macOS this suite gives the main
 * thread to Cocoa and the AWT event queue never runs.
 */
class SwingHostInitialSizeTest {

    private static final int VIEWPORT_HEIGHT = 100;

    @Test
    void anEmptyTableKeepsItsPreferredHeightAboveTheDivider() {
        JTable table = new JTable(new DefaultTableModel(new String[] {"Name", "Type", "Age", "Update Date"}, 0));
        table.setPreferredScrollableViewportSize(new Dimension(400, VIEWPORT_HEIGHT));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JLabel("Details"));
        JPanel frame = new JPanel(new BorderLayout());
        frame.add(split);

        resizeAndPaint(frame, new Rectangle(0, 0, 0, 0));
        resizeAndPaint(frame, new Rectangle(0, 0, 420, 468));

        int top = split.getTopComponent().getHeight();
        assertTrue(top >= VIEWPORT_HEIGHT,
                "table area " + top + " px high, expected at least its " + VIEWPORT_HEIGHT + " px viewport");
    }

    /** What {@code JLightweightFrame} does with a new size: lay out, then paint only if it allocated a buffer. */
    private static void resizeAndPaint(JPanel frame, Rectangle canvasArea) {
        EvolveSwingHost.sizeFrame(frame, canvasArea);
        layOut(frame);
        if (frame.getWidth() <= 0 || frame.getHeight() <= 0) return;
        BufferedImage buffer = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = buffer.getGraphics();
        try {
            frame.paint(g);
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

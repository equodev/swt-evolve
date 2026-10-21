package dev.equo.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * A view shaped like the cross-section in the application this was written against: a canvas on a
 * black ground inside a scroll pane, with a labelled ruler down its left edge, a scale bar in the
 * corner, and zoom controls floating at the top right.
 *
 * <p><b>Every one of those parts is here because one of them went missing.</b> Reported on the
 * application, with a screenshot of the same view working beside it: the canvas was entirely
 * black — no ruler numbers, no content, no scale bar, no zoom controls, and neither scrollbar —
 * while the colour-map panel and the icon toolbar <em>outside</em> the island kept rendering. So
 * the interesting part is not that a view can be blank; it is that a window can lose its own
 * chrome while its neighbours keep theirs, and a reproduction with a bare canvas would have
 * nothing to lose.
 *
 * <p>The geometry matters too and is deliberate: on the application the island's box was the
 * canvas <em>only</em> ({@code 273,131 721x639}), with the panel and the toolbar at window-x
 * 10..260, outside it. {@link SwingIslandWorkbenchSnippet} puts its SWT chrome outside the
 * embedded composite for the same reason.
 */
public class SectionLikeView extends JPanel {

    private static final Color GROUND = new Color(0x08, 0x08, 0x0C);
    private static final Color INK = new Color(0xC8, 0xCC, 0xD8);

    private final String name;
    private final Canvas canvas;

    public SectionLikeView(String name) {
        super(new BorderLayout());
        this.name = name;
        this.canvas = new Canvas();

        JScrollPane scroller = new JScrollPane(canvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroller.setBorder(null);
        scroller.getViewport().setBackground(GROUND);

        add(scroller, BorderLayout.CENTER);
        add(zoomControls(), BorderLayout.EAST);
        setBackground(GROUND);
    }

    /** The floating zoom controls: two buttons and a slider, the ones the application lost. */
    private JPanel zoomControls() {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBackground(GROUND);
        JButton in = new JButton("+");
        JButton out = new JButton("−");
        JSlider zoom = new JSlider(SwingConstants.VERTICAL, 0, 100, 50);
        zoom.setBackground(GROUND);
        for (JButton b : new JButton[] {in, out}) {
            b.setMargin(new java.awt.Insets(0, 4, 0, 4));
            b.addActionListener(e -> System.out.println(
                    "[section] " + name + " zoom " + b.getText() + " pressed"));
        }
        zoom.addChangeListener(e -> {
            if (!zoom.getValueIsAdjusting()) {
                System.out.println("[section] " + name + " zoom slider at " + zoom.getValue());
            }
        });
        column.add(in);
        column.add(zoom);
        column.add(out);
        column.add(Box.createVerticalGlue());
        return column;
    }

    /** The section itself: bigger than its viewport, so both scrollbars have something to do. */
    private final class Canvas extends JPanel {
        private int paints;

        Canvas() {
            setBackground(GROUND);
            setPreferredSize(new Dimension(1200, 900));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paints++;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(GROUND);
                g2.fillRect(0, 0, w, h);
                ruler(g2, h);
                horizon(g2, w, h);
                scaleBar(g2, h);
                g2.setColor(INK);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                g2.drawString(name + "  ·  painted " + paints + "x at " + w + "x" + h, 70, 18);
            } finally {
                g2.dispose();
            }
            System.out.println("[section] " + name + " canvas painted " + paints
                    + " time(s) at " + getWidth() + "x" + getHeight());
        }

        /** The labelled ruler down the left edge — the first thing the application lost. */
        private void ruler(Graphics2D g2, int h) {
            g2.setColor(INK);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.drawLine(56, 0, 56, h);
            for (int y = 40; y < h; y += 50) {
                g2.drawLine(48, y, 56, y);
                g2.drawString(String.valueOf(-6000 - y * 4), 8, y + 4);
            }
            g2.drawString("TVDSS (feet)", 4, 14);
        }

        private void horizon(Graphics2D g2, int w, int h) {
            g2.setColor(new Color(0xE8, 0x60, 0x30));
            g2.setStroke(new BasicStroke(2.5f));
            int steps = 40;
            int[] xs = new int[steps];
            int[] ys = new int[steps];
            for (int i = 0; i < steps; i++) {
                double u = i / (double) (steps - 1);
                xs[i] = 60 + (int) (u * (w - 80));
                ys[i] = (int) (h * 0.45 - u * h * 0.12 + Math.sin(u * 5) * h * 0.02);
            }
            g2.drawPolyline(xs, ys, steps);
        }

        private void scaleBar(Graphics2D g2, int h) {
            int y = h - 30;
            g2.setColor(new Color(0xE0, 0x50, 0x40));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(70, y, 230, y);
            g2.drawLine(70, y - 5, 70, y + 5);
            g2.drawLine(230, y - 5, 230, y + 5);
            g2.setColor(INK);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g2.drawString("1,500 feet", 118, y - 8);
            g2.drawString("400 meters", 116, y + 16);
        }
    }

    /** A label the host can show beside the island, standing in for the colour-map panel. */
    public static JLabel colourMapStandIn() {
        JLabel label = new JLabel("Default ColorMap", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(new Color(0x20, 0x20, 0x26));
        label.setForeground(INK);
        return label;
    }
}

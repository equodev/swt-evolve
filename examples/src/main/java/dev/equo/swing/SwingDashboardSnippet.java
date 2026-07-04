package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A richer Swing "dashboard" embedded in SWT Evolve via {@code SWT_AWT.new_Frame}
 * — the AWT/Swing mirror of {@code dev.equo.javafx.FXCanvasDashboardSnippet}. It
 * combines a gradient header, an interactive control panel ({@link JTextField},
 * {@link JSlider} + {@link JProgressBar}, {@link JRadioButton}s, {@link JCheckBox},
 * {@link JButton}), a {@link JTable}, and a small custom bar chart — all rendered
 * by real Swing (Java2D layout + painting intact) and composited onto the Evolve
 * canvas through Flutter.
 *
 * <p>Exercises pointer (clicks, slider drag), keyboard (typing into the field),
 * and rich rendering (table, gradients, custom chart). Interactions update the
 * status bar live, proving the embedded panel is fully interactive.</p>
 *
 * <p>v1 embedding note: heavyweight popups (e.g. a {@code JComboBox} dropdown that
 * overflows the frame) and mouse-wheel scrolling are follow-ups, so — like the
 * JavaFX dashboard — this demo uses inline controls and a short, non-scrolling
 * table.</p>
 */
public class SwingDashboardSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwtAwtDashboardSnippet");
        shell.setLayout(new FillLayout());

        final Composite awtComposite = new Composite(shell, SWT.EMBEDDED);
        final Frame frame = SWT_AWT.new_Frame(awtComposite);
        SwingUtilities.invokeLater(() -> frame.add(buildDashboard()));

        shell.setSize(760, 520);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static JPanel buildDashboard() {
        final JLabel status = new JLabel("Ready.");
        status.setForeground(new Color(0x2B, 0x3A, 0x4A));

        // --- Header -------------------------------------------------------
        JPanel header = new GradientPanel(new Color(0x2B, 0x6C, 0xB0), new Color(0x4A, 0x90, 0xD9));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("Swing Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("running inside SWT Evolve (Flutter) via SWT_AWT");
        subtitle.setFont(subtitle.getFont().deriveFont(11f));
        subtitle.setForeground(new Color(0xE8, 0xEE, 0xF7));
        JPanel headerText = new JPanel(new BorderLayout(0, 2));
        headerText.setOpaque(false);
        headerText.add(title, BorderLayout.NORTH);
        headerText.add(subtitle, BorderLayout.SOUTH);
        header.add(headerText, BorderLayout.WEST);

        // --- Left: interactive control panel ------------------------------
        JTextField nameField = new JTextField(14);
        nameField.addCaretListener(e -> {
            String t = nameField.getText().trim();
            status.setText(t.isEmpty() ? "Ready." : "Hello, " + t + "!");
        });

        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(40);
        JSlider slider = new JSlider(0, 100, 40);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            int v = slider.getValue();
            progress.setValue(v);
            status.setText(String.format("Level: %d%%", v));
        });

        JRadioButton small = new JRadioButton("Small");
        JRadioButton medium = new JRadioButton("Medium", true);
        JRadioButton large = new JRadioButton("Large");
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(small);
        sizeGroup.add(medium);
        sizeGroup.add(large);
        java.awt.event.ActionListener sizeListener =
                e -> status.setText("Size: " + ((JRadioButton) e.getSource()).getText());
        small.addActionListener(sizeListener);
        medium.addActionListener(sizeListener);
        large.addActionListener(sizeListener);
        JPanel sizes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));
        sizes.setOpaque(false);
        sizes.add(small);
        sizes.add(medium);
        sizes.add(large);

        JCheckBox notify = new JCheckBox("Enable notifications", true);
        notify.setOpaque(false);
        notify.addActionListener(e ->
                status.setText("Notifications " + (notify.isSelected() ? "on" : "off")));

        JButton apply = new JButton("Apply settings");
        apply.addActionListener(e -> status.setText("Applied "
                + (nameField.getText().isEmpty() ? "settings" : "settings for " + nameField.getText())
                + " @ " + slider.getValue() + "%"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(0xF5, 0xF8, 0xFC));
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xD7, 0xE1, 0xEE)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        addRow(form, 0, "Name:", nameField);
        addRow(form, 1, "Level:", slider);
        addRow(form, 2, "Progress:", progress);
        addRow(form, 3, "Size:", sizes);
        addRow(form, 4, "", notify);
        addRow(form, 5, "", apply);

        JLabel settingsHeader = new JLabel("  Settings");
        settingsHeader.setFont(settingsHeader.getFont().deriveFont(Font.BOLD));
        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setBackground(new Color(0xF5, 0xF8, 0xFC));
        left.add(settingsHeader, BorderLayout.NORTH);
        left.add(form, BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(340, 10));

        // --- Right: table + chart -----------------------------------------
        String[][] rows = {
                {"Ada", "Engineer", "92"},
                {"Linus", "Architect", "88"},
                {"Grace", "Designer", "95"},
                {"Alan", "Researcher", "81"},
        };
        DefaultTableModel model = new DefaultTableModel(rows, new String[]{"Name", "Role", "Score"}) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(10, 160));

        BarChartPanel chart = new BarChartPanel(
                new String[]{"Ada", "Linus", "Grace", "Alan"},
                new int[]{92, 88, 95, 81});
        chart.setPreferredSize(new Dimension(10, 180));

        JLabel teamHeader = new JLabel("  Team");
        teamHeader.setFont(teamHeader.getFont().deriveFont(Font.BOLD));
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        right.add(teamHeader, BorderLayout.NORTH);
        right.add(tableScroll, BorderLayout.CENTER);
        right.add(chart, BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Color.WHITE);
        body.add(left, BorderLayout.WEST);
        body.add(right, BorderLayout.CENTER);

        // --- Status bar ---------------------------------------------------
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xEE, 0xF2, 0xF7));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD7, 0xE1, 0xEE)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        statusBar.add(status, BorderLayout.WEST);

        // --- Compose ------------------------------------------------------
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        return root;
    }

    private static void addRow(JPanel form, int row, String label, java.awt.Component field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(5, 5, 5, 10);
        form.add(new JLabel(label), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = row;
        fc.weightx = 1.0;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.anchor = GridBagConstraints.WEST;
        fc.insets = new Insets(5, 0, 5, 5);
        form.add(field, fc);
    }

    /** A panel painted with a horizontal gradient — the dashboard header. */
    private static final class GradientPanel extends JPanel {
        private final Color from;
        private final Color to;
        GradientPanel(Color from, Color to) { this.from = from; this.to = to; setOpaque(true); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, from, getWidth(), 0, to));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** A minimal Java2D bar chart — Swing has no built-in chart, so we draw one. */
    private static final class BarChartPanel extends JPanel {
        private final String[] labels;
        private final int[] values;
        BarChartPanel(String[] labels, int[] values) {
            this.labels = labels;
            this.values = values;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createTitledBorder("Scores"));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Insets in = getInsets();
            int pad = 12;
            int x0 = in.left + pad;
            int y0 = in.top + pad;
            int w = getWidth() - in.left - in.right - 2 * pad;
            int h = getHeight() - in.top - in.bottom - 2 * pad;
            if (w <= 0 || h <= 0) { g2.dispose(); return; }
            int max = 100;
            int n = values.length;
            int gap = 16;
            int barW = Math.max(8, (w - (n - 1) * gap) / n);
            g2.setFont(g2.getFont().deriveFont(11f));
            for (int i = 0; i < n; i++) {
                int barH = (int) ((values[i] / (double) max) * (h - 20));
                int bx = x0 + i * (barW + gap);
                int by = y0 + (h - 20) - barH;
                g2.setColor(new Color(0x4A, 0x90, 0xD9));
                g2.fillRoundRect(bx, by, barW, barH, 6, 6);
                g2.setColor(new Color(0x2B, 0x3A, 0x4A));
                g2.drawString(labels[i], bx, y0 + h - 4);
                g2.drawString(String.valueOf(values[i]), bx, by - 3);
            }
            g2.dispose();
        }
    }
}

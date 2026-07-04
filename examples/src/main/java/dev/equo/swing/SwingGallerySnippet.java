package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A Swing "controls gallery" embedded in SWT Evolve via {@code SWT_AWT.new_Frame}
 * — a companion to the other {@code SwtAwt*Snippet}s that showcases a broad set of
 * Swing widgets across a {@link JTabbedPane}: a form tab ({@link JTextField},
 * {@link JPasswordField}, {@link JSpinner}, {@link JSlider}, {@link JCheckBox},
 * {@link JRadioButton}s, {@link JButton}) and a list tab ({@link JList} with
 * add/remove buttons and a {@link JProgressBar} that tracks its size).
 *
 * <p>Every control updates the shared status bar live, proving tabs, text input,
 * selection and range controls all work through the embedding. Like the dashboard
 * demo it sticks to inline controls (no {@code JComboBox}/menu dropdowns, whose
 * overflow popups are a v1 follow-up).</p>
 */
public class SwingGallerySnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwtAwtGallerySnippet");
        shell.setLayout(new FillLayout());

        final Composite awtComposite = new Composite(shell, SWT.EMBEDDED);
        final Frame frame = SWT_AWT.new_Frame(awtComposite);
        SwingUtilities.invokeLater(() -> frame.add(buildGallery()));

        shell.setSize(560, 460);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static JPanel buildGallery() {
        final JLabel status = new JLabel("Ready.");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Form", buildForm(status));
        tabs.addTab("List", buildList(status));
        tabs.addChangeListener(e ->
                status.setText("Tab: " + tabs.getTitleAt(tabs.getSelectedIndex())));

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0xEE, 0xF2, 0xF7));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD7, 0xE1, 0xEE)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        statusBar.add(status, BorderLayout.WEST);

        JLabel heading = new JLabel("  Swing controls, embedded in SWT Evolve");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 13f));
        heading.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.add(heading, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(statusBar, BorderLayout.SOUTH);
        return root;
    }

    private static JPanel buildForm(JLabel status) {
        JTextField name = new JTextField(16);
        name.addCaretListener(e -> {
            String t = name.getText().trim();
            status.setText(t.isEmpty() ? "Ready." : "Name: " + t);
        });

        JPasswordField pass = new JPasswordField(16);
        pass.addCaretListener(e ->
                status.setText("Password length: " + pass.getPassword().length));

        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 0, 99, 1));
        quantity.addChangeListener(e -> status.setText("Quantity: " + quantity.getValue()));

        JSlider volume = new JSlider(0, 100, 60);
        volume.setMajorTickSpacing(25);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);
        volume.addChangeListener(e -> status.setText("Volume: " + volume.getValue()));

        JCheckBox terms = new JCheckBox("Accept terms");
        terms.setOpaque(false);
        terms.addActionListener(e ->
                status.setText("Terms " + (terms.isSelected() ? "accepted" : "not accepted")));

        JRadioButton free = new JRadioButton("Free", true);
        JRadioButton pro = new JRadioButton("Pro");
        ButtonGroup plan = new ButtonGroup();
        plan.add(free);
        plan.add(pro);
        free.setOpaque(false);
        pro.setOpaque(false);
        free.addActionListener(e -> status.setText("Plan: Free"));
        pro.addActionListener(e -> status.setText("Plan: Pro"));
        JPanel plans = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));
        plans.setOpaque(false);
        plans.add(free);
        plans.add(pro);

        JButton submit = new JButton("Submit");
        submit.addActionListener(e -> status.setText("Submitted: "
                + (name.getText().isEmpty() ? "(no name)" : name.getText())
                + ", qty " + quantity.getValue() + ", vol " + volume.getValue()));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        addRow(form, 0, "Name:", name);
        addRow(form, 1, "Password:", pass);
        addRow(form, 2, "Quantity:", quantity);
        addRow(form, 3, "Volume:", volume);
        addRow(form, 4, "Plan:", plans);
        addRow(form, 5, "", terms);
        addRow(form, 6, "", submit);
        return form;
    }

    private static JPanel buildList(JLabel status) {
        final DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("First item");
        model.addElement("Second item");
        model.addElement("Third item");

        final JProgressBar fill = new JProgressBar(0, 10);
        Runnable sync = () -> {
            fill.setValue(model.getSize());
            fill.setString(model.getSize() + " / 10 items");
            fill.setStringPainted(true);
        };
        sync.run();

        JList<String> list = new JList<>(model);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) {
                status.setText("Selected: " + list.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(list);

        final int[] counter = {model.getSize()};
        JButton add = new JButton("Add");
        add.addActionListener(e -> {
            if (model.getSize() >= 10) { status.setText("List is full (10)."); return; }
            model.addElement("Item " + (++counter[0]));
            sync.run();
            status.setText("Added — " + model.getSize() + " items");
        });
        JButton remove = new JButton("Remove selected");
        remove.addActionListener(e -> {
            int i = list.getSelectedIndex();
            if (i < 0) { status.setText("Nothing selected."); return; }
            model.remove(i);
            sync.run();
            status.setText("Removed — " + model.getSize() + " items");
        });

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(add);
        buttons.add(remove);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(fill, BorderLayout.SOUTH);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private static void addRow(JPanel form, int row, String label, java.awt.Component field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 6, 6, 10);
        form.add(new JLabel(label), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = row;
        fc.weightx = 1.0;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.anchor = GridBagConstraints.WEST;
        fc.insets = new Insets(6, 0, 6, 6);
        form.add(field, fc);
    }
}

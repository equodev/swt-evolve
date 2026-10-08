package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.WindowConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An unmodified SWT_AWT application: an {@code SWT.EMBEDDED} composite, {@code SWT_AWT.new_Frame},
 * {@code frame.add(...)} with a {@code JButton("OK")} whose {@code ActionListener} is the
 * application's own. Nothing here names swing-evolve; only how the JVM is launched decides whether
 * the Swing tree is blitted or arrives as Flutter widgets through a SwingIsland.
 *
 * <p>An SWT {@link Text} above the island and one below it let keyboard focus move into the island
 * and out of it in both directions, and the {@code JTextField} inside shows where a key landed.
 * {@code -Dislands=N} stacks N islands, one AWT frame each.
 *
 * <p>The buttons under OK open the Swing windows an application opens on its own, one of each
 * owner and decoration. The last one is a modal dialog the SWT side blocks around the way an RCP
 * host does; which of the two opens first, the blocker or the dialog's own window, is not fixed.
 *
 * <p>{@code runDeskExample}/{@code runWebExample} with {@code -PmainClass=dev.equo.swing.SwingIslandSnippet}
 * run the blit path. The island path needs swing-evolve's engine on the JVM (its host launch line)
 * and a Flutter bundle that sets {@code swingMirrorBuilder}.
 */
public class SwingIslandSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwingIslandSnippet");
        shell.setLayout(new GridLayout(1, false));

        Text above = new Text(shell, SWT.BORDER | SWT.SINGLE);
        above.setMessage("SWT Text above the island");
        above.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        int islands = Math.max(1, Integer.getInteger("islands", 1));
        for (int i = 1; i <= islands; i++) addIsland(shell, islands == 1 ? "" : " " + i);

        Text below = new Text(shell, SWT.BORDER | SWT.SINGLE);
        below.setMessage("SWT Text below the island");
        below.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        shell.setSize(640, 480);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void addIsland(Shell shell, String suffix) {
        final Composite composite = new Composite(shell, SWT.EMBEDDED);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Frame frame = SWT_AWT.new_Frame(composite);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JLabel label = new JLabel("Swing island" + suffix + ": click OK");
        JTextField field = new JTextField("type here");
        JButton button = new JButton("OK");
        final int[] clicks = {0};
        button.addActionListener(e -> {
            clicks[0]++;
            label.setText("OK clicked " + clicks[0] + " time(s)");
            System.out.println("[snippet] ActionListener fired: " + clicks[0]
                    + (suffix.isEmpty() ? "" : " (island" + suffix + ")"));
        });
        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(button, BorderLayout.NORTH);
        south.add(windowButtons(shell.getDisplay(), frame, panel), BorderLayout.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        frame.add(panel);
        frame.validate();
    }

    private static JPanel windowButtons(Display display, Frame island, JPanel content) {
        JPanel buttons = new JPanel(new java.awt.GridLayout(0, 2, 4, 4));
        addButton(buttons, "Message (no parent)", () -> {
            JOptionPane.showMessageDialog(null, "A JOptionPane with a null parent.");
            log("message dialog returned");
        });
        addButton(buttons, "File chooser (no parent)", () -> {
            int answer = new JFileChooser().showOpenDialog(null);
            log("file chooser returned " + answer);
        });
        addButton(buttons, "Top-level JFrame", SwingIslandSnippet::openFrame);
        addButton(buttons, "Island-owned JDialog", () -> openOwnedDialog(island));
        addButton(buttons, "Color chooser (modal)", () -> {
            Color color = JColorChooser.showDialog(content, "Pick a color", Color.ORANGE);
            log("color chooser returned " + color);
        });
        addButton(buttons, "Ownerless JWindow", () -> openWindow(new JWindow(), "Ownerless JWindow", null));
        addButton(buttons, "Island-owned JWindow",
                () -> openWindow(new JWindow(island), "Island-owned JWindow", island));
        addButton(buttons, "Modal dialog, SWT blocker", () -> openBlockedDialog(display, island));
        return buttons;
    }

    private static void addButton(JPanel buttons, String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> {
            log("open: " + text);
            action.run();
        });
        buttons.add(button);
    }

    private static void openFrame() {
        JFrame frame = new JFrame("Top-level JFrame");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(new JLabel("  A top-level JFrame: its close box disposes it.  "));
        logLifecycle(frame, "top-level frame");
        frame.pack();
        frame.setLocation(40, 40);
        frame.setVisible(true);
    }

    private static void openOwnedDialog(Frame island) {
        JDialog dialog = new JDialog(island, "Island-owned JDialog", false);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setContentPane(closable(dialog, new JLabel("Its close box does nothing; use Close.")));
        show(dialog, "island-owned dialog", island);
    }

    private static void openWindow(JWindow window, String text, Frame island) {
        window.setContentPane(closable(window, new JLabel("  " + text + "  ")));
        show(window, text, island);
    }

    /**
     * A modal dialog that, once it shows, SWT blocks around: an invisible {@code APPLICATION_MODAL}
     * Shell under a hidden one, and an {@code SWT.Activate} filter that drops the activation of every
     * Shell already showing and gives the dialog focus instead. Both go when the dialog closes.
     */
    private static void openBlockedDialog(Display display, Frame island) {
        JDialog dialog = new JDialog(island, "Modal dialog over an SWT blocker", true);
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.add(new JLabel("SWT is blocked while this is open."), BorderLayout.NORTH);
        body.add(new JTextField("type here"), BorderLayout.CENTER);
        dialog.setContentPane(closable(dialog, body));
        // Both run on the SWT thread in the order AWT reports them, so the unblock never comes first.
        Runnable[] unblock = {null};
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                display.asyncExec(() -> unblock[0] = block(display, dialog));
            }

            @Override
            public void windowClosed(WindowEvent e) {
                display.asyncExec(() -> unblock[0].run());
            }
        });
        show(dialog, "blocked dialog", island);
    }

    /** Blocks SWT around {@code dialog} and answers how to undo it. */
    private static Runnable block(Display display, JDialog dialog) {
        Set<Shell> visibleAtStart = Arrays.stream(display.getShells()).filter(Shell::isVisible)
                .collect(Collectors.toSet());
        Listener refuseActivation = e -> {
            if (!visibleAtStart.contains(e.widget)) return;
            e.type = SWT.None;
            log("blocker dropped the activation of " + ((Shell) e.widget).getText());
            EventQueue.invokeLater(() -> {
                dialog.requestFocus();
                dialog.toFront();
            });
        };
        Shell hiddenParent = new Shell(display);
        hiddenParent.setBounds(0, 10000, 0, 0);
        Shell blocker = new Shell(hiddenParent, SWT.APPLICATION_MODAL);
        blocker.setBounds(0, 0, 2, 2);
        blocker.setAlpha(0);
        display.addFilter(SWT.Activate, refuseActivation);
        blocker.open();
        log("blocker open; Shells visible before it: " + visibleAtStart.size());
        return () -> {
            display.removeFilter(SWT.Activate, refuseActivation);
            hiddenParent.dispose();
            log("blocker removed");
        };
    }

    /** {@code content} above a Close button that disposes {@code window}. */
    private static JPanel closable(Window window, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.add(content, BorderLayout.CENTER);
        JButton close = new JButton("Close");
        close.addActionListener(e -> window.dispose());
        panel.add(close, BorderLayout.SOUTH);
        return panel;
    }

    private static void show(Window window, String name, Frame island) {
        logLifecycle(window, name);
        window.pack();
        window.setLocationRelativeTo(island);
        window.setVisible(true);
    }

    private static void logLifecycle(Window window, String name) {
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                log(name + " opened");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                log(name + " closing requested");
            }

            @Override
            public void windowClosed(WindowEvent e) {
                log(name + " closed");
            }
        });
    }

    private static void log(String message) {
        System.out.println("[snippet] " + message);
    }
}

package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Mixes <em>real SWT widgets and an embedded Swing panel in the same Shell</em>,
 * laid out with an SWT {@link org.eclipse.swt.layout.GridLayout GridLayout}. The
 * Swing/AWT mirror of {@code dev.equo.javafx.FXCanvasMixedSnippet}: SWT {@link
 * org.eclipse.swt.widgets.Label Label}, {@link org.eclipse.swt.widgets.Text Text},
 * {@link org.eclipse.swt.widgets.Button Button} and {@link
 * org.eclipse.swt.widgets.List List} controls live side-by-side with a Swing
 * {@link JPanel} hosted through {@code SWT_AWT.new_Frame}, and the SWT layout
 * manager positions all of them.
 *
 * <p>The two toolkits talk to each other, both directions, proving the merge is
 * live — not just two panes painted next to each other:</p>
 * <ul>
 *   <li><b>SWT → Swing.</b> Typing in the SWT {@code Text} and pressing the SWT
 *       "Send to Swing →" button pushes the message onto the AWT event thread
 *       ({@link SwingUtilities#invokeLater}) where it updates the Swing label.</li>
 *   <li><b>Swing → SWT.</b> Clicking the Swing "← Reply to SWT" button posts back
 *       onto the SWT thread ({@link Display#asyncExec}) where it appends a row to
 *       the SWT {@code List}.</li>
 * </ul>
 *
 * <p>Each side updates only its own widgets, and only on its own UI thread — the
 * standard rule when bridging SWT and Swing. A programmatic Swing change repaints
 * automatically (unlike FXCanvas there is no explicit {@code refresh()} needed).</p>
 */
public class SwingMixedSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwtAwtMixedSnippet");

        // SWT layout drives the whole window: 2 columns, the embedded panel spans both.
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 12;
        layout.marginHeight = 12;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 8;
        shell.setLayout(layout);

        // --- Row 0: SWT header spanning both columns ----------------------
        org.eclipse.swt.widgets.Label header =
                new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
        header.setText("SWT widgets + an embedded Swing panel, arranged by SWT GridLayout");
        header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        // --- Row 1: SWT input (label + text) ------------------------------
        org.eclipse.swt.widgets.Label prompt =
                new org.eclipse.swt.widgets.Label(shell, SWT.NONE);
        prompt.setText("Message:");
        prompt.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        final org.eclipse.swt.widgets.Text input =
                new org.eclipse.swt.widgets.Text(shell, SWT.BORDER | SWT.SINGLE);
        input.setText("Hello Swing, from SWT");
        input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // --- Row 2: the embedded Swing panel, spanning both columns -------
        // Holders so the SWT side can reach the Swing label, and the Swing side can
        // reach the SWT List, without either toolkit touching the other's thread.
        final JLabel[] swingLabel = new JLabel[1];
        final org.eclipse.swt.widgets.List[] log = new org.eclipse.swt.widgets.List[1];

        final Composite awtComposite = new Composite(shell, SWT.EMBEDDED);
        GridData awtData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        awtData.heightHint = 220;
        awtComposite.setLayoutData(awtData);

        final Frame frame = SWT_AWT.new_Frame(awtComposite);
        SwingUtilities.invokeLater(() -> frame.add(buildSwingPanel(display, swingLabel, log)));

        // --- Row 3: SWT button + SWT List (the Swing replies) -------------
        org.eclipse.swt.widgets.Button send =
                new org.eclipse.swt.widgets.Button(shell, SWT.PUSH);
        send.setText("Send to Swing →");
        send.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        final org.eclipse.swt.widgets.List replies =
                new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.V_SCROLL);
        replies.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        log[0] = replies;

        // SWT button → Swing: hop to the AWT thread to mutate the panel. The change
        // repaints on its own (imageUpdated), so no explicit canvas refresh is needed.
        send.addListener(SWT.Selection, e -> {
            final String text = input.getText().trim();
            SwingUtilities.invokeLater(() -> {
                if (swingLabel[0] != null) {
                    swingLabel[0].setText(text.isEmpty() ? "(empty message from SWT)" : text);
                }
            });
        });

        shell.setSize(560, 460);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /**
     * Builds the embedded Swing panel. Captures the Swing label (so the SWT button
     * can update it) and replies back to SWT through {@code display}.
     */
    private static JPanel buildSwingPanel(final Display display,
                                          final JLabel[] swingLabel,
                                          final org.eclipse.swt.widgets.List[] log) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(new Color(0xFA, 0xFA, 0xFA));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Embedded Swing panel", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(0x1F, 0x3B, 0x57));

        JLabel received = new JLabel("Waiting for a message from SWT…", SwingConstants.CENTER);
        received.setFont(received.getFont().deriveFont(15f));
        received.setForeground(new Color(0x2B, 0x3A, 0x4A));
        swingLabel[0] = received;

        final int[] replyCount = {0};
        JButton reply = new JButton("← Reply to SWT");
        reply.setAlignmentX(Component.CENTER_ALIGNMENT);
        reply.addActionListener(e -> {
            final int n = ++replyCount[0];
            // Swing button → SWT (always hop to the SWT thread).
            display.asyncExec(() -> {
                org.eclipse.swt.widgets.List list = log[0];
                if (list != null && !list.isDisposed()) {
                    list.add("Reply #" + n + " from Swing: \"" + received.getText() + "\"");
                    list.select(list.getItemCount() - 1);
                }
            });
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        received.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(Box.createVerticalGlue());
        center.add(title);
        center.add(Box.createVerticalStrut(16));
        center.add(received);
        center.add(Box.createVerticalStrut(16));
        center.add(reply);
        center.add(Box.createVerticalGlue());

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }
}

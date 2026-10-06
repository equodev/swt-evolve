package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

        shell.setSize(480, 320);
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
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        frame.add(panel);
        frame.validate();
    }
}

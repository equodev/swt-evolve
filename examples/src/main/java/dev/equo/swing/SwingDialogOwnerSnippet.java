package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A modeless Swing dialog opened from embedded Swing content, owned the way applications usually
 * pick an owner: {@code JOptionPane.getFrameForComponent} on the embedded panel, which answers the
 * frame {@code SWT_AWT.new_Frame} returned.
 *
 * <p>Natively that frame is a child of the SWT shell, so the dialog is owned by the application
 * window and the OS keeps it above that window. The dialog opens once on start and the button
 * reopens it; click the window after it opens: the dialog must stay in front.
 *
 * <p>Run with {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.swing.SwingDialogOwnerSnippet}.
 */
public class SwingDialogOwnerSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwingDialogOwnerSnippet");
        shell.setLayout(new FillLayout());

        final Composite composite = new Composite(shell, SWT.EMBEDDED);
        final Frame frame = SWT_AWT.new_Frame(composite);
        final JPanel panel = new JPanel();
        JButton open = new JButton("Open settings dialog");
        open.addActionListener(e -> openDialog(panel));
        panel.add(new JLabel("Embedded Swing"));
        panel.add(open);
        frame.add(panel);
        frame.validate();

        shell.setMaximized(true);
        shell.open();
        display.timerExec(3000, () -> openDialog(panel));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static void openDialog(JPanel panel) {
        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(panel), "Settings dialog", false);
        dialog.getContentPane().add(new JLabel("  Click the application window: this must stay in front.  "));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }
}

package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Embeds live Swing content inside an SWT {@link Shell} using the classic
 * {@code SWT_AWT.new_Frame(Composite)} bridge — the same call existing SWT/RCP
 * apps use to host a {@code JComponent} without any change to their source.
 *
 * <p>Mirrors {@code dev.equo.javafx.FXCanvasSnippet}: a tiny Swing panel with a
 * {@link JLabel} and a {@link JButton}; clicking the button updates the label,
 * proving the embedded content is live and interactive.</p>
 *
 * <p>Under SWT Evolve there is no native SWT window handle to reparent an AWT
 * frame into, so {@code SWT_AWT.new_Frame} is reimplemented to host the Swing
 * tree off-screen (a {@code JLightweightFrame}) and blit its pixels onto the
 * Flutter-rendered Evolve surface — the AWT/Swing mirror of the FXCanvas path.</p>
 *
 * <p><b>Run it:</b> {@code ./gradlew :examples:runWebExample
 * -PmainClass=dev.equo.swing.SwtAwtSnippet} (Flutter in the browser) or
 * {@code :examples:runDeskExample} (native window). Both work on macOS. The macOS
 * catch is that AWT and Flutter both want the main run loop: web sidesteps it by
 * not taking {@code -XstartOnFirstThread} (freeing OS thread 0 for AWT's AppKit
 * loop), and desk handles it in {@code flutter_bridge.swift}, which reports the
 * app as running after {@code finishLaunching} so embedded AWT coexists with the
 * bridge's hand-driven pump instead of hijacking the thread with its own
 * {@code [NSApp run]}.</p>
 */
public class SwingSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SwtAwtSnippet");
        shell.setLayout(new FillLayout());

        // The SWT.EMBEDDED composite is the host for the embedded AWT frame.
        final Composite composite = new Composite(shell, SWT.EMBEDDED);

        // Reparent (Evolve: off-screen host) an AWT Frame into the composite and
        // add ordinary Swing content — exactly what an unmodified app would do.
        final Frame frame = SWT_AWT.new_Frame(composite);
//        SwingUtilities.invokeLater(() ->
        {
            JPanel panel = new JPanel();
            JLabel label = new JLabel("Hello from Swing, embedded in SWT Evolve");
            JButton button = new JButton("Click me");
            final int[] clicks = {0};
            button.addActionListener(e ->
                    label.setText("Button clicked " + (++clicks[0]) + " time(s)"));
            panel.add(label);
            panel.add(button);
            frame.add(panel);
            frame.validate();
        }
//        );

        shell.setSize(420, 240);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

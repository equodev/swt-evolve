package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.Frame;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A view opened into a tab folder, holding an applet with two nested vertical {@link JSplitPane}s
 * with fixed dividers (200 inside, 600 outside, an empty panel below) embedded with
 * {@code SWT_AWT}. The applet is sized to its preferred size and repainted before the view is laid
 * out. Click "Open view": the middle pane should open about 390 px high; the console prints where
 * the dividers end up, to compare with native SWT.
 */
public class SwingNestedSplitViewSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingNestedSplitViewSnippet");
        shell.setLayout(new FillLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        CTabItem first = new CTabItem(folder, SWT.NONE);
        first.setText("Launcher");
        Composite launcher = new Composite(folder, SWT.NONE);
        launcher.setLayout(new RowLayout());
        Button open = new Button(launcher, SWT.PUSH);
        open.setText("Open view");
        open.addListener(SWT.Selection, e -> openView(folder));
        first.setControl(launcher);
        folder.setSelection(first);

        shell.setSize(500, 950);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void openView(CTabFolder folder) {
        CTabItem item = new CTabItem(folder, SWT.CLOSE);
        item.setText("Split view");
        Composite page = new Composite(folder, SWT.NONE);
        page.setLayout(new FillLayout());
        Composite embedded = new Composite(page, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(embedded);

        JSplitPane inner = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        inner.setTopComponent(new JLabel("Selection"));
        inner.setBottomComponent(new JLabel("Units"));
        inner.setDividerLocation(200);
        JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        outer.setTopComponent(inner);
        outer.setBottomComponent(new JPanel());
        outer.setDividerLocation(600);
        JApplet applet = new JApplet();
        applet.getContentPane().add(outer);
        frame.add(applet);
        frame.validate();
        SwingUtilities.invokeLater(() -> {
            applet.setSize(applet.getPreferredSize());
            applet.validate();
            applet.repaint();
        });

        folder.getDisplay().timerExec(800, () -> {
            item.setControl(page);
            folder.setSelection(item);
        });
        folder.getDisplay().timerExec(3000, () -> SwingUtilities.invokeLater(() ->
                System.out.println("dividers " + inner.getDividerLocation() + "/" + outer.getDividerLocation()
                        + ", middle pane " + inner.getBottomComponent().getHeight() + " px")));
    }
}

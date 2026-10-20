package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * An empty {@link JTable} asking for a 100 px viewport, at the top of a vertical {@link JSplitPane}
 * embedded with {@code SWT_AWT}. The table keeps that height above the divider once the shell opens;
 * the console prints the divider location to compare with native SWT.
 */
public class SwingEmptyTableSplitSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingEmptyTableSplitSnippet");
        shell.setLayout(new FillLayout());

        Composite composite = new Composite(shell, SWT.EMBEDDED);
        Frame frame = SWT_AWT.new_Frame(composite);

        JTable table = new JTable(new DefaultTableModel(new String[] {"Name", "Type", "Age", "Update Date"}, 0));
        table.setPreferredScrollableViewportSize(new Dimension(400, 100));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(table), new JLabel("Details"));
        frame.add(split);
        frame.validate();

        shell.setSize(420, 500);
        shell.open();
        display.timerExec(2000, () -> SwingUtilities.invokeLater(() ->
                System.out.println("divider at " + split.getDividerLocation()
                        + ", table header height " + table.getTableHeader().getHeight()
                        + ", top component height " + split.getTopComponent().getHeight())));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

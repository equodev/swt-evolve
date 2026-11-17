package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * A form row hidden the way custom combos are: a Canvas hosting a Text (the shape of Nebula's
 * CDateTime) is hidden with setVisible(false) and excluded from the GridLayout, so the next row
 * moves up into its place while the hidden Canvas keeps its old bounds.
 *
 * The row below must show whole: no "<choose date>" drawn over its spinner.
 *
 * For the native baseline, run this file without its Config lines against the stock SWT jar.
 */
public class CanvasHiddenChildrenSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Hidden Canvas with children");
        shell.setLayout(new GridLayout(2, false));

        Label dateLabel = new Label(shell, SWT.NONE);
        dateLabel.setText("Date");
        Canvas combo = new Canvas(shell, SWT.BORDER);
        combo.setLayout(new FillLayout());
        Text field = new Text(combo, SWT.READ_ONLY);
        field.setText("<choose date>");
        GridData comboData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        combo.setLayoutData(comboData);

        Composite row = new Composite(shell, SWT.NONE);
        row.setLayout(new RowLayout(SWT.HORIZONTAL));
        new Label(row, SWT.NONE).setText("Propose after");
        new Spinner(row, SWT.BORDER);
        new Label(row, SWT.NONE).setText("days");
        row.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

        shell.setSize(420, 160);
        shell.open();

        GridData labelData = new GridData();
        labelData.exclude = true;
        dateLabel.setLayoutData(labelData);
        dateLabel.setVisible(false);
        comboData.exclude = true;
        combo.setVisible(false);
        shell.layout(true, true);

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

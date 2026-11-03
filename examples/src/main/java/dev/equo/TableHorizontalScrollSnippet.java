package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/** A Table whose columns are wider than the table: it scrolls sideways to reach the last column. */
public class TableHorizontalScrollSnippet {

    public static void main(String[] args) {
        Config.useEquo(Table.class);
        Config.useEquo(TableItem.class);
        Config.useEquo(TableColumn.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TableHorizontalScrollSnippet");
        shell.setLayout(new GridLayout(1, false));

        Table table = new Table(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 587;
        data.heightHint = 200;
        table.setLayoutData(data);

        String[] names = {"Name", "Location", "Type"};
        int[] widths = {195, 1479, 106};
        for (int i = 0; i < names.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(names[i]);
            column.setWidth(widths[i]);
        }
        for (int i = 0; i < 8; i++) {
            new TableItem(table, SWT.NONE).setText(new String[] {
                    "jre-" + (17 + i), "/Library/Java/JavaVirtualMachines/jdk-" + (17 + i) + ".jdk/Contents/Home",
                    "Standard VM"});
        }

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

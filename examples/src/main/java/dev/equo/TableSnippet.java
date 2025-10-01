package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("TableSnippet");
        shell.setSize(400, 250);

        Table table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn nameColumn = new TableColumn(table, SWT.NONE);
        nameColumn.setText("Name");
        nameColumn.setWidth(150);

        TableColumn ageColumn = new TableColumn(table, SWT.NONE);
        ageColumn.setText("Age");
        ageColumn.setWidth(80);

        TableColumn cityColumn = new TableColumn(table, SWT.NONE);
        cityColumn.setText("City");
        cityColumn.setWidth(120);

        String[][] data = {
                {"John Doe", "25", "New York"},
                {"Jane Smith", "30", "Los Angeles"},
                {"Bob Johnson", "35", "Chicago"},
                {"Alice Brown", "28", "Houston"},
                {"Charlie Wilson", "42", "Phoenix"}
        };

        for (String[] row : data) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(row);
        }

        for (TableColumn column : table.getColumns()) {
            column.pack();
        }

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

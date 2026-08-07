package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableNoColumnsSnippet {

    public static void main(String[] args) {
        Config.useEquo(Table.class);
        Config.useEquo(TableItem.class);
        Config.useEquo(TableColumn.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TableNoColumnsSnippet");
        shell.setLayout(new GridLayout(1, false));

        new Label(shell, SWT.NONE).setText("Ranges:");

        Table ranges = new Table(shell, SWT.BORDER | SWT.V_SCROLL);
        ranges.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        for (String range : new String[] {
                "Current range", "Range linked to current", "Editable range", "Final caret location" }) {
            new TableItem(ranges, SWT.NONE).setText(range);
        }

        new Label(shell, SWT.NONE).setText("Annotation types:");

        Table annotations = new Table(shell, SWT.BORDER | SWT.V_SCROLL);
        annotations.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        for (String annotation : new String[] {
                "Bookmarks", "Breakpoints", "Cucumber unmatched step", "Debug Call Stack", "Errors",
                "Spelling Errors" }) {
            new TableItem(annotations, SWT.NONE).setText(annotation);
        }

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

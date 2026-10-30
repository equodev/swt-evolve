package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link CTabFolderNarrowSnippet} with the minimize and maximize buttons shown, which take their
 * width from the tab row as in SWT.
 */
public class CTabFolderNarrowMinMaxSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderNarrowMinMaxSnippet");
        shell.setLayout(new GridLayout());
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setMinimumCharacters(3);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        data.widthHint = 240;
        data.heightHint = 120;
        folder.setLayoutData(data);
        for (String label : new String[] {"Overview", "Configuration", "Dependencies", "Run History"}) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(label);
            Text body = new Text(folder, SWT.MULTI);
            body.setText("Content for " + label);
            item.setControl(body);
        }
        folder.setSelection(0);
        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

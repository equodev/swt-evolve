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
 * A CTabFolder too narrow for its tabs at full width. With -Dswt.evolve.ctabfolder_tab_scroll=false
 * the tabs shrink towards minimumCharacters as in SWT; by default the strip scrolls instead.
 */
public class CTabFolderNarrowSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderNarrowSnippet");
        shell.setLayout(new GridLayout());
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setMinimumCharacters(3);
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

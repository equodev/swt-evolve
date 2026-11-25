package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * A TabFolder narrower than its twelve tabs, opened on the last one: the selected tab should be in
 * view and every tab reachable.
 */
public class TabFolderOverflowSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TabFolderOverflowSnippet");
        shell.setLayout(new GridLayout());
        TabFolder folder = new TabFolder(shell, SWT.NONE);
        GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        data.widthHint = 300;
        data.heightHint = 120;
        folder.setLayoutData(data);
        for (int i = 1; i <= 12; i++) {
            TabItem item = new TabItem(folder, SWT.NONE);
            item.setText(String.format("Page %02d", i));
            Label body = new Label(folder, SWT.NONE);
            body.setText("Content of page " + i);
            item.setControl(body);
        }
        folder.setSelection(11);
        shell.setSize(400, 220);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Snippet to reproduce CTabFolder/CTabItem issues:
 * 1. Tab font color appears inactive (gray) even when selected
 * 2. After closing a tab via the close button, the tab stays visible
 *
 * Steps to reproduce:
 * - Click on different tabs to observe font color
 * - Click the X button on a tab to close it
 * - Observe if the tab disappears correctly
 */
public class CTabFolderCloseTabSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolder Close Tab Bug");
        shell.setLayout(new GridLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.CLOSE);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create tabs with close button
        for (int i = 0; i < 5; i++) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText("Tab " + i);
            Text text = new Text(folder, SWT.MULTI | SWT.V_SCROLL);
            text.setText("Content of Tab " + i + "\n\nClick the X to close this tab.");
            item.setControl(text);
        }

        // Select first tab
        folder.setSelection(0);

        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
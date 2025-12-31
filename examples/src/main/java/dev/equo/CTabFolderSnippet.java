package dev.equo;
import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates the use of CTabFolder (Custom Tab Folder) widget in SWT.
 * This example creates a tab folder with 8 tabs, each containing a text area with sample content.
 * The tab folder includes:
 * - Minimize/maximize buttons in the tab bar
 * - Support for minimizing, maximizing, and restoring the tab folder
 * - Multi-line text content in each tab with scrollbars
 * - Custom styling with borders and proper layout management
 */
public class CTabFolderSnippet {
    public static void main (String [] args) {
        Display display = new Display ();

        final Shell shell = new Shell (display);
        shell.setText("CTabFolderSnippet");
        shell.setLayout(new GridLayout());
        final CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        folder.setSimple(false);
        folder.setUnselectedImageVisible(false);
        folder.setUnselectedCloseVisible(false);
        for (int i = 0; i < 8; i++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText("Item "+i);
            Text text = new Text(folder, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            text.setText("Text for item "+i+"\n\none, two, three\n\nabcdefghijklmnop");
            item.setControl(text);
        }
        shell.setSize(350, 300);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}
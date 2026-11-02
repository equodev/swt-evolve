package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A view stack as a workbench builds it: a CTabFolder with a fixed tab height whose topRight tool
 * bar (SWT.RIGHT | SWT.WRAP) does not fit beside the tabs, so the folder moves it to a row of its
 * own under them and lays the page out under that row.
 */
public class CTabFolderWrappedToolBarSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderWrappedToolBarSnippet");
        shell.setLayout(new GridLayout());
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setTabHeight(40);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        data.widthHint = 300;
        data.heightHint = 200;
        folder.setLayoutData(data);

        Composite topRight = new Composite(folder, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginLeft = rowLayout.marginTop = rowLayout.marginRight = rowLayout.marginBottom = 0;
        topRight.setLayout(rowLayout);
        ToolBar toolBar = new ToolBar(topRight, SWT.FLAT | SWT.RIGHT);
        for (String label : new String[] {"Sort", "Filter", "Link", "Pin", "Menu"}) {
            new ToolItem(toolBar, SWT.PUSH).setText(label);
        }
        folder.setTopRight(topRight, SWT.RIGHT | SWT.WRAP);

        for (String label : new String[] {"Outline", "Properties", "Assistant", "Favorites"}) {
            CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(label);
            Text body = new Text(folder, SWT.MULTI);
            body.setText("Content for " + label);
            item.setControl(body);
        }
        folder.setSelection(1);
        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

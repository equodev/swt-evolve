package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class CTabFolderWithContentSnippet {

    private static void log(String message) {
        System.out.println("[DEBUG] " + message);
    }

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("CTabFolder Debug");
        shell.setLayout(new GridLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        log("CTabFolder created, clientArea: " + folder.getClientArea());

        // Tab with content
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText("Tab 1");

        Text text = new Text(folder, SWT.MULTI | SWT.V_SCROLL);
        text.setText("Tab 1 content\n\nLine 2\nLine 3");

        log("Before setControl - text bounds: " + text.getBounds());
        log("Before setControl - clientArea: " + folder.getClientArea());

        item.setControl(text);

        log("After setControl - text bounds: " + text.getBounds());
        log("After setControl - text visible: " + text.getVisible());

        folder.setSelection(0);

        log("After setSelection - text bounds: " + text.getBounds());
        log("After setSelection - text visible: " + text.getVisible());

        shell.setSize(400, 300);
        shell.open();

        log("After open - clientArea: " + folder.getClientArea());
        log("After open - text bounds: " + text.getBounds());
        log("After open - text visible: " + text.getVisible());

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
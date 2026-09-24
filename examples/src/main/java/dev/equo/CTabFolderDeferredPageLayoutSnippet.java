package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A CTabFolder whose selected page is laid out a turn after the folder itself is resized, as a
 * workbench does when it defers layout during a window resize.
 *
 * <p>Maximise or resize the window: the page (bordered) must fill the tab body every time,
 * without clicking a tab.
 */
public class CTabFolderDeferredPageLayoutSnippet {

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("CTabFolder deferred page layout");
        shell.setLayout(new GridLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        for (int i = 1; i <= 2; i++) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText("Tab " + i);
            Composite page = new Composite(folder, SWT.BORDER);
            page.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
            page.setLayout(new FillLayout());
            Label label = new Label(page, SWT.WRAP);
            label.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
            label.setText("Page " + i + ": this page must fill the whole tab body.");
            item.setControl(page);
        }
        folder.setSelection(0);

        shell.addListener(SWT.Resize, e -> {
            if (folder.isLayoutDeferred()) return;
            folder.setLayoutDeferred(true);
            display.timerExec(150, () -> {
                if (folder.isDisposed()) return;
                folder.setLayoutDeferred(false);
                System.out.println("folder=" + folder.getBounds() + " page=" + folder.getSelection().getControl().getBounds());
            });
        });

        shell.setSize(500, 320);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

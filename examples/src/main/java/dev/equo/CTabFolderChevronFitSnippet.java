package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A view stack of closable icon tabs that starts too narrow for them and is then widened step by
 * step, as a dragged sash does, until every tab fits. Run with -Dswt.evolve.ctabfolder_tab_scroll=false:
 * once every tab shows, the chevron must go.
 */
public class CTabFolderChevronFitSnippet {
    static final String[] LABELS = {"Search", "Console", "Outline", "Problems", "Tasks", "Progress", "History",
            "Markers", "Javadoc"};

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolderChevronFitSnippet");
        shell.setLayout(new GridLayout());
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER | SWT.CLOSE);
        folder.setMinimumCharacters(1);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        Image icon = new Image(display, 16, 16);
        GC gc = new GC(icon);
        gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        gc.fillOval(2, 2, 12, 12);
        gc.dispose();
        for (String label : LABELS) {
            CTabItem item = new CTabItem(folder, SWT.NONE);
            item.setText(label);
            item.setImage(icon);
            Text body = new Text(folder, SWT.MULTI);
            body.setText("Content for " + label);
            item.setControl(body);
        }
        folder.setSelection(0);
        GridData data = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
        data.widthHint = 200;
        folder.setLayoutData(data);
        shell.setSize(800, 300);
        shell.open();
        Runnable widen = new Runnable() {
            @Override
            public void run() {
                if (shell.isDisposed() || data.widthHint >= 760) return;
                data.widthHint += 10;
                shell.layout(true, true);
                display.timerExec(300, this);
            }
        };
        display.timerExec(1000, widen);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        icon.dispose();
        display.dispose();
    }
}

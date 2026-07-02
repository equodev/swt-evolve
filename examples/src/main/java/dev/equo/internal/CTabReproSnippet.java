package dev.equo.internal;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/** Repro: switch to tab 1 after 2.5s (programmatic). Observe whether the tab header switches and whether
 *  the content renders. */
public class CTabReproSnippet {
    public static void main(String[] args) {
        System.setProperty("dev.equo.swt.debug", "true"); Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTab Repro");
        shell.setLayout(new GridLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem i0 = new CTabItem(folder, SWT.NONE);
        i0.setText("Tab 0");
        Composite b0 = new Composite(folder, SWT.NONE);
        b0.setLayout(new FillLayout());
        new Button(b0, SWT.PUSH).setText("TAB 0 CONTENT");
        i0.setControl(b0);

        CTabItem i1 = new CTabItem(folder, SWT.NONE);
        i1.setText("Tab 1");
        Composite b1 = new Composite(folder, SWT.NONE);
        b1.setLayout(new FillLayout());
        new Button(b1, SWT.PUSH).setText("TAB 1 CONTENT");
        i1.setControl(b1);

        folder.setSelection(0);
        shell.setSize(420, 320);
        shell.open();

        display.timerExec(2500, () -> {
            System.out.println("[REPRO] setSelection(1)");
            folder.setSelection(1);
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

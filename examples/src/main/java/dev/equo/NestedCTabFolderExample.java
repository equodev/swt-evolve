package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class NestedCTabFolderExample {

    public static void main(String[] args) {
//        Config.forceEclipse();
        System.setProperty("dev.equo.swt.debug", "true");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Nested CTabFolder Example");
        shell.setSize(800, 400);
//        shell.setLayout(new GridLayout(2, true));
        shell.setLayout(new GridLayout(1, true));

        createMainFolder(shell, "Main Folder 1");

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static void createMainFolder(Shell parent, String title) {
        CTabFolder mainFolder = new CTabFolder(parent, SWT.BORDER);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 600;
        mainFolder.setLayoutData(gd);

        mainFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabItem mainItem = new CTabItem(mainFolder, SWT.NONE);
        mainItem.setText(title);

        org.eclipse.swt.widgets.Composite composite =
                new org.eclipse.swt.widgets.Composite(mainFolder, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));

        createInnerFolder(composite, "Inner A", "1", "2", false);
        createInnerFolder(composite, "Inner B", "3", "4", true);

        mainItem.setControl(composite);
        mainFolder.setSelection(mainItem);
    }

    private static void createInnerFolder(org.eclipse.swt.widgets.Composite parent,
                                          String title,
                                          String label1,
                                          String label2,
                                          boolean minMax) {

        CTabFolder innerFolder = new CTabFolder(parent, SWT.BORDER);
        innerFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        innerFolder.setTabPosition(SWT.DOWN);

        if (minMax) {
            innerFolder.setMinimizeVisible(true);
            innerFolder.setMaximizeVisible(true);
        }

        CTabItem item1 = new CTabItem(innerFolder, SWT.NONE);
        item1.setText(title + " - Tab " + label1);
        Label l1 = new Label(innerFolder, SWT.CENTER);
        l1.setText("Label " + label1);
        item1.setControl(l1);

        CTabItem item2 = new CTabItem(innerFolder, SWT.NONE);
        item2.setText(title + " - Tab " + label2);
        Label l2 = new Label(innerFolder, SWT.CENTER);
        l2.setText("Label " + label2);
        item2.setControl(l2);

        innerFolder.setSelection(0);
    }
}
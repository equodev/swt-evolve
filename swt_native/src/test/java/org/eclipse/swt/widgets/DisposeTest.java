package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

public class DisposeTest {

    //@RepeatedTest(25)
    @Disabled
    @Test
    void testDisposeShouldntCrash() {
        Display display = new Display();
        Shell shell = new Shell();
        shell.setText("Dispose test");
        shell.setLayout(new GridLayout(1, true));

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

        shell.open();
        int i = 0;
        while (!shell.isDisposed() && i < 1000) {
            if (!display.readAndDispatch()) {
            }
            ++i;
        }
        shell.dispose();

        i = 0;

        while (!shell.isDisposed() && i < 10000) {
            if (!display.readAndDispatch()) {
            }
            ++i;
        }

        display.dispose();
    }

}

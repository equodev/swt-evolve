package dev.equo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;


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
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void minimize(CTabFolderEvent event) {
                folder.setMinimized(true);
                folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                shell.layout(true);
            }
            @Override
            public void maximize(CTabFolderEvent event) {
                folder.setMaximized(true);
                folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                shell.layout(true);
            }
            @Override
            public void restore(CTabFolderEvent event) {
                folder.setMinimized(false);
                folder.setMaximized(false);
                folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
                shell.layout(true);
            }
        });
        shell.setSize(300, 300);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}

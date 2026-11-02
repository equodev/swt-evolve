package dev.equo;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** A dialog opened without setSize or pack: it gets the platform's default shell size. */
public class UnsizedDialogSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("UnsizedDialogSnippet");
        shell.setLayout(new RowLayout());
        Button open = new Button(shell, SWT.PUSH);
        open.setText("Open Unsized Dialog");
        open.addSelectionListener(widgetSelectedAdapter(e -> {
            Shell dialog = new Shell(shell, SWT.DIALOG_TRIM);
            dialog.setText("Unsized dialog");
            dialog.setLayout(new FillLayout());
            new Label(dialog, SWT.NONE).setText("Nobody sized this dialog");
            dialog.open();
        }));
        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

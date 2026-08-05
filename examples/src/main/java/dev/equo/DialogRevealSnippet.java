package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A dialog built the way recorder-style tools do it: a secondary
 * Shell is created and opened first, and its content is built in a later
 * event-loop iteration. The content payload then travels on the Shell's own
 * channel while the Flutter side is still mounting it from the Display embed —
 * exactly the buffered-payload window that used to leave the dialog empty.
 */
public class DialogRevealSnippet {
    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("DialogReveal main");
        shell.setLayout(new GridLayout(1, false));

        Button open = new Button(shell, SWT.PUSH);
        open.setText("Open recorder dialog");
        open.addListener(SWT.Selection, e -> {
            Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
            dialog.setText("Web Recorder");
            dialog.setLayout(new GridLayout(2, false));
            dialog.setSize(520, 260);
            dialog.open();
            // Populate AFTER the open reached Flutter, mirroring apps that build
            // the dialog's form while the dialog is already showing.
            display.asyncExec(() -> {
                Label lbl = new Label(dialog, SWT.NONE);
                lbl.setText("Recorder Setting");
                lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
                Button newBrowser = new Button(dialog, SWT.RADIO);
                newBrowser.setText("New Browser");
                newBrowser.setSelection(true);
                Button activeBrowser = new Button(dialog, SWT.RADIO);
                activeBrowser.setText("Active Browser");
                Label url = new Label(dialog, SWT.NONE);
                url.setText("URL");
                Text urlText = new Text(dialog, SWT.BORDER);
                urlText.setMessage("Enter URL");
                urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                Button start = new Button(dialog, SWT.PUSH);
                start.setText("Start Record");
                start.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false, 2, 1));
                dialog.layout(true, true);
            });
        });

        shell.setSize(700, 300);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

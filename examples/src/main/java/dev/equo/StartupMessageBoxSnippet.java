package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * A MessageBox asked at startup on a Shell that is never opened, before any other Shell exists, as an
 * application asks which workspace to use. The answer is then shown in a Shell of its own.
 */
public class StartupMessageBoxSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell owner = new Shell(display, SWT.ON_TOP);
        owner.setText("Startup");
        MessageBox box = new MessageBox(owner, SWT.ICON_WARNING | SWT.YES | SWT.NO);
        box.setText("Workspace in use");
        box.setMessage("The workspace is in use. Open another one?");
        int answer = box.open();
        owner.dispose();

        Shell shell = new Shell(display);
        shell.setText("StartupMessageBoxSnippet");
        shell.setLayout(new FillLayout());
        new Label(shell, SWT.NONE).setText(answer == SWT.YES ? "Answered Yes" : "Answered No");
        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Exercises the Shell close contract: the first close gesture is vetoed with {@code doit = false}
 * and must leave the window standing; the second is accepted and must dispose the shell and let the
 * process exit. Drive it with the title-bar X (or Alt+F4) and read the printed trace.
 */
public class ShellCloseVetoSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Close me twice");
        shell.setLayout(new FillLayout());
        shell.setSize(420, 160);
        Label label = new Label(shell, SWT.CENTER | SWT.WRAP);
        label.setText("First close is vetoed, second one closes.");

        boolean[] allow = { false };
        shell.addListener(SWT.Close, e -> {
            e.doit = allow[0];
            System.out.println("TRACE SWT.Close doit=" + e.doit);
            allow[0] = true;
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
        System.out.println("TRACE exited");
    }
}

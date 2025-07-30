package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

public class ButtonSnippet {
    public static void main (String [] args) {
        Display display = new Display ();
        Shell shell = new Shell (display);
        shell.setText("ButtonSnippet");

        Button pushButton = new Button (shell, SWT.PUSH);
        pushButton.setText ("This is a push button");
        pushButton.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Push button clicked")));

        Button toggleButton = new Button (shell, SWT.TOGGLE);
        toggleButton.setText ("This is a toggle button");
        toggleButton.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Toggle button clicked")));

        Button checkButton = new Button (shell, SWT.CHECK);
        checkButton.setText ("This is a check button");
        checkButton.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Check button clicked")));

        shell.setLayout (new RowLayout(SWT.VERTICAL));
        shell.pack ();
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}

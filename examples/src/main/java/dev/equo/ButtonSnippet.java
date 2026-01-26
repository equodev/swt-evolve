package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Demonstrates different types of SWT buttons including push, toggle, and check buttons.
 * This example creates a shell with three different button types arranged vertically:
 * - Push button: Standard clickable button
 * - Toggle button: Button that maintains pressed/unpressed state
 * - Check button: Checkbox with text label
 * Each button prints a message to the console when clicked.
 */
public class ButtonSnippet {
    public static void main (String [] args) {
        Config.useEquo(Button.class);

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

        Button radioButton = new Button (shell, SWT.RADIO);
        radioButton.setText ("This is a radio button");
        radioButton.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Radio button clicked")));

        shell.setLayout (new RowLayout(SWT.VERTICAL));
        shell.setSize(250, 150);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        display.dispose ();
    }
}

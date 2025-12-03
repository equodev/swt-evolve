package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Password Text widget
 * This example showcases:
 * - Password field with SWT.PASSWORD style
 * - Text modification events
 * - Enter key handling
 */
public class TextPasswordSnippet {
    public static void main(String[] args) {
        Config.useEquo(Text.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("PasswordSnippet");
        shell.setLayout(new GridLayout(2, false));

        // Password field
        Label label1 = new Label(shell, SWT.NONE);
        label1.setText("Password:");
        Text passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        passwordText.setMessage("Enter password...");
        passwordText.addListener(SWT.Modify, e -> System.out.println("Password modified (length: " + passwordText.getText().length() + ")"));
        passwordText.addListener(SWT.DefaultSelection, e -> System.out.println("Enter pressed, password: " + passwordText.getText()));

        shell.pack();
        shell.setSize(400, 150);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

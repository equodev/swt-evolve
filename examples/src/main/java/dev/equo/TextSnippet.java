package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Text widget
 * This example showcases:
 * - Single-line editable text field
 * - Password field
 */
public class TextSnippet {
    public static void main(String[] args) {
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TextSnippet");
        shell.setLayout(new GridLayout(2, false));

        // Single-line text field
        Label label1 = new Label(shell, SWT.NONE);
        label1.setText("Single-line:");
        Text text1 = new Text(shell, SWT.BORDER);
        text1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text1.setMessage("Enter text here...");
        text1.addListener(SWT.Modify, e -> System.out.println("Text modified: " + text1.getText()));
        text1.addListener(SWT.DefaultSelection, e -> System.out.println("Enter pressed: " + text1.getText()));

        // Read-only text field
        Label label2 = new Label(shell, SWT.NONE);
        label2.setText("Read-only:");
        Text text2 = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
        text2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        text2.setText("This text is read-only");

        shell.pack();
        shell.setSize(400, 450);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

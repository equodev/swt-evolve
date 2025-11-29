package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Text character limit
 * This example showcases:
 * - Text field with character limit using setTextLimit()
 * - Visual feedback of remaining characters
 */
public class TextLimitSnippet {
    public static void main(String[] args) {
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TextLimitSnippet");
        shell.setLayout(new GridLayout(1, false));

        // Text with 10 character limit
        Label label1 = new Label(shell, SWT.NONE);
        label1.setText("Username (max 10 characters):");

        Text limitedText = new Text(shell, SWT.BORDER);
        limitedText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        limitedText.setTextLimit(10);
        limitedText.setMessage("Enter username");
        limitedText.addListener(SWT.Modify, e -> {
            int length = limitedText.getText().length();
            int remaining = 10 - length;
            System.out.println("Characters used: " + length + "/10 (remaining: " + remaining + ")");
        });

        // Add some spacing
        Label spacer = new Label(shell, SWT.NONE);
        spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Text with 50 character limit
        Label label2 = new Label(shell, SWT.NONE);
        label2.setText("Description (max 50 characters):");

        Text limitedText2 = new Text(shell, SWT.BORDER);
        limitedText2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        limitedText2.setTextLimit(50);
        limitedText2.setMessage("Enter description");

        shell.pack();
        shell.setSize(400, 250);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

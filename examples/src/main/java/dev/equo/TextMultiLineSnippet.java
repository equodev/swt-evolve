package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Multi-line Text widget
 * This example showcases:
 * - Multi-line text area with SWT.MULTI
 * - Vertical scrolling
 * - Text modification events
 */
public class TextMultiLineSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TextMultiLineSnippet");
        shell.setLayout(new GridLayout(1, false));

        // Multi-line text area
        Label label = new Label(shell, SWT.NONE);
        label.setText("Multi-line text area:");

        Text multiText = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        multiText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        multiText.setMessage("Enter multiple lines of text...");
        multiText.setText("Line 1\nLine 2\nLine 3");
        multiText.addListener(SWT.Modify, e -> System.out.println("Text modified: " + multiText.getText()));

        shell.pack();
        shell.setSize(400, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

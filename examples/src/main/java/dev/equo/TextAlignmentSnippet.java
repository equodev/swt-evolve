package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Text alignment
 * This example showcases:
 * - Left-aligned text (default)
 * - Center-aligned text (SWT.CENTER)
 * - Right-aligned text (SWT.RIGHT)
 */
public class TextAlignmentSnippet {
    public static void main(String[] args) {
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TextAlignmentSnippet");
        shell.setLayout(new GridLayout(2, false));

        // Left-aligned (default)
        Label label1 = new Label(shell, SWT.NONE);
        label1.setText("Left:");
        Text leftText = new Text(shell, SWT.BORDER);
        leftText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        leftText.setText("Left aligned");

        // Center-aligned
        Label label2 = new Label(shell, SWT.NONE);
        label2.setText("Center:");
        Text centerText = new Text(shell, SWT.BORDER | SWT.CENTER);
        centerText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        centerText.setText("Center aligned");

        // Right-aligned
        Label label3 = new Label(shell, SWT.NONE);
        label3.setText("Right:");
        Text rightText = new Text(shell, SWT.BORDER | SWT.RIGHT);
        rightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        rightText.setText("Right aligned");

        shell.pack();
        shell.setSize(400, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

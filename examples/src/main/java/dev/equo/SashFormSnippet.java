package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates a horizontal SashForm with three resizable sections.
 * This example creates a shell with a SashForm containing three buttons.
 * Users can drag the sashes between sections to resize them.
 * The sections are initially sized with weights [1, 2, 1] meaning the middle
 * section is twice as large as the side sections.
 */
public class SashFormSnippet {
    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SashForm Horizontal Example");
        shell.setLayout(new FillLayout());

        // Create horizontal SashForm
        SashForm sashForm = new SashForm(shell, SWT.HORIZONTAL);

        // Add three buttons as children
        Button button1 = new Button(sashForm, SWT.PUSH);
        button1.setText("Left Section");

        Button button2 = new Button(sashForm, SWT.PUSH);
        button2.setText("Middle Section (2x)");

        Button button3 = new Button(sashForm, SWT.PUSH);
        button3.setText("Right Section");

        // Set weights: middle section is twice as wide
        sashForm.setWeights(new int[]{1, 2, 1});

        shell.setSize(600, 200);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
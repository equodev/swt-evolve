package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates SWT Scale widget with both horizontal and vertical orientations.
 * Shows how to track scale value changes and display the current selection.
 */
public class ScaleSnippet {
    public static void main(String[] args) {
        Config.useEquo(Scale.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Scale Example");
        shell.setSize(400, 450);

        Label horizontalLabel = new Label(shell, SWT.NONE);
        horizontalLabel.setText("Horizontal Scale:");
        horizontalLabel.setBounds(20, 20, 150, 20);

        Scale horizontalScale = new Scale(shell, SWT.HORIZONTAL);
        horizontalScale.setMinimum(0);
        horizontalScale.setMaximum(100);
        horizontalScale.setSelection(50);
        horizontalScale.setIncrement(1);
        horizontalScale.setPageIncrement(10);
        horizontalScale.setBounds(20, 50, 200, 40);

        Label verticalLabel = new Label(shell, SWT.NONE);
        verticalLabel.setText("Vertical Scale:");
        verticalLabel.setBounds(20, 120, 150, 20);

        Scale verticalScale = new Scale(shell, SWT.VERTICAL);
        verticalScale.setMinimum(0);
        verticalScale.setMaximum(100);
        verticalScale.setSelection(25);
        verticalScale.setIncrement(1);
        verticalScale.setPageIncrement(10);
        verticalScale.setBounds(80, 150, 40, 200);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

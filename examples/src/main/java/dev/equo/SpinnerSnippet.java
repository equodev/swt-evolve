package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates SWT Spinner widget with different configurations.
 * Shows integer spinner, decimal spinner, and read-only spinner.
 */
public class SpinnerSnippet {
    public static void main(String[] args) {
        Config.useEquo(Spinner.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Spinner Example");
        shell.setSize(400, 300);

        // Integer Spinner
        Label intLabel = new Label(shell, SWT.NONE);
        intLabel.setText("Integer Spinner:");
        intLabel.setBounds(20, 20, 150, 20);

        Spinner intSpinner = new Spinner(shell, SWT.BORDER);
        intSpinner.setMinimum(0);
        intSpinner.setMaximum(100);
        intSpinner.setSelection(50);
        intSpinner.setIncrement(1);
        intSpinner.setPageIncrement(10);
        intSpinner.setBounds(20, 50, 120, 32);

        // Decimal Spinner
        Label decimalLabel = new Label(shell, SWT.NONE);
        decimalLabel.setText("Decimal Spinner:");
        decimalLabel.setBounds(20, 100, 180, 20);

        Spinner decimalSpinner = new Spinner(shell, SWT.BORDER);
        decimalSpinner.setMinimum(0);
        decimalSpinner.setMaximum(1000);
        decimalSpinner.setSelection(500);
        decimalSpinner.setDigits(2);
        decimalSpinner.setIncrement(10);
        decimalSpinner.setBounds(20, 130, 120, 32);

        // Read-only Spinner
        Label readOnlyLabel = new Label(shell, SWT.NONE);
        readOnlyLabel.setText("Read-Only Spinner:");
        readOnlyLabel.setBounds(20, 180, 150, 20);

        Spinner readOnlySpinner = new Spinner(shell, SWT.BORDER | SWT.READ_ONLY);
        readOnlySpinner.setMinimum(0);
        readOnlySpinner.setMaximum(10);
        readOnlySpinner.setSelection(5);
        readOnlySpinner.setIncrement(1);
        readOnlySpinner.setBounds(20, 210, 120, 32);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

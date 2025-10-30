package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates SWT Slider widget with both horizontal and vertical orientations.
 * Shows how to track slider value changes and display the current selection.
 */
public class SliderSnippet {
    public static void main(String[] args) {
        Config.useEquo(Slider.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Slider Example");
        shell.setSize(400, 450);

        Label horizontalLabel = new Label(shell, SWT.NONE);
        horizontalLabel.setText("Horizontal Slider:");
        horizontalLabel.setBounds(20, 20, 150, 20);

        Slider horizontalSlider = new Slider(shell, SWT.HORIZONTAL);
        horizontalSlider.setMinimum(0);
        horizontalSlider.setMaximum(100);
        horizontalSlider.setSelection(50);
        horizontalSlider.setIncrement(1);
        horizontalSlider.setPageIncrement(10);
        horizontalSlider.setBounds(20, 50, 250, 40);

        Label verticalLabel = new Label(shell, SWT.NONE);
        verticalLabel.setText("Vertical Slider:");
        verticalLabel.setBounds(20, 120, 150, 20);

        Slider verticalSlider = new Slider(shell, SWT.VERTICAL);
        verticalSlider.setMinimum(0);
        verticalSlider.setMaximum(100);
        verticalSlider.setSelection(25);
        verticalSlider.setIncrement(1);
        verticalSlider.setPageIncrement(10);
        verticalSlider.setBounds(80, 150, 48, 250);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

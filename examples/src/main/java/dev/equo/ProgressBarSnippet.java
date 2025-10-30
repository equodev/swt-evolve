package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates SWT ProgressBar widget with different configurations.
 * Shows horizontal, vertical, determinate, and indeterminate progress bars.
 */
public class ProgressBarSnippet {
    public static void main(String[] args) {
        Config.useEquo(ProgressBar.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ProgressBar Example");
        shell.setSize(400, 450);

        // Horizontal Determinate ProgressBar
        Label horizontalLabel = new Label(shell, SWT.NONE);
        horizontalLabel.setText("Horizontal Progress (Determinate):");
        horizontalLabel.setBounds(20, 20, 250, 20);

        ProgressBar horizontalProgress = new ProgressBar(shell, SWT.HORIZONTAL);
        horizontalProgress.setMinimum(0);
        horizontalProgress.setMaximum(100);
        horizontalProgress.setSelection(0);
        horizontalProgress.setBounds(20, 50, 300, 20);

        // Label to show progress percentage
        Label percentageLabel = new Label(shell, SWT.NONE);
        percentageLabel.setText("0%");
        percentageLabel.setBounds(330, 50, 50, 20);

        // Horizontal Indeterminate ProgressBar
        Label indeterminateLabel = new Label(shell, SWT.NONE);
        indeterminateLabel.setText("Horizontal Progress (Indeterminate):");
        indeterminateLabel.setBounds(20, 100, 250, 20);

        ProgressBar indeterminateProgress = new ProgressBar(shell, SWT.HORIZONTAL | SWT.INDETERMINATE);
        indeterminateProgress.setBounds(20, 130, 300, 20);

        // Vertical Determinate ProgressBar
        Label verticalLabel = new Label(shell, SWT.NONE);
        verticalLabel.setText("Vertical Progress:");
        verticalLabel.setBounds(20, 180, 150, 20);

        ProgressBar verticalProgress = new ProgressBar(shell, SWT.VERTICAL);
        verticalProgress.setMinimum(0);
        verticalProgress.setMaximum(100);
        verticalProgress.setSelection(50);
        verticalProgress.setBounds(80, 210, 20, 150);

        // Label to show vertical progress percentage
        Label verticalPercentageLabel = new Label(shell, SWT.NONE);
        verticalPercentageLabel.setText("50%");
        verticalPercentageLabel.setBounds(110, 280, 50, 20);

        // Start button to trigger progress
        Button startButton = new Button(shell, SWT.PUSH);
        startButton.setText("Start Progress");
        startButton.setBounds(180, 300, 120, 30);

        startButton.addListener(SWT.Selection, event -> {
            startButton.setEnabled(false);

            Thread progressThread = new Thread(() -> {
                for (int i = 0; i <= 100; i++) {
                    final int progress = i;
                    display.asyncExec(() -> {
                        if (!horizontalProgress.isDisposed() && !verticalProgress.isDisposed()) {
                            horizontalProgress.setSelection(progress);
                            verticalProgress.setSelection(progress);
                            percentageLabel.setText(progress + "%");
                            verticalPercentageLabel.setText(progress + "%");

                            if (progress == 100) {
                                startButton.setEnabled(true);
                            }
                        }
                    });

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            progressThread.start();
        });

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

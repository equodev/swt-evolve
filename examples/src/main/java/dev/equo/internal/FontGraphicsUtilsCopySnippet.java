package dev.equo.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FontGraphicsUtilsCopySnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());
        shell.setText("GraphicsUtils copyFont Test");

        Label label = new Label(shell, SWT.NONE);
        label.setText("Label with Custom Font");
        Font font = new Font(display, "Arial", 14, SWT.ITALIC);
        label.setFont(font);

        Button pushButton = new Button(shell, SWT.PUSH);
        pushButton.setText("Button with Custom Font");
        pushButton.setFont(font);

        Text text = new Text(shell, SWT.BORDER);
        text.setText("Text widget with custom font");
        text.setFont(font);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        font.dispose();
        display.dispose();
    }
}
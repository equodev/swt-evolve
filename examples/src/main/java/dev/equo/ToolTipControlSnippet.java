package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates CoolBar with FillLayout and a Button with tooltip text.
 * Shows how to create a CoolBar with a button that displays a tooltip on hover.
 */
public class ToolTipControlSnippet {

    public static void main(String[] args) {
        Config.useEquo(CoolBar.class);
        Config.useEquo(CoolItem.class);
        Config.useEquo(Button.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("Control ToolTip Snippet");
        shell.setSize(400, 200);

        // Create the CoolBar
        CoolBar coolBar = new CoolBar(shell, SWT.FLAT | SWT.BORDER);

        // Create CoolItem with a button and tooltip text
        CoolItem item1 = new CoolItem(coolBar, SWT.NONE);
        Button button1 = new Button(coolBar, SWT.PUSH);
        button1.setText("Save");
        button1.setToolTipText("Saves a copy of the current file");

        Point size1 = button1.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item1.setControl(button1);
        item1.setPreferredSize(item1.computeSize(size1.x, size1.y));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

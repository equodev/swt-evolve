package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates a vertical CoolBar with wrap indices.
 * Shows how to create a CoolBar with vertical orientation and multiple rows/columns.
 */
public class CoolBarVerticalSnippet {

    public static void main(String[] args) {
        Config.useEquo(CoolBar.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("CoolBar Vertical Snippet");
        shell.setSize(300, 400);

        // Create a vertical CoolBar
        CoolBar coolBar = new CoolBar(shell, SWT.VERTICAL | SWT.BORDER);

        // Create multiple CoolItems with buttons
        for (int i = 0; i < 6; i++) {
            CoolItem item = new CoolItem(coolBar, SWT.NONE);
            Button button = new Button(coolBar, SWT.PUSH);
            button.setText("Button " + (i + 1));
            Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            item.setControl(button);
            item.setPreferredSize(item.computeSize(size.x, size.y));
            item.setMinimumSize(new Point(50, size.y));
        }

        // Set wrap indices to create multiple columns
        // Items 0, 1, 2 in first column, items 3, 4, 5 in second column
        coolBar.setWrapIndices(new int[]{3});

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

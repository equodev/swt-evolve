package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Simple demonstration of SWT ExpandBar widget.
 * Creates an ExpandBar with multiple items, each containing buttons stacked vertically.
 */

public class ExpandBarSnippet {
    public static void main(String[] args) {
        Config.useEquo(ExpandBar.class);
        Config.useEquo(TableItem.class);

        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("ExpandBar Example");

        ExpandBar expandBar = new ExpandBar(shell, SWT.NONE);
        expandBar.setLocation(25, 25);

        ExpandItem item1 = new ExpandItem(expandBar, SWT.NONE);
        item1.setText("Options");
        item1.setExpanded(true);

        Composite composite1 = new Composite(expandBar, SWT.NONE);

        Button button1 = new Button(composite1, SWT.PUSH);
        button1.setText("Option 1");
        button1.setBounds(5, 5, 120, 30);

        Button button2 = new Button(composite1, SWT.PUSH);
        button2.setText("Option 2");
        button2.setBounds(5, 45, 120, 30);

        Button button3 = new Button(composite1, SWT.PUSH);
        button3.setText("Option 3");
        button3.setBounds(5, 85, 120, 30);

        Button button4 = new Button(composite1, SWT.PUSH);
        button4.setText("Option 4");
        button4.setBounds(5, 125, 120, 30);

        composite1.pack();
        item1.setControl(composite1);

        ExpandItem item2 = new ExpandItem(expandBar, SWT.NONE);
        item2.setText("Actions");
        item2.setExpanded(true);

        Composite composite2 = new Composite(expandBar, SWT.NONE);

        Button action1 = new Button(composite2, SWT.PUSH);
        action1.setText("Action 1");
        action1.setBounds(5, 5, 120, 30);

        Button action2 = new Button(composite2, SWT.PUSH);
        action2.setText("Action 2");
        action2.setBounds(5, 45, 120, 30);

        composite2.pack();
        item2.setControl(composite2);

        expandBar.pack();
        expandBar.layout(true, true);
        shell.pack();

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

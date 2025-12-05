package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates the basic usage of CoolBar widget in SWT.
 * This example creates a cool bar with multiple cool items containing different controls.
 */
public class CoolBarSnippet {

    public static void main(String[] args) {
        Config.useEquo(CoolBar.class);
        Config.useEquo(CoolItem.class);
        Config.useEquo(Text.class);
        Config.useEquo(Label.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CoolBar Snippet");
        shell.setSize(800, 120);

        // Create the CoolBar
        CoolBar coolBar = new CoolBar(shell, SWT.FLAT | SWT.BORDER);
        coolBar.setBounds(0, 0, 800, 45);

        // Create CoolItem 1 with a button
        CoolItem item1 = new CoolItem(coolBar, SWT.NONE);
        Button button0 = new Button(coolBar, SWT.PUSH);
        button0.setText("New");
        Point size1 = button0.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item1.setControl(button0);
        item1.setPreferredSize(item1.computeSize(size1.x, size1.y));

        // Create CoolItem 2 with a button
        CoolItem item2 = new CoolItem(coolBar, SWT.NONE);
        Button button1 = new Button(coolBar, SWT.PUSH);
        button1.setText("Cut");
        Point size2 = button1.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item2.setControl(button1);
        item2.setPreferredSize(item2.computeSize(size2.x, size2.y));

        // Create CoolItem 3 with another button
        CoolItem item3 = new CoolItem(coolBar, SWT.NONE);
        Button button2 = new Button(coolBar, SWT.PUSH);
        button2.setText("Copy");
        Point size3 = button2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item3.setControl(button2);
        item3.setPreferredSize(item3.computeSize(size3.x, size3.y));

        // Create CoolItem 4 with text field
        CoolItem item4 = new CoolItem(coolBar, SWT.NONE);
        Text text = new Text(coolBar, SWT.BORDER);
        text.setText("Search...");
        Point size4 = text.computeSize(150, SWT.DEFAULT);
        item4.setControl(text);
        item4.setPreferredSize(item4.computeSize(size4.x, size4.y));

        // Create CoolItem 5 with label
        CoolItem item5 = new CoolItem(coolBar, SWT.NONE);
        Label label2 = new Label(coolBar, SWT.NONE);
        label2.setText("Count: 0");
        Point size5 = label2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item5.setControl(label2);
        item5.setPreferredSize(item5.computeSize(size5.x, size5.y));

        // Create CoolItem 6 with label
        CoolItem item6 = new CoolItem(coolBar, SWT.NONE);
        Label label = new Label(coolBar, SWT.NONE);
        label.setText("Status: Ready");
        Point size6 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        item6.setControl(label);
        item6.setPreferredSize(item6.computeSize(size6.x, size6.y));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

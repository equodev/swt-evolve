package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Simple snippet to test context menu functionality.
 * Right-click on the button to see the context menu.
 */
public class PopUpMenuSnippet {
    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("PopUp Context Menu Example");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(400, 300);

        // Group container
        Group group = new Group(shell, SWT.NONE);
        group.setText("READ_ONLY Combo");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Button with context menu
        Button button = new Button(group, SWT.PUSH);
        button.setText("Right-Click Me!");
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        // Create context menu
        Menu contextMenu = new Menu(button);
        button.setMenu(contextMenu);

        // Add menu items
        MenuItem item1 = new MenuItem(contextMenu, SWT.PUSH);
        item1.setText("Option 1");
        item1.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Option 1 selected")));

        MenuItem item2 = new MenuItem(contextMenu, SWT.PUSH);
        item2.setText("Option 2");
        item2.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Option 2 selected")));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        MenuItem item3 = new MenuItem(contextMenu, SWT.PUSH);
        item3.setText("Exit");
        item3.addSelectionListener(widgetSelectedAdapter(e -> {
            System.out.println("Exit selected");
            shell.close();
        }));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

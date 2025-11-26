package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Snippet with two buttons: one has a menu, the other triggers setVisible(true).
 */
public class SetVisibleMenuSnippet {
    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Set Visible Button Menu Test");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(400, 300);

        // Group container
        Group group = new Group(shell, SWT.NONE);
        group.setText("Menu Control Test");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Button 1: Has a menu
        Button button1 = new Button(group, SWT.PUSH);
        button1.setText("Button with Menu");

        // Create popup menu and assign to button1
        Menu popupMenu = new Menu(button1);
        button1.setMenu(popupMenu);
        popupMenu.setVisible(true);

        // Add menu items
        MenuItem item1 = new MenuItem(popupMenu, SWT.PUSH);
        item1.setText("Option 1");
        item1.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Option 1 selected")));

        MenuItem item2 = new MenuItem(popupMenu, SWT.PUSH);
        item2.setText("Option 2");
        item2.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Option 2 selected")));

        MenuItem item3 = new MenuItem(popupMenu, SWT.PUSH);
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

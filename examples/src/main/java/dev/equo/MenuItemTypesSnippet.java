package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Snippet to test different MenuItem types: CHECK, RADIO, and CASCADE (submenus).
 * Right-click on the button to see the context menu with different item types.
 */
public class MenuItemTypesSnippet {
    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Group.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("MenuItem Types Example");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(400, 500);

        // Group container
        Group group = new Group(shell, SWT.NONE);
        group.setText("Menu Item Types Test");
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Button with context menu
        Button button = new Button(group, SWT.PUSH);
        button.setText("Right-Click Me!");
        button.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        // Label to show menu selections
        Label statusLabel = new Label(shell, SWT.WRAP);
        statusLabel.setText("Status: No selection yet");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create context menu
        Menu contextMenu = new Menu(button);
        button.setMenu(contextMenu);

        // CHECK items
        MenuItem checkItem1 = new MenuItem(contextMenu, SWT.CHECK);
        checkItem1.setText("Enable Feature A");
        checkItem1.addSelectionListener(widgetSelectedAdapter(e -> {
            String status = checkItem1.getSelection() ? "enabled" : "disabled";
            System.out.println("Feature A " + status);
            statusLabel.setText("Status: Feature A " + status);
        }));

        MenuItem checkItem2 = new MenuItem(contextMenu, SWT.CHECK);
        checkItem2.setText("Enable Feature B");
        checkItem2.setSelection(true); // Start checked
        checkItem2.addSelectionListener(widgetSelectedAdapter(e -> {
            String status = checkItem2.getSelection() ? "enabled" : "disabled";
            System.out.println("Feature B " + status);
            statusLabel.setText("Status: Feature B " + status);
        }));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        // RADIO items
        MenuItem radioItem1 = new MenuItem(contextMenu, SWT.RADIO);
        radioItem1.setText("Option A");
        radioItem1.setSelection(true); // Start selected
        radioItem1.addSelectionListener(widgetSelectedAdapter(e -> {
            if (radioItem1.getSelection()) {
                System.out.println("Option A selected");
                statusLabel.setText("Status: Option A selected");
            }
        }));

        MenuItem radioItem2 = new MenuItem(contextMenu, SWT.RADIO);
        radioItem2.setText("Option B");
        radioItem2.addSelectionListener(widgetSelectedAdapter(e -> {
            if (radioItem2.getSelection()) {
                System.out.println("Option B selected");
                statusLabel.setText("Status: Option B selected");
            }
        }));

        MenuItem radioItem3 = new MenuItem(contextMenu, SWT.RADIO);
        radioItem3.setText("Option C");
        radioItem3.addSelectionListener(widgetSelectedAdapter(e -> {
            if (radioItem3.getSelection()) {
                System.out.println("Option C selected");
                statusLabel.setText("Status: Option C selected");
            }
        }));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        // CASCADE item (submenu)
        MenuItem cascadeItem = new MenuItem(contextMenu, SWT.CASCADE);
        cascadeItem.setText("More Options");

        Menu submenu = new Menu(shell, SWT.DROP_DOWN);
        cascadeItem.setMenu(submenu);

        MenuItem subItem1 = new MenuItem(submenu, SWT.PUSH);
        subItem1.setText("Submenu Item 1");
        subItem1.addSelectionListener(widgetSelectedAdapter(e -> {
            System.out.println("Submenu Item 1 selected");
            statusLabel.setText("Status: Submenu Item 1 selected");
        }));

        MenuItem subItem2 = new MenuItem(submenu, SWT.PUSH);
        subItem2.setText("Submenu Item 2");
        subItem2.addSelectionListener(widgetSelectedAdapter(e -> {
            System.out.println("Submenu Item 2 selected");
            statusLabel.setText("Status: Submenu Item 2 selected");
        }));

        new MenuItem(submenu, SWT.SEPARATOR);

        // Nested submenu (CASCADE within CASCADE)
        MenuItem nestedCascade = new MenuItem(submenu, SWT.CASCADE);
        nestedCascade.setText("Even More Options");

        Menu nestedSubmenu = new Menu(shell, SWT.DROP_DOWN);
        nestedCascade.setMenu(nestedSubmenu);

        MenuItem nestedItem1 = new MenuItem(nestedSubmenu, SWT.CHECK);
        nestedItem1.setText("Nested Check Item");
        nestedItem1.addSelectionListener(widgetSelectedAdapter(e -> {
            String status = nestedItem1.getSelection() ? "checked" : "unchecked";
            System.out.println("Nested Check Item " + status);
            statusLabel.setText("Status: Nested Check Item " + status);
        }));

        MenuItem nestedItem2 = new MenuItem(nestedSubmenu, SWT.PUSH);
        nestedItem2.setText("Nested Push Item");
        nestedItem2.addSelectionListener(widgetSelectedAdapter(e -> {
            System.out.println("Nested Push Item selected");
            statusLabel.setText("Status: Nested Push Item selected");
        }));

        new MenuItem(contextMenu, SWT.SEPARATOR);

        MenuItem exitItem = new MenuItem(contextMenu, SWT.PUSH);
        exitItem.setText("Exit");
        exitItem.addSelectionListener(widgetSelectedAdapter(e -> {
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

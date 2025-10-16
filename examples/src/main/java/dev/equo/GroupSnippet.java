package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Demonstrates the use of SWT Group widget with various controls inside.
 * This example creates a shell with multiple groups containing different types of widgets:
 * - A group with buttons arranged vertically
 * - A group with text input fields
 * - A group with checkboxes
 * Each group has a title and organizes related controls together.
 */
public class GroupSnippet {
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Group Widget Example");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(600, 600);

        // Create first group with text fields
        Group textGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        textGroup.setText("Text Input");
        textGroup.setLayout(new GridLayout(2, false));
        GridData textGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textGroupData.heightHint = 80;
        textGroup.setLayoutData(textGroupData);

        Label nameLabel = new Label(textGroup, SWT.NONE);
        nameLabel.setText("Name:");

        Text nameText = new Text(textGroup, SWT.BORDER);
        GridData nameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        nameData.widthHint = 200;
        nameText.setLayoutData(nameData);

        Label emailLabel = new Label(textGroup, SWT.NONE);
        emailLabel.setText("Email:");

        Text emailText = new Text(textGroup, SWT.BORDER);
        GridData emailData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        emailData.widthHint = 200;
        emailText.setLayoutData(emailData);

        // Create second group with buttons
        Group buttonGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        buttonGroup.setText("Button Controls");
        buttonGroup.setLayout(new RowLayout(SWT.VERTICAL));
        GridData buttonGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        buttonGroupData.heightHint = 150;
        buttonGroup.setLayoutData(buttonGroupData);

        Button pushButton = new Button(buttonGroup, SWT.PUSH);
        pushButton.setText("Push Button");
        pushButton.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Push button clicked")));

        Button toggleButton = new Button(buttonGroup, SWT.TOGGLE);
        toggleButton.setText("Toggle Button");
        toggleButton.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Toggle button clicked")));

        // Create third group with checkboxes
        Group checkGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        checkGroup.setText("Options");
        checkGroup.setLayout(new RowLayout(SWT.VERTICAL));
        GridData checkGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        checkGroupData.heightHint = 120;
        checkGroup.setLayoutData(checkGroupData);

        Button check1 = new Button(checkGroup, SWT.CHECK);
        check1.setText("Enable notifications");
        check1.setSelection(true);
        check1.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Notifications: " + check1.getSelection())));

        Button check2 = new Button(checkGroup, SWT.CHECK);
        check2.setText("Auto-save");
        check2.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Auto-save: " + check2.getSelection())));

        Button check3 = new Button(checkGroup, SWT.CHECK);
        check3.setText("Show advanced options");
        check3.addSelectionListener(widgetSelectedAdapter(e ->
            System.out.println("Advanced options: " + check3.getSelection())));

        // Create empty group to demonstrate minimal Group
        Group emptyGroup = new Group(shell, SWT.SHADOW_ETCHED_IN);
        emptyGroup.setText("Empty Group");
        emptyGroup.setLayout(new GridLayout());
        GridData emptyGroupData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        emptyGroupData.heightHint = 80;
        emptyGroup.setLayoutData(emptyGroupData);

        Label emptyLabel = new Label(emptyGroup, SWT.CENTER);
        emptyLabel.setText("(No controls yet)");
        emptyLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

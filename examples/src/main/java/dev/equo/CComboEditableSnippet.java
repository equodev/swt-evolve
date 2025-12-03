package dev.equo;

import static org.eclipse.swt.events.SelectionListener.*;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo Editable - Shows editable CCombo where user can type custom text
 * Key difference: Regular Combo READ_ONLY can't be made editable on all platforms
 * CCombo allows switching between editable and read-only at runtime
 */
public class CComboEditableSnippet {
	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Config.useEquo(Group.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo Editable Example");
		shell.setLayout(new GridLayout(1, false));

		// Editable CCombo - user can type anything
		Group editableGroup = new Group(shell, SWT.NONE);
		editableGroup.setText("Editable CCombo");
		editableGroup.setLayout(new GridLayout(2, false));
		editableGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label1 = new Label(editableGroup, SWT.NONE);
		label1.setText("Type or select:");

		CCombo editableCombo = new CCombo(editableGroup, SWT.BORDER);
		editableCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		editableCombo.setItems(new String[] {"Red", "Green", "Blue", "Yellow", "Orange"});
		editableCombo.setText("Type custom text...");

		// User can type custom text not in the list
		editableCombo.addModifyListener(e -> {
			System.out.println("Text changed to: " + editableCombo.getText());
		});

		// Toggle editable state
		Group toggleGroup = new Group(shell, SWT.NONE);
		toggleGroup.setText("Toggle Editable State");
		toggleGroup.setLayout(new GridLayout(2, false));
		toggleGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label2 = new Label(toggleGroup, SWT.NONE);
		label2.setText("Current state:");

		Button toggleButton = new Button(toggleGroup, SWT.PUSH);
		toggleButton.setText("Make Read-Only");
		toggleButton.addSelectionListener(widgetSelectedAdapter(e -> {
			boolean isEditable = editableCombo.getEditable();
			editableCombo.setEditable(!isEditable);
			toggleButton.setText(isEditable ? "Make Editable" : "Make Read-Only");
			System.out.println("CCombo is now " + (isEditable ? "READ-ONLY" : "EDITABLE"));
		}));

		// Show current text
		Group displayGroup = new Group(shell, SWT.NONE);
		displayGroup.setText("Current Text Display");
		displayGroup.setLayout(new GridLayout(2, false));
		displayGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label3 = new Label(displayGroup, SWT.NONE);
		label3.setText("Current text:");

		Label currentText = new Label(displayGroup, SWT.BORDER);
		currentText.setLayoutData(new GridData(200, SWT.DEFAULT));
		currentText.setText("");

		editableCombo.addSelectionListener(widgetSelectedAdapter(e -> {
			currentText.setText(editableCombo.getText());
		}));

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

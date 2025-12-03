package dev.equo;

import static org.eclipse.swt.events.SelectionListener.*;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo SIMPLE style - Shows list always visible (not dropdown)
 * Key difference: SIMPLE shows the list permanently below the text field
 */
public class CComboSimpleSnippet {
	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Config.useEquo(Group.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo SIMPLE Style");
		shell.setLayout(new GridLayout(1, false));

		String[] items = {"Red", "Green", "Blue", "Yellow", "Orange", "Purple", "Pink", "Brown"};

		// SIMPLE Read-Only CCombo
		Group simpleReadOnlyGroup = new Group(shell, SWT.NONE);
		simpleReadOnlyGroup.setText("SIMPLE + READ_ONLY");
		simpleReadOnlyGroup.setLayout(new GridLayout(2, false));
		simpleReadOnlyGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label1 = new Label(simpleReadOnlyGroup, SWT.NONE);
		label1.setText("Select from list:");

		CCombo simpleReadOnly = new CCombo(simpleReadOnlyGroup, SWT.SIMPLE | SWT.BORDER | SWT.READ_ONLY);
		simpleReadOnly.setLayoutData(new GridData(200, 150));
		simpleReadOnly.setItems(items);
		simpleReadOnly.select(0);

		// SIMPLE Editable CCombo
		Group simpleEditableGroup = new Group(shell, SWT.NONE);
		simpleEditableGroup.setText("SIMPLE + Editable");
		simpleEditableGroup.setLayout(new GridLayout(2, false));
		simpleEditableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label2 = new Label(simpleEditableGroup, SWT.NONE);
		label2.setText("Type or select:");

		CCombo simpleEditable = new CCombo(simpleEditableGroup, SWT.SIMPLE | SWT.BORDER);
		simpleEditable.setLayoutData(new GridData(200, 150));
		simpleEditable.setItems(items);
		simpleEditable.setText("Type custom...");

		// Display group
		Group displayGroup = new Group(shell, SWT.NONE);
		displayGroup.setText("Current Selection");
		displayGroup.setLayout(new GridLayout(2, false));
		displayGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label3 = new Label(displayGroup, SWT.NONE);
		label3.setText("Selected:");

		Label currentText = new Label(displayGroup, SWT.BORDER);
		currentText.setLayoutData(new GridData(200, SWT.DEFAULT));
		currentText.setText("Red");

		// Add listeners
		simpleReadOnly.addSelectionListener(widgetSelectedAdapter(e -> {
			currentText.setText(simpleReadOnly.getText());
			System.out.println("Read-Only selected: " + simpleReadOnly.getText());
		}));

		simpleEditable.addSelectionListener(widgetSelectedAdapter(e -> {
			currentText.setText(simpleEditable.getText());
			System.out.println("Editable selected: " + simpleEditable.getText());
		}));

		simpleEditable.addModifyListener(e -> {
			System.out.println("Text modified: " + simpleEditable.getText());
		});

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

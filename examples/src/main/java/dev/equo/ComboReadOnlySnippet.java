package dev.equo;

import static org.eclipse.swt.events.SelectionListener.*;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates READ_ONLY combo widget.
 * This combo allows selection from a predefined list but doesn't allow typing.
 */

public class ComboReadOnlySnippet {
	public static void main(String[] args) {
		Config.useEquo(Group.class);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(300, 300);
		shell.setText("ComboReadOnlySnippet - Selection Test");
		shell.setLayout(new GridLayout(1, false));

		// READ_ONLY Combo
		Group group1 = new Group(shell, SWT.NONE);
		group1.setText("READ_ONLY Combo");
		group1.setLayout(new GridLayout(2, false));
		GridData groupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		group1.setLayoutData(groupData);

		Label label1 = new Label(group1, SWT.NONE);
		label1.setText("READ_ONLY:");

		Combo readOnlyCombo = new Combo(group1, SWT.READ_ONLY);
		readOnlyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		for (int i = 0; i < 5; i++) {
			readOnlyCombo.add("Item " + i);
		}
		readOnlyCombo.select(0);
		readOnlyCombo.addSelectionListener(widgetSelectedAdapter(e ->
				System.out.println("READ_ONLY Selection: " + readOnlyCombo.getText())));

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

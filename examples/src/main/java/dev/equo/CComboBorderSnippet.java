package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo with different border styles
 */
public class CComboBorderSnippet {
	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Config.useEquo(Group.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo - Border Styles");
		shell.setLayout(new GridLayout(1, false));

		String[] items = {"Apple", "Banana", "Cherry", "Date", "Elderberry"};

		// Border styles group
		Group borderGroup = new Group(shell, SWT.NONE);
		borderGroup.setText("Border Styles");
		borderGroup.setLayout(new GridLayout(2, false));
		borderGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label1 = new Label(borderGroup, SWT.NONE);
		label1.setText("BORDER:");

		CCombo borderedCombo = new CCombo(borderGroup, SWT.BORDER | SWT.READ_ONLY);
		borderedCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		borderedCombo.setItems(items);
		borderedCombo.select(0);

		Label label2 = new Label(borderGroup, SWT.NONE);
		label2.setText("FLAT:");

		CCombo flatCombo = new CCombo(borderGroup, SWT.FLAT | SWT.READ_ONLY);
		flatCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		flatCombo.setItems(items);
		flatCombo.select(1);

		Label label3 = new Label(borderGroup, SWT.NONE);
		label3.setText("No style:");

		CCombo plainCombo = new CCombo(borderGroup, SWT.READ_ONLY);
		plainCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		plainCombo.setItems(items);
		plainCombo.select(2);

		// Selection info group
		Group infoGroup = new Group(shell, SWT.NONE);
		infoGroup.setText("Selection Info");
		infoGroup.setLayout(new GridLayout(2, false));
		infoGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label infoLabel = new Label(infoGroup, SWT.NONE);
		infoLabel.setText("Selected:");

		Label selectionValue = new Label(infoGroup, SWT.BORDER);
		selectionValue.setLayoutData(new GridData(200, SWT.DEFAULT));
		selectionValue.setText("Apple");

		// Add listeners
		SelectionAdapter updateSelection = new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				CCombo source = (CCombo) e.widget;
				selectionValue.setText(source.getText());
				System.out.println("Selected: " + source.getText());
			}
		};

		borderedCombo.addSelectionListener(updateSelection);
		flatCombo.addSelectionListener(updateSelection);
		plainCombo.addSelectionListener(updateSelection);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

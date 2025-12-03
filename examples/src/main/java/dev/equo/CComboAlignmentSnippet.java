package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo with different text alignments
 */
public class CComboAlignmentSnippet {
	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Config.useEquo(Group.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo - Text Alignment");
		shell.setLayout(new GridLayout(1, false));

		String[] items = {"Apple", "Banana", "Cherry", "Date", "Elderberry"};

		// Alignment group
		Group alignmentGroup = new Group(shell, SWT.NONE);
		alignmentGroup.setText("Text Alignment");
		alignmentGroup.setLayout(new GridLayout(2, false));
		alignmentGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label1 = new Label(alignmentGroup, SWT.NONE);
		label1.setText("LEFT aligned:");

		CCombo leftCombo = new CCombo(alignmentGroup, SWT.BORDER | SWT.READ_ONLY | SWT.LEFT);
		leftCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		leftCombo.setItems(items);
		leftCombo.select(0);

		Label label2 = new Label(alignmentGroup, SWT.NONE);
		label2.setText("CENTER aligned:");

		CCombo centerCombo = new CCombo(alignmentGroup, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		centerCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		centerCombo.setItems(items);
		centerCombo.select(1);

		Label label3 = new Label(alignmentGroup, SWT.NONE);
		label3.setText("RIGHT aligned:");

		CCombo rightCombo = new CCombo(alignmentGroup, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		rightCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		rightCombo.setItems(items);
		rightCombo.select(2);

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

		leftCombo.addSelectionListener(updateSelection);
		centerCombo.addSelectionListener(updateSelection);
		rightCombo.addSelectionListener(updateSelection);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

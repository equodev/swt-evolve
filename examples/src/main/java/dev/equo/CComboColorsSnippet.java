package dev.equo;

import static org.eclipse.swt.events.SelectionListener.*;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo with custom colors
 */
public class CComboColorsSnippet {
	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Config.useEquo(Group.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo - Custom Colors");
		shell.setLayout(new GridLayout(1, false));

		String[] items = {"Apple", "Banana", "Cherry", "Date", "Elderberry"};

		// Custom colors group
		Group colorGroup = new Group(shell, SWT.NONE);
		colorGroup.setText("Custom Colors");
		colorGroup.setLayout(new GridLayout(2, false));
		colorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label label1 = new Label(colorGroup, SWT.NONE);
		label1.setText("Default colors:");

		CCombo defaultCombo = new CCombo(colorGroup, SWT.BORDER | SWT.READ_ONLY);
		defaultCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		defaultCombo.setItems(items);
		defaultCombo.select(0);

		Label label2 = new Label(colorGroup, SWT.NONE);
		label2.setText("Blue background:");

		CCombo blueCombo = new CCombo(colorGroup, SWT.BORDER | SWT.READ_ONLY);
		blueCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		blueCombo.setItems(items);
		blueCombo.select(1);
		blueCombo.setBackground(new Color(220, 240, 255)); // Light blue

		Label label3 = new Label(colorGroup, SWT.NONE);
		label3.setText("Red foreground:");

		CCombo redTextCombo = new CCombo(colorGroup, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		redTextCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		redTextCombo.setItems(items);
		redTextCombo.select(2);
		redTextCombo.setForeground(new Color(200, 0, 0)); // Dark red

		Label label4 = new Label(colorGroup, SWT.NONE);
		label4.setText("Green bg + white fg:");

		CCombo greenCombo = new CCombo(colorGroup, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		greenCombo.setLayoutData(new GridData(200, SWT.DEFAULT));
		greenCombo.setItems(items);
		greenCombo.select(3);
		greenCombo.setBackground(new Color(0, 120, 0)); // Dark green
		greenCombo.setForeground(new Color(255, 255, 255)); // White

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

		defaultCombo.addSelectionListener(updateSelection);
		blueCombo.addSelectionListener(updateSelection);
		redTextCombo.addSelectionListener(updateSelection);
		greenCombo.addSelectionListener(updateSelection);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * A five-column ColumnLayout form of label-over-field cells, one of them a DROP_DOWN Combo, with a
 * hidden cell at the end of each of the first four columns. ColumnLayout stacks cells by height, so
 * the rows only line up across the form while the Combo cell is about as tall as a Text cell; a
 * taller Combo pushes the hidden cell to the top of the next column and every later column starts
 * one row lower.
 */
public class ComboColumnLayoutFormSnippet {

	static final int CELLS = 23;
	static final int ROWS = 5;
	static final int COMBO_CELL = 3;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("ColumnLayout form with a Combo");
		shell.setLayout(new FillLayout());

		Composite form = new Composite(shell, SWT.BORDER);
		ColumnLayout layout = new ColumnLayout();
		layout.minNumColumns = 5;
		layout.maxNumColumns = 5;
		form.setLayout(layout);

		for (int i = 0; i < CELLS; i++) {
			Composite cell = new Composite(form, SWT.NONE);
			cell.setLayout(new GridLayout(1, false));
			new Label(cell, SWT.LEFT).setText("Field " + (i + 1));
			Control field;
			if (i == COMBO_CELL) {
				Combo combo = new Combo(cell, SWT.BORDER | SWT.DROP_DOWN);
				combo.setItems("", "One", "Two");
				field = combo;
			} else {
				field = new Text(cell, SWT.BORDER);
			}
			field.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			boolean hidden = i % ROWS == ROWS - 1;
			cell.setVisible(!hidden);
		}

		shell.setSize(1100, 420);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * CCombo values at widths the application pins, the way a toolbar contribution does.
 *
 * Each row pins a width through RowData and selects the value that has to survive it. The
 * value must be readable in full at every pinned width, exactly as a native CCombo shows it.
 * The last row pins nothing, so the combo takes its own preferred width.
 *
 * Toggle Config.useEclipse(CCombo.class) below to compare against native SWT.
 */
public class CComboPinnedWidthSnippet {

	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo - application-pinned widths");
		shell.setLayout(new GridLayout(2, false));

		String[] sizes = {"Small", "Medium", "Large", "X-Large"};
		String[] materials = {"Aluminium", "Steel", "Stainless Steel"};

		row(shell, "Pinned to 80px:", sizes, 3, 80);
		row(shell, "Pinned to 145px:", materials, 2, 145);
		row(shell, "Unpinned:", sizes, 3, SWT.DEFAULT);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

	private static void row(Shell shell, String label, String[] items, int selected, int width) {
		Label caption = new Label(shell, SWT.NONE);
		caption.setText(label);
		caption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		// A RowLayout container, so the pinned width arrives as RowData rather than a
		// GridData hint -- the shape a toolbar contribution uses.
		Composite holder = new Composite(shell, SWT.NONE);
		holder.setLayout(new RowLayout(SWT.HORIZONTAL));

		CCombo combo = new CCombo(holder, SWT.BORDER | SWT.READ_ONLY);
		combo.setItems(items);
		combo.select(selected);
		if (width != SWT.DEFAULT) combo.setLayoutData(new RowData(width, SWT.DEFAULT));
	}
}

package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * CCombo per-item tooltips (issue #610): tooltip strings are delivered through
 * {@code setData("org.eclipse.swt.custom.CCombo.itemToolTips", String[])} and rendered by the
 * Flutter dropdown. Open the dropdown and hover an item to see its documentation panel.
 */
public class CComboTooltipSnippet {

	private static final String ITEM_TOOLTIPS_KEY = "org.eclipse.swt.custom.CCombo.itemToolTips";

	public static void main(String[] args) {
		Config.useEquo(CCombo.class);
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo per-item tooltips");
		shell.setLayout(new GridLayout(1, false));

		Label label = new Label(shell, SWT.NONE);
		label.setText("Open the dropdown and hover an item to see its tooltip:");

		String[] items = {"Red", "Green", "Blue"};
		String[] tooltips = {
			"<h4>Red</h4><p>The colour <b>red</b> (#FF0000).</p><p><a href=\"https://example.com/red\">More</a></p>",
			"<h4>Green</h4><p>The colour <b>green</b> (#00FF00).</p><ul><li>a primary colour</li></ul>",
			"<h4>Blue</h4><p>The colour <b>blue</b> (#0000FF).</p><p><a href=\"https://example.com/blue\">More</a></p>",
		};

		CCombo combo = new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(260, SWT.DEFAULT));
		combo.setItems(items);
		combo.setData(ITEM_TOOLTIPS_KEY, tooltips);
		combo.select(0);

		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

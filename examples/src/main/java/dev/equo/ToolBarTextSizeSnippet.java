package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * ToolItem text in a roomy ToolBar and in a ToolBar hosted through {@code ToolItem.setControl}, next
 * to a Label: every item's text, PUSH, CHECK and DROP_DOWN, draws at the theme's ToolItem size in
 * both bars, unscaled.
 */
public class ToolBarTextSizeSnippet {

	public static void main(String[] args) {
		Config.defaultToEquo();

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("ToolBarTextSizeSnippet");
		shell.setLayout(new GridLayout(2, false));

		new Label(shell, SWT.NONE).setText("Roomy bar:");
		ToolBar roomy = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		roomy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new ToolItem(roomy, SWT.PUSH).setText("Run");
		new ToolItem(roomy, SWT.PUSH).setText("Tools");
		new ToolItem(roomy, SWT.DROP_DOWN).setText("Debug");

		new Label(shell, SWT.NONE).setText("Hosted bar:");
		ToolBar outer = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		ToolItem host = new ToolItem(outer, SWT.SEPARATOR);
		Composite panel = new Composite(outer, SWT.NONE);
		panel.setLayout(new FillLayout());
		ToolBar inner = new ToolBar(panel, SWT.FLAT | SWT.RIGHT);
		new ToolItem(inner, SWT.PUSH).setText("All on");
		new ToolItem(inner, SWT.CHECK).setText("Columns");
		panel.pack();
		host.setWidth(panel.getSize().x);
		host.setControl(panel);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Demonstrates the basic usage of ToolBar widget in SWT.
 * This example creates a simple toolbar with 8 push-style tool items.
 */
public class ToolBarSnippet {

public static void main (String [] args) {
	Config.useEquo(ToolBar.class);

	Display display = new Display();
	Shell shell = new Shell (display);
	shell.setLayout (new FillLayout());
	shell.setText("ToolBarSnippet");
    shell.setSize(450, 64);
    ToolBar bar = new ToolBar (shell, SWT.BORDER);
	for (int i=0; i<8; i++) {
		ToolItem item = new ToolItem (bar, SWT.PUSH);
		item.setText ("Item " + i);
	}
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
}

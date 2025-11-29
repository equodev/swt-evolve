package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ListSnippet {

public static void main (String [] args) {
	Config.useEquo(List.class);

	Display display = new Display ();
	Shell shell = new Shell (display);
	shell.setText("ListSnippet");
	shell.setLayout (new FillLayout ());
	final List list = new List (shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
	String[] items = {"Item 0", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9"};
	list.setItems(items);
	list.addListener (SWT.Selection, e -> {
		String string = "";
		for (int index : list.getSelectionIndices())
			string += index + " ";
		System.out.println ("Selection={" + string + "}");
	});
	list.addListener (SWT.DefaultSelection, e -> {
		String string = "";
		for (int index : list.getSelectionIndices())
			string += index + " ";
		System.out.println ("DefaultSelection={" + string + "}");
	});
	shell.pack ();
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
}

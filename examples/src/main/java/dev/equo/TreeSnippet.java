package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeSnippet {

public static void main (String [] args) {
	Config.forceEquo();
	Display display = new Display ();
	Shell shell = new Shell (display);
	shell.setText("TreeSnippet");
	shell.setLayout (new FillLayout ());
	final Tree tree = new Tree (shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
	for (int i=0; i<4; i++) {
		TreeItem item0 = new TreeItem (tree, 0);
		item0.setText ("Item " + i);
		for (int j=0; j<4; j++) {
			TreeItem item1 = new TreeItem (item0, 0);
			item1.setText ("SubItem " + i + " " + j);
			for (int k=0; k<4; k++) {
				TreeItem item2 = new TreeItem (item1, 0);
				item2.setText ("SubItem " + i + " " + j + " " + k);
			}
		}
	}
	tree.addListener (SWT.Selection, e -> {
		String string = "";
		for (TreeItem item : tree.getSelection ())
			string += item + " ";
		System.out.println ("Selection={" + string + "}");
	});
	tree.addListener (SWT.DefaultSelection, e -> {
		String string = "";
		for (TreeItem item : tree.getSelection ())
			string += item + " ";
		System.out.println ("DefaultSelection={" + string + "}");
	});
	tree.addListener (SWT.Expand, e -> System.out.println ("Expand={" + e.item + "}"));
	tree.addListener (SWT.Collapse, e -> System.out.println ("Collapse={" + e.item + "}"));
	tree.getItems () [0].setExpanded (true);
	shell.pack ();
	shell.setSize(250,  300);
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	display.dispose ();
}
}

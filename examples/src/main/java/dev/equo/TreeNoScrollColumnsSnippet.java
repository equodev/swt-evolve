package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Two trees whose columns (150 + 120 + 110) are wider than the tree (300). Each cell is outlined
 * in red at the bounds Java gives it (TreeItem.getBounds). The outline must frame the cell's text
 * in both trees; the NO_SCROLL tree clips its last column, the other one scrolls.
 */
public class TreeNoScrollColumnsSnippet {

	public static void main(String[] args) {
		if (args.length == 0 || !"native".equals(args[0])) Config.forceEquo();
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("TreeNoScrollColumnsSnippet");
		shell.setLayout(new GridLayout(2, true));

		new Label(shell, SWT.NONE).setText("SWT.NO_SCROLL (clips)");
		new Label(shell, SWT.NONE).setText("default (scrolls)");
		createTree(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.NO_SCROLL);
		createTree(shell, SWT.BORDER | SWT.FULL_SELECTION);

		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

	static void createTree(Shell shell, int style) {
		Tree tree = new Tree(shell, style);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, true);
		data.widthHint = 300;
		data.heightHint = 200;
		tree.setLayoutData(data);
		int[] widths = {150, 120, 110};
		for (int i = 0; i < widths.length; i++) {
			TreeColumn column = new TreeColumn(tree, SWT.LEFT);
			column.setText("Column " + i);
			column.setWidth(widths[i]);
		}
		for (int i = 0; i < 5; i++) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(new String[] {"alpha " + i, "beta " + i, "gamma " + i});
		}
		tree.addListener(SWT.PaintItem, event -> {
			Rectangle cell = ((TreeItem) event.item).getBounds(event.index);
			event.gc.setForeground(event.display.getSystemColor(SWT.COLOR_RED));
			event.gc.drawRectangle(cell.x + 1, cell.y + 1, cell.width - 3, cell.height - 3);
		});
	}
}

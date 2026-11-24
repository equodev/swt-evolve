package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A feed view's tree: columns sized for the platform font (100px name, 80px date, time and author,
 * 50px counters). Every short header label ("Date", "Total", "Unread") must show whole, as it does
 * natively at these widths; only the long first label may be cut.
 */
public class TreeNarrowColumnHeadersSnippet {

	public static void main(String[] args) {
		if (args.length == 0 || !"native".equals(args[0])) Config.forceEquo();
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("TreeNarrowColumnHeadersSnippet");
		shell.setLayout(new FillLayout());

		Tree tree = new Tree(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);
		String[] titles = {"Project / Feed Query / Feed Entry", "Date", "Time", "Author", "Total", "Unread"};
		int[] widths = {100, 80, 80, 80, 50, 50};
		for (int i = 0; i < titles.length; i++) {
			TreeColumn column = new TreeColumn(tree, SWT.LEFT);
			column.setText(titles[i]);
			column.setWidth(widths[i]);
		}
		TreeItem project = new TreeItem(tree, SWT.NONE);
		project.setText(new String[] {"DEMO_PROJECT", "", "", "", "12", "3"});
		TreeItem feed = new TreeItem(project, SWT.NONE);
		feed.setText(new String[] {"Runtime errors", "2026-11-20", "10:42", "SYSTEM", "12", "3"});
		project.setExpanded(true);

		shell.setSize(520, 200);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

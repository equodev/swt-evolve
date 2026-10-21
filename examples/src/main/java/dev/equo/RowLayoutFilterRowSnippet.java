package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * A filter row as a RowLayout lays it out: each child top-aligned at its preferred height, so the
 * date fields, check boxes and combo only line up when they are equally tall.
 */
public class RowLayoutFilterRowSnippet {

	static class DateField extends Composite {
		final Text text;
		final Button arrow;

		DateField(Composite parent, int style) {
			super(parent, style);
			text = new Text(this, SWT.SINGLE);
			text.setText("21.10.2026");
			arrow = new Button(this, SWT.ARROW | SWT.DOWN);
			addListener(SWT.Resize, e -> {
				Rectangle area = getClientArea();
				Point arrowSize = arrow.computeSize(SWT.DEFAULT, area.height);
				text.setBounds(0, 0, area.width - arrowSize.x, area.height);
				arrow.setBounds(area.width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
			});
		}

		@Override
		public Point computeSize(int wHint, int hHint, boolean changed) {
			Point textSize = text.computeSize(wHint, SWT.DEFAULT, changed);
			Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
			int border = getBorderWidth();
			int height = Math.max(hHint, Math.max(textSize.y, arrowSize.y) + 2 * border);
			int width = Math.max(wHint, textSize.x + arrowSize.x + 2 * border);
			return new Point(width, height);
		}
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("RowLayout filter row");
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite row = new Composite(shell, SWT.BORDER);
		row.setLayout(new RowLayout());
		DateField from = new DateField(row, SWT.BORDER);
		DateField to = new DateField(row, SWT.BORDER);
		Button open = new Button(row, SWT.CHECK);
		open.setText("open");
		Button billed = new Button(row, SWT.CHECK);
		billed.setText("settlement");
		Combo all = new Combo(row, SWT.READ_ONLY);
		all.setItems("All", "KVG", "UVG");
		all.select(0);

		shell.setSize(700, 120);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

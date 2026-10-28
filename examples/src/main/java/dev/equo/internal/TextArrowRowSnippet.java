package dev.equo.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

/**
 * A row of single-line controls in a RowLayout, each top-aligned at its preferred height: two fields
 * made of a borderless Text with an ARROW button beside it, two check boxes and a read-only Combo.
 * The field is as tall as the taller of its Text and its arrow.
 */
public class TextArrowRowSnippet {

	static class ArrowField extends Composite {
		final Text text;
		final Button arrow;

		ArrowField(Composite parent, int style) {
			super(parent, style);
			text = new Text(this, SWT.SINGLE);
			text.setText("2026-01-01");
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
		shell.setText("Text and ARROW row");
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite row = new Composite(shell, SWT.BORDER);
		row.setLayout(new RowLayout());
		ArrowField from = new ArrowField(row, SWT.BORDER);
		ArrowField to = new ArrowField(row, SWT.BORDER);
		Button first = new Button(row, SWT.CHECK);
		first.setText("first");
		Button second = new Button(row, SWT.CHECK);
		second.setText("second");
		Combo choice = new Combo(row, SWT.READ_ONLY);
		choice.setItems("All", "A", "B");
		choice.select(0);

		shell.setSize(700, 120);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}
}

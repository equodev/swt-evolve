package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A text editor coloured by a light-themed application (white ground, black text, a pale band on
 * the current line) inside a Canvas, as a SourceViewer hosts it, while the theme paints dark. Every
 * line has to read: the editor keeps the ground its text colour was picked for.
 */
public class StyledTextLightColorsDarkThemeSnippet {

	public static void main(String[] args) {
		System.setProperty("swt.evolve.force_theme", "dark");

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("StyledText - light application colours on a dark theme");
		shell.setLayout(new FillLayout());

		Canvas canvas = new Canvas(shell, SWT.NONE);
		canvas.setLayout(new FillLayout());

		StyledText text = new StyledText(canvas, SWT.MULTI | SWT.V_SCROLL);
		text.setText("first line of text\nsecond line, caret here\nthird line of text");
		text.setBackground(new Color(display, 255, 255, 255));
		text.setForeground(new Color(display, 0, 0, 0));
		text.setLineBackground(1, 1, new Color(display, 232, 242, 254));

		shell.setSize(400, 200);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}

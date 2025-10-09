package dev.equo.draw2d;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Label;

/**
 * Demonstrates the simplest Draw2D example using a FigureCanvas and Label.
 * This example creates a basic Draw2D application that displays "Hello World"
 * using Draw2D's figure system on a canvas.
 */
public class Draw2DHelloWorld {

	public static void main(String[] args) {

		Display d = new Display();
		Shell shell = new Shell(d);
		shell.setLayout(new FillLayout());

		FigureCanvas canvas = new FigureCanvas(shell);
		canvas.setContents(new Label("Hello World")); //$NON-NLS-1$

		shell.setText("Draw2d"); //$NON-NLS-1$
		shell.setSize(200, 100);
		shell.open();
		while (!shell.isDisposed()) {
			while (!d.readAndDispatch()) {
				d.sleep();
			}
		}

	}

}

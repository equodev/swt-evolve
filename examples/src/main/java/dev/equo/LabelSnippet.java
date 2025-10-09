package dev.equo;

/*
 * Label example snippet: create a label (with an image)
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 */

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class LabelSnippet {

public static void main (String[] args) {
	Config.useEquo(Label.class);
	Display display = new Display();
	Image image = new Image(display, LabelSnippet.class.getClassLoader().getResourceAsStream("swt-evolve.png"));
	Shell shell = new Shell (display);
	shell.setText("Label Snippet");
	shell.setSize(400, 200);
	Label label = new Label (shell, SWT.BORDER);
	label.setText ("Label with Image");
	label.setImage (image);
	label.setSize(200, 100);
	shell.open ();
	while (!shell.isDisposed ()) {
		if (!display.readAndDispatch ()) display.sleep ();
	}
	image.dispose ();
	display.dispose ();
}

}

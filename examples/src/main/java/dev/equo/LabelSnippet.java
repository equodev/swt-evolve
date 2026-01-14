package dev.equo;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates Label widget with text and image.
 * This example showcases:
 * - Creating a label with both text and image
 * - Loading images from resources
 * - Basic label styling with border
 */
public class LabelSnippet {

    public static void main (String[] args) {
        ConfigFlags.use_swt_fonts(true);
        Display display = new Display();
        Image image = new Image(display, LabelSnippet.class.getClassLoader().getResourceAsStream("img.png"));
        Shell shell = new Shell (display);
        shell.setText("Label Snippet");
        shell.setSize(400, 200);
        Label label = new Label (shell, SWT.BORDER);
        label.setText ("Label with Image");
        label.setImage (image);
        label.setSize(300, 150);
        label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
        image.dispose ();
        display.dispose ();
    }
}

package dev.equo.internal;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Expected behavior:
 * - Images created from resources start as SwtImage
 * - When setImage() is called on widgets, copyImage() converts SwtImage → DartImage
 * - copyImageData() creates a deep copy of ImageData (clone() method)
 * - Once converted to DartImage, subsequent setImage() calls reuse the same DartImage
 */
public class ImageGraphicsUtilsCopySnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());
        shell.setText("GraphicsUtils copyImage Test");

        Label label = new Label(shell, SWT.NONE);
        label.setText("Label with image");
        Image image = new Image(display, ImageGraphicsUtilsCopySnippet.class.getClassLoader().getResourceAsStream("swt-evolve.png"));
        label.setImage(image);

        Button pushButton = new Button(shell, SWT.PUSH);
        pushButton.setText("Button with image");
        pushButton.setImage(image);

        Button checkButton = new Button(shell, SWT.CHECK);
        checkButton.setText("Check with Image");
        checkButton.setImage(image);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        image.dispose();
        display.dispose();
    }
}
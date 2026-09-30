package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A background drawn the way an application draws one: a one-pixel strip filled with a gradient by
 * a GC, repeated across a panel. The pixels of an Image a GC drew are the render side's, so what
 * this side holds is the buffer {@code init()} allocated — all zeros. Serializing that hands the
 * client a black image it cannot tell from a real one.
 *
 * <p>{@code ./gradlew :examples:runWebExample -PmainClass=dev.equo.GcBackgroundImageSnippet}
 */
public class GcBackgroundImageSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GC-drawn background image");
        shell.setLayout(new FillLayout());

        Image strip = new Image(display, 1, 28);
        GC gc = new GC(strip);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
        gc.fillGradientRectangle(0, 0, 1, 28, true);
        gc.dispose();

        Composite panel = new Composite(shell, SWT.NONE);
        panel.setLayout(new FillLayout());
        panel.setBackgroundImage(strip);
        new Label(panel, SWT.NONE).setText("The panel behind this label carries the strip.");

        shell.setSize(700, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        strip.dispose();
        display.dispose();
    }
}

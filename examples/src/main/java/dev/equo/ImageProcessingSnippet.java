package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The three transforms an application's image service builds on: scale, gray/disable, and
 * decoration. Each row is labelled with what it must look like, so a wrong render is readable
 * without a reference screenshot.
 *
 * <p>Row 1 is the source. Row 2 must be visibly larger. Rows 3 and 4 must be gray and dimmed —
 * a colored icon there is the bug. Row 5 must carry a red square in its lower-right corner.
 *
 * <p>Run: {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.ImageProcessingSnippet}
 * <br>Web: {@code ./gradlew :examples:runWebExample -PmainClass=dev.equo.ImageProcessingSnippet}
 * <br>Native oracle: swap {@link Config#forceEquo()} for {@link Config#forceEclipse()}.
 */
public class ImageProcessingSnippet {

    private static final int SIZE = 96;

    public static void main(String[] args) {
        // Images reach a Label as tinted monochrome icons otherwise, which flattens exactly the
        // color difference these rows exist to show.
        System.setProperty("swt.evolve.preserve_icon_colors", "true");
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ImageProcessingSnippet — scaled / grayed / decorated");
        shell.setLayout(new GridLayout(1, false));

        Image source = new Image(display, sourceData());

        row(shell, "1. source — orange disc on blue", source);
        row(shell, "2. scaled to 160x160 — same picture, larger", scaled(display, source, 160));
        row(shell, "3. IMAGE_GRAY — no color left, only grays", new Image(display, source, SWT.IMAGE_GRAY));
        row(shell, "4. IMAGE_DISABLE — washed out, lighter than the source", new Image(display, source, SWT.IMAGE_DISABLE));
        row(shell, "5. decorated — red square over the lower-right corner", decorated(display, source));

        shell.setSize(720, 900);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void row(Shell shell, String what, Image image) {
        Group group = new Group(shell, SWT.NONE);
        group.setText(what);
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Label label = new Label(group, SWT.NONE);
        label.setImage(image);
    }

    /** An orange disc on blue: any color left after IMAGE_GRAY is visible at a glance. */
    private static ImageData sourceData() {
        PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
        ImageData data = new ImageData(SIZE, SIZE, 24, palette);
        int blue = palette.getPixel(new RGB(30, 60, 200));
        int orange = palette.getPixel(new RGB(240, 140, 20));
        int c = SIZE / 2;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int dx = x - c, dy = y - c;
                data.setPixel(x, y, dx * dx + dy * dy <= (c - 3) * (c - 3) ? orange : blue);
            }
        }
        return data;
    }

    private static Image scaled(Display display, Image source, int size) {
        return new Image(display, source.getImageData().scaledTo(size, size));
    }

    /** JFace's decoration mechanism — what an application's "decorated image" service is built on. */
    private static Image decorated(Display display, Image base) {
        ImageData overlay = new ImageData(SIZE / 2, SIZE / 2, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        for (int y = 0; y < overlay.height; y++) {
            for (int x = 0; x < overlay.width; x++) {
                overlay.setPixel(x, y, overlay.palette.getPixel(new RGB(220, 0, 0)));
            }
        }
        ImageData baseData = base.getImageData();
        return new CompositeImageDescriptor() {
            @Override
            protected Point getSize() {
                return new Point(SIZE, SIZE);
            }

            @Override
            protected void drawCompositeImage(int width, int height) {
                drawImage(zoom -> zoom == 100 ? baseData : null, 0, 0);
                drawImage(zoom -> zoom == 100 ? overlay : null, SIZE / 2, SIZE / 2);
            }
        }.createImage(display);
    }
}

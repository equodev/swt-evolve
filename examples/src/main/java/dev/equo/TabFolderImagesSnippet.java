package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * TabFolder tabs with no image, with an image painted with a GC, and with an image built from
 * pixel data, on a TOP and on a BOTTOM folder. Uses the public SWT API only, so the same class
 * runs on native SWT for comparison.
 */
public class TabFolderImagesSnippet {

    /** A 16x16 image painted with a GC: an orange disc with a dark ring. */
    static Image gcImage(Display display) {
        Image image = new Image(display, 16, 16);
        GC gc = new GC(image);
        gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, 16, 16);
        gc.setBackground(new Color(display, 240, 140, 20));
        gc.fillOval(1, 1, 14, 14);
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawOval(1, 1, 13, 13);
        gc.dispose();
        return image;
    }

    /** A 16x16 image built from pixel data only: a green square with a blue diagonal. */
    static Image bytesImage(Display display) {
        PaletteData palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
        ImageData data = new ImageData(16, 16, 24, palette);
        int green = palette.getPixel(new RGB(30, 170, 60));
        int blue = palette.getPixel(new RGB(20, 60, 220));
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                data.setPixel(x, y, Math.abs(x - y) <= 1 ? blue : green);
            }
        }
        return new Image(display, data);
    }

    static TabFolder folder(Shell shell, int style, Image gc, Image bytes, int selected) {
        TabFolder folder = new TabFolder(shell, style);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        String[] names = { "Plain", "GC image", "Bytes image" };
        Image[] images = { null, gc, bytes };
        for (int i = 0; i < names.length; i++) {
            TabItem item = new TabItem(folder, SWT.NONE);
            item.setText(names[i]);
            if (images[i] != null) item.setImage(images[i]);
            Label page = new Label(folder, SWT.NONE);
            page.setText("Page of \"" + names[i] + "\"");
            item.setControl(page);
        }
        folder.setSelection(selected);
        return folder;
    }

    public static Shell create(Display display) {
        Shell shell = new Shell(display);
        shell.setText("TabFolderImagesSnippet");
        shell.setLayout(new GridLayout());
        Image gc = gcImage(display);
        Image bytes = bytesImage(display);

        TabFolder plain = new TabFolder(shell, SWT.TOP);
        plain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        for (int i = 1; i <= 3; i++) {
            TabItem item = new TabItem(plain, SWT.NONE);
            item.setText("Plain " + i);
            Label page = new Label(plain, SWT.NONE);
            page.setText("No tab has an image");
            item.setControl(page);
        }
        folder(shell, SWT.TOP, gc, bytes, 1);
        folder(shell, SWT.BOTTOM, gc, bytes, 2);

        shell.addDisposeListener(e -> {
            gc.dispose();
            bytes.dispose();
        });
        shell.setSize(420, 330);
        return shell;
    }

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = create(display);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

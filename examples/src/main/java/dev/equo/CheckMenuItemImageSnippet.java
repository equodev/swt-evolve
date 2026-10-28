package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A pop-up menu mixing PUSH and CHECK items, with and without an image, each CHECK item in both
 * selection states. The menu opens by itself shortly after the shell does.
 *
 * <p>Native macOS draws no box for a CHECK item: a check mark appears in the state column only
 * while the item is selected, and an unselected CHECK item looks exactly like a PUSH item. Compare
 * the leading area and the label start of every row against that.
 */
public class CheckMenuItemImageSnippet {
    public static void main(String[] args) {
        Config.useEquo(Menu.class);
        Config.useEquo(MenuItem.class);
        Config.useEquo(Button.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Check menu items with images");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(400, 300);

        Button button = new Button(shell, SWT.PUSH);
        button.setText("Right-click me");

        Image image = squareImage(display);
        Menu menu = new Menu(button);
        button.setMenu(menu);

        item(menu, SWT.PUSH, "Push, image", image, false);
        item(menu, SWT.CHECK, "Check, image, unselected", image, false);
        item(menu, SWT.CHECK, "Check, image, selected", image, true);
        item(menu, SWT.CHECK, "Check, unselected", null, false);
        item(menu, SWT.CHECK, "Check, selected", null, true);
        item(menu, SWT.PUSH, "Push", null, false);

        shell.open();
        display.timerExec(1500, () -> {
            for (MenuItem mi : menu.getItems()) {
                System.out.println("item '" + mi.getText() + "' check="
                        + ((mi.getStyle() & SWT.CHECK) != 0) + " selection=" + mi.getSelection()
                        + " image=" + (mi.getImage() != null));
            }
            menu.setLocation(button.toDisplay(0, button.getSize().y));
            menu.setVisible(true);
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        image.dispose();
        display.dispose();
    }

    private static void item(Menu menu, int style, String text, Image image, boolean selected) {
        MenuItem item = new MenuItem(menu, style);
        item.setText(text);
        if (image != null) item.setImage(image);
        if (selected) item.setSelection(true);
    }

    private static Image squareImage(Display display) {
        PaletteData palette = new PaletteData(new RGB[] {new RGB(255, 255, 255), new RGB(30, 120, 220)});
        ImageData data = new ImageData(16, 16, 1, palette);
        for (int y = 2; y < 14; y++) {
            for (int x = 2; x < 14; x++) {
                data.setPixel(x, y, 1);
            }
        }
        data.transparentPixel = 0;
        return new Image(display, data);
    }
}

package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A collapsible-section header built the way applications build one: a CLabel carrying the
 * title text plus a small state icon, given LEFT alignment and no horizontal grab, so the
 * GridLayout hands it exactly its computeSize.
 *
 * That makes the header a strict test of the image budget: CLabelSizes reserves
 * imageData.width for the icon, so the icon must render within that width or the leftover is
 * taken out of the text, which then ellipsizes.
 */
public class CLabelImageBudgetSnippet {

    private static final int ICON_SIZE = 16;
    private static final String TITLE = "Query Parameters";

    public static void main(String[] args) {
        Config.useEquo(CLabel.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CLabel image budget");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(520, 260);

        FontData systemFont = display.getSystemFont().getFontData()[0];
        Font boldFont = new Font(display, systemFont.getName(), systemFont.getHeight(), SWT.BOLD);
        Image collapsedIcon = arrowIcon(display);

        Composite section = new Composite(shell, SWT.NONE);
        GridLayout sectionLayout = new GridLayout();
        sectionLayout.verticalSpacing = 0;
        sectionLayout.horizontalSpacing = 0;
        sectionLayout.marginWidth = 0;
        sectionLayout.marginHeight = 0;
        section.setLayout(sectionLayout);
        section.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        CLabel header = new CLabel(section, SWT.NONE);
        header.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
        header.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        header.setText(TITLE);
        header.setFont(boldFont);
        header.setImage(collapsedIcon);

        Label reference = new Label(shell, SWT.NONE);
        reference.setFont(boldFont);
        reference.setText("expected: " + TITLE);

        Point headerSize = header.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        System.out.println("[budget] header computeSize=" + headerSize.x + "x" + headerSize.y
                + " icon=" + collapsedIcon.getImageData().width + "x" + collapsedIcon.getImageData().height);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        collapsedIcon.dispose();
        boldFont.dispose();
        display.dispose();
    }

    /** A right-pointing arrow drawn into a plain ICON_SIZE bitmap, so no image file is needed. */
    private static Image arrowIcon(Display display) {
        PaletteData palette = new PaletteData(new RGB[] { new RGB(255, 255, 255), new RGB(90, 96, 110) });
        ImageData data = new ImageData(ICON_SIZE, ICON_SIZE, 1, palette);
        data.transparentPixel = 0;
        for (int x = 5; x <= 9; x++) {
            for (int y = 8 - (9 - x); y <= 8 + (9 - x); y++) {
                data.setPixel(x, y, 1);
            }
        }
        return new Image(display, data);
    }
}

package dev.equo;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates CLabel (Custom Label) widget with Equo Flutter rendering.
 * CLabel supports:
 * - Text and/or image display
 * - Custom alignment (LEFT, CENTER, RIGHT)
 * - Custom colors and fonts
 * - Margins (top, bottom, left, right)
 * - Tooltips
 */
public class CLabelSnippet {

    public static void main(String[] args) {
        // Enable Equo Flutter rendering for CLabel
        Config.useEquo(CLabel.class);
        ConfigFlags.use_swt_fonts(true);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CLabel Snippet");
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(500, 400);

        // CLabel with text only
        CLabel textLabel = new CLabel(shell, SWT.BORDER);
        textLabel.setText("Text Only CLabel");
        textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textLabel.setAlignment(SWT.CENTER);
        textLabel.setForeground(display.getSystemColor(SWT.COLOR_BLUE));

        // CLabel with text and image (if img.png exists)
        CLabel imageLabel = new CLabel(shell, SWT.BORDER);
        imageLabel.setText("Label with Image");
        try {
            Image image = new Image(display, CLabelSnippet.class.getClassLoader().getResourceAsStream("img.png"));
            imageLabel.setImage(image);
        } catch (Exception e) {
            // Image not available, continue with text only
        }
        imageLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // CLabel with margins
        CLabel marginLabel = new CLabel(shell, SWT.BORDER);
        marginLabel.setText("CLabel with Margins (10px)");
        marginLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        marginLabel.setTopMargin(10);
        marginLabel.setBottomMargin(10);
        marginLabel.setLeftMargin(10);
        marginLabel.setRightMargin(10);
        marginLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_GREEN));

        // CLabel with tooltip
        CLabel tooltipLabel = new CLabel(shell, SWT.BORDER);
        tooltipLabel.setText("Hover for Tooltip");
        tooltipLabel.setToolTipText("This is a CLabel tooltip!");
        tooltipLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // CLabel right aligned
        CLabel rightLabel = new CLabel(shell, SWT.BORDER);
        rightLabel.setText("Right Aligned");
        rightLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        rightLabel.setAlignment(SWT.RIGHT);
        rightLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
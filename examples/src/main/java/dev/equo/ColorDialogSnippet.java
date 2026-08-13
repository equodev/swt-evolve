package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * The shape Eclipse's {@code ColorSelector} uses on every "Colors and Fonts" preference page:
 * seed the dialog with the colour currently in effect, open it, and apply whatever comes back
 * (null meaning the user cancelled).
 */
public class ColorDialogSnippet {
    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ColorDialog");
        shell.setLayout(new GridLayout(2, false));

        Label swatch = new Label(shell, SWT.BORDER);
        swatch.setLayoutData(new GridData(80, 24));
        RGB[] current = { new RGB(0, 120, 215) };
        Color[] color = { new Color(display, current[0]) };
        swatch.setBackground(color[0]);

        Button edit = new Button(shell, SWT.PUSH);
        edit.setText("Edit...");
        edit.addListener(SWT.Selection, e -> {
            ColorDialog dialog = new ColorDialog(shell);
            dialog.setText("Color");
            dialog.setRGB(current[0]);
            RGB picked = dialog.open();
            System.out.println("ColorDialog.open() -> " + picked);
            if (picked == null) return;
            current[0] = picked;
            color[0].dispose();
            color[0] = new Color(display, picked);
            swatch.setBackground(color[0]);
        });

        shell.setSize(360, 140);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        color[0].dispose();
        display.dispose();
    }
}

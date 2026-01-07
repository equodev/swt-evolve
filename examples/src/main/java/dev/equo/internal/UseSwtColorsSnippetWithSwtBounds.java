package dev.equo.internal;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Demonstrates the use of SWT colors and fonts flags.
 * This example shows how to enable use_swt_colors and use_swt_fonts
 * to use SWT-provided background, foreground, and font values in Flutter.
 */
public class UseSwtColorsSnippetWithSwtBounds {

    public static void main (String [] args) {
        Config.useEquo(Button.class);

        System.setProperty("swt.use_swt_colors", "true");
        System.setProperty("swt.use_swt_fonts", "true");

        Display display = new Display ();
        Shell shell = new Shell (display);

        shell.setText("UseSwtColorsSnippet - Button");

        Button pushButton = new Button (shell, SWT.PUSH | SWT.CENTER);
        pushButton.setText ("Button with SWT Colors & Font");
        pushButton.setImage(new Image(display, UseSwtColorsSnippetWithSwtBounds.class.getResourceAsStream("/synced.png")));
        pushButton.setAlignment(SWT.CENTER);
        
        pushButton.setBounds(10, 10, 250, 500);
            
        Color bgColor = new Color(display, 255, 200, 200);
        pushButton.setBackground(bgColor);
        
        Color fgColor = new Color(display, 0, 0, 150);
        pushButton.setForeground(fgColor);
        
        FontData fontData = new FontData("Arial", 16, SWT.BOLD);
        Font customFont = new Font(display, fontData);
        pushButton.setFont(customFont);
        
        pushButton.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Button clicked")));
        
        shell.setSize(350, 250);
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }

        customFont.dispose();
        bgColor.dispose();
        fgColor.dispose();
        display.dispose ();
    }
}


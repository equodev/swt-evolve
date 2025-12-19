package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GCFontSnippet {
    public static void main(String[] args) {
        //Config.forceEquo();
        Config.useEquo(Canvas.class);
        //Config.useEquo(Font.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("GC Font Variety Snippet");
        shell.setSize(600, 800);

        FontData fd = new FontData("Arial", 12, SWT.NORMAL);
        // Create various fonts with different families, sizes and styles
        Font arial12Normal = new Font(display, fd);
        Font arial16Bold = new Font(display, "Arial", 16, SWT.BOLD);
        Font arial14Italic = new Font(display, "Arial", 14, SWT.ITALIC);
        Font arial18BoldItalic = new Font(display, new FontData("Arial", 18, SWT.BOLD | SWT.ITALIC));

        Font courierNew10Normal = new Font(display, "Courier New", 10, SWT.NORMAL);
        Font courierNew14Bold = new Font(display, "Courier New", 14, SWT.BOLD);
        Font courierNew16Italic = new Font(display, "Courier New", 16, SWT.ITALIC);

        Font timesNewRoman14Normal = new Font(display, "Times New Roman", 14, SWT.NORMAL);
        Font timesNewRoman18Bold = new Font(display, "Times New Roman", 18, SWT.BOLD);
        Font timesNewRoman16Italic = new Font(display, "Times New Roman", 16, SWT.ITALIC);
        Font timesNewRoman20BoldItalic = new Font(display, "Times New Roman", 20, SWT.BOLD | SWT.ITALIC);

        Font helvetica12Normal = new Font(display, "Helvetica", 12, SWT.NORMAL);
        Font helvetica15Bold = new Font(display, "Helvetica", 15, SWT.BOLD);
        Font helvetica13Italic = new Font(display, "Helvetica", 13, SWT.ITALIC);

        Font verdana11Normal = new Font(display, "Verdana", 11, SWT.NORMAL);
        Font verdana14Bold = new Font(display, "Verdana", 14, SWT.BOLD);
        Font verdana12BoldItalic = new Font(display, "Verdana", 12, SWT.BOLD | SWT.ITALIC);

        Font georgia13Normal = new Font(display, "Georgia", 13, SWT.NORMAL);
        Font georgia17Bold = new Font(display, "Georgia", 17, SWT.BOLD);
        Font georgia15Italic = new Font(display, "Georgia", 15, SWT.ITALIC);

        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(25, 25, 550, 750);
        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            int y = 20;
            int lineHeight = 30;

            // Default font
            gc.drawText("Default System Font", 10, y, 0);
            y += lineHeight;

            // Arial variations
            gc.setFont(arial12Normal);
            gc.drawText("Arial 12 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(arial16Bold);
            gc.drawText("Arial 16 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(arial14Italic);
            gc.drawText("Arial 14 Italic - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(arial18BoldItalic);
            gc.drawText("Arial 18 Bold+Italic - The quick brown fox", 10, y, 0);
            y += lineHeight + 5;

            // Courier New variations
            gc.setFont(courierNew10Normal);
            gc.drawText("Courier New 10 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(courierNew14Bold);
            gc.drawText("Courier New 14 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(courierNew16Italic);
            gc.drawText("Courier New 16 Italic - The quick brown fox", 10, y, 0);
            y += lineHeight + 5;

            // Times New Roman variations
            gc.setFont(timesNewRoman14Normal);
            gc.drawText("Times New Roman 14 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(timesNewRoman18Bold);
            gc.drawText("Times New Roman 18 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(timesNewRoman16Italic);
            gc.drawText("Times New Roman 16 Italic - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(timesNewRoman20BoldItalic);
            gc.drawText("Times New Roman 20 Bold+Italic - The quick brown fox", 10, y, 0);
            y += lineHeight + 5;

            // Helvetica variations
            gc.setFont(helvetica12Normal);
            gc.drawText("Helvetica 12 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(helvetica15Bold);
            gc.drawText("Helvetica 15 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(helvetica13Italic);
            gc.drawText("Helvetica 13 Italic - The quick brown fox", 10, y, 0);
            y += lineHeight + 5;

            // Verdana variations
            gc.setFont(verdana11Normal);
            gc.drawText("Verdana 11 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(verdana14Bold);
            gc.drawText("Verdana 14 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(verdana12BoldItalic);
            gc.drawText("Verdana 12 Bold+Italic - The quick brown fox", 10, y, 0);
            y += lineHeight + 5;

            // Georgia variations
            gc.setFont(georgia13Normal);
            gc.drawText("Georgia 13 Normal - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(georgia17Bold);
            gc.drawText("Georgia 17 Bold - The quick brown fox", 10, y, 0);
            y += lineHeight;

            gc.setFont(georgia15Italic);
            gc.drawText("Georgia 15 Italic - The quick brown fox", 10, y, 0);
            y += lineHeight;

            // Reset to default
            gc.setFont(null);
            gc.drawText("Back to Default System Font", 10, y, 0);
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        // Dispose fonts
        arial12Normal.dispose();
        arial16Bold.dispose();
        arial14Italic.dispose();
        arial18BoldItalic.dispose();
        courierNew10Normal.dispose();
        courierNew14Bold.dispose();
        courierNew16Italic.dispose();
        timesNewRoman14Normal.dispose();
        timesNewRoman18Bold.dispose();
        timesNewRoman16Italic.dispose();
        timesNewRoman20BoldItalic.dispose();
        helvetica12Normal.dispose();
        helvetica15Bold.dispose();
        helvetica13Italic.dispose();
        verdana11Normal.dispose();
        verdana14Bold.dispose();
        verdana12BoldItalic.dispose();
        georgia13Normal.dispose();
        georgia17Bold.dispose();
        georgia15Italic.dispose();

        display.dispose();
    }
}
package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates font styling in StyledText via StyleRange.font.
 * Each line uses a different font family, size, or style to show how
 * per-run font overrides interact with the widget's base font:
 * - Arial 14 normal
 * - Arial 16 bold
 * - Courier New 13 normal (monospace)
 * - Courier New 13 bold
 * - Times New Roman 15 italic
 * - Times New Roman 15 bold+italic
 * - Verdana 12 normal
 * - Helvetica 14 normal
 */
public class StyledTextSnippet8 {

    static final String TEXT =
            "Arial 14 — The quick brown fox jumps over the lazy dog\n" +
            "Arial 16 Bold — The quick brown fox jumps over the lazy dog\n" +
            "Courier New 13 — The quick brown fox jumps over the lazy dog\n" +
            "Courier New 13 Bold — The quick brown fox jumps over the lazy dog\n" +
            "Times New Roman 15 Italic — The quick brown fox jumps over the lazy dog\n" +
            "Times New Roman 15 Bold+Italic — The quick brown fox jumps over the lazy dog\n" +
            "Verdana 12 — The quick brown fox jumps over the lazy dog\n" +
            "Helvetica 14 — The quick brown fox jumps over the lazy dog";

    public static void main(String[] args) throws ClassNotFoundException {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("StyledText Snippet 8 — Font showcase");
        shell.setLayout(new FillLayout());
        shell.setSize(620, 340);

        StyledText styledText = new StyledText(shell, SWT.BORDER | SWT.V_SCROLL);
        styledText.setText(TEXT);

        Font arial14         = new Font(display, "Arial",            14, SWT.NORMAL);
        Font arial16Bold     = new Font(display, "Arial",            16, SWT.BOLD);
        Font courier13       = new Font(display, "Courier New",      13, SWT.NORMAL);
        Font courier13Bold   = new Font(display, "Courier New",      13, SWT.BOLD);
        Font times15Italic   = new Font(display, "Times New Roman",  15, SWT.ITALIC);
        Font times15BoldItal = new Font(display, "Times New Roman",  15, SWT.BOLD | SWT.ITALIC);
        Font verdana12       = new Font(display, "Verdana",          12, SWT.NORMAL);
        Font helvetica14     = new Font(display, "Helvetica",        14, SWT.NORMAL);

        String[] lines = TEXT.split("\n");
        int offset = 0;
        Font[] fonts = { arial14, arial16Bold, courier13, courier13Bold,
                         times15Italic, times15BoldItal, verdana12, helvetica14 };

        for (int i = 0; i < lines.length; i++) {
            StyleRange range = new StyleRange();
            range.start      = offset;
            range.length     = lines[i].length();
            range.font       = fonts[i];
            range.foreground = display.getSystemColor(SWT.COLOR_BLACK);
            styledText.setStyleRange(range);
            offset += lines[i].length() + 1; // +1 for '\n'
        }

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }

        arial14.dispose();
        arial16Bold.dispose();
        courier13.dispose();
        courier13Bold.dispose();
        times15Italic.dispose();
        times15BoldItal.dispose();
        verdana12.dispose();
        helvetica14.dispose();

        display.dispose();
    }
}
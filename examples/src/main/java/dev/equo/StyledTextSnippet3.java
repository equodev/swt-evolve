package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.intellij.lang.annotations.JdkConstants;

/**
 * Combines character-level styling with paragraph-level formatting in SWT StyledText.
 * This example demonstrates both types of formatting applied together:
 * Character-level styling:
 * - First paragraph: Bold font style applied to specific text range
 * - Second paragraph: Red foreground color applied to specific text range  
 * - Third paragraph: Blue background color applied to specific text range
 * Paragraph-level formatting:
 * - First paragraph: 50-pixel left indentation
 * - Second paragraph: Center alignment
 * - Third paragraph: Justified alignment
 */
public class StyledTextSnippet3 {
    static String text =
            "This paragraph has an indentation of fifty pixels and bold font style applied.\n\n" +
                    "This paragraph is center aligned and has red foreground color.\n\n" +
                    "This paragraph is justified and has blue background color.";

    public static void main(String [] args) {
        Config.useEclipse(Font.class);
        Config.useEclipse(FontData.class);
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("StyledText Snippet 3");
        shell.setLayout(new FillLayout());
        StyledText styledText = new StyledText(shell, SWT.WRAP | SWT.BORDER);
        styledText.setText(text);

        StyleRange style1 = new StyleRange();
        style1.start = 0;
        style1.length = 78;
        style1.fontStyle = SWT.BOLD;
        styledText.setStyleRange(style1);

        StyleRange style2 = new StyleRange();
        style2.start = 80;
        style2.length = 62;
        style2.foreground = display.getSystemColor(SWT.COLOR_RED);
        styledText.setStyleRange(style2);

        StyleRange style3 = new StyleRange();
        style3.start = 144;
        style3.length = 58;
        style3.background = display.getSystemColor(SWT.COLOR_BLUE);
        styledText.setStyleRange(style3);

        styledText.setLineIndent(0, 1, 50);
        styledText.setLineAlignment(2, 1, SWT.CENTER);
        styledText.setLineJustify(4, 1, true);

        shell.setSize(300, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}
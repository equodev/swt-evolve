package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates text paragraph formatting with SWT StyledText widget.
 * This example shows different paragraph-level formatting options:
 * - First paragraph: 50-pixel indentation from the left margin
 * - Second paragraph: Center alignment
 * - Third paragraph: Justified text alignment (text aligned to both margins)
 * The text content explains each formatting feature while demonstrating it visually.
 */
public class StyledTextSnippet2 {

	static String text =
		"The first paragraph has an indentation of fifty pixels. Indentation is the amount of white space in front of the first line of a paragraph.\n\n" +
		"The second paragraph is center aligned. Alignment, as with all other line attributes, can be set for the whole widget or just for a set of lines.\n\n" +
		"The third paragraph is justified, which means the text is aligned to both the left and right margins. This creates a uniform appearance by evenly distributing spaces between words.";

	public static void main(String [] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("StyledText Snippet 2");
		shell.setLayout(new FillLayout());
		StyledText styledText = new StyledText(shell, SWT.WRAP | SWT.BORDER);
		styledText.setText(text);
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

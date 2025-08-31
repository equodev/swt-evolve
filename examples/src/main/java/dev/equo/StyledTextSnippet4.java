package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates an editable StyledText widget with user interaction capabilities.
 * This example shows:
 * - A StyledText widget that allows user input and text editing
 * - Bold formatting applied to the entire initial text content
 * - Interactive text editing where users can click anywhere to position the caret
 * - Full editing capabilities including typing, deleting, and modifying text
 * The text content explains its own interactive features to guide user interaction.
 */
public class StyledTextSnippet4 {

public static void main(String[] args) {
	Display display = new Display();
	Shell shell = new Shell(display);
	shell.setText("StyledText Snippet 4");
	shell.setLayout(new FillLayout());
	StyledText text = new StyledText (shell, SWT.BORDER);
	text.setText("This text is editable. You may click at any part of it to change caret location and start modifying its content");
	StyleRange style1 = new StyleRange();
	style1.start = 0;
	style1.length = text.getCharCount();
	style1.fontStyle = SWT.BOLD;
	text.setStyleRange(style1);

	shell.setSize(500, 80);
	shell.open();
	while (!shell.isDisposed()) {
		if (!display.readAndDispatch())
			display.sleep();
	}
	display.dispose();
}
}
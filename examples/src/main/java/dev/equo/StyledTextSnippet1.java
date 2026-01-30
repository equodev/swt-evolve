package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates basic text styling with SWT StyledText widget.
 * This example shows how to apply different text styles to different portions of text:
 * - Bold formatting for the first 10 characters
 * - Red foreground color for the middle section
 * - Blue background color for the last section
 * Each style range is applied using StyleRange objects with specific start positions and lengths.
 */
public class StyledTextSnippet1 {

public static void main(String[] args) throws ClassNotFoundException {
	Config.useEquo(StyledText.class);
	Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));
	Config.useEquo(StyleRange.class);

	Display display = new Display();
	Shell shell = new Shell(display);
	shell.setText("StyledText Snippet 1");
	shell.setLayout(new FillLayout());
	shell.setSize(400, 100);
	StyledText text = new StyledText (shell, SWT.BORDER);
	text.setText("0123456789 ABCDEFGHIJKLM NOPQRSTUVWXYZ");
	StyleRange style1 = new StyleRange();
	style1.start = 0;
	style1.length = 10;
	style1.fontStyle = SWT.BOLD;
	text.setStyleRange(style1);
	StyleRange style2 = new StyleRange();
	style2.start = 11;
	style2.length = 13;
	style2.foreground = display.getSystemColor(SWT.COLOR_RED);
	text.setStyleRange(style2);
	StyleRange style3 = new StyleRange();
	style3.start = 25;
	style3.length = 13;
	style3.background = display.getSystemColor(SWT.COLOR_BLUE);
	text.setStyleRange(style3);

	shell.open();
	while (!shell.isDisposed()) {
		if (!display.readAndDispatch())
			display.sleep();
	}
	display.dispose();
}
}
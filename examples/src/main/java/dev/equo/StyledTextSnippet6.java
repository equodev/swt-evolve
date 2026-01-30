package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates StyledText with different fonts, colors, and styles applied to different portions of text.
 * This example shows how to apply:
 * - Different font sizes (8pt, 12pt, 18pt, 24pt)
 * - Different font families
 * - Combined font styles (bold, italic)
 * - Different foreground colors
 * - Different background colors
 */
public class StyledTextSnippet6 {

	public static void main(String[] args) throws ClassNotFoundException {
		Config.useEquo(StyledText.class);
		Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));
		Config.useEquo(StyleRange.class);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("StyledText Snippet 6 - Fonts, Colors & Styles");
		shell.setLayout(new FillLayout());
		shell.setSize(700, 250);

		StyledText text = new StyledText(shell, SWT.BORDER | SWT.WRAP);
		text.setText("Red 8pt - Blue Italic 12pt - Green Bold 18pt - Purple Bold Italic 24pt - Yellow on Black");

		// Colors
		Color red = new Color(display, 255, 0, 0);
		Color blue = new Color(display, 0, 0, 255);
		Color green = new Color(display, 0, 128, 0);
		Color purple = new Color(display, 128, 0, 128);
		Color yellow = new Color(display, 255, 255, 0);
		Color black = new Color(display, 0, 0, 0);

		// Text: "Red 8pt - Blue Italic 12pt - Green Bold 18pt - Purple Bold Italic 24pt - Yellow on Black"
		// Indices: 0-6 "Red 8pt", 10-25 "Blue Italic 12pt", 29-43 "Green Bold 18pt", 47-69 "Purple Bold Italic 24pt", 73-87 "Yellow on Black"

		// Style 1: Small red font (8pt)
		StyleRange style1 = new StyleRange();
		style1.start = 0;
		style1.length = 7; // "Red 8pt"
		style1.font = new Font(display, "Arial", 8, SWT.NORMAL);
		style1.foreground = red;
		text.setStyleRange(style1);

		// Style 2: Blue italic font (12pt)
		StyleRange style2 = new StyleRange();
		style2.start = 10;
		style2.length = 16; // "Blue Italic 12pt"
		style2.font = new Font(display, "Arial", 12, SWT.ITALIC);
		style2.foreground = blue;
		text.setStyleRange(style2);

		// Style 3: Green bold font (18pt)
		StyleRange style3 = new StyleRange();
		style3.start = 29;
		style3.length = 15; // "Green Bold 18pt"
		style3.font = new Font(display, "Arial", 18, SWT.BOLD);
		style3.foreground = green;
		text.setStyleRange(style3);

		// Style 4: Purple bold italic font (24pt)
		StyleRange style4 = new StyleRange();
		style4.start = 47;
		style4.length = 23; // "Purple Bold Italic 24pt"
		style4.font = new Font(display, "Arial", 24, SWT.BOLD | SWT.ITALIC);
		style4.foreground = purple;
		text.setStyleRange(style4);

		// Style 5: Yellow text on black background
		StyleRange style5 = new StyleRange();
		style5.start = 73;
		style5.length = 15; // "Yellow on Black"
		style5.font = new Font(display, "Arial", 14, SWT.BOLD);
		style5.foreground = yellow;
		style5.background = black;
		text.setStyleRange(style5);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}

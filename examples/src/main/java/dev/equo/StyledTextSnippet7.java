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
 * Reproduces the SwtFontData serialization issue:
 * Sets a font directly on the StyledText widget (like Eclipse editor does),
 * which triggers serialization of a SwtFont/SwtFontData.
 */
public class StyledTextSnippet7 {

	public static void main(String[] args) throws ClassNotFoundException {
		Config.useEquo(StyledText.class);
		Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));
		Config.useEquo(StyleRange.class);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("StyledText Snippet 7 - Font on Widget");
		shell.setLayout(new FillLayout());
		shell.setSize(700, 250);

		StyledText text = new StyledText(shell, SWT.BORDER | SWT.WRAP);
		text.setText("This text has a font set directly on the StyledText widget.");

		// Set font directly on the widget (like Eclipse editor does)
		text.setFont(new Font(display, "Consolas", 14, SWT.NORMAL));

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
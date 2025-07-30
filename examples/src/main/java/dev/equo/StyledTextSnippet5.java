package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StyledTextSnippet5 {

public static void main(String[] args) {
	Display display = new Display();
	Shell shell = new Shell(display);
	shell.setText("StyledText Snippet 5");
	shell.setLayout(new FillLayout());
	StyledText text = new StyledText (shell, SWT.BORDER);
	text.setText("This text is not editable, which means you can't modify its content");
	StyleRange style1 = new StyleRange();
	style1.start = 0;
	style1.length = text.getCharCount();
	style1.fontStyle = SWT.BOLD;
	text.setStyleRange(style1);
	text.setEditable(false);

	shell.pack();
	shell.open();
	while (!shell.isDisposed()) {
		if (!display.readAndDispatch())
			display.sleep();
	}
	display.dispose();
}
}
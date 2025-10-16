package dev.equo;

import static org.eclipse.swt.events.SelectionListener.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates the use of the Link widget with custom link colors.
 * <p>
 * This example creates a Link widget with a simple hyperlink and provides
 * buttons to customize the link foreground color. It shows how to:
 * </p>
 * <ul>
 * <li>Create a Link widget with HTML-like anchor tags</li>
 * <li>Set a custom link foreground color using setLinkForeground()</li>
 * <li>Reset the link color to the system default by passing null</li>
 * <li>Use ColorDialog to let users choose colors interactively</li>
 * </ul>
 */

public class LinkSnippet {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("LinkSnippet");
		shell.setLayout(new RowLayout());

		Link link = new Link(shell, SWT.NONE);
		link.setText("This a very simple link widget <a>https://www.equo.dev/</a> ");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.text != null && !e.text.isEmpty()) {
					try {
						Program.launch(e.text);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		Button setButton = new Button(shell, SWT.PUSH);
		setButton.setText("Choose link color");
		setButton.addSelectionListener(widgetSelectedAdapter(e -> {
			System.out.println("default link color " + link.getLinkForeground());
			ColorDialog colorDialog = new ColorDialog(shell);
			RGB color = colorDialog.open();
			link.setLinkForeground(new Color(color));
			System.out.println("user selected link color " + link.getLinkForeground());
		}));

		Button resetButton = new Button(shell, SWT.PUSH);
		resetButton.setText("Reset link color");
		resetButton.addSelectionListener(widgetSelectedAdapter(e -> {
			System.out.println("link color reset to system default");
			link.setLinkForeground(null);
		}));

		shell.pack ();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
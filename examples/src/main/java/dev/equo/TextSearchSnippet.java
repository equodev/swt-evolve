package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates Search Text widget
 * This example showcases:
 * - Search field with SWT.SEARCH style
 * - Search icon prefix
 * - Cancel/clear button suffix
 * - DefaultSelection event (when Enter is pressed)
 */
public class TextSearchSnippet {
    public static void main(String[] args) {
        Config.useEquo(Text.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TextSearchSnippet");
        shell.setLayout(new GridLayout(1, false));

        // Search field with icon
        Label label1 = new Label(shell, SWT.NONE);
        label1.setText("Search field:");

        Text searchText = new Text(shell, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        searchText.setMessage("Search...");
        searchText.addListener(SWT.Modify, e -> System.out.println("Search text changed: " + searchText.getText()));
        searchText.addListener(SWT.DefaultSelection, e -> {
            Event event = (Event) e;
            if (event.detail == SWT.ICON_CANCEL) {
                System.out.println("Cancel button clicked");
            } else {
                System.out.println("Search submitted: " + searchText.getText());
            }
        });

        shell.pack();
        shell.setSize(400, 150);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}

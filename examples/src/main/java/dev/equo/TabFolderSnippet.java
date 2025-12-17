package dev.equo;
import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Demonstrates the use of TabFolder widget in SWT.
 * This example creates a tab folder with 6 tabs, each containing a text area with sample content.
 * The tab folder includes:
 * - Standard SWT tab navigation
 * - Selection events to track tab changes
 * - Multi-line text content in each tab with scrollbars
 * - Proper layout management
 */
public class TabFolderSnippet {
    public static void main(String[] args) {
        Config.useEquo(TabFolder.class);
        Config.useEquo(TabItem.class);
        Display display = new Display();

        final Shell shell = new Shell(display);
        shell.setText("TabFolderSnippet");
        shell.setLayout(new GridLayout());

        // Create a TabFolder with border style
        final TabFolder folder = new TabFolder(shell, SWT.BORDER | SWT.TOP);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create tabs
        for (int i = 0; i < 6; i++) {
            TabItem item = new TabItem(folder, SWT.NONE);
            item.setText("Tab " + i);
            item.setToolTipText("This is tab number " + i);

            // Create content for each tab
            Text text = new Text(folder, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            text.setText("Content for Tab " + i + "\n\n" +
                    "This is a multi-line text area.\n" +
                    "You can add any content here.\n\n" +
                    "Line 1\nLine 2\nLine 3\n\n" +
                    "The TabFolder widget provides a simple\n" +
                    "way to organize multiple pages of content.");
            item.setControl(text);
        }

        // Set initial selection
        folder.setSelection(0);

        // Add selection listener to track tab changes
        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println("Tab selected: " + folder.getSelectionIndex());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println("Tab double-clicked: " + folder.getSelectionIndex());
            }
        });

        shell.setSize(500, 350);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

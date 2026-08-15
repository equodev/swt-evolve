package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * Faithful stand-in for EGit's Git &rarr; Label Decorations preference page: a
 * {@link TabFolder} whose {@link SelectionListener} swaps a <em>dependent</em> "Preview:" area below
 * it. The two preview panels ({@code navigatorPreview}, {@code changeSetPreview}) are sibling
 * composites in a {@link GridLayout} parent; switching tabs hides one and shows the other using the
 * exact mechanism EGit's {@code Preview.show()/hide()} uses — toggle {@code GridData.exclude} and
 * {@code Composite.setVisible(...)}, then {@code layout()}.
 *
 * <p>The reported bug: on the Flutter (web) backend the tab body switched but the preview never did,
 * because the Flutter tab click did not fire {@code SWT.Selection}, so this listener never ran. With
 * the fix the listener runs and the visible preview follows the selected tab.
 *
 * <p>Drive it on the web example and read the two panels' live {@code visible} state by id:
 * <pre>flutter-mcp launch TabFolderPreviewSwapSnippet ; flutter-mcp state</pre>
 */
public class TabFolderPreviewSwapSnippet {

    /** One preview panel: a composite that can be shown/hidden exactly like EGit's Preview. */
    static final class Preview {
        final Composite composite;

        Preview(Composite parent, String caption) {
            composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout());
            composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            Label label = new Label(composite, SWT.NONE);
            label.setText(caption);
        }

        void show() {
            ((GridData) composite.getLayoutData()).exclude = false;
            composite.setVisible(true);
            composite.getParent().layout();
        }

        void hide() {
            ((GridData) composite.getLayoutData()).exclude = true;
            composite.setVisible(false);
            composite.getParent().layout();
        }
    }

    public static void main(String[] args) {
        Config.useEquo(TabFolder.class);
        Config.useEquo(TabItem.class);
        Display display = new Display();

        Shell shell = new Shell(display);
        shell.setText("TabFolderPreviewSwapSnippet");
        shell.setLayout(new GridLayout());

        TabFolder folder = new TabFolder(shell, SWT.TOP);
        folder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        String[] tabs = { "General", "Other" };
        for (String name : tabs) {
            TabItem item = new TabItem(folder, SWT.NONE);
            item.setText(name);
            Text body = new Text(folder, SWT.MULTI | SWT.BORDER);
            body.setText(name + " tab content");
            item.setControl(body);
        }
        folder.setSelection(0);

        Label previewCaption = new Label(shell, SWT.NONE);
        previewCaption.setText("Preview:");

        // The dependent area: both panels stacked in a GridLayout parent; only one is shown at a time.
        Composite previewArea = new Composite(shell, SWT.BORDER);
        previewArea.setLayout(new GridLayout());
        previewArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Preview navigatorPreview = new Preview(previewArea, "resources tree (Project / folder / tracked.txt)");
        Preview changeSetPreview = new Preview(previewArea, "commits preview ([Author] (date) message)");

        // Initial state mirrors the General tab: navigator shown, change set hidden.
        navigatorPreview.show();
        changeSetPreview.hide();

        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean other = folder.getSelectionIndex() == 1;
                System.out.println("Tab selected: " + folder.getSelectionIndex()
                        + " -> showing " + (other ? "changeSetPreview" : "navigatorPreview"));
                if (other) {
                    navigatorPreview.hide();
                    changeSetPreview.show();
                } else {
                    changeSetPreview.hide();
                    navigatorPreview.show();
                }
            }
        });

        shell.setSize(560, 380);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}

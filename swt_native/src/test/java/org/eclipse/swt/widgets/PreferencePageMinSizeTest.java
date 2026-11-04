package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.lang.reflect.Method;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code PreferencePage.computeSize()} caches the first size it measures, and
 * {@code PreferenceDialog.showPage} hands that size to the page's ScrolledComposite as its minimum.
 * Building a page must therefore not run a layout of the dialog before the page is complete: a size
 * cached half-way through stays the minimum, so the page gets no scroll bar and whatever was added
 * afterwards (here the rows of a table) is squeezed or cut off at the bottom.
 */
class PreferencePageMinSizeTest {

    private static final String[] COLOR_ENTRIES = {"Line number foreground", "Current line highlight",
            "Print margin", "Find scope", "Selection foreground color", "Selection background color",
            "Background color", "Foreground color", "Hyperlink"};

    private Display display;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private static final class SmallPage extends PreferencePage {
        @Override
        protected Control createContents(Composite parent) {
            Composite c = new Composite(parent, SWT.NONE);
            c.setLayout(new GridLayout());
            new Label(c, SWT.NONE).setText("Small page");
            return c;
        }
    }

    /** A long page shaped like a text-editor settings page: rows of options, then a color list. */
    private static final class LongPage extends PreferencePage {
        TableViewer viewer;

        @Override
        protected Control createContents(Composite parent) {
            Composite page = new Composite(parent, SWT.NONE);
            page.setLayout(new GridLayout(2, false));
            for (int i = 0; i < 20; i++) {
                Button check = new Button(page, SWT.CHECK);
                check.setText("Option " + i);
                check.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            }
            Label colors = new Label(page, SWT.LEFT);
            colors.setText("Appearance color options:");
            colors.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

            Composite editor = new Composite(page, SWT.NONE);
            GridLayout editorLayout = new GridLayout(2, false);
            editorLayout.marginHeight = 0;
            editorLayout.marginWidth = 0;
            editor.setLayout(editorLayout);
            GridData editorData = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL
                    | GridData.HORIZONTAL_ALIGN_FILL);
            editorData.horizontalSpan = 2;
            editor.setLayoutData(editorData);

            Composite tableHolder = new Composite(editor, SWT.NONE);
            tableHolder.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
            viewer = new TableViewer(tableHolder, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
            TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE, 0);
            TableColumnLayout columns = new TableColumnLayout(true);
            columns.setColumnData(column, new ColumnWeightData(1, 200));
            tableHolder.setLayout(columns);

            Composite styles = new Composite(editor, SWT.NONE);
            styles.setLayout(new GridLayout(2, false));
            new Label(styles, SWT.LEFT).setText("Color:");
            // Draws its swatch on an image and reads the pixels back while the page is being built.
            new ColorSelector(styles);

            Link link = new Link(page, SWT.NONE);
            link.setText("More colors can be configured on the <a>Colors and Fonts</a> preference page.");
            link.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

            viewer.setContentProvider(ArrayContentProvider.getInstance());
            viewer.setLabelProvider(new LabelProvider());
            viewer.setInput(COLOR_ENTRIES);
            return page;
        }
    }

    @Test
    void pageShownAfterAnotherScrollsToItsCompleteContent() throws Exception {
        PreferenceManager manager = new PreferenceManager();
        manager.addToRoot(new PreferenceNode("small", new SmallPage()));
        LongPage longPage = new LongPage();
        PreferenceNode longNode = new PreferenceNode("long", longPage);
        manager.addToRoot(longNode);
        PreferenceDialog dialog = new PreferenceDialog(null, manager);
        dialog.setBlockOnOpen(false);
        dialog.setSelectedNode("small");
        dialog.open();
        try {
            Method showPage = PreferenceDialog.class.getDeclaredMethod("showPage", IPreferenceNode.class);
            showPage.setAccessible(true);
            showPage.invoke(dialog, longNode);

            Control content = longPage.getControl();
            ScrolledComposite scrolled = null;
            for (Composite p = content.getParent(); scrolled == null && p != null; p = p.getParent()) {
                if (p instanceof ScrolledComposite sc) scrolled = sc;
            }
            assertThat(scrolled).isNotNull();
            int completeHeight = content.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
            assertThat(scrolled.getMinHeight())
                    .as("the page's scroll minimum covers its complete content")
                    .isGreaterThanOrEqualTo(completeHeight);
            assertThat(longPage.viewer.getTable().getSize().y)
                    .as("the color list keeps its rows")
                    .isGreaterThan(0);
        } finally {
            dialog.close();
        }
    }
}

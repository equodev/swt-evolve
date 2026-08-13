package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A TabFolder renders as a tab strip stacked above its content, so the selected tab's control only
 * ever gets {@code height - stripHeight} of the folder. Java has to account for that strip in both
 * directions: {@code getClientArea()} must hand the content only the area below the strip, and
 * {@code computeSize()} must ask for the content's preferred height *plus* the strip.
 *
 * <p>Without it, a folder inside a height-preferred parent is laid out exactly as tall as its
 * content, the content is then laid out over the folder's full height, and the renderer clips the
 * bottom {@code stripHeight} pixels — losing the last row of a multi-row page.
 */
@Tag("flutter-it")
class TabFolderTabStripSizeFlutterTest {

    private static final String[] ROW_LABELS = { "Files:", "Folders:", "Projects:", "Submodules:" };

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
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    /**
     * The shape of a decoration-format preference tab: a folder whose parent only fills
     * horizontally, so the folder's height comes from {@code computeSize()}, holding a 3-column grid
     * of label / text / button rows.
     */
    private static final class Page {
        Shell shell;
        TabFolder folder;
        Composite content;
        Button lastRowButton;
    }

    private Page buildPage() {
        Page page = new Page();
        page.shell = new Shell(display);
        page.shell.setLayout(new GridLayout());

        Composite folderParent = new Composite(page.shell, SWT.NONE);
        folderParent.setLayout(new GridLayout());
        folderParent.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        page.folder = new TabFolder(folderParent, SWT.NONE);
        page.folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        page.content = new Composite(page.folder, SWT.NONE);
        page.content.setLayout(new GridLayout(3, false));
        for (String label : ROW_LABELS) {
            new Label(page.content, SWT.NONE).setText(label);
            Text text = new Text(page.content, SWT.BORDER);
            text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            page.lastRowButton = new Button(page.content, SWT.PUSH);
            page.lastRowButton.setText("Add Variables...");
        }

        TabItem item = new TabItem(page.folder, SWT.NONE);
        item.setText("Text Decorations");
        item.setControl(page.content);

        page.shell.pack();
        page.shell.layout(true, true);
        return page;
    }

    @Test
    void theTabStripIsExcludedFromTheClientArea() {
        Page page = buildPage();

        Rectangle bounds = page.folder.getBounds();
        Rectangle client = page.folder.getClientArea();

        assertThat(client.height)
                .as("the tab strip sits above the content, so the client area must be shorter than "
                        + "the folder by exactly the strip height")
                .isEqualTo(bounds.height - Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT);
        assertThat(client.y)
                .as("the content starts below the tab strip")
                .isEqualTo(Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT);
    }

    @Test
    void computeSizeAddsTheTabStripToTheContentHeight() {
        Page page = buildPage();

        Point contentSize = page.content.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        Point folderSize = page.folder.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

        assertThat(folderSize.y)
                .as("a folder must ask for room for its content AND its tab strip")
                .isEqualTo(contentSize.y + Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT);
    }

    @Test
    void theLastRowOfTheTabContentIsFullyVisible() {
        Page page = buildPage();

        int paintedContentHeight =
                page.folder.getBounds().height - Sizes.TAB_FOLDER_TAB_STRIP_HEIGHT;
        Rectangle lastRow = page.lastRowButton.getBounds();

        assertThat(lastRow.y + lastRow.height)
                .as("the last row (%s) must fit inside the area left below the tab strip, "
                        + "otherwise the renderer clips it", ROW_LABELS[ROW_LABELS.length - 1])
                .isLessThanOrEqualTo(paintedContentHeight);
    }
}

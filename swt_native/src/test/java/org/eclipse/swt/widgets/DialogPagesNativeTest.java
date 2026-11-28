package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A page (a wizard, the preferences) is placed over the workbench's editor area, follows it, and
 * blocks nothing outside itself; a dialog it opens blocks only the page.
 */
@Tag("native-unit")
class DialogPagesNativeTest {

    private static final int DIALOG = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE;

    /** What the e4 workbench stores in the editor area's composite, and the placeholder it is shown through. */
    public static final class EditorArea {
        final Placeholder placeholder = new Placeholder();

        public String getElementId() {
            return "org.eclipse.ui.editorss";
        }

        public Object getCurSharedRef() {
            return placeholder;
        }
    }

    /** The perspective's placeholder of the editor area: e4 maximizes the area through its tags. */
    public static final class Placeholder {
        final java.util.List<String> tags = new java.util.ArrayList<>();

        public java.util.List<String> getTags() {
            return tags;
        }
    }

    /** An editor area e4 shows through no placeholder: there is nothing to maximize it through. */
    public static final class BareEditorArea {
        public String getElementId() {
            return "org.eclipse.ui.editorss";
        }
    }

    /** An editor area shown directly rather than through a placeholder, carrying the tags itself. */
    public static final class DirectEditorArea {
        java.util.List<String> tags = new java.util.ArrayList<>();

        public String getElementId() {
            return "org.eclipse.ui.editorss";
        }

        public Object getCurSharedRef() {
            return null;
        }

        public java.util.List<String> getTags() {
            return tags;
        }
    }

    /** An e4 element with no id. */
    public static final class Unnamed {
        public String getElementId() {
            return null;
        }
    }

    private EditorArea model;
    private RecordingBridge bridge;

    private Display display;
    private Shell main;
    private Composite editorArea;
    private Button inWorkbench;
    private boolean savedFlag;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        when(((DartDisplay) display.getImpl()).isValidThread()).thenReturn(true);
        when(display.getShells()).thenReturn(new Shell[0]);
        // The mocked display delivers no events and keeps no modal shells; these tests need both.
        DartDisplay impl = (DartDisplay) display.getImpl();
        impl.modalShells = null;
        when(impl.getShells()).thenReturn(new Shell[0]);
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doCallRealMethod().when(impl).setModalShell(any(Shell.class));
        doCallRealMethod().when(impl).clearModal(any(Shell.class));
        savedFlag = Config.getConfigFlags().modern_dialogs;
        Config.getConfigFlags().modern_dialogs = true;
        main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 0, 2000, 1000);
        Composite views = new Composite(main, SWT.NONE);
        views.setBounds(0, 0, 300, 1000);
        inWorkbench = new Button(views, SWT.PUSH);
        editorArea = new Composite(main, SWT.NONE);
        model = new EditorArea();
        editorArea.setData("modelElement", model);
        editorArea.setBounds(300, 40, 1500, 900);
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        doNothing().when(impl).setModalShell(any(Shell.class));
        doNothing().when(impl).clearModal(any(Shell.class));
        doNothing().when(display).asyncExec(any());
        impl.modalShells = null;
        Config.getConfigFlags().modern_dialogs = savedFlag;
        FlutterBridge.set(null);
    }

    private Shell page() {
        Shell page = new Shell(main, DIALOG);
        page.setData(DialogPresentation.DATA_KEY, "page");
        page.setSize(500, 400);
        return page;
    }

    private Rectangle area() {
        Point at = editorArea.toDisplay(0, 0);
        Point size = editorArea.getSize();
        return new Rectangle(at.x, at.y, size.x, size.y);
    }

    @Test
    @DisplayName("a page opens over the editor area, below its tab strip, no wider than a readable column")
    void placedOverTheEditorArea() {
        Shell page = page();
        page.open();
        Rectangle area = area();
        Rectangle b = page.getBounds();
        assertThat(b.width).isEqualTo(DialogPages.MAX_WIDTH);
        assertThat(b.x).isEqualTo(area.x + (area.width - DialogPages.MAX_WIDTH) / 2);
        assertThat(b.y).isEqualTo(area.y + DialogPages.TAB_STRIP);
        assertThat(b.y + b.height).isEqualTo(area.y + area.height);
        assertThat(DialogPresentation.pageHost(page)).isEqualTo(area);
        assertThat(((DartShell) page.getImpl()).getValue().getPageStrip())
                .as("described again once placed, though it was first described as no page").isEqualTo(DialogPages.TAB_STRIP);
    }

    @Test
    @DisplayName("a narrow editor area gives the page its whole width")
    void narrowArea() {
        editorArea.setBounds(300, 40, 700, 900);
        Shell page = page();
        page.open();
        assertThat(page.getBounds().width).isEqualTo(700);
        assertThat(page.getBounds().x).isEqualTo(area().x);
    }

    @Test
    @DisplayName("the page follows the editor area, and the dialog's own resizing does not move it off")
    void follows() {
        Shell page = page();
        page.open();
        editorArea.setBounds(300, 40, 1200, 600);
        assertThat(page.getBounds().y + page.getBounds().height).isEqualTo(area().y + 600);
        assertThat(DialogPresentation.pageHost(page)).isEqualTo(area());
        page.setSize(640, 480);
        assertThat(page.getBounds().width).isEqualTo(DialogPages.MAX_WIDTH);
        assertThat(page.getBounds().height).isEqualTo(600 - DialogPages.TAB_STRIP);
    }

    @Test
    @DisplayName("a minimum size the dialog sets does not push the page past the editor area")
    void minimumSizeIgnored() {
        Shell page = page();
        page.open();
        page.setMinimumSize(800, 2000);
        Rectangle b = page.getBounds();
        assertThat(b.y + b.height).isEqualTo(area().y + area().height);
    }

    /** A page whose content asks for {@code height} pixels. */
    private Shell pageNeeding(int height) {
        Shell page = page();
        page.setLayout(new org.eclipse.swt.layout.GridLayout());
        Composite content = new Composite(page, SWT.NONE);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData();
        data.heightHint = height;
        data.widthHint = 400;
        content.setLayoutData(data);
        return page;
    }

    @Test
    @DisplayName("a page whose content does not fit the editor area maximizes it, and closing the page restores it")
    void maximizesWhenItDoesNotFit() {
        Shell page = pageNeeding(2000);
        page.open();
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED);
        assertThat(DialogPages.isAreaMaximized(page)).isTrue();
        page.dispose();
        assertThat(model.placeholder.tags).doesNotContain(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("an editor area another maximized stack minimized still takes the page: it is maximized, and restored with the page")
    void hiddenEditorArea() {
        editorArea.setVisible(false);
        model.placeholder.tags.add(DialogPages.MINIMIZED);
        Shell page = pageNeeding(300);
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isNotNull();
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED).doesNotContain(DialogPages.MINIMIZED);
        page.dispose();
        assertThat(model.placeholder.tags).doesNotContain(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("an editor area e4 moved to its hidden shell, while another stack is maximized, still takes the page")
    void editorAreaInLimbo() {
        editorArea.setData("modelElement", null);
        Shell limbo = new Shell(display, SWT.NONE);
        limbo.setVisible(false);
        Composite area = new Composite(limbo, SWT.NONE);
        area.setBounds(0, 0, 1500, 900);
        area.setData("modelElement", model);
        model.placeholder.tags.addAll(java.util.List.of(DialogPages.MINIMIZED, DialogPages.MINIMIZED_BY_ZOOM));
        when(display.getShells()).thenReturn(new Shell[] { main, limbo });
        Shell page = pageNeeding(300);
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isNotNull();
        assertThat(model.placeholder.tags).containsExactly(DialogPages.MAXIMIZED);
        page.dispose();
        assertThat(model.placeholder.tags).isEmpty();
    }

    @Test
    @DisplayName("a composite whose model is not an e4 element is not taken for the editor area")
    void otherModelsAreNotTheEditorArea() {
        editorArea.setData("modelElement", new Object());
        Shell page = page();
        page.open();
        assertThat(DialogPresentation.pageHost(page)).as("no editor area: a large sheet").isNull();
    }

    @Test
    @DisplayName("a page wider than the editor area maximizes it, though its height fits")
    void maximizesWhenTooWide() {
        editorArea.setBounds(300, 40, 700, 900);
        Shell page = page();
        page.setLayout(new org.eclipse.swt.layout.GridLayout());
        Composite content = new Composite(page, SWT.NONE);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData();
        data.heightHint = 100;
        data.widthHint = 900;
        content.setLayoutData(data);
        page.open();
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("an editor area shown through no placeholder takes the page, and is never maximized")
    void areaWithoutPlaceholder() {
        editorArea.setData("modelElement", new BareEditorArea());
        Shell page = pageNeeding(2000);
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isEqualTo(area());
        assertThat(DialogPages.isAreaMaximized(page)).isFalse();
        DialogPages.toggleMaximized(page);
        assertThat(DialogPages.isAreaMaximized(page)).isFalse();
    }

    @Test
    @DisplayName("an editor area shown directly is maximized through its own tags, and one with no tags is left alone")
    void areaShownDirectly() {
        DirectEditorArea direct = new DirectEditorArea();
        editorArea.setData("modelElement", direct);
        Shell page = pageNeeding(2000);
        page.open();
        assertThat(direct.tags).contains(DialogPages.MAXIMIZED);
        page.dispose();
        assertThat(direct.tags).doesNotContain(DialogPages.MAXIMIZED);

        direct.tags = null;
        Shell untagged = pageNeeding(2000);
        untagged.open();
        assertThat(DialogPresentation.pageHost(untagged)).isNotNull();
        assertThat(DialogPages.isAreaMaximized(untagged)).isFalse();
    }

    @Test
    @DisplayName("an editor area e4 lets go of under an open page is no longer maximized through it")
    void areaLetGo() {
        Shell page = pageNeeding(300);
        page.open();
        editorArea.setData("modelElement", null);
        DialogPages.toggleMaximized(page);
        assertThat(DialogPages.isAreaMaximized(page)).isFalse();
        assertThat(model.placeholder.tags).isEmpty();
    }

    @Test
    @DisplayName("a hidden editor area with no placeholder still takes the page, and a dialog's shell is not searched for one")
    void hiddenAreaWithoutPlaceholder() {
        editorArea.setVisible(false);
        editorArea.setData("modelElement", new BareEditorArea());
        Shell sheet = new Shell(main, DIALOG);
        Composite inSheet = new Composite(sheet, SWT.NONE);
        inSheet.setData("modelElement", model);
        when(display.getShells()).thenReturn(new Shell[] { main, sheet });
        Shell page = pageNeeding(300);
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isEqualTo(area());
        assertThat(model.placeholder.tags).as("the sheet's composite is not the editor area").isEmpty();
    }

    @Test
    @DisplayName("an element with no id is not taken for the editor area")
    void unnamedElements() {
        Composite unnamed = new Composite(main, SWT.NONE);
        unnamed.setData("modelElement", new Unnamed());
        unnamed.moveAbove(null);
        Shell page = page();
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isEqualTo(area());
    }

    @Test
    @DisplayName("a page whose editor area is disposed stops following it, and closes without touching it")
    void areaDisposedUnderThePage() {
        Shell page = pageNeeding(2000);
        page.open();
        Rectangle placed = page.getBounds();
        editorArea.dispose();
        assertThat(DialogPresentation.pageHost(page)).isNull();
        main.setBounds(0, 0, 1800, 900);
        assertThat(page.getBounds()).isEqualTo(placed);
        DialogPages.toggleMaximized(page);
        page.dispose();
        assertThat(model.placeholder.tags).as("left as the page found it").contains(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("a page measures its height again when the editor area gets narrower")
    void narrowerAreaRemeasures() {
        Shell page = pageNeeding(300);
        page.open();
        editorArea.setBounds(300, 40, 700, 900);
        assertThat(page.getBounds().width).isEqualTo(700);
        assertThat(page.getBounds().y + page.getBounds().height).isEqualTo(area().y + area().height);
    }

    @Test
    @DisplayName("the strip's maximize button maximizes the editor area, and is ignored once the page is gone")
    @SuppressWarnings("unchecked")
    void maximizeFromTheStrip() {
        java.util.List<Runnable> queued = new java.util.ArrayList<>();
        org.mockito.Mockito.doAnswer(i -> queued.add(i.getArgument(0))).when(display).asyncExec(any());
        Shell page = pageNeeding(300);
        page.open();
        java.util.function.Consumer<org.eclipse.swt.widgets.Event> maximize = (java.util.function.Consumer<org.eclipse.swt.widgets.Event>)
                bridge.comm.handlers.get(FlutterBridge.event((DartWidget) page.getImpl(), "DialogFrame", "maximize"));
        maximize.accept(new org.eclipse.swt.widgets.Event());
        queued.forEach(Runnable::run);
        queued.clear();
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED);

        maximize.accept(new org.eclipse.swt.widgets.Event());
        page.dispose();
        queued.forEach(Runnable::run);
        assertThat(model.placeholder.tags).as("the user's choice outlives the page").contains(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("the modal list's empty slots, closed shells and pages block nothing")
    void modalListSkipsWhatBlocksNothing() {
        Shell page = page();
        page.open();
        Shell closed = new Shell(main, DIALOG);
        closed.dispose();
        Shell sheet = new Shell(main, DIALOG);
        assertThat(DialogPresentation.modalShell(inWorkbench.getShell(), new Shell[] { sheet, closed, page, null })).isSameAs(sheet);
        assertThat(DialogPresentation.modalShell(inWorkbench.getShell(), new Shell[] { closed, page, null })).isNull();
    }

    @Test
    @DisplayName("a page that fits leaves the layout as it is")
    void fitsWithoutMaximizing() {
        Shell page = pageNeeding(300);
        page.open();
        assertThat(model.placeholder.tags).doesNotContain(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("the strip's toggle maximizes and restores, and the user's choice outlives the page")
    void toggle() {
        Shell page = pageNeeding(300);
        page.open();
        DialogPages.toggleMaximized(page);
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED);
        page.dispose();
        assertThat(model.placeholder.tags).as("maximized by the user, so left maximized").contains(DialogPages.MAXIMIZED);

        model.placeholder.tags.clear();
        Shell tall = pageNeeding(2000);
        tall.open();
        DialogPages.toggleMaximized(tall);
        assertThat(model.placeholder.tags).doesNotContain(DialogPages.MAXIMIZED);
        tall.dispose();
        assertThat(model.placeholder.tags).as("restored by the user, so not touched again").doesNotContain(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("of two pages that maximized the area, it is restored when the last one closes")
    void lastPageRestores() {
        Shell first = pageNeeding(2000);
        first.open();
        Shell second = pageNeeding(2000);
        second.open();
        first.dispose();
        assertThat(model.placeholder.tags).contains(DialogPages.MAXIMIZED);
        second.dispose();
        assertThat(model.placeholder.tags).doesNotContain(DialogPages.MAXIMIZED);
    }

    @Test
    @DisplayName("a page taller than the editor area is laid out at its height and scrolls by moving up inside the area")
    void tallPageScrolls() {
        Shell page = pageNeeding(2000);
        page.open();
        Rectangle area = area();
        int top = area.y + DialogPages.TAB_STRIP;
        int visible = area.height - DialogPages.TAB_STRIP;
        Rectangle b = page.getBounds();
        assertThat(DialogPresentation.drawnInParent(page, main)).as("taller than the window, still drawn in it").isTrue();
        assertThat(b.y).as("opens unscrolled").isEqualTo(top);
        assertThat(b.height).as("as tall as its content").isGreaterThan(visible);

        page.setLocation(b.x, top - 300);
        assertThat(page.getBounds().y).as("the client's scroll is kept").isEqualTo(top - 300);
        page.setLocation(b.x, top - 99_999);
        assertThat(page.getBounds().y).as("no further than its end").isEqualTo(top - (b.height - visible));
        page.setLocation(b.x, top + 50);
        assertThat(page.getBounds().y).as("never below the strip").isEqualTo(top);
    }

    @Test
    @DisplayName("the Preferences category column widens to fit its labels, no wider than its cap")
    void navigationColumnFitsItsLabels() {
        Composite area = new Composite(main, SWT.NONE);
        area.setLayout(new org.eclipse.swt.layout.GridLayout(2, false));
        Tree tree = new Tree(area, SWT.BORDER);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData();
        data.widthHint = 60;
        tree.setLayoutData(data);
        new TreeItem(tree, SWT.NONE).setText("General");
        new TreeItem(tree, SWT.NONE).setText("Plug-in Development");
        DialogPageParts.widenColumn(tree);
        assertThat(data.widthHint).isEqualTo(Math.min(DialogPageParts.MAX_NAVIGATION_WIDTH,
                        tree.computeTrim(0, 0, DialogPageParts.labelsWidth(tree), 0).width))
                .isGreaterThan(60);
    }

    @Test
    @DisplayName("a column sized by FormData widens too, and a column already wide enough is left alone")
    void navigationColumnByFormData() {
        Composite area = new Composite(main, SWT.NONE);
        area.setLayout(new org.eclipse.swt.layout.FormLayout());
        Composite holder = new Composite(area, SWT.NONE);
        org.eclipse.swt.layout.FormData form = new org.eclipse.swt.layout.FormData();
        form.width = 60;
        holder.setLayoutData(form);
        holder.setLayout(new org.eclipse.swt.layout.FillLayout());
        Tree tree = new Tree(holder, SWT.BORDER);
        new TreeItem(tree, SWT.NONE).setText("Plug-in Development");
        DialogPageParts.widenColumn(tree);
        assertThat(form.width).isGreaterThan(60);

        int widened = form.width;
        form.width = 1000;
        DialogPageParts.widenColumn(tree);
        assertThat(form.width).isEqualTo(1000);
        assertThat(widened).isLessThanOrEqualTo(DialogPageParts.MAX_NAVIGATION_WIDTH);

        Composite grid = new Composite(main, SWT.NONE);
        grid.setLayout(new org.eclipse.swt.layout.GridLayout());
        Tree wide = new Tree(grid, SWT.BORDER);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData();
        data.widthHint = 1000;
        wide.setLayoutData(data);
        new TreeItem(wide, SWT.NONE).setText("General");
        DialogPageParts.widenColumn(wide);
        assertThat(data.widthHint).isEqualTo(1000);
    }

    @Test
    @DisplayName("an action in a dialog that is gone, hidden, or not above the parent leaves a new dialog with its own parent")
    void staleActionsAreIgnored() {
        Shell page = page();
        page.open();
        Shell sheet = new Shell(page, DIALOG);
        Button inSheet = new Button(sheet, SWT.PUSH);
        DialogPresentation.acted(inSheet);
        Shell whileHidden = new Shell(main, DIALOG);
        whileHidden.setData(DialogPresentation.DATA_KEY, "page");
        whileHidden.open();
        assertThat(DialogPresentation.pageOwner(whileHidden)).as("the sheet was never shown").isNull();

        sheet.open();
        DialogPresentation.acted(inSheet);
        Shell ofTheSheet = new Shell(sheet, DIALOG);
        ofTheSheet.open();
        assertThat(DialogPresentation.pageOwner(ofTheSheet)).as("its own parent already").isSameAs(page);

        ofTheSheet.dispose();
        sheet.dispose();
        Shell afterwards = new Shell(main, DIALOG);
        afterwards.setData(DialogPresentation.DATA_KEY, "page");
        afterwards.open();
        assertThat(DialogPresentation.pageOwner(afterwards)).as("the sheet is gone").isNull();
    }

    @Test
    @DisplayName("an action in a modeless window, in a dialog beside the parent, in a disposed control or in the dialog itself leaves a new dialog with its own parent")
    void actionsElsewhereAreIgnored() {
        Shell page = page();
        page.open();
        Shell tool = new Shell(main, SWT.DIALOG_TRIM);
        tool.open();
        DialogPresentation.acted(new Button(tool, SWT.PUSH));
        Shell afterTool = new Shell(main, DIALOG);
        afterTool.setData(DialogPresentation.DATA_KEY, "page");
        afterTool.open();
        assertThat(DialogPresentation.pageOwner(afterTool)).as("acted in a modeless window").isNull();

        Shell beside = new Shell(main, DIALOG);
        beside.open();
        DialogPresentation.acted(new Button(beside, SWT.PUSH));
        Shell ofPage = new Shell(page, DIALOG);
        ofPage.open();
        assertThat(DialogPresentation.pageOwner(ofPage)).as("acted in a dialog beside the page, not above it").isSameAs(page);
        assertThat(DialogPresentation.of(ofPage)).isEqualTo(DialogPresentation.SHEET);
        beside.dispose();

        Shell sheet = new Shell(page, DIALOG);
        sheet.open();
        Button gone = new Button(sheet, SWT.PUSH);
        gone.dispose();
        DialogPresentation.acted(gone);
        Shell reopened = new Shell(main, DIALOG);
        reopened.setData(DialogPresentation.DATA_KEY, "page");
        reopened.open();
        DialogPresentation.acted(new Button(reopened, SWT.PUSH));
        reopened.setVisible(false);
        reopened.open();
        assertThat(DialogPresentation.pageOwner(reopened)).as("acted in itself").isNull();
    }

    @Test
    @DisplayName("a dialog opened from a dialog that has since closed falls back to its own parent")
    void openerGone() {
        Shell page = page();
        page.open();
        Shell sheet = new Shell(page, DIALOG);
        sheet.open();
        DialogPresentation.acted(new Button(sheet, SWT.PUSH));
        Shell opened = new Shell(main, DIALOG);
        opened.open();
        assertThat(DialogPresentation.pageOwner(opened)).isSameAs(page);
        sheet.dispose();
        assertThat(DialogPresentation.pageOwner(opened)).isNull();
    }

    @Test
    @DisplayName("an open page blocks nothing outside itself")
    void pageIsNotModal() {
        Shell page = page();
        page.open();
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isTrue();
    }

    @Test
    @DisplayName("a dialog the page opens blocks the page, and only the page")
    void sheetOfAPageBlocksThePage() {
        Shell page = page();
        page.open();
        Button inPage = new Button(page, SWT.PUSH);
        Shell sheet = new Shell(page, DIALOG);
        sheet.open();
        assertThat(((DartControl) inPage.getImpl()).isActive()).isFalse();
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isTrue();
        assertThat(DialogPresentation.pageOwner(sheet)).isSameAs(page);
        assertThat(DialogPresentation.pageOwner(page)).isNull();
    }

    @Test
    @DisplayName("a dialog given the workbench as parent, opened from a dialog of a page, stacks over that dialog")
    void openedFromADialogOfAPage() {
        Shell page = page();
        page.open();
        Button inPage = new Button(page, SWT.PUSH);
        Shell sheet = new Shell(page, DIALOG);
        Button inSheet = new Button(sheet, SWT.PUSH);
        sheet.open();
        DialogPresentation.acted(inSheet);
        Shell preferences = new Shell(main, DIALOG);
        preferences.setData(new org.eclipse.jface.preference.PreferenceDialog(null, new org.eclipse.jface.preference.PreferenceManager()));
        preferences.open();
        assertThat(DialogPresentation.of(preferences)).isEqualTo(DialogPresentation.SHEET);
        assertThat(DialogPresentation.pageHost(preferences)).isNull();
        assertThat(DialogPresentation.pageOwner(preferences)).isSameAs(page);
        assertThat(((DartControl) inSheet.getImpl()).isActive()).isFalse();
        assertThat(((DartControl) inPage.getImpl()).isActive()).isFalse();
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isTrue();
    }

    @Test
    @DisplayName("a menu or tool item the user chooses counts as an action in its shell")
    void actionsThroughItems() {
        Shell page = page();
        page.open();
        Shell sheet = new Shell(page, DIALOG);
        Menu bar = new Menu(main, SWT.BAR);
        MenuItem fromMenu = new MenuItem(bar, SWT.PUSH);
        ToolBar tools = new ToolBar(sheet, SWT.NONE);
        ToolItem fromTools = new ToolItem(tools, SWT.PUSH);
        sheet.open();

        DialogPresentation.acted(fromTools);
        Shell opened = new Shell(main, DIALOG);
        opened.setData(DialogPresentation.DATA_KEY, "page");
        opened.open();
        assertThat(DialogPresentation.pageOwner(opened)).as("opened from the sheet's tool bar").isSameAs(page);

        DialogPresentation.acted(fromMenu);
        DialogPresentation.acted(null);
        Tree tree = new Tree(sheet, SWT.NONE);
        DialogPresentation.acted(new TreeItem(tree, SWT.NONE));
        Shell fromBar = new Shell(main, DIALOG);
        fromBar.setData(DialogPresentation.DATA_KEY, "page");
        fromBar.open();
        assertThat(DialogPresentation.pageOwner(fromBar)).as("opened from the window's menu bar").isNull();

        opened.dispose();
        sheet.dispose();
        assertThat(DialogPresentation.of(page)).isEqualTo(DialogPresentation.PAGE);
    }

    @Test
    @DisplayName("a dialog the user opens from the workbench while a page has a dialog open is a page of its own")
    void openedFromTheWorkbenchBesideAPage() {
        Shell page = page();
        page.open();
        Shell sheet = new Shell(page, DIALOG);
        sheet.open();
        DialogPresentation.acted(new Button(sheet, SWT.PUSH));
        DialogPresentation.acted(inWorkbench);
        Shell preferences = new Shell(main, DIALOG);
        preferences.setData(new org.eclipse.jface.preference.PreferenceDialog(null, new org.eclipse.jface.preference.PreferenceManager()));
        preferences.open();
        assertThat(DialogPresentation.of(preferences)).isEqualTo(DialogPresentation.PAGE);
        assertThat(DialogPresentation.pageOwner(preferences)).isNull();
    }

    @Test
    @DisplayName("a dialog of the workbench still blocks it")
    void workbenchSheetBlocks() {
        Shell sheet = new Shell(main, DIALOG);
        sheet.open();
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isFalse();
        assertThat(DialogPresentation.pageOwner(sheet)).isNull();
    }

    @Test
    @DisplayName("with no editor area a page is a large modal sheet")
    void noEditorArea() {
        editorArea.setData("modelElement", null);
        Shell page = page();
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isNull();
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isFalse();
        Rectangle client = main.getClientArea();
        assertThat(page.getBounds().width).isEqualTo(client.width * DialogPages.LARGE_SHEET_PERCENT / 100);
    }

    @Test
    @DisplayName("the flag off places nothing and keeps the dialog modal")
    void flagOff() {
        Config.getConfigFlags().modern_dialogs = false;
        Shell page = page();
        page.open();
        assertThat(page.getBounds().width).isEqualTo(500);
        assertThat(((DartControl) inWorkbench.getImpl()).isActive()).isFalse();
    }

    /** What JFace's resources ask of the display before it builds a dialog. */
    private void jfaceReady() {
        when(display.getFontList(any(), org.mockito.ArgumentMatchers.anyBoolean())).thenReturn(new org.eclipse.swt.graphics.FontData[0]);
        when(display.getBounds()).thenReturn(new Rectangle(0, 0, 2560, 1440));
        when(display.getClientArea()).thenReturn(new Rectangle(0, 0, 2560, 1440));
    }

    /** A wizard of two pages, the first with a message. */
    private static org.eclipse.jface.wizard.Wizard twoPageWizard() {
        return new org.eclipse.jface.wizard.Wizard() {
            @Override
            public void addPages() {
                addPage(new org.eclipse.jface.wizard.WizardPage("one", "Pick a kind", null) {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                        setMessage("Choose one", INFORMATION);
                    }
                });
                addPage(new org.eclipse.jface.wizard.WizardPage("two", "Name it", null) {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                    }
                });
            }

            @Override
            public boolean performFinish() {
                return true;
            }
        };
    }

    @Test
    @DisplayName("a wizard page shows its steps, its heading and its message")
    void wizardParts() {
        jfaceReady();
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(main, twoPageWizard());
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.open();
        assertThat(DialogPresentation.pageHost(page)).isNotNull();
        assertThat(DialogPageParts.steps(page)).containsExactly("Pick a kind", "Name it");
        assertThat(DialogPageParts.step(page)).isZero();
        assertThat(DialogPageParts.message(page)).isEqualTo("Choose one");
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_INFORMATION);
        assertThat(DialogPageParts.title(page)).isEqualTo("Pick a kind");
        assertThat(DialogPageParts.headingHeight(page)).isPositive();

        org.eclipse.jface.wizard.WizardPage first = (org.eclipse.jface.wizard.WizardPage) dialog.getCurrentPage();
        first.setMessage("Careful", org.eclipse.jface.dialogs.IMessageProvider.WARNING);
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_WARNING);
        first.setErrorMessage("Not a kind");
        assertThat(DialogPageParts.message(page)).isEqualTo("Not a kind");
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_ERROR);
        first.setErrorMessage(null);

        dialog.showPage(first.getNextPage());
        assertThat(DialogPageParts.step(page)).as("the page change listener follows the wizard").isEqualTo(1);
        assertThat(DialogPageParts.title(page)).isEqualTo("Name it");
    }

    @Test
    @DisplayName("a wizard of one page has no steps, and a page with no title is named by its name")
    void oneStepWizard() {
        jfaceReady();
        org.eclipse.jface.wizard.Wizard wizard = new org.eclipse.jface.wizard.Wizard() {
            @Override
            public void addPages() {
                addPage(new org.eclipse.jface.wizard.WizardPage("only") {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                    }
                });
            }

            @Override
            public boolean performFinish() {
                return true;
            }
        };
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(main, wizard);
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.open();
        assertThat(DialogPageParts.steps(page)).isNull();
        assertThat(DialogPageParts.step(page)).isEqualTo(-1);
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_NONE);
        assertThat(DialogPageParts.steps(main)).as("not a page").isNull();
        assertThat(DialogPageParts.message(main)).isNull();
        assertThat(DialogPageParts.title(main)).isNull();
        assertThat(DialogPageParts.messageType(main)).isEqualTo(DialogPageParts.MESSAGE_NONE);
    }

    @Test
    @DisplayName("a title-area dialog presented as a page says its message's kind through its message image")
    void titleAreaMessageKinds() {
        jfaceReady();
        org.eclipse.jface.dialogs.TitleAreaDialog dialog = new org.eclipse.jface.dialogs.TitleAreaDialog(main);
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.setData(DialogPresentation.DATA_KEY, "page");
        page.open();
        dialog.setTitle("Configure");
        dialog.setMessage("Plain");
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_NONE);
        dialog.setMessage("Note", org.eclipse.jface.dialogs.IMessageProvider.INFORMATION);
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_INFORMATION);
        dialog.setMessage("Careful", org.eclipse.jface.dialogs.IMessageProvider.WARNING);
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_WARNING);
        dialog.setMessage("Broken", org.eclipse.jface.dialogs.IMessageProvider.ERROR);
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_ERROR);
        assertThat(DialogPageParts.message(page)).isEqualTo("Broken");
        assertThat(DialogPageParts.steps(page)).as("not a wizard").isNull();
    }

    @Test
    @DisplayName("a tree whose column sets no width is left as it is")
    void navigationColumnWithoutWidth() {
        Composite area = new Composite(main, SWT.NONE);
        area.setLayout(new org.eclipse.swt.layout.GridLayout());
        Tree tree = new Tree(area, SWT.BORDER);
        org.eclipse.swt.layout.GridData data = new org.eclipse.swt.layout.GridData();
        tree.setLayoutData(data);
        new TreeItem(tree, SWT.NONE).setText("Plug-in Development");
        DialogPageParts.widenColumn(tree);
        assertThat(data.widthHint).isEqualTo(SWT.DEFAULT);
    }

    @Test
    @DisplayName("a wizard's page it did not list is a step too, and a page with no title or an empty one is named by its name")
    void unlistedWizardPage() {
        jfaceReady();
        org.eclipse.jface.wizard.WizardPage extra = new org.eclipse.jface.wizard.WizardPage("details", "", null) {
            @Override
            public void createControl(Composite parent) {
                setControl(new Composite(parent, SWT.NONE));
            }
        };
        org.eclipse.jface.wizard.Wizard wizard = new org.eclipse.jface.wizard.Wizard() {
            @Override
            public void addPages() {
                addPage(new org.eclipse.jface.wizard.WizardPage("kind", "Pick a kind", null) {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                    }
                });
                addPage(new org.eclipse.jface.wizard.WizardPage("location") {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                    }
                });
            }

            @Override
            public boolean performFinish() {
                return true;
            }
        };
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(main, wizard);
        dialog.setBlockOnOpen(false);
        dialog.create();
        extra.setWizard(wizard);
        dialog.addPageChangedListener(event -> { });
        Shell page = dialog.getShell();
        page.open();
        dialog.showPage(extra);
        assertThat(DialogPageParts.steps(page)).containsExactly("Pick a kind", "location", "details");
        assertThat(DialogPageParts.step(page)).isEqualTo(2);
        assertThat(DialogPageParts.title(page)).isEqualTo("");
    }

    @Test
    @DisplayName("a wizard with no pages has no steps")
    void emptyWizard() {
        jfaceReady();
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(main, new org.eclipse.jface.wizard.Wizard() {
            @Override
            public boolean performFinish() {
                return true;
            }
        });
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.open();
        assertThat(DialogPageParts.steps(page)).isNull();
    }

    @Test
    @DisplayName("a page saying a message kind JFace does not have is read through its message image instead")
    void unknownMessageKind() {
        jfaceReady();
        int[] kind = { 9 };
        org.eclipse.jface.wizard.Wizard wizard = new org.eclipse.jface.wizard.Wizard() {
            @Override
            public void addPages() {
                addPage(new org.eclipse.jface.wizard.WizardPage("odd", "Odd", null) {
                    @Override
                    public void createControl(Composite parent) {
                        setControl(new Composite(parent, SWT.NONE));
                    }

                    @Override
                    public int getMessageType() {
                        return kind[0];
                    }
                });
            }

            @Override
            public boolean performFinish() {
                return true;
            }
        };
        org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(main, wizard);
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.open();
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_NONE);
        kind[0] = -1;
        assertThat(DialogPageParts.messageType(page)).isEqualTo(DialogPageParts.MESSAGE_NONE);
    }

    @Test
    @DisplayName("the Preferences dialog as a page widens its category column to its labels")
    void preferencesColumn() {
        jfaceReady();
        org.eclipse.jface.preference.PreferenceManager manager = new org.eclipse.jface.preference.PreferenceManager();
        manager.addToRoot(new org.eclipse.jface.preference.PreferenceNode("general",
                new org.eclipse.jface.preference.PreferencePage("General") {
                    @Override
                    protected Control createContents(Composite parent) {
                        return new Composite(parent, SWT.NONE);
                    }
                }));
        org.eclipse.jface.preference.PreferenceDialog dialog = new org.eclipse.jface.preference.PreferenceDialog(main, manager);
        dialog.setBlockOnOpen(false);
        dialog.create();
        Shell page = dialog.getShell();
        page.open();
        assertThat(DialogPresentation.of(page)).isEqualTo(DialogPresentation.PAGE);
        assertThat(DialogPresentation.pageHost(page)).isNotNull();
        assertThat(dialog.getTreeViewer().getTree().getItemCount()).isEqualTo(1);
    }
}

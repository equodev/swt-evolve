package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * What {@link DartTree} keeps for the render side: the selection and the item at the top are Java's,
 * every change to them is named for the client, and a scroll the client reports moves the top.
 */
@Tag("native-unit")
class TreeImplementationNativeTest extends SerializeTestBase {

    private static TreeItem[] roots(Tree tree, int count) {
        TreeItem[] items = new TreeItem[count];
        for (int i = 0; i < count; i++) {
            items[i] = new TreeItem(tree, SWT.NONE);
            items[i].setText("item " + i);
        }
        return items;
    }

    private static Tree sized(int style) {
        Tree tree = new Tree(shell(), style);
        tree.setBounds(0, 0, 200, 120);
        return tree;
    }

    private static DartTree impl(Tree tree) {
        return (DartTree) tree.getImpl();
    }

    private static void clearDirty(Tree tree) {
        impl(tree).getValue().clearDirty();
    }

    private static boolean named(Tree tree, String key) {
        return impl(tree).getValue().changedKeys().contains(key);
    }

    private static void scrolledBy(Tree tree, int pixels) {
        Event event = new Event();
        event.y = pixels;
        TreeHelper.recordScrollOffset(impl(tree), event);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartTree impl = new DartTree(shell(), SWT.MULTI, null);

        Tree api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a scroll the client reports moves the item at the top, without sending it back")
    void aReportedScrollMovesTheTopItem() {
        Tree tree = sized(SWT.NONE);
        TreeItem[] items = roots(tree, 60);
        int rowHeight = tree.getItemHeight();
        clearDirty(tree);

        scrolledBy(tree, 12 * rowHeight + rowHeight / 2);

        assertThat(tree.getTopItem()).isSameAs(items[12]);
        assertThat(impl(tree).topItem).isSameAs(items[12]);
        assertThat(named(tree, VTree.TOP_ITEM)).isFalse();
        assertThat(tree.getItem(new org.eclipse.swt.graphics.Point(5, 1))).isSameAs(items[12]);
    }

    @Test
    @DisplayName("a scroll reported past the last row stops at the last row the tree can show at the top")
    void aScrollPastTheRowsIsClamped() {
        Tree tree = sized(SWT.NONE);
        TreeItem[] items = roots(tree, 10);

        scrolledBy(tree, 10_000);

        TreeItem top = tree.getTopItem();
        assertThat(top).isNotSameAs(items[9]).isNotSameAs(items[0]);
        assertThat(TreeHelper.scrollOffsetY(impl(tree))).isLessThan(10_000);
    }

    @Test
    @DisplayName("a scroll reported on a tree without rows has no item at the top")
    void aScrollOnAnEmptyTree() {
        Tree tree = sized(SWT.NONE);

        scrolledBy(tree, 50);

        assertThat(impl(tree).topItem).isNull();
        assertThat(tree.getTopItem()).isNull();
    }

    @Test
    @DisplayName("setTopItem names the item for the client")
    void setTopItemNamesTheItem() {
        Tree tree = sized(SWT.NONE);
        TreeItem[] items = roots(tree, 60);
        clearDirty(tree);

        tree.setTopItem(items[20]);

        assertThat(named(tree, VTree.TOP_ITEM)).isTrue();
        assertThat(impl(tree).topItem).isSameAs(items[20]);
        assertThat(impl(tree).getValue().getTopItem()).isSameAs(items[20]);
    }

    @Test
    @DisplayName("showItem of a row already shown sends nothing")
    void showingAShownRowSendsNothing() {
        Tree tree = sized(SWT.NONE);
        TreeItem[] items = roots(tree, 60);
        clearDirty(tree);

        tree.showItem(items[1]);

        assertThat(named(tree, VTree.TOP_ITEM)).isFalse();
        assertThat(tree.getTopItem()).isSameAs(items[0]);
    }

    @Test
    @DisplayName("an item that shows on no row is not scrolled to")
    void anItemOnNoRowIsNotScrolledTo() {
        Tree tree = sized(SWT.NONE);
        roots(tree, 60);
        TreeItem foreign = new TreeItem(new Tree(shell(), SWT.NONE), SWT.NONE);
        clearDirty(tree);

        TreeHelper.setTopItem(impl(tree), foreign);
        TreeHelper.showItem(impl(tree), foreign);

        assertThat(named(tree, VTree.TOP_ITEM)).isFalse();
    }

    @Test
    @DisplayName("removeAll starts the next rows at the top")
    void removeAllStartsAtTheTop() {
        Tree tree = sized(SWT.NONE);
        roots(tree, 60);
        scrolledBy(tree, 20 * tree.getItemHeight());

        tree.removeAll();
        TreeItem[] fresh = roots(tree, 60);

        assertThat(TreeHelper.scrollOffsetY(impl(tree))).isZero();
        assertThat(impl(tree).topItem).isNull();
        assertThat(tree.getTopItem()).isSameAs(fresh[0]);
    }

    @Test
    @DisplayName("every change of the selection is named for the client, and only a change")
    void selectionChangesAreNamed() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem[] items = roots(tree, 4);
        Runnable[] changes = {
            () -> tree.select(items[1]),
            () -> tree.select(items[2]),
            () -> tree.deselect(items[1]),
            () -> tree.selectAll(),
            () -> tree.deselectAll(),
            () -> tree.setSelection(items[3]),
            () -> tree.setSelection(new TreeItem[] {items[0], items[2]}),
            () -> items[2].dispose(),
            () -> tree.removeAll(),
        };
        for (Runnable change : changes) {
            clearDirty(tree);
            change.run();
            assertThat(named(tree, VTree.SELECTION)).as("after %s", change).isTrue();
        }
    }

    @Test
    @DisplayName("re-applying the selection a tree has names nothing")
    void sameSelectionNamesNothing() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem[] items = roots(tree, 3);
        tree.setSelection(new TreeItem[] {items[0], items[2]});
        clearDirty(tree);

        tree.select(items[2]);
        tree.deselect(items[1]);
        tree.selectAll();
        clearDirty(tree);
        tree.selectAll();

        assertThat(named(tree, VTree.SELECTION)).isFalse();
    }

    @Test
    @DisplayName("a selection holding an item of no row keeps it, after the rows")
    void anItemOnNoRowGoesLast() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem[] items = roots(tree, 3);
        TreeItem hidden = new TreeItem(items[0], SWT.NONE);

        TreeItem[] ordered = TreeHelper.inRowOrder(impl(tree), new TreeItem[] {hidden, items[2], items[1]});

        assertThat(ordered).containsExactly(items[1], items[2], hidden);
    }

    @Test
    @DisplayName("a column order only grows with a new column when the application set one")
    void columnOrderGrowsOnlyWhenSet() {
        Tree tree = new Tree(shell(), SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);

        assertThat(impl(tree).columnOrder).isEmpty();
        assertThat(tree.getColumnOrder()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Cocoa's drag, check column and selection hooks answer as stubs")
    void cocoaHooksAreStubs() {
        Tree tree = new Tree(shell(), SWT.CHECK);
        DartTree impl = impl(tree);

        assertThat(impl.dragDetect(1, 1, false, new boolean[1])).isFalse();
        assertThat(impl.getCheckColumnWidth()).isZero();
        impl.sendSelection();
        impl.ignoreSelect = true;
        impl.sendSelection();
        impl.ignoreSelect = false;
        impl.sendDoubleSelection();
        assertThat(tree.getSelectionCount()).isZero();
    }

    @Test
    @DisplayName("the row height Cocoa lets SWT set internally takes -1 and up")
    void setItemHeightChecksItsArgument() {
        DartTree impl = impl(new Tree(shell(), SWT.NONE));
        int height = impl.getItemHeight();

        impl.setItemHeight(-1);
        impl.setItemHeight(30);

        assertThat(impl.getItemHeight()).isEqualTo(height);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> impl.setItemHeight(-2))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("the API's subclass check is the implementation's, which accepts SWT's own package")
    void theSubclassCheckIsTheImplementations() {
        DartTree impl = new DartTree(shell(), SWT.NONE, null);
        Tree subclass = new Tree(impl) {
        };
        impl.setApi(subclass);

        org.assertj.core.api.Assertions.assertThatCode(subclass::checkSubclass).doesNotThrowAnyException();
        org.assertj.core.api.Assertions.assertThatCode(impl::checkSubclass).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an editor is named for the client when it is added, disposed or loses its control")
    void editorsAreNamed() {
        Tree tree = new Tree(shell(), SWT.NONE);
        TreeItem[] items = roots(tree, 2);
        org.eclipse.swt.custom.TreeEditor editor = new org.eclipse.swt.custom.TreeEditor(tree);
        Text text = new Text(tree, SWT.NONE);
        editor.setEditor(text, items[0]);
        assertThat(impl(tree)._editors()).contains(editor);
        clearDirty(tree);
        impl(tree)._addEditor(editor);
        assertThat(named(tree, VTree.EDITORS)).isFalse();

        text.dispose();
        assertThat(named(tree, VTree.EDITORS)).isTrue();

        clearDirty(tree);
        editor.dispose();
        assertThat(impl(tree)._editors()).doesNotContain(editor);
        assertThat(named(tree, VTree.EDITORS)).isTrue();

        clearDirty(tree);
        impl(tree)._removeEditor(editor);
        assertThat(named(tree, VTree.EDITORS)).isFalse();
    }

    @Test
    @DisplayName("the sort indicator and the header foreground are named for the client when they change")
    void headerChangesAreNamed() {
        Tree tree = new Tree(shell(), SWT.NONE);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        clearDirty(tree);

        tree.setSortColumn(column);
        assertThat(named(tree, VTree.SORT_COLUMN)).isTrue();
        assertThat(named(tree, VTree.SORT_DIRECTION)).isFalse();

        clearDirty(tree);
        tree.setSortDirection(SWT.DOWN);
        assertThat(named(tree, VTree.SORT_DIRECTION)).isTrue();
        assertThat(named(tree, VTree.SORT_COLUMN)).isFalse();

        clearDirty(tree);
        tree.setHeaderForeground(new org.eclipse.swt.graphics.Color(tree.getDisplay(), 1, 2, 3));
        assertThat(named(tree, VTree.HEADER_FOREGROUND)).isTrue();
    }

    @Test
    @DisplayName("looking for an open item skips empty slots, disposed items and closed ones")
    void findingAnOpenItemSkipsWhatCannotBeIt() {
        Tree tree = new Tree(shell(), SWT.NONE);
        TreeItem[] items = roots(tree, 3);
        new TreeItem(items[1], SWT.NONE);
        items[1].setExpanded(true);
        TreeItem gone = new TreeItem(tree, SWT.NONE);
        gone.dispose();
        DartTree impl = impl(tree);
        int id = (int) dev.equo.swt.FlutterBridge.id(items[1]);

        assertThat(impl.findOpenItem(null, id)).isNull();
        assertThat(impl.findOpenItem(new TreeItem[] {null, gone, items[0], items[1]}, id)).isSameAs(items[1]);
        assertThat(impl.findOpenItem(new TreeItem[] {null, gone, items[1]}, -1)).isNull();
    }
}

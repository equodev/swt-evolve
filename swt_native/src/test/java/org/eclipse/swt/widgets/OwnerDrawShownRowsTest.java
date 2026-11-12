package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.table;
import static org.eclipse.swt.widgets.Mocks.tree;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A repaint of an owner-drawing Tree or Table captures the rows on screen and a screenful above and
 * below them, as native SWT runs the PaintItem listener only for the rows it exposes. The other rows
 * are captured once the repaints stop, and as a scroll brings them on screen.
 */
class OwnerDrawShownRowsTest extends SerializeTestBase {

    private static final int ROW = 20;
    private static final int ROWS = 40;
    /** Five rows fit; a screenful is counted as six, the part-row below included. */
    private static final int HEIGHT = 5 * ROW;

    @Test
    void aTreeRepaintNamesTheRowsAroundTheScreenAndTheRestOnceRepaintsStop() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        List<TreeItem> rows = treeRows(tree, impl);

        TreeHelper.nameOwnerDrawnCells(impl);

        assertThat(named(rows)).containsExactlyElementsOf(range(0, 12));
        settle(tree.getDisplay()).run();
        assertThat(named(rows)).containsExactlyElementsOf(range(0, ROWS));
    }

    @Test
    void theRowsATreeRepaintNamedAreNotNamedAgainOnceRepaintsStop() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        List<TreeItem> rows = treeRows(tree, impl);
        TreeHelper.nameOwnerDrawnCells(impl);
        rows.forEach(OwnerDrawShownRowsTest::clean);

        settle(tree.getDisplay()).run();

        assertThat(named(rows)).containsExactlyElementsOf(range(12, ROWS));
    }

    @Test
    void aTreeWhoseRowsAllFitNamesOnlyTheChildrenOfACollapsedRowOnceRepaintsStop() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        TreeItem collapsed = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(collapsed, SWT.NONE);
        TreeItem expanded = new TreeItem(tree, SWT.NONE);
        TreeItem shownChild = new TreeItem(expanded, SWT.NONE);
        ((DartTreeItem) expanded.getImpl()).expanded = true;
        when(impl.getApi()).thenReturn(tree);
        impl.items = new TreeItem[] { collapsed, expanded };
        ((DartTreeItem) collapsed.getImpl()).items = new TreeItem[] { child };
        ((DartTreeItem) expanded.getImpl()).items = new TreeItem[] { shownChild };
        clearInvocations(tree.getDisplay());
        TreeHelper.nameOwnerDrawnCells(impl);
        List<TreeItem> all = List.of(collapsed, child, expanded, shownChild);
        all.forEach(OwnerDrawShownRowsTest::clean);

        settle(tree.getDisplay()).run();

        assertThat(named(all)).containsExactly(1);
    }

    @Test
    void theRowsATableRepaintNamedAreNotNamedAgainOnceRepaintsStop() {
        Table table = table();
        DartTable impl = ownerDrawnTable(table);
        List<TableItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) rows.add(new TableItem(table, SWT.NONE));
        impl.items = rows.toArray(new TableItem[0]);
        clearInvocations(table.getDisplay());
        TableHelper.nameOwnerDrawnCells(impl);
        rows.forEach(OwnerDrawShownRowsTest::clean);

        settle(table.getDisplay()).run();

        assertThat(named(rows)).containsExactlyElementsOf(range(12, ROWS));
    }

    @Test
    void theRowsLeftAreNamedAFewAtATimeSoNoFlushCapturesThemAll() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        List<TreeItem> rows = new ArrayList<>();
        for (int i = 0; i < 500; i++) rows.add(new TreeItem(tree, SWT.NONE));
        when(impl.getApi()).thenReturn(tree);
        when(tree.getItems()).thenReturn(rows.toArray(new TreeItem[0]));
        impl.items = rows.toArray(new TreeItem[0]);
        TreeHelper.nameOwnerDrawnCells(impl);
        rows.forEach(OwnerDrawShownRowsTest::clean);

        Runnable rowsLeft = settle(tree.getDisplay());
        rowsLeft.run();
        assertThat(named(rows)).hasSize(OwnerDrawRowsLeft.ROWS_PER_STEP);
        verify(tree.getDisplay()).timerExec(OwnerDrawRowsLeft.STEP_MS, rowsLeft);

        rowsLeft.run();
        rowsLeft.run();
        assertThat(named(rows)).as("all but the twelve the repaint named").hasSize(500 - 12);
        assertThat(impl.ownerDrawRowsLeft).as("none left").isNull();
    }

    @Test
    void aTreeScrolledWhileRowsAreLeftNamesTheRowsNowShown() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        List<TreeItem> rows = treeRows(tree, impl);
        TreeHelper.nameOwnerDrawnCells(impl);
        rows.forEach(OwnerDrawShownRowsTest::clean);

        Event scrolled = new Event();
        scrolled.y = 20 * ROW;
        TreeHelper.recordScrollOffset(impl, scrolled);

        assertThat(named(rows)).containsExactlyElementsOf(range(14, 32));
    }

    @Test
    void aTreeWhoseRowsAllFitLeavesOutTheChildrenOfACollapsedRow() {
        Tree tree = tree();
        DartTree impl = ownerDrawn(tree);
        TreeItem parent = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(parent, SWT.NONE);
        when(impl.getApi()).thenReturn(tree);
        when(tree.getItems()).thenReturn(new TreeItem[] { parent });
        impl.items = new TreeItem[] { parent };
        ((DartTreeItem) parent.getImpl()).items = new TreeItem[] { child };
        clean(parent);
        clean(child);
        clearInvocations(tree.getDisplay());

        TreeHelper.nameOwnerDrawnCells(impl);

        assertThat(isNamed(parent)).isTrue();
        assertThat(isNamed(child)).isFalse();
        settle(tree.getDisplay()).run();
        assertThat(isNamed(child)).isTrue();
    }

    @Test
    void aTableRepaintNamesTheRowsAroundTheScreenAndTheRestOnceRepaintsStop() {
        Table table = table();
        DartTable impl = ownerDrawnTable(table);
        List<TableItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) rows.add(new TableItem(table, SWT.NONE));
        impl.items = rows.toArray(new TableItem[0]);
        rows.forEach(OwnerDrawShownRowsTest::clean);
        clearInvocations(table.getDisplay());

        Event scrolled = new Event();
        scrolled.y = 20 * ROW;
        TableHelper.recordScrollOffset(impl, scrolled);
        TableHelper.nameOwnerDrawnCells(impl);

        assertThat(named(rows)).containsExactlyElementsOf(range(14, 32));
        settle(table.getDisplay()).run();
        assertThat(named(rows)).containsExactlyElementsOf(range(0, ROWS));
    }

    private static DartTable ownerDrawnTable(Table table) {
        DartTable impl = (DartTable) table.getImpl();
        when(impl.hooks(SWT.PaintItem)).thenReturn(true);
        when(impl.getItemHeight()).thenReturn(ROW);
        when(impl.getBounds()).thenReturn(new Rectangle(0, 0, 200, HEIGHT));
        Display display = table.getDisplay();
        when(impl.getDisplay()).thenReturn(display);
        when(impl.getApi()).thenReturn(table);
        when(impl.getItemCount()).thenReturn(ROWS);
        return impl;
    }

    private static DartTree ownerDrawn(Tree tree) {
        DartTree impl = (DartTree) tree.getImpl();
        when(impl.hooks(SWT.PaintItem)).thenReturn(true);
        when(impl.getItemHeight()).thenReturn(ROW);
        when(impl.getBounds()).thenReturn(new Rectangle(0, 0, 200, HEIGHT));
        Display display = tree.getDisplay();
        when(impl.getDisplay()).thenReturn(display);
        return impl;
    }

    private static List<TreeItem> treeRows(Tree tree, DartTree impl) {
        List<TreeItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) rows.add(new TreeItem(tree, SWT.NONE));
        when(impl.getApi()).thenReturn(tree);
        when(tree.getItems()).thenReturn(rows.toArray(new TreeItem[0]));
        impl.items = rows.toArray(new TreeItem[0]);
        rows.forEach(OwnerDrawShownRowsTest::clean);
        clearInvocations(tree.getDisplay());
        return rows;
    }

    /** The settle timer the last repaint armed. */
    private static Runnable settle(Display display) {
        ArgumentCaptor<Runnable> armed = ArgumentCaptor.forClass(Runnable.class);
        verify(display, atLeastOnce()).timerExec(eq(TreeHelper.OWNER_DRAW_SETTLE_MS), armed.capture());
        return armed.getValue();
    }

    private static void clean(Item row) {
        ((DartWidget) row.getImpl()).getValue().clearDirty();
    }

    private static boolean isNamed(Item row) {
        return ((DartWidget) row.getImpl()).getValue().changedKeys().contains(paintedTexts(row));
    }

    private static int paintedTexts(Item row) {
        return row instanceof TreeItem ? VTreeItem.PAINTED_TEXTS : VTableItem.PAINTED_TEXTS;
    }

    private static List<Integer> named(List<? extends Item> rows) {
        return IntStream.range(0, rows.size()).filter(i -> isNamed(rows.get(i))).boxed().collect(Collectors.toList());
    }

    private static List<Integer> range(int from, int to) {
        return IntStream.range(from, to).boxed().collect(Collectors.toList());
    }
}

package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TreeColumn;
import org.junit.jupiter.api.Test;

/**
 * A multi-column Tree's default width fell back to a fixed per-column guess instead of summing
 * each column's already-packed width, and never budgeted the vertical scrollbar's width, the same
 * way Table's computeSize did — both share the same generated sizing logic.
 */
class TreeComputeSizeTest {

    private static DartTree treeWithStyle(int style) {
        DartTree tree = mock(DartTree.class);
        when(tree.getStyle()).thenReturn(style);
        return tree;
    }

    @Test
    void widthReflectsThePackedColumnWidthNotAFixedFallback() {
        DartTree tree = treeWithStyle(SWT.NONE);
        when(tree.getColumnCount()).thenReturn(1);
        TreeColumn column = mock(TreeColumn.class);
        when(column.getWidth()).thenReturn(180);
        when(tree.getColumns()).thenReturn(new TreeColumn[] { column });

        Point size = TreeSizes.computeSize(tree, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(180);
    }

    @Test
    void widthSumsEveryColumnsPackedWidth() {
        DartTree tree = treeWithStyle(SWT.NONE);
        when(tree.getColumnCount()).thenReturn(2);
        TreeColumn first = mock(TreeColumn.class);
        when(first.getWidth()).thenReturn(60);
        TreeColumn second = mock(TreeColumn.class);
        when(second.getWidth()).thenReturn(90);
        when(tree.getColumns()).thenReturn(new TreeColumn[] { first, second });

        Point size = TreeSizes.computeSize(tree, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(150);
    }

    @Test
    void widthAddsTheVerticalScrollbarAllowanceWhenTheTreeScrolls() {
        DartTree tree = treeWithStyle(SWT.V_SCROLL);
        when(tree.getColumnCount()).thenReturn(1);
        TreeColumn column = mock(TreeColumn.class);
        when(column.getWidth()).thenReturn(142);
        when(tree.getColumns()).thenReturn(new TreeColumn[] { column });
        ScrollBar vBar = mock(ScrollBar.class);
        when(vBar.getSize()).thenReturn(new Point(20, 20));
        when(tree.getVerticalBar()).thenReturn(vBar);

        Point size = TreeSizes.computeSize(tree, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(142 + 20);
    }
}

package dev.equo.swt.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartTable;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.jupiter.api.Test;

/**
 * NatCombo (NatTable's dropdown editor) sizes its popup off Table.computeSize(SWT.DEFAULT, ...,
 * true) after packing its column. Falling back to a fixed per-column width made the popup track
 * the activated cell's size instead of its own content, and never budgeting the vertical
 * scrollbar's width let a later column-width shrink (to keep the scrollbar off the last column)
 * eat into the text itself.
 */
class TableComputeSizeTest {

    private static DartTable tableWithStyle(int style) {
        DartTable table = mock(DartTable.class);
        when(table.getStyle()).thenReturn(style);
        return table;
    }

    @Test
    void widthReflectsThePackedColumnWidthNotAFixedFallback() {
        DartTable table = tableWithStyle(SWT.NONE);
        when(table.getColumnCount()).thenReturn(1);
        TableColumn column = mock(TableColumn.class);
        when(column.getWidth()).thenReturn(180);
        when(table.getColumns()).thenReturn(new TableColumn[] { column });

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(180);
    }

    @Test
    void widthSumsEveryColumnsPackedWidth() {
        DartTable table = tableWithStyle(SWT.NONE);
        when(table.getColumnCount()).thenReturn(2);
        TableColumn first = mock(TableColumn.class);
        when(first.getWidth()).thenReturn(60);
        TableColumn second = mock(TableColumn.class);
        when(second.getWidth()).thenReturn(90);
        when(table.getColumns()).thenReturn(new TableColumn[] { first, second });

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(150);
    }

    @Test
    void widthAddsTheCheckboxAllowanceWhenTheTableHasCheckStyle() {
        // A checked Table's popup must be wide enough for the checkbox Flutter paints in column 0,
        // on top of the column's own packed text width, or the checkbox+text overflows the popup.
        DartTable table = tableWithStyle(SWT.CHECK);
        when(table.getColumnCount()).thenReturn(1);
        TableColumn column = mock(TableColumn.class);
        when(column.getWidth()).thenReturn(70);
        when(table.getColumns()).thenReturn(new TableColumn[] { column });

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(70 + 20 + 8);
    }

    @Test
    void widthAddsTheVerticalScrollbarAllowanceWhenTheTableScrolls() {
        // A caller that shrinks column 0 by the vertical scrollbar's own width — so the scrollbar
        // doesn't overlap the last column — needs that space already budgeted here, or the column
        // (and its text) end up narrower than what was packed, truncating long items.
        DartTable table = tableWithStyle(SWT.V_SCROLL);
        when(table.getColumnCount()).thenReturn(1);
        TableColumn column = mock(TableColumn.class);
        when(column.getWidth()).thenReturn(142);
        when(table.getColumns()).thenReturn(new TableColumn[] { column });
        ScrollBar vBar = mock(ScrollBar.class);
        when(vBar.getSize()).thenReturn(new Point(20, 20));
        when(table.getVerticalBar()).thenReturn(vBar);

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, 100, true);

        assertThat(size.x).isEqualTo(142 + 20);
    }

    @Test
    void heightOfABorderlessTableIsExactlyItsRows() {
        // NatCombo sizes its popup as visibleItems * Table.getItemHeight() and lays the tables out
        // at their computeSize height. Any trim the second answer adds and the first does not comes
        // straight out of the item list, clipping its last row.
        DartTable table = tableWithStyle(SWT.NONE);
        when(table.getItemCount()).thenReturn(10);
        when(table.getItems()).thenReturn(new TableItem[0]);

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, SWT.DEFAULT, true);

        assertThat(size.y).isEqualTo(10 * TableSizes.getItemHeight(table));
    }

    @Test
    void heightOfABorderedTableAddsTheBorderItPaints() {
        DartTable table = tableWithStyle(SWT.BORDER);
        when(table.getItemCount()).thenReturn(10);
        when(table.getItems()).thenReturn(new TableItem[0]);

        Point size = TableSizes.computeSize(table, SWT.DEFAULT, SWT.DEFAULT, true);

        assertThat(size.y).isEqualTo(10 * TableSizes.getItemHeight(table) + TableSizes.getFrameBorder(table));
        assertThat(TableSizes.getFrameBorder(table)).isPositive();
    }
}

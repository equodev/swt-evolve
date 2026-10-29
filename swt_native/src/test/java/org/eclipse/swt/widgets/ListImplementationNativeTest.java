package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** What {@link DartList} keeps for the render side and answers to the API, driven without a display. */
@Tag("native-unit")
class ListImplementationNativeTest extends SerializeTestBase {

    private static DartList impl(List list) {
        return (DartList) list.getImpl();
    }

    private static List list(int style, int items) {
        List list = new List(shell(), style);
        list.setBounds(0, 0, 200, 120);
        String[] names = new String[items];
        for (int i = 0; i < items; i++)
            names[i] = "item " + i;
        list.setItems(names);
        return list;
    }

    @Test
    @DisplayName("removing a range before the selection shifts the selected items with it")
    void removingARangeBeforeTheSelectionShiftsIt() {
        List list = list(SWT.MULTI, 8);
        list.select(new int[] {5, 6});

        list.remove(1, 2);

        assertThat(list.getSelectionIndices()).containsExactly(3, 4);
        assertThat(list.getSelection()).containsExactly(list.getItem(3), list.getItem(4));
    }

    @Test
    @DisplayName("removing every item empties the selection")
    void removeAllEmptiesTheSelection() {
        List list = list(SWT.MULTI, 4);
        list.select(new int[] {1, 3});

        list.removeAll();

        assertThat(list.getSelectionIndices()).isEmpty();
        assertThat(impl(list).getValue().changedKeys()).contains(VList.SELECTION);
    }

    @Test
    @DisplayName("removing the selected last item and replacing the items with fewer drop the selection beyond them")
    void selectionStaysInsideTheItems() {
        List list = list(SWT.SINGLE, 3);
        list.select(2);
        list.remove(2);
        assertThat(list.getSelectionIndices()).isEmpty();

        List multi = list(SWT.MULTI, 6);
        multi.select(new int[] {1, 5});
        multi.setItems(new String[] {"a", "b", "c"});
        assertThat(multi.getSelectionIndices()).containsExactly(1);
        assertThat(multi.getSelection()).containsExactly("b");
    }

    @Test
    @DisplayName("the focus stays on the item that was selected when the selection is cleared, as on Win32 and GTK")
    void focusSurvivesAClearedSelection() {
        List list = list(SWT.SINGLE, 5);
        list.setSelection(3);
        list.setSelection(1000);

        assertThat(list.getSelectionCount()).isZero();
        assertThat(list.getFocusIndex()).isEqualTo(3);

        list.setSelection(2);
        list.deselectAll();
        assertThat(list.getFocusIndex()).isEqualTo(2);

        list.removeAll();
        assertThat(list.getFocusIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("scrolling stops where the last item is shown")
    void setTopIndexStopsAtTheLastPage() {
        List list = list(SWT.SINGLE, 100);

        list.setTopIndex(99);

        int top = list.getTopIndex();
        assertThat(top).isBetween(1, 98);
        assertThat(top + list.getClientArea().height / list.getItemHeight()).isGreaterThanOrEqualTo(99);
        list.setTopIndex(-4);
        assertThat(list.getTopIndex()).isZero();
        list.setTopIndex(10);
        assertThat(list.getTopIndex()).isEqualTo(10);
    }

    @Test
    @DisplayName("an index past the last item scrolls as far as the last item, as on Win32")
    void setTopIndexPastTheItemsStopsAtTheLastPage() {
        List list = list(SWT.SINGLE, 100);
        list.setTopIndex(99);
        int last = list.getTopIndex();
        list.setTopIndex(30);

        list.setTopIndex(1000);

        assertThat(list.getTopIndex()).isEqualTo(last);
        list.setTopIndex(30);
        list.setTopIndex(100);
        assertThat(list.getTopIndex()).isEqualTo(last);
    }

    @Test
    @DisplayName("selecting an item scrolls it into view, and selecting without showing it does not")
    void setSelectionShowsTheItem() {
        List list = list(SWT.MULTI, 100);
        list.select(60);
        assertThat(list.getTopIndex()).isZero();

        list.setSelection(60);
        int top = list.getTopIndex();
        assertThat(top).isBetween(1, 60);

        list.setSelection(new String[] {"item 5", "item 90"});
        assertThat(list.getTopIndex()).as("the first string's first match is the one shown").isEqualTo(5);

        list.setSelection(0, 5000);
        assertThat(list.getTopIndex()).as("the start of a range is the one shown").isZero();
    }

    @Test
    @DisplayName("removing items takes the list back inside what is left of them")
    void removingItemsKeepsTheTopInside() {
        List list = list(SWT.SINGLE, 100);
        list.setTopIndex(90);

        list.removeAll();

        assertThat(list.getTopIndex()).isZero();
    }

    @Test
    @DisplayName("the preferred size follows the hints, and each item adds its height, with no gap between items")
    void computeSizeHonoursTheHints() {
        List list = list(SWT.NONE, 4);

        Point small = list.computeSize(100, 50);
        Point large = list.computeSize(200, 150);
        assertThat(large.x - small.x).isEqualTo(100);
        assertThat(large.y - small.y).isEqualTo(100);

        int four = list.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        list.add("fifth");
        assertThat(list.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - four).isEqualTo(list.getItemHeight());
        assertThat(new List(shell(), SWT.NONE).computeSize(SWT.DEFAULT, SWT.DEFAULT).y).isPositive();
    }
}

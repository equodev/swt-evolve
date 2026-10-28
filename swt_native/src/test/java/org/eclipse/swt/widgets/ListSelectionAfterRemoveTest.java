package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** Removing items drops the removed ones from a List's selection and shifts the rest, as a native List does. */
@ExtendWith(Mocks.class)
class ListSelectionAfterRemoveTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUpBridge() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDownBridge() {
        FlutterBridge.set(null);
    }

    private static List list(int style, int items) {
        List list = new List(new Composite(Mocks.shell(), SWT.NONE), style);
        for (int i = 0; i < items; i++)
            list.add("item " + i);
        return list;
    }

    private static VList value(List list) {
        return ((DartList) list.getImpl()).getValue();
    }

    private static void assertNothingSelected(List list) {
        assertThat(list.getSelectionIndex()).isEqualTo(-1);
        assertThat(list.getSelectionIndices()).isEmpty();
        assertThat(list.getSelectionCount()).isZero();
        assertThat(list.getSelection()).isEmpty();
    }

    @Test
    @DisplayName("removeAll() leaves nothing selected and tells the client the selection changed")
    void removeAllClearsTheSelection() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(0);
        value(list).clearDirty();

        list.removeAll();

        assertNothingSelected(list);
        assertThat(value(list).changedKeys()).contains(VList.SELECTION);
    }

    @Test
    @DisplayName("removing the selected first item leaves nothing selected")
    void removeSelectedFirstItem() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(0);

        list.remove(0);

        assertNothingSelected(list);
    }

    @Test
    @DisplayName("removing the selected last item leaves nothing selected and tells the client")
    void removeSelectedLastItem() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(2);
        value(list).clearDirty();

        list.remove(2);

        assertNothingSelected(list);
        assertThat(value(list).changedKeys()).contains(VList.SELECTION);
    }

    @Test
    @DisplayName("remove(String) of the selected last item leaves nothing selected")
    void removeSelectedLastItemByString() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(2);

        list.remove("item 2");

        assertNothingSelected(list);
    }

    @Test
    @DisplayName("removing an item above the selection shifts it up and tells the client")
    void removeItemAboveTheSelection() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(2);
        value(list).clearDirty();

        list.remove(0);

        assertThat(list.getSelectionIndices()).containsExactly(1);
        assertThat(list.getSelection()).containsExactly("item 2");
        assertThat(value(list).changedKeys()).contains(VList.SELECTION);
    }

    @Test
    @DisplayName("remove(start, end) up to the last item drops the selected ones in the range")
    void removeRangeToTheEnd() {
        List list = list(SWT.MULTI, 6);
        list.setSelection(new int[] { 1, 4, 5 });

        list.remove(3, 5);

        assertThat(list.getSelectionIndices()).containsExactly(1);
        assertThat(list.getSelection()).containsExactly("item 1");
    }

    @Test
    @DisplayName("remove(int[]) drops the selected items it names, the last one included")
    void removeIndices() {
        List list = list(SWT.MULTI, 5);
        list.setSelection(new int[] { 1, 3, 4 });

        list.remove(new int[] { 1, 4 });

        assertThat(list.getSelectionIndices()).containsExactly(2);
        assertThat(list.getSelection()).containsExactly("item 3");
    }

    @Test
    @DisplayName("adding an item above the selection shifts it down and tells the client")
    void addItemAboveTheSelection() {
        List list = list(SWT.SINGLE, 3);
        list.setSelection(1);
        value(list).clearDirty();

        list.add("new", 0);

        assertThat(list.getSelectionIndices()).containsExactly(2);
        assertThat(list.getSelection()).containsExactly("item 1");
        assertThat(value(list).changedKeys()).contains(VList.SELECTION);
    }
}

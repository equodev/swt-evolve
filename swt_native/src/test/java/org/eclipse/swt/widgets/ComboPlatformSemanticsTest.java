package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * Where the natives differ, a Combo does what the SWT of the platform it runs on does. Removing the selected item
 * is the visible case: Cocoa's pop-up button hands the selection to the first item and its combo box keeps the
 * index; GTK and Win32 deselect and empty the text. The macOS tests run on the macOS builds and the others on
 * the Linux and Windows ones.
 */
@Tag("native-unit")
class ComboPlatformSemanticsTest {

    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        shell = DartMocks.dartShell(display);
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private Combo abc(int style, int selected) {
        Combo combo = new Combo(shell, style);
        combo.setItems("a", "bb", "ccc");
        if (selected != -1)
            combo.select(selected);
        return combo;
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("on Cocoa, removing the selected item of a read-only combo selects the first item, silently")
    void cocoaReadOnlyRemoveSelectsTheFirstItem() {
        Combo combo = abc(SWT.READ_ONLY, 2);
        List<Event> modified = new ArrayList<>();
        combo.addListener(SWT.Modify, modified::add);

        combo.remove(2);

        assertThat(combo.getSelectionIndex()).isZero();
        assertThat(combo.getText()).isEqualTo("a");
        assertThat(modified).isEmpty();
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("on Cocoa, removing the selected item of an editable combo keeps its index and its text, silently")
    void cocoaEditableRemoveKeepsTheIndex() {
        Combo combo = abc(SWT.NONE, 1);
        List<Event> modified = new ArrayList<>();
        combo.addListener(SWT.Modify, modified::add);

        combo.remove(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(combo.getText()).isEqualTo("bb");
        assertThat(modified).isEmpty();
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("on Cocoa, deselecting an item of an editable combo keeps its text")
    void cocoaEditableDeselectKeepsTheText() {
        Combo combo = abc(SWT.NONE, 1);

        combo.deselect(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
        assertThat(combo.getText()).isEqualTo("bb");
    }

    @Test
    @EnabledOnOs(OS.MAC)
    @DisplayName("on Cocoa, a text change leaves the selection unset, the caret at the end of the text and at 0")
    void cocoaTextChangeLeavesTheCaretAtTheEnd() {
        Combo combo = new Combo(shell, SWT.NONE);
        combo.setText("hello");
        combo.setSelection(new Point(1, 3));

        combo.setText("hi");

        assertThat(combo.getSelection()).isEqualTo(new Point(2, 2));
        assertThat(combo.getCaretPosition()).isZero();
    }

    @Test
    @DisabledOnOs(OS.MAC)
    @DisplayName("on GTK and Win32, removing the selected item deselects it, empties the text and tells the listeners")
    void removeDeselectsAndEmptiesTheText() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 1);
            List<Event> modified = new ArrayList<>();
            combo.addListener(SWT.Modify, modified::add);

            combo.remove(1);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(-1);
            assertThat(combo.getText()).as("style %d", style).isEmpty();
            assertThat(modified).as("style %d", style).hasSize(1);
        }
    }

    @Test
    @DisabledOnOs(OS.MAC)
    @DisplayName("on GTK and Win32, removing an item before the selection moves the index along with its item")
    void removeBeforeTheSelectionMovesTheIndex() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 2);

            combo.remove(0);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(1);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("ccc");
        }
    }

    @Test
    @DisabledOnOs(OS.MAC)
    @DisplayName("on GTK and Win32, deselecting an item of an editable combo empties its text")
    void deselectEmptiesTheText() {
        Combo combo = abc(SWT.NONE, 1);

        combo.deselect(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
        assertThat(combo.getText()).isEmpty();
    }

    @Test
    @DisabledOnOs(OS.MAC)
    @DisplayName("on GTK and Win32, a text change puts the selection back at the start")
    void textChangePutsTheSelectionAtTheStart() {
        Combo combo = new Combo(shell, SWT.NONE);
        combo.setText("hello");
        combo.setSelection(new Point(1, 3));

        combo.setText("hi");

        assertThat(combo.getSelection()).isEqualTo(new Point(0, 0));
        assertThat(combo.getCaretPosition()).isZero();
    }

    @Test
    @DisplayName("a new combo shows at least five items at once, and GTK always ten")
    void newComboShowsSeveralItems() {
        Combo combo = new Combo(shell, SWT.NONE);

        assertThat(combo.getVisibleItemCount()).isGreaterThanOrEqualTo(5);
        if ("gtk".equals(SWT.getPlatform()))
            assertThat(combo.getVisibleItemCount()).isEqualTo(10);
    }
}

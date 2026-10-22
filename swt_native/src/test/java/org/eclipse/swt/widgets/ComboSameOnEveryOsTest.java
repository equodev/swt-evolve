package org.eclipse.swt.widgets;

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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * A Combo behaves the same on every OS, whatever the native combo of the host does. Where the natives disagree it
 * does what two of them do: removing or deselecting the selected item empties the text, so does deselecting all,
 * replacing the list deselects, a text change puts the selection back at the start and deselects the item, the
 * selected item stays selected when one goes in before it, clearing the selection collapses it onto its end, a
 * read-only combo's text is set without asking the Verify listeners, and a read-only combo answers the visible item
 * count it was given. Where all three differ it does what Win32 does: a replaced selected item shows its new text.
 */
@Tag("native-unit")
class ComboSameOnEveryOsTest {

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
    @DisplayName("removing the selected item deselects it, empties the text and tells the listeners")
    void removeDeselectsAndEmptiesTheText() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE, SWT.READ_ONLY}) {
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
    @DisplayName("removing a selected item whose text is empty deselects it and tells no listener, the text being unchanged")
    void removeOfAnEmptySelectedItemSaysNothing() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = new Combo(shell, style);
            combo.setItems("", "a");
            combo.select(0);
            List<Event> modified = new ArrayList<>();
            combo.addListener(SWT.Modify, modified::add);

            combo.remove(0);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(-1);
            assertThat(combo.getText()).as("style %d", style).isEmpty();
            assertThat(modified).as("style %d", style).isEmpty();
        }
    }

    @Test
    @DisplayName("removing the selected item of an editable combo leaves no selection beyond the emptied text")
    void removeEmptiesTheSelectionInTheText() {
        Combo combo = abc(SWT.NONE, 1);
        combo.setSelection(new Point(1, 2));

        combo.remove(1);

        assertThat(combo.getSelection()).isEqualTo(new Point(0, 0));
        assertThatCode(combo::cut).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("removing an item before the selection moves the index along with its item")
    void removeBeforeTheSelectionMovesTheIndex() {
        for (int style : new int[] {SWT.NONE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 2);

            combo.remove(0);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(1);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("ccc");
        }
    }

    @Test
    @DisplayName("deselecting the selected item of an editable combo empties its text")
    void deselectEmptiesTheText() {
        Combo combo = abc(SWT.NONE, 1);

        combo.deselect(1);

        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
        assertThat(combo.getText()).isEmpty();
        assertThat(combo.getSelection()).isEqualTo(new Point(0, 0));
    }

    @Test
    @DisplayName("deselecting all in an editable combo empties its text, whether or not an item was selected, and says nothing")
    void deselectAllEmptiesTheText() {
        for (int selected : new int[] {1, -1}) {
            Combo combo = abc(SWT.NONE, selected);
            if (selected == -1)
                combo.setText("typed");
            List<Event> modifies = new ArrayList<>();
            combo.addListener(SWT.Modify, modifies::add);

            combo.deselectAll();

            assertThat(combo.getSelectionIndex()).as("selected %d", selected).isEqualTo(-1);
            assertThat(combo.getText()).as("selected %d", selected).isEmpty();
            assertThat(combo.getSelection()).as("selected %d", selected).isEqualTo(new Point(0, 0));
            assertThat(modifies).as("selected %d", selected).isEmpty();
        }
    }

    @Test
    @DisplayName("replacing or emptying the list deselects, and a longer list does not bring the index back")
    void replacingTheListDeselects() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 1);
            combo.setItems("m", "n", "o");
            assertThat(combo.getSelectionIndex()).as("style %d replaced", style).isEqualTo(-1);

            combo = abc(style, 2);
            combo.removeAll();
            combo.setItems("p", "q", "r");
            assertThat(combo.getSelectionIndex()).as("style %d emptied", style).isEqualTo(-1);

            combo = abc(style, 2);
            combo.setItems("x");
            combo.add("y");
            combo.add("z");
            assertThat(combo.getSelectionIndex()).as("style %d grown again", style).isEqualTo(-1);
        }
    }

    @Test
    @DisplayName("a text change puts the selection back at the start, and the caret is at the start of the selection")
    void textChangePutsTheSelectionAtTheStart() {
        Combo combo = new Combo(shell, SWT.NONE);
        combo.setText("hello");
        assertThat(combo.getSelection()).isEqualTo(new Point(0, 0));

        combo.setSelection(new Point(1, 3));
        assertThat(combo.getCaretPosition()).isEqualTo(1);

        combo.setText("hi");
        assertThat(combo.getSelection()).isEqualTo(new Point(0, 0));
        assertThat(combo.getCaretPosition()).isZero();
    }

    @Test
    @DisplayName("a new text deselects the item of an editable combo; selecting an item keeps it selected")
    void newTextDeselects() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE}) {
            Combo combo = abc(style, 1);

            combo.setText("zzz");

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(-1);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("zzz");

            combo.select(2);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(2);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("ccc");
        }
    }

    @Test
    @DisplayName("an item that goes in before the selected one leaves that item selected")
    void insertBeforeTheSelectionKeepsTheItemSelected() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 1);

            combo.add("x", 0);
            combo.add("y", 1);

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(3);
            assertThat(combo.getItem(combo.getSelectionIndex())).as("style %d", style).isEqualTo("bb");
            assertThat(combo.getText()).as("style %d", style).isEqualTo("bb");
        }
    }

    @Test
    @DisplayName("clearing the selection in the text collapses it onto its end")
    void clearSelectionCollapsesOntoTheEnd() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE}) {
            Combo combo = new Combo(shell, style);
            combo.setText("hello");
            combo.setSelection(new Point(1, 3));

            combo.clearSelection();

            assertThat(combo.getSelection()).as("style %d", style).isEqualTo(new Point(3, 3));
            assertThat(combo.getCaretPosition()).as("style %d", style).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("setting the text of a read-only combo selects the item of that name without asking the Verify listeners")
    void readOnlySetTextAsksNoVerifyListener() {
        Combo combo = abc(SWT.READ_ONLY, 0);
        List<Event> verified = new ArrayList<>();
        combo.addListener(SWT.Verify, e -> {
            verified.add(e);
            e.doit = false;
        });

        combo.setText("bb");

        assertThat(verified).isEmpty();
        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(combo.getText()).isEqualTo("bb");
    }

    @Test
    @DisplayName("replacing the selected item keeps it selected and shows its new text")
    void replacingTheSelectedItemShowsItsNewText() {
        for (int style : new int[] {SWT.NONE, SWT.SIMPLE, SWT.READ_ONLY}) {
            Combo combo = abc(style, 1);

            combo.setItem(0, "AA");

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(1);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("bb");

            combo.setItem(1, "NN");

            assertThat(combo.getSelectionIndex()).as("style %d", style).isEqualTo(1);
            assertThat(combo.getText()).as("style %d", style).isEqualTo("NN");
        }
    }

    @Test
    @DisplayName("a read-only combo answers the visible item count it was given, not its item count")
    void readOnlyComboKeepsItsVisibleItemCount() {
        Combo combo = new Combo(shell, SWT.READ_ONLY);
        assertThat(combo.getVisibleItemCount()).isEqualTo(5);
        combo.setItems("a", "b", "c");
        assertThat(combo.getVisibleItemCount()).isEqualTo(5);
        combo.setVisibleItemCount(2);
        assertThat(combo.getVisibleItemCount()).isEqualTo(2);
        combo.setVisibleItemCount(-1);
        assertThat(combo.getVisibleItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("a new editable combo shows five items at once on every OS, whatever the size of the screen")
    void newComboShowsFiveItems() {
        when(display.getBounds()).thenReturn(new Rectangle(0, 0, 1920, 1200));
        try {
            for (int style : new int[] {SWT.NONE, SWT.SIMPLE}) {
                Combo combo = new Combo(shell, style);

                assertThat(combo.getVisibleItemCount()).as("style %d", style).isEqualTo(5);
            }
        } finally {
            when(display.getBounds()).thenReturn(null);
        }
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/** What a List does with a key no listener vetoed: Enter is the default selection, a letter selects the item it starts. */
@Tag("native-unit")
class ListKeysNativeTest {

    private Display display;
    private final java.util.List<String> heard = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private List list(int style) {
        List list = new List(DartMocks.dartShell(display), style);
        list.setBounds(0, 0, 200, 100);
        list.setItems(new String[] {"alpha", "bravo", "charlie", "delta", "dog"});
        list.addListener(SWT.Selection, e -> heard.add("Selection " + list.getSelectionIndex()));
        list.addListener(SWT.DefaultSelection, e -> heard.add("DefaultSelection index=" + e.index + " text=" + e.text));
        return list;
    }

    private static void key(List list, int keyCode, char character, int time) {
        key(list, keyCode, character, time, 0);
    }

    private static void key(List list, int keyCode, char character, int time, int stateMask) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.character = character;
        key.time = time;
        key.stateMask = stateMask;
        ListHelper.moveSelectionByKey((DartList) list.getImpl(), key);
    }

    @Test
    @DisplayName("the keypad's Enter is the DefaultSelection too")
    void keypadEnterIsTheDefaultSelection() {
        List list = list(SWT.SINGLE);
        list.setSelection(2);

        key(list, SWT.KEYPAD_CR, '\r', 0);

        assertThat(heard).containsExactly("DefaultSelection index=0 text=null");
    }

    @Test
    @DisplayName("a letter typed with Shift still searches; with Ctrl it does not")
    void shiftStillSearchesCtrlDoesNot() {
        List list = list(SWT.SINGLE);
        list.setSelection(0);

        key(list, 'c', 'C', 100, SWT.SHIFT);
        key(list, 'b', (char) 2, 5000, SWT.CTRL);

        assertThat(heard).containsExactly("Selection 2");
    }

    @Test
    @DisplayName("the search wraps past the last item, and a letter no item starts selects nothing")
    void theSearchWrapsAndANoMatchSelectsNothing() {
        List list = list(SWT.SINGLE);
        list.setSelection(4);

        key(list, 'a', 'a', 100);
        key(list, 'z', 'z', 5000);

        assertThat(heard).containsExactly("Selection 0");
        assertThat(list.getSelectionIndex()).isZero();
    }

    @Test
    @DisplayName("Enter on a selected item is its DefaultSelection, with no index or text, as Win32 and Cocoa report it")
    void enterIsTheDefaultSelection() {
        List list = list(SWT.SINGLE);
        list.setSelection(1);

        key(list, SWT.CR, '\r', 0);

        assertThat(heard).containsExactly("DefaultSelection index=0 text=null");
        assertThat(list.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enter with nothing selected is no default selection")
    void enterWithNoSelectionIsNothing() {
        List list = list(SWT.SINGLE);

        key(list, SWT.CR, '\r', 0);

        assertThat(heard).isEmpty();
    }

    @Test
    @DisplayName("a letter selects the next item it starts; typed again it steps on, and more letters extend the text")
    void aLetterSelectsTheItemItStarts() {
        List list = list(SWT.SINGLE);
        list.setSelection(0);

        key(list, 'd', 'd', 100);
        key(list, 'd', 'd', 200);
        key(list, 'b', 'b', 5000);
        key(list, 'r', 'r', 5100);

        assertThat(heard).containsExactly("Selection 3", "Selection 4", "Selection 1");
    }

    @Test
    @DisplayName("a NO_SEARCH list selects nothing by typing")
    void noSearchSelectsNothingByTyping() {
        List list = list(SWT.SINGLE | TableHelper.NO_SEARCH);
        list.setSelection(0);

        key(list, 'd', 'd', 100);

        assertThat(heard).isEmpty();
        assertThat(list.getSelectionIndex()).isZero();
    }
}

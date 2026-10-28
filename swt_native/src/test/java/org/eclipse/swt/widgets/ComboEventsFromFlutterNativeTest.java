package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the events a Combo receives from Flutter do to it. Each hops through {@code asyncExec}, so by the time it
 * runs the combo may be gone or a modal shell may have opened. A Modify is the text the user typed: the Verify
 * listeners ruled on it already, so it is not put to them again. A Selection is an item picked from the list.
 */
@Tag("native-unit")
class ComboEventsFromFlutterNativeTest extends EventsFromFlutterNativeTestBase {

    private void fromFlutter(Combo combo, String name, String event, String text) {
        Event e = new Event();
        e.text = text;
        fromFlutter(combo, name + "/" + event, e);
    }

    private Combo combo(int style, String... items) {
        Combo combo = new Combo(DartMocks.dartShell(display), style);
        if (items.length > 0)
            combo.setItems(items);
        return combo;
    }

    @Test
    @DisplayName("a Modify from Flutter sets the text and tells the Modify listeners, without asking Verify again")
    void modifySetsTheTextWithoutAskingVerifyAgain() {
        Combo combo = combo(SWT.NONE);
        List<Event> modified = record(combo, SWT.Modify);
        List<Event> verified = record(combo, SWT.Verify);

        fromFlutter(combo, "Modify", "Modify", "typed");
        runQueued();

        assertThat(combo.getText()).isEqualTo("typed");
        assertThat(modified).hasSize(1);
        assertThat(verified).isEmpty();
    }

    @Test
    @DisplayName("a Modify from Flutter is cut to the text limit")
    void modifyHonoursTheTextLimit() {
        Combo combo = combo(SWT.NONE);
        combo.setTextLimit(3);

        fromFlutter(combo, "Modify", "Modify", "typed");
        runQueued();

        assertThat(combo.getText()).isEqualTo("typ");
    }

    @Test
    @DisplayName("a Modify with no text is refused")
    void modifyWithoutTextIsRefused() {
        Combo combo = combo(SWT.NONE);
        combo.setText("kept");

        fromFlutter(combo, "Modify", "Modify", null);

        assertThatThrownBy(this::runQueued).isInstanceOf(IllegalArgumentException.class);
        assertThat(combo.getText()).isEqualTo("kept");
    }

    @Test
    @DisplayName("a Modify that arrives after the combo was disposed is dropped")
    void modifyToADisposedComboIsDropped() {
        Combo combo = combo(SWT.NONE);
        List<Event> modified = record(combo, SWT.Modify);

        fromFlutter(combo, "Modify", "Modify", "typed");
        combo.dispose();
        runQueued();

        assertThat(modified).isEmpty();
    }

    @Test
    @DisplayName("a Verify from Flutter reaches the Verify listeners of a live combo")
    void verifyReachesTheListeners() {
        Combo combo = combo(SWT.NONE);
        List<Event> verified = record(combo, SWT.Verify);

        fromFlutter(combo, "Verify", "Verify", "x");
        runQueued();

        assertThat(verified).hasSize(1);
        assertThat(verified.get(0).text).isEqualTo("x");
    }

    @Test
    @DisplayName("a Verify that arrives after the combo was disposed is dropped")
    void verifyToADisposedComboIsDropped() {
        Combo combo = combo(SWT.NONE);
        List<Event> verified = record(combo, SWT.Verify);

        fromFlutter(combo, "Verify", "Verify", "x");
        combo.dispose();
        runQueued();

        assertThat(verified).isEmpty();
    }

    @Test
    @DisplayName("a Segment from Flutter reaches the Segments listeners of a live combo")
    void segmentReachesTheListeners() {
        Combo combo = combo(SWT.NONE);
        List<Event> segments = record(combo, SWT.Segments);

        fromFlutter(combo, "Segment", "Segments", "text");
        runQueued();

        assertThat(segments).hasSize(1);
    }

    @Test
    @DisplayName("a Segment that arrives after the combo was disposed is dropped")
    void segmentToADisposedComboIsDropped() {
        Combo combo = combo(SWT.NONE);
        List<Event> segments = record(combo, SWT.Segments);

        fromFlutter(combo, "Segment", "Segments", "text");
        combo.dispose();
        runQueued();

        assertThat(segments).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches the listeners of a live combo that nothing blocks")
    void defaultSelectionReachesListeners() {
        Combo combo = combo(SWT.NONE);
        List<Event> events = record(combo, SWT.DefaultSelection);

        fromFlutter(combo, "Selection", "DefaultSelection", null);
        runQueued();

        assertThat(events).hasSize(1);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the combo was disposed is dropped")
    void defaultSelectionToADisposedComboIsDropped() {
        Combo combo = combo(SWT.NONE);
        List<Event> events = record(combo, SWT.DefaultSelection);

        fromFlutter(combo, "Selection", "DefaultSelection", null);
        combo.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the combo's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Combo combo = new Combo(shell, SWT.NONE);
        List<Event> events = record(combo, SWT.DefaultSelection);

        fromFlutter(combo, "Selection", "DefaultSelection", null);
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("an item picked from the list becomes the text and the selection, and Modify comes before Selection")
    void pickedItemIsSelected() {
        Combo combo = combo(SWT.NONE, "a", "bb", "ccc");
        List<Integer> seen = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> seen.add(SWT.Modify));
        combo.addListener(SWT.Selection, e -> seen.add(SWT.Selection));

        fromFlutter(combo, "Selection", "Selection", "bb");
        runQueued();

        assertThat(combo.getText()).isEqualTo("bb");
        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(seen).containsExactly(SWT.Modify, SWT.Selection);
    }

    @Test
    @DisplayName("picking the item that is already selected keeps the selection")
    void pickingTheSelectedItemAgain() {
        Combo combo = combo(SWT.NONE, "a", "bb", "ccc");
        combo.select(1);

        fromFlutter(combo, "Selection", "Selection", "bb");
        runQueued();

        assertThat(combo.getSelectionIndex()).isEqualTo(1);
        assertThat(combo.getText()).isEqualTo("bb");
    }

    @Test
    @DisplayName("a picked text that is no item changes the text and leaves the selection")
    void pickedTextThatIsNoItem() {
        Combo combo = combo(SWT.NONE, "a", "bb");

        fromFlutter(combo, "Selection", "Selection", "zzz");
        runQueued();

        assertThat(combo.getText()).isEqualTo("zzz");
        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("a pick without text only tells the listeners")
    void pickWithoutText() {
        Combo combo = combo(SWT.NONE, "a", "bb");
        combo.select(0);
        List<Event> selected = record(combo, SWT.Selection);

        fromFlutter(combo, "Selection", "Selection", null);
        runQueued();

        assertThat(combo.getText()).isEqualTo("a");
        assertThat(selected).hasSize(1);
    }

    @Test
    @DisplayName("a pick that arrives after the combo was disposed is dropped")
    void pickToADisposedComboIsDropped() {
        Combo combo = combo(SWT.NONE, "a", "bb");
        List<Event> selected = record(combo, SWT.Selection);

        fromFlutter(combo, "Selection", "Selection", "bb");
        combo.dispose();
        runQueued();

        assertThat(selected).isEmpty();
    }

    @Test
    @DisplayName("a Modify listener that disposes the combo ends a pick before Selection")
    void pickEndsWhenAModifyListenerDisposesTheCombo() {
        Combo combo = combo(SWT.NONE, "a", "bb");
        List<Event> selected = record(combo, SWT.Selection);
        combo.addListener(SWT.Modify, e -> combo.dispose());

        fromFlutter(combo, "Selection", "Selection", "bb");
        runQueued();

        assertThat(combo.isDisposed()).isTrue();
        assertThat(selected).isEmpty();
    }
}

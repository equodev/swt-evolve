package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A partial update carries only the properties it names, so the client keeps whatever it last heard about the
 * rest. A read-only combo shows its selected item, so whatever moves its selection or edits its items changes the
 * text it shows, and the update has to say so: otherwise the client goes on showing the old item.
 */
@ExtendWith(Mocks.class)
class ComboUpdateTest {

    /** Delivery is recorded against a client, so a test that sends has to name one. */
    private static final int CLIENT = 1;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    /** A combo the client has seen whole, with items "a", "bb", "ccc" and the item at {@code selected} selected. */
    private Combo delivered(int style, int selected) {
        Combo combo = new Combo(Mocks.shell(), style);
        combo.setItems(new String[] {"a", "bb", "ccc"});
        if (selected != -1)
            combo.select(selected);
        WireFrames.record((DartWidget) combo.getImpl(), CLIENT);
        return combo;
    }

    /** The properties the next update for the client names, by name; empty when there is nothing to send. */
    private Map<String, Object> update(Combo combo) {
        return changeOf((DartWidget) combo.getImpl());
    }

    /**
     * What changed in {@code impl} since the client last heard of it, as its record names it; empty
     * when nothing did. A widget with nothing changed is described whole on request, which says
     * nothing about what an update names, so only a changed one is asked for, and it has to come back
     * as a change: a whole record would hold every property, named or not.
     */
    private static Map<String, Object> changeOf(DartWidget impl) {
        if (!impl.getValue().anyDirty()) return Map.of();
        Map<String, Object> update = WireFrames.record(impl, CLIENT);
        if (update == null) return Map.of();
        if (!update.containsKey("_d")) throw new AssertionError("described whole, not as a change: " + update);
        return update;
    }

    @Test
    @DisplayName("selecting an item from code tells the client the text a read-only combo shows")
    void selectNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, -1);

        combo.select(2);

        assertThat(update(combo)).as("the client shows the text, not the index").containsEntry("text", "ccc");
    }

    @Test
    @DisplayName("moving the selection of a read-only combo from one item to another names the new text")
    void selectingAnotherItemNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 0);

        combo.select(1);

        assertThat(update(combo)).containsEntry("text", "bb");
    }

    @Test
    @DisplayName("deselecting names the empty text of a read-only combo")
    void deselectNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);
        combo.deselect(1);
        assertThat(update(combo)).containsKey("text");

        combo = delivered(SWT.READ_ONLY, 1);
        combo.deselectAll();
        assertThat(update(combo)).containsKey("text");
    }

    @Test
    @DisplayName("emptying the list names the empty text of a read-only combo")
    void removeAllNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);
        combo.removeAll();
        assertThat(update(combo)).containsKey("text");

        combo = delivered(SWT.READ_ONLY, 1);
        combo.setItems(new String[] {"m", "n"});
        assertThat(update(combo)).containsKey("text");
    }

    @Test
    @DisplayName("replacing the selected item names the new text of a read-only combo")
    void setItemNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.setItem(1, "NN");

        assertThat(update(combo)).containsEntry("text", "NN");
    }

    @Test
    @DisplayName("replacing another item leaves the text of a read-only combo out of the update")
    void setItemOfAnotherItemLeavesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.setItem(0, "AA");

        assertThat(update(combo)).doesNotContainKey("text");
    }

    @Test
    @DisplayName("selecting the item that is already selected is no update of the text")
    void reselectingSaysNothingAboutTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.select(1);

        assertThat(update(combo)).doesNotContainKey("text");
    }

    @Test
    @DisplayName("removing the selected item names the text of a read-only combo")
    void removeNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.remove(1);

        assertThat(update(combo)).containsKey("text");
    }

    @Test
    @DisplayName("setting the text of an editable combo, or selecting an item into it, names the text")
    void editableTextIsNamed() {
        Combo combo = delivered(SWT.NONE, -1);
        combo.setText("typed");
        assertThat(update(combo)).containsEntry("text", "typed");

        combo = delivered(SWT.NONE, -1);
        combo.select(2);
        assertThat(update(combo)).containsEntry("text", "ccc");
    }

    @Test
    @DisplayName("setting the text an editable combo already shows is no update of the text")
    void sameTextIsNoUpdate() {
        Combo combo = delivered(SWT.NONE, 1);

        combo.setText("bb");

        assertThat(update(combo)).doesNotContainKey("text");
    }

    @Test
    @DisplayName("the list a combo was given is its own: changing the array afterwards changes nothing")
    void setItemsKeepsItsOwnCopy() {
        String[] given = {"a", "b"};
        Combo combo = new Combo(Mocks.shell(), SWT.NONE);
        combo.setItems(given);

        given[0] = "changed";

        assertThat(combo.getItems()).containsExactly("a", "b");
    }
}

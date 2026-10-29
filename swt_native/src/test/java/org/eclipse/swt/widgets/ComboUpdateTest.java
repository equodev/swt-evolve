package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;

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

    private final Serializer serializer = new Serializer();

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
        try {
            serializer.to(combo);
            Serializer.markDelivered();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
        return combo;
    }

    private String update(Combo combo) {
        byte[] diff = serializer.toDiff((DartWidget) combo.getImpl());
        return diff == null ? "" : new String(diff, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("selecting an item from code tells the client the text a read-only combo shows")
    void selectNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, -1);

        combo.select(2);

        assertThat(update(combo)).as("the client shows the text, not the index").contains("\"text\"").contains("ccc");
    }

    @Test
    @DisplayName("moving the selection of a read-only combo from one item to another names the new text")
    void selectingAnotherItemNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 0);

        combo.select(1);

        assertThat(update(combo)).contains("\"text\"").contains("bb");
    }

    @Test
    @DisplayName("deselecting names the empty text of a read-only combo")
    void deselectNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);
        combo.deselect(1);
        assertThat(update(combo)).contains("\"text\"");

        combo = delivered(SWT.READ_ONLY, 1);
        combo.deselectAll();
        assertThat(update(combo)).contains("\"text\"");
    }

    @Test
    @DisplayName("emptying the list names the empty text of a read-only combo")
    void removeAllNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);
        combo.removeAll();
        assertThat(update(combo)).contains("\"text\"");

        combo = delivered(SWT.READ_ONLY, 1);
        combo.setItems(new String[] {"m", "n"});
        assertThat(update(combo)).contains("\"text\"");
    }

    @Test
    @DisplayName("replacing the selected item names the new text of a read-only combo")
    void setItemNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.setItem(1, "NN");

        assertThat(update(combo)).contains("\"text\"").contains("NN");
    }

    @Test
    @DisplayName("replacing another item leaves the text of a read-only combo out of the update")
    void setItemOfAnotherItemLeavesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.setItem(0, "AA");

        assertThat(update(combo)).doesNotContain("\"text\"");
    }

    @Test
    @DisplayName("selecting the item that is already selected is no update of the text")
    void reselectingSaysNothingAboutTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.select(1);

        assertThat(update(combo)).doesNotContain("\"text\"");
    }

    @Test
    @DisplayName("removing the selected item names the text of a read-only combo")
    void removeNamesTheText() {
        Combo combo = delivered(SWT.READ_ONLY, 1);

        combo.remove(1);

        assertThat(update(combo)).contains("\"text\"");
    }

    @Test
    @DisplayName("setting the text of an editable combo, or selecting an item into it, names the text")
    void editableTextIsNamed() {
        Combo combo = delivered(SWT.NONE, -1);
        combo.setText("typed");
        assertThat(update(combo)).contains("\"text\"").contains("typed");

        combo = delivered(SWT.NONE, -1);
        combo.select(2);
        assertThat(update(combo)).contains("\"text\"").contains("ccc");
    }

    @Test
    @DisplayName("setting the text an editable combo already shows is no update of the text")
    void sameTextIsNoUpdate() {
        Combo combo = delivered(SWT.NONE, 1);

        combo.setText("bb");

        assertThat(update(combo)).doesNotContain("\"text\"");
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

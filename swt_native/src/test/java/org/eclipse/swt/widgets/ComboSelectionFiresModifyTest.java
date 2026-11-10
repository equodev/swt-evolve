package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Picking an item from a Combo's list must fire {@code SWT.Modify} as well as {@code SWT.Selection},
 * and Modify first — the contract every native backend implements: {@code SwtCombo.sendSelection()}
 * on cocoa is literally {@code sendEvent(SWT.Modify)} then {@code sendSelectionEvent(SWT.Selection)},
 * and gtk's {@code gtk_changed} / win32's {@code WM_LBUTTONDOWN} do the same in that order.
 *
 * <p>A dialog that revalidates from {@code Combo.addModifyListener} therefore never revalidates when
 * the user changes the combo: it stays on the previous validation result until some unrelated widget
 * fires an event of its own.
 *
 * <p>Uses a {@link RecordingBridge} whose {@code RecordingComm} captures the {@code comm().on(...)}
 * handler DartCombo registers, so the Dart&rarr;Java Selection can be fired directly. The mocked
 * Display's {@code sendEvent} is stubbed to deliver to the widget's EventTable and {@code asyncExec}
 * to run inline, so the hook's dispatch hop completes synchronously.
 */
@ExtendWith(Mocks.class)
class ComboSelectionFiresModifyTest {

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    /** A file-size unit combo sitting on "MB", the shape the reported dialog uses. */
    private Combo sizeUnitCombo() {
        return sizeUnitCombo(SWT.READ_ONLY);
    }

    private Combo sizeUnitCombo(int style) {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            Event event = inv.getArgument(1);
            if (table != null)
                table.sendEvent(event);
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        // The Selection hook hops through asyncExec; run it inline.
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));

        Combo combo = new Combo(shell, style);
        combo.setItems(new String[] {"B", "KB", "MB", "GB"});
        combo.select(2);
        return combo;
    }

    /** Records event types in arrival order; attached after setup so construction noise stays out. */
    private static List<Integer> recordEvents(Combo combo) {
        List<Integer> seen = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> seen.add(SWT.Modify));
        combo.addListener(SWT.Selection, e -> seen.add(SWT.Selection));
        return seen;
    }

    private void pickFromTheList(String item) {
        Event e = new Event();
        e.text = item;
        bridge.comm.fireContaining("/Selection/Selection", e);
    }

    @Test
    @DisplayName("picking a list item fires Modify before Selection")
    void pickFiresModifyThenSelection() {
        Combo combo = sizeUnitCombo();
        List<Integer> seen = recordEvents(combo);

        pickFromTheList("GB");

        assertThat(seen)
                .as("a list pick is a text change too — SwtCombo.sendSelection() sends Modify, " +
                        "then Selection; a ModifyListener-driven validation never runs without it")
                .containsExactly(SWT.Modify, SWT.Selection);
    }

    @Test
    @DisplayName("the new item is already current when Modify runs")
    void modifyListenerSeesTheNewValue() {
        Combo combo = sizeUnitCombo();
        List<String> textAtModify = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> textAtModify.add(combo.getText()));

        pickFromTheList("GB");

        assertThat(textAtModify)
                .as("a ModifyListener reads Combo.getText() to revalidate, so the pick must " +
                        "already be applied when it runs")
                .containsExactly("GB");
        assertThat(combo.getText()).as("and the pick stays applied afterwards").isEqualTo("GB");
    }

    @Test
    @DisplayName("an editable combo asks the Verify listeners about the picked item replacing its text, before Modify")
    void editablePickVerifiesTheWholeTextFirst() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        List<String> seen = new ArrayList<>();
        combo.addListener(SWT.Verify, e -> seen.add("Verify " + e.text + " " + e.start + ".." + e.end));
        combo.addListener(SWT.Modify, e -> seen.add("Modify"));
        combo.addListener(SWT.Selection, e -> seen.add("Selection"));

        pickFromTheList("GB");

        assertThat(seen)
                .as("Win32, GTK and Cocoa put a pick into an editable combo's field through Verify, which " +
                        "replaces the whole of the old text")
                .containsExactly("Verify GB 0..2", "Modify", "Selection");
    }

    @Test
    @DisplayName("a Verify listener's veto keeps the old text and fires no Modify")
    void editablePickVetoed() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> e.doit = false);
        List<Integer> seen = recordEvents(combo);

        pickFromTheList("GB");

        assertThat(seen).as("a refused text changes nothing").isEmpty();
        assertThat(combo.getText()).isEqualTo("MB");
        assertThat(verdicts()).as("the client, which shows the pick, is told it was refused").containsExactly(false);
    }

    @Test
    @DisplayName("a pick a Verify listener rewrites to no item leaves no item selected")
    void editablePickRewrittenToNoItem() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> e.text = "TB");

        pickFromTheList("GB");

        assertThat(combo.getText()).isEqualTo("TB");
        assertThat(combo.getSelectionIndex()).isEqualTo(-1);
    }

    /** The {@code doit} of each modify verdict sent to the client, in order. */
    private List<Object> verdicts() {
        List<Object> verdicts = new ArrayList<>();
        for (dev.equo.swt.harness.RecordingComm.Frame frame : bridge.comm.sent) {
            if (frame.event.contains("modify/verdict")) verdicts.add(frame.state().get("doit"));
        }
        return verdicts;
    }

    @Test
    @DisplayName("the Selection of a pick carries no text, as no native's does")
    void selectionCarriesNoText() {
        Combo combo = sizeUnitCombo();
        List<String> texts = new ArrayList<>();
        combo.addListener(SWT.Selection, e -> texts.add(String.valueOf(e.text)));

        pickFromTheList("GB");

        assertThat(texts).containsExactly("null");
    }

    private void typeIntoTheField(String newText) {
        Event e = new Event();
        e.text = newText;
        bridge.comm.fireContaining("/Modify/Modify", e);
    }

    @Test
    @DisplayName("a typed edit of an editable combo goes through Verify, naming only what was typed, before Modify")
    void typedEditVerifiesFirst() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        List<String> seen = new ArrayList<>();
        combo.addListener(SWT.Verify, e -> seen.add("Verify " + e.text + " " + e.start + ".." + e.end));
        combo.addListener(SWT.Modify, e -> seen.add("Modify " + combo.getText()));

        typeIntoTheField("MBx");

        assertThat(seen).containsExactly("Verify x 2..2", "Modify MBx");
    }

    @Test
    @DisplayName("a Verify listener that refuses a typed edit keeps the old text")
    void typedEditVetoed() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> e.doit = false);
        List<Integer> seen = recordEvents(combo);

        typeIntoTheField("MBx");

        assertThat(seen).isEmpty();
        assertThat(combo.getText()).isEqualTo("MB");
        assertThat(verdicts()).containsExactly(false);
    }

    @Test
    @DisplayName("a Verify listener that disposes the combo on a pick ends it there: no Modify, no Selection")
    void pickDisposedByVerify() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> combo.dispose());
        List<Integer> seen = recordEvents(combo);

        pickFromTheList("GB");

        assertThat(combo.isDisposed()).isTrue();
        assertThat(seen).isEmpty();
    }

    @Test
    @DisplayName("a Verify listener that disposes the combo on a typed edit ends it there: no Modify")
    void typedEditDisposedByVerify() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> combo.dispose());
        List<Integer> seen = recordEvents(combo);

        typeIntoTheField("MBx");

        assertThat(combo.isDisposed()).isTrue();
        assertThat(seen).isEmpty();
        assertThat(verdicts()).as("a disposed combo has no client to answer").isEmpty();
    }

    @Test
    @DisplayName("a Verify listener that rewrites a typed edit makes the edit what it wrote, and the client is sent it")
    void typedEditRewrittenByVerify() {
        Combo combo = sizeUnitCombo(SWT.DROP_DOWN);
        combo.addListener(SWT.Verify, e -> e.text = e.text.toUpperCase());
        VCombo value = (VCombo) ((DartWidget) combo.getImpl()).getValue();
        value.clearDirty();

        typeIntoTheField("MBx");

        assertThat(combo.getText()).isEqualTo("MBX");
        assertThat(value.changedKeys()).as("the client shows the x it typed, so the X goes back to it").contains(VCombo.TEXT);
    }
}

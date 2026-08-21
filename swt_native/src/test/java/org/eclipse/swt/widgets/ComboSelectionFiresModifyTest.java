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
        Shell shell = Mocks.swtShell();
        Display display = shell.getDisplay();
        SwtDisplay displayImpl = (SwtDisplay) display.getImpl();
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

        Combo combo = new Combo(shell, SWT.READ_ONLY);
        combo.setItems("B", "KB", "MB", "GB");
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
}

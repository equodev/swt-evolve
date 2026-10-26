package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.DartCCombo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/** When a CCombo fires Modify, getText() already returns the new text. */
@ExtendWith(Mocks.class)
class CComboModifyReportsNewTextTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static List<String> textsSeenOnModify(int style, Consumer<CCombo> change) {
        Shell shell = Mocks.shell();
        DartDisplay display = (DartDisplay) shell.getDisplay().getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            if (table != null)
                table.sendEvent(inv.getArgument(1));
            return null;
        }).when(display).sendEvent(any(EventTable.class), any(Event.class));
        CCombo combo = new CCombo(shell, style);
        combo.setItems(new String[] {"Alpha", "Bravo", "Charlie"});
        combo.select(0);
        List<String> seen = new ArrayList<>();
        combo.addListener(SWT.Modify, e -> seen.add(combo.getText()));
        change.accept(combo);
        return seen;
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.BORDER, SWT.BORDER | SWT.READ_ONLY})
    void select(int style) {
        assertThat(textsSeenOnModify(style, c -> c.select(1))).containsExactly("Bravo");
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.BORDER, SWT.BORDER | SWT.READ_ONLY})
    void arrowDown(int style) {
        assertThat(textsSeenOnModify(style, c -> {
            Event down = new Event();
            down.keyCode = SWT.ARROW_DOWN;
            ((DartCCombo) c.getImpl())._text().notifyListeners(SWT.KeyDown, down);
        })).containsExactly("Bravo");
    }

    @Test
    void setTextToAnItem() {
        assertThat(textsSeenOnModify(SWT.BORDER, c -> c.setText("Charlie"))).containsExactly("Charlie");
    }

    @Test
    void setTextToFreeText() {
        assertThat(textsSeenOnModify(SWT.BORDER, c -> c.setText("Bravoz"))).containsExactly("Bravoz");
    }

    @Test
    void clearSelection() {
        assertThat(textsSeenOnModify(SWT.BORDER, c -> c.select(-1))).containsExactly("");
    }
}

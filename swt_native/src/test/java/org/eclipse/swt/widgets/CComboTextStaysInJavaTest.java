package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.DartCCombo;
import org.eclipse.swt.custom.VCCombo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Flutter draws the whole CCombo from its own value. The Text it keeps for SWT's sake (it is the focus
 * control and the source of the field's events) is never rendered, so it is never sent.
 */
@ExtendWith(Mocks.class)
class CComboTextStaysInJavaTest {

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

    private CCombo combo() {
        Shell shell = Mocks.shell();
        DartDisplay display = (DartDisplay) shell.getDisplay().getImpl();
        // Deliver events to the widget's listeners, like the real display does.
        doAnswer(inv -> {
            EventTable table = inv.getArgument(0);
            if (table != null)
                table.sendEvent(inv.getArgument(1));
            return null;
        }).when(display).sendEvent(any(EventTable.class), any(Event.class));
        CCombo combo = new CCombo(shell, SWT.BORDER);
        combo.setItems(new String[] {"Alpha", "Bravo", "Charlie"});
        return combo;
    }

    private static Text innerText(CCombo combo) {
        return ((DartCCombo) combo.getImpl())._text();
    }

    private List<String> sentEvents() {
        return bridge.comm.sent.stream().map(f -> f.event).collect(Collectors.toList());
    }

    @Test
    void theComboSendsNoChildren() {
        CCombo combo = combo();

        assertThat(((VCCombo) ((DartWidget) combo.getImpl()).getValue()).getChildren()).isEmpty();
    }

    @Test
    void changingTheFieldSendsNothingForTheText() {
        CCombo combo = combo();
        FlutterBridge.update().join();
        bridge.comm.sent.clear();

        combo.select(1);
        combo.setText("Bravoz");
        combo.setSelection(new org.eclipse.swt.graphics.Point(0, 2));
        FlutterBridge.update().join();

        List<Object> payloads = BatchEntries.of(bridge.comm.sent).stream().map(GcFrames.Entry::value).collect(Collectors.toList());
        assertThat(payloads).as("the combo still ships its own text").anyMatch(value -> WireValues.containsText(value, "Bravoz"));
        assertThat(sentEvents()).as("nothing goes out on a Text channel").noneMatch(e -> e.startsWith("Text/"));
        assertThat(payloads).as("no payload describes a Text")
                .noneMatch(value -> !WireValues.find(value, m -> "Text".equals(m.get("swt"))).isEmpty());
    }

    @Test
    void changingOnlyTheTextSendsNothing() {
        CCombo combo = combo();
        Text text = innerText(combo);
        FlutterBridge.update().join();
        bridge.comm.sent.clear();

        text.setSelection(1, 3);
        text.setToolTipText("tip");
        FlutterBridge.update().join();
        // What DisplayBridge answers for a key while the field has Java focus.
        FlutterBridge.send((DartWidget) text.getImpl(), dev.equo.swt.WireTable.CHANNEL_KEY_VERDICT, dev.equo.swt.Java8.map("doit", true));

        assertThat(sentEvents()).noneMatch(e -> e.startsWith("Text/"));
    }
}

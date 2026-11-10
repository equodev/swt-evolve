package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

/**
 * What the text a user types into an editable CCombo's field does on the Java side: the CCombo's text
 * becomes it, and its Verify and Modify listeners hear the edit as a native CCombo's Text reports it,
 * the Verify naming only what was typed and where.
 */
@Tag("native-unit")
class CComboTypedEditFromFlutterNativeTest {

    private RecordingBridge bridge;
    private Display display;
    private final List<Runnable> queued = new ArrayList<>();

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = DartMocks.dartDisplay();
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
    }

    @AfterEach
    void tearDown() {
        doNothing().when(display).asyncExec(any(Runnable.class));
        DartDisplay impl = dartDisplay();
        doNothing().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        FlutterBridge.set(null);
    }

    private DartDisplay dartDisplay() {
        return (DartDisplay) display.getImpl();
    }

    private void typed(CCombo combo, String text, int caret) {
        Event event = new Event();
        event.text = text;
        event.start = caret;
        bridge.comm.fireContaining("CCombo/" + combo.hashCode() + "/Modify/Modify", event);
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    @Test
    void typedTextBecomesTheCCombosTextAndIsVerifiedAndModified() {
        CCombo combo = new CCombo(DartMocks.dartShell(display), SWT.BORDER);
        combo.setItems(new String[] {"alpha", "bravo"});
        List<String> heard = new ArrayList<>();
        combo.addListener(SWT.Verify, e -> heard.add("Verify " + e.text + " " + e.start + "-" + e.end));
        combo.addListener(SWT.Modify, e -> heard.add("Modify " + combo.getText()));

        typed(combo, "x", 1);
        typed(combo, "xy", 2);

        assertThat(combo.getText()).isEqualTo("xy");
        assertThat(heard).containsExactly("Verify x 0-0", "Modify x", "Verify y 1-1", "Modify xy");
    }

    @Test
    void anEditWithoutTextChangesNothing() {
        CCombo combo = new CCombo(DartMocks.dartShell(display), SWT.BORDER);
        combo.setText("alpha");
        List<String> heard = new ArrayList<>();
        combo.addListener(SWT.Verify, e -> heard.add("Verify"));
        combo.addListener(SWT.Modify, e -> heard.add("Modify"));

        typed(combo, null, 0);

        assertThat(combo.getText()).isEqualTo("alpha");
        assertThat(heard).isEmpty();
    }

    @Test
    void aVetoedKeystrokeLeavesTheTextAlone() {
        CCombo combo = new CCombo(DartMocks.dartShell(display), SWT.BORDER);
        combo.addListener(SWT.Verify, e -> e.doit = false);

        typed(combo, "x", 1);

        assertThat(combo.getText()).isEmpty();
    }
}

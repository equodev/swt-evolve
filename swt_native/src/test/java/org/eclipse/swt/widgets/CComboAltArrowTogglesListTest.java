package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Alt+Down and Alt+Up open and close a CCombo's list from its field. Flutter draws the list, so the
 * change has to reach it.
 */
@ExtendWith(Mocks.class)
class CComboAltArrowTogglesListTest {

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

    private static CCombo combo() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        CCombo combo = new CCombo(shell, SWT.BORDER);
        combo.setItems(new String[] {"Alpha", "Bravo", "Charlie"});
        combo.select(0);
        return combo;
    }

    /** What {@code DisplayBridge} does with a key while the CCombo's field has Java focus. */
    private static void press(CCombo combo, int keyCode) {
        Event key = new Event();
        key.keyCode = keyCode;
        key.stateMask = SWT.ALT;
        ControlHelper.routeKeyDown((DartControl) ((DartCCombo) combo.getImpl())._text().getImpl(), key);
    }

    private static VCCombo value(CCombo combo) {
        return ((DartCCombo) combo.getImpl()).getValue();
    }

    @Test
    void altDownOpensTheListInFlutter() {
        CCombo combo = combo();
        value(combo).clearDirty();

        press(combo, SWT.ARROW_DOWN);

        assertThat(combo.getListVisible()).isTrue();
        assertThat(value(combo).changedKeys()).contains(VCCombo.LIST_VISIBLE);
        assertThat(combo.getText()).as("the selection does not move").isEqualTo("Alpha");
    }

    @Test
    void altUpClosesTheListInFlutter() {
        CCombo combo = combo();
        press(combo, SWT.ARROW_DOWN);
        value(combo).clearDirty();

        press(combo, SWT.ARROW_UP);

        assertThat(combo.getListVisible()).isFalse();
        assertThat(value(combo).changedKeys()).contains(VCCombo.LIST_VISIBLE);
    }
}

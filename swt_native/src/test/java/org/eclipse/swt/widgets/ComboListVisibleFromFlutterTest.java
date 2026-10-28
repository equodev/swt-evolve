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
 * The drop-down list of a Combo is drawn by Flutter, so Java learns that it opened or closed (F4,
 * Alt+Down, the arrow, Escape, a pick, a click outside) only when Flutter says so.
 */
@ExtendWith(Mocks.class)
class ComboListVisibleFromFlutterTest {

    private RecordingBridge bridge;
    private final List<Runnable> queued = new ArrayList<>();

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

    private Combo combo(int style) {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            queued.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Combo combo = new Combo(shell, style);
        combo.setItems(new String[] {"Alpha", "Bravo", "Charlie"});
        combo.select(0);
        return combo;
    }

    /** What Flutter sends when the user opens or closes the list; Java handles it on the UI thread. */
    private void reportList(Combo combo, boolean shown) {
        Event e = new Event();
        e.detail = shown ? 1 : 0;
        bridge.comm.fireContaining("Combo/" + combo.hashCode() + "/List/Visible", e);
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private void listShownByFlutter(Combo combo, boolean shown) {
        reportList(combo, shown);
        runQueued();
    }

    private static VCombo value(Combo combo) {
        return (VCombo) ((DartWidget) combo.getImpl()).getValue();
    }

    @Test
    @DisplayName("a list Flutter opened or closed is what getListVisible returns")
    void listOpenedByFlutterIsVisible() {
        for (int style : new int[] {SWT.READ_ONLY, SWT.DROP_DOWN}) {
            Combo combo = combo(style);

            listShownByFlutter(combo, true);
            assertThat(combo.getListVisible()).isTrue();

            listShownByFlutter(combo, false);
            assertThat(combo.getListVisible()).isFalse();
        }
    }

    @Test
    @DisplayName("closing from Java a list Flutter opened reaches Flutter")
    void javaClosesListFlutterOpened() {
        Combo combo = combo(SWT.READ_ONLY);
        listShownByFlutter(combo, true);
        value(combo).clearDirty();

        combo.setListVisible(false);

        assertThat(value(combo).changedKeys()).contains(VCombo.LIST_VISIBLE);
    }

    @Test
    @DisplayName("a list report that arrives after the combo was disposed is dropped")
    void listReportToADisposedComboIsDropped() {
        Combo combo = combo(SWT.READ_ONLY);

        reportList(combo, true);
        combo.dispose();
        runQueued();

        assertThat(combo.isDisposed()).isTrue();
        assertThat(((DartCombo) combo.getImpl()).listVisible).isFalse();
    }
}

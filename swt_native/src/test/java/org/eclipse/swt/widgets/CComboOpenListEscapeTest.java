package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.VCCombo;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * The list of a CCombo is drawn by Flutter, so Java learns that it is open only when Flutter says
 * so; with it open, Escape belongs to the list, as it does to a native CCombo's popup list: one
 * Traverse with doit=false on the CCombo, nothing for its parents, and the list closes.
 */
@ExtendWith(Mocks.class)
class CComboOpenListEscapeTest {

    private RecordingBridge bridge;
    private final List<String> events = new ArrayList<>();

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

    private CCombo comboInToolBar() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        DartDisplay displayImpl = (DartDisplay) display.getImpl();
        doAnswer(inv -> inv.callRealMethod()).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        ToolBar toolBar = new ToolBar(shell, SWT.FLAT);
        toolBar.addListener(SWT.Traverse, e -> events.add("toolbar Traverse doit=" + e.doit));
        CCombo combo = new CCombo(toolBar, SWT.BORDER);
        combo.setItems(new String[] {"MD", "TVD", "TVDSS", "TWT"});
        combo.select(1);
        combo.addListener(SWT.Traverse, e -> events.add("ccombo Traverse doit=" + e.doit));
        combo.addListener(SWT.KeyDown, e -> events.add("ccombo KeyDown"));
        return combo;
    }

    private void listShownByFlutter(CCombo combo, boolean shown) {
        Event e = new Event();
        e.detail = shown ? 1 : 0;
        bridge.comm.fireContaining("CCombo/" + combo.hashCode() + "/List/Visible", e);
    }

    /** What {@code DisplayBridge} does with a key while the CCombo's field has Java focus. */
    private static void pressEscape(CCombo combo) throws ReflectiveOperationException {
        Field text = combo.getImpl().getClass().getDeclaredField("text");
        text.setAccessible(true);
        Event key = new Event();
        key.keyCode = SWT.ESC;
        key.character = SWT.ESC;
        ControlHelper.routeKeyDown((DartControl) ((Text) text.get(combo.getImpl())).getImpl(), key);
    }

    @Test
    @DisplayName("a list Flutter opened is visible to getListVisible")
    void listOpenedByFlutterIsVisible() {
        CCombo combo = comboInToolBar();

        listShownByFlutter(combo, true);
        assertThat(combo.getListVisible()).isTrue();

        listShownByFlutter(combo, false);
        assertThat(combo.getListVisible()).isFalse();
    }

    @Test
    @DisplayName("Escape on the open list traverses once, with doit=false, and closes the list")
    void escapeOnOpenListStaysInTheCombo() throws Exception {
        CCombo combo = comboInToolBar();
        listShownByFlutter(combo, true);

        pressEscape(combo);

        assertThat(events).containsExactly("ccombo Traverse doit=false", "ccombo KeyDown");
        assertThat(combo.getListVisible()).isFalse();
        assertThat(((VCCombo) ((DartWidget) combo.getImpl()).getValue()).changedKeys())
                .as("Flutter is told to close its list").contains(VCCombo.LIST_VISIBLE);
    }

    @Test
    @DisplayName("Escape with the list closed still traverses out of the combo")
    void escapeOnClosedListTraverses() throws Exception {
        CCombo combo = comboInToolBar();

        pressEscape(combo);

        assertThat(events).contains("toolbar Traverse doit=true");
    }
}

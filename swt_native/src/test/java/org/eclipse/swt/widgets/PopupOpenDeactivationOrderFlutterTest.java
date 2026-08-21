package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * When a popup shell opens (e.g. Eclipse Quick Access via Ctrl+3), native SWT fires SWT.Deactivate
 * on the workbench BEFORE SWT.Activate on the popup — the OS deactivates the old window first.
 * In web mode both events arrive asynchronously from Flutter after SWT.Activate has already fired,
 * so Eclipse's close-on-workbench-deactivate listener (registered during SWT.Activate) would catch
 * the delayed SWT.Deactivate and immediately dismiss the popup.
 *
 * <p>The fix: DartShell.open() synchronously pre-fires setActiveControl(null) on the previous
 * active shell before sending SWT.Activate, restoring native event ordering.  The async
 * Flutter Shell/Deactivate that would fire the same event a second time is suppressed.
 */
@Tag("flutter-it")
class PopupOpenDeactivationOrderFlutterTest {

    private RecordingBridge bridge;
    private Display display;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll  static void reset()   { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("workbench fires SWT.Deactivate before popup fires SWT.Activate")
    void workbenchDeactivatesBeforePopupActivates() {
        Shell workbench = new Shell(display);
        workbench.open();

        Shell popup = new Shell(display, SWT.NONE);

        List<String> order = new ArrayList<>();
        workbench.addListener(SWT.Deactivate, e -> order.add("workbench-deactivate"));
        popup.addListener(SWT.Activate, e -> order.add("popup-activate"));

        popup.open();

        assertThat(order)
                .as("SWT.Deactivate on workbench must appear before any SWT.Activate on popup")
                .contains("workbench-deactivate", "popup-activate");
        assertThat(order.indexOf("workbench-deactivate"))
                .as("workbench SWT.Deactivate must precede popup SWT.Activate")
                .isLessThan(order.indexOf("popup-activate"));
    }

    @Test
    @DisplayName("async Flutter Shell/Deactivate for workbench is suppressed after popup opens")
    void asyncFlutterDeactivateDoesNotDismissPopupAfterOpen() {
        Shell workbench = new Shell(display);
        workbench.open();

        Shell popup = new Shell(display, SWT.NONE);

        // Simulate Eclipse Quick Access: register a close-on-workbench-deactivate listener
        // during the popup's SWT.Activate — exactly as Quick Access does to self-dismiss on
        // focus loss.  If the async Flutter Shell/Deactivate fires a second SWT.Deactivate
        // on the workbench, this listener would dispose the popup immediately.
        popup.addListener(SWT.Activate, e ->
                workbench.addListener(SWT.Deactivate, ev -> popup.dispose()));

        popup.open();

        // Simulate the async Flutter Shell/Deactivate arriving for the workbench after open().
        // The suppression flag must swallow it so it does not re-fire SWT.Deactivate.
        String channel = FlutterBridge.event((DartWidget) workbench.getImpl(), "Shell", "Deactivate");
        bridge.comm.fireContaining(channel, new Event());
        while (display.readAndDispatch()) {
            // drain queued asyncExec runnables
        }

        assertThat(popup.isDisposed())
                .as("popup must not be disposed by the async Flutter Shell/Deactivate that "
                        + "arrives after open() (the suppression flag must eat it)")
                .isFalse();
    }
}

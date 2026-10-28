package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/**
 * {@code Shell.setActive()} announces the activation before it moves the focus into the shell: a shell that its
 * Activate listener disposes takes no focus.
 *
 * <pre>./gradlew :swt-evolve:swt_native:test --tests '*ShellActivationFlutterTest'</pre>
 */
@Tag("flutter-it")
class ShellActivationFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aShellItsActivateListenerDisposesTakesNoFocus() {
        install(TestWebBridge::new);
        Shell main = new Shell(display);
        main.setSize(400, 300);
        new Text(main, SWT.SINGLE);
        main.open();
        Shell other = new Shell(main);
        Text field = new Text(other, SWT.SINGLE);
        other.setSize(200, 100);
        other.open();
        main.setActive();
        List<String> events = new ArrayList<>();
        other.addListener(SWT.Activate, e -> {
            events.add("Activate");
            other.dispose();
        });
        field.addListener(SWT.FocusIn, e -> events.add("FocusIn"));

        other.setActive();

        assertThat(other.isDisposed()).isTrue();
        assertThat(events).containsExactly("Activate");
    }

    @Test
    void activatingTheMainShellDeactivatesTheDialogFirst() {
        openMainAndDialog();

        main.setActive();

        assertThat(log).containsSubsequence("Dialog.Deactivate", "Main.Activate");
        assertThat(log).containsOnlyOnce("Dialog.Deactivate").doesNotContain("Main.Deactivate");
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    @Test
    void activatingTheDialogAgainDeactivatesTheMainShellFirst() {
        openMainAndDialog();
        main.setActive();
        log.clear();

        dialog.setActive();

        assertThat(log).containsSubsequence("Main.Deactivate", "Dialog.Activate");
        assertThat(log).containsOnlyOnce("Main.Deactivate").doesNotContain("Dialog.Deactivate");
        assertThat(display.getActiveShell()).isSameAs(dialog);
    }

    @ParameterizedTest
    @ValueSource(strings = {"gtk", "win32", "cocoa"})
    void hidingTheActiveDialogHandsActivationBackAfterTheHideOnEveryPlatform(String platform) {
        openMainAndDialog();

        onPlatform(platform, () -> dialog.setVisible(false));

        assertThat(log).containsSubsequence("Dialog.Hide", "Dialog.Deactivate", "Main.Activate");
        assertThat(log).contains("Dialog.FocusOut");
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    @Test
    void hidingTheActiveDialogHandsActivationBackAfterTheHideOnTheWeb() {
        openMainAndDialog();

        dialog.setVisible(false);

        assertThat(log).containsSubsequence("Dialog.Hide", "Dialog.Deactivate", "Main.Activate");
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    @Test
    void showingAHiddenShellAgainAnnouncesNoDeactivate() {
        install(TestWebBridge::new);
        main = new Shell(display);
        main.setSize(400, 300);
        main.open();
        record(main, "Main");
        main.setVisible(false);
        log.clear();

        main.setVisible(true);

        assertThat(log).doesNotContain("Main.Deactivate");
    }

    private static void onPlatform(String platform, Runnable action) {
        try (MockedStatic<SWT> swt = mockStatic(SWT.class, CALLS_REAL_METHODS)) {
            swt.when(SWT::getPlatform).thenReturn(platform);
            action.run();
        }
    }

    @Test
    void closingTheActiveDialogSendsItNoFocusOutAfterItsDispose() {
        openMainAndDialog();
        main.setActive();
        dialog.setActive();
        log.clear();

        dialog.close();

        assertThat(log).contains("Dialog.Dispose", "Main.Activate");
        assertThat(log.subList(log.indexOf("Dialog.Dispose"), log.size())).doesNotContain("Dialog.FocusOut");
        assertThat(display.getActiveShell()).isSameAs(main);
    }

    // ---- two shells -------------------------------------------------------------------------------

    private Shell main;
    private Shell dialog;
    private final List<String> log = new ArrayList<>();

    /** A main shell and a dialog opened over it, which is the active shell. */
    private void openMainAndDialog() {
        install(TestWebBridge::new);
        main = new Shell(display);
        main.setSize(400, 300);
        main.open();
        dialog = new Shell(main, SWT.DIALOG_TRIM);
        dialog.setBounds(400, 100, 200, 120);
        dialog.open();
        assertThat(display.getActiveShell()).as("precondition: the dialog is active").isSameAs(dialog);
        record(main, "Main");
        record(dialog, "Dialog");
    }

    /** Through display filters, which still hear a widget once its Dispose has cleared its listeners. */
    private void record(Shell shell, String name) {
        int[] types = {SWT.Activate, SWT.Deactivate, SWT.FocusIn, SWT.FocusOut, SWT.Hide, SWT.Dispose};
        String[] names = {"Activate", "Deactivate", "FocusIn", "FocusOut", "Hide", "Dispose"};
        for (int i = 0; i < types.length; i++) {
            String event = name + "." + names[i];
            display.addFilter(types[i], e -> {
                if (e.widget == shell)
                    log.add(event);
            });
        }
    }

    // ---- harness ----------------------------------------------------------------------------------

    /** See {@code ControlFocusRequestFlutterTest.install} — a Display whose bridge is the test bridge. */
    private <B extends DisplayBridge> B install(Function<DartDisplay, B> factory) {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        B bridge = factory.apply(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }
}

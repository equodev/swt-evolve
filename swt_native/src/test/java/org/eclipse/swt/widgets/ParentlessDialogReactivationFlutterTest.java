package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * When a parentless dialog shell (created with {@code new Shell(display, style)} rather than
 * {@code new Shell(parentShell, style)}) closes in web mode, DartShell.releaseWidget() must still
 * fire SWT.Activate on the active shell so that Eclipse's ACTIVE_WORKBENCH_WINDOW context variable
 * is restored.  Without this, handlers such as WizardHandler$Import silently return null and
 * File > Import / Export appears to do nothing.
 *
 * <p>In native SWT the OS re-activates the previous window automatically after any dialog closes;
 * in web mode there is no OS-level window activation, so releaseWidget() is the only mechanism.
 */
@Tag("flutter-it")
class ParentlessDialogReactivationFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void parentlessDialog_onClose_reactivatesMainShell() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();

        Shell main = new Shell(display);
        main.open();

        int[] activations = {0};
        main.addListener(SWT.Activate, e -> activations[0]++);

        // Parentless dialog — no parent shell, models a top-level startup dialog (e.g. a login
        // screen or Mylyn notification) that temporarily steals Flutter focus from the workbench.
        Shell dialog = new Shell(display, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        dialog.open();

        int beforeClose = activations[0];

        // Close the dialog.
        dialog.dispose();

        // releaseWidget() queues an asyncExec; drain it.
        while (display.readAndDispatch()) {}

        assertThat(activations[0])
                .as("closing a parentless dialog must re-fire SWT.Activate on the active shell "
                        + "so Eclipse's workbench-window tracking is restored after the dialog closes")
                .isGreaterThan(beforeClose);
    }

    @Test
    void parentedDialog_onClose_reactivatesParentShell() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();

        Shell main = new Shell(display);
        main.open();

        int[] activations = {0};
        main.addListener(SWT.Activate, e -> activations[0]++);

        // Parented dialog — already worked before the fix; kept as a sanity check.
        Shell dialog = new Shell(main, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        dialog.open();

        int beforeClose = activations[0];

        dialog.dispose();
        while (display.readAndDispatch()) {}

        assertThat(activations[0])
                .as("closing a parented dialog must re-fire SWT.Activate on the parent shell")
                .isGreaterThan(beforeClose);
    }
}

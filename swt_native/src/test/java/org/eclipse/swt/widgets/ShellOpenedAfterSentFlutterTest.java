package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A shell can be sent before it is opened: a wizard flushes its shell while it builds its pages.
 * Opening it has to send that it is visible now, or the client keeps it hidden and the application
 * waits on a dialog nobody can see.
 */
@Tag("flutter-it")
class ShellOpenedAfterSentFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void openingAShellSentWhileHiddenSendsThatItIsVisible() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell main = new Shell(display);
        main.open();

        Shell dialog = new Shell(main, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        VWidget value = ((DartShell) dialog.getImpl()).getValue();
        // As if it had been sent hidden.
        value.clearDirty();

        dialog.open();

        assertThat(dialog.getVisible()).isTrue();
        assertThat(value.changedNames()).as("the changes to send once it is open").contains("visible");
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A tooltip or popup shell carries SWT.NO_FOCUS and never becomes the active shell natively -- a
 * window manager does not activate a window that cannot take focus. Returning one is not cosmetic:
 * a dialog parented on Display#getActiveShell() (the JFace pattern behind
 * {@code DialogCellEditor#openDialogBox}) then hangs off a shell that hides itself moments later,
 * and the dialog is disposed along with it -- the editor that opens and closes by itself.
 */
@Tag("flutter-it")
class ActiveShellSkipsNoFocusShellFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void activeShell_skipsATooltipShellShownOverTheApplication() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();

        Shell main = new Shell(display);
        main.open();

        Shell tooltip = new Shell(main, SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);
        tooltip.setBounds(718, 229, 177, 20);
        tooltip.open();

        assertThat(display.getActiveShell())
                .as("a shell that cannot take focus is not the active shell")
                .isSameAs(main);
    }

    @Test
    void activeShell_stillReportsAnOrdinaryShellShownLast() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();

        Shell main = new Shell(display);
        main.open();

        Shell dialog = new Shell(main, SWT.DIALOG_TRIM);
        dialog.open();

        assertThat(display.getActiveShell())
                .as("an ordinary shell opened last is still the active one")
                .isSameAs(dialog);
    }
}

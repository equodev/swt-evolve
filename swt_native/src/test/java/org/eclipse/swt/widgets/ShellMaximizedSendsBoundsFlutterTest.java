package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Maximizing a shell moves it to the display's bounds, and the client has to hear it: the client
 * draws every other top-level shell relative to the main shell's origin, so a main shell the client
 * still holds at its old place puts popups (a content assist list) that far off the caret.
 */
@Tag("flutter-it")
class ShellMaximizedSendsBoundsFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void maximizingASentShellSendsItsNewBounds() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell shell = new Shell(display);
        shell.setBounds(315, 180, 1024, 768);
        shell.open();
        VWidget value = ((DartShell) shell.getImpl()).getValue();
        // As if it had been sent at those bounds.
        value.clearDirty();
        int[] resizes = {0};
        shell.addListener(SWT.Resize, e -> resizes[0]++);

        shell.setMaximized(true);

        Rectangle maximized = display.getBounds();
        assertThat(shell.getBounds()).as("a maximized shell covers the display").isEqualTo(maximized);
        assertThat(maximized).as("the display is not where the shell was").isNotEqualTo(new Rectangle(315, 180, 1024, 768));
        assertThat(value.changedNames()).as("the changes to send once it is maximized").contains("bounds");
        assertThat(resizes[0]).as("Resize events for the new size").isPositive();
    }

    /** As on GTK and Win32, maximizing a full-screen shell leaves it full screen. */
    @Test
    void maximizingAFullScreenShellKeepsItFullScreen() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell shell = new Shell(display);
        shell.setFullScreen(true);

        shell.setMaximized(true);

        assertThat(shell.getFullScreen()).isTrue();
    }
}

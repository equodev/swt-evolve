package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import dev.equo.swt.Config;

/**
 * The whole-tree build renders every Shell through Flutter, so a Shell has no NSWindow — {@code
 * Control.view} is a placeholder whose {@code window()} is nil. Cocoa answers a nil-parent sheet by
 * showing the panel modelessly and returning from {@code runModalForWindow} at once, so a dialog
 * that kept SWT.SHEET reports "cancelled" while the chooser is still on screen and the caller
 * silently drops the path. The two choosers must therefore fall back to the modal path, which is
 * what an absent SHEET bit selects.
 *
 * <p>Only macOS builds a sheet at all; the Windows and Linux choosers are modal either way.
 */
@Tag("native-unit")
@EnabledOnOs(OS.MAC)
class FileDialogSheetStyleNativeTest {

    private static final String WEB_HYBRID = "dev.equo.swt.desktop";

    private static final String LOAD_LIBRARY = "dev.equo.swt.loadLibrary";

    private String previous;
    private String previousLoadLibrary;
    private String previousFileDialogs;

    @BeforeEach
    void routeChoosersToTheOs() {
        // A whole-tree app (DisplayBridge turns WEB_HYBRID off) with the native library opens the OS
        // choosers on request.
        previous = System.getProperty(WEB_HYBRID);
        previousLoadLibrary = System.getProperty(LOAD_LIBRARY);
        previousFileDialogs = System.getProperty(Config.FILE_DIALOGS);
        System.setProperty(WEB_HYBRID, "false");
        System.setProperty(LOAD_LIBRARY, "true");
        System.setProperty(Config.FILE_DIALOGS, "eclipse");
    }

    @AfterEach
    void restore() {
        restore(WEB_HYBRID, previous);
        restore(LOAD_LIBRARY, previousLoadLibrary);
        restore(Config.FILE_DIALOGS, previousFileDialogs);
    }

    private static void restore(String key, String value) {
        if (value == null)
            System.clearProperty(key);
        else
            System.setProperty(key, value);
    }

    @Test
    void fileDialogGivesUpTheSheetItHasNoWindowFor() {
        Shell shell = DartMocks.dartShell();

        FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.SHEET);

        assertThat(dialog.getImpl()).isInstanceOf(SwtFileDialog.class);
        assertThat(dialog.getStyle() & SWT.SHEET).isZero();
    }

    @Test
    void directoryDialogGivesUpTheSheetItHasNoWindowFor() {
        Shell shell = DartMocks.dartShell();

        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);

        assertThat(dialog.getImpl()).isInstanceOf(SwtDirectoryDialog.class);
        assertThat(dialog.getStyle() & SWT.SHEET).isZero();
    }
}

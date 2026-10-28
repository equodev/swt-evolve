package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@link DartDirectoryDialog#open()} answers. No chooser is drawn for it, so it answers as a cancelled
 * chooser does, unless an E2E test names the directory to pick through a system property.
 */
@Tag("native-unit")
class DirectoryDialogOpenNativeTest {

    private static final String PATH = "dev.equo.swt.test.directoryDialog.path";
    private static final String PATH_URI = "dev.equo.swt.test.directoryDialog.path.uri";

    @AfterEach
    void clearProperties() {
        System.clearProperty(PATH);
        System.clearProperty(PATH_URI);
    }

    @Test
    void openAnswersACancelledChooserAndLeavesTheState() {
        DartDirectoryDialog dialog = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null);
        dialog.setFilterPath("/tmp");
        dialog.setMessage("Pick");

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFilterPath()).isEqualTo("/tmp");
        assertThat(dialog.getMessage()).isEqualTo("Pick");
    }

    @Test
    void openOnADisposedParentIsRefused() {
        Shell shell = DartMocks.dartShell();
        DartDirectoryDialog dialog = new DartDirectoryDialog(shell, SWT.SHEET, null);
        when(shell.isDisposed()).thenReturn(true);

        assertThatThrownBy(dialog::open).isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED);
    }

    @Test
    void aDirectoryNamedByTheTestHookIsPicked() {
        System.setProperty(PATH, "/picked/dir");
        DartDirectoryDialog dialog = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null);

        assertThat(dialog.open()).isEqualTo("/picked/dir");
    }

    @Test
    void aFileUriNamedByTheTestHookIsPicked() {
        java.io.File picked = new java.io.File(System.getProperty("java.io.tmpdir"), "from uri");
        System.setProperty(PATH_URI, picked.toURI().toString());
        DartDirectoryDialog dialog = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null);

        assertThat(dialog.open()).isEqualTo(picked.getPath());
    }

    @Test
    void aBlankDirectoryNamedByTheTestHookIsACancel() {
        System.setProperty(PATH, "  ");
        DartDirectoryDialog dialog = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null);

        assertThat(dialog.open()).isNull();
    }

    @Test
    void theInterfaceAccessorsAnswerTheDialogsState() {
        DartDirectoryDialog dialog = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null);
        dialog.setMessage("Pick");
        dialog.setFilterPath("/tmp");

        assertThat(dialog._message()).isEqualTo("Pick");
        assertThat(dialog._filterPath()).isEqualTo("/tmp");
        assertThat(dialog.getValue()).isSameAs(dialog.getValue());
        assertThat(dialog.getApi().getImpl()).isSameAs(dialog);
    }

    @Test
    void theApiOpensThroughTheImplementation() {
        DirectoryDialog api = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null).getApi();

        assertThat(api.open()).isNull();
    }
}

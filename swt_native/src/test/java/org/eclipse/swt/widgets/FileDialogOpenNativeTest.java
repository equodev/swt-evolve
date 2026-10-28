package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@link DartFileDialog#open()} answers. No chooser is drawn for it, so it answers as a cancelled chooser
 * does, unless an E2E test names the path to pick through a system property.
 */
@Tag("native-unit")
class FileDialogOpenNativeTest {

    private static final String PATH = "dev.equo.swt.test.fileDialog.path";
    private static final String PATH_URI = "dev.equo.swt.test.fileDialog.path.uri";

    @AfterEach
    void clearProperties() {
        System.clearProperty(PATH);
        System.clearProperty(PATH_URI);
    }

    private static DartFileDialog dialog(Shell shell, int style) {
        return new DartFileDialog(shell, style, null);
    }

    @Test
    void openAnswersACancelledChooserAndLeavesTheState() {
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.OPEN);
        dialog.setFileName("start.txt");
        dialog.setFilterPath("/tmp");

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFileName()).isEqualTo("start.txt");
        assertThat(dialog.getFileNames()).isEmpty();
        assertThat(dialog.getFilterPath()).isEqualTo("/tmp");
    }

    @Test
    void openWithFilterExtensionsDoesNotFail() {
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] {"*.txt", "*.*"});
        dialog.setFilterNames(new String[] {"Text", "All"});

        assertThat(dialog.open()).isNull();
    }

    @Test
    void openOnADisposedParentIsRefused() {
        Shell shell = DartMocks.dartShell();
        DartFileDialog dialog = dialog(shell, SWT.OPEN);
        when(shell.isDisposed()).thenReturn(true);

        assertThatThrownBy(dialog::open).isInstanceOf(SWTException.class)
                .extracting(e -> ((SWTException) e).code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED);
    }

    @Test
    void aPathNamedByTheTestHookIsPicked() {
        File picked = new File(System.getProperty("java.io.tmpdir"), "picked.txt");
        System.setProperty(PATH, picked.getPath());
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.SAVE);

        assertThat(dialog.open()).isEqualTo(picked.getAbsolutePath());
        assertThat(dialog.getFileName()).isEqualTo("picked.txt");
        assertThat(dialog.getFileNames()).containsExactly("picked.txt");
        assertThat(dialog.getFilterPath()).isEqualTo(picked.getParent());
    }

    @Test
    void aFileUriNamedByTheTestHookIsPicked() {
        File picked = new File(System.getProperty("java.io.tmpdir"), "from uri.txt");
        System.setProperty(PATH_URI, picked.toURI().toString());
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.OPEN);

        assertThat(dialog.open()).isEqualTo(picked.getAbsolutePath());
        assertThat(dialog.getFileName()).isEqualTo("from uri.txt");
    }

    @Test
    void aBlankPathNamedByTheTestHookIsACancel() {
        System.setProperty(PATH, " ");
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.OPEN);
        dialog.setFileName("kept.txt");

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFileName()).isEqualTo("kept.txt");
    }

    @Test
    void aRelativeFileNameKeepsTheFilterPath() {
        System.setProperty(PATH, "alone.txt");
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.OPEN);
        dialog.setFilterPath("/kept");

        assertThat(dialog.open()).isEqualTo(new File("alone.txt").getAbsolutePath());
        assertThat(dialog.getFilterPath()).isEqualTo("/kept");
    }

    /** As on GTK and Win32: Cocoa's initial true only reflects an NSSavePanel prompt it cannot suppress. */
    @Test
    void aNewDialogDoesNotPromptBeforeOverwriting() {
        assertThat(dialog(DartMocks.dartShell(), SWT.SAVE).getOverwrite()).isFalse();
    }

    @Test
    void setOverwriteKeepsWhatItIsGiven() {
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.SAVE);
        dialog.setOverwrite(true);
        assertThat(dialog.getOverwrite()).isTrue();
        dialog.setOverwrite(false);
        assertThat(dialog.getOverwrite()).isFalse();
    }

    @Test
    void theInterfaceAccessorsAnswerTheDialogsState() {
        DartFileDialog dialog = dialog(DartMocks.dartShell(), SWT.SAVE);
        dialog.setFilterNames(new String[] {"Text"});
        dialog.setFilterExtensions(new String[] {"*.txt"});
        dialog.setFilterPath("/tmp");
        dialog.setFileName("a.txt");
        dialog.setFilterIndex(0);

        assertThat(dialog._filterNames()).containsExactly("Text");
        assertThat(dialog._filterExtensions()).containsExactly("*.txt");
        assertThat(dialog._fileNames()).isEmpty();
        assertThat(dialog._filterPath()).isEqualTo("/tmp");
        assertThat(dialog._fileName()).isEqualTo("a.txt");
        assertThat(dialog._filterIndex()).isZero();
        assertThat(dialog._overwrite()).isFalse();
        assertThat(dialog.getValue()).isSameAs(dialog.getValue());
        assertThat(dialog.getApi().getImpl()).isSameAs(dialog);
    }

    @Test
    void theApiOpensThroughTheImplementation() {
        FileDialog api = dialog(DartMocks.dartShell(), SWT.OPEN).getApi();

        assertThat(api.open()).isNull();
    }
}

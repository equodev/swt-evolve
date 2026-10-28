package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@code openDialog()} answers on {@link FileDialog} and {@link DirectoryDialog}. It is the
 * Optional-returning twin of {@code open()}, added in SWT 3.126, and it goes through the same
 * {@code choosePath()}: no chooser is drawn, so an unset test hook answers an empty Optional and a
 * set one answers the path it names.
 */
@Tag("native-unit")
class DialogOpenDialogNativeTest {

    private static final String FILE_PATH = "dev.equo.swt.test.fileDialog.path";
    private static final String DIRECTORY_PATH = "dev.equo.swt.test.directoryDialog.path";

    @AfterEach
    void clearProperties() {
        System.clearProperty(FILE_PATH);
        System.clearProperty(DIRECTORY_PATH);
    }

    @Test
    void aFileDialogOpenDialogAnswersACancelledChooserAsAnEmptyOptional() {
        FileDialog api = new DartFileDialog(DartMocks.dartShell(), SWT.OPEN, null).getApi();

        assertThat(api.openDialog()).isEmpty();
    }

    @Test
    void aFileDialogOpenDialogAnswersThePathTheTestHookNames() {
        File picked = new File(System.getProperty("java.io.tmpdir"), "picked.txt");
        System.setProperty(FILE_PATH, picked.getPath());
        FileDialog api = new DartFileDialog(DartMocks.dartShell(), SWT.SAVE, null).getApi();

        assertThat(api.openDialog()).contains(picked.getAbsolutePath());
    }

    @Test
    void aDirectoryDialogOpenDialogAnswersACancelledChooserAsAnEmptyOptional() {
        DirectoryDialog api = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null).getApi();

        assertThat(api.openDialog()).isEmpty();
    }

    @Test
    void aDirectoryDialogOpenDialogAnswersTheDirectoryTheTestHookNames() {
        System.setProperty(DIRECTORY_PATH, "/picked/dir");
        DirectoryDialog api = new DartDirectoryDialog(DartMocks.dartShell(), SWT.NONE, null).getApi();

        assertThat(api.openDialog()).contains("/picked/dir");
    }
}

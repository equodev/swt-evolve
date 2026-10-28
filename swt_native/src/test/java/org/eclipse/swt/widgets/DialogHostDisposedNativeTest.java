package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * A dialog is drawn on the Shell it is parked on. When that Shell is disposed while the dialog is open, nobody
 * can answer the dialog any more, so open() returns what it returns for a cancel instead of waiting forever.
 */
@Tag("native-unit")
class DialogHostDisposedNativeTest extends DialogBridgeTestBase {

    private Display display;
    private Shell shell;
    private final AtomicBoolean disposed = new AtomicBoolean();

    @BeforeEach
    void setUp() {
        display = DartMocks.dartDisplay();
        shell = DartMocks.dartShell(display);
        when(shell.isDisposed()).thenAnswer(invocation -> disposed.get());
        // A client that shows the dialog and never answers it.
        installBridge(display, mock(DisplayBridge.class));
        // The Shell is disposed by the first event the dialog's loop dispatches.
        doAnswer(invocation -> {
            disposed.set(true);
            return true;
        }).when(display).readAndDispatch();
    }

    @AfterEach
    void tearDown() {
        doReturn(false).when(display).readAndDispatch();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void aMessageBoxWhoseShellIsDisposedAnswersCancel() {
        MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO);

        assertThat(box.open()).isEqualTo(SWT.CANCEL);
        verify((DartShell) shell.getImpl()).addDialog((DartDialog) box.getImpl());
        verify((DartShell) shell.getImpl(), never()).removeDialog(any());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void aColorDialogWhoseShellIsDisposedAnswersNull() {
        ColorDialog dialog = new ColorDialog(shell);

        assertThat(dialog.open()).isNull();
    }

    /** A dialog on an invisible Shell is drawn on the visible ancestor, so its own parent is not its host. */
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void aMessageBoxWhoseOwnParentIsDisposedAnswersCancelWhileItsHostLives() {
        Shell host = DartMocks.dartShell(display);
        when(host.getVisible()).thenReturn(true);
        Shell nested = DartMocks.dartShell(display);
        when(nested.getParent()).thenReturn(host);
        when(nested.isDisposed()).thenAnswer(invocation -> disposed.get());

        MessageBox box = new MessageBox(nested, SWT.YES | SWT.NO);

        assertThat(box.open()).isEqualTo(SWT.CANCEL);
        verify((DartShell) host.getImpl()).addDialog((DartDialog) box.getImpl());
        verify((DartShell) nested.getImpl(), never()).addDialog(any());
    }
}

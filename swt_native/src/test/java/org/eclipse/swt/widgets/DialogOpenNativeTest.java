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
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * How {@link DartDialog#openDialogWithFlutter} waits: it runs the event loop until the client answers on the
 * dialog's close channel or the Shell the dialog is drawn on is disposed, and parks the dialog only on a Shell
 * Flutter draws.
 */
@Tag("native-unit")
class DialogOpenNativeTest extends DialogBridgeTestBase {

    private Display display;
    private final AtomicBoolean disposed = new AtomicBoolean();
    private final AtomicInteger events = new AtomicInteger();

    @BeforeEach
    void setUp() {
        display = DartMocks.dartDisplay();
        DartMocks.resetDisplayState();
        rememberBridge(display);
        // One event is dispatched, then the loop finds the queue empty and the Shell disposed.
        doAnswer(invocation -> {
            if (events.incrementAndGet() == 1)
                return true;
            disposed.set(true);
            return false;
        }).when(display).readAndDispatch();
    }

    @AfterEach
    void tearDown() {
        doReturn(false).when(display).readAndDispatch();
        when(display.getActiveShell()).thenReturn(null);
    }

    private Shell shellDisposedByTheLoop() {
        Shell shell = DartMocks.dartShell(display);
        when(shell.isDisposed()).thenAnswer(invocation -> disposed.get());
        return shell;
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void withoutABridgeTheDialogWaitsForItsShellAndSleepsBetweenEvents() {
        installBridge(display, null);
        MessageBox box = new MessageBox(shellDisposedByTheLoop(), SWT.OK | SWT.CANCEL);

        assertThat(box.open()).isEqualTo(SWT.CANCEL);
        assertThat(events.get()).isEqualTo(2);
        verify(display).sleep();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void aShellFlutterDoesNotDrawGetsNoDialog() {
        installBridge(display, mock(DisplayBridge.class));
        Shell shell = shellDisposedByTheLoop();
        IShell notDart = mock(IShell.class);
        when(shell.getImpl()).thenReturn(notDart);
        ColorDialog dialog = new ColorDialog(shell);

        assertThat(dialog.open()).isNull();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void anActiveShellThatIsNotVisibleDoesNotHostTheDialog() {
        installBridge(display, mock(DisplayBridge.class));
        Shell hidden = shellDisposedByTheLoop();
        Shell active = DartMocks.dartShell(display);
        when(display.getActiveShell()).thenReturn(active);
        MessageBox box = new MessageBox(hidden, SWT.YES | SWT.NO);

        assertThat(box.open()).isEqualTo(SWT.CANCEL);
        verify((DartShell) hidden.getImpl()).addDialog((DartDialog) box.getImpl());
        verify((DartShell) active.getImpl(), never()).addDialog(any());
    }

    @Test
    void aDialogOfItsOwnKeepsItsStateForTheInterface() {
        Shell shell = DartMocks.dartShell(display);
        DartDialog dialog = new DartDialog(shell, SWT.APPLICATION_MODAL, null) {
        };
        dialog.setText("Title");

        assertThat(dialog._style()).isEqualTo(SWT.APPLICATION_MODAL);
        assertThat(dialog._parent()).isSameAs(shell);
        assertThat(dialog._title()).isEqualTo("Title");
        assertThat(dialog.getValue()).isSameAs(dialog.getValue());
        assertThat(dialog.getApi()).isNull();
    }
}

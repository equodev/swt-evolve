package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

/**
 * What BusyIndicator does when the display goes away while it waits, or a Shell is gone before its cursor is set:
 * paths a dual-run test cannot take without disposing the display every other test runs on.
 */
@Tag("native-unit")
@ExtendWith(Mocks.class)
class BusyIndicatorImplementationNativeTest {

    private Display display;
    private final AtomicBoolean disposed = new AtomicBoolean();

    @BeforeEach
    void setUp() {
        display = Mocks.display();
        doReturn(new Shell[0]).when(display).getShells();
        doAnswer(invocation -> disposed.get()).when(display).isDisposed();
    }

    @AfterEach
    void tearDown() {
        doReturn(null).when(display).getShells();
        doReturn(false).when(display).isDisposed();
        doReturn(false).when(display).readAndDispatch();
        org.mockito.Mockito.doNothing().when(display).asyncExec(any());
    }

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        DartBusyIndicator impl = new DartBusyIndicator(null);
        BusyIndicator api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
    }

    @Test
    void aShellAlreadyDisposedIsGivenNoCursorAndNoId() {
        Display own = mock(Display.class);
        Shell gone = mock(Shell.class);
        when(gone.isDisposed()).thenReturn(true);
        when(own.getShells()).thenReturn(new Shell[] {gone});
        BusyIndicator.showWhile(own, () -> {
        });
        verify(gone, never()).setCursor(any());
        verify(gone, never()).setData(anyString(), any());
    }

    @Test
    void aDisplayDisposedByTheRunnableIsNotCleared() {
        Display own = mock(Display.class);
        when(own.getShells()).thenReturn(new Shell[0]);
        BusyIndicator.showWhile(own, () -> when(own.isDisposed()).thenReturn(true));
        verify(own, times(1)).getShells();
    }

    @Test
    void theWaitEndsWhenTheDisplayIsDisposedAndTheLateCompletionIsIgnored() {
        CompletableFuture<String> future = new CompletableFuture<>();
        doAnswer(invocation -> {
            disposed.set(true);
            return false;
        }).when(display).readAndDispatch();

        BusyIndicator.showWhile(future);
        assertThat(future).isNotDone();
        verify(display, times(1)).getShells();

        future.complete("late");
        verify(display, never()).asyncExec(any());
    }

    @Test
    void aWakeRefusedByADisplayGoingAwayIsIgnored() {
        CompletableFuture<String> future = new CompletableFuture<>();
        doThrow(new SWTException(SWT.ERROR_DEVICE_DISPOSED)).when(display).asyncExec(any());
        doAnswer(invocation -> {
            future.complete("done");
            return false;
        }).when(display).readAndDispatch();

        BusyIndicator.showWhile(future);
        assertThat(future).isDone();
        verify(display).asyncExec(any());
    }

    @Test
    void aPlainFutureIsPolledWhileItIsPendingAndTheDisplayIsAlive() {
        FutureTask<String> task = new FutureTask<>(() -> "done");
        doAnswer(invocation -> {
            disposed.set(true);
            return false;
        }).when(display).readAndDispatch();
        BusyIndicator.showWhile(task);
        ArgumentCaptor<Runnable> timer = ArgumentCaptor.forClass(Runnable.class);
        verify(display).timerExec(eq(10), timer.capture());
        Runnable poll = timer.getValue();

        org.mockito.Mockito.clearInvocations(display);
        poll.run();
        verify(display, never()).timerExec(anyInt(), any());

        disposed.set(false);
        poll.run();
        verify(display).timerExec(10, poll);

        task.run();
        org.mockito.Mockito.clearInvocations(display);
        poll.run();
        verify(display, never()).timerExec(anyInt(), any());
    }
}

package dev.equo.swt.awt;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.SecondaryLoop;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for a host that merges AWT and JavaFX dispatch onto a single thread:
 * EvolveSwingHost installs {@code EvolveDispatcherWrapper} so dispatch is routed through
 * the SWT {@link Display}'s own pump instead of a native nested loop nothing here services (see
 * EvolveSwingHost's class javadoc for the full mechanism). These tests exercise that wrapper and
 * its {@link EvolveSwingHost.EvolveSecondaryLoop} directly, without needing a real JavaFX/AWT
 * dispatch-merge to actually trigger.
 */
@ExtendWith(Mocks.class)
class EvolveDispatcherWrapperTest {

    @Test
    void isDispatchThreadMatchesTheDisplaysOwnThread() throws InterruptedException {
        Display display = Mocks.swtDisplay();
        EvolveSwingHost.EvolveDispatcherWrapper wrapper = new EvolveSwingHost.EvolveDispatcherWrapper(display);

        assertThat(wrapper.isDispatchThread()).isTrue();

        AtomicBoolean fromOtherThread = new AtomicBoolean(true);
        Thread other = new Thread(() -> fromOtherThread.set(wrapper.isDispatchThread()));
        other.start();
        other.join();
        assertThat(fromOtherThread.get()).isFalse();
    }

    @Test
    void isDispatchThreadIsFalseOnceTheDisplayIsDisposed() {
        Display display = Mocks.swtDisplay();
        when(display.isDisposed()).thenReturn(true);
        EvolveSwingHost.EvolveDispatcherWrapper wrapper = new EvolveSwingHost.EvolveDispatcherWrapper(display);

        assertThat(wrapper.isDispatchThread()).isFalse();
    }

    @Test
    void scheduleDispatchRunsThroughTheDisplaysAsyncExec() {
        Display display = Mocks.swtDisplay();
        EvolveSwingHost.EvolveDispatcherWrapper wrapper = new EvolveSwingHost.EvolveDispatcherWrapper(display);

        Runnable task = () -> {};
        wrapper.scheduleDispatch(task);

        verify(display).asyncExec(task);
    }

    @Test
    void scheduleDispatchIsANoOpOnceTheDisplayIsDisposed() {
        Display display = Mocks.swtDisplay();
        when(display.isDisposed()).thenReturn(true);
        EvolveSwingHost.EvolveDispatcherWrapper wrapper = new EvolveSwingHost.EvolveDispatcherWrapper(display);

        wrapper.scheduleDispatch(() -> {});

        verify(display, org.mockito.Mockito.never()).asyncExec(org.mockito.ArgumentMatchers.any());
    }

    /**
     * installEvolveDispatcher() can capture a Display before its Synchronizer is set up --
     * JavaFX/AWT startup can reach the EDT before the SWT Display finishes its own construction,
     * so asyncExec() NPEs even though isDisposed() still reports false. scheduleDispatch must
     * retry instead of losing the task.
     */
    @Test
    void scheduleDispatchRetriesInsteadOfLosingTheTaskWhenTheDisplayIsNotYetInitialized()
            throws InterruptedException, java.lang.reflect.InvocationTargetException {
        Display display = Mocks.swtDisplay();
        AtomicBoolean firstAttempt = new AtomicBoolean(true);
        CountDownLatch dispatched = new CountDownLatch(2);
        org.mockito.Mockito.doAnswer(inv -> {
            dispatched.countDown();
            if (firstAttempt.compareAndSet(true, false)) {
                throw new NullPointerException("synchronizer is null");
            }
            return null;
        }).when(display).asyncExec(org.mockito.ArgumentMatchers.any());
        EvolveSwingHost.EvolveDispatcherWrapper wrapper = new EvolveSwingHost.EvolveDispatcherWrapper(display);
        Runnable task = () -> {};

        java.awt.EventQueue.invokeAndWait(() -> wrapper.scheduleDispatch(task));

        assertThat(dispatched.await(2, TimeUnit.SECONDS)).isTrue();
        verify(display, org.mockito.Mockito.times(2)).asyncExec(task);
    }

    /**
     * The heart of the fix: {@code enter()} must block the calling thread until {@code exit()}
     * releases it -- exactly the contract JavaFX's own {@code initFx()} relies on -- but pumping the
     * SWT Display instead of a native nested loop that nothing here services.
     */
    @Test
    void secondaryLoopEnterBlocksUntilExitIsCalledFromAnotherThread() throws InterruptedException {
        Display display = Mocks.swtDisplay();
        // Unstubbed readAndDispatch()/sleep() both default to false on a Mockito mock, which would
        // busy-spin enter()'s loop; give sleep() a small real pause so the loop behaves like a real
        // Display parking between empty polls.
        when(display.sleep()).thenAnswer(inv -> {
            Thread.sleep(2);
            return true;
        });
        SecondaryLoop loop = new EvolveSwingHost.EvolveDispatcherWrapper(display).createSecondaryLoop();

        CountDownLatch entered = new CountDownLatch(1);
        AtomicBoolean enterReturned = new AtomicBoolean(false);
        Thread runner = new Thread(() -> {
            entered.countDown();
            boolean result = loop.enter();
            enterReturned.set(result);
        });
        runner.start();
        assertThat(entered.await(2, TimeUnit.SECONDS)).isTrue();

        // enter() must still be blocked -- give the runner thread a moment to have looped at least
        // once, then confirm it has NOT returned yet.
        Thread.sleep(50);
        assertThat(enterReturned.get()).isFalse();

        assertThat(loop.exit()).isTrue();
        runner.join(2000);
        assertThat(runner.isAlive()).isFalse();
        assertThat(enterReturned.get()).isTrue();
    }

    @Test
    void secondaryLoopExitBeforeEnterReturnsFalse() {
        SecondaryLoop loop = new EvolveSwingHost.EvolveSecondaryLoop(Mocks.swtDisplay());
        assertThat(loop.exit()).isFalse();
    }

    @Test
    void secondaryLoopEnterTwiceRejectsTheSecondCall() throws InterruptedException {
        Display display = Mocks.swtDisplay();
        when(display.sleep()).thenAnswer(inv -> {
            Thread.sleep(2);
            return true;
        });
        SecondaryLoop loop = new EvolveSwingHost.EvolveSecondaryLoop(display);

        Thread runner = new Thread(loop::enter);
        runner.start();
        Thread.sleep(20); // let the first enter() actually start running

        assertThat(loop.enter()).isFalse();

        loop.exit();
        runner.join(2000);
    }
}

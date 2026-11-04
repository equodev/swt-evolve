package org.eclipse.swt.graphics;

import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link GCHelper#awaitRenderPumping}: the wait that runs the display's event loop until a render answers. */
@Tag("native-unit")
class GCHelperRenderWaitTest {

    @Test
    void noDisplayOrADisposedOneDoesNotWait() {
        GCHelper.awaitRenderPumping(null, new CompletableFuture<Void>(), 30000);
        Display disposed = mock(Display.class);
        when(disposed.isDisposed()).thenReturn(true);

        GCHelper.awaitRenderPumping(disposed, new CompletableFuture<Void>(), 30000);

        verify(disposed, never()).readAndDispatch();
        verify(disposed, never()).timerExec(anyInt(), any());
    }

    @Test
    void theLoopRunsUntilTheRenderAnswersAndDisarmsItsTimer() {
        Display display = mock(Display.class);
        CompletableFuture<Void> render = new CompletableFuture<>();
        when(display.readAndDispatch()).thenReturn(true).thenAnswer(i -> {
            render.complete(null);
            return false;
        });

        GCHelper.awaitRenderPumping(display, render, 30000);

        verify(display).sleep();
        verify(display).timerExec(eq(-1), any());
    }

    @Test
    void anUnansweredRenderEndsAtTheDeadlineWokenByItsTimer() {
        Display display = mock(Display.class);
        AtomicBoolean timerRan = new AtomicBoolean();
        doAnswer(i -> {
            if (i.<Integer>getArgument(0) > 0) {
                ((Runnable) i.getArgument(1)).run();
                timerRan.set(true);
            }
            return null;
        }).when(display).timerExec(anyInt(), any());

        long started = System.nanoTime();
        GCHelper.awaitRenderPumping(display, new CompletableFuture<Void>(), 50);

        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)).isBetween(40L, 5000L);
        assertThat(timerRan).isTrue();
    }

    @Test
    void aDisplayDisposedMidWaitEndsTheWait() {
        Display display = mock(Display.class);
        // Alive on entry and for the first turn, then gone.
        when(display.isDisposed()).thenReturn(false, false, true);

        GCHelper.awaitRenderPumping(display, new CompletableFuture<Void>(), 30000);

        verify(display).readAndDispatch();
        verify(display, never()).timerExec(eq(-1), any());
    }
}

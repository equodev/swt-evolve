package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Where an exception thrown by a {@code timerExec} runnable goes. Its own source because
 * {@link Display#setRuntimeExceptionHandler} and {@link Display#setErrorHandler} were added in 3.106:
 * below that release {@code runTimers} has no handler to route a failure to and keeps the console.
 */
@Tag("flutter-it")
class DisplayTimerExceptionHandlerFlutterTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        try {
            display = new Display();
        } finally {
            FlutterBridge.set(null);
        }
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private boolean spinUntil(BooleanSupplier done) {
        long deadline = System.currentTimeMillis() + 2000;
        while (!done.getAsBoolean() && System.currentTimeMillis() < deadline) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return done.getAsBoolean();
    }

    @Test
    void aFailingTimerGoesToTheDisplayHandlers() {
        List<Throwable> handled = new ArrayList<>();
        display.setRuntimeExceptionHandler(handled::add);
        display.setErrorHandler(handled::add);
        IllegalStateException exception = new IllegalStateException("timer");
        AssertionError error = new AssertionError("timer");
        display.timerExec(0, () -> {
            throw exception;
        });
        display.timerExec(0, () -> {
            throw error;
        });
        assertThat(spinUntil(() -> handled.size() == 2)).isTrue();
        assertThat(handled).containsExactlyInAnyOrder(exception, error);
    }

    @Test
    void anErrorInATimerComesOutOfTheLoopThroughTheDefaultHandler() {
        Error failure = new Error("from the timer");
        display.timerExec(0, () -> {
            throw failure;
        });
        assertThatThrownBy(() -> spinUntil(() -> false)).isSameAs(failure);
    }
}

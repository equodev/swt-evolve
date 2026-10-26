package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LatestEventTest {

    private final Display display = mock(Display.class);
    private final List<Runnable> queued = new ArrayList<>();
    private final List<Event> handled = new ArrayList<>();
    private final LatestEvent latest = new LatestEvent();

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> queued.add(invocation.getArgument(0))).when(display).asyncExec(any(Runnable.class));
    }

    private void runQueued() {
        List<Runnable> now = new ArrayList<>(queued);
        queued.clear();
        now.forEach(Runnable::run);
    }

    private static Event event(int detail) {
        Event event = new Event();
        event.detail = detail;
        return event;
    }

    @Test
    @DisplayName("the events posted before the display runs are handed on once, as the newest of them")
    void handsOnTheNewest() {
        latest.post(display, event(1), handled::add);
        latest.post(display, event(2), handled::add);
        latest.post(display, event(3), handled::add);

        assertThat(queued).hasSize(1);
        runQueued();
        assertThat(handled).extracting(e -> e.detail).containsExactly(3);
    }

    @Test
    @DisplayName("an event posted after the display ran the previous one is handed on by itself")
    void postsAgainOnceHandled() {
        latest.post(display, event(1), handled::add);
        runQueued();
        latest.post(display, event(2), handled::add);
        runQueued();

        assertThat(handled).extracting(e -> e.detail).containsExactly(1, 2);
    }

    @Test
    @DisplayName("a null event leaves nothing to hand on")
    void aNullEventIsNotHandedOn() {
        latest.post(display, null, handled::add);
        runQueued();

        assertThat(handled).isEmpty();
    }
}

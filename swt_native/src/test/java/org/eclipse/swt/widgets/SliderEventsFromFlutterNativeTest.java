package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a Selection or DefaultSelection arriving from Flutter does to a Slider. Both hop through {@code asyncExec},
 * so by the time they run the slider may be gone or a modal shell may have opened. A Selection (a drag) moves the
 * thumb, clamped to what the range leaves, and reports SWT.DRAG; a DefaultSelection (the end of a drag) does the
 * same and reports SWT.NONE, to a live slider that nothing blocks.
 */
@Tag("native-unit")
class SliderEventsFromFlutterNativeTest extends EventsFromFlutterNativeTestBase {

    private void fromFlutter(Slider slider, String event, int index) {
        Event e = new Event();
        e.index = index;
        fromFlutter(slider, "Selection/" + event, e);
    }

    @Test
    @DisplayName("a Selection from Flutter moves the thumb, clamped to what the range leaves, and reports a drag")
    void selectionMovesTheThumb() {
        Slider slider = new Slider(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(slider, SWT.Selection);

        fromFlutter(slider, "Selection", 60);
        runQueued();
        assertThat(slider.getSelection()).isEqualTo(60);

        fromFlutter(slider, "Selection", 500);
        runQueued();
        assertThat(slider.getSelection()).isEqualTo(90);
        assertThat(events).extracting(e -> e.detail).containsExactly(SWT.DRAG, SWT.DRAG);
    }

    @Test
    @DisplayName("a Selection that arrives after the slider was disposed is dropped")
    void selectionToADisposedSliderIsDropped() {
        Slider slider = new Slider(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(slider, SWT.Selection);

        fromFlutter(slider, "Selection", 60);
        slider.dispose();
        runQueued();

        assertThat(slider.isDisposed()).isTrue();
        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter moves the thumb and reports the end of a drag")
    void defaultSelectionEndsTheDrag() {
        Slider slider = new Slider(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(slider, SWT.Selection);

        fromFlutter(slider, "DefaultSelection", 35);
        runQueued();

        assertThat(slider.getSelection()).isEqualTo(35);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).detail).isEqualTo(SWT.NONE);
        assertThat(events.get(0).widget).isSameAs(slider);
    }

    @Test
    @DisplayName("a DefaultSelection that arrives after the slider was disposed is dropped")
    void defaultSelectionToADisposedSliderIsDropped() {
        Slider slider = new Slider(DartMocks.dartShell(display), SWT.NONE);
        List<Event> events = record(slider, SWT.Selection);

        fromFlutter(slider, "DefaultSelection", 35);
        slider.dispose();
        runQueued();

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("a DefaultSelection while a modal shell blocks the slider's shell is dropped")
    void defaultSelectionBehindAModalShellIsDropped() {
        Shell shell = DartMocks.dartShell(display);
        Shell modal = mock(Shell.class);
        when(((DartShell) shell.getImpl()).getModalShell()).thenReturn(modal);
        Slider slider = new Slider(shell, SWT.NONE);
        List<Event> events = record(slider, SWT.Selection);

        fromFlutter(slider, "DefaultSelection", 35);
        runQueued();

        assertThat(slider.getSelection()).isZero();
        assertThat(events).isEmpty();
    }
}

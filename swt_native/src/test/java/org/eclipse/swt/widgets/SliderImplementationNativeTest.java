package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartSlider} does when it is driven directly rather than through {@link Slider}. */
@Tag("native-unit")
class SliderImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartSlider impl = new DartSlider(shell, SWT.NONE, null);

        Slider api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports the range, the thumb, the selection and the increments it holds")
    void implementationReportsWhatItHolds() {
        Slider slider = new Slider(shell, SWT.NONE);
        DartSlider impl = (DartSlider) slider.getImpl();

        assertThat(held(impl)).containsExactly(0, 100, 10, 0, 1, 10);

        slider.setMaximum(250);
        slider.setMinimum(20);
        slider.setThumb(30);
        slider.setSelection(137);
        slider.setIncrement(3);
        slider.setPageIncrement(40);
        assertThat(held(impl)).containsExactly(20, 250, 30, 137, 3, 40);
        assertThat(impl._dragging()).isFalse();
    }

    private static List<Integer> held(DartSlider impl) {
        return List.of(impl._minimum(), impl._maximum(), impl._thumb(), impl._selection(), impl._increment(), impl._pageIncrement());
    }

    @Test
    @DisplayName("the native selection callback keeps the selection and tells the Selection listeners")
    void nativeSelectionCallbackKeepsTheSelection() {
        Slider slider = new Slider(shell, SWT.NONE);
        DartSlider impl = (DartSlider) slider.getImpl();
        slider.setSelection(40);
        List<Event> seen = new ArrayList<>();
        slider.addListener(SWT.Selection, seen::add);

        impl.sendSelection();

        assertThat(slider.getSelection()).isEqualTo(40);
        assertThat(seen).hasSize(1);
        assertThat(seen.get(0).detail).isEqualTo(SWT.NONE);
    }

    @Test
    @DisplayName("the small-size hook of the native scroller does nothing")
    void smallSizeHookDoesNothing() {
        Slider slider = new Slider(shell, SWT.NONE);
        DartSlider impl = (DartSlider) slider.getImpl();
        slider.setSelection(40);

        impl.setSmallSize();

        assertThat(held(impl)).containsExactly(0, 100, 10, 40, 1, 10);
    }
}

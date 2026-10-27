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

/** What {@link DartScale} does when it is driven directly rather than through {@link Scale}. */
@Tag("native-unit")
class ScaleImplementationNativeTest {

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
        DartScale impl = new DartScale(shell, SWT.NONE, null);

        Scale api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports the range, the selection and the increments it holds")
    void implementationReportsWhatItHolds() {
        Scale scale = new Scale(shell, SWT.NONE);
        DartScale impl = (DartScale) scale.getImpl();

        assertThat(held(impl)).containsExactly(0, 100, 0, 1, 10);

        scale.setMaximum(250);
        scale.setMinimum(20);
        scale.setSelection(137);
        scale.setIncrement(3);
        scale.setPageIncrement(40);
        assertThat(held(impl)).containsExactly(20, 250, 137, 3, 40);
    }

    private static List<Integer> held(DartScale impl) {
        return List.of(impl._minimum(), impl._maximum(), impl._selection(), impl._increment(), impl._pageIncrement());
    }

    @Test
    @DisplayName("the native selection callback leaves a Dart scale and its listeners alone")
    void nativeSelectionCallbackIsInert() {
        Scale scale = new Scale(shell, SWT.NONE);
        DartScale impl = (DartScale) scale.getImpl();
        scale.setSelection(40);
        List<Event> seen = new ArrayList<>();
        scale.addListener(SWT.Selection, seen::add);

        impl.sendSelection();

        assertThat(scale.getSelection()).isEqualTo(40);
        assertThat(seen).isEmpty();
    }
}

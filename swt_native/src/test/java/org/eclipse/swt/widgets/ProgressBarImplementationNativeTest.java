package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartProgressBar} does when it is driven directly rather than through {@link ProgressBar}. */
@Tag("native-unit")
class ProgressBarImplementationNativeTest {

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
        DartProgressBar impl = new DartProgressBar(shell, SWT.NONE, null);

        ProgressBar api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the implementation reports the range and the selection it holds")
    void implementationReportsWhatItHolds() {
        ProgressBar bar = new ProgressBar(shell, SWT.NONE);
        DartProgressBar impl = (DartProgressBar) bar.getImpl();

        assertThat(new int[] {impl._minimum(), impl._maximum(), impl._selection()}).containsExactly(0, 100, 0);

        bar.setMaximum(250);
        bar.setMinimum(20);
        bar.setSelection(137);
        assertThat(new int[] {impl._minimum(), impl._maximum(), impl._selection()}).containsExactly(20, 250, 137);
    }
}

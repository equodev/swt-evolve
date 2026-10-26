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

/**
 * A new scale spans 0 to 100 with its selection at 0, an increment of 1 and a page increment of 10, as on every
 * SWT platform.
 */
@Tag("native-unit")
class ScaleRangeTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static int[] range(Scale scale) {
        return new int[] {scale.getMinimum(), scale.getMaximum(), scale.getSelection()};
    }

    @Test
    @DisplayName("a new scale spans 0 to 100 with its selection at 0")
    void startsWithTheDocumentedRange() {
        Shell shell = DartMocks.dartShell();
        for (int style : new int[] {SWT.NONE, SWT.HORIZONTAL, SWT.VERTICAL, SWT.BORDER}) {
            Scale scale = new Scale(shell, style);
            assertThat(range(scale)).as("style %d", style).containsExactly(0, 100, 0);
            assertThat(new int[] {scale.getIncrement(), scale.getPageIncrement()}).as("style %d", style).containsExactly(1, 10);
        }
    }

    @Test
    @DisplayName("the selection and the minimum are clamped against the default range")
    void clampsAgainstTheDefaultRange() {
        Scale scale = new Scale(DartMocks.dartShell(), SWT.NONE);

        scale.setSelection(3);
        assertThat(range(scale)).containsExactly(0, 100, 3);
        scale.setSelection(1000);
        assertThat(range(scale)).containsExactly(0, 100, 100);
        scale.setSelection(0);
        scale.setMinimum(3);
        assertThat(range(scale)).containsExactly(3, 100, 3);
    }

    @Test
    @DisplayName("a maximum that does not exceed the minimum is ignored")
    void maximumMustExceedTheMinimum() {
        Scale scale = new Scale(DartMocks.dartShell(), SWT.NONE);

        scale.setMaximum(0);
        assertThat(range(scale)).containsExactly(0, 100, 0);
        scale.setMaximum(1);
        assertThat(range(scale)).containsExactly(0, 1, 0);
    }
}

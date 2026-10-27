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
 * A new progress bar spans 0 to 100 with its selection at 0, as on every SWT platform.
 */
@Tag("native-unit")
class ProgressBarRangeTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static int[] range(ProgressBar bar) {
        return new int[] {bar.getMinimum(), bar.getMaximum(), bar.getSelection()};
    }

    @Test
    @DisplayName("a new progress bar spans 0 to 100 with its selection at 0")
    void startsWithTheDocumentedRange() {
        Shell shell = DartMocks.dartShell();
        for (int style : new int[] {SWT.NONE, SWT.VERTICAL, SWT.SMOOTH, SWT.INDETERMINATE}) {
            assertThat(range(new ProgressBar(shell, style))).as("style %d", style).containsExactly(0, 100, 0);
        }
    }

    @Test
    @DisplayName("the selection and the minimum are clamped against the default range")
    void clampsAgainstTheDefaultRange() {
        ProgressBar bar = new ProgressBar(DartMocks.dartShell(), SWT.NONE);

        bar.setSelection(3);
        assertThat(range(bar)).containsExactly(0, 100, 3);
        bar.setSelection(1000);
        assertThat(range(bar)).containsExactly(0, 100, 100);
        bar.setSelection(0);
        bar.setMinimum(3);
        assertThat(range(bar)).containsExactly(3, 100, 3);
    }
}

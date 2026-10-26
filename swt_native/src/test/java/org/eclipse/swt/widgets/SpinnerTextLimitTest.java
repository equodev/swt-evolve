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

/** A negative text limit means no limit, as on Win32 and GTK. */
@Tag("native-unit")
class SpinnerTextLimitTest {

    private Spinner spinner;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        spinner = new Spinner(DartMocks.dartShell(), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a negative limit leaves the spinner with Spinner.LIMIT")
    void aNegativeLimitMeansNoLimit() {
        for (int negative : new int[] {-1, Integer.MIN_VALUE}) {
            spinner.setTextLimit(7);

            spinner.setTextLimit(negative);

            assertThat(spinner.getTextLimit()).as("setTextLimit(%d)", negative).isEqualTo(Spinner.LIMIT);
        }
    }

    @Test
    @DisplayName("a positive limit is kept as given")
    void aPositiveLimitIsKept() {
        spinner.setTextLimit(7);

        assertThat(spinner.getTextLimit()).isEqualTo(7);
    }
}

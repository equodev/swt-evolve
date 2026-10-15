package org.eclipse.swt.graphics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@code FontMetrics.toString()} starts with the SWT class name, as upstream's does, not the implementation's. */
@Tag("native-unit")
class FontMetricsToStringTest {

    @Test
    @DisplayName("FontMetrics.toString() names its SWT class")
    void namesTheSwtClass() {
        assertThat(new FontMetrics().toString()).startsWith("FontMetrics{");
    }
}

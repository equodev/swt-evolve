package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.WebFontSubstitutions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The web measures the system font with its own bold widths, which {@link SystemFontBoldTest} shows it renders. */
class SystemFontWebMetricsTest {

    private static final String TEXT = "Show Active Editors";
    private static final int SIZE = 40;

    @Test
    void theSystemFontIsMeasuredWithItsWebBoldWidths() {
        WebFontSubstitutions.ensureRegistered();

        assertThat(FontMetricsUtil.getId("System", false, true)).isEqualTo("System Web-0-6");
        assertThat(FontMetricsUtil.getFontSize(TEXT, FontMetricsUtil.getId("System", false, true), SIZE).x())
                .as("bold is measured with bold widths, not the regular ones")
                .isGreaterThan(FontMetricsUtil.getFontSize(TEXT, FontMetricsUtil.getId("System", false, false), SIZE).x() + 1);
    }
}

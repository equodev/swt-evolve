package org.eclipse.swt.custom;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.WebFontSubstitutions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** StyledText measures "system" on the web with the web's default face, as TextLayout and GC do. */
class StyledTextWidthWebMetricsTest {

    private static final String TEXT = "Show Active Editors";

    @Test
    void theDefaultWebFontIsMeasuredAsTextLayoutMeasuresIt() {
        WebFontSubstitutions.ensureRegistered();
        Display display = DartMocks.dartDisplay();
        Font font = new Font(display, "system", 11, SWT.NORMAL);
        TextLayout layout = new TextLayout(display);
        layout.setFont(font);
        layout.setText(TEXT);
        try {
            double expected = FontMetricsUtil.getFontSize(TEXT, "System Web-0-3", 11).x() * FontMetricsUtil.dpiScale();
            assertThat((double) StyledTextHelper.computeTextWidth(TEXT, font)).isCloseTo(expected, within(0.5));
            assertThat((double) StyledTextHelper.computeTextWidth(TEXT, font))
                    .as("TextLayout's width of the same run")
                    .isCloseTo(layout.getBounds().width, within(1.0));
        } finally {
            layout.dispose();
            font.dispose();
        }
    }
}

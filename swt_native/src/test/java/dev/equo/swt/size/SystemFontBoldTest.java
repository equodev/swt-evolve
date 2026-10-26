package dev.equo.swt.size;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.WebFontSubstitutions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The system font has a real bold face in the browser. CanvasKit renders a family it does not have
 * with its Roboto fallback, and with only Roboto's regular face loaded it fakes every bold by
 * thickening the regular glyphs at their regular advances, so a bold run in a TextLayout (the match
 * an owner-drawn result list highlights) comes out cramped.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SystemFontBoldTest {

    private static final String TEXT = "Show Active Editors";
    private static final int SIZE = 40;

    @RegisterExtension
    final FontMeasureBridge bridge = new FontMeasureBridge();

    @Test
    void theSystemFontsBoldIsARealFaceInTheBrowser() throws Exception {
        String family = webFamilyOfSystemFont();
        double regular = SizeAssert.assertCompletes(bridge, bridge.measureLineMetrics(family, false, 400, SIZE, TEXT))[0];
        double bold = SizeAssert.assertCompletes(bridge, bridge.measureLineMetrics(family, false, 700, SIZE, TEXT))[0];

        assertThat(bold)
                .as("a bold face of its own advances differently from the regular one; a faked bold does not")
                .isGreaterThan(regular + 1);
    }

    private static String webFamilyOfSystemFont() {
        WebFontSubstitutions.ensureRegistered();
        return FontMetricsUtil.substituteFontName("system");
    }
}

package dev.equo.swt.compat;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class to compare the sizing behavior of Eclipse SWT Button vs Equo SWT Button.
 * This ensures that both implementations provide compatible sizes for layouts to work correctly.
 *
 * Test cases mirror those in measure.dart to validate that Java sizing matches Flutter measurements.
 */
@Disabled
class ButtonCompatibilityTest {

    /**
     * Tolerance percentage for size differences.
     * Sizes can differ by up to this percentage and still be considered compatible.
     */
    private static final double TOLERANCE_PERCENT = 0.01;

    private static Display display;

    @BeforeAll
    static void setUpClass() {
        display = new Display();
    }

    @AfterAll
    static void tearDownClass() {
        if (display != null && !display.isDisposed())
            display.dispose();
    }

    @AfterEach
    void cleanup() {
        Config.defaultToEclipse();
    }

    @Nested
    @DisplayName("computeSize - All Button Styles")
    class ComputeSizeByStyle {

        @Nested
        @DisplayName("ARROW Button")
        class ArrowButton {

            @Test
            @DisplayName("ARROW - short text, small font")
            void shortTextSmallFont() {
                testButtonSize(SWT.ARROW, "OK", 10);
            }

            @Test
            @DisplayName("ARROW - short text, large font")
            void shortTextLargeFont() {
                testButtonSize(SWT.ARROW, "OK", 18);
            }

            @Test
            @DisplayName("ARROW - long text, small font")
            void longTextSmallFont() {
                testButtonSize(SWT.ARROW, "This is a long button text", 10);
            }

            @Test
            @DisplayName("ARROW - long text, large font")
            void longTextLargeFont() {
                testButtonSize(SWT.ARROW, "This is a long button text", 18);
            }
        }

        @Nested
        @DisplayName("CHECK Button")
        class CheckButton {

            @Test
            @DisplayName("CHECK - short text, small font")
            void shortTextSmallFont() {
                testButtonSize(SWT.CHECK, "OK", 10);
            }

            @Test
            @DisplayName("CHECK - short text, large font")
            void shortTextLargeFont() {
                testButtonSize(SWT.CHECK, "OK", 18);
            }

            @Test
            @DisplayName("CHECK - long text, small font")
            void longTextSmallFont() {
                testButtonSize(SWT.CHECK, "This is a long button text", 10);
            }

            @Test
            @DisplayName("CHECK - long text, large font")
            void longTextLargeFont() {
                testButtonSize(SWT.CHECK, "This is a long button text", 18);
            }
        }

        @Nested
        @DisplayName("PUSH Button")
        class PushButton {

            @Test
            @DisplayName("PUSH - short text, small font")
            void shortTextSmallFont() {
                testButtonSize(SWT.PUSH, "OK", 10);
            }

            @Test
            @DisplayName("PUSH - short text, large font")
            void shortTextLargeFont() {
                testButtonSize(SWT.PUSH, "OK", 18);
            }

            @Test
            @DisplayName("PUSH - long text, small font")
            void longTextSmallFont() {
                testButtonSize(SWT.PUSH, "This is a long button text", 10);
            }

            @Test
            @DisplayName("PUSH - long text, large font")
            void longTextLargeFont() {
                testButtonSize(SWT.PUSH, "This is a long button text", 18);
            }
        }

        @Nested
        @DisplayName("RADIO Button")
        class RadioButton {

            @Test
            @DisplayName("RADIO - short text, small font")
            void shortTextSmallFont() {
                testButtonSize(SWT.RADIO, "OK", 10);
            }

            @Test
            @DisplayName("RADIO - short text, large font")
            void shortTextLargeFont() {
                testButtonSize(SWT.RADIO, "OK", 18);
            }

            @Test
            @DisplayName("RADIO - long text, small font")
            void longTextSmallFont() {
                testButtonSize(SWT.RADIO, "This is a long button text", 10);
            }

            @Test
            @DisplayName("RADIO - long text, large font")
            void longTextLargeFont() {
                testButtonSize(SWT.RADIO, "This is a long button text", 18);
            }
        }

        @Nested
        @DisplayName("TOGGLE Button")
        class ToggleButton {

            @Test
            @DisplayName("TOGGLE - short text, small font")
            void shortTextSmallFont() {
                testButtonSize(SWT.TOGGLE, "OK", 10);
            }

            @Test
            @DisplayName("TOGGLE - short text, large font")
            void shortTextLargeFont() {
                testButtonSize(SWT.TOGGLE, "OK", 18);
            }

            @Test
            @DisplayName("TOGGLE - long text, small font")
            void longTextSmallFont() {
                testButtonSize(SWT.TOGGLE, "This is a long button text", 10);
            }

            @Test
            @DisplayName("TOGGLE - long text, large font")
            void longTextLargeFont() {
                testButtonSize(SWT.TOGGLE, "This is a long button text", 18);
            }
        }
    }

    @Nested
    @DisplayName("computeSize - Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Empty text")
        void emptyText() {
            testButtonSize(SWT.PUSH, "", 14);
        }

        @Test
        @DisplayName("Null text")
        void nullText() {
            testButtonSize(SWT.PUSH, null, 14);
        }

        @Test
        @DisplayName("Very long text")
        void veryLongText() {
            testButtonSize(SWT.PUSH, "This is an extremely long button text that should force the button to expand significantly", 14);
        }

        @Test
        @DisplayName("Single character")
        void singleCharacter() {
            testButtonSize(SWT.PUSH, "A", 14);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Tests button size compatibility between Eclipse and Equo implementations.
     *
     * @param style Button style (SWT.PUSH, SWT.CHECK, etc.)
     * @param text Button text
     * @param fontSize Font height
     */
    private void testButtonSize(int style, String text, int fontSize) {
        // Test with Eclipse implementation
        Shell eclipseShell = new Shell(display);
        Config.useEclipse(Button.class);
        Button eclipseButton = new Button(eclipseShell, style);
        if (text != null) {
            eclipseButton.setText(text);
        }
        eclipseButton.setFont(createFont(fontSize));
        Point eclipseSize = eclipseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        eclipseShell.dispose();

        // Test with Equo implementation
        Shell equoShell = new Shell(display);
        Config.useEquo(Button.class);
        Button equoButton = new Button(equoShell, style);
        if (text != null) {
            equoButton.setText(text);
        }
        equoButton.setFont(createFont(fontSize));
        Point equoSize = equoButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        equoShell.dispose();

        // Compare
        String scenario = String.format(
            "Button[style=%s, text=\"%s\", fontSize=%d]",
            styleToString(style),
            text != null ? text : "null",
            fontSize
        );
        assertSizeSimilar(eclipseSize, equoSize, scenario);
    }

    /**
     * Creates a font with the specified height.
     *
     * @param fontSize Font height
     * @return Font instance
     */
    private Font createFont(int fontSize) {
        FontData fontData = new FontData();
        fontData.setHeight(fontSize);
        return new Font(display, fontData);
    }

    /**
     * Converts button style constant to readable string.
     *
     * @param style Button style constant
     * @return String representation
     */
    private String styleToString(int style) {
        if ((style & SWT.ARROW) != 0) return "ARROW";
        if ((style & SWT.CHECK) != 0) return "CHECK";
        if ((style & SWT.PUSH) != 0) return "PUSH";
        if ((style & SWT.RADIO) != 0) return "RADIO";
        if ((style & SWT.TOGGLE) != 0) return "TOGGLE";
        return "UNKNOWN";
    }

    /**
     * Asserts that two Points are compatible within the configured tolerance.
     *
     * @param eclipse Point from Eclipse implementation
     * @param equo Point from Equo implementation
     * @param scenario Description of the test scenario
     */
    private void assertSizeSimilar(Point eclipse, Point equo, String scenario) {
        double xDiff = Math.abs(eclipse.x - equo.x);
        double yDiff = Math.abs(eclipse.y - equo.y);

        double xTolerance = eclipse.x * TOLERANCE_PERCENT;
        double yTolerance = eclipse.y * TOLERANCE_PERCENT;

        String message = String.format(
            "%s: Eclipse(%d, %d) vs Equo(%d, %d)",
            scenario, eclipse.x, eclipse.y, equo.x, equo.y
        );

        assertThat(xDiff)
            .as("%s - Width difference (%.1f) exceeds tolerance (%.1f)", message, xDiff, xTolerance)
            .isLessThanOrEqualTo(xTolerance);
        assertThat(yDiff)
            .as("%s - Height difference (%.1f) exceeds tolerance (%.1f)", message, yDiff, yTolerance)
            .isLessThanOrEqualTo(yTolerance);

        System.out.println("PASS: " + message);
    }
}
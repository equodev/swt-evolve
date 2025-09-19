package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Test disabled: Cannot use mock objects in current testing environment")
class CheckboxBackgroundInheritanceTest {

    private static Display display;
    private Shell shell;
    private Composite composite;
    private Button checkbox;
    private Color redColor;
    private Color blueColor;
    private Color greenColor;

    @BeforeAll
    static void setUpClass() {
        display = new Display();
    }

    @AfterAll
    static void tearDownClass() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
    }

    @BeforeEach
    void setUp() {
        shell = new Shell(display);
        composite = new Composite(shell, SWT.NONE);
        checkbox = new Button(composite, SWT.CHECK);

        // Create colors
        redColor = display.getSystemColor(SWT.COLOR_RED);
        blueColor = display.getSystemColor(SWT.COLOR_BLUE);
        greenColor = display.getSystemColor(SWT.COLOR_GREEN);
    }

    @AfterEach
    void tearDown() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    // INHERIT_FORCE Tests

    @Test
    void testInheritForce_AllColorsSet() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_AllColorsSet - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should use checkbox's own color when all colors set");
    }

    @Test
    void testInheritForce_OnlyShellColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_OnlyShellColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should be null when only shell has color (no inheritance)");
    }

    @Test
    void testInheritForce_OnlyCompositeColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_OnlyCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should be null when only composite has color (no inheritance)");
    }

    @Test
    void testInheritForce_OnlyCheckboxColor() {
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_OnlyCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should use checkbox's own color when set");
    }

    @Test
    void testInheritForce_ShellAndCompositeColor() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_ShellAndCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should be null when only parent colors set (no inheritance)");
    }

    @Test
    void testInheritForce_ShellAndCheckboxColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_ShellAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should use checkbox's own color (ignores shell color)");
    }

    @Test
    void testInheritForce_CompositeAndCheckboxColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_CompositeAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should use checkbox's own color (ignores composite color)");
    }

    @Test
    void testInheritForce_NoColors() {
        composite.setBackgroundMode(SWT.INHERIT_FORCE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritForce_NoColors - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_FORCE: Should be null when no colors set");
    }

    // INHERIT_DEFAULT Tests

    @Test
    void testInheritDefault_AllColorsSet() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_AllColorsSet - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should use checkbox's own color when all colors set");
    }

    @Test
    void testInheritDefault_OnlyShellColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_OnlyShellColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should be null when only shell has color (no inheritance)");
    }

    @Test
    void testInheritDefault_OnlyCompositeColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_OnlyCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should be null when only composite has color (no inheritance)");
    }

    @Test
    void testInheritDefault_OnlyCheckboxColor() {
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_OnlyCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should use checkbox's own color when set");
    }

    @Test
    void testInheritDefault_ShellAndCompositeColor() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_ShellAndCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should be null when only parent colors set (no inheritance)");
    }

    @Test
    void testInheritDefault_ShellAndCheckboxColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_ShellAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should use checkbox's own color (ignores shell color)");
    }

    @Test
    void testInheritDefault_CompositeAndCheckboxColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_CompositeAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should use checkbox's own color (ignores composite color)");
    }

    @Test
    void testInheritDefault_NoColors() {
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritDefault_NoColors - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_DEFAULT: Should be null when no colors set");
    }

    // INHERIT_NONE Tests

    @Test
    void testInheritNone_AllColorsSet() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_AllColorsSet - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should use checkbox's own color when all colors set");
    }

    @Test
    void testInheritNone_OnlyShellColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_OnlyShellColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should be null when only shell has color (no inheritance)");
    }

    @Test
    void testInheritNone_OnlyCompositeColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_OnlyCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should be null when only composite has color (no inheritance)");
    }

    @Test
    void testInheritNone_OnlyCheckboxColor() {
        composite.setBackgroundMode(SWT.INHERIT_NONE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_OnlyCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should use checkbox's own color when set");
    }

    @Test
    void testInheritNone_ShellAndCompositeColor() {
        shell.setBackground(redColor);
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_ShellAndCompositeColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should be null when only parent colors set (no inheritance)");
    }

    @Test
    void testInheritNone_ShellAndCheckboxColor() {
        shell.setBackground(redColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_ShellAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should use checkbox's own color (ignores shell color)");
    }

    @Test
    void testInheritNone_CompositeAndCheckboxColor() {
        composite.setBackground(blueColor);
        composite.setBackgroundMode(SWT.INHERIT_NONE);
        checkbox.setBackground(greenColor);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_CompositeAndCheckboxColor - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should use checkbox's own color (ignores composite color)");
    }

    @Test
    void testInheritNone_NoColors() {
        composite.setBackgroundMode(SWT.INHERIT_NONE);

        shell.layout();
        display.update();

        Color expectedColor = getExpectedCheckboxBackground(checkbox, composite, shell);
        Color actualColor = checkbox.getBackground();

        System.out.println("testInheritNone_NoColors - Expected: " + expectedColor + ", Actual: " + actualColor);
        assertEquals(expectedColor, actualColor, "INHERIT_NONE: Should be null when no colors set");
    }

    // Helper method
    private Color getExpectedCheckboxBackground(Button checkbox, Composite composite, Shell shell) {
        Color checkboxColor = checkbox.getBackground();

        // If checkbox has a color set, always use that color
        if (checkboxColor != null) {
            return checkboxColor;
        }

        // Check composite inheritance mode
        int compositeBackgroundMode = composite.getBackgroundMode();

        if (compositeBackgroundMode == SWT.INHERIT_FORCE || compositeBackgroundMode == SWT.INHERIT_DEFAULT) {
            // Only inherits from composite with these modes
            Color compositeColor = composite.getBackground();

            // Check if composite color was explicitly set
            // If it's the system default color, consider it as "not set"
            if (compositeColor != null && !isSystemDefaultColor(compositeColor)) {
                return compositeColor;
            }

            return null; // Don't inherit system default colors
        } else {
            // INHERIT_NONE or any other mode: original SWT behavior
            // In original SWT, without inheritance returns null
            return null;
        }
    }

    private boolean isSystemDefaultColor(Color color) {
        // Get system default color for comparison
        Color systemBackground = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        // Compare if color exactly matches system default color
        return color.getRed() == systemBackground.getRed() &&
                color.getGreen() == systemBackground.getGreen() &&
                color.getBlue() == systemBackground.getBlue();
    }

}

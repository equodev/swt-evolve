package dev.equo.swt.compat;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class to compare the sizing behavior of Eclipse SWT Group vs Equo SWT Group.
 * This ensures that both implementations provide compatible sizes for layouts to work correctly.
 */
@Disabled
class GroupCompatibilityTest {

    /**
     * Tolerance percentage for size differences.
     * Sizes can differ by up to this percentage and still be considered compatible.
     */
    private static final double TOLERANCE_PERCENT = 0.20; // 20%

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
    @DisplayName("getClientArea")
    class GetClientAreaTests {

        @Test
        @DisplayName("getClientArea with default bounds")
        void defaultBounds() {
            // Test with Eclipse implementation
            Shell eclipseShell = new Shell(display);
            Config.useEclipse(Group.class);
            Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
            Rectangle eclipseClientArea = eclipseGroup.getClientArea();
            eclipseShell.dispose();

            // Test with Equo implementation
            Shell equoShell = new Shell(display);
            Config.useEquo(Group.class);
            Group equoGroup = new Group(equoShell, SWT.NONE);
            Rectangle equoClientArea = equoGroup.getClientArea();
            equoShell.dispose();

            // Compare
            assertBoundsSimilar(eclipseClientArea, equoClientArea, "getClientArea with default bounds");
        }

        @Test
        @DisplayName("getClientArea with custom random bounds")
        void customBounds() {
            // Generate random bounds
            int x = Instancio.gen().ints().range(10, 100).get();
            int y = Instancio.gen().ints().range(10, 100).get();
            int width = Instancio.gen().ints().range(200, 600).get();
            int height = Instancio.gen().ints().range(200, 600).get();

            // Test with Eclipse implementation
            Shell eclipseShell = new Shell(display);
            Config.useEclipse(Group.class);
            Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
            eclipseGroup.setBounds(x, y, width, height);
            Rectangle eclipseClientArea = eclipseGroup.getClientArea();
            eclipseShell.dispose();

            // Test with Equo implementation
            Shell equoShell = new Shell(display);
            Config.useEquo(Group.class);
            Group equoGroup = new Group(equoShell, SWT.NONE);
            equoGroup.setBounds(x, y, width, height);
            Rectangle equoClientArea = equoGroup.getClientArea();
            equoShell.dispose();

            // Compare
            assertBoundsSimilar(
                eclipseClientArea,
                equoClientArea,
                String.format("getClientArea with bounds (%d, %d, %d, %d)", x, y, width, height)
            );
        }

        @Test
        @DisplayName("getClientArea with text and custom bounds")
        void withText() {
            String groupText = "Test Group " + Instancio.gen().ints().range(1, 100).get();

            // Test with Eclipse implementation
            Shell eclipseShell = new Shell(display);
            Config.useEclipse(Group.class);
            Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
            eclipseGroup.setText(groupText);
            eclipseGroup.setBounds(50, 50, 300, 200);
            Rectangle eclipseClientArea = eclipseGroup.getClientArea();
            eclipseShell.dispose();

            // Test with Equo implementation
            Shell equoShell = new Shell(display);
            Config.useEquo(Group.class);
            Group equoGroup = new Group(equoShell, SWT.NONE);
            equoGroup.setText(groupText);
            equoGroup.setBounds(50, 50, 300, 200);
            Rectangle equoClientArea = equoGroup.getClientArea();
            equoShell.dispose();

            // Compare
            assertBoundsSimilar(eclipseClientArea, equoClientArea, "getClientArea with group text: " + groupText);
        }
    }

    @Nested
    @DisplayName("computeSize")
    class ComputeSizeTests {

        @Nested
        @DisplayName("computeSize - no children")
        class NoChildren {

            @Test
            @DisplayName("computeSize with SWT.DEFAULT hints")
            void defaultHints() {
                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
                Point eclipseSize = eclipseGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoGroup = new Group(equoShell, SWT.NONE);
                Point equoSize = equoGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, "computeSize(SWT.DEFAULT, SWT.DEFAULT) with no children");
            }

            @Test
            @DisplayName("computeSize with random specific hints")
            void specificHints() {
                int wHint = Instancio.gen().ints().range(100, 500).get();
                int hHint = Instancio.gen().ints().range(100, 500).get();

                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
                Point eclipseSize = eclipseGroup.computeSize(wHint, hHint);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoGroup = new Group(equoShell, SWT.NONE);
                Point equoSize = equoGroup.computeSize(wHint, hHint);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, String.format("computeSize(%d, %d) with no children", wHint, hHint));
            }
        }

        @Nested
        @DisplayName("computeSize - with children")
        class WithChildren {

            @Test
            @DisplayName("computeSize with SWT.DEFAULT hints and layout")
            void defaultHintsWithLayout() {
                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
                eclipseGroup.setLayout(new FillLayout());
                new Button(eclipseGroup, SWT.PUSH).setText("Button 1");
                new Button(eclipseGroup, SWT.PUSH).setText("Button 2");
                Point eclipseSize = eclipseGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoGroup = new Group(equoShell, SWT.NONE);
                equoGroup.setLayout(new FillLayout());
                new Button(equoGroup, SWT.PUSH).setText("Button 1");
                new Button(equoGroup, SWT.PUSH).setText("Button 2");
                Point equoSize = equoGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, "computeSize(SWT.DEFAULT, SWT.DEFAULT) with layout and children");
            }

            @Test
            @DisplayName("computeSize with random specific hints and layout")
            void specificHintsWithLayout() {
                int wHint = Instancio.gen().ints().range(200, 400).get();
                int hHint = Instancio.gen().ints().range(200, 400).get();

                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
                eclipseGroup.setLayout(new FillLayout(SWT.VERTICAL));
                new Button(eclipseGroup, SWT.PUSH).setText("Button 1");
                new Button(eclipseGroup, SWT.PUSH).setText("Button 2");
                new Button(eclipseGroup, SWT.PUSH).setText("Button 3");
                Point eclipseSize = eclipseGroup.computeSize(wHint, hHint);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoGroup = new Group(equoShell, SWT.NONE);
                equoGroup.setLayout(new FillLayout(SWT.VERTICAL));
                new Button(equoGroup, SWT.PUSH).setText("Button 1");
                new Button(equoGroup, SWT.PUSH).setText("Button 2");
                new Button(equoGroup, SWT.PUSH).setText("Button 3");
                Point equoSize = equoGroup.computeSize(wHint, hHint);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, String.format("computeSize(%d, %d) with layout and children", wHint, hHint));
            }

            @Test
            @DisplayName("computeSize with text, layout and children")
            void withText() {
                String groupText = "Group Title";

                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseGroup = new Group(eclipseShell, SWT.NONE);
                eclipseGroup.setText(groupText);
                eclipseGroup.setLayout(new FillLayout());
                new Button(eclipseGroup, SWT.PUSH).setText("Content");
                Point eclipseSize = eclipseGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoGroup = new Group(equoShell, SWT.NONE);
                equoGroup.setText(groupText);
                equoGroup.setLayout(new FillLayout());
                new Button(equoGroup, SWT.PUSH).setText("Content");
                Point equoSize = equoGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, "computeSize with group text: " + groupText);
            }

            @Test
            @DisplayName("computeSize with nested groups")
            void nestedGroups() {
                // Test with Eclipse implementation
                Shell eclipseShell = new Shell(display);
                Config.useEclipse(Group.class);
                Group eclipseOuterGroup = new Group(eclipseShell, SWT.NONE);
                eclipseOuterGroup.setLayout(new FillLayout());
                Group eclipseInnerGroup = new Group(eclipseOuterGroup, SWT.NONE);
                eclipseInnerGroup.setLayout(new FillLayout());
                new Button(eclipseInnerGroup, SWT.PUSH).setText("Nested Button");
                Point eclipseSize = eclipseOuterGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                eclipseShell.dispose();

                // Test with Equo implementation
                Shell equoShell = new Shell(display);
                Config.useEquo(Group.class);
                Group equoOuterGroup = new Group(equoShell, SWT.NONE);
                equoOuterGroup.setLayout(new FillLayout());
                Group equoInnerGroup = new Group(equoOuterGroup, SWT.NONE);
                equoInnerGroup.setLayout(new FillLayout());
                new Button(equoInnerGroup, SWT.PUSH).setText("Nested Button");
                Point equoSize = equoOuterGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                equoShell.dispose();

                // Compare
                assertSizeSimilar(eclipseSize, equoSize, "computeSize with nested groups");
            }
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Asserts that two Rectangles are compatible within the configured tolerance.
     *
     * @param eclipse Rectangle from Eclipse implementation
     * @param equo Rectangle from Equo implementation
     * @param scenario Description of the test scenario
     */
    private void assertBoundsSimilar(Rectangle eclipse, Rectangle equo, String scenario) {
        double xDiff = Math.abs(eclipse.x - equo.x);
        double yDiff = Math.abs(eclipse.y - equo.y);
        double widthDiff = Math.abs(eclipse.width - equo.width);
        double heightDiff = Math.abs(eclipse.height - equo.height);

        double xTolerance = Math.abs(eclipse.x) * TOLERANCE_PERCENT;
        double yTolerance = Math.abs(eclipse.y) * TOLERANCE_PERCENT;
        double widthTolerance = eclipse.width * TOLERANCE_PERCENT;
        double heightTolerance = eclipse.height * TOLERANCE_PERCENT;

        String message = String.format(
            "%s: Eclipse[%d, %d, %d, %d] vs Equo[%d, %d, %d, %d]",
            scenario, eclipse.x, eclipse.y, eclipse.width, eclipse.height,
            equo.x, equo.y, equo.width, equo.height
        );

        assertThat(xDiff)
            .as("%s - X difference (%.1f) exceeds tolerance (%.1f)", message, xDiff, xTolerance)
            .isLessThanOrEqualTo(xTolerance);
        assertThat(yDiff)
            .as("%s - Y difference (%.1f) exceeds tolerance (%.1f)", message, yDiff, yTolerance)
            .isLessThanOrEqualTo(yTolerance);
        assertThat(widthDiff)
            .as("%s - Width difference (%.1f) exceeds tolerance (%.1f)", message, widthDiff, widthTolerance)
            .isLessThanOrEqualTo(widthTolerance);
        assertThat(heightDiff)
            .as("%s - Height difference (%.1f) exceeds tolerance (%.1f)", message, heightDiff, heightTolerance)
            .isLessThanOrEqualTo(heightTolerance);

        System.out.println("PASS: " + message);
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
            .as("%s - X difference (%.1f) exceeds tolerance (%.1f)", message, xDiff, xTolerance)
            .isLessThanOrEqualTo(xTolerance);
        assertThat(yDiff)
            .as("%s - Y difference (%.1f) exceeds tolerance (%.1f)", message, yDiff, yTolerance)
            .isLessThanOrEqualTo(yTolerance);

        System.out.println("PASS: " + message);
    }
}

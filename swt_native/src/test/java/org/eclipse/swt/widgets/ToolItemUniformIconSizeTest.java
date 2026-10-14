package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.ToolItemSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code bigger_icon_size} measures every ToolItem image as an icon of the theme's size,
 * so the render side draws every bar's icons at that size without shrinking any of them. Off, an
 * item is measured from its image.
 */
@Tag("native-unit")
class ToolItemUniformIconSizeTest {

    private static final int THEME_ICON = (int) Math.round(ToolItemSizes.ICON_SIZE);
    private static final int PAD_W = 7;
    private static final int PAD_H = 6;
    private static final int ARROW = 15;

    private ConfigFlags savedFlags;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        savedFlags = Config.getConfigFlags();
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    private static void uniform(boolean on) {
        ConfigFlags flags = new ConfigFlags();
        flags.bigger_icon_size = on;
        Config.setConfigFlags(flags);
    }

    private static Point barSize(int style, int imageSize) {
        ToolBar bar = new ToolBar(DartMocks.dartShell(), SWT.HORIZONTAL);
        ToolItem item = new ToolItem(bar, style);
        item.setImage(new Image(DartMocks.dartDisplay(), imageSize, imageSize));
        return bar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    @Test
    void off_an_item_is_measured_from_its_image() {
        uniform(false);

        assertThat(barSize(SWT.PUSH, 16)).isEqualTo(new Point(16 + PAD_W, 16 + PAD_H));
        assertThat(barSize(SWT.PUSH, 24)).isEqualTo(new Point(24 + PAD_W, 24 + PAD_H));
    }

    @Test
    void on_every_image_is_measured_at_the_theme_icon_size() {
        uniform(true);

        Point themed = new Point(THEME_ICON + PAD_W, THEME_ICON + PAD_H);
        assertThat(barSize(SWT.PUSH, 16)).isEqualTo(themed);
        assertThat(barSize(SWT.PUSH, 48)).as("a larger image measures the same").isEqualTo(themed);
        assertThat(barSize(SWT.DROP_DOWN, 16)).as("a drop-down keeps its arrow's room")
                .isEqualTo(new Point(THEME_ICON + PAD_W + ARROW, THEME_ICON + PAD_H));
    }

    @Test
    void the_system_property_turns_it_on() {
        String saved = System.getProperty("swt.evolve.bigger_icon_size");
        try {
            System.setProperty("swt.evolve.bigger_icon_size", "true");
            Config.setConfigFlags(null);
            assertThat(Config.getConfigFlags().bigger_icon_size).isTrue();
        } finally {
            if (saved == null) System.clearProperty("swt.evolve.bigger_icon_size");
            else System.setProperty("swt.evolve.bigger_icon_size", saved);
        }
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.ToolItemSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text-only ToolItem is measured the way the render side draws it: in the theme's text style, with
 * the padding measured for its style. The bar then holds every item's text at the theme's size,
 * whatever the ToolBar's font.
 */
@Tag("native-unit")
class ToolItemTextThemeSizeTest {

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
        Config.setConfigFlags(new ConfigFlags());
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    private static Point barSize(int style, int fontHeight) {
        ToolBar bar = new ToolBar(DartMocks.dartShell(), SWT.HORIZONTAL | SWT.RIGHT);
        bar.setFont(new Font(DartMocks.dartDisplay(), "Arial", fontHeight, SWT.NORMAL));
        ToolItem item = new ToolItem(bar, style);
        item.setText("Columns");
        Point measured = ToolItemSizes.computeSize((DartToolItem) item.getImpl(), SWT.DEFAULT, SWT.DEFAULT, true);
        assertThat(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT))
                .as("the bar is the item as the render side measures it")
                .isEqualTo(measured);
        return measured;
    }

    @ParameterizedTest
    @ValueSource(ints = {SWT.PUSH, SWT.CHECK, SWT.DROP_DOWN})
    void text_is_measured_in_the_theme_style_whatever_the_toolbar_font(int style) {
        assertThat(barSize(style, 8)).isEqualTo(barSize(style, 30));
    }

    private static int imageAndTextWidth() {
        ToolBar bar = new ToolBar(DartMocks.dartShell(), SWT.HORIZONTAL | SWT.RIGHT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setImage(new Image(DartMocks.dartDisplay(), 16, 16));
        item.setText("Columns");
        return Sizes.computeSize((DartToolItem) item.getImpl()).x;
    }

    @Test
    void with_an_image_and_swt_fonts_the_text_keeps_the_theme_style_while_the_bar_has_no_font() {
        int themeFonts = imageAndTextWidth();
        ConfigFlags flags = new ConfigFlags();
        flags.use_swt_fonts = true;
        Config.setConfigFlags(flags);
        assertThat(imageAndTextWidth()).isEqualTo(themeFonts);
    }
}

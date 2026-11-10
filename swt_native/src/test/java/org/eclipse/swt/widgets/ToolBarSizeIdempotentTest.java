package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A ToolBar shaped like a workbench main toolbar measures the same on every call with the same
 * input, so a layout that asks again settles instead of laying out once more.
 */
@Tag("native-unit")
class ToolBarSizeIdempotentTest {

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

    private static ToolBar mainToolBar(int barStyle) {
        ToolBar bar = new ToolBar(DartMocks.dartShell(), barStyle);
        Image icon = new Image(DartMocks.dartDisplay(), 16, 16);
        new ToolItem(bar, SWT.PUSH).setText("&Columns");
        ToolItem both = new ToolItem(bar, SWT.PUSH);
        both.setImage(icon);
        both.setText("&Run");
        ToolItem drop = new ToolItem(bar, SWT.DROP_DOWN);
        drop.setImage(icon);
        new ToolItem(bar, SWT.SEPARATOR);
        new ToolItem(bar, SWT.DROP_DOWN).setText("Debug");
        new ToolItem(bar, SWT.CHECK).setImage(icon);
        return bar;
    }

    private static List<Point> sizes(ToolBar bar) {
        List<Point> out = new ArrayList<>();
        for (ToolItem item : bar.getItems()) {
            out.add(Sizes.computeSize((DartToolItem) item.getImpl()));
        }
        out.add(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        out.add(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
        out.add(bar.computeSize(200, SWT.DEFAULT));
        out.add(bar.computeSize(SWT.DEFAULT, 30));
        return out;
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void the_bar_and_its_items_measure_the_same_on_every_call(boolean swtFonts) {
        ConfigFlags flags = new ConfigFlags();
        flags.use_swt_fonts = swtFonts;
        Config.setConfigFlags(flags);
        for (int barStyle : new int[] {SWT.FLAT | SWT.RIGHT, SWT.FLAT, SWT.FLAT | SWT.WRAP | SWT.RIGHT}) {
            ToolBar bar = mainToolBar(barStyle);
            List<Point> first = sizes(bar);
            assertThat(first).allSatisfy(p -> assertThat(p.x).isPositive());
            for (int i = 0; i < 4; i++) {
                assertThat(sizes(bar)).as("call %d, style %x", i + 2, barStyle).isEqualTo(first);
            }
            bar.setSize(bar.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            bar.layout(true, true);
            assertThat(sizes(bar)).as("after layout, style %x", barStyle).isEqualTo(first);
            bar.pack();
            assertThat(sizes(bar)).as("after pack, style %x", barStyle).isEqualTo(first);

            Font font = new Font(DartMocks.dartDisplay(), "Arial", 11, SWT.NORMAL);
            bar.setFont(font);
            List<Point> withFont = sizes(bar);
            for (int i = 0; i < 4; i++) {
                assertThat(sizes(bar)).as("with a font, call %d, style %x", i + 2, barStyle).isEqualTo(withFont);
            }
            bar.setFont(new Font(DartMocks.dartDisplay(), "Arial", 11, SWT.NORMAL));
            assertThat(sizes(bar)).as("the same font set again, style %x", barStyle).isEqualTo(withFont);
        }
    }
}

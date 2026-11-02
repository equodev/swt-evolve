package dev.equo.swt;

import dev.equo.swt.harness.WireFrames;
import dev.equo.swt.harness.WireValues;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code -Dswt.evolve.disable_swt_canvas_colors_<Class>=true|false}, keyed by the application's
 * class name as written.
 */
class ConfigThemedCanvasTest {

    private static final String GRID = "swt.evolve.disable_swt_canvas_colors_a.Grid";
    private static final String RAMP = "swt.evolve.disable_swt_canvas_colors_b.Ramp$1";
    private static final String BLANK = "swt.evolve.disable_swt_canvas_colors_c.Blank";

    @AfterEach
    void clearProperties() {
        System.clearProperty(GRID);
        System.clearProperty(RAMP);
        System.clearProperty(BLANK);
        System.clearProperty("swt.evolve.disable_swt_canvas_colors");
    }

    @Test
    void eachClassKeepsItsValueAndItsCase() {
        System.setProperty(GRID, "true");
        System.setProperty(RAMP, "false");
        System.setProperty(BLANK, " ");

        assertThat(Config.canvasColorsByClassFromProperties())
                .containsExactly(Map.entry("a.Grid", true), Map.entry("b.Ramp$1", false));
    }

    @Test
    void theGlobalFlagIsNotAClass() {
        System.setProperty("swt.evolve.disable_swt_canvas_colors", "true");

        assertThat(Config.canvasColorsByClassFromProperties()).isNull();
    }

    @Test
    void theMapStaysInJava() throws Exception {
        ConfigFlags flags = new ConfigFlags();
        flags.disable_swt_canvas_colors_by_class = Map.of("a.Grid", true);

        Map<String, Object> described = WireFrames.describe(flags);

        assertThat(WireValues.containsText(described, "disable_swt_canvas_colors_by_class"))
                .as("the client gets the outcome per GC, never the application's class names")
                .isFalse();
        assertThat(WireValues.containsText(described, "a.Grid"))
                .as("the client gets the outcome per GC, never the application's class names")
                .isFalse();
    }
}

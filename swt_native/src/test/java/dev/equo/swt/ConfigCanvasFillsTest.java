package dev.equo.swt;

import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code -Dswt.evolve.canvas_hover_fills_<Class>=RRGGBB,...} and its {@code canvas_selection_fills_}
 * twin, keyed by the application's class name as written, parsed into the colors of each role.
 */
class ConfigCanvasFillsTest {

    private static final String GRID_HOVER = Config.CANVAS_HOVER_FILLS_BY_CLASS + "a.Grid";
    private static final String GRID_SELECTION = Config.CANVAS_SELECTION_FILLS_BY_CLASS + "a.Grid";
    private static final String BLANK = Config.CANVAS_HOVER_FILLS_BY_CLASS + "c.Blank";

    @AfterEach
    void clearProperties() {
        System.clearProperty(GRID_HOVER);
        System.clearProperty(GRID_SELECTION);
        System.clearProperty(BLANK);
    }

    @Test
    void eachRoleKeepsTheColorsOfEachClass() {
        System.setProperty(GRID_HOVER, " #f6e6d6 , EEDDCC ");
        System.setProperty(GRID_SELECTION, "DBC7B4");
        System.setProperty(BLANK, " ");

        Map<String, RGB[]> hover = Config.fillsByClassFromProperties(Config.CANVAS_HOVER_FILLS_BY_CLASS);
        Map<String, RGB[]> selection = Config.fillsByClassFromProperties(Config.CANVAS_SELECTION_FILLS_BY_CLASS);

        assertThat(hover).containsOnlyKeys("a.Grid");
        assertThat(hover.get("a.Grid")).containsExactly(new RGB(0xF6, 0xE6, 0xD6), new RGB(0xEE, 0xDD, 0xCC));
        assertThat(selection).containsOnlyKeys("a.Grid");
        assertThat(selection.get("a.Grid")).containsExactly(new RGB(0xDB, 0xC7, 0xB4));
    }

    @Test
    void entriesThatAreNotASixDigitColorAreDropped() {
        assertThat(Config.fillColors("F6E6D6,FFF,ZZZZZZ")).containsExactly(new RGB(0xF6, 0xE6, 0xD6));
        assertThat(Config.fillColors("FFF")).isNull();
    }

    @Test
    void theMapsStayInJava() throws Exception {
        ConfigFlags flags = new ConfigFlags();
        RGB[] fills = {new RGB(0xF6, 0xE6, 0xD6)};
        flags.canvas_hover_fills_by_class = Collections.singletonMap("a.Grid", fills);
        flags.canvas_selection_fills_by_class = Collections.singletonMap("a.Grid", fills);

        Map<String, Object> wire = dev.equo.swt.harness.WireFrames.describe(flags);

        assertThat(List.of("canvas_hover_fills_by_class", "canvas_selection_fills_by_class", "a.Grid"))
                .as("the client gets the fills per GC, never the application's class names")
                .noneMatch(text -> WireValues.containsText(wire, text));
    }
}

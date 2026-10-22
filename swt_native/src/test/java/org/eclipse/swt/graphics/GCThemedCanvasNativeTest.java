package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code swt.evolve.disable_swt_canvas_colors_<Class>} decides per control class whether what a GC
 * draws takes the theme's colors: a grid can follow the theme while a color ramp next to it keeps
 * the colors that are its data. Each GC carries the decision for the control it draws on, or none
 * when no listed class covers it.
 */
@Tag("native-unit")
class GCThemedCanvasNativeTest {

    static class Grid extends Canvas {
        Grid(Composite parent) {
            super(parent, SWT.NONE);
        }
    }

    static class FrozenGrid extends Grid {
        FrozenGrid(Composite parent) {
            super(parent);
        }
    }

    static class Ramp extends Composite {
        Ramp(Composite parent) {
            super(parent, SWT.NONE);
        }
    }

    private ConfigFlags savedFlags;
    private Shell shell;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell(DartMocks.dartDisplay());
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    @Test
    void only_a_gc_on_the_listed_class_carries_a_decision() {
        byClass(Map.of(Grid.class.getName(), true));
        Composite view = new Composite(shell, SWT.NONE);
        Grid grid = new Grid(view);
        Composite colorBar = new Composite(view, SWT.NONE);

        assertThat(themed(grid)).isTrue();
        assertThat(themed(colorBar))
                .as("an unlisted control is left to the global flag")
                .isNull();
        assertThat(themed(view)).isNull();
    }

    @Test
    void a_gc_on_a_bare_control_inside_a_listed_one_follows_it() {
        byClass(Map.of(Grid.class.getName(), true));
        Grid grid = new Grid(shell);
        Composite cell = new Composite(grid, SWT.NONE);

        assertThat(themed(cell)).isTrue();
    }

    @Test
    void the_nearest_listed_class_wins() {
        byClass(Map.of(Grid.class.getName(), true, Ramp.class.getName(), false));
        Grid grid = new Grid(shell);
        Ramp ramp = new Ramp(grid);

        assertThat(themed(ramp))
                .as("a ramp excluded inside a themed grid keeps the application's colors")
                .isFalse();
    }

    @Test
    void a_subclass_of_a_listed_class_follows_it() {
        byClass(Map.of(Grid.class.getName(), true));

        assertThat(themed(new FrozenGrid(shell))).isTrue();
    }

    @Test
    void nothing_is_decided_without_a_listed_class() {
        byClass(null);
        Grid grid = new Grid(shell);

        assertThat(themed(grid)).isNull();
    }

    @Test
    void an_explicit_false_travels_and_no_decision_stays_off_the_wire() throws IOException {
        byClass(Map.of(Ramp.class.getName(), false));
        Ramp ramp = new Ramp(shell);
        Composite unlisted = new Composite(shell, SWT.NONE);

        assertThat(json(ramp)).contains("\"themedCanvas\":false");
        assertThat(json(unlisted)).doesNotContain("themedCanvas");
    }

    private static void byClass(Map<String, Boolean> byClass) {
        ConfigFlags flags = new ConfigFlags();
        flags.disable_swt_canvas_colors_by_class = byClass;
        Config.setConfigFlags(flags);
    }

    private static Boolean themed(Control control) {
        GC gc = new GC(control);
        try {
            return ((VGC) ((DartGC) gc.getImpl()).getValue()).getThemedCanvas();
        } finally {
            gc.dispose();
        }
    }

    private static String json(Control control) throws IOException {
        GC gc = new GC(control);
        try {
            return new String(new Serializer().to(((DartGC) gc.getImpl()).getValue()), StandardCharsets.UTF_8);
        } finally {
            gc.dispose();
        }
    }
}

package org.eclipse.swt.graphics;

import dev.equo.swt.CanvasFillRoles;
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
 * {@code swt.evolve.canvas_fill_roles_<Class>} tells a themed GC which of the application's fill
 * colors are a hover or a selection. Each GC carries the roles for the control it draws on, found
 * the way {@code disable_swt_canvas_colors_<Class>} is.
 */
@Tag("native-unit")
class GCCanvasFillRolesNativeTest {

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

    private ConfigFlags savedFlags;
    private Shell shell;

    @BeforeEach
    void setUp() {
        savedFlags = Config.getConfigFlags();
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell(DartMocks.dartDisplay());
        ConfigFlags flags = new ConfigFlags();
        flags.canvas_fill_roles_by_class = Map.of(Grid.class.getName(), CanvasFillRoles.parse("F6E6D6:hover,DBC7B4:selection"));
        Config.setConfigFlags(flags);
    }

    @AfterEach
    void tearDown() {
        Config.setConfigFlags(savedFlags);
        FlutterBridge.set(null);
    }

    @Test
    void a_gc_on_the_listed_class_a_subclass_or_a_child_carries_its_roles() {
        Grid grid = new Grid(shell);

        for (Control control : new Control[] {grid, new FrozenGrid(shell), new Composite(grid, SWT.NONE)}) {
            RGB[][] fills = fills(control);
            assertThat(fills[0]).containsExactly(new RGB(0xF6, 0xE6, 0xD6));
            assertThat(fills[1]).containsExactly(new RGB(0xDB, 0xC7, 0xB4));
        }
    }

    @Test
    void an_unlisted_control_carries_no_roles_on_the_wire() throws IOException {
        Composite unlisted = new Composite(shell, SWT.NONE);

        assertThat(fills(unlisted)[0]).isNull();
        assertThat(fills(unlisted)[1]).isNull();
        assertThat(json(unlisted)).doesNotContain("hoverFills").doesNotContain("selectionFills");
        assertThat(json(new Grid(shell)))
                .contains("\"hoverFills\":[{\"red\":246,\"green\":230,\"blue\":214}]")
                .contains("\"selectionFills\":[{\"red\":219,\"green\":199,\"blue\":180}]");
    }

    /** The hover and the selection fills a GC on {@code control} carries. */
    private static RGB[][] fills(Control control) {
        GC gc = new GC(control);
        try {
            VGC value = (VGC) ((DartGC) gc.getImpl()).getValue();
            return new RGB[][] {value.getHoverFills(), value.getSelectionFills()};
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

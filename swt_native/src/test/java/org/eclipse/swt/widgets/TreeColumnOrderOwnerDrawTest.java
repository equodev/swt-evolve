package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An owner-drawn tree's rows are drawn again where its columns now stand once their order changes,
 * as native SWT repaints the cells a reorder moves.
 */
@Tag("flutter-it")
class TreeColumnOrderOwnerDrawTest {

    private RecordingBridge bridge;
    private Display display;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a reorder of an owner-drawn tree's columns draws each cell again at its column's new place")
    void aReorderRepaintsTheCellsAtTheirNewPlace() {
        Tree tree = new Tree(new Shell(display), SWT.FULL_SELECTION);
        tree.setBounds(0, 0, 400, 200);
        tree.setHeaderVisible(true);
        for (String name : new String[] {"A", "B", "C"}) {
            TreeColumn column = new TreeColumn(tree, SWT.NONE);
            column.setText(name);
            column.setWidth(100);
            column.setMoveable(true);
        }
        List<String> painted = new ArrayList<>();
        tree.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        tree.addListener(SWT.PaintItem, e -> {
            TreeItem item = (TreeItem) e.item;
            Rectangle text = item.getTextBounds(e.index);
            e.gc.drawText(item.getText(e.index), text.x, text.y, true);
            if (e.x == item.getBounds(e.index).x) painted.add(item.getText(e.index) + "@" + e.x);
        });
        TreeItem row = new TreeItem(tree, SWT.NONE);
        row.setText(new String[] {"a", "b", "c"});
        flush();
        mounted(tree, row);
        flush();
        int b = row.getBounds(1).x;
        painted.clear();

        tree.setColumnOrder(new int[] {1, 0, 2});
        flush();

        assertThat(row.getBounds(1).x).as("the moved column now starts the row").isLessThan(b);
        assertThat(painted).as("the cells drawn after the reorder")
                .contains("b@" + row.getBounds(1).x, "a@" + row.getBounds(0).x);
    }

    /** The client mounted {@code row}'s overlay. */
    private void mounted(Tree tree, TreeItem row) {
        Event e = new Event();
        e.segments = new int[] {row.hashCode(), 0xFFFFFF};
        bridge.comm.fireContaining("Tree/" + tree.hashCode() + "/PaintItem/PaintItem", e);
    }

    private void flush() {
        while (display.readAndDispatch()) {
            // run the handlers' asyncExec and flush
        }
    }
}

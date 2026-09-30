package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A cell whose overlay asked to draw text but produced none still belongs to the model.
 *
 * <p>An app that clears {@link SWT#FOREGROUND} in {@link SWT#EraseItem} and then draws its cell
 * text through a {@code TextLayout} whose text is empty asks for a text draw that puts no text op
 * in the overlay. {@code getTexts()} already answers that with the model text; {@code
 * getPaintedTexts()} has to agree, or the render side suppresses the text it was just handed and
 * the row goes blank with the grid intact.
 *
 * <p>Needs the real overlay handshake (a row is only reported painted once Flutter says its overlay
 * listens), so it runs on the whole-tree-Flutter backend.
 */
@Tag("flutter-it")
class OwnerDrawEmptyTextDrawFlutterTest {

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
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    @Test
    void tableCellKeepsItsModelText_whenTheOverlayDrewNone() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 480, 200);
        for (int i = 0; i < 4; i++) {
            new TableColumn(table, SWT.LEFT).setWidth(120);
        }
        table.addListener(SWT.EraseItem, event -> {
            event.detail &= ~SWT.FOREGROUND;
            event.gc.fillRectangle(event.x, event.y, event.width, event.height);
        });
        table.addListener(SWT.PaintItem, event -> event.gc.drawText("", 2, 2, true));

        TableItem row = new TableItem(table, SWT.NONE);
        row.setText(new String[] { "1/4", "teaspoon", "ground", "nutmeg" });
        reportOverlayListening("Table/" + table.hashCode(), row);

        DartTableItem item = (DartTableItem) row.getImpl();
        assertThat(item.getTexts())
                .as("the model text survives an overlay that drew no text")
                .containsExactly("1/4", "teaspoon", "ground", "nutmeg");
        assertThat(item.getPaintedTexts())
                .as("no cell claims the overlay paints a text the overlay never drew")
                .isEmpty();
    }

    @Test
    void tableCellStaysOverlayPainted_whenTheOverlayDrewText() {
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.NONE);
        table.setBounds(0, 0, 240, 200);
        for (int i = 0; i < 2; i++) {
            new TableColumn(table, SWT.LEFT).setWidth(120);
        }
        table.addListener(SWT.EraseItem, event -> event.detail &= ~SWT.FOREGROUND);
        table.addListener(SWT.PaintItem, event -> event.gc.drawText("Package Explorer", 2, 2, true));

        TableItem row = new TableItem(table, SWT.NONE);
        row.setText(new String[] { "Views", "Package Explorer" });
        reportOverlayListening("Table/" + table.hashCode(), row);

        DartTableItem item = (DartTableItem) row.getImpl();
        assertThat(item.getTexts()).containsExactly("Package Explorer", "Package Explorer");
        assertThat(item.getPaintedTexts())
                .as("a cell the overlay really draws must not be painted twice")
                .containsExactly(0, 1);
    }

    @Test
    void treeCellKeepsItsModelText_whenTheOverlayDrewNone() {
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.NONE);
        tree.setBounds(0, 0, 360, 200);
        for (int i = 0; i < 3; i++) {
            new TreeColumn(tree, SWT.LEFT).setWidth(120);
        }
        tree.addListener(SWT.EraseItem, event -> {
            event.detail &= ~SWT.FOREGROUND;
            event.gc.fillRectangle(event.x, event.y, event.width, event.height);
        });
        tree.addListener(SWT.PaintItem, event -> event.gc.drawText("", 2, 2, true));

        TreeItem row = new TreeItem(tree, SWT.NONE);
        row.setText(new String[] { "src", "folder", "2 KB" });
        reportOverlayListening("Tree/" + tree.hashCode(), row);

        DartTreeItem item = (DartTreeItem) row.getImpl();
        assertThat(item.getTexts())
                .as("the model text survives an overlay that drew no text")
                .containsExactly("src", "folder", "2 KB");
        assertThat(item.getPaintedTexts())
                .as("no cell claims the overlay paints a text the overlay never drew")
                .isEmpty();
    }

    private void reportOverlayListening(String parentChannel, Item row) {
        Event e = new Event();
        e.segments = new int[] { row.hashCode() };
        bridge.comm.fireContaining(parentChannel + "/PaintItem/PaintItem", e);
        while (display.readAndDispatch()) {
            // run the handler's asyncExec
        }
    }
}

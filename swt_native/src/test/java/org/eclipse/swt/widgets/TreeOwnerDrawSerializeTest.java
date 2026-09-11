package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

import dev.equo.swt.SerializeTestBase;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.eclipse.swt.widgets.Mocks.*;
import static org.mockito.Mockito.*;

/**
 * Owner-draw parity for Tree cells, mirroring {@link TableOwnerDrawSerializeTest}.
 *
 * <p>JFace's {@code OwnerDrawLabelProvider} installs {@link SWT#MeasureItem}, {@link SWT#EraseItem}
 * and {@link SWT#PaintItem} on the underlying control and never sets the item's model text — the
 * cell's content exists only as whatever {@code paint()} draws. A tree column wired to such a
 * provider therefore has a permanently null model text, so the serialized payload must prefer what
 * the PaintItem listener drew.
 *
 * <p>The shape pinned here is a component-picker tab: column 0 is a
 * {@code TreeViewerColumn} whose label provider extends {@code OwnerDrawLabelProvider} and whose
 * {@code paint()} does {@code gc.drawImage(icon, x, y)} then {@code gc.drawText(name, x, y, flags)},
 * while columns 1..n use a plain {@code ColumnLabelProvider} and go through {@code setText(index, …)}.
 * Its {@code erase()} is empty, so {@link SWT#FOREGROUND} is never suppressed.
 */
class TreeOwnerDrawSerializeTest extends SerializeTestBase {

    /**
     * A tree wired the way {@code OwnerDrawLabelProvider} wires one: all three custom-draw events
     * hooked, and an {@code erase()} that leaves {@link SWT#FOREGROUND} alone.
     */
    private DartTree ownerDrawnTree(Tree tree, int columnCount) {
        when(tree.getColumnCount()).thenReturn(columnCount);
        DartTree treeImpl = (DartTree) tree.getImpl();
        // setText(index, s) reads the column count straight off the impl field on macOS and Linux,
        // and through the API getter on Windows. Mocking only the getter leaves the field at 0
        // there, so every setText past column 0 is silently dropped and the model never gets the
        // text these tests are about.
        treeImpl.columnCount = columnCount;
        when(treeImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(treeImpl.hooks(SWT.EraseItem)).thenReturn(true);
        when(treeImpl.getItemHeight()).thenReturn(20);
        doAnswer(invocation -> null).when(treeImpl).sendEvent(eq(SWT.EraseItem), any(Event.class));
        return treeImpl;
    }

    @Test
    void ownerDrawnText_isCaptured_whenModelTextIsNeverSet() {
        Tree tree = tree();
        DartTree treeImpl = ownerDrawnTree(tree, 4);

        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 0) {
                event.gc.drawText("Arm DSP Library Source", 2, 2, true);
            }
            return null;
        }).when(treeImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TreeItem item = new TreeItem(tree, SWT.NONE);
        // Column 0 is owner-drawn, so it never goes through setText(0, …); the plain
        // ColumnLabelProvider columns do.
        item.setText(1, "1.16.2+fsp.6.5.0");
        item.setText(2, "Arm DSP Library Source");

        String json = serialize(item);

        assertThatJson(json).node("texts[0]").isEqualTo("Arm DSP Library Source");
        assertThatJson(json).node("texts[1]").isEqualTo("1.16.2+fsp.6.5.0");
        assertThatJson(json).node("texts[2]").isEqualTo("Arm DSP Library Source");
    }

    @Test
    void ownerDrawnImage_isCaptured_fromUnscaledDrawImage() {
        Tree tree = tree();
        DartTree treeImpl = ownerDrawnTree(tree, 4);

        Image icon = solidImage(16, 16);
        // The provider draws its vendor/group icon with the 3-arg overload.
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 0) {
                event.gc.drawImage(icon, 4, 2);
            }
            return null;
        }).when(treeImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TreeItem item = new TreeItem(tree, SWT.NONE);

        String json = serialize(item);

        assertThatJson(json).node("images[0]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 16)
                .containsEntry("height", 16);
    }

    @Test
    void modelText_survives_whenListenerDrawsNothingForThatCell() {
        Tree tree = tree();
        DartTree treeImpl = ownerDrawnTree(tree, 2);

        doAnswer(invocation -> null).when(treeImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TreeItem item = new TreeItem(tree, SWT.NONE);
        item.setText(new String[] { "Vendor", "6.5.0" });

        String json = serialize(item);

        assertThatJson(json).node("texts[0]").isEqualTo("Vendor");
        assertThatJson(json).node("texts[1]").isEqualTo("6.5.0");
    }

    private Image solidImage(int width, int height) {
        return new Image(device(), new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)));
    }
}

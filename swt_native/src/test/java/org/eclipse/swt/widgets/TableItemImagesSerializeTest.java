package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.eclipse.swt.widgets.Mocks.*;
import static org.mockito.Mockito.*;

/**
 * Checks the "images" field that gets serialized for a table cell - the data Flutter
 * uses to draw each cell's icon. When a cell has no image of its own but the table has
 * an SWT.PaintItem listener, that listener is asked to paint the
 * cell so its icon ends up in the serialized "images" array instead of being lost.
 */
class TableItemImagesSerializeTest extends SerializeTestBase {

    @Test
    void should_serialize_nullImages_whenTableHasNoPaintItemListener() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(2);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(false);

        TableItem item = new TableItem(table, SWT.NONE);

        String json = serialize(item);

        assertThatJson(json).node("images").isArray().hasSize(2)
                .allSatisfy(value -> assertThatJson(value).isEqualTo(null));
        verify(tableImpl, never()).sendEvent(eq(SWT.PaintItem), any(Event.class));
    }

    @Test
    void should_serialize_ownerDrawnIcon_forCellWithoutItsOwnImage() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(2);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);

        Image checkboxImage = solidImage(16, 16);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 1) {
                event.gc.drawImage(checkboxImage, 2, 2);
            }
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);

        String json = serialize(item);

        assertThatJson(json).node("images[0]").isEqualTo(null);
        assertThatJson(json).node("images[1]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 16)
                .containsEntry("height", 16);
    }

    @Test
    void should_keepOwnImage_andStillPaintOtherEmptyColumns() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(2);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);

        Image existingImage = solidImage(24, 24);
        Image checkboxImage = solidImage(16, 16);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 1) {
                event.gc.drawImage(checkboxImage, 2, 2);
            }
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        ((DartTableItem) item.getImpl()).images = new Image[] { existingImage, null };

        String json = serialize(item);

        assertThatJson(json).node("images[0]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 24)
                .containsEntry("height", 24);
        assertThatJson(json).node("images[1]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 16)
                .containsEntry("height", 16);
        verify(tableImpl, never()).sendEvent(eq(SWT.PaintItem), argThat(event -> event.index == 0));
    }

    private Image solidImage(int width, int height) {
        return new Image(device(), new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)));
    }
}
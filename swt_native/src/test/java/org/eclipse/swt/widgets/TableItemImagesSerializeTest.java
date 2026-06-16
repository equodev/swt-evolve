package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.comm.CommService;
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

    @Test
    void should_notSendDrawTextToFlutterBridge_whenPaintItemDrawsTextAndImage_forCellWithoutItsOwnImage() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(1);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);

        // Capture the comm this throwaway capture-GC talks through, so we can verify
        // no draw op is forwarded to Flutter for it without starting a real CommService.
        CommService comm = mock(CommService.class);
        when(tableImpl.getBridge()).thenReturn(new MockFlutterBridge() {
            @Override
            protected CommService comm() {
                return comm;
            }
        });

        Image valueIcon = solidImage(12, 12);
        // Mimics Katalon's profile editor MyLabelProvider: for a column with no
        // item.image of its own, PaintItem draws the cell's own text *and* an icon.
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            TableItem item = (TableItem) event.item;
            event.gc.drawText(item.getText(event.index), event.x + 2, event.y + 2, true);
            event.gc.drawImage(valueIcon, event.x + 50, event.y + 2);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, "Profile value");

        String json = serialize(item);

        assertThatJson(json).node("images[0]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 12)
                .containsEntry("height", 12);
        verify(comm, never()).send(anyString(), any(byte[].class));
    }

    @Test
    void should_firePaintItem_afterColumnImageIsCleared() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(2);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);

        Image repaintedIcon = solidImage(16, 16);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 0) {
                event.gc.drawImage(repaintedIcon, event.x, event.y);
            }
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        DartTableItem dartItem = (DartTableItem) item.getImpl();
        // Simulate setImage(nonNull) followed by setImage(null): the images array is
        // initialized with a value and then the cell is cleared.
        dartItem.images = new Image[] { solidImage(24, 24), null };
        dartItem.images[0] = null;

        String json = serialize(item);

        // Cleared column must still trigger PaintItem and capture the drawn icon.
        assertThatJson(json).node("images[0]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 16)
                .containsEntry("height", 16);
        // Column 1 has no image and PaintItem draws nothing for it → stays null.
        assertThatJson(json).node("images[1]").isEqualTo(null);
    }

    @Test
    void should_keepNull_whenPaintItemDrawsNoImage() {
        Table table = table();
        when(table.getColumnCount()).thenReturn(1);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);

        // Listener is called but never invokes drawImage.
        doAnswer(invocation -> null).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);

        String json = serialize(item);

        assertThatJson(json).node("images[0]").isEqualTo(null);
    }

    private Image solidImage(int width, int height) {
        return new Image(device(), new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)));
    }
}
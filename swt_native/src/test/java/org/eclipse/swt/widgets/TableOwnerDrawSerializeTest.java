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
 * Owner-draw parity for Table cells (issue #818, Eclipse "Find Actions").
 *
 * <p>An owner-drawing app clears {@link SWT#FOREGROUND} from the {@link SWT#EraseItem} event to tell
 * SWT "do not paint this item's own text/image — I will paint it", then paints the cell itself in
 * {@link SWT#PaintItem}. Eclipse's {@code QuickAccessEntry} does exactly that: {@code erase()} does
 * {@code detail &= ~SWT.FOREGROUND}, and {@code paint()} draws the category only on the first row of
 * each group plus the row icon. Because we render the item's model {@code texts[]}/{@code images[]}
 * verbatim, the category was repeated on every row and the icon never appeared.
 *
 * <p>These tests pin the fix: for a cell whose foreground the app suppressed, what the PaintItem
 * listener actually draws (text and image) is what gets serialized — not the model's values.
 */
class TableOwnerDrawSerializeTest extends SerializeTestBase {

    /** A table that owner-draws its cells: EraseItem clears FOREGROUND, as QuickAccessEntry does. */
    private DartTable ownerDrawnTable(Table table, int columnCount) {
        when(table.getColumnCount()).thenReturn(columnCount);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.hooks(SWT.EraseItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            event.detail &= ~SWT.FOREGROUND;
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.EraseItem), any(Event.class));
        return tableImpl;
    }

    @Test
    void ownerDrawnText_replacesModelText_whenForegroundSuppressed() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 2);

        // The QuickAccess shape: the category is set on EVERY item, but paint() only draws it for
        // the first row of a group. This item is not first, so nothing is drawn in column 0.
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 1) {
                event.gc.drawText("Package Explorer", 2, 2, true);
            }
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "Views", "Package Explorer" });

        String json = serialize(item);

        assertThatJson(json).node("texts[0]").isEqualTo("");
        assertThatJson(json).node("texts[1]").isEqualTo("Package Explorer");
    }

    @Test
    void ownerDrawnText_isCaptured_whenListenerDrawsIt() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 2);

        // First row of the group: paint() draws the category too.
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            event.gc.drawText(event.index == 0 ? "Views" : "Package Explorer", 2, 2, true);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "Views", "Package Explorer" });

        String json = serialize(item);

        assertThatJson(json).node("texts[0]").isEqualTo("Views");
        assertThatJson(json).node("texts[1]").isEqualTo("Package Explorer");
    }

    @Test
    void ownerDrawnImage_isCaptured_fromScaledDrawImage() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 2);

        Image icon = solidImage(16, 16);
        // QuickAccessEntry.paint() scales the icon with the 9-arg drawImage overload.
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            if (event.index == 1) {
                event.gc.drawImage(icon, 0, 0, 16, 16, 4, 2, 16, 16);
            }
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);

        String json = serialize(item);

        assertThatJson(json).node("images[1]").isObject()
                .node("imageData").isObject()
                .containsEntry("width", 16)
                .containsEntry("height", 16);
    }

    @Test
    void scaledDrawImage_isNotSentToFlutter_duringCapture() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 1);

        CommService comm = mock(CommService.class);
        when(tableImpl.getBridge()).thenReturn(new MockFlutterBridge() {
            @Override
            protected CommService comm() {
                return comm;
            }
        });

        Image icon = solidImage(16, 16);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            event.gc.drawImage(icon, 0, 0, 16, 16, 4, 2, 16, 16);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);

        serialize(item);

        // The scaled draw must be captured into the cell, not leaked onto the table's GC overlay.
        verify(comm, never()).send(anyString(), any(byte[].class));
    }

    private Image solidImage(int width, int height) {
        return new Image(device(), new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)));
    }
}

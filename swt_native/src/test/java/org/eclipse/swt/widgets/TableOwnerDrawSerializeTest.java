package org.eclipse.swt.widgets;

import dev.equo.swt.MockFlutterBridge;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;
import static org.mockito.Mockito.*;

/**
 * Owner-draw parity for Table cells (Eclipse "Find Actions").
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
    void ownerDrawnText_isCaptured_whenForegroundNeverSuppressed_andModelHasNoText() {
        // An app's custom TableViewer LabelProvider can override only measure()/paint(),
        // never erase() -- so EraseItem never clears SWT.FOREGROUND -- yet paint() still draws the
        // real, current value on every repaint. The column's model text is never set (owner-drawn
        // columns never go through item.setText()), so getTexts() must still prefer what PaintItem
        // drew over the (always null) model -- mirroring what getImages() already did correctly.
        Table table = table();
        when(table.getColumnCount()).thenReturn(1);
        DartTable tableImpl = (DartTable) table.getImpl();
        when(tableImpl.hooks(SWT.PaintItem)).thenReturn(true);
        when(tableImpl.hooks(SWT.EraseItem)).thenReturn(true);
        when(tableImpl.getItemHeight()).thenReturn(20);
        // EraseItem fires but never clears FOREGROUND, as a label provider with no erase() override behaves.
        doAnswer(invocation -> null).when(tableImpl).sendEvent(eq(SWT.EraseItem), any(Event.class));
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            event.gc.drawText("aaaa", 2, 2, true);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        // No item.setText(...) call -- the model stays null, as a real owner-drawn column's does.

        String json = serialize(item);

        assertThatJson(json).node("texts[0]").isEqualTo("aaaa");
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

    /**
     * An owner-drawing app may read the GC inside {@code erase()} before deciding what it paints,
     * and native SWT always supplies one. Capturing a cell must hand the EraseItem listener the
     * same GC the PaintItem listener already gets: a null one throws inside the app's listener,
     * mid-serialization of the very first frame, so nothing ever reaches Flutter.
     */
    @Test
    void eraseItemListener_getsAGc_likePaintItemDoes() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 2);

        List<GC> erased = new ArrayList<>();
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            erased.add(event.gc);
            event.gc.isClipped();
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.EraseItem), any(Event.class));
        doAnswer(invocation -> null).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "Vendor", "6.5.0" });

        serialize(item);

        assertThat(erased).isNotEmpty().doesNotContainNull();
    }

    /**
     * Native SWT measures a cell before painting it, and an owner-drawing app may compute its cell
     * layout in {@code measure()} for {@code paint()} to read back. Capturing a cell must keep that
     * order, or the app paints against state its own measure pass never filled in.
     */
    @Test
    void measureItem_runsBeforePaintItem_withAGc() {
        Table table = table();
        DartTable tableImpl = ownerDrawnTable(table, 2);
        when(tableImpl.hooks(SWT.MeasureItem)).thenReturn(true);

        List<String> order = new ArrayList<>();
        List<GC> measured = new ArrayList<>();
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            order.add("measure");
            measured.add(event.gc);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.MeasureItem), any(Event.class));
        doAnswer(invocation -> {
            order.add("paint");
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));

        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "Vendor", "6.5.0" });

        serialize(item);

        assertThat(order).containsSubsequence("measure", "paint");
        assertThat(measured).isNotEmpty().doesNotContainNull();
    }

    private Image solidImage(int width, int height) {
        return new Image(device(), new ImageData(width, height, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF)));
    }
}

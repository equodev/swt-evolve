package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.table;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * An owner-drawn row on screen is painted into its overlay with the row's first description, not a
 * round trip later when the client reports the overlay listening: its overlay mounts in the frame the
 * row arrives in. The description then names the text and image the overlay draws, which the row
 * keeps drawing until the overlay shows. A row off screen still waits for its overlay.
 */
class TableOwnerDrawPaintAheadTest extends SerializeTestBase {

    private static final int ROW = 20;
    private static final int ROWS = 40;
    /** Five rows fit in the view. */
    private static final int HEIGHT = 5 * ROW;

    private final AtomicInteger paints = new AtomicInteger();
    private DartTable impl;

    @Test
    void aRowOnScreenIsPaintedWithItsFirstDescription() {
        List<TableItem> rows = ownerDrawnRows();

        java.util.Map<String, Object> state = serialize(rows.get(2));

        assertThat(OwnerDrawOverlay.owns(rows.get(2))).isTrue();
        assertThat(paints.get()).as("captured, then painted into the overlay").isEqualTo(2);
        assertThatWire(state).node("paintedTexts").hasSize(1);
        assertThatWire(state).node("paintedTexts[0]").isEqualTo(0);
        assertThatWire(state).node("paintedImages").hasSize(1);
        assertThatWire(state).node("paintedImages[0]").isEqualTo(0);
    }

    @Test
    void aRowOffScreenWaitsForItsOverlay() {
        List<TableItem> rows = ownerDrawnRows();

        java.util.Map<String, Object> state = serialize(rows.get(30));

        assertThat(OwnerDrawOverlay.owns(rows.get(30))).isFalse();
        assertThat(paints.get()).as("captured only").isEqualTo(1);
        assertThatWire(state).node("paintedTexts").hasSize(0);
    }

    @Test
    void aRowPaintedAheadIsNotPaintedAgainBeforeItsOverlayReports() {
        List<TableItem> rows = ownerDrawnRows();
        serialize(rows.get(1));
        paints.set(0);

        serialize(rows.get(1));

        assertThat(paints.get()).as("captured only").isEqualTo(1);
    }

    @Test
    void aVirtualTableRowOnScreenIsPaintedAheadWhileTheRowsAroundItAreNotCreatedYet() {
        List<TableItem> rows = ownerDrawnRows();
        for (int i = 0; i < ROWS; i++) {
            if (i != 2 && i != 30) impl.items[i] = null;
        }

        serialize(rows.get(2));
        serialize(rows.get(30));

        assertThat(OwnerDrawOverlay.owns(rows.get(2))).as("on screen").isTrue();
        assertThat(OwnerDrawOverlay.owns(rows.get(30))).as("off screen").isFalse();
        assertThat(paints.get()).as("both captured, only the row on screen painted").isEqualTo(3);
    }

    private List<TableItem> ownerDrawnRows() {
        Table table = table();
        impl = (DartTable) table.getImpl();
        when(table.getColumnCount()).thenReturn(1);
        when(impl.hooks(SWT.PaintItem)).thenReturn(true);
        when(impl.hooks(SWT.EraseItem)).thenReturn(true);
        when(impl.getItemHeight()).thenReturn(ROW);
        when(impl.getBounds()).thenReturn(new Rectangle(0, 0, 200, HEIGHT));
        when(impl.getItemCount()).thenReturn(ROWS);
        when(impl.getApi()).thenReturn(table);
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            event.detail &= ~SWT.FOREGROUND;
            return null;
        }).when(impl).sendEvent(eq(SWT.EraseItem), any(Event.class));
        doAnswer(invocation -> {
            Event event = invocation.getArgument(1);
            paints.incrementAndGet();
            event.gc.drawText("drawn", 2, 2, true);
            return null;
        }).when(impl).sendEvent(eq(SWT.PaintItem), any(Event.class));
        List<TableItem> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            TableItem row = new TableItem(table, SWT.NONE);
            row.setText("row " + i);
            // The colors a real table resolves for a row, which the overlay's GC starts from.
            row.setForeground(new Color(table.getDisplay(), 0, 0, 0));
            row.setBackground(new Color(table.getDisplay(), 255, 255, 255));
            rows.add(row);
        }
        impl.items = rows.toArray(new TableItem[0]);
        return rows;
    }
}

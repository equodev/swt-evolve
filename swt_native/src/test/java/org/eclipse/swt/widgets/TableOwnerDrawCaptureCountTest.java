package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Owner-draw capture is what an app's SWT.PaintItem listener draws into a cell. Serializing an item
 * needs it for both texts and images, and the listener is an application callback of unbounded cost,
 * so one serialization must dispatch PaintItem at most once per cell.
 */
class TableOwnerDrawCaptureCountTest extends SerializeTestBase {

    private DartTable ownerDrawnTable(Table table, int columnCount, AtomicInteger paintItemCount) {
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
        doAnswer(invocation -> {
            paintItemCount.incrementAndGet();
            Event event = invocation.getArgument(1);
            event.gc.drawText("cell " + event.index, 2, 2, true);
            return null;
        }).when(tableImpl).sendEvent(eq(SWT.PaintItem), any(Event.class));
        return tableImpl;
    }

    @Test
    void serializingAnItem_dispatchesPaintItemOncePerColumn() {
        AtomicInteger paintItems = new AtomicInteger();
        Table table = table();
        ownerDrawnTable(table, 3, paintItems);

        TableItem item = new TableItem(table, SWT.NONE);

        serialize(item);

        assertThat(paintItems.get()).isEqualTo(3);
    }

    @Test
    void serializingAnItemTwice_dispatchesPaintItemAgain() {
        AtomicInteger paintItems = new AtomicInteger();
        Table table = table();
        ownerDrawnTable(table, 3, paintItems);

        TableItem item = new TableItem(table, SWT.NONE);

        serialize(item);
        int afterFirst = paintItems.get();
        serialize(item);

        assertThat(paintItems.get() - afterFirst).isEqualTo(3);
    }
}

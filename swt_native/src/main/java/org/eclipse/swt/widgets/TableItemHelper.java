package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GCHelper;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import java.util.Arrays;

public class TableItemHelper {

    public static void firePaintItemForAllColumns(DartTableItem item) {
        int colCount = Math.max(1, ((DartTable) item.parent.getImpl()).columnCount);
        Image[] cellImages = item.images != null ? Arrays.copyOf(item.images, colCount) : new Image[colCount];
        boolean hadCapture = false;
        for (int col = 0; col < colCount; col++) {
            GC gc = new GC(item.parent);
            GCHelper.startPaintCapture();
            try {
                Event event = new Event();
                event.item = item.getApi();
                event.gc = gc;
                event.index = col;
                event.x = 0;
                event.y = 0;
                event.width = 50;
                event.height = 20;
                ((DartWidget) item.parent.getImpl()).sendEvent(SWT.PaintItem, event);
                Image captured = GCHelper.stopPaintCapture();
                if (captured != null) {
                    cellImages[col] = captured;
                    hadCapture = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                GCHelper.stopPaintCapture();
            } finally {
                gc.dispose();
            }
        }
        if (hadCapture) {
            TableHelper.setImages(cellImages, item);
        }
    }
}
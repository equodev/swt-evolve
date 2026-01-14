package org.eclipse.swt.widgets;

import org.eclipse.swt.events.*;

public class TableHelper {

    public static void sendSelection(DartTable table, Event event, int selectionType) {
        table.setSelection(event.segments);
        table.sendSelectionEvent(selectionType, event, true);
    }

    public static void handleModify(DartTable table, Event event) {
        if (event.text != null && event.index >= 0 && event.start >= 0) {
            int rowIndex = event.index;
            int columnIndex = event.start;
            TableItem[] items = table.items;
            if (items != null && rowIndex >= 0 && rowIndex < items.length) {
                TableItem item = items[rowIndex];
                if (item != null) {
                    item.setText(columnIndex, event.text);
                    table.dirty();
                }
            }
        }
    }

}

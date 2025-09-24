package org.eclipse.swt.widgets;

public class TableHelper {

    public static void sendSelection(DartTable table, Event event, int selectionType) {
        table.setSelection(event.segments);
        table.sendSelectionEvent(selectionType, event, true);
    }

}

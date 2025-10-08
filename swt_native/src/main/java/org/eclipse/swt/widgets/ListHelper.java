package org.eclipse.swt.widgets;

public class ListHelper {

    public static void sendSelection(DartList list, Event event, int selectionType) {
        list.setSelection(event.segments);
        list.sendSelectionEvent(selectionType, event, true);
    }

}

package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

public final class MenuItemHelper {

    private MenuItemHelper() {
    }

    /**
     * Picks the item, from a click in the client or from its accelerator. The Selection is posted, as
     * every platform's MenuItem does: selectRadio() posts the deselected item's Selection, and a
     * listener applying the item it hears must hear the picked one last.
     */
    static void pick(DartMenuItem item, Event e) {
        MenuItem api = item.getApi();
        if ((api.getStyle() & SWT.CHECK) != 0) {
            item.setSelection(!item.getSelection());
        } else if ((api.getStyle() & SWT.RADIO) != 0) {
            if ((api.getParent().getStyle() & SWT.NO_RADIO_GROUP) != 0)
                item.setSelection(!item.getSelection());
            else
                item.selectRadio();
        }
        item.sendSelectionEvent(SWT.Selection, e, false);
    }
}

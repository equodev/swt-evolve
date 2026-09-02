package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import dev.equo.swt.FlutterBridge;

public class MenuHelper {

    static void hookEvents(DartMenu menu) {
        FlutterBridge.on(menu, "Help", "Help", e -> {
            menu.getDisplay().asyncExec(() -> {
                menu.sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(menu, "Menu", "Hide", e -> {
            menu.getDisplay().asyncExec(() -> onClientHid(menu, e));
        });
        FlutterBridge.on(menu, "Menu", "Show", e -> {
            menu.getDisplay().asyncExec(() -> {
                try {
                    menu.sendEvent(SWT.Show, e);
                } catch (RuntimeException ex) {
                }
                menu.dirty();
                FlutterBridge.update().whenComplete((r, ex) -> FlutterBridge.sendEvent(menu, "shown"));
            });
        });
    }

    /**
     * The client hid the popup -- an item was picked, or a click landed outside it. Natively the
     * platform owns a shown menu and taking it down is what makes {@code Menu.isVisible()} false
     * again; here the client owns it, so its notice is the same signal and has to reach the same
     * state. Without it the menu keeps reporting itself visible and gets shown straight back.
     */
    static void onClientHid(DartMenu menu, Event e) {
        if (!menu.getApi().isDisposed())
            menu.getApi().setVisible(false);
        menu.sendEvent(SWT.Hide, e);
    }

    static MenuItem[] removeItem(MenuItem[] items, MenuItem item) {
        if (items == null) return null;
        int index = 0;
        while (index < items.length) {
            if (items[index] == item) break;
            index++;
        }
        if (index == items.length) return items;
        MenuItem[] newItems = new MenuItem[items.length - 1];
        System.arraycopy(items, 0, newItems, 0, index);
        System.arraycopy(items, index + 1, newItems, index, items.length - index - 1);
        return newItems;
    }

}
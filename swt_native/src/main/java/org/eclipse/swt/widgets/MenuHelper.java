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
            menu.getDisplay().asyncExec(() -> {
                menu.sendEvent(SWT.Hide, e);
            });
        });
        FlutterBridge.on(menu, "Menu", "Show", e -> {
            menu.getDisplay().asyncExec(() -> {
                try {
                    menu.sendEvent(SWT.Show, e);
                } catch (RuntimeException ex) {
                }
                menu.dirty();
            });
        });
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
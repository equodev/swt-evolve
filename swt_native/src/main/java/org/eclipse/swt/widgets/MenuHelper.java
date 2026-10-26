package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import dev.equo.swt.FlutterBridge;

public class MenuHelper {

    static void hookEvents(DartMenu menu) {
        FlutterBridge.on(menu, "Help", "Help", e -> {
            menu.getDisplay().asyncExec(() -> {
                if (menu.isDisposed())
                    return;
                menu.sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(menu, "Menu", "Hide", e -> {
            menu.getDisplay().asyncExec(() -> onClientHid(menu, e));
        });
        FlutterBridge.on(menu, "Menu", "Show", e -> {
            menu.getDisplay().asyncExec(() -> {
                if (menu.isDisposed())
                    return;
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
        if (menu.isDisposed())
            return;
        menu.getApi().setVisible(false);
        menu.sendEvent(SWT.Hide, e);
    }

    /**
     * A pop-up takes the orientation it is given; a bar or a drop-down keeps its own, as on Win32 and GTK.
     * The orientation is a style bit, so the style is what changes and what is sent.
     */
    static void applyOrientation(DartMenu menu, int orientation) {
        Menu api = menu.getApi();
        int flags = SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT;
        int wanted = orientation & flags;
        if ((api.style & (SWT.BAR | SWT.DROP_DOWN)) != 0 || wanted == 0 || wanted == flags || (api.style & flags) == wanted)
            return;
        api.style = (api.style & ~flags) | wanted;
        menu.getValue().markDirty(VMenu.STYLE);
        menu.getValue().markDirty(VMenu.ORIENTATION);
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
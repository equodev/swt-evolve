package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import dev.equo.swt.FlutterBridge;

public class MenuHelper {

    static DartMenu activePopupMenu = null;
    static Listener focusOutListener = null;
    static Listener mouseDownFilter = null;

    static void hookEvents(DartMenu menu) {
        FlutterBridge.on(menu, "Help", "Help", e -> {
            menu.getDisplay().asyncExec(() -> {
                menu.sendEvent(SWT.Help, e);
            });
        });
        FlutterBridge.on(menu, "Menu", "Hide", e -> {
            menu.getDisplay().asyncExec(() -> {
                if (MenuHelper.activePopupMenu == menu) {
                    MenuHelper.activePopupMenu = null;
                }
                removeOwnerFocusListener(menu);
                menu.sendEvent(SWT.Hide, e);
            });
        });
        FlutterBridge.on(menu, "Menu", "Show", e -> {
            menu.getDisplay().asyncExec(() -> {
                DartMenu previous = MenuHelper.activePopupMenu;
                if (previous != null && previous != menu) {
                    FlutterBridge.sendEvent(previous, "closeMenu");
                }
                MenuHelper.activePopupMenu = menu;
                addOwnerFocusListener(menu);
                try {
                    menu.sendEvent(SWT.Show, e);
                } catch (RuntimeException ex) {
                }
                menu.dirty();
            });
        });
    }

    static void addOwnerFocusListener(DartMenu menu) {
        if (MenuHelper.focusOutListener != null) return;
        Control owner = menu.findOwnerControl();
        if (owner == null || owner.isDisposed()) return;
        MenuHelper.focusOutListener = event -> closeIfActive(menu, event.widget, owner);
        menu.getDisplay().addFilter(SWT.FocusIn, MenuHelper.focusOutListener);
        MenuHelper.mouseDownFilter = event -> {
            if (event.button == 1) closeIfActive(menu, event.widget, owner);
        };
        menu.getDisplay().addFilter(SWT.MouseDown, MenuHelper.mouseDownFilter);
    }

    static void closeIfActive(DartMenu menu, Widget clickedWidget, Control owner) {
        if (!menu.isDisposed() && MenuHelper.activePopupMenu == menu && clickedWidget != owner) {
            menu.getDisplay().asyncExec(() -> {
                if (!menu.isDisposed() && MenuHelper.activePopupMenu == menu) {
                    FlutterBridge.sendEvent(menu, "closeMenu");
                }
            });
        }
    }

    static void removeOwnerFocusListener(DartMenu menu) {
        if (MenuHelper.focusOutListener == null) return;
        menu.getDisplay().removeFilter(SWT.FocusIn, MenuHelper.focusOutListener);
        MenuHelper.focusOutListener = null;
        if (MenuHelper.mouseDownFilter != null) {
            menu.getDisplay().removeFilter(SWT.MouseDown, MenuHelper.mouseDownFilter);
            MenuHelper.mouseDownFilter = null;
        }
    }
}
package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/**
 * Menu accelerators for the Dart-backed widgets.
 *
 * <p>Natively the platform matches a key against the menu bar before the focused control sees it
 * (Win32 {@code TranslateAccelerator}, Cocoa {@code performKeyEquivalent}, a GTK accel group): the
 * matching item fires {@link SWT#Selection} and the key is consumed. Dart menus are in no platform
 * accelerator table, so the key-routing paths call {@link #translate} first.
 *
 * <p>The bar is the one of the focused control's shell. On Cocoa, where the menu bar belongs to the
 * application, a shell without one falls back to {@link Display#getMenuBar()}, as AppKit's main menu
 * does; elsewhere a shell without a bar has no accelerators. Only Dart items are matched: a native
 * item's accelerator is in its platform's table already.
 */
public final class AcceleratorHelper {

    private AcceleratorHelper() {
    }

    /** Fires the enabled menu-bar item whose accelerator is {@code key}; whether one took the key. */
    public static boolean translate(Control focus, Event key) {
        if (focus == null || focus.isDisposed() || key.keyCode == 0 || ControlHelper.isModifierKey(key.keyCode))
            return false;
        Decorations shell = focus.getImpl().menuShell();
        if (shell == null || shell.isDisposed() || !shell.isEnabled())
            return false;
        Menu bar = shell.getMenuBar();
        if (bar == null && "cocoa".equals(SWT.getPlatform()))
            bar = shell.getDisplay().getMenuBar();
        if (bar == null || bar.isDisposed() || !bar.isEnabled())
            return false;
        MenuItem item = find(bar, normalize((key.stateMask & SWT.MODIFIER_MASK) | key.keyCode));
        if (item == null)
            return false;
        Event selection = new Event();
        selection.stateMask = key.stateMask;
        selection.time = key.time;
        MenuItemHelper.pick((DartMenuItem) item.getImpl(), selection);
        return true;
    }

    /** An accelerator with its key upper-cased, so {@code MOD1 | 'n'} and {@code MOD1 | 'N'} compare equal. */
    private static int normalize(int accelerator) {
        return (accelerator & SWT.MODIFIER_MASK) | Character.toUpperCase(accelerator & SWT.KEY_MASK);
    }

    private static MenuItem find(Menu menu, int accelerator) {
        for (MenuItem item : menu.getItems()) {
            // A disabled cascade cannot be opened, so nothing under it can be picked.
            if (item.isDisposed() || !item.isEnabled())
                continue;
            int own = item.getAccelerator();
            if (own != 0 && normalize(own) == accelerator && item.getImpl() instanceof DartMenuItem)
                return item;
            Menu sub = item.getMenu();
            if (sub != null && !sub.isDisposed()) {
                MenuItem found = find(sub, accelerator);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
}

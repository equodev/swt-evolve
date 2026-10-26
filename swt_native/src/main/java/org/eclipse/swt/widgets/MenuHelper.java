package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import dev.equo.swt.FlutterBridge;

public class MenuHelper {

    /** Display data set while the last KeyDown was Alt and no other key has been pressed since. */
    private static final String ALT_ALONE = "org.eclipse.swt.internal.menuBarAltAlone";

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

    /** Notes a KeyDown for the lone Alt, which activates the menu bar when it is released before any other key. */
    static void noteKeyDown(Control control, Event key) {
        control.getDisplay().setData(ALT_ALONE, key.keyCode == SWT.ALT ? Boolean.TRUE : null);
    }

    /** A mouse press or the shell losing the keyboard ends a lone Alt, so its release does not activate the bar. */
    static void forgetLoneAlt(Display display) {
        display.setData(ALT_ALONE, null);
    }

    /**
     * Once its KeyDown went through unvetoed, F10 with no modifier activates the menu bar, as on
     * Windows, and an Alt+letter no control took opens the menu bar title carrying it.
     */
    static void keyDownDelivered(Control control, Event key) {
        if (key.keyCode == SWT.F10 && (key.stateMask & SWT.MODIFIER_MASK) == 0)
            activateMenuBar(control);
        else if (ControlHelper.isMnemonicTrigger(key))
            MnemonicHelper.openMenuBarTitle(control, (char) key.keyCode);
    }

    /** The release of a lone Alt activates the menu bar, unless a KeyUp listener vetoed it. */
    static void keyUpDelivered(Control control, Event key) {
        Display display = control.getDisplay();
        if (key.keyCode != SWT.ALT || display.getData(ALT_ALONE) == null)
            return;
        display.setData(ALT_ALONE, null);
        if (key.doit)
            activateMenuBar(control);
    }

    /** The client takes the keyboard into the shell's menu bar, with its first title current. */
    private static void activateMenuBar(Control control) {
        Menu bar = usableMenuBar(control);
        if (bar != null)
            FlutterBridge.sendEvent(bar.getImpl(), "activate");
    }

    /** The menu bar of the control's shell when the keyboard can enter it: enabled, with a title. */
    static Menu usableMenuBar(Control control) {
        Menu bar = control.getShell().getMenuBar();
        return bar != null && bar.isEnabled() && bar.getItemCount() > 0 ? bar : null;
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
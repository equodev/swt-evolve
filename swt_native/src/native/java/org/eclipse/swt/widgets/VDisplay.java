package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

/**
 * Value object for Display — serializes the display's shells to Flutter.
 * Display is NOT a Widget, so VDisplay does NOT extend VWidget.
 */
public class VDisplay {

    public long id;
    public String swt;
    public Shell[] shells;
    public Menu[] popups;

    /**
     * The application menu (macOS only, null elsewhere). The system menu bar is owned by the OS and
     * is out of reach whenever the tree renders in a browser tab, so the same menu is handed to the
     * client for the menu bar Evolve draws inside the window.
     */
    public Menu systemMenu;
    public ToolTip[] tooltips;
    public ConfigFlags config;

    /**
     * The shell SWT considers active, or 0 when none is. Filled in by the Display bridge, which
     * tracks focus; a shell only takes keyboard focus on the client when it is named here.
     */
    public long activeShellId;

    /**
     * The shell that drives (and is slaved to) the viewport, or 0 when none does. Filled in by the
     * Display bridge: parentage, modality, trim and the e4 workbench layout all decide this and
     * none of them reach the client, so the client renders the shell named here full-bleed and
     * every other shell as a window with its own chrome.
     */
    public long mainShellId;

    /**
     * The control SWT holds as its focus control, or 0 when none. Filled in by the Display bridge; the
     * client starts Tab from it when its own keyboard focus is on no control.
     */
    public long focusHolderId;

    /**
     * The shells drawn in a window of their own rather than inside this one, or null when there are
     * none. The client must leave each out of its own stack: a second Flutter client, rooted at that
     * shell, is drawing it, and rendering it here as well would put it on screen twice.
     */
    public long[] windowedShellIds;

    protected VDisplay() {
    }

    public static VDisplay of(DartDisplay display) {
        VDisplay v = new VDisplay();
        v.id = display.getApi().hashCode();
        v.swt = "Display";
        v.config = Config.getConfigFlags();
        // Every shell, not only the ones on screen. This list is how the client knows a shell
        // exists at all: a shell missing from it cannot be placed, so the frames this side sends on
        // that shell's own channel land nowhere - while the widgets written inside them are counted
        // delivered, and from then on named rather than described. A hidden shell is most of the
        // trim during startup, which is why the client was being handed names for toolbars and menu
        // items it had never been given. Whether a shell is drawn is decided from its own visible
        // flag, on the side that draws. In the order the client draws them, bottom first.
        Shell[] all = display.shellsInDrawOrder();
        ArrayList<Shell> live = new ArrayList<>();
        for (Shell s : all) {
            if (s != null && !s.isDisposed()) {
                live.add(s);
            }
        }
        v.shells = live.toArray(new Shell[0]);
        Menu[] displayPopups = display.popups;
        ArrayList<Menu> popupList = new ArrayList<>();
        if (displayPopups != null) {
            DisplayBridge bridge = display.displayBridge;
            for (Menu menu : displayPopups) {
                if (menu == null || menu.isDisposed()) {
                    continue;
                }
                // A popup opened over a shell that has a window of its own is drawn by that
                // window's client, which was sent it on the shell's own channel. Drawing it here
                // as well would put it on screen twice, in the wrong window both times.
                Shell owner = bridge == null ? null : DisplayBridge.popupShell(menu);
                if (owner != null && bridge.isShellWindowed(owner)) {
                    continue;
                }
                popupList.add(menu);
            }
        }
        v.popups = popupList.toArray(new Menu[0]);
        Menu appMenu = DisplayBridgePlatform.systemMenu(display.getApi());
        v.systemMenu = appMenu != null && !appMenu.isDisposed() ? appMenu : null;
        ToolTip[] allTooltips = display._activeTooltips();
        ArrayList<ToolTip> tooltipList = new ArrayList<>();
        if (allTooltips != null) {
            for (ToolTip t : allTooltips) {
                if (t != null && !t.isDisposed()) {
                    tooltipList.add(t);
                }
            }
        }
        v.tooltips = tooltipList.toArray(new ToolTip[0]);
        return v;
    }

    /** Property numbers, as the generated wire table gives them. */
    static final int SHELLS = dev.equo.swt.WireTable.DISPLAY_SHELLS;
    static final int POPUPS = dev.equo.swt.WireTable.DISPLAY_POPUPS;
    static final int SYSTEM_MENU = dev.equo.swt.WireTable.DISPLAY_SYSTEM_MENU;
    static final int TOOLTIPS = dev.equo.swt.WireTable.DISPLAY_TOOLTIPS;
    static final int ACTIVE_SHELL_ID = dev.equo.swt.WireTable.DISPLAY_ACTIVE_SHELL_ID;
    static final int MAIN_SHELL_ID = dev.equo.swt.WireTable.DISPLAY_MAIN_SHELL_ID;
    static final int WINDOWED_SHELL_IDS = dev.equo.swt.WireTable.DISPLAY_WINDOWED_SHELL_IDS;
    static final int CONFIG = dev.equo.swt.WireTable.DISPLAY_CONFIG;
    static final int FOCUS_HOLDER_ID = dev.equo.swt.WireTable.DISPLAY_FOCUS_HOLDER_ID;

    /**
     * The properties a record of the binary widget wire describes the display by. Not its popups,
     * which come and go far more often than the rest: they travel in a record of their own.
     */
    public static final int[] WIRE_KEYS = {
            SHELLS, SYSTEM_MENU, TOOLTIPS, ACTIVE_SHELL_ID, MAIN_SHELL_ID, WINDOWED_SHELL_IDS, CONFIG, FOCUS_HOLDER_ID,
    };

    /** Writes one of {@link #WIRE_KEYS}, as a widget's keyed writer does. */
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch (key) {
            case SHELLS:
                w.refs(shells);
                return true;
            case POPUPS:
                w.refs(popups);
                return true;
            case SYSTEM_MENU:
                w.ref(systemMenu);
                return true;
            case TOOLTIPS:
                w.refs(tooltips);
                return true;
            case ACTIVE_SHELL_ID:
                w.write(activeShellId);
                return true;
            case MAIN_SHELL_ID:
                w.write(mainShellId);
                return true;
            case WINDOWED_SHELL_IDS:
                w.write(windowedShellIds);
                return true;
            case CONFIG:
                w.object(config);
                return true;
            case FOCUS_HOLDER_ID:
                w.write(focusHolderId);
                return true;
        }
        return false;
    }

}

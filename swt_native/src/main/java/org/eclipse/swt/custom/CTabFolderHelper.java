package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Event;

public class CTabFolderHelper {
    public static void handleClose(DartCTabFolder obj, Event e) {
        if (e.index >= 0 && e.index < obj.items.length) {
            CTabItem item = obj.items[e.index];
            CTabFolderEvent closeEvent = new CTabFolderEvent(obj.getApi());
            closeEvent.item = item;
            closeEvent.doit = true;
            for (CTabFolder2Listener listener : obj.folderListeners) {
                listener.close(closeEvent);
            }
            for (CTabFolderListener listener : obj.tabListeners) {
                listener.itemClosed(closeEvent);
            }
            if (closeEvent.doit)
                item.dispose();
        }
    }

    public static void handleMinimize(DartCTabFolder obj, Event e) {
        CTabFolderEvent minimizeEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.minimize(minimizeEvent);
        }
        obj.minimized = true;
        obj.maximized = false;
    }

    public static void handleMaximize(DartCTabFolder obj, Event e) {
        CTabFolderEvent maximizeEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.maximize(maximizeEvent);
        }
        obj.maximized = true;
        obj.minimized = false;
    }

    public static void handleRestore(DartCTabFolder obj, Event e) {
        CTabFolderEvent restoreEvent = new CTabFolderEvent(obj.getApi());
        for (CTabFolder2Listener listener : obj.folderListeners) {
            listener.restore(restoreEvent);
        }
        obj.minimized = false;
        obj.maximized = false;
    }
}

package org.eclipse.swt.widgets;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * The clipboard Text and Combo copy to and paste from: the Clipboard API's, which keeps its text in memory
 * where the browser's clipboard is unavailable and mirrors it to the system's. One Clipboard per display,
 * disposed with it.
 */
final class WidgetClipboard {

    private static final String KEY = WidgetClipboard.class.getName();

    private WidgetClipboard() {
    }

    static void copy(Display display, char[] buffer) {
        if (buffer == null || buffer.length == 0)
            return;
        of(display).setContents(new Object[] {new String(buffer)}, new Transfer[] {TextTransfer.getInstance()});
    }

    static String text(Display display) {
        return (String) of(display).getContents(TextTransfer.getInstance());
    }

    private static Clipboard of(Display display) {
        Clipboard clipboard = (Clipboard) display.getData(KEY);
        if (clipboard == null || clipboard.isDisposed()) {
            Clipboard created = new Clipboard(display);
            display.setData(KEY, created);
            display.disposeExec(created::dispose);
            clipboard = created;
        }
        return clipboard;
    }
}

package org.eclipse.swt.widgets;

/** AWT's clipboard, kept out of DisplayBridge: a runtime without java.desktop must still load that. */
final class SystemClipboard {

    private SystemClipboard() {
    }

    static void write(String text) {
        try {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
        } catch (java.awt.HeadlessException | IllegalStateException ignored) {
        }
    }

    static String read() {
        try {
            Object data = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(java.awt.datatransfer.DataFlavor.stringFlavor);
            return data instanceof String ? (String) data : null;
        } catch (java.awt.HeadlessException | IllegalStateException
                | java.awt.datatransfer.UnsupportedFlavorException | java.io.IOException ignored) {
            return null;
        }
    }
}

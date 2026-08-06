package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/**
 * Alt+letter mnemonic activation for the whole-tree (Flutter) model.
 *
 * <p>A mnemonic is the {@code '&'}-marked character in a widget's text ({@code "&File"} &rarr; 'F').
 * Native SWT resolves them per-platform through {@code Decorations.traverseMnemonic}; that machinery
 * is stubbed for the Dart-backed widgets. Rather than override {@code traverseMnemonic} on every
 * generated {@code Dart*.java}, this helper walks the focused control's shell over the <em>public</em>
 * SWT widget tree, finds the control/item whose mnemonic matches, and activates it — Button clicks,
 * Label moves focus to the following control, Group focuses its first child, TabItem selects its tab.
 *
 * <p>Invoked from {@link ControlHelper#sendFlutterTraverse} after a {@code TRAVERSE_MNEMONIC} event
 * is fired and not vetoed. Uses only public API, so it is backend-agnostic and needs no generator
 * changes.
 */
public final class MnemonicHelper {

    private MnemonicHelper() {
    }

    /** Activates the widget whose mnemonic matches {@code key} within {@code focus}'s shell. */
    public static boolean dispatch(Control focus, char key) {
        if (focus == null || focus.isDisposed())
            return false;
        Shell shell = focus.getShell();
        if (shell == null || shell.isDisposed())
            return false;
        return walk(shell, Character.toLowerCase(key));
    }

    private static boolean walk(Composite parent, char target) {
        for (Control c : parent.getChildren()) {
            if (c == null || c.isDisposed() || !c.getVisible() || !c.isEnabled())
                continue;
            if (activate(c, target))
                return true;
            // Recurse into composites that did not themselves match (a Group whose own mnemonic
            // matched has already been handled and returned above).
            if (c instanceof Composite comp && walk(comp, target))
                return true;
        }
        return false;
    }

    private static boolean activate(Control c, char target) {
        if (c instanceof Button b) {
            if (!matches(b.getText(), target))
                return false;
            b.setFocus();
            if ((b.getStyle() & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) != 0)
                b.setSelection(!b.getSelection());
            b.notifyListeners(SWT.Selection, new Event());
            return true;
        }
        if (c instanceof Label l) {
            // A label cannot take focus; its mnemonic moves focus to the control it labels — the
            // next control in tab order.
            return matches(l.getText(), target) && focusAfter(l);
        }
        if (c instanceof Group g) {
            return matches(g.getText(), target) && focusFirstChild(g);
        }
        if (c instanceof Link link) {
            // A Link carries its mnemonic in its text (before any <a> markup). It is focusable, so
            // its mnemonic gives it focus — matching native SWT, where the checkbox/Link coupling
            // (e.g. a preference "&Enable …" row) is app-side listener logic, not the widget model.
            return matches(link.getText(), target) && link.setFocus();
        }
        if (c instanceof TabFolder tf) {
            for (TabItem item : tf.getItems()) {
                if (item == null || item.isDisposed() || !matches(item.getText(), target))
                    continue;
                tf.setSelection(item);
                Event e = new Event();
                e.item = item;
                tf.notifyListeners(SWT.Selection, e);
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String text, char target) {
        char m = findMnemonic(text);
        return m != 0 && Character.toLowerCase(m) == target;
    }

    /**
     * Returns the mnemonic character (the one after the first un-escaped {@code '&'}), or 0 if the
     * string has none. {@code "&&"} is a literal ampersand and does not mark a mnemonic. Mirrors
     * SWT's own {@code findMnemonic}.
     */
    public static char findMnemonic(String string) {
        if (string == null)
            return 0;
        int index = 0;
        int length = string.length();
        do {
            while (index < length && string.charAt(index) != '&') index++;
            if (++index >= length)
                return 0;
            if (string.charAt(index) != '&')
                return string.charAt(index);
            index++;
        } while (index < length);
        return 0;
    }

    private static boolean focusAfter(Control label) {
        Composite parent = label.getParent();
        if (parent == null)
            return false;
        Control[] tabList = parent.getTabList();
        Control[] order = (tabList != null && tabList.length > 0) ? tabList : parent.getChildren();
        int start = indexOf(order, label);
        for (int j = start + 1; start >= 0 && j < order.length; j++) {
            if (focusable(order[j]) && order[j].setFocus())
                return true;
        }
        return false;
    }

    private static boolean focusFirstChild(Composite group) {
        for (Control child : group.getChildren()) {
            if (focusable(child) && child.setFocus())
                return true;
        }
        return false;
    }

    private static boolean focusable(Control c) {
        return c != null && !c.isDisposed() && c.getVisible() && c.isEnabled();
    }

    private static int indexOf(Control[] arr, Control c) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == c)
                return i;
        }
        return -1;
    }
}

package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;

/**
 * Alt+letter mnemonic activation for the whole-tree (Flutter) model.
 *
 * <p>A mnemonic is the {@code '&'}-marked character in a widget's text ({@code "&File"} &rarr; 'F').
 * Native SWT resolves them per-platform through {@code Decorations.traverseMnemonic}; that machinery
 * is stubbed for the Dart-backed widgets. Rather than override {@code traverseMnemonic} on every
 * generated {@code Dart*.java}, this helper walks the focused control's shell over the <em>public</em>
 * SWT widget tree, offering the mnemonic to each control with a Traverse of its own, and activates
 * the one that takes it — Button clicks, Label moves focus to the following control, Group focuses
 * its first child, a TabItem selects its tab; a CTabFolder selects its own from its Traverse
 * listener. A mnemonic no control claims is delivered as a KeyDown, and then opens the menu bar's
 * menu whose title carries it ({@link #openMenuBarTitle}).
 *
 * <p>Invoked from {@link ControlHelper#routeKeyDown} once the focus control's own
 * {@code TRAVERSE_MNEMONIC} has run. Uses only public API, so it is backend-agnostic and needs no
 * generator changes.
 */
public final class MnemonicHelper {

    private MnemonicHelper() {
    }

    /**
     * Whether {@code key} is {@code control}'s own mnemonic: the doit a mnemonic Traverse is offered to
     * it with (Control.mnemonicMatch).
     */
    public static boolean matchesOwn(Control control, char key) {
        return ownMnemonicMatches(control, Character.toLowerCase(key));
    }

    /**
     * Acts on {@code control}'s own mnemonic, as its traverseMnemonic does once its Traverse went
     * through: only when {@code key} is that mnemonic. A selection it makes is announced with
     * {@code stateMask}, the modifiers of the key, as natively.
     */
    public static boolean activate(Control control, char key, int stateMask) {
        return control != null && !control.isDisposed() && act(control, Character.toLowerCase(key), stateMask);
    }

    /**
     * Offers a mnemonic the focus control did not take to the rest of its shell, as
     * Composite.translateMnemonic does: the shell, then each control in turn, depth first, each with a
     * Traverse of its own whose doit says whether the key is its mnemonic. The first one whose Traverse
     * still has doit set acts on it; the focus control, already asked, is not asked again.
     */
    public static boolean offer(Control focus, Event key) {
        if (focus == null || focus.isDisposed())
            return false;
        Shell shell = focus.getShell();
        if (shell == null || shell.isDisposed())
            return false;
        return offerTo(shell, focus, key);
    }

    private static boolean offerTo(Control c, Control focus, Event key) {
        // The shell is not asked whether it is shown: a key reaches only a shown shell.
        if (c.isDisposed() || c != focus.getShell() && (!c.getVisible() || !c.getEnabled()))
            return false;
        if (c != focus && traverse(c, key))
            return true;
        if (c instanceof Composite) {
            for (Control child : ((Composite) c).getChildren()) {
                if (offerTo(child, focus, key))
                    return true;
                if (focus.isDisposed())
                    return true;
            }
        }
        return false;
    }

    /** Control.traverse(Event) for a mnemonic: the Traverse, then the control's own mnemonic action. */
    private static boolean traverse(Control c, Event key) {
        char target = Character.toLowerCase((char) key.keyCode);
        Event e = new Event();
        e.character = key.character;
        e.keyCode = key.keyCode;
        e.stateMask = key.stateMask;
        e.detail = SWT.TRAVERSE_MNEMONIC;
        e.doit = ownMnemonicMatches(c, target);
        c.notifyListeners(SWT.Traverse, e);
        if (c.isDisposed())
            return true;
        if (!e.doit)
            return false;
        // A listener that consumed the key clears the detail; Control.traverse counts it as done.
        return e.detail == SWT.TRAVERSE_NONE || act(c, target, key.stateMask);
    }

    /** Whether {@code target} is the mnemonic of {@code c} itself or, for a TabFolder, of one of its tabs. */
    private static boolean ownMnemonicMatches(Control c, char target) {
        String text = mnemonicText(c);
        if (text != null)
            return matches(text, target);
        if (c instanceof TabFolder) {
            for (TabItem item : ((TabFolder) c).getItems())
                if (item != null && !item.isDisposed() && matches(item.getText(), target))
                    return true;
        }
        // A CTabFolder is offered the key with doit unset, as any custom Composite is: its own Traverse
        // listener sets it from its tabs and selects the tab itself.
        return false;
    }

    /**
     * Win32 and GTK hand a mnemonic no control took to the menu bar once its KeyDown went through
     * unvetoed, whether or not the focused control's Traverse listener rejected it. The client opens
     * the menu.
     */
    static void openMenuBarTitle(Control focus, char key) {
        Menu bar = MenuHelper.usableMenuBar(focus);
        if (bar == null)
            return;
        char target = Character.toLowerCase(key);
        for (MenuItem item : bar.getItems()) {
            if (item.isDisposed() || !item.isEnabled() || item.getMenu() == null || !matches(item.getText(), target))
                continue;
            if (!(item.getImpl() instanceof DartMenuItem))
                return;
            FlutterBridge.sendEvent(item.getImpl(), dev.equo.swt.WireTable.CHANNEL_OPEN_MENU);
            return;
        }
    }

    /** The text that carries the mnemonic of a control that holds one in its own text, or null. */
    private static String mnemonicText(Control c) {
        if (c instanceof Button)
            return ((Button) c).getText();
        if (c instanceof Label)
            return ((Label) c).getText();
        if (c instanceof Group)
            return ((Group) c).getText();
        if (c instanceof Link)
            return ((Link) c).getText();
        return null;
    }

    private static boolean act(Control c, char target, int stateMask) {
        String text = mnemonicText(c);
        if (text != null && !matches(text, target))
            return false;
        if (c instanceof Button) { Button b = (Button) c;
            b.setFocus();
            if ((b.getStyle() & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) != 0)
                b.setSelection(!b.getSelection());
            Event selection = new Event();
            selection.stateMask = stateMask;
            b.notifyListeners(SWT.Selection, selection);
            return true;
        }
        if (c instanceof Label) { Label l = (Label) c;
            // A label cannot take focus; its mnemonic moves focus to the control it labels — the
            // next control in tab order.
            return focusAfter(l);
        }
        if (c instanceof Group)
            return focusFirstChild((Group) c);
        if (c instanceof Link) { Link link = (Link) c;
            // A Link carries its mnemonic in its text (before any <a> markup). It is focusable, so
            // its mnemonic gives it focus — matching native SWT, where the checkbox/Link coupling
            // (e.g. a preference "&Enable …" row) is app-side listener logic, not the widget model.
            return link.setFocus();
        }
        if (c instanceof TabFolder) { TabFolder tf = (TabFolder) c;
            for (TabItem item : tf.getItems()) {
                if (item == null || item.isDisposed() || !matches(item.getText(), target))
                    continue;
                tf.setSelection(item);
                Event e = new Event();
                e.item = item;
                e.stateMask = stateMask;
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

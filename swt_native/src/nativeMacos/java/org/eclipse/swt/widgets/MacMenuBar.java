package org.eclipse.swt.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.internal.cocoa.id;

/**
 * Mirrors the active Shell's menu bar into the macOS system menu bar, next to the application menu
 * {@link MacApplicationMenu} installs.
 *
 * <p>On macOS the menu bar belongs to the OS, not to the window, so a Shell menu bar drawn by
 * Flutter inside the window is in the wrong place. This publishes it where the OS expects it and
 * routes a click back into SWT as {@code SWT.Selection} on the corresponding {@link MenuItem} — the
 * job {@code SwtDisplay.setMenuBar} does natively through each item's {@code nsItem}, which this
 * backend has no equivalent of.
 *
 * <p>The mirror is a subscription rather than a copy, because the SWT tree keeps changing after the
 * bar is installed: an application populates a drop-down lazily from its {@code SWT.Show} listener,
 * and renames, enables and checks items at any time. Each mirrored {@link NSMenu} therefore carries
 * this class as its delegate and is refilled from {@code menuNeedsUpdate:}, which Cocoa calls right
 * before the menu is displayed, after {@code SWT.Show} has had its chance to fill the SWT side.
 *
 * <p>Only a surface whose window this process owns reaches here -- the native window and the
 * Chromium standalone one. In a plain browser tab the system menu bar belongs to the browser, so the
 * in-window bar stays the only one there. {@code ConfigFlags#system_menu_bar} is the single answer to
 * which of the two is live, and the Flutter side reads the same flag to stand its own bar down.
 */
final class MacMenuBar {

    private static final String DELEGATE_CLASS = "EvolveMenuBarDelegate";

    /** Index 0 of the main menu is the application menu; the Shell's own menus follow it. */
    private static final int APP_MENU_ITEMS = 1;

    /** Held for the process lifetime: Cocoa only weakly references a delegate and an item target. */
    private static Callback actionCallback;

    private static Callback needsUpdateCallback;

    private static Callback didCloseCallback;

    private static id delegate;

    private static long actionSelector;

    /** What one mirrored {@link NSMenu} owns, so refilling it can take its old contents back out. */
    private static final class Mirror {

        final NSMenu ns;

        /** The cascade item, not its menu: an application replaces a drop-down by handing the item a
         *  new {@link Menu}, and a cached one would leave this mirror filling from a disposed menu
         *  forever -- the menu then opens empty and never recovers. Not final: the item itself is
         *  replaced too, and {@link #resolve} re-points this at the one standing in its place. */
        MenuItem owner;

        final List<Long> tags = new ArrayList<>();

        final List<Long> children = new ArrayList<>();

        Mirror(NSMenu ns, MenuItem owner) {
            this.ns = ns;
            this.owner = owner;
        }

        Menu swt() {
            if (owner == null || owner.isDisposed())
                return null;
            Menu menu = owner.getMenu();
            return menu != null && !menu.isDisposed() ? menu : null;
        }
    }

    /** Mirrors by {@code NSMenu} id, so a delegate callback can find the SWT menu it stands for. */
    private static final Map<Long, Mirror> mirrors = new HashMap<>();

    /** Mirrored items by the tag their {@code NSMenuItem} carries, which is how a click identifies one. */
    private static final Map<Long, MenuItem> items = new HashMap<>();

    private static long nextTag = 1;

    private static Menu installed;

    /** Last {@link #sync} that actually looked, so the event loop's call costs nothing most passes. */
    private static long lastSyncNanos;

    private static final long SYNC_INTERVAL_NANOS = 200_000_000L;

    /** The top-level row as it was last mirrored, so Cocoa's frequent update pass can skip a rebuild. */
    private static String topLevel = "";

    private MacMenuBar() {
    }

    /**
     * Publishes {@code bar} as the system menu bar. A null or itemless bar leaves the application
     * menu alone in the system bar, which is what an application with no menus should look like.
     */
    static void set(Menu bar) {
        if (!Config.getConfigFlags().system_menu_bar)
            return;
        Menu next = isShellMenuBar(bar) ? bar : null;
        boolean changed = next != installed;
        installed = next;
        rebuildTopLevel(changed);
    }

    /**
     * Brings the bar in step with the Shell's menus, from the event loop. The menu bar is the one
     * piece of the tree the OS draws rather than Flutter, so no update reaches it on its own: a menu
     * the workbench adds or withdraws would otherwise sit wrong until the user happened to open
     * another one. Rate-limited because the loop calls this on every pass; the work when nothing
     * changed is one pass over the top-level items.
     */
    static void sync() {
        if (installed == null || !Config.getConfigFlags().system_menu_bar)
            return;
        long now = System.nanoTime();
        if (now - lastSyncNanos < SYNC_INTERVAL_NANOS)
            return;
        lastSyncNanos = now;
        rebuildTopLevel(false);
    }

    /**
     * A BAR menu belonging to a Decorations. The Display's own BAR — the holder
     * {@link MacApplicationMenu#systemMenu} builds, which has no parent — is not one: its single
     * item mirrors the application menu Cocoa already carries, so installing it would put a second
     * app menu in the bar in place of the Shell's menus.
     */
    private static boolean isShellMenuBar(Menu bar) {
        return bar != null && !bar.isDisposed() && (bar.getStyle() & SWT.BAR) != 0
                && bar.getParent() != null;
    }

    /**
     * Brings the bar's row of menus in line with the Shell's.
     *
     * <p>Rebuilt whole rather than patched item by item: {@link NSMenu} is reference-counted and an
     * item taken out of the bar is freed with it, so moving one around costs a retain/release dance
     * that reaches for a freed item the moment it is got wrong. This runs between menu interactions,
     * driven by {@link #sync} from the event loop, so there is no open menu to preserve.
     */
    private static void rebuildTopLevel(boolean force) {
        NSMenu main = NSApplication.sharedApplication().mainMenu();
        if (main == null)
            return;
        String signature = topLevelSignature();
        if (!force && signature.equals(topLevel))
            return;
        topLevel = signature;
        main.setDelegate(delegate());
        for (long i = main.numberOfItems() - 1; i >= APP_MENU_ITEMS; i--) {
            NSMenu gone = main.itemAtIndex(i).submenu();
            if (gone != null)
                discard(mirrors.remove(gone.id));
            main.removeItemAtIndex(i);
        }
        for (MenuItem item : topLevelItems()) {
            String title = label(item);
            NSMenuItem nsItem = main.addItemWithTitle(NSString.string(), 0, NSString.string());
            nsItem.setTitle(NSString.stringWith(title));
            nsItem.setEnabled(item.isEnabled());
            if (isCascade(item)) {
                Mirror mirror = newMirror(title, item);
                main.setSubmenu(mirror.ns, nsItem);
                mirror.ns.release();
                fill(mirror, false);
            } else {
                bind(nsItem, item);
            }
        }
    }

    /** The bar's menus, in order. A separator between them is a Windows/Linux idea; macOS has none. */
    private static List<MenuItem> topLevelItems() {
        List<MenuItem> out = new ArrayList<>();
        if (installed == null || installed.isDisposed())
            return out;
        for (MenuItem item : installed.getItems()) {
            if (item != null && !item.isDisposed() && (item.getStyle() & SWT.SEPARATOR) == 0)
                out.add(item);
        }
        return out;
    }

    /**
     * Identity, label and enablement of every top-level menu. Cocoa asks the menu bar to update far
     * more often than the bar actually changes, and rebuilding it tears down menus that are about to
     * be opened, so a rebuild only happens when this differs from the last one.
     */
    private static String topLevelSignature() {
        if (installed == null || installed.isDisposed())
            return "";
        StringBuilder out = new StringBuilder();
        for (MenuItem item : installed.getItems()) {
            if (item == null || item.isDisposed())
                continue;
            out.append(item.hashCode()).append(':').append(label(item))
               .append(':').append(item.isEnabled()).append('\n');
        }
        return out.toString();
    }

    /**
     * A menu holder, whether or not it holds a menu yet. An application builds the bar's items
     * before the drop-downs they carry, so a cascade with a null menu is a menu that has not been
     * created yet -- not a command. Mirroring it as one gives the bar an entry that opens nothing.
     */
    private static boolean isCascade(MenuItem item) {
        if ((item.getStyle() & SWT.CASCADE) != 0)
            return true;
        Menu menu = item.getMenu();
        return menu != null && !menu.isDisposed();
    }

    private static Mirror newMirror(String title, MenuItem owner) {
        NSMenu ns = (NSMenu) new NSMenu().alloc();
        ns.initWithTitle(NSString.stringWith(title));
        // Cocoa's own enabling keeps an item live only while the responder chain implements its
        // action; these are dispatched into SWT instead, so enablement is driven from the SWT item.
        ns.setAutoenablesItems(false);
        ns.setDelegate(delegate());
        Mirror mirror = new Mirror(ns, owner);
        mirrors.put(ns.id, mirror);
        return mirror;
    }

    /**
     * Refills one mirrored menu from its SWT menu. Nested drop-downs are left empty: each is filled
     * by its own {@code menuNeedsUpdate:}, which is what keeps a deep menu tree off the cost of a
     * single click and lets every level see its {@code SWT.Show} at the moment it opens.
     */
    private static void fill(Mirror mirror, boolean sendShow) {
        Menu swt = resolve(mirror);
        if (swt == null)
            return;
        if (sendShow && swt.getImpl() instanceof DartMenu dart) {
            // An application fills a drop-down from here, and that work can throw -- a contribution
            // whose enablement expression fails, say. Mirror whatever it did manage to add rather
            // than letting the menu come up empty, and never let it reach Cocoa (see the procs).
            try {
                dart.sendEvent(SWT.Show);
            } catch (RuntimeException | Error e) {
                e.printStackTrace();
            }
            swt = mirror.swt();
            if (swt == null)
                return;
        }
        discardChildren(mirror);
        NSMenu ns = mirror.ns;
        for (long i = ns.numberOfItems() - 1; i >= 0; i--) ns.removeItemAtIndex(i);
        for (MenuItem item : swt.getItems()) {
            if (item == null || item.isDisposed())
                continue;
            if ((item.getStyle() & SWT.SEPARATOR) != 0) {
                ns.addItem(NSMenuItem.separatorItem());
                continue;
            }
            String title = label(item);
            NSMenuItem nsItem = ns.addItemWithTitle(NSString.string(), 0, NSString.string());
            nsItem.setTitle(NSString.stringWith(title));
            nsItem.setEnabled(item.isEnabled());
            if (isCascade(item)) {
                Mirror child = newMirror(title, item);
                ns.setSubmenu(child.ns, nsItem);
                child.ns.release();
                mirror.children.add(child.ns.id);
            } else {
                nsItem.setState(item.getSelection() ? OS.NSOnState : OS.NSOffState);
                accelerator(nsItem, item.getAccelerator());
                mirror.tags.add(bind(nsItem, item));
            }
        }
    }

    /**
     * The menu behind this mirror, following the application when it replaces the item carrying it.
     * A menu bar identifies a menu by its name, and an application is free to rebuild the item
     * behind that name -- JDT rebuilds Source and Refactor whenever the active editor changes. The
     * mirror would otherwise be left holding a disposed item and the menu would open empty for good.
     */
    private static Menu resolve(Mirror mirror) {
        Menu menu = mirror.swt();
        if (menu != null)
            return menu;
        String title = mirror.ns.title().getString();
        for (MenuItem item : topLevelItems()) {
            if (label(item).equals(title)) {
                mirror.owner = item;
                return mirror.swt();
            }
        }
        return null;
    }

    private static long bind(NSMenuItem nsItem, MenuItem item) {
        long tag = nextTag++;
        items.put(tag, item);
        nsItem.setTag(tag);
        nsItem.setTarget(delegate());
        nsItem.setAction(actionSelector);
        return tag;
    }

    private static void discard(Mirror mirror) {
        if (mirror == null)
            return;
        discardChildren(mirror);
    }

    private static void discardChildren(Mirror mirror) {
        for (Long tag : mirror.tags) items.remove(tag);
        mirror.tags.clear();
        for (Long child : mirror.children) discard(mirrors.remove(child));
        mirror.children.clear();
    }

    /**
     * The text as macOS shows it: no {@code &} mnemonics (the platform has none) and no accelerator
     * text after the tab, which Cocoa draws itself from the key equivalent.
     */
    private static String label(MenuItem item) {
        String text = item.getText();
        if (text == null)
            return "";
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t')
                break;
            if (c == '&') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '&')
                    i++;
                else
                    continue;
            }
            out.append(text.charAt(i));
        }
        return out.toString();
    }

    private static void accelerator(NSMenuItem nsItem, int accelerator) {
        if (accelerator == 0) {
            nsItem.setKeyEquivalent(NSString.string());
            nsItem.setKeyEquivalentModifierMask(0);
            return;
        }
        int key = accelerator & SWT.KEY_MASK;
        int virtual = keyChar(key);
        char equivalent = (char) (virtual != 0 ? virtual : key);
        nsItem.setKeyEquivalent(NSString.stringWith(String.valueOf(equivalent)).lowercaseString());
        int mask = 0;
        if ((accelerator & SWT.SHIFT) != 0)
            mask |= OS.NSShiftKeyMask;
        if ((accelerator & SWT.CONTROL) != 0)
            mask |= OS.NSControlKeyMask;
        if ((accelerator & SWT.COMMAND) != 0)
            mask |= OS.NSCommandKeyMask;
        if ((accelerator & SWT.ALT) != 0)
            mask |= OS.NSAlternateKeyMask;
        nsItem.setKeyEquivalentModifierMask(mask);
    }

    /** The Cocoa character standing for an SWT key code; 0 when the key code is the character. */
    private static int keyChar(int key) {
        switch(key) {
            case SWT.BS: return OS.NSBackspaceCharacter;
            case SWT.CR: return OS.NSCarriageReturnCharacter;
            case SWT.DEL: return OS.NSDeleteCharacter;
            case SWT.ESC: return SWT.ESC;
            case SWT.LF: return OS.NSNewlineCharacter;
            case SWT.TAB: return OS.NSTabCharacter;
            case SWT.KEYPAD_CR: return OS.NSEnterCharacter;
            case SWT.HELP: return OS.NSHelpFunctionKey;
            case SWT.ARROW_UP: return 0x2191;
            case SWT.ARROW_DOWN: return 0x2193;
            case SWT.ARROW_LEFT: return 0x2190;
            case SWT.ARROW_RIGHT: return 0x2192;
            case SWT.PAGE_UP: return 0x21DE;
            case SWT.PAGE_DOWN: return 0x21DF;
            case SWT.HOME: return 0xF729;
            case SWT.END: return 0xF72B;
            default:
                if (key >= SWT.F1 && key <= SWT.F20)
                    return 0xF704 + (key - SWT.F1);
                return 0;
        }
    }

    private static id delegate() {
        if (delegate != null)
            return delegate;
        actionSelector = OS.sel_registerName("evolveMenuAction:");
        actionCallback = new Callback(MacMenuBar.class, "actionProc", 3);
        needsUpdateCallback = new Callback(MacMenuBar.class, "needsUpdateProc", 3);
        didCloseCallback = new Callback(MacMenuBar.class, "didCloseProc", 3);
        long cls = OS.objc_lookUpClass(DELEGATE_CLASS);
        if (cls == 0) {
            cls = OS.objc_allocateClassPair(OS.class_NSObject, DELEGATE_CLASS, 0);
            OS.class_addProtocol(cls, OS.protocol_NSMenuDelegate);
            OS.class_addMethod(cls, actionSelector, actionCallback.getAddress(), "@:@");
            OS.class_addMethod(cls, OS.sel_menuNeedsUpdate_, needsUpdateCallback.getAddress(), "@:@");
            OS.class_addMethod(cls, OS.sel_menuDidClose_, didCloseCallback.getAddress(), "@:@");
            OS.objc_registerClassPair(cls);
        }
        delegate = new id(OS.objc_msgSend(OS.objc_msgSend(cls, OS.sel_alloc), OS.sel_init));
        return delegate;
    }

    // Cocoa fires all three on the main thread, which is the Display thread. Each one runs
    // application code, and an exception thrown back across the Objective-C frame that called it is
    // undefined behaviour, so none of them may propagate one.
    static long actionProc(long targetId, long sel, long sender) {
        try {
            MenuItem item = items.get(new NSMenuItem(sender).tag());
            if (item != null && !item.isDisposed() && item.getImpl() instanceof DartMenuItem dart)
                dart.sendSelection();
        } catch (RuntimeException | Error e) {
            e.printStackTrace();
        }
        return 0;
    }

    static long needsUpdateProc(long targetId, long sel, long menu) {
        try {
            NSMenu ns = new NSMenu(menu);
            if (ns.id == mainMenuId()) {
                rebuildTopLevel(false);
                return 0;
            }
            Mirror mirror = mirrors.get(ns.id);
            if (mirror != null)
                fill(mirror, true);
        } catch (RuntimeException | Error e) {
            e.printStackTrace();
        }
        return 0;
    }

    static long didCloseProc(long targetId, long sel, long menu) {
        try {
            Mirror mirror = mirrors.get(new NSMenu(menu).id);
            Menu swt = mirror != null ? mirror.swt() : null;
            if (swt != null && swt.getImpl() instanceof DartMenu dart)
                dart.sendEvent(SWT.Hide);
        } catch (RuntimeException | Error e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static long mainMenuId() {
        NSMenu main = NSApplication.sharedApplication().mainMenu();
        return main != null ? main.id : 0;
    }
}

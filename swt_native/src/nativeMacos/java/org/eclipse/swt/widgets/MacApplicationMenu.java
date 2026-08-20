package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSBundle;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.internal.cocoa.id;

/**
 * Installs the macOS application menu -- the app-name item at the left of the system menu bar and
 * its standard About/Settings/Services/Hide/Quit block.
 *
 * <p>On macOS that bar belongs to the OS, so unlike the Shell menu bar it cannot be drawn by
 * Flutter inside the window. The whole-tree backend has no {@code SwtDisplay} to run Cocoa's menu
 * setup, so without this the bar keeps only the bare item {@code NSApplication.finishLaunching()}
 * installs, which carries no About and no Settings and cannot reach SWT.
 *
 * <p>Every standard item is left with a nil target so Cocoa dispatches it up the responder chain to
 * {@code NSApp}, which implements them. Quit is the exception: it must raise {@code SWT.Close} on
 * the Display first, so the workbench can save or veto, which is what native SWT does from
 * {@code applicationShouldTerminate:}.
 *
 * <p>{@link #systemMenu} mirrors the same block as ordinary SWT widgets, for the menu bar Evolve
 * draws inside the window. That one is the only application menu a user can reach when the tree is
 * rendered in a plain browser tab, where the window — and with it the system menu bar — belongs to
 * the browser rather than to this process.
 */
final class MacApplicationMenu {

    private static final String DELEGATE_CLASS = "EvolveApplicationMenuDelegate";

    /** The NSApp main menu is process-global, so it is built once even if Displays come and go. */
    private static boolean installed;

    /** Both are held for the process lifetime: the native trampoline and the Obj-C target the menu
     * item points at are only weakly referenced from Cocoa, so dropping these would leave the Quit
     * item calling into freed memory. */
    private static Callback quitCallback;

    private static id quitTarget;

    /** Cached because the Display value object is rebuilt on every update. */
    private static Menu systemMenu;

    /** The Shell {@link #systemMenu} hangs off; the menu dies with it and has to be rebuilt. */
    private static Shell systemMenuShell;

    private MacApplicationMenu() {
    }

    static void install() {
        if (installed)
            return;
        installed = true;

        NSApplication application = NSApplication.sharedApplication();
        String appName = applicationName();
        NSString empty = NSString.string();
        NSString name = NSString.stringWith(appName);

        NSMenu mainMenu = (NSMenu) new NSMenu().alloc();
        mainMenu.initWithTitle(empty);
        // Cocoa's own enabling only keeps an item live while something in the responder chain
        // implements its action; the SWT-driven items have none, so it is done explicitly here.
        mainMenu.setAutoenablesItems(false);

        NSMenuItem appItem = mainMenu.addItemWithTitle(empty, 0, empty);
        appItem.setTitle(name);

        NSMenu appMenu = (NSMenu) new NSMenu().alloc();
        appMenu.initWithTitle(name);
        appMenu.setAutoenablesItems(false);
        // Tells Cocoa which submenu is the application menu, so it gets the bold app-name styling.
        OS.objc_msgSend(application.id, OS.sel_registerName("setAppleMenu:"), appMenu.id);

        add(appMenu, SWT.getMessage("SWT_About") + " " + appName,
                OS.sel_orderFrontStandardAboutPanel_, empty, SWT.ID_ABOUT);
        appMenu.addItem(NSMenuItem.separatorItem());
        // No action: the preference page is contributed by the application (JFace hooks it through
        // the item's SWT.ID_PREFERENCES tag), exactly as in native SWT.
        add(appMenu, SWT.getMessage("SWT_Preferences"), 0, NSString.stringWith(","), SWT.ID_PREFERENCES);
        appMenu.addItem(NSMenuItem.separatorItem());

        NSMenuItem servicesItem = add(appMenu, SWT.getMessage("SWT_Services"), 0, empty, 0);
        NSMenu servicesMenu = (NSMenu) new NSMenu().alloc();
        servicesMenu.initWithTitle(empty);
        appMenu.setSubmenu(servicesMenu, servicesItem);
        servicesMenu.release();
        application.setServicesMenu(servicesMenu);
        appMenu.addItem(NSMenuItem.separatorItem());

        add(appMenu, SWT.getMessage("SWT_Hide") + " " + appName,
                OS.sel_hide_, NSString.stringWith("h"), SWT.ID_HIDE);
        NSMenuItem hideOthers = add(appMenu, SWT.getMessage("SWT_HideOthers"),
                OS.sel_hideOtherApplications_, NSString.stringWith("h"), SWT.ID_HIDE_OTHERS);
        hideOthers.setKeyEquivalentModifierMask(OS.NSCommandKeyMask | OS.NSAlternateKeyMask);
        add(appMenu, SWT.getMessage("SWT_ShowAll"),
                OS.sel_unhideAllApplications_, empty, SWT.ID_SHOW_ALL);
        appMenu.addItem(NSMenuItem.separatorItem());

        NSMenuItem quit = add(appMenu, SWT.getMessage("SWT_Quit") + " " + appName,
                quitSelector(), NSString.stringWith("q"), SWT.ID_QUIT);
        quit.setTarget(quitTarget);

        mainMenu.setSubmenu(appMenu, appItem);
        appMenu.release();
        application.setMainMenu(mainMenu);
        mainMenu.release();
    }

    /**
     * The application menu as SWT widgets, so it serializes to the Flutter side like any other menu.
     * Services is left out: its content is filled in by Cocoa and only exists in the native bar.
     */
    static Menu systemMenu(Display display) {
        // Serializing a Menu reads its Decorations parent, so it needs a Shell to hang off even
        // though the application menu belongs to the Display.
        Shell shell = firstShell(display);
        if (shell == null)
            return null;
        if (systemMenu != null && !systemMenu.isDisposed() && systemMenuShell == shell)
            return systemMenu;
        String appName = applicationName();
        Menu holder = new Menu(shell, SWT.BAR);
        MenuItem appItem = new MenuItem(holder, SWT.CASCADE);
        appItem.setText(appName);
        Menu appMenu = new Menu(appItem);
        appItem.setMenu(appMenu);

        NSApplication application = NSApplication.sharedApplication();
        action(appMenu, SWT.getMessage("SWT_About") + " " + appName, SWT.ID_ABOUT,
                () -> application.orderFrontStandardAboutPanel(null));
        new MenuItem(appMenu, SWT.SEPARATOR);
        // No action: the application contributes the page through the SWT.ID_PREFERENCES id.
        action(appMenu, SWT.getMessage("SWT_Preferences"), SWT.ID_PREFERENCES, null);
        new MenuItem(appMenu, SWT.SEPARATOR);
        action(appMenu, SWT.getMessage("SWT_Hide") + " " + appName, SWT.ID_HIDE,
                () -> application.hide(null));
        action(appMenu, SWT.getMessage("SWT_HideOthers"), SWT.ID_HIDE_OTHERS,
                () -> application.hideOtherApplications(null));
        action(appMenu, SWT.getMessage("SWT_ShowAll"), SWT.ID_SHOW_ALL,
                () -> application.unhideAllApplications(null));
        new MenuItem(appMenu, SWT.SEPARATOR);
        action(appMenu, SWT.getMessage("SWT_Quit") + " " + appName, SWT.ID_QUIT,
                MacApplicationMenu::closeDisplay);

        ((DartDisplay) display.getImpl()).appMenu = appMenu;
        systemMenu = holder;
        systemMenuShell = shell;
        return systemMenu;
    }

    private static Shell firstShell(Display display) {
        for (Shell shell : display.getShells()) {
            if (shell != null && !shell.isDisposed())
                return shell;
        }
        return null;
    }

    private static void action(Menu menu, String text, int id, Runnable onSelection) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.setID(id);
        if (onSelection != null)
            item.addListener(SWT.Selection, event -> onSelection.run());
    }

    private static NSMenuItem add(NSMenu menu, String title, long action, NSString keyEquivalent, int tag) {
        NSMenuItem item = menu.addItemWithTitle(NSString.stringWith(title), action, keyEquivalent);
        if (tag != 0)
            item.setTag(tag);
        item.setEnabled(true);
        return item;
    }

    /**
     * Registers the Objective-C class backing the Quit item and returns its action selector. A
     * dedicated selector rather than {@code terminate:} keeps the item off NSApp's own hard
     * terminate, which would kill the process before SWT could raise {@code SWT.Close}.
     */
    private static long quitSelector() {
        long selector = OS.sel_registerName("evolveQuit:");
        quitCallback = new Callback(MacApplicationMenu.class, "quitProc", 3);
        long cls = OS.objc_lookUpClass(DELEGATE_CLASS);
        if (cls == 0) {
            cls = OS.objc_allocateClassPair(OS.class_NSObject, DELEGATE_CLASS, 0);
            OS.class_addMethod(cls, selector, quitCallback.getAddress(), "@:@");
            OS.objc_registerClassPair(cls);
        }
        quitTarget = new id(OS.objc_msgSend(OS.objc_msgSend(cls, OS.sel_alloc), OS.sel_init));
        return selector;
    }

    static long quitProc(long targetId, long sel, long arg0) {
        closeDisplay();
        return 0;
    }

    private static void closeDisplay() {
        // Cocoa fires the action on the main thread, which is the Display thread.
        Display current = Display.getCurrent();
        if (current == null || current.isDisposed()) {
            NSApplication.sharedApplication().terminate(null);
            return;
        }
        Event event = new Event();
        ((DartDisplay) current.getImpl()).sendEvent(SWT.Close, event);
        if (event.doit)
            current.dispose();
    }

    /**
     * The name shown on the app-name item. The Eclipse launcher exports it per-process; a bundled
     * app carries it in its Info.plist.
     */
    private static String applicationName() {
        if (DartDisplay.APP_NAME != null)
            return DartDisplay.APP_NAME;
        String exported = System.getenv("APP_NAME_" + OS.getpid());
        if (exported != null)
            return exported;
        id bundleName = NSBundle.mainBundle().objectForInfoDictionaryKey(NSString.stringWith("CFBundleName"));
        if (bundleName != null)
            return new NSString(bundleName).getString();
        String awtName = System.getProperty("com.apple.mrj.application.apple.menu.about.name");
        return awtName != null ? awtName : "SWT";
    }
}

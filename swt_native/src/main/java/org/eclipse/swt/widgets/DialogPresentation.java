package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;

/**
 * How a dialog Shell is presented: as an alert, a sheet in its window's single dialog frame, a page,
 * a progress card, or left a window ({@link #NONE}). Only the presentation changes: the Shell's
 * widgets, listeners and event loop are the application's, untouched.
 *
 * <p>JFace's {@code Window.createShell()} stores the JFace window in the Shell's data, so its class is
 * read by name, with no JFace dependency. The nearest class in its hierarchy that is known decides.
 */
public final class DialogPresentation {

    public static final int NONE = 0;
    public static final int ALERT = 1;
    public static final int SHEET = 2;
    public static final int PAGE = 3;
    public static final int PROGRESS = 4;

    /** Shell data an application sets to pin its Shell: {@code alert}, {@code sheet}, {@code page}, {@code progress} or {@code window}. */
    public static final String DATA_KEY = "swt.evolve.presentation";


    /** Shell data naming the dialog a shell was opened from, when that is not its parent (see {@link #opening}). */
    private static final String OPENER_KEY = "swt.evolve.opener";

    /** A window's data naming the shell of it the user acted in last. */
    private static final String ACTED_KEY = "swt.evolve.actedIn";

    /** Display data set once the user's actions are followed. */
    private static final String TRACKING_KEY = "swt.evolve.followsActions";

    /** Display data holding the value {@link #setEnabled} gave it. */
    private static final String ENABLED_KEY = "swt.evolve.modernDialogs";

    private static final int[] ACTIONS = { SWT.Selection, SWT.DefaultSelection, SWT.KeyDown, SWT.MouseDown };

    private static final String[][] KNOWN = {
            { "org.eclipse.jface.dialogs.MessageDialog", "alert" },
            { "org.eclipse.jface.dialogs.ErrorDialog", "alert" },
            { "org.eclipse.jface.dialogs.ProgressMonitorDialog", "progress" },
            { "org.eclipse.jface.wizard.WizardDialog", "page" },
            { "org.eclipse.jface.preference.PreferenceDialog", "page" },
    };

    private static final int TRIM = SWT.TITLE | SWT.CLOSE;
    private static final int MODAL = SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL;
    private static final int POPUP = SWT.NO_TRIM | SWT.ON_TOP | SWT.TOOL;

    private DialogPresentation() {
    }

    /** Whether {@code display} presents dialogs: {@code swt.evolve.modern_dialogs}, unless {@link #setEnabled} changed it. */
    public static boolean enabled(Display display) {
        Object on = display.getData(ENABLED_KEY);
        return on instanceof Boolean ? (Boolean) on : Config.getConfigFlags().modern_dialogs;
    }

    /** The presentation of {@code shell}; {@link #NONE} with {@code swt.evolve.modern_dialogs} off. */
    public static int of(Shell shell) {
        if (shell == null || shell.isDisposed() || !enabled(shell.getDisplay())) return NONE;
        Object pinned = shell.getData(DATA_KEY);
        if (pinned instanceof String) {
            int p = parse((String) pinned);
            if (p >= 0) return p;
        }
        int style = shell.getStyle();
        if (!(shell.getParent() instanceof Shell) || (style & MODAL) == 0 || (style & TRIM) == 0 || (style & POPUP) != 0) {
            return NONE;
        }
        // -Dswt.evolve.dialog_presentation=<class>=<presentation>,...: for code the application cannot change.
        int known = knownClass(shell);
        // The editor area takes the workbench window's own pages; one a dialog opens stacks in that dialog's frame.
        if (known == PAGE && parentOf(shell).getParent() instanceof Shell) return SHEET;
        return known >= 0 ? known : SHEET;
    }

    /** The flags {@code display} sends its client: the JVM's, with this display's {@link #enabled} value. */
    public static dev.equo.swt.ConfigFlags flagsFor(Display display) {
        dev.equo.swt.ConfigFlags flags = Config.getConfigFlags();
        boolean on = enabled(display);
        if (on == flags.modern_dialogs) return flags;
        flags = flags.copy();
        flags.modern_dialogs = on;
        return flags;
    }

    /**
     * Turns the presentations on or off while {@code display} runs, as {@code swt.evolve.modern_dialogs}
     * does at startup. Every shell is described again, and the client gets the flag with the display.
     */
    public static void setEnabled(Display display, boolean on) {
        display.setData(ENABLED_KEY, on);
        for (Shell shell : display.getShells()) {
            if (shell.getImpl() instanceof DartShell) ((DartShell) shell.getImpl()).getValue().markDirty(VShell.PRESENTATION);
        }
        DartDisplay dart = (DartDisplay) display.getImpl();
        if (dart.displayBridge != null) dart.displayBridge.sendDisplayUpdate(dart);
    }

    /** The shell data remembering where a dialog was first drawn, so it does not move between windows. */
    private static final String IN_PARENT_KEY = "swt.evolve.drawnInParent";

    /**
     * Whether a presented dialog of the {@code main} window is drawn inside it rather than in a window
     * of its own, whatever {@code dev.equo.swt.windows} says: what a sheet is on a desktop. Not when
     * the application asked for modal shells in windows, or its resolver or this shell's data decides ({@link dev.equo.swt.WindowPolicy}),
     * or when the dialog is larger than the window. Decided once, when the dialog is first shown.
     */
    public static boolean drawnInParent(Shell shell, Shell main) {
        Object decided = shell.getData(IN_PARENT_KEY);
        if (decided instanceof Boolean) return (Boolean) decided;
        if (main == null || of(shell) == NONE || dev.equo.swt.WindowPolicy.decidedFor(shell) != null
                || dev.equo.swt.WindowPolicy.modalShellsGetOwnWindows()) {
            return false;
        }
        if (DialogPages.window(shell) != main) return false;
        Rectangle area = main.getClientArea();
        org.eclipse.swt.graphics.Point size = shell.getSize();
        boolean inParent = DialogPages.isHosted(shell) || (size.x <= area.width && size.y <= area.height);
        if (shell.getVisible()) shell.setData(IN_PARENT_KEY, inParent);
        return inParent;
    }

    /**
     * Called as {@code shell} opens, before it is shown: a page is placed over the editor area. A
     * dialog given the window as parent while the user acted in a dialog above it (a JFace dialog
     * opened on the workbench shell from a button of another dialog) is opened from that dialog, and
     * stacks in its frame.
     */
    static void opening(Shell shell) {
        if (of(shell) == NONE) return;
        follow(shell.getDisplay());
        Object acted = DialogPages.window(shell).getData(ACTED_KEY);
        Composite parent = shell.getParent();
        if (acted instanceof Shell && acted != shell && acted != parent && !((Shell) acted).isDisposed()
                && ((Shell) acted).getVisible() && of((Shell) acted) != NONE && contains(parent, (Shell) acted)) {
            shell.setData(OPENER_KEY, acted);
        }
        if (of(shell) == PAGE) DialogPages.attach(shell);
    }

    /** Records the shell of {@code widget} as the one its window's user acted in last. */
    static void acted(Widget widget) {
        Shell shell = shellOf(widget);
        if (shell == null || shell.isDisposed()) return;
        DialogPages.window(shell).setData(ACTED_KEY, shell);
    }

    private static void follow(Display display) {
        if (display.getData(TRACKING_KEY) != null) return;
        display.setData(TRACKING_KEY, Boolean.TRUE);
        Listener listener = e -> acted(e.widget);
        for (int type : ACTIONS) display.addFilter(type, listener);
    }

    private static Shell shellOf(Widget widget) {
        if (widget == null || widget.isDisposed()) return null;
        if (widget instanceof Control) return ((Control) widget).getShell();
        if (widget instanceof MenuItem) return ((MenuItem) widget).getParent().getShell();
        if (widget instanceof ToolItem) return ((ToolItem) widget).getParent().getShell();
        return null;
    }

    /** The dialog {@code control} was opened from when it is a shell opened from one, otherwise its parent. */
    private static Composite parentOf(Control control) {
        if (control instanceof Shell) {
            Object opener = control.getData(OPENER_KEY);
            if (opener instanceof Shell && !((Shell) opener).isDisposed()) return (Shell) opener;
        }
        return control.getParent();
    }

    /**
     * The modal shell that blocks {@code target}, as SWT's {@code Shell.getModalShell} answers it, with
     * two exceptions: a page placed over the editor area blocks nothing, and a dialog a page opens
     * blocks only that page and what it opened.
     */
    static Shell modalShell(Shell target, Shell[] modalShells) {
        if (modalShells == null) return null;
        Shell shell = null;
        for (int index = modalShells.length - 1; index >= 0; index--) {
            Shell modal = modalShells[index];
            if (modal == null || modal.isDisposed() || DialogPages.isHosted(modal)) continue;
            Shell page = pageOwner(modal);
            if (page != null && !contains(page, target)) continue;
            if ((modal.getStyle() & (SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL)) != 0) {
                return contains(modal, target) ? null : modal;
            }
            if ((modal.getStyle() & SWT.PRIMARY_MODAL) != 0) {
                if (shell == null) shell = target.getShell();
                if (modal.getParent() == shell) return modal;
            }
        }
        return null;
    }

    /** The editor area a page covers, in display coordinates; null for any other shell. */
    public static Rectangle pageHost(Shell shell) {
        return DialogPages.isHosted(shell) ? DialogPages.hostBounds(shell) : null;
    }

    /** The page {@code shell} was opened from, directly or through other dialogs; null when none. */
    public static Shell pageOwner(Shell shell) {
        for (Composite p = parentOf(shell); p != null; p = parentOf(p)) {
            if (p instanceof Shell && DialogPages.isHosted((Shell) p)) return (Shell) p;
        }
        return null;
    }

    private static boolean contains(Control ancestor, Control control) {
        for (Control c = control; c != null; c = parentOf(c)) {
            if (c == ancestor) return true;
        }
        return false;
    }

    /** Shell data caching {@link #byClass} for the JFace window the shell holds. */
    private static final String CLASS_KEY = "swt.evolve.presentationByClass";

    private static int knownClass(Shell shell) {
        Object window = shell.getData();
        Object cached = shell.getData(CLASS_KEY);
        if (cached instanceof Object[] && ((Object[]) cached)[0] == window) return (Integer) ((Object[]) cached)[1];
        int known = byClass(window, Config.getConfigFlags().dialog_presentation);
        shell.setData(CLASS_KEY, new Object[] { window, known });
        return known;
    }

    /** The nearest class of {@code window}'s hierarchy an override or {@link #KNOWN} names, or -1. */
    private static int byClass(Object window, String overrides) {
        if (window == null) return -1;
        for (Class<?> c = window.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            String name = c.getName();
            int pinned = overrideFor(name, overrides);
            if (pinned >= 0) return pinned;
            for (String[] known : KNOWN) {
                if (known[0].equals(name)) return parse(known[1]);
            }
        }
        return -1;
    }

    private static int overrideFor(String className, String overrides) {
        if (overrides == null || overrides.indexOf(className) < 0) return -1;
        for (String entry : overrides.split(",")) {
            int eq = entry.indexOf('=');
            if (eq > 0 && entry.substring(0, eq).trim().equals(className)) return parse(entry.substring(eq + 1).trim());
        }
        return -1;
    }

    private static int parse(String name) {
        switch (name) {
            case "alert": return ALERT;
            case "sheet": return SHEET;
            case "page": return PAGE;
            case "progress": return PROGRESS;
            case "window": return NONE;
            default: return -1;
        }
    }
}

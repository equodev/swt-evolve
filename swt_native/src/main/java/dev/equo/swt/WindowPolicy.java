package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Decides which {@link Shell}s are hosted in a window of their own rather than drawn inside the
 * window that hosts the {@code Display}.
 *
 * <p>The whole-tree surfaces render every shell into one window by default: the main shell fills it
 * and every other shell is drawn as a floating pane with its own chrome. An application built around
 * several real top-level windows — detached views, an editor torn off into its own frame — needs the
 * other answer, so the choice is configuration rather than a fixed rendering rule.
 *
 * <p>The main shell is never its own window: it <em>is</em> the Display's window. Neither is a
 * shell with no trim, whatever the mode says — see {@link #hasTrim}.
 *
 * <h2>Configuration</h2>
 * <pre>
 * -Ddev.equo.swt.windows=single|top-level|all   (default: single — today's behaviour)
 * -Ddev.equo.swt.windows.dialogs=inline|own     (child shells)
 * -Ddev.equo.swt.windows.modal=inline|own       (modal shells)
 * </pre>
 * {@code dialogs}/{@code modal} default to {@code inline} under {@code top-level} and to
 * {@code own} under {@code all}, so each mode has a coherent default and either can still be
 * overridden on its own. A modal shell drawn inline keeps the overlay that dims what it blocks,
 * which a separate OS window cannot reproduce — hence the default.
 *
 * <p>A single shell overrides all of it through {@link #SHELL_DATA_KEY}:
 * <pre>shell.setData("dev.equo.swt.window", Boolean.TRUE);</pre>
 *
 * <p>An embedding application that needs a rule these flags cannot express installs a
 * {@link Resolver} instead.
 *
 * <h2>On the web, the browser has the last word</h2>
 * A second browser window is opened by the page, and a popup blocker can refuse it: the open
 * follows a round trip to this side, so it is never inside the user gesture a browser wants to
 * attribute it to. A refusal is reported back and the shell is drawn inside the main window
 * instead — the alternative being a shell that exists nowhere at all — so an application stays
 * usable either way, and this is configuration rather than a guarantee. Allow popups for the
 * application's origin to get the windows.
 */
public final class WindowPolicy {

    /** Which shells are eligible for a window of their own. */
    public enum Mode {
        /** Every shell is drawn inside the Display's window. */
        SINGLE,
        /** Parentless, non-modal shells get their own window. */
        TOP_LEVEL,
        /** Every shell gets its own window. */
        ALL
    }

    /** Where one kind of shell is rendered. */
    public enum Placement { INLINE, OWN }

    public static final String MODE_PROPERTY = "dev.equo.swt.windows";
    public static final String DIALOGS_PROPERTY = "dev.equo.swt.windows.dialogs";
    public static final String MODAL_PROPERTY = "dev.equo.swt.windows.modal";

    /** {@code shell.setData(SHELL_DATA_KEY, Boolean)} — overrides every flag for that one shell. */
    public static final String SHELL_DATA_KEY = "dev.equo.swt.window";

    private static final int MODAL_MASK =
            SWT.PRIMARY_MODAL | SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;

    /** The trim that makes a shell a window a user can name, move, size or dismiss. */
    private static final int TRIM_MASK =
            SWT.TITLE | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.BORDER;

    /**
     * An application-supplied rule. Returning {@code null} falls through to the flags, so a resolver
     * can answer for the shells it cares about and leave the rest alone.
     */
    public interface Resolver {
        Boolean ownsWindow(Shell shell, boolean isMainShell);
    }

    private static volatile Resolver resolver;

    /** Installs (or, with null, removes) the application's rule. */
    public static void setResolver(Resolver r) {
        resolver = r;
    }

    private WindowPolicy() {
    }

    /** The configured mode. Read live: a test or an application may set it after startup. */
    public static Mode mode() {
        String value = System.getProperty(MODE_PROPERTY);
        if (value == null) return Mode.SINGLE;
        String mode = value.trim().toLowerCase();
        if ("top-level".equals(mode) || "toplevel".equals(mode) || "top_level".equals(mode))
            return Mode.TOP_LEVEL;
        if ("all".equals(mode))
            return Mode.ALL;
        return Mode.SINGLE;
    }

    /** True when nothing can get a window of its own, so a surface can skip the whole question. */
    public static boolean isSingleWindow() {
        return mode() == Mode.SINGLE && resolver == null;
    }

    /**
     * Whether {@code shell} is hosted in a window of its own.
     *
     * @param isMainShell whether this is the shell that drives the Display's own window
     */
    public static boolean ownsWindow(Shell shell, boolean isMainShell) {
        if (shell == null || shell.isDisposed() || isMainShell) return false;

        Resolver r = resolver;
        if (r != null) {
            Boolean answer = r.ownsWindow(shell, false);
            if (answer != null) return answer;
        }

        Boolean override = shellOverride(shell);
        if (override != null) return override;

        Mode mode = mode();
        if (mode == Mode.SINGLE) return false;
        if (!hasTrim(shell)) return false;
        if (isModal(shell)) return placement(MODAL_PROPERTY, mode) == Placement.OWN;
        if (shell.getParent() != null) return placement(DIALOGS_PROPERTY, mode) == Placement.OWN;
        return true;
    }

    /** The per-shell override, or null when the shell carries none. */
    private static Boolean shellOverride(Shell shell) {
        Object data = shell.getData(SHELL_DATA_KEY);
        if (data instanceof Boolean) return (Boolean) data;
        if (data instanceof String) {
            String s = (String) data;
            if ("true".equalsIgnoreCase(s)) return Boolean.TRUE;
            if ("false".equalsIgnoreCase(s)) return Boolean.FALSE;
        }
        return null;
    }

    static boolean isModal(Shell shell) {
        return (shell.getStyle() & MODAL_MASK) != 0;
    }

    /**
     * Whether the shell asked for any window chrome at all.
     *
     * <p>A shell with no trim declared none of what makes a window: nothing to name it, nothing to
     * move or size it by, and no way for the user to close it. A toolkit uses such a shell as a
     * container rather than as a window — a splash painted edge to edge, an off-screen shell a
     * layout parks hidden widgets under — and a window of its own cannot honour that, because the
     * OS or the browser frames it regardless: the user gets chrome the shell refused, around
     * content never meant to be looked at, that it may dispose moments later. Drawn inside the
     * Display's window instead, which is the frameless floating pane such a shell asked to be.
     *
     * <p>A trimless shell that does want a window says so through {@link #SHELL_DATA_KEY} or a
     * {@link Resolver}; both are read before this.
     */
    static boolean hasTrim(Shell shell) {
        return (shell.getStyle() & TRIM_MASK) != 0;
    }

    /** An explicitly configured placement, else the one the mode implies. */
    private static Placement placement(String property, Mode mode) {
        String value = System.getProperty(property);
        if (value != null) {
            String normalized = value.trim().toLowerCase();
            if ("own".equals(normalized) || "window".equals(normalized)) return Placement.OWN;
            if ("inline".equals(normalized)) return Placement.INLINE;
        }
        return mode == Mode.ALL ? Placement.OWN : Placement.INLINE;
    }
}

package dev.equo;

import dev.equo.swt.WindowPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * Every way a shell can be placed under {@link WindowPolicy}: drawn inside the application's window,
 * or given a window of its own — a real top-level window on the desktop surface, a real browser
 * window on the web one.
 *
 * <p>Run it with the policy on the command line to compare the two renderings of the same code:
 * <pre>
 * -Ddev.equo.swt.windows=single      the default; every shell is drawn inside this window
 * -Ddev.equo.swt.windows=top-level   parentless, non-modal shells get their own window
 * -Ddev.equo.swt.windows=all         every shell does, dialogs and modals included
 *
 * -Ddev.equo.swt.windows.dialogs=inline|own   child shells, independently of the mode
 * -Ddev.equo.swt.windows.modal=inline|own     modal shells, independently of the mode
 * </pre>
 * Each button below opens one kind of shell, so the effect of a flag is visible directly: under
 * {@code top-level} the first opens a window and the rest stay here; under {@code all} they all
 * open windows except the two the last group pins.
 *
 * <p>Nothing here is conditional on the policy. That is the point: an application asks for a shell
 * the way it always has, and where it is rendered is configuration.
 *
 * <p>Run on the web with popups allowed for the application's origin, or the browser refuses the
 * windows and every shell below is drawn inside the page instead — which is the designed fallback,
 * and also exactly what this snippet shows when it happens.
 */
public class MultiWindowSnippet {

    private static int counter;

    public static void main(String[] args) {
        // Defaults for a bare launch (an IDE run with no VM arguments), so the snippet shows
        // something worth seeing on its own. Set only when nothing was given: assigning them
        // outright would override the -D flags this snippet exists to compare.
        defaultProperty("dev.equo.swt.mode", "desktop");
        defaultProperty(WindowPolicy.MODE_PROPERTY, "all");

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Multi-window — " + describePolicy());
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(720, 560);

        Label banner = new Label(shell, SWT.WRAP);
        banner.setText("Policy in force: " + describePolicy()
                + "\nRe-run with -Ddev.equo.swt.windows=single|top-level|all to compare.");
        banner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        detachableShells(shell, display);
        dialogShells(shell);
        pinnedShells(shell, display);
        resolverGroup(shell, display);

        shell.open();

        // Launched asking for multi-window, so show what that looks like without waiting for a
        // click: one detached shell alongside the application's own window.
        if (WindowPolicy.mode() != WindowPolicy.Mode.SINGLE) {
            Shell opening = new Shell(display, SWT.SHELL_TRIM);
            fill(opening, "Opened at startup " + (++counter),
                    "Opened by the snippet itself, to show the policy in effect straight away.");
            opening.setLocation(shell.getLocation().x + 240, shell.getLocation().y + 180);
            opening.open();
        }

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    /** Parentless, non-modal: the shells {@code top-level} and {@code all} both detach. */
    private static void detachableShells(Shell parent, Display display) {
        Group group = group(parent, "Top-level shells (detached under top-level and all)");

        button(group, "Open a top-level shell", () -> {
            Shell detached = new Shell(display, SWT.SHELL_TRIM);
            fill(detached, "Top-level shell " + (++counter),
                    "Parentless and non-modal. Its own window under top-level and all.");
            detached.open();
        });

        button(group, "Open three at once", () -> {
            for (int i = 0; i < 3; i++) {
                Shell detached = new Shell(display, SWT.SHELL_TRIM);
                fill(detached, "Batch shell " + (++counter),
                        "Several windows at once, all on one Display and one comm.");
                detached.setLocation(120 + i * 60, 120 + i * 60);
                detached.open();
            }
        });
    }

    /** A child shell and a modal one: governed by {@code windows.dialogs} and {@code windows.modal}. */
    private static void dialogShells(Shell parent) {
        Group group = group(parent, "Dialogs (windows.dialogs / windows.modal)");

        button(group, "Open a child shell", () -> {
            Shell child = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
            fill(child, "Child shell " + (++counter),
                    "Has a parent, so windows.dialogs decides. Inline unless asked otherwise.");
            child.open();
        });

        button(group, "Open a modal Ok/Cancel shell", () -> {
            Shell modal = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            fill(modal, "Modal shell " + (++counter),
                    "windows.modal decides. Inline by default: drawn here it keeps the overlay "
                            + "dimming what it blocks, which a separate window cannot reproduce.");
            Button ok = new Button(modal, SWT.PUSH);
            ok.setText("Ok");
            ok.addSelectionListener(widgetSelectedAdapter(e -> modal.close()));
            modal.open();
        });

        button(group, "Open a MessageBox", () -> {
            MessageBox box = new MessageBox(parent, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            box.setText("A MessageBox");
            box.setMessage("Rendered wherever the modal policy puts a modal shell.");
            box.open();
        });
    }

    /** The per-shell escape hatch, in both directions, against whatever the mode says. */
    private static void pinnedShells(Shell parent, Display display) {
        Group group = group(parent, "Per-shell override (setData)");

        button(group, "Always its own window", () -> {
            Shell pinned = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
            pinned.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
            fill(pinned, "Pinned out " + (++counter),
                    "A child shell that asked for a window of its own, so it gets one even under "
                            + "the default single-window policy.");
            pinned.open();
        });

        button(group, "Never its own window", () -> {
            Shell pinned = new Shell(display, SWT.SHELL_TRIM);
            pinned.setData(WindowPolicy.SHELL_DATA_KEY, Boolean.FALSE);
            fill(pinned, "Pinned in " + (++counter),
                    "A top-level shell that opted out, so it stays in this window even under all.");
            pinned.open();
        });
    }

    /** The programmatic rule, for an application whose answer the flags cannot express. */
    private static void resolverGroup(Shell parent, Display display) {
        Group group = group(parent, "Application rule (WindowPolicy.Resolver)");

        Button install = new Button(group, SWT.CHECK);
        install.setText("Detach only shells whose title starts with \"Detach\"");
        // The state is tracked here rather than read back with getSelection(): on a Dart-backed
        // CHECK button that getter does not follow a click, so reading it would leave the rule
        // permanently uninstalled and this whole group looking broken.
        boolean[] installed = {false};
        install.addSelectionListener(widgetSelectedAdapter(e -> {
            installed[0] = !installed[0];
            if (installed[0]) {
                // "Only" has to be said, not implied: answering null for a non-matching shell means
                // "no opinion", and the flags then decide — which under `all` detaches it anyway, so
                // an abstaining rule would look like it was being ignored. FALSE is the answer that
                // actually keeps a shell in this window whatever the mode says.
                WindowPolicy.setResolver((s, isMain) ->
                        s.getText() != null && s.getText().startsWith("Detach"));
            } else {
                WindowPolicy.setResolver(null);
            }
            System.out.println("[MultiWindow] title resolver " + (installed[0] ? "installed" : "removed"));
        }));

        button(group, "Open \"Detach me\"", () -> {
            Shell named = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
            fill(named, "Detach me " + (++counter), "The resolver matches this title.");
            named.open();
        });

        button(group, "Open \"Keep me\"", () -> {
            Shell named = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
            fill(named, "Keep me " + (++counter), "The resolver abstains, so the flags decide.");
            named.open();
        });
    }

    /** Content that makes a window recognisable, and proves it is live rather than a picture. */
    private static void fill(Shell shell, String title, String explanation) {
        shell.setText(title);
        shell.setLayout(new GridLayout(1, false));
        shell.setSize(460, 320);

        Label label = new Label(shell, SWT.WRAP);
        label.setText(explanation);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Text echo = new Text(shell, SWT.BORDER | SWT.SINGLE);
        echo.setMessage("type here — this window has its own keyboard focus");
        echo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Label count = new Label(shell, SWT.NONE);
        count.setText("characters: 0");
        echo.addModifyListener(e -> count.setText("characters: " + echo.getText().length()));

        Button close = new Button(shell, SWT.PUSH);
        close.setText("Close this shell");
        close.addSelectionListener(widgetSelectedAdapter(e -> shell.close()));

        // Proves the close gesture on a detached window runs SWT's contract rather than just
        // destroying the window: this fires with something still on screen to answer into.
        shell.addListener(SWT.Close, e -> System.out.println("[MultiWindow] SWT.Close on " + title));
        shell.addListener(SWT.Dispose, e -> System.out.println("[MultiWindow] disposed " + title));
    }

    private static Group group(Shell parent, String text) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(text);
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return group;
    }

    private static void button(Group parent, String text, Runnable action) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        button.addSelectionListener(widgetSelectedAdapter(e -> action.run()));
    }

    /** Sets {@code key} only when it carries no value, so a command-line {@code -D} still decides. */
    private static void defaultProperty(String key, String value) {
        if (System.getProperty(key) == null) System.setProperty(key, value);
    }

    private static String describePolicy() {
        StringBuilder text = new StringBuilder(WindowPolicy.mode().name().toLowerCase());
        String dialogs = System.getProperty(WindowPolicy.DIALOGS_PROPERTY);
        String modal = System.getProperty(WindowPolicy.MODAL_PROPERTY);
        if (dialogs != null) text.append(", dialogs=").append(dialogs);
        if (modal != null) text.append(", modal=").append(modal);
        return text.toString();
    }
}

package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

/** How a {@link Shell} hands the activation on, kept out of the generated implementation. */
final class ShellActivation {

    private ShellActivation() {
    }

    /** A shell taking the activation deactivates the visible one it takes it from. */
    static void deactivatePrevious(Shell previous, Shell next) {
        if (previous != null && previous != next && !previous.isDisposed() && previous.isVisible())
            deactivate((DartShell) previous.getImpl());
    }

    /** Announces the shell's Deactivate; the client reports it afterwards, and that report is not announced again. */
    static void deactivate(DartShell shell) {
        shell.setActiveControl(null);
        shell.sendEvent(SWT.Deactivate);
        shell._suppressNextFlutterDeactivate = true;
    }

    /**
     * The active shell, once hidden, is deactivated and hands the activation back to the shell
     * active before it, after its Hide, as GTK does. Every platform and the web do the same.
     */
    static void handBackAfterHide(DartShell shell) {
        Shell api = shell.getApi();
        if (api.isDisposed() || (api.state & DartWidget.HIDDEN) == 0)
            return;
        DisplayBridge bridge = shell.getBridge() instanceof DisplayBridge ? (DisplayBridge) shell.getBridge() : null;
        Control focus = api.getDisplay().getFocusControl();
        boolean holdsFocus = focus != null && !focus.isDisposed() && focus.getShell() == api;
        if (!holdsFocus && (bridge == null || !bridge.isActiveShell(api)))
            return;
        Shell returnTo = bridge != null ? bridge.shellToReturnTo(api) : null;
        Composite parent = api.getParent();
        if (returnTo == null && parent instanceof Shell && parent.isVisible() && (parent.state & DartWidget.DISPOSE_SENT) == 0)
            returnTo = (Shell) parent;
        deactivate(shell);
        if (api.isDisposed() || returnTo == null || returnTo.isDisposed() || !returnTo.isVisible())
            return;
        DartShell to = (DartShell) returnTo.getImpl();
        to._awaitClientActivate();
        to.sendEvent(SWT.Activate);
        if (!returnTo.isDisposed())
            to._takeFocusHolder(true);
    }
}

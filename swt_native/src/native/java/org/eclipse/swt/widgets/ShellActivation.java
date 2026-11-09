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

    /**
     * Announces the shell's Deactivate, then its active control's, as Win32 and GTK do; the client
     * reports it afterwards, and that report is not announced again.
     */
    static void deactivate(DartShell shell) {
        DisplayBridge bridge = shell.getBridge() instanceof DisplayBridge ? (DisplayBridge) shell.getBridge() : null;
        Shell outer = bridge != null ? bridge.markDeactivating(shell.getApi()) : null;
        try {
            shell._suppressNextFlutterDeactivate = true;
            shell.sendEvent(SWT.Deactivate);
            if (!shell.isDisposed())
                shell.setActiveControl(null);
        } finally {
            if (bridge != null)
                bridge.markDeactivating(outer);
        }
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
        // Hidden by its own Deactivate listener, as a popup closes itself: the shell taking the
        // activation from it is already being activated.
        if (bridge != null && bridge.deactivating() == api)
            return;
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
        handBack((DartShell) returnTo.getImpl());
    }

    /**
     * Gives the activation back to {@code to} once the active shell is gone: its Activate, then its
     * focus, as Win32 and GTK announce them. The client reports the same activation afterwards.
     */
    static void handBack(DartShell to) {
        to._awaitClientActivate();
        to.sendEvent(SWT.Activate);
        if (!to.isDisposed())
            to._takeFocusHolder(true);
    }
}

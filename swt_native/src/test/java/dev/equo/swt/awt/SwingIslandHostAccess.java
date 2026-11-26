package dev.equo.swt.awt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.Map;

/** The host's package-private entry points, for tests that live in {@code org.eclipse.swt.widgets}. */
public final class SwingIslandHostAccess {

    private SwingIslandHostAccess() {
    }

    public static void listen(Display display, CommService comm) {
        SwingIslandHost.listen(display, comm);
    }

    public static void openSurface(Display display, CommService comm, Map<?, ?> request, boolean swtModal) {
        SwingIslandHost.openSurface(display, comm, request, swtModal);
    }

    public static Shell surfaceShell(Display display, int windowId) {
        return SwingIslandHost.surfaceShell(display, windowId);
    }
}

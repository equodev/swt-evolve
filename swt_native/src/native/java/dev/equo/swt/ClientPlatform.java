package dev.equo.swt;

import org.eclipse.swt.internal.cloudready.DefaultDisplayResolver;
import org.eclipse.swt.widgets.DartDisplay;
import org.eclipse.swt.widgets.Display;

/**
 * The platform of the machine the user is on, named as {@code SWT.getPlatform()} names platforms
 * ({@code win32}, {@code gtk}, {@code cocoa}).
 *
 * <p>On the web platform {@code SWT.getPlatform()} answers with this for the calling thread's
 * session, and with the application's own platform where there is no session or the client has not
 * reported its OS yet. Code that needs the application's machine whatever the session asks
 * {@link ServerPlatform}.
 */
public final class ClientPlatform {

    /**
     * Key under which the {@code Display} carries its client's platform once the client has
     * reported it; absent from a standard SWT {@code Display}.
     */
    public static final String DISPLAY_DATA_KEY = "dev.equo.swt.client";

    private ClientPlatform() {
    }

    /**
     * The platform of the current thread's session client: the one {@code SWT.getPlatform()}
     * answers with on the web platform. A worker thread that carries a session but is not its UI
     * thread has no {@code Display.getCurrent()}, so the session's display is asked instead.
     */
    public static String current() {
        Display display = Display.getCurrent();
        if (display == null) {
            Object session = DefaultDisplayResolver.get();
            if (session instanceof Display)
                display = (Display) session;
        }
        return of(display);
    }

    /**
     * The platform of {@code display}'s client, or the application's own until the client has
     * reported its OS. Safe from any thread.
     */
    public static String of(Display display) {
        String platform = null;
        if (display != null && display.getImpl() instanceof DartDisplay)
            platform = fromTargetPlatform(((DartDisplay) display.getImpl()).getClientPlatform());
        return platform != null ? platform : ServerPlatform.get();
    }

    /** Whether {@code display}'s user is on a Mac, where Cmd is the shortcut modifier. */
    public static boolean isMac(Display display) {
        return "cocoa".equals(of(display));
    }

    /** The SWT platform name for a Flutter {@code TargetPlatform} name, or null when there is none. */
    static String fromTargetPlatform(String targetPlatform) {
        if (targetPlatform == null)
            return null;
        switch (targetPlatform) {
            case "macOS":
            case "iOS":
                return "cocoa";
            case "windows":
                return "win32";
            case "linux":
            case "android":
            case "fuchsia":
                return "gtk";
            default:
                return null;
        }
    }
}

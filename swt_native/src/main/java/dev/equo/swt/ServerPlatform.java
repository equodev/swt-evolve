package dev.equo.swt;

/**
 * The platform of the machine the application runs on, named as {@code SWT.getPlatform()} names
 * platforms ({@code win32}, {@code gtk}, {@code cocoa}). Unlike {@code SWT.getPlatform()}, which
 * on the web platform answers for the user's machine, this one never depends on the session.
 */
public final class ServerPlatform {

    private ServerPlatform() {
    }

    public static String get() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
        if (os.contains("win"))
            return "win32";
        if (os.contains("mac"))
            return "cocoa";
        return "gtk";
    }
}

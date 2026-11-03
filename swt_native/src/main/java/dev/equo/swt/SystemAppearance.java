package dev.equo.swt;

/**
 * The light/dark appearance of the system a Display's client runs on: the browser's on web, the OS's on
 * desktop. The client reports it with its viewport; until it does, a desktop-mode Display reads the
 * desktop's own, once. Held per Display, because one JVM serves browsers that answer differently.
 */
public final class SystemAppearance {
    private volatile String reported;
    private volatile String desktop;
    private volatile boolean desktopRead;

    /** Records what the client reported; a value that names neither variant is ignored. */
    public void report(String appearance) {
        String variant = Config.themeVariant(appearance);
        if (variant != null) reported = variant;
    }

    /** What the client reported, "dark" or "light"; null before it reports. */
    public String reported() {
        return reported;
    }

    /** The client's appearance, else the desktop's in desktop mode; null when neither is known. */
    public String get() {
        String variant = reported;
        if (variant != null || !ConfigFlags.isDesktopMode()) return variant;
        if (!desktopRead) {
            desktop = EclipseWorkspaceTheme.osAppearance.get();
            desktopRead = true;
        }
        return desktop;
    }
}

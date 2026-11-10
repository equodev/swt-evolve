package dev.equo.swt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * The light/dark appearance of the system a Display's client runs on: the browser's on web, the OS's on
 * desktop. The client reports it with its viewport; until it does, a desktop-mode Display reads the
 * desktop's own, once. Held per Display, because one JVM serves browsers that answer differently.
 */
public final class SystemAppearance {
    /** Names the thread a Display's desktop read runs on. */
    static final String READ_THREAD = "Evolve desktop appearance";

    /** The desktop's appearance lookup, replaced in tests so no real process is spawned. */
    static Supplier<String> osAppearance = DesktopAppearance::read;

    private volatile String reported;
    private CompletableFuture<String> desktop;

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
        CompletableFuture<String> read = desktopRead();
        // A wait is bounded by the command's own 2 s limit, and only Display.init() comes here before it ends.
        try (BootTrace.Span span = BootTrace.span(read.isDone() ? null : "desktopAppearance.wait")) {
            return read.join();
        }
    }

    /**
     * A Display's appearance, with the desktop's already being read on a thread of its own in desktop
     * mode. The read runs a system command, so a Display starts it first and {@link #get} only waits
     * for what is left.
     */
    public static SystemAppearance readAhead() {
        SystemAppearance appearance = new SystemAppearance();
        if (ConfigFlags.isDesktopMode())
            appearance.startDesktopRead();
        return appearance;
    }

    private synchronized void startDesktopRead() {
        desktop = CompletableFuture.supplyAsync(() -> osAppearance.get(), task -> {
            Thread thread = new Thread(task, READ_THREAD);
            thread.setDaemon(true);
            thread.start();
        });
    }

    /** The one read of the desktop's appearance: the one started ahead, else one made here. */
    private synchronized CompletableFuture<String> desktopRead() {
        if (desktop == null) desktop = CompletableFuture.completedFuture(osAppearance.get());
        return desktop;
    }
}

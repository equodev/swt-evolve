package dev.equo.swt;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

    /** How long after a web Display starts {@link #awaitClient} waits for its client's first report. */
    static final long CLIENT_WAIT_MS = 5_000;

    private volatile String reported;
    private CompletableFuture<String> desktop;
    private final CountDownLatch clientReported = new CountDownLatch(1);
    private final long clientDeadline;

    public SystemAppearance() {
        this(CLIENT_WAIT_MS);
    }

    private SystemAppearance(long clientWaitMs) {
        clientDeadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(clientWaitMs);
    }

    /** Records what the client reported; a value that names neither variant is ignored. */
    public void report(String appearance) {
        String variant = Config.themeVariant(appearance);
        if (variant != null) reported = variant;
        clientReported.countDown();
    }

    /**
     * Off the desktop, waits for the client's first report until {@link #CLIENT_WAIT_MS} after this
     * Display started: the page loads in parallel with the application, which picks its theme from
     * the first answer it gets. Returns at once once the client reported or that time is up.
     */
    public void awaitClient() {
        if (reported != null || ConfigFlags.isDesktopMode()) return;
        long left = clientDeadline - System.nanoTime();
        if (left <= 0) return;
        try (BootTrace.Span span = BootTrace.span("clientAppearance.wait")) {
            clientReported.await(left, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
        return readAhead(CLIENT_WAIT_MS);
    }

    static SystemAppearance readAhead(long clientWaitMs) {
        SystemAppearance appearance = new SystemAppearance(clientWaitMs);
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

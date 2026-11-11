package dev.equo.swt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Stands in for the system command that reads the desktop's appearance, and records the thread of
 * every read. Restores the real command when closed.
 */
public final class DesktopAppearanceStub implements AutoCloseable {
    private final Supplier<String> saved = EclipseWorkspaceTheme.osAppearance;
    public final List<Thread> readOn = new CopyOnWriteArrayList<>();

    public DesktopAppearanceStub(String appearance) {
        this(() -> appearance);
    }

    public DesktopAppearanceStub(Supplier<String> appearance) {
        EclipseWorkspaceTheme.osAppearance = () -> {
            readOn.add(Thread.currentThread());
            return appearance.get();
        };
    }

    /** Waits for every desktop read started on a thread of its own, so the count is final. */
    public void awaitReadThreads() throws InterruptedException {
        for (Thread thread : Thread.getAllStackTraces().keySet())
            if (SystemAppearance.READ_THREAD.equals(thread.getName())) thread.join(10_000);
    }

    @Override
    public void close() {
        EclipseWorkspaceTheme.osAppearance = saved;
    }
}

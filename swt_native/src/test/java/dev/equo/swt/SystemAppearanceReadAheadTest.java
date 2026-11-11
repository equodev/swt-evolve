package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A desktop Display reads the desktop's appearance with a system command. It starts that read on a
 * thread of its own as it begins, so the Display thread only waits for what is left of it when the
 * window needs its background.
 */
class SystemAppearanceReadAheadTest {

    private String savedMode;

    @BeforeEach
    void desktop() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        System.setProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP);
    }

    @AfterEach
    void restore() {
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
    }

    private static String after(CountDownLatch answer, String appearance) {
        try {
            answer.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return appearance;
    }

    @Test
    void theReadAheadLeavesTheCallerFreeWhileTheCommandRuns() {
        CountDownLatch answer = new CountDownLatch(1);
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub(() -> after(answer, "dark"))) {
            AtomicReference<Thread> caller = new AtomicReference<>();
            CompletableFuture<SystemAppearance> ahead = CompletableFuture.supplyAsync(() -> {
                caller.set(Thread.currentThread());
                return SystemAppearance.readAhead();
            });

            assertThat(ahead).as("the Display thread does not wait for the command it started")
                    .succeedsWithin(Duration.ofSeconds(5));
            answer.countDown();
            assertThat(ahead.join().get()).isEqualTo("dark");
            assertThat(desktop.readOn).hasSize(1).doesNotContain(caller.get());
        }
    }

    @Test
    void theDesktopIsReadOnceWhetherOrNotTheReadWasStartedAhead() {
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub("light")) {
            SystemAppearance ahead = SystemAppearance.readAhead();

            assertThat(ahead.get()).isEqualTo("light");
            assertThat(ahead.get()).isEqualTo("light");
            assertThat(desktop.readOn).hasSize(1);

            SystemAppearance inline = new SystemAppearance();
            assertThat(inline.get()).isEqualTo("light");
            assertThat(desktop.readOn).hasSize(2).last().isSameAs(Thread.currentThread());
        }
    }

    @Test
    void whatTheClientReportedWinsOverTheDesktopReadAhead() {
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub("light")) {
            SystemAppearance appearance = SystemAppearance.readAhead();

            appearance.report("dark");

            assertThat(appearance.get()).isEqualTo("dark");
        }
    }

    @Test
    void outsideTheDesktopNothingIsReadAhead() throws InterruptedException {
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        CountDownLatch started = new CountDownLatch(1);
        try (DesktopAppearanceStub desktop = new DesktopAppearanceStub(() -> {
            started.countDown();
            return "dark";
        })) {
            SystemAppearance.readAhead();

            assertThat(started.await(500, TimeUnit.MILLISECONDS)).isFalse();
        }
    }
}

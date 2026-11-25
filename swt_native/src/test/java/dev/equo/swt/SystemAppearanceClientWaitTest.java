package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A web Display's client reports its appearance once its page has loaded, which can be after the
 * application asked for the system theme. The application picks its theme from that first answer,
 * so the Display waits for the client's report a bounded time after it started, then falls back to
 * light as native SWT does.
 */
class SystemAppearanceClientWaitTest {

    private String savedMode;
    private ConfigFlags savedFlags;

    @BeforeEach
    void web() {
        savedMode = System.getProperty(ConfigFlags.MODE_PROPERTY);
        System.clearProperty(ConfigFlags.MODE_PROPERTY);
        savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(new ConfigFlags());
    }

    @AfterEach
    void restore() {
        if (savedMode == null) System.clearProperty(ConfigFlags.MODE_PROPERTY);
        else System.setProperty(ConfigFlags.MODE_PROPERTY, savedMode);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void anApplicationAskingBeforeTheClientReportedGetsTheClientsAppearance() {
        SystemAppearance appearance = SystemAppearance.readAhead(10_000);
        CompletableFuture.runAsync(() -> appearance.report("dark"),
                CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS));

        long start = System.nanoTime();
        appearance.awaitClient();

        assertThat(Config.darkTheme(appearance)).isTrue();
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(5_000);
    }

    @Test
    void withNoClientTheWaitEndsAtItsBoundAndIsNotRepeated() {
        SystemAppearance appearance = SystemAppearance.readAhead(300);

        long start = System.nanoTime();
        appearance.awaitClient();
        long waited = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(waited).isBetween(250L, 3_000L);
        assertThat(Config.darkTheme(appearance)).isFalse();

        start = System.nanoTime();
        appearance.awaitClient();
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(100);
    }

    @Test
    void aReportNamingNoVariantEndsTheWait() {
        SystemAppearance appearance = SystemAppearance.readAhead(10_000);
        appearance.report("");

        long start = System.nanoTime();
        appearance.awaitClient();

        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(1_000);
        assertThat(appearance.reported()).isNull();
        assertThat(Config.darkTheme(appearance)).isFalse();
    }

    @Test
    void aDesktopDisplayDoesNotWaitForAClient() {
        System.setProperty(ConfigFlags.MODE_PROPERTY, ConfigFlags.MODE_DESKTOP);
        SystemAppearance appearance = SystemAppearance.readAhead(10_000);

        long start = System.nanoTime();
        appearance.awaitClient();

        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(1_000);
    }

    @Test
    void theSystemThemeWaitsForTheClientWhenTheApplicationHasNoThemeOfItsOwn() {
        SystemAppearance appearance = SystemAppearance.readAhead(10_000);
        CompletableFuture.runAsync(() -> appearance.report("dark"),
                CompletableFuture.delayedExecutor(200, TimeUnit.MILLISECONDS));

        assertThat(Config.darkTheme(SystemAppearance.awaitingClient(appearance))).isTrue();
    }

    @Test
    void noWaitWhenTheApplicationSetsItsOwnTheme() {
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = "light";
        Config.setConfigFlags(flags);
        SystemAppearance appearance = SystemAppearance.readAhead(10_000);

        long start = System.nanoTime();
        SystemAppearance answered = SystemAppearance.awaitingClient(appearance);

        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(1_000);
        assertThat(answered).isSameAs(appearance);
        assertThat(Config.darkTheme(answered)).isFalse();
    }
}

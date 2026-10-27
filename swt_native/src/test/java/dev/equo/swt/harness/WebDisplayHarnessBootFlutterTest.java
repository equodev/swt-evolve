package dev.equo.swt.harness;

import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A browser that never loads the app must not fail the boot while a fresh one would. */
@Tag("flutter-it")
class WebDisplayHarnessBootFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void a_browser_that_never_connects_is_replaced() {
        flutter.injectBootFailures(1);

        Display display = flutter.boot();

        assertThat(display.isDisposed()).isFalse();
        flutter.flush();
    }
}

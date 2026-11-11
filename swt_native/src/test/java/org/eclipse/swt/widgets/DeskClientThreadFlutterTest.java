package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WidgetFlutterHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The desktop engine's UI isolate answers Java while the Java thread is out of the OS event loop: on
 * that loop's thread every task it is posted runs a millisecond late or more, and only while Java pumps.
 * Runs with {@code -Dharness.client=native} on Linux.
 */
@Tag("flutter-it")
class DeskClientThreadFlutterTest {

    private WidgetFlutterHarness flutter;
    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    void theClientAnswersWhileJavaDoesNotPumpTheEventLoop() throws Exception {
        assumeTrue("native".equals(System.getProperty("harness.client")), "needs the desktop engine");
        assumeTrue(SWT.getPlatform().equals("gtk"), "the GTK engine's thread policy");
        flutter = new WidgetFlutterHarness();
        flutter.init();
        display = new Display();
        Shell shell = new Shell(display);
        shell.setSize(300, 200);
        flutter.show(shell);

        CompletableFuture<Map<String, Object>> answer = flutter.requestState(shell);
        try {
            // Blocking here keeps this thread, the one that pumps the event loop, out of it.
            Map<String, Object> state = answer.get(2, TimeUnit.SECONDS);
            assertThat(state.get("found")).as("the root the client was booted for, %s", state).isEqualTo(true);
        } catch (TimeoutException e) {
            fail("the client did not answer while Java was not pumping the event loop");
        }
    }
}

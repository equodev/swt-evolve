package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Navigation is delivered to Flutter as a one-shot {@code navigate} op, and ops only route once the
 * Dart State exists. A Browser whose Flutter widget is built <em>after</em> {@code setUrl} already
 * fired — an Eclipse part that is created, disposed and recreated does exactly this — therefore
 * never learns where to go. The serialized state cannot stand in for the op either: for a
 * {@code file:} URL the only loadable form is the {@code /local-file/<token>/…} path, which travels
 * in that op alone. The Browser then stays permanently blank (its iframe falls back to the app
 * origin) while every other operation on it still reports success.
 *
 * <p>The fix is a replay: such a State asks for its navigation with a {@code navigateRequest}, and
 * the Browser re-sends the last one with a fresh {@code seq} (the Dart side drops any seq it has
 * already seen). These tests drive that request directly through the recording comm.
 *
 * <p>Lives in {@code dev.equo.swt} for {@link Config#resetForceEclipse()}: that flag short-circuits
 * every impl decision, is package-private to reset, and is left set by anything exercising
 * {@code CrashReporter} — which, given the test-class order varies, otherwise makes this class
 * observe a non-Dart Browser at random.
 */
@ExtendWith(Mocks.class)
class BrowserNavigateReplayTest {

    private RecordingBridge bridge;
    private Shell shell;

    // Config is global mutable state for the whole JVM, so this is done per test rather than per
    // class. Teardown restores Impl.equo — the default Config itself starts from; restoring
    // "eclipse" instead would silently change what classes running afterwards resolve to.
    @BeforeEach
    void setUp() {
        Config.resetForceEclipse();
        Config.forceEquo();
        Config.useEquo(Browser.class);
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        shell = Mocks.swtShell();
    }

    // Dispose what was built under force_equo before restoring the default, so nothing leaks out.
    @AfterEach
    void tearDown() {
        if (shell != null && !shell.isDisposed()) shell.dispose();
        FlutterBridge.set(null);
        Config.clear(Browser.class);
        Config.defaultToEquo();
    }

    /** The {@code navigate} frames recorded so far, oldest first. */
    private List<RecordingComm.Frame> navigateFrames() {
        return bridge.comm.sent.stream().filter(f -> f.event.endsWith("/navigate")).toList();
    }

    @Test
    @DisplayName("a file: navigation is replayed, keeping the /local-file token, on request")
    void replaysLocalFileNavigation() throws Exception {
        Path page = Files.createTempDirectory("equo-viewer").resolve("viewer.html");
        Files.writeString(page, "<!doctype html><title>Viewer</title>");
        page.toFile().deleteOnExit();

        Browser browser = new Browser(shell, SWT.NONE);
        // The single-slash form Eclipse's FileLocator.toFileURL() produces.
        browser.setUrl("file:/" + page.toString().replace('\\', '/'));

        List<RecordingComm.Frame> initial = navigateFrames();
        assertThat(initial).as("setUrl sends exactly one navigate op").hasSize(1);
        assertThat(initial.get(0).json)
                .as("the op carries the same-origin /local-file token, which the state does not")
                .contains("localFilePath")
                .contains("viewer.html");

        // A Flutter Browser built after the op above would have missed it; this is what it sends.
        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> after = navigateFrames();
        assertThat(after).as("the navigation is replayed").hasSize(2);
        assertThat(after.get(1).json)
                .as("the replay carries the same target, token included")
                .contains("localFilePath")
                .contains("viewer.html");
        assertThat(seq(after.get(1).json))
                .as("the replay uses a fresh seq, else the Dart side drops it as stale")
                .isGreaterThan(seq(after.get(0).json));
    }

    @Test
    @DisplayName("a setText navigation is replayed too")
    void replaysSetText() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setText("<!doctype html><title>Inline</title>");

        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> frames = navigateFrames();
        assertThat(frames).hasSize(2);
        assertThat(frames.get(1).json).contains("Inline");
        assertThat(seq(frames.get(1).json)).isGreaterThan(seq(frames.get(0).json));
    }

    @Test
    @DisplayName("a request before any navigation is ignored rather than sending a bogus op")
    void requestBeforeAnyNavigationIsIgnored() {
        new Browser(shell, SWT.NONE);

        bridge.comm.fireContaining("/navigateRequest", null);

        assertThat(navigateFrames()).as("nothing to replay yet").isEmpty();
    }

    /** Reads the {@code "seq":<n>} value out of a recorded navigate payload. */
    private static int seq(String json) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"seq\":(\\d+)").matcher(json);
        assertThat(m.find()).as("navigate payload carries a seq: " + json).isTrue();
        return Integer.parseInt(m.group(1));
    }
}

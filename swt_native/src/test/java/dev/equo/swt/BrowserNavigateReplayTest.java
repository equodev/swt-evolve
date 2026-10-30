package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireValues;
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
 * the Browser re-sends the last one under its <em>original</em> {@code seq}. A State that never saw
 * the op has seen no seq at all and so accepts it, while one that did drops it as stale — which is
 * what keeps the replay from reloading a page that is already there, throwing away everything it did
 * after {@code completed}. These tests drive that request directly through the recording comm.
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
        shell = Mocks.shell();
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
        assertThat(initial.get(0).state())
                .as("the op carries the same-origin /local-file token, which the state does not")
                .containsKey("localFilePath");
        assertThat(WireValues.containsText(initial.get(0).value, "viewer.html")).as("the op names the file").isTrue();

        // A Flutter Browser built after the op above would have missed it; this is what it sends.
        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> after = navigateFrames();
        assertThat(after).as("the navigation is replayed").hasSize(2);
        assertThat(after.get(1).state())
                .as("the replay carries the same target, token included")
                .containsKey("localFilePath");
        assertThat(WireValues.containsText(after.get(1).value, "viewer.html")).as("the replay names the file").isTrue();
        assertThat(seq(after.get(1)))
                .as("the replay keeps the original seq, so a State that already navigated drops it")
                .isEqualTo(seq(after.get(0)));
    }

    @Test
    @DisplayName("a setText navigation is replayed too")
    void replaysSetText() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setText("<!doctype html><title>Inline</title>");

        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> frames = navigateFrames();
        assertThat(frames).hasSize(2);
        assertThat(WireValues.containsText(frames.get(1).value, "Inline")).isTrue();
        assertThat(seq(frames.get(1))).isEqualTo(seq(frames.get(0)));
    }

    @Test
    @DisplayName("repeated requests all replay the same seq, so only a State that missed it navigates")
    void repeatedRequestsReplayTheSameSeq() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setUrl("https://example.invalid/first");

        // A widget rebuild puts the new State in the tree before the old one is disposed, so both
        // ask; the second navigation must not become a reload of the first.
        bridge.comm.fireContaining("/navigateRequest", null);
        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> frames = navigateFrames();
        assertThat(frames).hasSize(3);
        assertThat(seq(frames.get(1))).isEqualTo(seq(frames.get(0)));
        assertThat(seq(frames.get(2))).isEqualTo(seq(frames.get(0)));
    }

    @Test
    @DisplayName("a later setUrl replays under its own seq, not the earlier navigation's")
    void replayFollowsTheLatestNavigation() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setUrl("https://example.invalid/first");
        browser.setUrl("https://example.invalid/second");

        bridge.comm.fireContaining("/navigateRequest", null);

        List<RecordingComm.Frame> frames = navigateFrames();
        assertThat(frames).hasSize(3);
        assertThat(WireValues.containsText(frames.get(2).value, "second")).isTrue();
        assertThat(seq(frames.get(2)))
                .as("a State that only saw the first navigation must still take the second")
                .isEqualTo(seq(frames.get(1)))
                .isGreaterThan(seq(frames.get(0)));
    }

    @Test
    @DisplayName("a request before any navigation is ignored rather than sending a bogus op")
    void requestBeforeAnyNavigationIsIgnored() {
        new Browser(shell, SWT.NONE);

        bridge.comm.fireContaining("/navigateRequest", null);

        assertThat(navigateFrames()).as("nothing to replay yet").isEmpty();
    }

    /** The {@code seq} a recorded navigate payload carries. */
    private static int seq(RecordingComm.Frame frame) {
        Object seq = frame.state().get("seq");
        assertThat(seq).as("navigate payload carries a seq: " + frame.value).isInstanceOf(Number.class);
        return ((Number) seq).intValue();
    }
}

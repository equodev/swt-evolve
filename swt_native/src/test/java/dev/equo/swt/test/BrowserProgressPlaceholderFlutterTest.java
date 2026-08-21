package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import dev.equo.swt.harness.BrowserFlutterHarness;
import dev.equo.swt.harness.BrowserKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Pins {@code ProgressListener.completed} to documents the Browser was actually asked to display.
 *
 * <p>On the web target the Browser is an {@code <iframe>} created with a placeholder
 * {@code about:blank} source (an empty {@code src} would resolve to the app origin and self-embed
 * the app). That placeholder is a real document load, so reporting it makes consumers — which SWT's
 * javadoc tells to do their page setup in {@code completed} — run that setup against an empty
 * document.
 *
 * <p>Needs a Browser that has <em>never</em> navigated, so it boots its own fixture per test:
 * {@link BrowserFlutterTest} shares one Browser, and a Browser created after {@code show()} is not
 * picked up by the live client.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests "*BrowserProgressPlaceholderFlutterTest*"</pre>
 */
@Tag("flutter-it")
class BrowserProgressPlaceholderFlutterTest {

    /** Ceiling for waiting on something that should happen; only a stuck action pays it in full. */
    private static final long ACTION_TIMEOUT = 15_000;
    /** Paid in full every time — used to confirm something did NOT happen. */
    private static final long NEGATIVE_WAIT = 2_000;

    Display display;
    Shell shell;
    BrowserFlutterHarness flutter;
    BrowserKit.Handle browser;
    /** Counts {@code ProgressListener.completed} from before the client even booted. */
    private final AtomicInteger completed = new AtomicInteger();
    private File fixtureFile;

    @BeforeEach
    void boot() throws Exception {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        fixtureFile = writeFixture();

        flutter = new BrowserFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser progress placeholder test");
        shell.setLayout(new FillLayout());
        shell.setSize(800, 600);
        browser = BrowserKit.swt().newBrowser(shell, SWT.NONE);
        // Registered before the Flutter client exists, so the placeholder load — which happens as
        // soon as the client renders the iframe — is counted.
        browser.addProgressListener(ProgressListener.completedAdapter(e -> completed.incrementAndGet()));
        shell.open();
        shell.layout(true, true);
        flutter.show(shell);
    }

    @AfterEach
    void teardown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
        if (fixtureFile != null) fixtureFile.delete();
    }

    @Test
    @DisplayName("completed does not fire for the placeholder document, then fires for the real one")
    void completed_reportsOnlyRequestedDocuments() {
        // Nothing has been asked for, so the only document the webview can have loaded is the
        // placeholder — which SWT must never see.
        assertThat(pumpUntil(() -> completed.get() > 0, NEGATIVE_WAIT))
                .as("completed must not fire before any setUrl/setText (fired %d times)", completed.get())
                .isFalse();

        // A real navigation still reports completed, and the page it reports for is really there.
        flutter.clearIframeLoads();
        browser.setUrl(fixtureFile.toURI().toString());
        flutter.flush();
        assertThat(pumpUntil(() -> completed.get() > 0, ACTION_TIMEOUT))
                .as("completed fires for the requested navigation").isTrue();
        assertThat(flutter.awaitIframeRendered("PlaceholderProgressPage", ACTION_TIMEOUT))
                .as("the document completed was reported for actually rendered").isTrue();
    }

    @Test
    @DisplayName("navigating to about:blank on purpose still reports completed")
    void completed_firesForAnExplicitAboutBlankNavigation() {
        // The placeholder is suppressed because it was never asked for, not because of its URL:
        // about:blank is a legitimate navigation target and must keep reporting its events.
        browser.setUrl("about:blank");
        flutter.flush();
        assertThat(pumpUntil(() -> completed.get() > 0, ACTION_TIMEOUT))
                .as("completed fires for an explicit setUrl(\"about:blank\")").isTrue();
    }

    @Test
    @DisplayName("setText content reports completed")
    void completed_firesForSetText() {
        // setText is loaded from a data: URL whose location the web backend cannot read back, so it
        // is reported under about:blank too — the placeholder check must not swallow it.
        browser.setText("<!doctype html><html><head><title>InlinePage</title></head>"
                + "<body><h1>inline</h1></body></html>");
        flutter.flush();
        assertThat(pumpUntil(() -> completed.get() > 0, ACTION_TIMEOUT))
                .as("completed fires for setText").isTrue();
    }

    /** Driving {@code readAndDispatch} is what delivers the browser's {@code asyncExec}'d listeners. */
    private boolean pumpUntil(BooleanSupplier cond, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (!cond.getAsBoolean() && System.currentTimeMillis() < end) {
            flutter.pumpClient();
            if (!display.readAndDispatch()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return cond.getAsBoolean();
    }

    private File writeFixture() throws Exception {
        // Posts the on-load ping the harness listens for, so the test can tell "the page rendered"
        // from "the URL merely reached the widget state".
        String html = "<!doctype html><html><head><title>PlaceholderProgressPage</title></head><body>"
                + "<h1 id='h'>hello</h1>"
                + "<script>try{parent.postMessage('equo-iframe-loaded:'+document.title,'*')}catch(e){}</script>"
                + "</body></html>";
        File f = File.createTempFile("equo-progress-placeholder", ".html");
        Files.writeString(f.toPath(), html, StandardCharsets.UTF_8);
        return f;
    }
}

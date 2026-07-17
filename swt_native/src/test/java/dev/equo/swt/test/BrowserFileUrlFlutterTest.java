package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import dev.equo.swt.harness.BrowserFlutterHarness;
import dev.equo.swt.harness.BrowserKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Reproduces the Katalon "Web Recorder" bug end to end: {@code WebRecorderNavigator.init()}
 * (com.katalon.recorder.web) does
 * <pre>
 *   URL webviewUrl = FileLocator.toFileURL(bundle.getResource("resources/webview/WebRecorder.html"));
 *   browser.setUrl(webviewUrl.toString());
 * </pre>
 * i.e. always navigates a {@code com.equo.chromium.swt.Browser} to a {@code file:} URL for a
 * bundled local HTML resource. On the web-target Browser (an {@code <iframe>}), a real capture
 * of exactly this flow (see the {@code logRECORDER.txt} repro) shows the SWT-side navigation
 * events (Progress/completed, Location/changed) firing normally, yet the shell renders blank —
 * browsers refuse to display {@code file://} content framed by a non-{@code file://} page.
 *
 * <p>Unlike {@link BrowserFlutterTest}'s HTTP-served fixtures, this drives a real filesystem
 * {@code file:} URL through the actual Java {@code EvolveBrowser.setUrl()} path, a real
 * {@code WebFlutterServer}, and a real headless Chrome — the only way to observe whether the
 * page actually rendered (the {@link BrowserFlutterHarness#awaitIframeRendered} on-load ping),
 * as opposed to merely reaching the Flutter widget state.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests "*BrowserFileUrlFlutterTest*"</pre>
 */
@Tag("flutter-it")
class BrowserFileUrlFlutterTest {

    private static final long IFRAME_LOAD_TIMEOUT = 5_000;

    /** Posted by the fixture page once it actually runs in the webview (see BrowserFlutterTest). */
    private static final String LOAD_PING =
            "<script>try{parent.postMessage('equo-iframe-loaded:'+document.title,'*')}catch(e){}</script>";

    Display display;
    Shell shell;
    BrowserFlutterHarness flutter;
    BrowserKit.Handle browser;
    private File fixtureFile;

    @BeforeEach
    void boot() throws Exception {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        fixtureFile = writeFixture();

        flutter = new BrowserFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser file:// url test");
        shell.setLayout(new FillLayout());
        shell.setSize(800, 600);
        // com.equo.chromium.swt.Browser: the exact package Katalon's WebRecorderNavigator uses.
        browser = BrowserKit.chromium().newBrowser(shell, SWT.NONE);
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
    @Tag("flutter-it")
    void setUrl_withFileScheme_actuallyRendersPage() {
        String title = "FileUrlReproPage";
        flutter.clearIframeLoads();
        // Mirrors Browser.setUrl(FileLocator.toFileURL(...).toString()) exactly: a plain file: URL,
        // no proxying/serving done by the caller -- that's what the fix under test must handle.
        browser.setUrl(fixtureFile.toURI().toString());
        flutter.flush();
        assertThat(flutter.awaitIframeRendered(title, IFRAME_LOAD_TIMEOUT))
                .as("file:// page actually rendered in the iframe (on-load ping received) -- "
                        + "if this is false, the Browser widget shows the same blank content as "
                        + "the reported Katalon \"Web Recorder\" bug")
                .isTrue();
    }

    private File writeFixture() throws Exception {
        String html = "<!doctype html><html><head><title>FileUrlReproPage</title></head><body>"
                + "<h1 id='h'>hello from disk</h1>" + LOAD_PING + "</body></html>";
        File f = File.createTempFile("equo-file-url-repro", ".html");
        Files.writeString(f.toPath(), html, StandardCharsets.UTF_8);
        return f;
    }
}

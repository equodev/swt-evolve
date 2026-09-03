package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import dev.equo.swt.harness.BrowserFlutterHarness;
import dev.equo.swt.harness.BrowserKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Reproduces a local-file navigation bug end to end: a navigator
 * does
 * <pre>
 *   URL webviewUrl = FileLocator.toFileURL(bundle.getResource("resources/webview/LocalPage.html"));
 *   browser.setUrl(webviewUrl.toString());
 * </pre>
 * i.e. always navigates a {@code com.equo.chromium.swt.Browser} to a {@code file:} URL for a
 * bundled local HTML resource. On the web-target Browser (an {@code <iframe>}), a real capture
 * of exactly this flow shows the SWT-side navigation
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

    /** 1x1 transparent PNG. */
    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk"
                    + "YPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    Display display;
    Shell shell;
    BrowserFlutterHarness flutter;
    BrowserKit.Handle browser;
    private File fixtureFile;
    private File imageFile;

    @BeforeEach
    void boot() throws Exception {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        imageFile = File.createTempFile("equo-file-url-resource", ".png");
        Files.write(imageFile.toPath(), PIXEL_PNG);
        fixtureFile = writeFixture();

        flutter = new BrowserFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser file:// url test");
        shell.setLayout(new FillLayout());
        shell.setSize(800, 600);
        // com.equo.chromium.swt.Browser: the exact package the navigator uses.
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
        if (imageFile != null) imageFile.delete();
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
                        + "the reported blank-render bug")
                .isTrue();
    }

    /**
     * The consumer shape that motivated this: navigate to a local template, then script it from
     * {@code ProgressListener.completed} — the page declares a top-level function and the listener
     * calls it. Scripting requires the document to be same-origin, which is exactly what serving the
     * {@code file:} URL through {@code /local-file/} is for.
     */
    @Test
    @Tag("flutter-it")
    void evaluate_fromCompletedListener_scriptsTheLocalPage() {
        AtomicReference<Object> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        browser.addProgressListener(ProgressListener.completedAdapter(e -> {
            try {
                result.set(browser.evaluate("return equoMarker();"));
            } catch (Throwable t) {
                error.set(t);
            } finally {
                done.countDown();
            }
        }));
        browser.setUrl(fixtureFile.toURI().toString());
        flutter.flush();

        long end = System.currentTimeMillis() + IFRAME_LOAD_TIMEOUT;
        while (done.getCount() > 0 && System.currentTimeMillis() < end) {
            flutter.pumpClient();
            if (!display.readAndDispatch()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        assertThat(done.getCount()).as("completed fired for the local page").isZero();
        assertThat(error.get()).as("evaluate() must not fail on the local page").isNull();
        assertThat(result.get()).isEqualTo("ready");
    }

    /**
     * The page a {@code setUrl("file:...")} names is served same-origin, but the absolute
     * {@code file:} URLs <em>inside</em> it are still requested as {@code file:} from an
     * {@code http:} document, which the browser refuses ("Not allowed to load local resource") —
     * so the page renders with blank icons. Eclipse-based applications write exactly this markup:
     * {@code FileLocator.toFileURL} yields an absolute {@code file:} URL per bundled resource.
     */
    @Test
    @Tag("flutter-it")
    void setUrl_withFileSchemeImageInPage_actuallyLoadsTheImage() throws Exception {
        File page = writeImageFixture(imageFile.toURI().toString());
        try {
            flutter.clearIframeLoads();
            browser.setUrl(page.toURI().toString());
            flutter.flush();
            assertThat(flutter.awaitIframeRendered("ImageLoaded", IFRAME_LOAD_TIMEOUT))
                    .as("the file: image referenced by the local page actually loaded -- if this "
                            + "is false the page renders with the blank icons reported in web mode")
                    .isTrue();
        } finally {
            page.delete();
        }
    }

    /** Reports which of load/error the image ended on, so a red run says why rather than timing out. */
    private File writeImageFixture(String imageSrc) throws Exception {
        String html = "<!doctype html><html><head><title>FileUrlImagePage</title>"
                + "<script>function ping(t){document.title=t;"
                + "try{parent.postMessage('equo-iframe-loaded:'+t,'*')}catch(e){}}</script>"
                + "</head><body><h1>hello from disk</h1>"
                + "<img src='" + imageSrc + "' onload=\"ping('ImageLoaded')\""
                + " onerror=\"ping('ImageFailed')\"></body></html>";
        File f = File.createTempFile("equo-file-url-image", ".html");
        Files.writeString(f.toPath(), html, StandardCharsets.UTF_8);
        return f;
    }

    private File writeFixture() throws Exception {
        String html = "<!doctype html><html><head><title>FileUrlReproPage</title></head><body>"
                + "<h1 id='h'>hello from disk</h1>"
                + "<script>function equoMarker(){return 'ready';}</script>" + LOAD_PING + "</body></html>";
        File f = File.createTempFile("equo-file-url-repro", ".html");
        Files.writeString(f.toPath(), html, StandardCharsets.UTF_8);
        return f;
    }
}

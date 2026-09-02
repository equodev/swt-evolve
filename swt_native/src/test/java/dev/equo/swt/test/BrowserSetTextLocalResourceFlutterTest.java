package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

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
 * Reproduces the Eclipse Welcome/Intro page's broken images: the intro builds its HTML in memory
 * and hands it over with {@code browser.setText(html)}, referencing every image, stylesheet and
 * script by an absolute {@code file:} URL. The web-target Browser renders {@code setText} content
 * from a {@code data:} URL, and a {@code data:} document may not load {@code file:} sub-resources
 * — so the markup renders while every resource is refused ("Not allowed to load local resource").
 *
 * <p>The sibling {@link BrowserFileUrlFlutterTest} covers the {@code setUrl("file:...")} half of
 * the same problem. Both need the real thing — a real {@code WebFlutterServer} and a real headless
 * Chrome — because whether a sub-resource actually loaded is only observable in the browser.
 *
 * <pre>./gradlew :swt-evolve:swt_native:nativeTest --tests "*BrowserSetTextLocalResourceFlutterTest*"</pre>
 */
@Tag("flutter-it")
class BrowserSetTextLocalResourceFlutterTest {

    private static final long IFRAME_LOAD_TIMEOUT = 5_000;

    /** 1x1 transparent PNG. */
    private static final byte[] PIXEL_PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk"
                    + "YPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    Display display;
    Shell shell;
    BrowserFlutterHarness flutter;
    BrowserKit.Handle browser;
    private File imageFile;

    @BeforeEach
    void boot() throws Exception {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        imageFile = File.createTempFile("equo-settext-resource", ".png");
        Files.write(imageFile.toPath(), PIXEL_PNG);

        flutter = new BrowserFlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser setText local resource test");
        shell.setLayout(new FillLayout());
        shell.setSize(800, 600);
        browser = BrowserKit.swt().newBrowser(shell, SWT.NONE);
        shell.open();
        shell.layout(true, true);
        flutter.show(shell);
    }

    @AfterEach
    void teardown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
        if (imageFile != null) imageFile.delete();
    }

    @Test
    @Tag("flutter-it")
    void setText_withFileSchemeImage_actuallyLoadsTheImage() {
        flutter.clearIframeLoads();
        browser.setText(fixtureHtml(imageFile.toURI().toString()));
        flutter.flush();
        assertThat(flutter.awaitIframeRendered("ImageLoaded", IFRAME_LOAD_TIMEOUT))
                .as("the file: image embedded in setText content actually loaded -- if this is "
                        + "false the page shows the broken-image placeholders reported on the "
                        + "Eclipse Welcome page")
                .isTrue();
    }

    /**
     * The page's own {@code <base href="file:...">} — which the intro emits — must survive the
     * rewrite as a servable directory, so a resource named relative to it still resolves.
     */
    @Test
    @Tag("flutter-it")
    void setText_withFileSchemeBaseHref_resolvesRelativeResources() {
        flutter.clearIframeLoads();
        String base = imageFile.getParentFile().toURI().toString();
        String html = fixtureHtml(imageFile.getName())
                .replace("<head>", "<head><base href=\"" + base + "\">");
        browser.setText(html);
        flutter.flush();
        assertThat(flutter.awaitIframeRendered("ImageLoaded", IFRAME_LOAD_TIMEOUT))
                .as("an image named relative to the page's own file: <base href> loaded")
                .isTrue();
    }

    /** Reports which of load/error the image ended on, so a red run says why rather than timing out. */
    private static String fixtureHtml(String imageSrc) {
        return "<!doctype html><html><head><title>SetTextLocalResource</title>"
                + "<script>function ping(t){document.title=t;"
                + "try{parent.postMessage('equo-iframe-loaded:'+t,'*')}catch(e){}}</script>"
                + "</head><body><h1>welcome</h1>"
                + "<img src='" + imageSrc + "' onload=\"ping('ImageLoaded')\""
                + " onerror=\"ping('ImageFailed')\">"
                + "</body></html>";
    }
}

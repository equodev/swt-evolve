package org.eclipse.swt.graphics;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An Image drawn before the web client listens for GCs, read back from that client. Java holds what
 * it sends until the client connects and hands it all over at once, while the client is still
 * loading its fallback fonts: the GC's frames reach it before it listens for the drawer they belong
 * to. Held, they were replayed a channel at a time, the GC's state before any of its ops and its ops
 * a kind at a time, so a quarter fillArc on a white image came back all red.
 *
 * <p>The page this client is served holds those fonts back, so the frames arrive first on any
 * machine, not only where a slow browser boots (GCParityTest.draw_arcsFollowSwtAngles in the amd64
 * container).
 */
@Tag("flutter-it")
class GCImageDrawnBeforeClientListensFlutterTest {

    private static final RGB WHITE = new RGB(255, 255, 255);
    private static final RGB RED = new RGB(255, 0, 0);
    private static final int FONT_DELAY_MS = 2000;

    private final Map<String, String> overridden = new HashMap<>();
    private Path webDir;

    private void override(String key, String value) {
        overridden.putIfAbsent(key, System.getProperty(key));
        System.setProperty(key, value);
    }

    @AfterEach
    void restore() throws IOException {
        overridden.forEach((key, previous) -> {
            if (previous == null) System.clearProperty(key);
            else System.setProperty(key, previous);
        });
        if (webDir != null) {
            try (Stream<Path> paths = Files.walk(webDir)) {
                // Not followed: the entries are links into the real build.
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    /** The Flutter web build, with a page whose fetches of the fallback fonts answer late. */
    private static Path webBuildWithLateFonts() throws IOException {
        File web = new File("../flutter-lib/build/web").getAbsoluteFile();
        if (!web.isDirectory()) web = new File("flutter-lib/build/web").getAbsoluteFile();
        assertThat(web).as("the Flutter web build").isDirectory();
        Path dir = Files.createTempDirectory("evolve-web-late-fonts");
        for (File entry : web.listFiles()) {
            if (!entry.getName().equals("index.html"))
                Files.createSymbolicLink(dir.resolve(entry.getName()), entry.toPath());
        }
        String page = new String(Files.readAllBytes(new File(web, "index.html").toPath()), StandardCharsets.UTF_8);
        String lateFonts = "<script>(function () {"
                + " var fetch = window.fetch;"
                + " window.fetch = function (request) {"
                + "  var url = typeof request === 'string' ? request : (request && request.url) || '';"
                + "  var answer = fetch.apply(this, arguments);"
                + "  if (!/Roboto-(Regular|Bold)\\.ttf/.test(String(url))) return answer;"
                + "  return answer.then(function (r) { return new Promise(function (ok) { setTimeout(function () { ok(r); }, "
                + FONT_DELAY_MS + "); }); });"
                + " };"
                + "})();</script>";
        int head = page.indexOf("<head>");
        assertThat(head).as("the page has a head").isNotNegative();
        page = page.substring(0, head + 6) + lateFonts + page.substring(head + 6);
        Files.write(dir.resolve("index.html"), page.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private static boolean near(ImageData data, int x, int y, RGB expected) {
        RGB actual = data.palette.getRGB(data.getPixel(x, y));
        return Math.abs(actual.red - expected.red) <= 32 && Math.abs(actual.green - expected.green) <= 32
                && Math.abs(actual.blue - expected.blue) <= 32;
    }

    /** One character per 5 px: '#' red, '.' white, '?' anything else. */
    private static String picture(ImageData data) {
        StringBuilder out = new StringBuilder("\n");
        for (int y = 0; y < data.height; y += 5) {
            for (int x = 0; x < data.width; x += 5)
                out.append(near(data, x, y, RED) ? '#' : near(data, x, y, WHITE) ? '.' : '?');
            out.append('\n');
        }
        return out.toString();
    }

    @Test
    void aQuarterFillArcDrawnBeforeTheClientListensIsReadBackAsDrawn() throws IOException {
        webDir = webBuildWithLateFonts();
        override("dev.equo.swt.web.dir", webDir.toString());
        override("dev.equo.swt.web.headless", "true");
        override("swt.use_swt_colors", "true");
        Display display = new Display();
        try {
            Shell shell = new Shell(display);
            shell.setSize(300, 300);
            Image image = new Image(display, 100, 100);
            Color white = new Color(display, WHITE);
            Color red = new Color(display, RED);
            GC gc = new GC(image);
            gc.setBackground(white);
            gc.fillRectangle(0, 0, 100, 100);
            gc.setBackground(red);
            gc.fillArc(10, 10, 60, 60, 0, 90);
            gc.dispose();
            ImageData d = image.getImageData();
            String drawn = picture(d);
            assertThat(near(d, 55, 25, RED)).as("the first quadrant is up and to the right" + drawn).isTrue();
            assertThat(near(d, 25, 25, WHITE)).as("the second is not filled" + drawn).isTrue();
            assertThat(near(d, 25, 55, WHITE)).as("the third is not filled" + drawn).isTrue();
            assertThat(near(d, 55, 55, WHITE)).as("the fourth is not filled" + drawn).isTrue();
            assertThat(near(d, 95, 95, WHITE)).as("outside the oval the image stays white" + drawn).isTrue();
            image.dispose();
        } finally {
            display.dispose();
        }
    }
}

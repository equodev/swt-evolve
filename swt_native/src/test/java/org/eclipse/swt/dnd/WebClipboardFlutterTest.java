package org.eclipse.swt.dnd;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.equo.swt.harness.DevTools;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * On the web the clipboard is the browser's: a copy in the application has to land there, and a
 * paste has to bring in what the user copied anywhere else. {@code Clipboard} also keeps its own
 * copy, so each test crosses the real browser clipboard rather than reading back what it wrote.
 */
@Tag("flutter-it")
class WebClipboardFlutterTest {

    private static final long DEADLINE_MS = 5_000;

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private DevTools devTools;
    private Clipboard clipboard;

    @BeforeEach
    void boot() {
        Display display = flutter.boot();
        devTools = flutter.devTools();
        // A page reads the clipboard only when focused and allowed to; headless Chrome is neither.
        JsonObject focus = new JsonObject();
        focus.addProperty("enabled", true);
        devTools.send("Emulation.setFocusEmulationEnabled", focus);
        JsonObject grant = new JsonObject();
        JsonArray permissions = new JsonArray();
        permissions.add("clipboardReadWrite");
        permissions.add("clipboardSanitizedWrite");
        grant.add("permissions", permissions);
        grant.addProperty("origin", devTools.evaluate("location.origin").getAsString());
        devTools.send("Browser.grantPermissions", grant);
        clipboard = new Clipboard(display);
    }

    @AfterEach
    void shutdown() {
        if (clipboard != null) clipboard.dispose();
        flutter.teardown();
    }

    @Test
    void a_copy_lands_on_the_browser_clipboard() throws InterruptedException {
        clipboard.setContents(new Object[]{"copied in the application"}, new Transfer[]{TextTransfer.getInstance()});

        long deadline = System.currentTimeMillis() + DEADLINE_MS;
        String inBrowser = browserClipboard();
        while (!"copied in the application".equals(inBrowser) && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
            inBrowser = browserClipboard();
        }
        assertThat(inBrowser).isEqualTo("copied in the application");
    }

    @Test
    void a_paste_brings_in_what_the_browser_clipboard_holds() {
        devTools.evaluate("navigator.clipboard.writeText('copied in another tab')");

        assertThat(clipboard.getContents(TextTransfer.getInstance())).isEqualTo("copied in another tab");
    }

    private String browserClipboard() {
        JsonElement text = devTools.evaluate("navigator.clipboard.readText()");
        return text == null ? null : text.getAsString();
    }
}

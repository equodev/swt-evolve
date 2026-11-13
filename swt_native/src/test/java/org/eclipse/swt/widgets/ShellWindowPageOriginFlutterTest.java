package org.eclipse.swt.widgets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.equo.swt.WindowPolicy;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A dialog in a browser window of its own, with the Display's page loaded from an address other than
 * the server's own name for itself. The served pages isolate themselves from other origins, so a
 * window opened on a different origin than the page that opened it is one the page can neither see
 * nor close.
 */
@Tag("flutter-it")
class ShellWindowPageOriginFlutterTest {

    /** Longer than the client's check for windows the user closed, which runs every second. */
    private static final long CLOSE_CHECK_MS = 2_500;

    @Test
    void aDialogWindowLivesAndClosesWithItsShell() {
        WebDisplayHarness flutter = new WebDisplayHarness()
                .pageHost("127.0.0.1")
                .allowPopups()
                .property(WindowPolicy.MODE_PROPERTY, "all");
        Display display = flutter.boot();
        try {
            Shell parent = new Shell(display);
            parent.setBounds(0, 0, 800, 600);
            parent.open();
            flutter.settle();

            Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            dialog.setText("Preferences");
            dialog.setBounds(140, 140, 400, 300);
            dialog.open();
            flutter.settle();

            assertThat(flutter.pumpUntil(() -> !shellWindows(flutter).isEmpty(), 10_000))
                    .as("the dialog opened a window of its own").isTrue();
            flutter.pumpUntil(dialog::isDisposed, CLOSE_CHECK_MS);
            assertThat(dialog.isDisposed())
                    .as("nobody closed the dialog's window, so the dialog is still there").isFalse();
            assertThat(shellWindows(flutter))
                    .as("the dialog's window is on the page's origin")
                    .allSatisfy(url -> assertThat(URI.create(url).getHost()).isEqualTo("127.0.0.1"));

            dialog.close();
            flutter.settle();
            assertThat(flutter.pumpUntil(() -> shellWindows(flutter).isEmpty(), 10_000))
                    .as("closing the dialog closes its window").isTrue();
        } finally {
            flutter.teardown();
        }
    }

    /** The URLs of the browser's open shell windows. */
    private static List<String> shellWindows(WebDisplayHarness flutter) {
        List<String> urls = new ArrayList<>();
        JsonObject targets = flutter.devTools().send("Target.getTargets", new JsonObject());
        for (JsonElement e : targets.getAsJsonArray("targetInfos")) {
            JsonObject target = e.getAsJsonObject();
            String url = target.get("url").getAsString();
            if ("page".equals(target.get("type").getAsString()) && url.contains("widgetName=Shell")) urls.add(url);
        }
        return urls;
    }
}

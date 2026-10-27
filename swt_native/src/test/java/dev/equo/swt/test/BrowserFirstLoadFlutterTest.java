package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import dev.equo.swt.harness.BrowserFlutterHarness;
import dev.equo.swt.harness.BrowserKit;
import dev.equo.swt.harness.StallTolerantDeadline;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A Browser created on demand inside an already-rendered view, with its content set straight away:
 * the webview's blank placeholder document and the requested one are loading at the same time, and
 * only the requested one may reach {@code ProgressListener.completed}. Its own client, since the
 * placeholder is loaded once per Browser.
 */
@Tag("flutter-it")
class BrowserFirstLoadFlutterTest {

    private static final long TIMEOUT = 15_000;

    private BrowserFlutterHarness flutter;
    private Display display;

    @AfterEach
    void teardown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (flutter != null) flutter.teardown();
    }

    @Test
    void setText_completedFiresOnlyForTheRequestedDocument() {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        flutter = new BrowserFlutterHarness();
        flutter.init();
        display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(800, 600);
        Composite view = new Composite(shell, SWT.NONE);
        view.setLayout(new FillLayout());
        shell.open();
        flutter.show(shell);

        BrowserKit.Handle browser = BrowserKit.swt().newBrowser(view, SWT.NONE);
        List<Object> seen = new CopyOnWriteArrayList<>();
        browser.addProgressListener(ProgressListener.completedAdapter(e -> {
            try {
                seen.add(browser.evaluate(
                        "var m = document.getElementById('menu'); m.focus(); return m.id;"));
            } catch (RuntimeException ex) {
                seen.add(ex.getMessage());
            }
        }));
        browser.setText("<!doctype html><html><head><title>Menu</title></head><body>"
                + "<button id='menu'>menu</button></body></html>");
        view.layout(true, true);
        flutter.flush();

        pump(() -> seen.contains("menu"), TIMEOUT);
        pump(() -> false, 1_000);

        assertThat(seen).as("evaluate() from every completed").containsExactly("menu");
    }

    private void pump(BooleanSupplier until, long ms) {
        StallTolerantDeadline end = new StallTolerantDeadline(ms);
        while (!until.getAsBoolean() && end.hasTimeLeft()) {
            flutter.pumpClient();
            if (!display.readAndDispatch()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}

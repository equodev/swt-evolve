package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

import java.util.List;

import dev.equo.swt.harness.BatchEntries;
import dev.equo.swt.harness.GcFrames;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The url a Browser's value carries is where the webview reports it is, so every report that moves
 * it has to reach the client as well. A report the client sends for the page before a {@code setUrl}
 * can land after it, and the url sent then is that older page's; the client keeps it until the next
 * change is sent, which has to be the report of the page that did load.
 */
@ExtendWith(Mocks.class)
class BrowserLocationReachesClientTest {

    private static final String POSTED = "https://example.invalid/post";
    private static final String NEXT = "https://example.invalid/a";

    private RecordingBridge bridge;
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        Config.forceEquo();
        Config.useEquo(Browser.class);
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = Mocks.display();
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        shell = Mocks.shell();
    }

    @AfterEach
    void tearDown() {
        if (shell != null && !shell.isDisposed()) shell.dispose();
        doNothing().when(display).asyncExec(any(Runnable.class));
        FlutterBridge.set(null);
        Config.clear(Browser.class);
        Config.defaultToEquo();
    }

    @Test
    @DisplayName("after a late report of the page before a setUrl, the report of the page that loaded reaches the client")
    void theLoadedPagesLocationReachesTheClient() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setUrl(POSTED);
        FlutterBridge.update();

        browser.setUrl(NEXT);
        report(POSTED);
        FlutterBridge.update();
        bridge.comm.sent.clear();

        report(NEXT);
        FlutterBridge.update();

        assertThat(stateSent())
                .as("the client hears where the Browser now is")
                .anyMatch(value -> WireValues.containsText(value, NEXT));
    }

    @Test
    @DisplayName("a report of where the Browser already is sends nothing")
    void anUnchangedLocationSendsNothing() {
        Browser browser = new Browser(shell, SWT.NONE);
        browser.setUrl(NEXT);
        FlutterBridge.update();
        bridge.comm.sent.clear();

        report(NEXT);
        FlutterBridge.update();

        assertThat(stateSent()).as("nothing changed").isEmpty();
    }

    /** What the client sends once a page has loaded: its history and where it is. */
    private void report(String location) {
        Event state = new Event();
        state.text = location;
        bridge.comm.fireContaining("/navigationState", state);
    }

    /** The values sent since the last clear, ops left out. */
    private List<Object> stateSent() {
        return BatchEntries.of(bridge.comm.sent).stream()
                .filter(entry -> !entry.channel().endsWith("/navigate"))
                .map(GcFrames.Entry::value)
                .filter(value -> value != null)
                .toList();
    }
}

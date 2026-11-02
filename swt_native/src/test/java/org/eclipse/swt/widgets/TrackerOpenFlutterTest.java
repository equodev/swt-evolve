package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Tracker tells the client it has opened and closed, so the client routes the pointer to it for as
 * long as it tracks. Both go out as plain values: anything else has no form on the wire, and the send
 * throws before the Tracker loop starts.
 */
@Tag("flutter-it")
class TrackerOpenFlutterTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aTrackerTellsTheClientItOpensAndCloses() {
        RecordingWebBridge web = RecordingWebBridge.install();
        display = web.display;
        Shell shell = new Shell(display);
        shell.setSize(400, 300);
        shell.open();
        Tracker tracker = new Tracker(shell, SWT.NONE);
        tracker.setRectangles(new Rectangle[] { new Rectangle(10, 10, 50, 40) });
        web.comm.sent.clear();
        display.asyncExec(tracker::close);

        tracker.open();

        String host = "Shell/" + shell.hashCode();
        assertThat(web.comm.sent.stream().map(f -> f.event))
                .as("the client hears the Tracker open, then close")
                .containsSubsequence(host + "/Tracker/open", host + "/Tracker/close");
    }
}

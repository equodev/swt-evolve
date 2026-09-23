package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the client is told about the Display, and when.
 *
 * <p>The Display reaches Flutter by a path of its own: rather than being marked dirty and flushed
 * with everything else, a handful of call sites push the whole Display value the moment they change
 * something — a popup added, a tooltip shown, a shell closed, the active shell moving. Bringing that
 * onto the ordinary dirty path is the first slice of the incremental-delivery work, and these tests
 * are the net under that move: they describe the <em>observable</em> outcome (what the client
 * eventually knows) rather than the mechanism (which push produced it), so they should survive a
 * change of mechanism and fail only if the client would actually be told something different.
 *
 * <p>Each therefore asserts on the latest Display frame's content, never on how many frames arrived
 * — collapsing several pushes into one is precisely the change being made, and a test that counted
 * them would have to be rewritten to accept it, which is no test at all.
 */
@Tag("native-unit")
class DisplayDeliveryNativeTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an opened shell is in the Display state the client holds")
    void openedShellIsPublished() {
        TestWebBridge web = install();
        Shell shell = new Shell(display);
        shell.setVisible(true);

        assertThat(latestDisplayState(web))
                .as("a shell the client has not been told about cannot be rendered")
                .contains("\"id\":" + shell.hashCode());
    }

    @Test
    @DisplayName("a disposed shell is gone from the Display state the client holds")
    void disposedShellIsRetracted() {
        TestWebBridge web = install();
        Shell kept = new Shell(display);
        kept.setVisible(true);
        Shell closed = new Shell(display);
        closed.setVisible(true);

        closed.dispose();

        String state = latestDisplayState(web);
        assertThat(state)
                .as("a closed shell left in the list renders as a window that will not go away")
                .doesNotContain("\"id\":" + closed.hashCode());
        assertThat(state)
                .as("and the shells that remain must still be there")
                .contains("\"id\":" + kept.hashCode());
    }

    @Test
    @DisplayName("the active shell is named in the Display state")
    void activeShellIsPublished() {
        TestWebBridge web = install();
        Shell first = new Shell(display);
        first.setVisible(true);
        Shell second = new Shell(display);
        second.setVisible(true);
        second.setActive();

        assertThat(latestDisplayState(web))
                .as("keyboard focus follows the active shell, so the client has to know which it is")
                .contains("\"activeShellId\":" + second.hashCode());
    }

    @Test
    @DisplayName("an open popup menu is in the Display state, and gone once closed")
    void popupsAreTracked() {
        TestWebBridge web = install();
        Shell shell = new Shell(display);
        shell.setVisible(true);
        Menu popup = new Menu(shell, SWT.POP_UP);

        ((DartDisplay) display.getImpl()).addPopup(popup);
        assertThat(latestDisplayState(web))
                .as("a popup renders as an overlay owned by the Display, not by a shell")
                .contains("\"id\":" + popup.hashCode());

        ((DartDisplay) display.getImpl()).removePopup(popup);
        assertThat(latestDisplayState(web))
                .as("a popup left in the list stays on screen after it was dismissed")
                .doesNotContain("\"id\":" + popup.hashCode());
    }

    @Test
    @DisplayName("a shown tooltip is in the Display state, and gone once hidden")
    void tooltipsAreTracked() {
        TestWebBridge web = install();
        Shell shell = new Shell(display);
        shell.setVisible(true);
        ToolTip tip = new ToolTip(shell, SWT.BALLOON);

        ((DartDisplay) display.getImpl())._addActiveTooltip(tip);
        assertThat(latestDisplayState(web)).contains("\"id\":" + tip.hashCode());

        ((DartDisplay) display.getImpl())._removeActiveTooltip(tip);
        assertThat(latestDisplayState(web)).doesNotContain("\"id\":" + tip.hashCode());
    }

    @Test
    @DisplayName("the Display state is produced before any client has connected")
    void publishedBeforeClientReady() {
        TestWebBridge web = install();
        Shell shell = new Shell(display);
        shell.setVisible(true);

        // No ClientReady has been fired. The frame must exist anyway: the comm buffers what is sent
        // before the socket opens and flushes it on connect, and the alternative - waiting for the
        // client - deadlocks a workbench whose UI thread is parked in the native event pump.
        assertThat(displayFrames(web))
                .as("a Display update produced before connect still has to be handed to the comm")
                .isNotEmpty();
        assertThat(latestDisplayState(web)).contains("\"id\":" + shell.hashCode());
    }

    @Test
    @DisplayName("census: how many Display frames each action costs today")
    void displayFrameCensus() {
        TestWebBridge web = install();

        int atStart = displayFrames(web).size();
        Shell shell = new Shell(display);
        shell.setVisible(true);
        int afterOpen = displayFrames(web).size();

        Menu popup = new Menu(shell, SWT.POP_UP);
        ((DartDisplay) display.getImpl()).addPopup(popup);
        int afterPopup = displayFrames(web).size();
        ((DartDisplay) display.getImpl()).removePopup(popup);
        int afterPopupClose = displayFrames(web).size();

        Shell second = new Shell(display);
        second.setVisible(true);
        second.setActive();
        int afterSecond = displayFrames(web).size();

        int bytes = displayFrames(web).stream()
                .mapToInt(f -> f.json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).sum();

        System.out.printf("DISPLAY CENSUS  start=%d  open-shell=+%d  popup-open=+%d  popup-close=+%d  "
                        + "second-shell+activate=+%d  total-bytes=%d%n",
                atStart, afterOpen - atStart, afterPopup - afterOpen, afterPopupClose - afterPopup,
                afterSecond - afterPopupClose, bytes);
    }

    // ---- harness ----------------------------------------------------------------------------------
    // Same shape as DisplayConfigFlagsFlutterTest: a real Display whose bridge is a test bridge with
    // a RecordingComm standing in for the Flutter client.

    private String displayChannel() {
        return "Display/" + display.hashCode();
    }

    private List<RecordingComm.Frame> displayFrames(TestWebBridge web) {
        return web.comm.sent.stream().filter(f -> f.event.equals(displayChannel())).toList();
    }

    /** The most recent Display payload — what the client would hold after everything so far. */
    private String latestDisplayState(TestWebBridge web) {
        List<RecordingComm.Frame> frames = displayFrames(web);
        assertThat(frames).as("no Display frame was ever produced").isNotEmpty();
        return frames.get(frames.size() - 1).json;
    }

    private TestWebBridge install() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        return bridge;
    }

    private static final class TestWebBridge extends WebDisplayBridge {
        final RecordingComm comm = new RecordingComm();

        TestWebBridge(DartDisplay display) {
            super(display);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        protected void start(DartDisplay display) {
            registerDisplayClientReady(display);
        }
    }
}

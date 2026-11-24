package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A client keeps sending events for a widget until it hears the widget is gone, so events for a
 * widget Java has just disposed are expected and dropped without a word: "No handler for" is kept
 * for a channel nothing on the Java side takes, and for an id Java never registered.
 */
@ExtendWith(Mocks.class)
class ClientEventsAfterDisposeTest {

    private final Feeder comm = new Feeder();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private PrintStream realErr;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @AfterEach
    void tearDown() {
        if (realErr != null) System.setErr(realErr);
        FlutterBridge.set(null);
    }

    private void captureErr() {
        realErr = System.err;
        System.setErr(new PrintStream(err, true));
    }

    @Test
    void anEventForAWidgetJustDisposedIsDroppedSilently() {
        Shell shell = Mocks.shell();
        FlutterBridge.set(new RecordingBridge(comm));
        Button button = new Button(shell, SWT.PUSH);
        Label label = new Label(shell, SWT.NONE);
        long buttonId = button.hashCode();
        long labelId = label.hashCode();
        captureErr();
        comm.deliver("Button/" + buttonId + "/Paint/Paint");
        comm.deliver("Label/" + labelId + "/Paint/Paint");
        assertThat(err.toString()).as("events for live widgets").doesNotContain("No handler for");
        System.setErr(realErr);

        button.dispose();
        label.dispose();

        captureErr();
        comm.deliver("Button/" + buttonId + "/Paint/Paint");
        comm.deliver("Button/" + buttonId + "/MouseTrack/MouseEnter");
        comm.deliver("Label/" + labelId + "/Paint/Paint");

        assertThat(err.toString()).doesNotContain("No handler for");
    }

    @Test
    void aChannelNoTypeTakesIsStillReported() {
        FlutterBridge.set(new RecordingBridge(comm));

        captureErr();
        comm.deliver("Button/1/Paint/Paint");

        assertThat(err.toString()).contains("No handler for: Button/1/Paint/Paint");
    }

    @Test
    void anEventForAnIdNeverRegisteredIsStillReported() {
        Shell shell = Mocks.shell();
        FlutterBridge.set(new RecordingBridge(comm));
        Button button = new Button(shell, SWT.PUSH);
        long unknownId = button.hashCode() + 1;
        while (FlutterBridge.holdsId(unknownId)) unknownId++;

        captureErr();
        comm.deliver("Button/" + unknownId + "/Paint/Paint");

        assertThat(err.toString()).contains("No handler for: Button/" + unknownId + "/Paint/Paint");
    }

    @Test
    void aChannelALiveWidgetDoesNotTakeIsStillReported() {
        Shell shell = Mocks.shell();
        FlutterBridge.set(new RecordingBridge(comm));
        Label label = new Label(shell, SWT.NONE);
        long labelId = label.hashCode();

        captureErr();
        comm.deliver("Label/" + labelId + "/Selection/Selection");

        assertThat(err.toString()).contains("No handler for: Label/" + labelId + "/Selection/Selection");
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class Feeder extends AbstractBinaryCommService {
        void deliver(String channel) {
            byte[] header = dev.equo.swt.harness.Channels.header(channel);
            byte[] frame = Arrays.copyOf(header, header.length);
            onBinaryMessage(frame, 0, frame.length);
        }

        @Override
        protected void broadcast(byte[] frame, int offset, int length) {
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public void stop() {
        }
    }
}

package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Under {@code dev.equo.swt.debug} a frame of widget records is printed one record a line, its
 * properties by name: the binary form is not something to read.
 */
@ExtendWith(Mocks.class)
class DebugWidgetFrameTest {

    private RecordingBridge bridge;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        FlutterBridge.setSendObserver(null);
    }

    @Test
    void aWidgetFrameIsPrintedARecordALineWithItsPropertiesByName() {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        label.setText("Hello there");
        // A new widget goes out inside its parent's description; one the client has is sent by itself.
        FlutterBridge.update();
        label.setData("dev.equo.swt.new", false);
        List<byte[]> frames = new ArrayList<>();
        List<String> events = new ArrayList<>();
        FlutterBridge.setSendObserver((event, payload) -> {
            events.add(event);
            if (WidgetWire.CHANNEL.equals(event)) frames.add(payload);
        });
        bridge.dirty((DartWidget) label.getImpl());
        FlutterBridge.update().join();
        assertThat(frames).as("a frame of widget records was sent, among " + events).isNotEmpty();

        String printed = printedBy(() -> frames.forEach(DebugLog::logWidgetFrame));

        assertThat(printed).contains("send: Label/" + label.hashCode() + " (whole _s=")
                .contains("text").contains("Hello there");
    }

    private static String printedBy(Runnable run) {
        PrintStream out = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            run.run();
        } finally {
            System.setOut(out);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }
}

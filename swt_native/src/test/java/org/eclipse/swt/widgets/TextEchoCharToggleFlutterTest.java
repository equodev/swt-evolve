package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "Show Password" toggle: a plain single-line Text masked/unmasked at runtime via
 * setEchoChar, with the content typed while the field was unmasked. The re-mask must reach
 * Dart as a state push whose echoCharacter the Dart VText (an int field) can read.
 */
@Tag("flutter-it")
class TextEchoCharToggleFlutterTest {

    private RecordingBridge bridge;
    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
        }
    }

    private List<RecordingComm.Frame> framesFor(Text text) {
        String stateEvent = "Text/" + text.hashCode();
        return bridge.comm.sent.stream()
                .filter(f -> f.event.equals(stateEvent))
                .collect(Collectors.toList());
    }

    @Test
    void reMaskingAfterTypingPushesTheEchoCharAndKeepsTheRealText() {
        Shell shell = new Shell(display);
        Text password = new Text(shell, SWT.BORDER);
        password.setEchoChar((char) 8226);
        pump();

        // "Show Password" checked: reveal
        password.setEchoChar((char) 8226);
        password.setEchoChar((char) 0);
        pump();

        // the user types while the field is revealed (Dart forwards a Modify)
        Event modify = new Event();
        modify.text = "secret";
        modify.start = 6;
        bridge.comm.fireContaining("Modify/Modify", modify);
        pump();
        assertThat(password.getText()).isEqualTo("secret");

        // "Show Password" unchecked: re-mask
        bridge.comm.sent.clear();
        password.setEchoChar((char) 8226);
        pump();

        List<RecordingComm.Frame> frames = framesFor(password);
        assertThat(frames)
                .as("the re-mask must push a state update for the Text")
                .isNotEmpty();
        String payload = frames.get(frames.size() - 1).json;
        assertThat(payload)
                .as("the pushed state must carry the real text")
                .contains("\"text\":\"secret\"");
        assertThat(payload)
                .as("the pushed state must carry the echo char as a number the Dart "
                        + "VText.echoCharacter (int?) can read")
                .contains("\"echoCharacter\":8226");
        // The Dart VText types hiddenText as List<int>; a JSON string here makes the whole
        // payload fail to deserialize, so the re-mask silently never happens.
        assertThat(payload)
                .as("hiddenText must be pushed as an array of code units, not a JSON string")
                .doesNotContain("\"hiddenText\":\"secret\"")
                .contains("\"hiddenText\":[115,101,99,114,101,116]");
    }
}

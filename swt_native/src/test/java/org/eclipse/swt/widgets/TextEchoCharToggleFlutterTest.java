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
        bridge.comm.sent.clear();
        Event modify = new Event();
        modify.text = "secret";
        modify.start = 6;
        bridge.comm.fireContaining("Modify/Modify", modify);
        pump();
        assertThat(password.getText()).isEqualTo("secret");

        // The typing is when the text changed, so it is the update that has to carry it. A later
        // push does not repeat what the client already holds - that is the point of an update -
        // so the assertion is that the client was told, not that it is told every time.
        assertThat(framesFor(password))
                .as("the edit must carry the real text")
                .anyMatch(f -> f.json.contains("\"text\":\"secret\""));

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
                .as("the pushed state must carry the echo char as a number the Dart "
                        + "VText.echoCharacter (int?) can read")
                .contains("\"echoCharacter\":8226");
        // The masked form is derived where it is drawn, from the real text and the echo character
        // that both already travel, so pushing it as well would be sending the same thing twice.
        assertThat(payload)
                .as("the masked text is derived on the far side rather than pushed")
                .doesNotContain("hiddenText");
    }
}

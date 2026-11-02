package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class StyledTextHiddenScrollBarFlutterTest {

    private RecordingComm comm;
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
        comm = new RecordingComm();
        FlutterBridge.set(new RecordingBridge(comm));
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    private void pump() {
        for (int i = 0; i < 100 && display.readAndDispatch(); i++) {
        }
        FlutterBridge.update();
    }

    private Optional<Map<String, Object>> lastStateFor(long id) {
        Map<String, Object> found = null;
        for (RecordingComm.Frame frame : comm.sent) {
            if (frame.value == null) continue;
            Optional<Map<String, Object>> hit = WireValues.describing(frame.value, id).stream()
                    .filter(m -> !m.containsKey("_r"))
                    .findFirst();
            if (hit.isPresent()) found = hit.get();
        }
        return Optional.ofNullable(found);
    }

    @Test
    void aScrollBarIsVisibleUntilTheApplicationHidesIt() {
        Shell shell = new Shell(display);
        shell.setSize(600, 400);
        StyledText text = new StyledText(shell, SWT.V_SCROLL | SWT.READ_ONLY);
        text.setBounds(0, 0, 600, 400);
        ScrollBar bar = text.getVerticalBar();
        assertThat(bar).as("a V_SCROLL StyledText has a vertical bar").isNotNull();

        assertThat(bar.getVisible())
                .as("SWT's default: a scroll bar is visible until something hides it")
                .isTrue();
    }

    @Test
    void hidingAScrollBarReachesDart() {
        Shell shell = new Shell(display);
        shell.setSize(600, 400);
        StyledText text = new StyledText(shell, SWT.V_SCROLL | SWT.READ_ONLY);
        text.setBounds(0, 0, 600, 400);
        pump();

        text.getVerticalBar().setVisible(false);
        pump();

        Map<String, Object> state = lastStateFor(text.getVerticalBar().hashCode())
                .orElseThrow(() -> new AssertionError("the bar never reached Dart at all; frames: "
                        + comm.sent.stream().map(f -> f.event + "=" + f.value).toList()));
        assertThat(state.containsKey("visible"))
                .as("the hidden flag has to be on the wire; a primitive false is exactly what the "
                        + "minimal serializer drops")
                .isTrue();
        assertThat((Boolean) state.get("visible"))
                .as("the bar the application hid must reach Dart hidden")
                .isFalse();
    }
}

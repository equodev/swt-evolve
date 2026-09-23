package org.eclipse.swt.custom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
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

    private Optional<JsonObject> lastStateFor(long id) {
        JsonObject found = null;
        for (RecordingComm.Frame frame : comm.sent) {
            if (frame.json == null || frame.json.isEmpty()) continue;
            JsonObject hit = findById(JsonParser.parseString(frame.json), id);
            if (hit != null && !hit.has("_r")) found = hit;
        }
        return Optional.ofNullable(found);
    }

    private static JsonObject findById(JsonElement node, long id) {
        if (node.isJsonArray()) {
            for (JsonElement e : node.getAsJsonArray()) {
                JsonObject hit = findById(e, id);
                if (hit != null) return hit;
            }
            return null;
        }
        if (!node.isJsonObject()) return null;
        JsonObject obj = node.getAsJsonObject();
        if (obj.has("id") && obj.get("id").getAsLong() == id && !obj.has("_r")) return obj;
        for (String key : obj.keySet()) {
            JsonObject hit = findById(obj.get(key), id);
            if (hit != null) return hit;
        }
        return null;
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

        JsonObject state = lastStateFor(text.getVerticalBar().hashCode())
                .orElseThrow(() -> new AssertionError("the bar never reached Dart at all; frames: "
                        + comm.sent.stream().map(f -> f.event + "=" + f.json).toList()));
        assertThat(state.has("visible"))
                .as("the hidden flag has to be on the wire; a primitive false is exactly what the "
                        + "minimal serializer drops")
                .isTrue();
        assertThat(state.get("visible").getAsBoolean())
                .as("the bar the application hid must reach Dart hidden")
                .isFalse();
    }
}

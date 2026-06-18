package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

/**
 * Investigates how the radio-group deselection is delivered to Flutter, and in
 * particular the web-specific path where {@code selectRadio()} additionally
 * dirties the <em>parent</em> Composite. When the parent is dirty, the
 * dirty-ancestor filter in {@link FlutterBridge#update()} drops the deselected
 * sibling from individual sends and relies on the parent send to carry it.
 */
@ExtendWith(Mocks.class)
class RadioGroupDeliveryTest {

    static class Frame { final String event; final String json; Frame(String e, String j){event=e;json=j;} }

    static class RecordingComm implements CommService {
        final List<Frame> sent = new ArrayList<>();
        public void send(String eventName) { sent.add(new Frame(eventName, "")); }
        public void send(String eventName, byte[] payload) {
            sent.add(new Frame(eventName, new String(payload, StandardCharsets.UTF_8)));
        }
        public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) { }
        public void remove(String eventName) { }
        public int getPort() { return 0; }
        public void stop() { }
    }

    static class RecordingBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();
        RecordingBridge() { clientReady.complete(true); }
        @Override protected CommService comm() { return comm; }
        @Override public void initFlutterView(Composite parent, DartControl control) { }
        @Override public void destroy(DartWidget control) { }
    }

    private RecordingBridge bridge;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }
    @BeforeEach void setUp() { bridge = new RecordingBridge(); FlutterBridge.set(bridge); }
    @AfterEach void tearDown() { FlutterBridge.set(null); }

    private static void click(Button b) {
        // selectRadio() is the group logic a Selection event triggers on every
        // platform: select this radio, deselect the siblings. Driven directly.
        ((DartButton) b.getImpl()).selectRadio();
    }

    @Test
    @DisplayName("web path: deselected sibling must be communicated when parent is also dirty")
    void deselectionDeliveredOnWebPath() {
        Shell shell = Mocks.swtShell();
        Composite group = new Composite(shell, SWT.NONE);
        Button r1 = new Button(group, SWT.RADIO);
        r1.setText("Option 1");
        Button r2 = new Button(group, SWT.RADIO);
        r2.setText("Option 2");

        for (Widget w : new Widget[] { group, r1, r2 }) w.setData("dev.equo.swt.new", false);

        click(r1);
        FlutterBridge.update();

        bridge.comm.sent.clear();
        click(r2);
        // Mimic the WEB-only behaviour of selectRadio(): also dirty the parent Composite.
        bridge.dirty((DartWidget) group.getImpl());
        FlutterBridge.update();

        System.out.println("=== frames sent after selecting r2 (web path) ===");
        for (Frame f : bridge.comm.sent) System.out.println(f.event + " -> " + f.json);

        assertThat(r1.getSelection()).as("r1 deselected in Java").isFalse();

        String r1Id = String.valueOf(r1.hashCode());
        String r2Id = String.valueOf(r2.hashCode());

        // r1's deselection reaches Flutter iff either r1 is sent on its own channel,
        // or the parent payload carries r1 (and r1 is NOT marked selected there).
        boolean r1OwnChannel = bridge.comm.sent.stream().anyMatch(f -> f.event.contains("/" + r1Id));
        Frame parentFrame = bridge.comm.sent.stream()
                .filter(f -> f.event.startsWith("Composite/" + group.hashCode()))
                .reduce((a, b) -> b).orElse(null);

        boolean parentCarriesR1Deselected = false;
        if (parentFrame != null && parentFrame.json.contains("\"id\":" + r1Id)) {
            // The serializer omits selection when false, so r1's slice must not say selection:true.
            int r1Slice = parentFrame.json.indexOf("\"id\":" + r1Id);
            int r2Slice = parentFrame.json.indexOf("\"id\":" + r2Id);
            int end = r2Slice > r1Slice ? r2Slice : parentFrame.json.length();
            String r1Json = parentFrame.json.substring(r1Slice, end);
            parentCarriesR1Deselected = !r1Json.contains("\"selection\":true");
        }

        assertThat(r1OwnChannel || parentCarriesR1Deselected)
                .as("deselected radio r1 must be communicated to Flutter as not-selected "
                        + "(own channel, or carried deselected in the parent payload), otherwise "
                        + "Flutter keeps showing it selected and two radios appear checked "
                        + "(issue #597). Frames: " + bridge.comm.sent.size())
                .isTrue();
    }
}

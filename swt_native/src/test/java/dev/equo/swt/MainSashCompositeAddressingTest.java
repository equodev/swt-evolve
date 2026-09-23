package dev.equo.swt;

import dev.equo.swt.comm.AbstractBinaryCommService;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A plain Composite that holds a perspective's part-stacks under the e4 SashLayout is described
 * to the client as a {@code MainComposite} ({@link Config#isMainSashComposite}). The client
 * subscribes to a widget under the name its description carries, so everything addressed to that
 * composite afterwards has to use that name too, in both directions. When updates kept travelling
 * as {@code Composite/<id>}, the composite stayed at the unlaid-out size it was first described
 * with, and every part of the perspective inside it was clipped away.
 */
@ExtendWith(Mocks.class)
class MainSashCompositeAddressingTest {

    private static final String BATCH = "swt.evolve.batch";
    private static final Pattern CHANNEL = Pattern.compile("\"([A-Za-z]+/\\d+)\"");

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
    }

    @Test
    @DisplayName("an update to the main sash area travels under the name its description gave it")
    void updateIsAddressedByTheDescribedName() {
        Shell shell = Mocks.shell();
        Composite perspective = new Composite(shell, SWT.NONE);
        Composite sash = new Composite(perspective, SWT.NONE);
        sash.setLayout(new ConfigMainSashCompositeTest.FakeSashLayout());
        new CTabFolder(sash, SWT.NONE);
        settle(perspective);

        sash.setBounds(0, 0, 800, 600);
        FlutterBridge.update();

        long id = FlutterBridge.id(sash);
        assertThat(sentJson())
                .as("the composite is described as the main sash area")
                .contains("\"id\":" + id + ",\"swt\":\"MainComposite\"");
        assertThat(channels())
                .as("so its update is addressed where the client listens for it")
                .contains("MainComposite/" + id)
                .doesNotContain("Composite/" + id);
    }

    @Test
    @DisplayName("a message addressed under either name reaches the handler registered at construction")
    void inboundMessageReachesTheWidgetUnderEitherName() {
        FlutterBridge.widgetName(new Object()); // loading FlutterBridge is what installs the alias
        FrameFeeder comm = new FrameFeeder();
        List<String> handled = new ArrayList<>();
        comm.on("Composite/7/MouseTrack/MouseEnter", Object.class, v -> handled.add("enter"));
        comm.on("MainComposite/8/Paint/Paint", Object.class, v -> handled.add("paint"));

        comm.deliver("MainComposite/7/MouseTrack/MouseEnter");
        comm.deliver("Composite/8/Paint/Paint");

        assertThat(handled).containsExactly("enter", "paint");
    }

    // ---- harness ----

    /** Gets the tree onto the wire and clears the record, as FlushBatchTest does. */
    private void settle(Widget widget) {
        markKnown(widget);
        FlutterBridge.update();
        bridge.comm.sent.clear();
    }

    private void markKnown(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite)
            for (Widget child : ((Composite) widget).getChildren()) markKnown(child);
    }

    private String sentJson() {
        StringBuilder all = new StringBuilder();
        for (RecordingComm.Frame frame : bridge.comm.sent) all.append(frame.json);
        return all.toString();
    }

    /** Every channel a message was addressed to, including those fused into a batch. */
    private List<String> channels() {
        List<String> channels = new ArrayList<>();
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (!BATCH.equals(frame.event)) {
                channels.add(frame.event);
                continue;
            }
            Matcher m = CHANNEL.matcher(frame.json);
            while (m.find()) channels.add(m.group(1));
        }
        return channels;
    }

    /** The real dispatcher, fed frames directly instead of over a socket. */
    private static final class FrameFeeder extends AbstractBinaryCommService {
        void deliver(String eventName) {
            byte[] header = CommService.frameHeader(eventName);
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

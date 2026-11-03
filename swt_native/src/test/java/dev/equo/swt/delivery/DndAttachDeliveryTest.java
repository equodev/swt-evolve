package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A DragSource or DropTarget attached to, or detached from, a control the client already holds
 * reaches the client. JFace's addDragSupport attaches to a live viewer, and Flutter only makes a
 * control draggable, or a drop target, from what it was sent.
 *
 * <p>The client already holds the control from a frame sent before it was ready, as the Display's
 * frame is. The attach is flushed before the client is ready, with the control's parent dirty too,
 * so the parent's frame carries the control and is written only once the client is ready.
 */
@ExtendWith(Mocks.class)
class DndAttachDeliveryTest {

    private LateClient client;
    private Canvas canvas;
    private Mocks.Delivery delivery;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        client = new LateClient();
        FlutterBridge.set(client);
        // A DragSource or DropTarget detaches from its control on its own Dispose event.
        delivery = Mocks.deliverEvents(Mocks.display());
        Shell shell = Mocks.shell();
        Composite parent = new Composite(shell, SWT.NONE);
        canvas = new Canvas(parent, SWT.NONE);
        FlutterBridge.update();
        client.pushBeforeReady(parent);
    }

    @AfterEach
    void tearDown() {
        delivery.close();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a DragSource attached to a control the client holds is sent")
    void dragSourceAttachedLater() {
        flushedBeforeClientReady(() -> new DragSource(canvas, DND.DROP_COPY));

        assertThat(canvasAsSent().get("dragSource")).isEqualTo(true);
    }

    @Test
    @DisplayName("a DragSource disposed on a control the client holds is sent")
    void dragSourceDisposedLater() {
        DragSource source = new DragSource(canvas, DND.DROP_COPY);
        client.pushBeforeReady(canvas);

        flushedBeforeClientReady(() -> { source.dispose(); return null; });

        assertThat(canvasAsSent().get("dragSource")).isNotEqualTo(true);
    }

    @Test
    @DisplayName("a DropTarget attached to a control the client holds is sent")
    void dropTargetAttachedLater() {
        DropTarget target = flushedBeforeClientReady(() -> new DropTarget(canvas, DND.DROP_COPY));

        Object sent = canvasAsSent().get("dropTargetId");
        assertThat(sent).isInstanceOf(Number.class);
        assertThat(((Number) sent).longValue()).isEqualTo(FlutterBridge.id(target.getImpl()));
    }

    @Test
    @DisplayName("a DropTarget disposed on a control the client holds is sent")
    void dropTargetDisposedLater() {
        DropTarget target = new DropTarget(canvas, DND.DROP_COPY);
        client.pushBeforeReady(canvas);

        flushedBeforeClientReady(() -> { target.dispose(); return null; });

        Object id = canvasAsSent().get("dropTargetId");
        assertThat(id == null || ((Number) id).longValue() == 0).as("dropTargetId sent as %s", id).isTrue();
    }

    /**
     * Runs {@code change} with the canvas's parent dirty as well, flushes before the client is
     * ready, then lets the client connect: the frames that go out then are what the client learns.
     */
    private <T> T flushedBeforeClientReady(java.util.function.Supplier<T> change) {
        T result = change.get();
        client.dirty((DartWidget) canvas.getParent().getImpl());
        client.comm.sent.clear();
        FlutterBridge.update();
        client.connect();
        return result;
    }

    /** The canvas as the last record that describes it states it. */
    private Map<String, Object> canvasAsSent() {
        long id = FlutterBridge.id(canvas.getImpl());
        List<Map<String, Object>> found = new ArrayList<>();
        for (RecordingComm.Frame frame : client.comm.sent) {
            if (!(frame.value instanceof Map)) continue;
            Object own = frame.state().get("id");
            if (own instanceof Number && ((Number) own).longValue() == id) found.add(frame.state());
        }
        assertThat(found).as("records describing the canvas in %s", client.comm.sent).isNotEmpty();
        return found.get(found.size() - 1);
    }

    /** A client that becomes ready when the test says so. */
    private static final class LateClient extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        void connect() { clientReady.complete(true); }

        /**
         * Credits {@code control} as delivered whole to this client without waiting for it, as the
         * Display's frame is.
         */
        void pushBeforeReady(Control control) {
            DartWidget impl = (DartWidget) control.getImpl();
            Serializer.forgetDelivery(impl.getValue());
            WireFrames.record(impl, comm.connectionId());
        }

        @Override protected CommService comm() { return comm; }
        @Override public void initFlutterView(Composite parent, DartControl control) { }
        @Override public void destroy(DartWidget control) { }
    }
}

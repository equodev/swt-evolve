package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.GCImageDrawer;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.MessageBatch;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.EmbeddedBridge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/** What a GC's image drawer sends on the Display's shared comm when it is driven directly. */
@Tag("native-unit")
class GCImageDrawerNativeTest {

    private Display display;
    private final List<String> sent = new ArrayList<>();
    private final List<String> payloads = new ArrayList<>();
    private final List<Image> images = new ArrayList<>();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        FlutterBridge.setDisplayGcCommResolver(d -> new CommService() {
            @Override
            public void send(String eventName) {
                sent.add(eventName);
            }

            @Override
            public void send(String eventName, byte[] payload) {
                sent.add(eventName);
                payloads.add(new String(payload, java.nio.charset.StandardCharsets.UTF_8));
            }

            @Override
            public <T> void on(String eventName, Class<T> cls, Consumer<T> callback) {
            }

            @Override
            public void remove(String eventName) {
            }

            @Override
            public int getPort() {
                return 0;
            }

            @Override
            public void stop() {
            }
        });
    }

    @AfterEach
    void tearDown() {
        images.forEach(Image::dispose);
        FlutterBridge.setDisplayGcCommResolver(null);
        FlutterBridge.set(null);
    }

    private GCImageDrawer drawer(long gcId) {
        Image image = new Image(display, 4, 4);
        images.add(image);
        return EmbeddedBridge.of(gcId, image, bytes -> {
        });
    }

    @Test
    @DisplayName("a drawer started for a reply and then abandoned tears its Flutter side down")
    void abandoningAStartedDrawerSendsGcDispose() {
        GCImageDrawer drawer = drawer(71);
        drawer.startForPendingReply();
        sent.clear();

        drawer.abandon();

        assertThat(sent).containsExactly("GC/71/gcDispose");
    }

    /** The drawers holding queued ops, a static set other tests' unfinished GCs may still be in. */
    @SuppressWarnings("unchecked")
    private static Set<GCImageDrawer> holdingOps() throws ReflectiveOperationException {
        Field field = GCImageDrawer.class.getDeclaredField("holdingOps");
        field.setAccessible(true);
        return (Set<GCImageDrawer>) field.get(null);
    }

    @Test
    @DisplayName("what waits for the queued ops runs once every drawer holding some has sent them")
    void afterQueuedOpsWaitsForTheDrawersHoldingOps() throws ReflectiveOperationException {
        Set<GCImageDrawer> holding = holdingOps();
        Set<GCImageDrawer> others = new HashSet<>(holding);
        holding.removeAll(others);
        try {
            GCImageDrawer first = drawer(72);
            GCImageDrawer second = drawer(73);
            first.queueOp(() -> sent.add("first op"));
            second.queueOp(() -> sent.add("second op"));
            AtomicBoolean ran = new AtomicBoolean();

            GCImageDrawer.afterQueuedOps(() -> ran.set(true));
            first.startForPendingReply();
            assertThat(ran).as("the second drawer still holds its op").isFalse();
            second.startForPendingReply();

            assertThat(ran).isTrue();
            assertThat(sent).contains("first op", "second op");
        } finally {
            holding.addAll(others);
        }
    }

    @Test
    @DisplayName("an abandoned drawer's dispose rides with the ops its GC buffered since it started")
    void anAbandonedDrawerSendsItsDisposeWithTheBufferedOps() {
        Image image = new Image(display, 4, 4);
        images.add(image);
        GC gc = new GC(image);
        GCImageDrawer drawer = (GCImageDrawer) ((DartGC) gc.getImpl()).bridge;
        drawer.startForPendingReply();
        gc.drawLine(0, 0, 3, 3);
        sent.clear();
        payloads.clear();

        GCHelper.disposeImageGC(gc);

        assertThat(sent).containsExactly(MessageBatch.EVENT);
        assertThat(payloads.get(0)).contains("drawLine", "/gcDispose");
    }
}

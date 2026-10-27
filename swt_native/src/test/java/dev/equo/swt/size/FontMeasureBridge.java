package dev.equo.swt.size;

import dev.equo.swt.harness.FlutterHarness;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * JUnit extension used by {@link FontSizeTest} that boots a real Flutter renderer and speaks the
 * fontSizeRequest/fontSizeResponse protocol (see {@code flutter-lib/lib/fontSize.dart}), to
 * validate {@code GenFontMetrics.DATA} against live measurements. The boot itself (headless
 * Chrome + WebFlutterServer, or the native engine under {@code -Dharness.client=native}) is
 * entirely {@link FlutterHarness}'s own {@code startWebClient}/{@code initNativeRenderer}/
 * {@code awaitClientReadyResilient} — this class only adds the request/response plumbing on top,
 * rather than re-booting a renderer from scratch the way {@code WidgetFlutterHarness.show(Control)}
 * does for a real widget tree.
 */
class FontMeasureBridge extends FlutterHarness implements BeforeAllCallback, AfterAllCallback {

    /**
     * One entry per request still awaiting its {@code fontSizeResponse}, oldest first.
     *
     * <p>The protocol carries no request id, so a response is matched to its request by arrival
     * order — safe because the transport is ordered and the Dart side answers one request per
     * handler call. A queue rather than a single field is what keeps a slow response from being
     * mistaken for the next request's: previously the field was overwritten by the following
     * request, so a response that arrived after its own test had given up waiting would complete
     * the *next* test's future with the wrong measurement, failing a test whose request was fine.
     *
     * <p>Concurrent because the two ends run on different threads: requests are queued from the
     * test thread, while responses are delivered on the comm thread (for the web client
     * {@code pump()} only yields, it does not dispatch onto the caller).
     */
    private final Queue<CompletableFuture<double[]>> pendingResults = new ConcurrentLinkedQueue<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        registerBridge();
        initFlutterView();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        teardown();
    }

    private void initFlutterView() {
        onReady(this, Void.class);
        bootRenderer(id(this), widgetName(this));
        onPayload(this, "fontSizeResponse", p -> {
            try {
                double[] arr = serializer.from(double[].class, p);
                CompletableFuture<double[]> awaiting = pendingResults.poll();
                if (awaiting != null) awaiting.complete(arr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** [width, height] for one sample-text measurement. */
    CompletableFuture<double[]> measureLineMetrics(String font, boolean italic, int weight, int size, String text) {
        CompletableFuture<double[]> result = new CompletableFuture<>();
        pendingResults.add(result);
        Map<String, Object> request = Map.of(
                "font", font, "style", italic, "weight", weight,
                "size", size, "text", text);
        clientReady.thenRun(() -> sendRequest(request));
        return result;
    }

    private void sendRequest(Map<String, Object> request) {
        try {
            serializeAndSend(eventName(this, "fontSizeRequest"), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

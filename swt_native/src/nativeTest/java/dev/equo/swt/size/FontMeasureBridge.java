package dev.equo.swt.size;

import dev.equo.swt.harness.FlutterHarness;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

    private CompletableFuture<double[]> lineMetricsResult;

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
                if (lineMetricsResult != null) lineMetricsResult.complete(arr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** [width, height] for one sample-text measurement. */
    CompletableFuture<double[]> measureLineMetrics(String font, boolean italic, int weight, int size, String text) {
        lineMetricsResult = new CompletableFuture<>();
        Map<String, Object> request = Map.of(
                "font", font, "style", italic, "weight", weight,
                "size", size, "text", text);
        clientReady.thenRun(() -> sendRequest(request));
        return lineMetricsResult;
    }

    private void sendRequest(Map<String, Object> request) {
        try {
            serializeAndSend(eventName(this, "fontSizeRequest"), request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

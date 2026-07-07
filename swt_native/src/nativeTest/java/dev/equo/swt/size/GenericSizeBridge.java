package dev.equo.swt.size;

import dev.equo.swt.harness.FlutterHarness;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartWidget;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Generic base class for size measurement bridges.
 * Eliminates duplicate code across ImageSizeBridge and WidgetSizeBridge.
 *
 * @param <REQUEST> The type of request payload to send to Flutter
 * @param <SERIALIZED> The type used for deserialization from Flutter
 * @param <RESULT> The final result type (may be same as SERIALIZED or converted)
 */
abstract class GenericSizeBridge<REQUEST, SERIALIZED, RESULT> extends FlutterHarness implements BeforeAllCallback, AfterAllCallback {

    private CompletableFuture<RESULT> result;
    private final String requestChannel;
    private final String responseChannel;
    private final Class<SERIALIZED> serializedClass;
    Point windowSize;

    protected GenericSizeBridge(String channelPrefix, Class<SERIALIZED> serializedClass) {
        this.requestChannel = channelPrefix + "Request";
        this.responseChannel = channelPrefix + "Response";
        this.serializedClass = serializedClass;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        registerBridge();
        initFlutterView();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        teardown();
    }

    /**
     * Convert the serialized result to the final result type.
     * Override this if conversion is needed (e.g., double[] to PointD).
     */
    protected RESULT convertResult(SERIALIZED serialized) {
        return (RESULT) serialized;
    }

    /**
     * Measure by sending a request to Flutter and returning a future with the result.
     */
    protected CompletableFuture<RESULT> measure(REQUEST request) {
        result = new CompletableFuture<>();
        clientReady.thenRun(() -> {
            try {
                serializeAndSend(eventName(this, requestChannel), request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return result;
    }

    private void initFlutterView() {
        CompletableFuture<Point> windowSizeFuture = onReady(this, Point.class);
        CompletableFuture<Void> windowReady = windowSizeFuture.thenAccept((p) -> {
            System.out.println("Received windowSize " + p);
            this.windowSize = p;
        });
        bootRenderer(id(this), widgetName(this));
        onPayload(this, responseChannel, p -> {
            try {
                SERIALIZED serialized = serializer.from(serializedClass, p);
                RESULT dartResult = convertResult(serialized);
                windowReady.thenRun(() ->
                    result.complete(dartResult)
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void destroy(DartWidget control) {
        removeEvent(control, this.responseChannel);
    }
}

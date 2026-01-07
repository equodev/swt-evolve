package dev.equo.swt.size;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Generic base class for size measurement bridges.
 * Eliminates duplicate code across ImageSizeBridge, FontSizeBridge, and WidgetSizeBridge.
 *
 * @param <REQUEST> The type of request payload to send to Flutter
 * @param <SERIALIZED> The type used for deserialization from Flutter
 * @param <RESULT> The final result type (may be same as SERIALIZED or converted)
 */
abstract class GenericSizeBridge<REQUEST, SERIALIZED, RESULT> extends SwtFlutterBridgeBase implements BeforeAllCallback, AfterAllCallback {

    private CompletableFuture<RESULT> result;
    private final String requestChannel;
    private final String responseChannel;
    private final Class<SERIALIZED> serializedClass;
    private long ctx;
    Point windowSize;

    protected GenericSizeBridge(String channelPrefix, Class<SERIALIZED> serializedClass) {
        super(null);
        this.requestChannel = channelPrefix + "Request";
        this.responseChannel = channelPrefix + "Response";
        this.serializedClass = serializedClass;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        SwtFlutterBridge.set(this);
        initFlutterView();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        Dispose(ctx);
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
        CompletableFuture<Point> windowSize = super.onReady(this, Point.class);
        CompletableFuture<Void> windowReady = windowSize.thenAccept((p) ->
            this.windowSize = (p != null && p.x != 0 && p.y != 0) ? p : this.windowSize
        );
        ctx = InitializeFlutterWindow(client.getPort(), 0, id(this), widgetName(this), "", 0, 0);
        onPayload(this, responseChannel, p -> {
            ByteArrayInputStream in = new ByteArrayInputStream(((String) p).getBytes(StandardCharsets.UTF_8));
            try {
                SERIALIZED serialized = serializer.from(serializedClass, in);
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
    protected long getHandle(Control control) {
        return 0;
    }

    @Override
    protected void setHandle(DartControl control, long view) {
    }

    @Override
    public void destroy(DartWidget control) {
        super.destroy(control);
        removeEvent(control, this.responseChannel);
    }

    @Override
    protected void destroyHandle(DartControl dartControl) {
    }

    @Override
    public Object container(DartComposite parent) {
        return null;
    }

    @Override
    public void reparent(DartControl control, Composite newParent) {
    }
}
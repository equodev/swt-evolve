package dev.equo.swt.harness;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A minimal Chrome DevTools Protocol session on the harness page, so a test delivers input through
 * the browser into Flutter's own handling, as a user does.
 */
public final class DevTools implements AutoCloseable {

    private static final long TIMEOUT_MS = Long.getLong("harness.devtoolsTimeoutMs", 10_000);

    private final WebSocket socket;
    private final AtomicInteger ids = new AtomicInteger();
    private final Map<Integer, CompletableFuture<JsonObject>> pending = new ConcurrentHashMap<>();

    private DevTools(WebSocket socket) {
        this.socket = socket;
    }

    /** Connects to the first page target whose URL starts with {@code pageUrlPrefix}. */
    public static DevTools connect(Path profileDir, String pageUrlPrefix) {
        HttpClient http = HttpClient.newHttpClient();
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        String wsUrl = null;
        while (wsUrl == null) {
            try {
                Path portFile = profileDir.resolve("DevToolsActivePort");
                if (Files.isRegularFile(portFile)) {
                    String port = Files.readAllLines(portFile).get(0).trim();
                    HttpResponse<String> list = http.send(
                            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/json/list")).build(),
                            HttpResponse.BodyHandlers.ofString());
                    for (JsonElement e : JsonParser.parseString(list.body()).getAsJsonArray()) {
                        JsonObject target = e.getAsJsonObject();
                        if ("page".equals(target.get("type").getAsString())
                                && target.get("url").getAsString().startsWith(pageUrlPrefix)) {
                            wsUrl = target.get("webSocketDebuggerUrl").getAsString();
                            break;
                        }
                    }
                }
            } catch (IOException | RuntimeException ignored) {
                // Chrome has not written the port file or opened the page yet.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            if (wsUrl == null) {
                if (System.currentTimeMillis() > deadline)
                    throw new IllegalStateException("No DevTools page target for " + pageUrlPrefix);
                sleep(50);
            }
        }
        Listener listener = new Listener();
        WebSocket ws = http.newWebSocketBuilder()
                .connectTimeout(Duration.ofMillis(TIMEOUT_MS))
                .buildAsync(URI.create(wsUrl), listener)
                .join();
        DevTools devTools = new DevTools(ws);
        listener.owner = devTools;
        return devTools;
    }

    /** Sends a command and blocks for its result; a protocol error is thrown. */
    public JsonObject send(String method, JsonObject params) {
        int id = ids.incrementAndGet();
        JsonObject message = new JsonObject();
        message.addProperty("id", id);
        message.addProperty("method", method);
        message.add("params", params == null ? new JsonObject() : params);
        CompletableFuture<JsonObject> response = new CompletableFuture<>();
        pending.put(id, response);
        socket.sendText(message.toString(), true).join();
        JsonObject result;
        try {
            result = response.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("DevTools " + method + " did not answer", e);
        } finally {
            pending.remove(id);
        }
        if (result.has("error"))
            throw new IllegalStateException("DevTools " + method + ": " + result.get("error"));
        return result.has("result") ? result.getAsJsonObject("result") : new JsonObject();
    }

    /** Evaluates {@code expression} in the page, awaiting a returned promise, and returns its value. */
    public JsonElement evaluate(String expression) {
        JsonObject params = new JsonObject();
        params.addProperty("expression", expression);
        params.addProperty("awaitPromise", true);
        params.addProperty("returnByValue", true);
        params.addProperty("userGesture", true);
        JsonObject result = send("Runtime.evaluate", params);
        if (result.has("exceptionDetails"))
            throw new IllegalStateException("Page threw: " + result.get("exceptionDetails"));
        JsonObject value = result.getAsJsonObject("result");
        return value.has("value") ? value.get("value") : null;
    }

    @Override
    public void close() {
        socket.abort();
    }

    private final Map<String, java.util.function.Consumer<JsonObject>> eventListeners = new ConcurrentHashMap<>();

    /** Calls {@code listener} with the params of every {@code method} event, on the socket's thread. */
    public void onEvent(String method, java.util.function.Consumer<JsonObject> listener) {
        if (listener == null) eventListeners.remove(method);
        else eventListeners.put(method, listener);
    }

    private void onMessage(String text) {
        JsonObject message = JsonParser.parseString(text).getAsJsonObject();
        if (!message.has("id")) {
            java.util.function.Consumer<JsonObject> listener =
                    message.has("method") ? eventListeners.get(message.get("method").getAsString()) : null;
            if (listener != null) listener.accept(message.getAsJsonObject("params"));
            return;
        }
        CompletableFuture<JsonObject> f = pending.get(message.get("id").getAsInt());
        if (f != null) f.complete(message);
    }

    static JsonArray array(String... values) {
        JsonArray array = new JsonArray();
        for (String v : values) array.add(v);
        return array;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class Listener implements WebSocket.Listener {
        DevTools owner;
        private final StringBuilder partial = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            partial.append(data);
            if (last) {
                String text = partial.toString();
                partial.setLength(0);
                if (owner != null) owner.onMessage(text);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, java.nio.ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }
    }
}

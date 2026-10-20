package dev.equo.swt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serves the documents a web {@code Browser} shows for {@code setText}. The page posts the markup
 * and loads the path it gets back, so the frame holds a same-origin http document the page can
 * script; a frame showing a {@code blob:} URL is not scriptable from its creator on every browser.
 *
 * <p>{@code POST /inline-document} stores a document and answers its path,
 * {@code GET /inline-document/<token>} serves it with {@link BrowserFunctionBootstrap} injected, and
 * {@code DELETE} drops it once the Browser shows another {@code setText} document or is disposed.
 */
class InlineDocumentHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(InlineDocumentHandler.class.getName());

    static final String PATH = "/inline-document";

    /** Bounds what a page that never deletes its documents can leave behind. */
    private static final int MAX_DOCUMENTS = 32;

    /** The page shows a document refused here from a {@code blob:} URL instead. */
    static final int MAX_DOCUMENT_BYTES = 16 * 1024 * 1024;

    private final Map<String, byte[]> documents =
            Collections.synchronizedMap(new LinkedHashMap<String, byte[]>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > MAX_DOCUMENTS;
                }
            });

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String token = path.startsWith(PATH + "/") ? path.substring(PATH.length() + 1) : "";
            if ("POST".equalsIgnoreCase(method) && token.isEmpty()) {
                // Only the app's own page may add a document; a browser marks another origin's request.
                String site = exchange.getRequestHeaders().getFirst("Sec-Fetch-Site");
                if (site != null && !site.equals("same-origin")) {
                    sendText(exchange, 403, "Forbidden");
                    return;
                }
                byte[] document = readAtMost(exchange.getRequestBody(), MAX_DOCUMENT_BYTES);
                if (document == null) {
                    sendText(exchange, 413, "Payload Too Large");
                    return;
                }
                String newToken = LocalFileServing.newToken();
                documents.put(newToken, document);
                sendText(exchange, 200, PATH.substring(1) + "/" + newToken);
            } else if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
                byte[] document = documents.get(token);
                if (document == null) {
                    sendText(exchange, 404, "Not Found");
                } else {
                    send(exchange, 200, "text/html; charset=utf-8", BrowserFunctionBootstrap
                            .inject(new String(document, StandardCharsets.UTF_8))
                            .getBytes(StandardCharsets.UTF_8));
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                documents.remove(token);
                send(exchange, 204, "text/plain; charset=utf-8", new byte[0]);
            } else {
                sendText(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "inline-document error for " + exchange.getRequestURI(), e);
            try {
                sendText(exchange, 500, "error");
            } catch (IOException ignored) {
                // The response had already started.
            }
        } finally {
            exchange.close();
        }
    }

    /** The whole body, or {@code null} when it is longer than {@code max} bytes. */
    static byte[] readAtMost(InputStream in, int max) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) != -1; ) {
            if (out.size() + n > max) return null;
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private static void sendText(HttpExchange exchange, int code, String text) throws IOException {
        send(exchange, code, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange exchange, int code, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        WebFlutterServer.StaticFileHandler.setCrossOriginHeaders(exchange);
        boolean head = "HEAD".equalsIgnoreCase(exchange.getRequestMethod());
        exchange.sendResponseHeaders(code, body.length == 0 || head ? -1 : body.length);
        if (body.length > 0 && !head) {
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        }
    }
}

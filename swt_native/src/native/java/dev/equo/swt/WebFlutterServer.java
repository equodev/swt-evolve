package dev.equo.swt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP server that serves Flutter web build files and optionally launches a browser.
 * <p>
 * This class uses the JDK built-in {@link com.sun.net.httpserver.HttpServer} to serve
 * the Flutter web application, adding the required CORS and cross-origin isolation headers
 * for WASM SharedArrayBuffer support.
 * <p>
 * Usage:
 * <pre>
 *   WebFlutterServer server = new WebFlutterServer.Builder()
 *       .commPort(8090)
 *       .widgetId(12345)
 *       .widgetName("Button")
 *       .theme("dark")
 *       .backgroundColor(0x1E1E1E)
 *       .parentBackgroundColor(0x2D2D2D)
 *       .build();
 *
 *   server.start();           // starts HTTP server
 *   server.launchBrowser();   // opens browser pointing to the app
 *   // ...
 *   server.stop();
 * </pre>
 */
public class WebFlutterServer {

    static {
        // Trigger WebFontSubstitutions' static init so FontMetricsUtil maps system font names
        // (Arial, Courier, etc.) to their bundled Liberation equivalents on the web backend.
        WebFontSubstitutions.ensureRegistered();
    }

    private static final Logger LOG = Logger.getLogger(WebFlutterServer.class.getName());

    // Default MIME types for Flutter web files
    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"),
            Map.entry("htm", "text/html; charset=utf-8"),
            Map.entry("js", "application/javascript"),
            Map.entry("mjs", "application/javascript"),
            Map.entry("wasm", "application/wasm"),
            Map.entry("css", "text/css"),
            Map.entry("json", "application/json"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("webp", "image/webp"),
            Map.entry("woff", "font/woff"),
            Map.entry("woff2", "font/woff2"),
            Map.entry("ttf", "font/ttf"),
            Map.entry("otf", "font/otf"),
            Map.entry("eot", "application/vnd.ms-fontobject"),
            Map.entry("map", "application/json"),
            Map.entry("txt", "text/plain"),
            Map.entry("xml", "application/xml"),
            Map.entry("webmanifest", "application/manifest+json")
    );

    private static final String DEFAULT_MIME = "application/octet-stream";

    private final File webDirectory;
    private final int httpPort;
    private final int commPort;
    private final long widgetId;
    private final String widgetName;
    private final String theme;
    private final int backgroundColor;
    private final int parentBackgroundColor;
    private final String browserCommand;
    private final boolean serveServiceWorker;

    private HttpServer httpServer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    /** The headless Chrome we launched (see {@link #launchHeadless}); reaped in {@link #stop}. */
    private HeadlessChrome headlessChrome;

    private WebFlutterServer(Builder builder) {
        this.webDirectory = builder.webDirectory;
        this.httpPort = builder.httpPort;
        this.commPort = builder.commPort;
        this.widgetId = builder.widgetId;
        this.widgetName = builder.widgetName;
        this.theme = builder.theme;
        this.backgroundColor = builder.backgroundColor;
        this.parentBackgroundColor = builder.parentBackgroundColor;
        this.browserCommand = builder.browserCommand;
        this.serveServiceWorker = builder.serveServiceWorker;
    }

    /**
     * Starts the HTTP server. The server binds to {@code localhost} on the configured port
     * (or an ephemeral port if 0 was specified).
     *
     * @return the actual port the server is listening on.
     * @throws IOException          if the server cannot be started.
     * @throws IllegalStateException if the server is already running.
     */
    public int start() throws IOException {
        if (running.getAndSet(true)) {
            throw new IllegalStateException("Server is already running");
        }

        if (!webDirectory.isDirectory()) {
            running.set(false);
            throw new IOException("Web directory does not exist: " + webDirectory.getAbsolutePath());
        }

        httpServer = HttpServer.create(new InetSocketAddress("localhost", httpPort), 0);
        httpServer.createContext("/", new StaticFileHandler(webDirectory, commPort, widgetId, widgetName, serveServiceWorker));
        httpServer.createContext("/proxy", new ProxyHandler());
        httpServer.createContext("/equo-browser-function", new BrowserFunctionHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "WebFlutterServer-worker");
            t.setDaemon(true);
            return t;
        }));
        httpServer.start();

        int actualPort = httpServer.getAddress().getPort();
        LOG.info("WebFlutterServer started on http://localhost:" + actualPort);
//        System.out.println("WebFlutterServer started on http://localhost:" + actualPort);
        return actualPort;
    }

    /**
     * Stops the HTTP server, allowing in-flight requests up to 2 seconds to complete.
     */
    public void stop() {
        reapHeadlessBrowser();
        if (running.getAndSet(false) && httpServer != null) {
            httpServer.stop(2);
            LOG.info("WebFlutterServer stopped");
//            System.out.println("WebFlutterServer stopped");
        }
    }

    /**
     * Returns the actual port the server is listening on.
     *
     * @throws IllegalStateException if the server is not running.
     */
    public int getPort() {
        if (httpServer == null) {
            throw new IllegalStateException("Server is not running");
        }
        return httpServer.getAddress().getPort();
    }

    /**
     * Returns whether the server is currently running.
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Builds the full application URL including query parameters for Flutter communication.
     *
     * @return the complete URL string.
     */
    public String getApplicationUrl() {
        int port = httpServer != null ? httpServer.getAddress().getPort() : httpPort;
        return "http://localhost:" + port;
    }

    /**
     * Launches a browser pointing to the Flutter web application URL.
     * <p>
     * If a browser command was configured via the builder, that command is used.
     * Otherwise, the JDK {@link java.awt.Desktop} API is used to open the default browser.
     * If the system property {@code equo.swt.browser} is set to {@code "none"},
     * the URL is printed to stdout instead.
     *
     * @throws IOException if the browser cannot be launched.
     */
    public void launchBrowser() throws IOException {
        String url = getApplicationUrl();

        // Server-side auto-launch is gated by the equo.swt.browser system property:
        // "none" disables it. A host that serves a remote browser (where the
        // browser is on the user's machine, not the server) sets
        // equo.swt.browser=none so this never opens a tab server-side.
        if ("none".equalsIgnoreCase(browserCommand)) {
            System.out.println("Browser launch disabled. Open manually: " + url);
            return;
        }

        if (Boolean.getBoolean("dev.equo.swt.web.headless")) {
            launchHeadless(url);
            return;
        }

        if (browserCommand != null && !browserCommand.isEmpty()) {
            launchBrowserWithCommand(browserCommand, url);
        } else {
            launchDefaultBrowser(url);
        }
    }

    private void launchHeadless(String url) throws IOException {
        String chrome = HeadlessChrome.resolveBinary(browserCommand);
        if (chrome == null) {
            throw new IOException("Headless web mode requested but Chrome/Chromium not found; "
                    + "set -Dequo.swt.browser=<path to chrome>");
        }
        boolean console = Boolean.getBoolean("dev.equo.swt.web.console");
        headlessChrome = HeadlessChrome.launch(chrome, url, true, console,
                console ? HeadlessChrome.Io.INHERIT : HeadlessChrome.Io.DISCARD);
        LOG.info("Launched headless Chrome: " + chrome + " -> " + url);
    }

    private void reapHeadlessBrowser() {
        if (headlessChrome != null) {
            headlessChrome.close();
            headlessChrome = null;
        }
    }

    private void launchBrowserWithCommand(String command, String url) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        ProcessBuilder pb;
        if (os.contains("mac")) {
            pb = new ProcessBuilder("open", "-a", command, url);
        } else if (os.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", "start", "", command, url);
        } else {
            pb = new ProcessBuilder(command, url);
        }
        pb.inheritIO();
        Process proc = pb.start();
        LOG.info("Launched browser: " + command + " " + url);
        System.out.println("Launched browser: " + command + " -> " + url);
    }

    private void launchDefaultBrowser(String url) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).inheritIO().start();
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", url).inheritIO().start();
            } else if (os.contains("linux") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).inheritIO().start();
            } else {
                System.out.println("Cannot detect browser. Open manually: " + url);
                return;
            }
            LOG.info("Opened default browser: " + url);
            System.out.println("Opened default browser: " + url);
        } catch (Exception e) {
            throw new IOException("Failed to launch browser for URL: " + url, e);
        }
    }

    /**
     * Minimal URL encoding for query parameter values.
     */
    private static String urlEncode(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append(c);
            } else {
                byte[] bytes = String.valueOf(c).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%');
                    sb.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16)));
                    sb.append(Character.toUpperCase(Character.forDigit(b & 0xF, 16)));
                }
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Static file HTTP handler
    // -------------------------------------------------------------------------

    /**
     * Handles HTTP requests by serving static files from the Flutter web directory.
     * Adds cross-origin isolation headers required for WASM SharedArrayBuffer.
     */
    private static class StaticFileHandler implements HttpHandler {

        private final Path rootDir;
        private final int commPort;
        private final long widgetId;
        private final String widgetName;
        private final boolean serveServiceWorker;

        StaticFileHandler(File rootDir, int commPort, long widgetId, String widgetName, boolean serveServiceWorker) {
            this.rootDir = rootDir.toPath().toAbsolutePath().normalize();
            this.commPort = commPort;
            this.widgetId = widgetId;
            this.widgetName = widgetName;
            this.serveServiceWorker = serveServiceWorker;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Only GET and HEAD are meaningful for static files
                String method = exchange.getRequestMethod();
                if ("OPTIONS".equalsIgnoreCase(method)) {
                    handlePreflight(exchange);
                    return;
                }
                if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
                    sendError(exchange, 405, "Method Not Allowed");
                    return;
                }

                String requestPath = exchange.getRequestURI().getPath();

                // Normalize: strip leading slash, default to index.html
                if (requestPath == null || requestPath.isEmpty() || "/".equals(requestPath)) {
                    requestPath = "index.html";
                } else {
                    if (requestPath.startsWith("/")) {
                        requestPath = requestPath.substring(1);
                    }
                }

                // Optionally refuse the Flutter service worker so the page loads directly. Its
                // first-load install/activate races (a cold install on a fresh browser profile can
                // hang before the app connects); a deterministic boot matters for the test harness.
                // 404 (not the index.html SPA fallback) so registration cleanly fails and the loader
                // falls back to fetching the app directly.
                if (!serveServiceWorker && requestPath.endsWith("flutter_service_worker.js")) {
                    sendError(exchange, 404, "service worker disabled");
                    return;
                }

                // Prevent directory traversal
                Path resolved = rootDir.resolve(requestPath).normalize();
                if (!resolved.startsWith(rootDir)) {
                    sendError(exchange, 403, "Forbidden");
                    return;
                }

                File file = resolved.toFile();

                // If path is a directory, look for index.html inside it
                if (file.isDirectory()) {
                    file = new File(file, "index.html");
                }

                if (!file.isFile() || !file.canRead()) {
                    // For single-page apps, fall back to index.html for navigation routes
                    File fallback = rootDir.resolve("index.html").toFile();
                    if (fallback.isFile() && fallback.canRead()) {
                        file = fallback;
                    } else {
                        sendError(exchange, 404, "Not Found: " + requestPath);
                        return;
                    }
                }

                String mimeType = getMimeType(file.getName());

                // Set response headers
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                setCrossOriginHeaders(exchange);
                setCorsHeaders(exchange);
                setCacheHeaders(exchange, file.getName());

                if ("HEAD".equalsIgnoreCase(method)) {
                    exchange.sendResponseHeaders(200, -1);
                    return;
                }

                if (file.getName().equals("index.html")) {
                    String content = Files.readString(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
                    content = content
                            .replace("{{EQUO_COMM_PORT}}", String.valueOf(commPort))
                            .replace("{{EQUO_COMM_URL}}", "")
                            .replace("{{EQUO_WIDGETID}}", String.valueOf(widgetId))
                            .replace("{{EQUO_WIDGETNAME}}", widgetName != null ? widgetName : "")
                            .replace("{{EQUO_BROWSER_PROXY}}", String.valueOf(proxyEnabled()));
                    byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } else {
                    exchange.sendResponseHeaders(200, file.length());
                    try (OutputStream os = exchange.getResponseBody();
                         InputStream is = Files.newInputStream(file.toPath())) {
                        is.transferTo(os);
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Error serving request: " + exchange.getRequestURI(), e);
                throw e;
            } finally {
                exchange.close();
            }
        }

        private void handlePreflight(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            setCrossOriginHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }

        /**
         * Sets Cross-Origin headers required for WASM SharedArrayBuffer support.
         * Flutter web with multi-threading needs these headers to enable
         * SharedArrayBuffer in the browser.
         */
        private void setCrossOriginHeaders(HttpExchange exchange) {
            if (isCrossOriginIsolated()) {
                // Cross-origin isolated: enables SharedArrayBuffer (Flutter web threads),
                // but the page can NOT be embedded in a cross-origin iframe.
                exchange.getResponseHeaders().set("Cross-Origin-Opener-Policy", "same-origin");
                exchange.getResponseHeaders().set("Cross-Origin-Embedder-Policy", "credentialless");
            } else {
                // Permissive: embeddable in a cross-origin iframe (e.g. hosted browser),
                // but SharedArrayBuffer is unavailable.
                exchange.getResponseHeaders().set("Cross-Origin-Opener-Policy", "unsafe-none");
                exchange.getResponseHeaders().set("Cross-Origin-Embedder-Policy", "unsafe-none");
            }
        }

        /**
         * Whether to serve the app in cross-origin isolated mode (COOP {@code same-origin} +
         * COEP {@code credentialless}), which is required for WASM SharedArrayBuffer / Flutter
         * web multi-threading but prevents the page from being embedded in a cross-origin iframe.
         * <p>
         * Controlled by the {@code dev.equo.swt.web.crossOriginIsolated} system property. When the
         * property is unset, the default is {@code true}, except under Chromium standalone mode
         * ({@code dev.equo.swt.mode=chromium}), where the default reverts to {@code false} so the
         * app can run embedded in the Chromium window.
         */
        private boolean isCrossOriginIsolated() {
            String prop = System.getProperty("dev.equo.swt.web.crossOriginIsolated");
            if (prop != null) {
                return Boolean.parseBoolean(prop);
            }
            return !ConfigFlags.isChromiumMode();
        }

        /**
         * Sets CORS headers to allow the WebSocket connection from the served page.
         */
        private void setCorsHeaders(HttpExchange exchange) {
            String origin = exchange.getRequestHeaders().getFirst("Origin");
            if (origin != null) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            } else {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            }
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
                    "Content-Type, Authorization, X-Requested-With");
            exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");
        }

        /**
         * Sets cache headers. Immutable hashed assets get long cache, HTML is never cached.
         */
        private void setCacheHeaders(HttpExchange exchange, String fileName) {
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
                exchange.getResponseHeaders().set("Pragma", "no-cache");
            } else if (fileName.endsWith(".wasm") || fileName.endsWith(".js") || fileName.endsWith(".mjs")) {
                // Moderate cache for compiled assets; they may change between builds
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
            } else {
                exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
            }
        }

        private void sendError(HttpExchange exchange, int code, String message) throws IOException {
            byte[] body = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(code, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            exchange.close();
        }

        private static String getMimeType(String fileName) {
            int dot = fileName.lastIndexOf('.');
            if (dot >= 0 && dot < fileName.length() - 1) {
                String ext = fileName.substring(dot + 1).toLowerCase();
                return MIME_TYPES.getOrDefault(ext, DEFAULT_MIME);
            }
            return DEFAULT_MIME;
        }
    }

    // -------------------------------------------------------------------------
    // Same-origin proxy (opt-in)
    // -------------------------------------------------------------------------

    /**
     * Whether the same-origin Browser proxy is enabled, via the system property
     * {@code dev.equo.swt.web.proxy} set to {@code all} or a comma-separated host whitelist.
     * Off by default — it is a real reverse proxy (SSRF surface), so keep it opt-in.
     */
    static boolean proxyEnabled() {
        String p = System.getProperty("dev.equo.swt.web.proxy");
        return p != null && !p.isBlank();
    }

    /** Whether {@code url}'s host is permitted by the proxy property ({@code all} or whitelist). */
    static boolean proxyAllowed(String url) {
        String p = System.getProperty("dev.equo.swt.web.proxy");
        if (p == null || p.isBlank()) return false;
        if ("all".equalsIgnoreCase(p.trim())) return true;
        String host;
        try {
            host = URI.create(url).getHost();
        } catch (Exception e) {
            return false;
        }
        if (host == null) return false;
        for (String allowed : p.split(",")) {
            allowed = allowed.trim();
            if (!allowed.isEmpty() && (host.equals(allowed) || host.endsWith("." + allowed))) return true;
        }
        return false;
    }

    /** Extracts a single URL-decoded query parameter from a raw query string. */
    private static String queryParam(String rawQuery, String name) {
        if (rawQuery == null) return null;
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * Fetches an external URL server-side and re-serves it from this origin, so the Browser's iframe
     * content becomes same-origin (enabling eval/execute/BrowserFunction). Strips framing-blocking
     * headers and injects a {@code <base href>} so the page's relative sub-resources still resolve
     * against the original site.
     */
    private static class ProxyHandler implements HttpHandler {

        private final HttpClient client =
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!proxyEnabled()) { sendPlain(exchange, 403, "proxy disabled"); return; }
                String target = queryParam(exchange.getRequestURI().getRawQuery(), "url");
                if (target == null || !proxyAllowed(target)) { sendPlain(exchange, 403, "url not allowed"); return; }

                HttpRequest req = HttpRequest.newBuilder(URI.create(target)).GET().build();
                HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                String contentType = resp.headers().firstValue("content-type").orElse("text/html; charset=utf-8");
                byte[] body = resp.body();
                if (contentType.toLowerCase().contains("html")) {
                    body = injectBaseHref(new String(body, StandardCharsets.UTF_8), resp.uri().toString())
                            .getBytes(StandardCharsets.UTF_8);
                }
                // Serve from this origin; deliberately do NOT copy X-Frame-Options / CSP frame-ancestors.
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "proxy error for " + exchange.getRequestURI(), e);
                try { sendPlain(exchange, 502, "proxy error"); } catch (IOException ignored) { }
            } finally {
                exchange.close();
            }
        }

        private static String injectBaseHref(String html, String finalUrl) {
            String baseTag = "<base href=\"" + finalUrl.replace("\"", "%22") + "\">";
            Matcher m = Pattern.compile("<head[^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
            return m.find() ? html.substring(0, m.end()) + baseTag + html.substring(m.end())
                            : baseTag + html;
        }

        private static void sendPlain(HttpExchange exchange, int code, String msg) throws IOException {
            byte[] b = msg.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(code, b.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
        }
    }

    // -------------------------------------------------------------------------
    // BrowserFunction bridge (same-origin, synchronous)
    // -------------------------------------------------------------------------

    /**
     * Backs the JS shim injected into a same-origin Browser iframe for each
     * registered {@code BrowserFunction} (see {@code EvolveBrowser.createFunction}).
     * The shim does a blocking XHR here with {@code {browserId, name, args}};
     * this handler dispatches to the Java callback via
     * {@link BrowserFunctionRegistry} (which runs it on the SWT display thread)
     * and returns {@code {"value": <json>}} or {@code {"error": <message>}}. The
     * iframe is served from this origin (via the proxy), so no CORS is needed.
     */
    private static class BrowserFunctionHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                    return;
                }
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Object parsed = EvalJson.parse(body);
                if (!(parsed instanceof Map)) {
                    sendJson(exchange, 400, "{\"error\":\"bad request\"}");
                    return;
                }
                Map<?, ?> m = (Map<?, ?>) parsed;
                long browserId = m.get("browserId") instanceof Number
                        ? ((Number) m.get("browserId")).longValue() : -1;
                String name = String.valueOf(m.get("name"));
                Object argsObj = m.get("args");
                Object[] args = (argsObj instanceof Object[]) ? (Object[]) argsObj : new Object[0];

                Object value = BrowserFunctionRegistry.invoke(browserId, name, args);
                sendJson(exchange, 200, "{\"value\":" + EvalJson.encode(value) + "}");
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                sendJson(exchange, 200, "{\"error\":" + EvalJson.encode(msg) + "}");
            } finally {
                exchange.close();
            }
        }

        private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(code, b.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Builder for {@link WebFlutterServer}.
     */
    public static class Builder {

        private File webDirectory;
        private int httpPort = 0; // 0 = ephemeral
        private int commPort;
        private long widgetId;
        private String widgetName = "";
        private String theme = "light";
        private int backgroundColor = 0xFFFFFF;
        private int parentBackgroundColor = 0xFFFFFF;
        private String browserCommand;
        private boolean serveServiceWorker = true;

        public Builder() {
        }

        /**
         * Sets the directory containing the Flutter web build output.
         * If not set, the builder will use {@link FlutterLibraryLoader#getWebDirectory()}.
         */
        public Builder webDirectory(File dir) {
            this.webDirectory = dir;
            return this;
        }

        /**
         * Sets the HTTP port. Use 0 (default) to let the OS pick an available port.
         */
        public Builder httpPort(int port) {
            this.httpPort = port;
            return this;
        }

        /**
         * Sets the WebSocket communication port that Flutter uses to talk to the Java side.
         */
        public Builder commPort(int port) {
            this.commPort = port;
            return this;
        }

        /** Sets the widget ID passed to Flutter via query parameter. */
        public Builder widgetId(long id) {
            this.widgetId = id;
            return this;
        }

        /** Sets the widget name passed to Flutter via query parameter. */
        public Builder widgetName(String name) {
            this.widgetName = name;
            return this;
        }

        /** Sets the theme (e.g., "dark" or "light") passed to Flutter via query parameter. */
        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }

        /** Sets the background color (RGB int) passed to Flutter via query parameter. */
        public Builder backgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        /** Sets the parent background color (RGB int) passed to Flutter via query parameter. */
        public Builder parentBackgroundColor(int color) {
            this.parentBackgroundColor = color;
            return this;
        }

        /**
         * Sets the browser command to use for launching.
         * <p>
         * Examples:
         * <ul>
         *   <li>{@code "google-chrome"} or {@code "/usr/bin/chromium-browser"} on Linux</li>
         *   <li>{@code "Google Chrome"} on macOS (used with {@code open -a})</li>
         *   <li>{@code "msedge"} on Windows</li>
         *   <li>{@code "none"} to disable browser launch (URL printed to stdout)</li>
         *   <li>{@code null} (default) to use the OS default browser</li>
         * </ul>
         * Can also be set via the system property {@code equo.swt.browser}.
         */
        public Builder browserCommand(String command) {
            this.browserCommand = command;
            return this;
        }

        /**
         * When {@code false}, the server refuses {@code flutter_service_worker.js} so the page loads
         * directly without registering the Flutter service worker. Defaults to {@code true}
         * (production keeps the SW for offline caching). Tests/automation set this {@code false} for a
         * deterministic boot — a cold SW install on a fresh browser profile can race and hang.
         */
        public Builder serveServiceWorker(boolean serve) {
            this.serveServiceWorker = serve;
            return this;
        }

        /**
         * Builds the {@link WebFlutterServer} instance.
         *
         * @throws IllegalStateException if the web directory cannot be resolved.
         */
        public WebFlutterServer build() {
            if (webDirectory == null) {
                webDirectory = FlutterLibraryLoader.initializeWeb();
            }
            if (webDirectory == null || !webDirectory.isDirectory()) {
                throw new IllegalStateException(
                        "Flutter web directory not found. Build the Flutter web app first, "
                                + "or set the directory explicitly via webDirectory().");
            }
            if (browserCommand == null) {
                browserCommand = System.getProperty("equo.swt.browser");
            }
            return new WebFlutterServer(this);
        }
    }
}

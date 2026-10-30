package dev.equo.swt;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * The static {@code Browser.getCookie}/{@code setCookie}. Cookies belong to the webview profile,
 * so the request goes through any live Browser, and like {@code evaluate} it pumps the event loop
 * until the Flutter side replies.
 */
public final class BrowserCookies {

    private BrowserCookies() {
    }

    /**
     * The value of cookie {@code name} for {@code url}, or null when there is none. On the web the
     * page cannot read the iframe's cookies at all; the ones it can have are the proxied sites',
     * kept server-side by the Display's web server.
     */
    public static String get(DartWidget browser, Display display, String name, String url) {
        ProxyCookieJar web = ProxyCookieJar.forDisplay(display);
        if (web != null) return web.get(name, url);
        return request(browser, display, dev.equo.swt.WireTable.CHANNEL_GET_COOKIE, Java8.map("name", name, "url", url));
    }

    /**
     * Sets {@code value}, a {@code Set-Cookie} header value, on {@code url}. A cookie without a
     * domain or path takes the URL's, as the native SWT browsers do. Returns whether it was set.
     */
    public static boolean set(DartWidget browser, Display display, String value, String url) {
        ProxyCookieJar web = ProxyCookieJar.forDisplay(display);
        if (web != null) return web.set(value, url);
        HttpCookie cookie;
        URI origin;
        try {
            List<HttpCookie> parsed = HttpCookie.parse(value);
            if (parsed.isEmpty()) return false;
            cookie = parsed.get(0);
            origin = new URI(url);
        } catch (Exception e) {
            return false;
        }
        String domain = cookie.getDomain() != null ? cookie.getDomain() : origin.getHost();
        String path = cookie.getPath() != null ? cookie.getPath() : origin.getPath();
        if (domain == null) return false;
        Map<String, Object> args = Java8.map(
                "name", cookie.getName(),
                "value", cookie.getValue(),
                "domain", domain,
                "path", path == null || path.isEmpty() ? "/" : path,
                "secure", cookie.getSecure(),
                "httpOnly", cookie.isHttpOnly());
        if (cookie.getMaxAge() != -1) {
            args.put("expires", System.currentTimeMillis() / 1000.0 + cookie.getMaxAge());
        }
        return "true".equals(request(browser, display, dev.equo.swt.WireTable.CHANNEL_SET_COOKIE, args));
    }

    private static String request(DartWidget browser, Display display, int op, Map<String, Object> args) {
        int reqId = NEXT_REQUEST.incrementAndGet();
        CompletableFuture<String> reply = new CompletableFuture<>();
        PENDING.put(reqId, new Pending(browser, reply));
        // One channel for every reply; each names the request it answers in its detail.
        FlutterBridge.onPayload(browser, dev.equo.swt.WireTable.CHANNEL_COOKIE, Event.class, e -> complete(browser, e));
        args.put("reqId", reqId);
        try {
            FlutterBridge.send(browser, op, args);
            return BrowserScripting.awaitReply(display, reply) ? reply.getNow(null) : null;
        } finally {
            PENDING.remove(reqId);
        }
    }

    private static final java.util.concurrent.atomic.AtomicInteger NEXT_REQUEST = new java.util.concurrent.atomic.AtomicInteger();

    /**
     * The cookie requests waiting for their reply, by request number. Request numbers are shared by
     * every session in the JVM, so a reply only completes a request made through the Browser it
     * arrived on.
     */
    private static final Map<Integer, Pending> PENDING = new java.util.concurrent.ConcurrentHashMap<>();

    private static final class Pending {
        final DartWidget browser;
        final CompletableFuture<String> reply;

        Pending(DartWidget browser, CompletableFuture<String> reply) {
            this.browser = browser;
            this.reply = reply;
        }
    }

    private static void complete(DartWidget browser, Event reply) {
        if (reply == null) return;
        Pending waiting = PENDING.get(reply.detail);
        if (waiting == null || waiting.browser != browser) return;
        if (PENDING.remove(reply.detail, waiting)) waiting.reply.complete(reply.text);
    }
}

package dev.equo.swt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches an external URL server-side and re-serves it from this origin, so the Browser's iframe
 * content becomes same-origin (enabling eval/execute/BrowserFunction). Strips framing-blocking
 * headers and injects a {@code <base href>} so the page's relative sub-resources still resolve
 * against the original site.
 *
 * <p>Every response here carries this app's own cross-origin isolation headers. When the app is
 * served cross-origin isolated, a nested document that does not itself declare a
 * {@code Cross-Origin-Embedder-Policy} is refused by the browser even when it is same-origin —
 * the frame shows "refused to connect" instead of the page. {@code credentialless} is what makes
 * that work: the proxied document's own sub-resources still come from the original site (via the
 * injected {@code <base href>}) and would need {@code Cross-Origin-Resource-Policy} headers we
 * cannot add on its behalf under {@code require-corp}.
 *
 * <p>Uses {@link HttpURLConnection}, not {@code java.net.http.HttpClient} (Java 11): this class
 * lives in {@code src/native/java}, compiled down to the oldest JDK a supported SWT release
 * targets, which can be Java 8.
 */
class ProxyHandler implements HttpHandler {

    private static final Logger LOG = Logger.getLogger(ProxyHandler.class.getName());

    private static final int MAX_REDIRECTS = 5;

    /** The proxied sites' cookies: they reach no browser, since every page is served from here. */
    private final ProxyCookieJar cookies;

    ProxyHandler(ProxyCookieJar cookies) {
        this.cookies = cookies;
    }

    // Headers last seen for a given target url, keyed by the exact decoded target. A served
    // page has no idea it is being proxied (the injected <base href> hides that), so when its
    // own client-side script re-navigates or "canonicalizes" the address -- a bare reload of
    // the same target, no synthetic header= param, because the page never knew that param
    // existed to preserve -- that re-request still needs the same auth this origin already
    // proved once. Caching here, keyed purely by target url, covers that regardless of whether
    // the repeat request came from Dart's own navigate op or from inside the iframe itself.
    // Bounded LRU (eldest-evicted, access-ordered) so a long session cycling through many
    // distinct local endpoints doesn't grow this without limit.
    private static final int TARGET_AUTH_CACHE_MAX = 64;
    private static final Map<String, List<String>> TARGET_AUTH_CACHE = lruMap(TARGET_AUTH_CACHE_MAX);

    // The header lines last sent for an origin on this server: a module requested in path form has
    // no query of its own to carry them. One per server, like the cookie jar.
    private final Map<String, List<String>> originAuthCache = lruMap(TARGET_AUTH_CACHE_MAX);

    private static <K, V> Map<K, V> lruMap(final int max) {
        return Collections.synchronizedMap(new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > max;
            }
        });
    }

    /**
     * Request headers from the page that describe its body and pick the representation and
     * language, forwarded upstream unless {@code Browser.setUrl} gave its own. Not
     * {@code Accept-Encoding} ({@link HttpURLConnection} does not decode a compressed body) nor
     * conditional-cache headers (a {@code 304} has no body to serve).
     */
    private static final String[] FORWARDED_HEADERS = {"Content-Type", "Accept", "Accept-Language"};

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            // The path form first: a module's own query may carry url= or header= of its own.
            String target = pathFormTarget(exchange.getRequestURI());
            boolean pathForm = target != null;
            if (!pathForm) target = queryParam(rawQuery, "url");
            if (target == null || !WebFlutterServer.proxyAllowed(target)) { sendPlain(exchange, 403, "url not allowed"); return; }

            // Headers from Browser.setUrl(url, postData, headers): an iframe navigation can't
            // carry custom HTTP headers at all (a web-platform limit, not an Equo one), so a
            // caller that gates a GET on a header (an auth token an embedded local server
            // expects, say) is unreachable through a plain src= load. The Dart side forwards
            // each "Name: value" line here (one repeated "header" param) and we attach them to
            // the server-side fetch instead. A header HttpURLConnection itself rejects (Host,
            // Content-Length, …) is skipped rather than failing the whole proxy.
            List<String> headerLines = pathForm ? new ArrayList<String>() : queryParams(rawQuery, "header");
            if (!headerLines.isEmpty()) {
                TARGET_AUTH_CACHE.put(target, headerLines);
                originAuthCache.put(originOf(URI.create(target)), headerLines);
            } else if (pathForm) {
                List<String> remembered = TARGET_AUTH_CACHE.get(target);
                if (remembered == null) remembered = originAuthCache.get(originOf(URI.create(target)));
                if (remembered != null) headerLines = remembered;
            } else {
                List<String> remembered = TARGET_AUTH_CACHE.get(target);
                if (remembered == null && isNavigation(exchange.getRequestHeaders().getFirst("Sec-Fetch-Dest"))) {
                    // A header-bearing sibling request for this same target can be in flight on
                    // another worker thread right now (the app's own two near-simultaneous
                    // requests for one logical "open" -- see TARGET_AUTH_CACHE's own comment).
                    // Racing it empty-handed serves an avoidable 403 the very first time a target
                    // is opened. But most targets a Browser ever navigates to carry no auth
                    // headers at all and never will -- this wait runs for every one of them on a
                    // cache miss, not just the header-gated ones, so it has to stay short: a
                    // caller with no sibling in flight pays it in full on every first navigation.
                    long deadline = System.currentTimeMillis() + 300;
                    while (remembered == null && System.currentTimeMillis() < deadline) {
                        try { Thread.sleep(20); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        remembered = TARGET_AUTH_CACHE.get(target);
                    }
                }
                if (remembered != null) headerLines = remembered;
            }

            // Forward the real method + body too: the shim (injectFetchShim) redirects a page's
            // own fetch/XHR calls through this same URL scheme, and those aren't all reads — a
            // checkbox toggle or a cell edit in an embedded app is a PUT/POST/PATCH with a body.
            // Hardcoding GET here silently turned every write into a no-op read.
            String method = exchange.getRequestMethod();
            boolean hasBody = !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method);
            byte[] requestBody = hasBody ? Java8.readAllBytes(exchange.getRequestBody()) : new byte[0];
            Map<String, String> forwardedHeaders = new LinkedHashMap<>();
            for (String name : FORWARDED_HEADERS) {
                String value = exchange.getRequestHeaders().getFirst(name);
                if (value != null) forwardedHeaders.put(name, value);
            }

            Fetched resp = fetch(target, method, requestBody, forwardedHeaders, headerLines, cookies);
            String contentType = resp.contentType != null ? resp.contentType : "text/html; charset=utf-8";
            byte[] body = resp.body;
            String lowerContentType = contentType.toLowerCase();
            // The URL a rewritten link routes through: this server's own origin, not the target's.
            // A page we serve carries an injected <base href> pointing at the *target* (so the
            // target's own untouched relative references keep resolving against it) -- so a
            // root-relative "/proxy?..." link we generate would resolve against that base and hit
            // the target's real server directly, on a path it never heard of, instead of coming
            // back to us.
            String selfOrigin = "http://localhost:" + exchange.getLocalAddress().getPort();
            if (lowerContentType.contains("html")) {
                String html = new String(body, StandardCharsets.UTF_8);
                URI finalUri = URI.create(resp.finalUrl);
                // Rewrite first, inject the <base> afterwards: rewriteResourceUrls rewrites every
                // href=, so a <base> already in place would itself be rewritten to a /proxy?url=
                // wrapper -- and the page's real address is read back from document.baseURI.
                html = rewriteResourceUrls(html, finalUri, headerLines, selfOrigin);
                html = rewriteModuleScripts(html, selfOrigin);
                html = rewriteFormActions(html, finalUri, headerLines, selfOrigin);
                html = injectModuleImportMap(html, finalUri, selfOrigin);
                html = injectBaseHref(html, resp.finalUrl);
                html = injectNavigationShim(html, originOf(finalUri), headerLines, selfOrigin);
                if (!headerLines.isEmpty()) {
                    html = injectFetchShim(html, originOf(finalUri), headerLines, selfOrigin);
                }
                html = BrowserFunctionBootstrap.inject(html);
                body = html.getBytes(StandardCharsets.UTF_8);
            } else if (lowerContentType.contains("css")) {
                // A stylesheet's own url() references (icon fonts, background/mask-image icons)
                // are a browser-native CSS fetch, not src=/href= markup and not fetch/XHR — neither
                // rewriteResourceUrls nor the injected shim ever sees them, so without this an
                // icon font or SVG icon set behind the same auth gate as the page silently 403s
                // (or CORS-blocked) and every icon that depends on it renders blank.
                String css = new String(body, StandardCharsets.UTF_8);
                css = rewriteCssUrls(css, URI.create(resp.finalUrl), headerLines, selfOrigin);
                body = css.getBytes(StandardCharsets.UTF_8);
            }
            // Serve from this origin; deliberately do NOT copy X-Frame-Options / CSP frame-ancestors.
            exchange.getResponseHeaders().set("Content-Type", contentType);
            // Cached as the origin says, unless the response depends on what the browser's cache key
            // cannot see: a page (the cookie jar, the shims), header lines (a rewritten stylesheet
            // embeds them), a redirect (the headers belong to another URL), or a Vary on
            // credentials the browser never sends here.
            boolean cacheable = !lowerContentType.contains("html") && headerLines.isEmpty()
                    && resp.finalUrl.equals(target) && !resp.cacheHeaders.isEmpty()
                    && !variesOnCredentials(resp.cacheHeaders.get("Vary"));
            if (!cacheable) {
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
            } else {
                for (Map.Entry<String, String> header : resp.cacheHeaders.entrySet()) {
                    exchange.getResponseHeaders().set(header.getKey(), header.getValue());
                }
            }
            WebFlutterServer.StaticFileHandler.setCrossOriginHeaders(exchange);
            exchange.sendResponseHeaders(resp.statusCode, body.length == 0 ? -1 : body.length);
            if (body.length > 0) {
                try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "proxy error for " + exchange.getRequestURI(), e);
            try { sendPlain(exchange, 502, "proxy error"); } catch (IOException ignored) { }
        } finally {
            exchange.close();
        }
    }

    /** What the proxied request came back with, plus the URL and method it ended on. */
    static final class Fetched {
        final String finalUrl;
        final int statusCode;
        final byte[] body;
        final String contentType;
        /** The origin's caching headers ({@link #CACHE_HEADERS}) it sent. */
        final Map<String, String> cacheHeaders;

        Fetched(String finalUrl, int statusCode, byte[] body, String contentType, Map<String, String> cacheHeaders) {
            this.finalUrl = finalUrl;
            this.statusCode = statusCode;
            this.body = body;
            this.contentType = contentType;
            this.cacheHeaders = cacheHeaders;
        }
    }

    /**
     * The headers that decide how long a response may be reused. No validators: a conditional request
     * is not forwarded, so a {@code 304} could never come back to revalidate with.
     */
    private static final String[] CACHE_HEADERS = {"Cache-Control", "Expires", "Vary"};

    private static Map<String, String> cacheHeaders(HttpURLConnection conn) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String name : CACHE_HEADERS) {
            String value = conn.getHeaderField(name);
            if (value != null) headers.put(name, value);
        }
        if (!headers.containsKey("Cache-Control") && !headers.containsKey("Expires")) headers.clear();
        return headers;
    }

    /**
     * Whether the request loads a document. Only a Browser's own navigation can race a sibling
     * request that carries its headers; a sub-resource of a page served without headers has none
     * to wait for.
     */
    static boolean isNavigation(String secFetchDest) {
        return secFetchDest == null || secFetchDest.equals("iframe") || secFetchDest.equals("document")
                || secFetchDest.equals("frame") || secFetchDest.equals("embed") || secFetchDest.equals("object");
    }

    private static boolean variesOnCredentials(String vary) {
        if (vary == null) return false;
        String lower = vary.toLowerCase();
        return lower.contains("cookie") || lower.contains("authorization") || lower.contains("*");
    }

    /**
     * Follows redirects itself: {@link HttpURLConnection} will not follow one that changes
     * protocol (the http -> https hop most sites open with), and the URL the response ended on —
     * which the injected base href has to name — is not something it reports either.
     *
     * <p>Cookies go both ways on every hop, redirects included: an OAuth2 login sets its session on
     * the hop that redirects to the identity provider and checks it on the one coming back.
     */
    static Fetched fetch(String target, String method, byte[] requestBody, Map<String, String> forwardedHeaders,
            List<String> headerLines, ProxyCookieJar cookies) throws IOException {
        String url = target;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            for (String header : headerLines) {
                int colon = header.indexOf(':');
                if (colon <= 0) continue;
                String name = header.substring(0, colon).trim();
                String value = header.substring(colon + 1).trim();
                if (name.isEmpty()) continue;
                try { conn.setRequestProperty(name, value); } catch (Exception ignored) { }
            }
            for (Map.Entry<String, String> header : forwardedHeaders.entrySet()) {
                if (conn.getRequestProperty(header.getKey()) == null) {
                    try { conn.setRequestProperty(header.getKey(), header.getValue()); } catch (Exception ignored) { }
                }
            }
            URI hopUri = toUri(url);
            String jarCookies = hopUri != null ? cookies.header(hopUri) : null;
            if (jarCookies != null) {
                String own = conn.getRequestProperty("Cookie");
                conn.setRequestProperty("Cookie", own == null ? jarCookies : own + "; " + jarCookies);
            }
            if (requestBody.length > 0) {
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(requestBody.length);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(requestBody);
                }
            }
            int code = conn.getResponseCode();
            if (hopUri != null) cookies.store(hopUri, setCookieHeaders(conn));
            if (code >= 300 && code < 400) {
                String location = conn.getHeaderField("Location");
                conn.disconnect();
                if (location == null) throw new IOException("redirect with no Location from " + url);
                url = new URL(new URL(url), location).toString();
                // Standard redirect semantics: 307/308 preserve method + body, everything else
                // (301/302/303) downgrades to a bodyless GET, same as a browser's own top-level
                // navigation would do. This proxy's caller is always a plain navigation or a
                // same-origin fetch/XHR the shim redirected here -- a write redirecting in
                // practice is not a case this needs to preserve the body across.
                if (code != 307 && code != 308) {
                    method = "GET";
                    requestBody = new byte[0];
                }
                continue;
            }
            InputStream in = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            try {
                byte[] body = in == null ? new byte[0] : Java8.readAllBytes(in);
                return new Fetched(url, code, body, conn.getContentType(), cacheHeaders(conn));
            } finally {
                if (in != null) in.close();
                conn.disconnect();
            }
        }
        throw new IOException("too many redirects for " + target);
    }

    private static List<String> setCookieHeaders(HttpURLConnection conn) {
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : conn.getHeaderFields().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase("Set-Cookie")) values.addAll(e.getValue());
        }
        return values;
    }

    private static URI toUri(String url) {
        try {
            return new URL(url).toURI();
        } catch (Exception e) {
            return null;
        }
    }

    /** Extracts a single URL-decoded query parameter from a raw query string. */
    private static String queryParam(String rawQuery, String name) {
        if (rawQuery == null) return null;
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return Java8.urlDecode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /** Extracts every URL-decoded occurrence of a repeated query parameter, in order. */
    private static List<String> queryParams(String rawQuery, String name) {
        List<String> values = new ArrayList<>();
        if (rawQuery == null) return values;
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                values.add(Java8.urlDecode(pair.substring(eq + 1), StandardCharsets.UTF_8));
            }
        }
        return values;
    }

    /**
     * Injects the {@code <base>} tag naming the target's real address, plus a small script pinning
     * it there. A served single-page app's own router (Angular's {@code PlatformLocation} does this)
     * routinely rewrites the {@code <base>} element's {@code href} at runtime to match the page's
     * own navigated address -- this document's {@code /proxy} URL, not the target it names. Anything
     * that resolves a relative reference against {@code document.baseURI} afterwards -- a
     * background-image set via an inline {@code style} attribute is the one this proxy's static and
     * runtime rewriting otherwise can't reach at all -- silently starts resolving against this
     * origin instead, and 404s (or worse, a same-origin resource that happens to exist at that path).
     * Re-pinning the attribute whenever it changes is simpler and more general than chasing down
     * every consumer of it.
     */
    private static String injectBaseHref(String html, String finalUrl) {
        String hrefLiteral = finalUrl.replace("\"", "%22");
        String baseTag = "<base href=\"" + hrefLiteral + "\">";
        String pinScript = "<script>(function(){"
                + "var TARGET_BASE_HREF=" + jsStringLiteral(finalUrl) + ";"
                + "function pin(el){try{if(el.getAttribute('href')!==TARGET_BASE_HREF){el.setAttribute('href',TARGET_BASE_HREF);}}catch(e){}}"
                + "var b=document.currentScript.previousElementSibling;"
                + "if(b&&b.tagName==='BASE'){pin(b);"
                + "try{new MutationObserver(function(){pin(b);}).observe(b,{attributes:true,attributeFilter:['href']});}catch(e){}}"
                + "})();</script>";
        return insertAfterHead(html, baseTag + pinScript);
    }

    private static final Pattern HEAD_TAG = Pattern.compile("<head(?:\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);

    /** {@code html} with {@code insertion} right after its {@code <head>} tag, or first when it has none. */
    private static String insertAfterHead(String html, String insertion) {
        Matcher m = HEAD_TAG.matcher(html);
        return m.find() ? html.substring(0, m.end()) + insertion + html.substring(m.end()) : insertion + html;
    }

    private static final Pattern RESOURCE_ATTR = Pattern.compile(
            "\\b(src|href)\\s*=\\s*([\"'])(?!(?:data:|mailto:|javascript:|#))([^\"']*)\\2",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern FORM_TAG = Pattern.compile("<form\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern POST_METHOD = Pattern.compile("\\smethod\\s*=\\s*([\"']?)post\\1[\\s>/]", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_ATTR = Pattern.compile(
            "(\\saction\\s*=\\s*)([\"'])(?!(?:javascript:|#))([^\"']*)\\2", Pattern.CASE_INSENSITIVE);

    private static final Pattern CHAR_REF = Pattern.compile("&(#[0-9]+|#[xX][0-9a-fA-F]+|amp|quot|apos|lt|gt);");

    /**
     * Rewrites {@code src=}/{@code href=} references that resolve to a proxyable http(s) URL so
     * they route through this same {@code /proxy} endpoint too, carrying the same auth headers as
     * the page itself. Without this, a page whose static assets sit behind the same auth gate as
     * the page (this proxy's whole reason to exist) fails to load its own scripts/stylesheets: the
     * browser fetches them directly, cross-origin, with no header — typically blocked by CORS
     * before the response is even read, and 403 from the target's own auth check regardless.
     *
     * <p>Best-effort only: this covers markup present in the initial response, not a resource URL
     * a script constructs and fetches at runtime. A single-page app making its own XHR/fetch calls
     * for data is unreachable this way — no static rewrite can anticipate a dynamically-built URL —
     * and needs the target's own cooperation (exempt those paths from the auth gate, or send CORS
     * headers) to fully work through a cross-origin proxy.
     */
    static String rewriteResourceUrls(String html, URI baseUri, List<String> headerLines, String selfOrigin) {
        Matcher m = RESOURCE_ATTR.matcher(html);
        // Matcher.appendReplacement/appendTail only gained a StringBuilder overload in Java 9;
        // this class is compiled down to Java 8 for older SWT releases, so StringBuffer it is.
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String replacement = m.group(0);
            String proxied = proxiedUrl(m.group(3), baseUri, headerLines, selfOrigin);
            if (proxied != null) replacement = m.group(1) + "=" + m.group(2) + proxied + m.group(2);
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    /**
     * Routes a POST form's {@code action} through this endpoint too. Submitted as written, a login
     * form posts the whole iframe to the target's own origin, which a site that forbids framing
     * refuses. A GET form is left alone: the browser replaces its action's query with the form's
     * fields, which would drop the {@code url=} this endpoint needs.
     */
    static String rewriteFormActions(String html, URI baseUri, List<String> headerLines, String selfOrigin) {
        Matcher form = FORM_TAG.matcher(html);
        StringBuffer out = new StringBuffer();
        while (form.find()) {
            String tag = form.group();
            if (POST_METHOD.matcher(tag).find()) {
                Matcher action = ACTION_ATTR.matcher(tag);
                if (action.find()) {
                    String proxied = proxiedUrl(action.group(3), baseUri, headerLines, selfOrigin);
                    if (proxied != null) {
                        tag = tag.substring(0, action.start()) + action.group(1) + action.group(2) + proxied
                                + action.group(2) + tag.substring(action.end());
                    }
                }
            }
            form.appendReplacement(out, Matcher.quoteReplacement(tag));
        }
        form.appendTail(out);
        return out.toString();
    }

    /** The {@code /proxy} URL serving {@code rawValue}, or null when it is not proxied. */
    private static String proxiedUrl(String rawValue, URI baseUri, List<String> headerLines, String selfOrigin) {
        // The value as the browser reads it: a literal &amp; in a query is a different URL.
        URI target = proxiableTarget(baseUri, decodeCharRefs(rawValue));
        return target == null ? null : queryFormUrl(selfOrigin, target, headerLines);
    }

    /** {@code value} resolved against {@code base} when it is an http(s) URL the proxy may serve, else null. */
    private static URI proxiableTarget(URI base, String value) {
        try {
            URI resolved = base.resolve(value);
            String scheme = resolved.getScheme();
            if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && WebFlutterServer.proxyAllowed(resolved.toString())) {
                return resolved;
            }
        } catch (Exception ignored) { }
        return null;
    }

    private static String queryFormUrl(String selfOrigin, URI target, List<String> headerLines) {
        StringBuilder proxied = new StringBuilder(selfOrigin).append("/proxy?url=")
                .append(Java8.urlEncode(target.toString(), StandardCharsets.UTF_8));
        for (String header : headerLines) {
            proxied.append("&header=").append(Java8.urlEncode(header, StandardCharsets.UTF_8));
        }
        return proxied.toString();
    }

    private static String pathFormUrl(String selfOrigin, String target) {
        return selfOrigin + PATH_FORM + target;
    }

    /**
     * The target of a path-form request, {@code /proxy/<absolute url>}. Modules are served this way
     * so that a module's own relative imports resolve against a URL that still mirrors the target's
     * path; the query-form wrapper would resolve them against this origin's root.
     */
    static String pathFormTarget(URI requestUri) {
        String path = requestUri.getRawPath();
        if (path == null || !path.startsWith(PATH_FORM)) return null;
        String target = path.substring(PATH_FORM.length());
        if (!target.startsWith("http://") && !target.startsWith("https://")) return null;
        String query = requestUri.getRawQuery();
        return query == null ? target : target + "?" + query;
    }

    private static final String PATH_FORM = "/proxy/";

    private static final Pattern MODULE_TAG = Pattern.compile(
            "<(?:script\\b[^>]*\\btype\\s*=\\s*[\"']?module\\b|link\\b[^>]*\\brel\\s*=\\s*[\"']?modulepreload\\b)[^>]*>",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern IMPORT_MAP = Pattern.compile(
            "(<script\\b[^>]*\\btype\\s*=\\s*[\"']?importmap\\b[^>]*>)(.*?)(</script>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** Switches the module scripts and preloads {@link #rewriteResourceUrls} proxied to the path form. */
    static String rewriteModuleScripts(String html, String selfOrigin) {
        String queryForm = selfOrigin + "/proxy?";
        Matcher tag = MODULE_TAG.matcher(html);
        StringBuffer out = new StringBuffer();
        while (tag.find()) {
            String rewritten = tag.group();
            Matcher attr = RESOURCE_ATTR.matcher(rewritten);
            if (attr.find() && attr.group(3).startsWith(queryForm)) {
                String target = queryParam(attr.group(3).substring(queryForm.length()), "url");
                if (target != null) {
                    String pathForm = pathFormUrl(selfOrigin, target).replace("&", "&amp;").replace("\"", "%22").replace("'", "%27");
                    rewritten = rewritten.substring(0, attr.start(3)) + pathForm + rewritten.substring(attr.end(3));
                }
            }
            tag.appendReplacement(out, Matcher.quoteReplacement(rewritten));
        }
        tag.appendTail(out);
        return out.toString();
    }

    private static final Pattern DYNAMIC_IMPORT = Pattern.compile("\\bimport\\s*\\(");

    /**
     * Routes the page's module imports through the path form. An {@code import} specifier in script
     * text is not markup {@link #rewriteResourceUrls} can see, and it resolves against the injected
     * {@code <base>}, straight to the target, where CORS blocks it. For the page's origin and every
     * other origin it loads modules from, the import map sends that origin to the path form, and a
     * scope sends a proxied module's root-relative imports there too; its relative ones already
     * resolve inside it. The page's own import maps get their addresses moved to the path form,
     * since an address an import map gives is not mapped again, and the first one carries these
     * entries. A map that does not parse is left as the browser will read it.
     */
    @SuppressWarnings("unchecked")
    static String injectModuleImportMap(String html, URI pageUri, String selfOrigin) {
        Matcher m = IMPORT_MAP.matcher(html);
        boolean hasMap = m.find();
        if (!hasMap && !MODULE_TAG.matcher(html).find() && !DYNAMIC_IMPORT.matcher(html).find()) return html;
        List<int[]> regions = new ArrayList<>();
        List<Map<String, Object>> maps = new ArrayList<>();
        if (hasMap) {
            do {
                Object parsed = EvalJson.parse(m.group(2));
                if (parsed instanceof Map) {
                    regions.add(new int[]{m.start(2), m.end(2)});
                    maps.add(proxiedImportMap((Map<String, Object>) parsed, pageUri, selfOrigin));
                }
            } while (m.find());
        }

        Set<String> origins = new LinkedHashSet<>();
        origins.add(originOf(pageUri));
        Matcher proxied = Pattern.compile(Pattern.quote(pathFormUrl(selfOrigin, "")) + "(https?://[^/\"'\\s?#&]+)").matcher(html);
        while (proxied.find()) origins.add(proxied.group(1));
        for (Map<String, Object> map : maps) collectProxiedOrigins(map, selfOrigin, origins);

        Map<String, Object> carrier = maps.isEmpty() ? new LinkedHashMap<String, Object>() : maps.get(0);
        addProxyEntries(carrier, origins, selfOrigin);
        if (maps.isEmpty()) return insertAfterHead(html, "<script type=\"importmap\">" + importMapJson(carrier) + "</script>");

        StringBuilder out = new StringBuilder();
        int pos = 0;
        for (int i = 0; i < maps.size(); i++) {
            out.append(html, pos, regions.get(i)[0]).append(importMapJson(maps.get(i)));
            pos = regions.get(i)[1];
        }
        return out.append(html.substring(pos)).toString();
    }

    private static String importMapJson(Map<String, Object> map) {
        return EvalJson.encode(map).replace("</", "<\\/");
    }

    /** The page's import map with every address, scope and integrity key the proxy serves in the path form. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> proxiedImportMap(Map<String, Object> map, URI pageUri, String selfOrigin) {
        Map<String, Object> out = new LinkedHashMap<>(map);
        out.put("imports", proxiedAddresses(map.get("imports"), pageUri, selfOrigin));
        Map<String, Object> scopes = new LinkedHashMap<>();
        if (map.get("scopes") instanceof Map) {
            for (Map.Entry<String, Object> scope : ((Map<String, Object>) map.get("scopes")).entrySet()) {
                scopes.put(proxiedAddress(scope.getKey(), pageUri, selfOrigin), proxiedAddresses(scope.getValue(), pageUri, selfOrigin));
            }
        }
        out.put("scopes", scopes);
        if (map.get("integrity") instanceof Map) {
            Map<String, Object> integrity = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : ((Map<String, Object>) map.get("integrity")).entrySet()) {
                integrity.put(proxiedAddress(e.getKey(), pageUri, selfOrigin), e.getValue());
            }
            out.put("integrity", integrity);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static void collectProxiedOrigins(Map<String, Object> map, String selfOrigin, Set<String> origins) {
        String prefix = pathFormUrl(selfOrigin, "");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            for (String s : new String[]{e.getKey(), e.getValue() instanceof String ? (String) e.getValue() : null}) {
                if (s == null || !s.startsWith(prefix)) continue;
                URI target = toUri(s.substring(prefix.length()));
                if (target != null && target.getHost() != null) origins.add(originOf(target));
            }
            if (e.getValue() instanceof Map) collectProxiedOrigins((Map<String, Object>) e.getValue(), selfOrigin, origins);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addProxyEntries(Map<String, Object> map, Set<String> origins, String selfOrigin) {
        Map<String, Object> imports = map.get("imports") instanceof Map
                ? (Map<String, Object>) map.get("imports") : new LinkedHashMap<String, Object>();
        Map<String, Object> scopes = map.get("scopes") instanceof Map
                ? (Map<String, Object>) map.get("scopes") : new LinkedHashMap<String, Object>();
        for (String origin : origins) {
            String root = pathFormUrl(selfOrigin, origin + "/");
            if (!imports.containsKey(origin + "/")) imports.put(origin + "/", root);
            Map<String, Object> inRoot = scopes.get(root) instanceof Map
                    ? (Map<String, Object>) scopes.get(root) : new LinkedHashMap<String, Object>();
            if (!inRoot.containsKey(selfOrigin + "/")) inRoot.put(selfOrigin + "/", root);
            // The longest prefix wins: a proxied module's relative imports, already in the path form,
            // match this identity entry and are left alone by the one above.
            if (!inRoot.containsKey(selfOrigin + PATH_FORM)) inRoot.put(selfOrigin + PATH_FORM, selfOrigin + PATH_FORM);
            scopes.put(root, inRoot);
        }
        map.put("imports", imports);
        map.put("scopes", scopes);
    }

    /** A specifier map with every address the proxy serves moved to the path form. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> proxiedAddresses(Object specifierMap, URI pageUri, String selfOrigin) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!(specifierMap instanceof Map)) return out;
        for (Map.Entry<String, Object> e : ((Map<String, Object>) specifierMap).entrySet()) {
            Object address = e.getValue();
            out.put(e.getKey(), address instanceof String ? proxiedAddress((String) address, pageUri, selfOrigin) : address);
        }
        return out;
    }

    private static String proxiedAddress(String address, URI pageUri, String selfOrigin) {
        boolean urlLike = address.startsWith("/") || address.startsWith("./") || address.startsWith("../")
                || address.startsWith("http://") || address.startsWith("https://");
        URI target = urlLike ? proxiableTarget(pageUri, address) : null;
        return target == null ? address : pathFormUrl(selfOrigin, target.toString());
    }

    /** Decodes the character references a URL in an attribute value can carry. */
    private static String decodeCharRefs(String value) {
        if (value.indexOf('&') < 0) return value;
        Matcher m = CHAR_REF.matcher(value);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String ref = m.group(1);
            String decoded;
            if (ref.startsWith("#x") || ref.startsWith("#X")) {
                decoded = new String(Character.toChars(Integer.parseInt(ref.substring(2), 16)));
            } else if (ref.startsWith("#")) {
                decoded = new String(Character.toChars(Integer.parseInt(ref.substring(1))));
            } else if (ref.equals("amp")) {
                decoded = "&";
            } else if (ref.equals("quot")) {
                decoded = "\"";
            } else if (ref.equals("apos")) {
                decoded = "'";
            } else if (ref.equals("lt")) {
                decoded = "<";
            } else {
                decoded = ">";
            }
            m.appendReplacement(out, Matcher.quoteReplacement(decoded));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static final Pattern CSS_URL = Pattern.compile(
            "url\\(\\s*(['\"]?)([^'\")]+)\\1\\s*\\)", Pattern.CASE_INSENSITIVE);

    /** Same idea as {@link #rewriteResourceUrls}, for a stylesheet's own {@code url(...)} references. */
    private static String rewriteCssUrls(String css, URI baseUri, List<String> headerLines, String selfOrigin) {
        Matcher m = CSS_URL.matcher(css);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String replacement = m.group(0);
            String value = m.group(2).trim();
            URI target = value.startsWith("data:") ? null : proxiableTarget(baseUri, value);
            if (target != null) replacement = "url(\"" + queryFormUrl(selfOrigin, target, headerLines) + "\")";
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String originOf(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    /**
     * Routes the page's own script-driven navigations back through this endpoint. Markup rewriting
     * only sees URLs written in an attribute; a {@code location.href = url} built in script (a
     * language picker's {@code onchange}, say) takes the iframe straight to the target, which a site
     * that forbids framing refuses. Uses the Navigation API: where a browser lacks it, those
     * navigations behave as before. Only navigations to the proxied page's own origin are rerouted,
     * and POST submissions are left to {@link #rewriteFormActions}.
     */
    static String injectNavigationShim(String html, String targetOrigin, List<String> headerLines, String selfOrigin) {
        StringBuilder headersJs = new StringBuilder("[");
        for (int i = 0; i < headerLines.size(); i++) {
            if (i > 0) headersJs.append(",");
            headersJs.append(jsStringLiteral(headerLines.get(i)));
        }
        headersJs.append("]");
        String script = "<script>(function(){"
                + "var nav=window.navigation;if(!nav)return;"
                + "var TARGET_ORIGIN;try{TARGET_ORIGIN=new URL(" + jsStringLiteral(targetOrigin) + ").origin;}catch(e){return;}"
                + "var SELF_ORIGIN=" + jsStringLiteral(selfOrigin) + ";"
                + "var HEADERS=" + headersJs + ";"
                + "nav.addEventListener('navigate',function(e){"
                + "try{if(!e.cancelable||e.hashChange||e.downloadRequest||e.formData)return;"
                + "var dest=new URL(e.destination.url);if(dest.origin!==TARGET_ORIGIN)return;"
                + "var u=SELF_ORIGIN+'/proxy?url='+encodeURIComponent(dest.href);"
                + "for(var i=0;i<HEADERS.length;i++){u+='&header='+encodeURIComponent(HEADERS[i]);}"
                + "e.preventDefault();location.href=u;}catch(err){}"
                + "});})();</script>";
        return insertAfterHead(html, script);
    }

    /**
     * Injects a same-origin {@code fetch}/{@code XMLHttpRequest} shim as the very first script in
     * {@code <head>}, so it patches those globals before any of the page's own scripts run. Static
     * markup rewriting ({@link #rewriteResourceUrls}) only covers {@code src=}/{@code href=}
     * references present in this response; a single-page app's own runtime API calls — the actual
     * point of a grid-editor's own auth gate — are URLs it builds and fetches in JavaScript, which
     * no static rewrite can see in advance. This patches the two ways browser JS can make an HTTP
     * request: any call whose resolved URL's origin matches the proxied target is transparently
     * redirected through this same {@code /proxy} endpoint (same-origin, so no CORS problem) with
     * the same auth headers embedded directly — no dependency on the server-side per-target cache
     * having already seen that exact path.
     *
     * <p>Still incomplete by nature: a WebSocket connection, a request built from a string this
     * shim can't recognize as absolute before the browser resolves it, or a request signed/shaped
     * in a way the target only accepts from its own real origin remain out of reach.
     */
    private static String injectFetchShim(String html, String targetOrigin, List<String> headerLines, String selfOrigin) {
        StringBuilder headersJs = new StringBuilder("[");
        for (int i = 0; i < headerLines.size(); i++) {
            if (i > 0) headersJs.append(",");
            headersJs.append(jsStringLiteral(headerLines.get(i)));
        }
        headersJs.append("]");
        String script = "<script>(function(){"
                + "var TARGET_ORIGIN=" + jsStringLiteral(targetOrigin) + ";"
                + "var SELF_ORIGIN=" + jsStringLiteral(selfOrigin) + ";"
                + "var AUTH_HEADERS=" + headersJs + ";"
                + "function buildProxyUrl(abs){"
                + "var u=SELF_ORIGIN+'/proxy?url='+encodeURIComponent(abs);"
                + "for(var i=0;i<AUTH_HEADERS.length;i++){u+='&header='+encodeURIComponent(AUTH_HEADERS[i]);}"
                + "return u;}"
                // Resolve against the TARGET origin, not document.baseURI: a served single-page
                // app routinely manages its own <base>/location state at runtime (Angular's
                // PlatformLocation, in particular, normalizes it against the *served* document's
                // own location -- this origin, not the proxied target), so by the time a relative
                // API path reaches fetch/XHR it can no longer be trusted to resolve against the
                // <base> tag this proxy injected. Try it as already-absolute first; only a truly
                // relative string falls back to the target origin.
                + "function resolveAgainstTarget(url){"
                + "try{return new URL(url).href;}catch(e){}"
                + "try{return new URL(url,TARGET_ORIGIN+'/').href;}catch(e){return null;}}"
                + "function sameTargetOrigin(abs){try{return new URL(abs).origin===TARGET_ORIGIN;}catch(e){return false;}}"
                + "var origFetch=window.fetch;"
                + "if(origFetch){window.fetch=function(input,init){"
                + "try{var url=typeof input==='string'?input:(input&&input.url);"
                + "if(url){var abs=resolveAgainstTarget(url);"
                + "if(abs&&sameTargetOrigin(abs)){var proxied=buildProxyUrl(abs);"
                + "input=typeof input==='string'?proxied:new Request(proxied,input);}}"
                + "}catch(e){}"
                + "return origFetch.call(this,input,init);};}"
                + "var origOpen=XMLHttpRequest.prototype.open;"
                + "XMLHttpRequest.prototype.open=function(method,url){"
                + "try{var abs=resolveAgainstTarget(url);"
                + "if(abs&&sameTargetOrigin(abs)){arguments[1]=buildProxyUrl(abs);}"
                + "}catch(e){}"
                + "return origOpen.apply(this,arguments);};"
                // A resource element (an <img> built from JSON data after load, say) the page adds
                // or repoints via straight DOM/property writes never touches fetch or XHR at all --
                // the browser loads it natively. Its src/href, built relative to this document,
                // resolves through the injected <base> to the *target*'s real address and goes
                // there directly, missing the auth header only this shim carries. Watching the DOM
                // for exactly that (a src/href attribute, present or newly set, pointing at the
                // target origin) and rewriting it the same way the initial-response markup already
                // is closes that gap without needing to know in advance how the page builds it.
                + "var REWRITE_ATTRS=['src','href'];"
                + "function shouldSkipAttr(raw){"
                + "if(!raw)return true;var t=raw.trim();if(t==='')return true;"
                + "if(t.charAt(0)==='#')return true;"
                + "var low=t.toLowerCase();"
                + "return low.indexOf('data:')===0||low.indexOf('mailto:')===0||low.indexOf('javascript:')===0;}"
                + "function rewriteAttr(el,attr){"
                // The <base> tag itself is excluded: its href is this document's base URL, not a
                // fetchable resource, and injectBaseHref's own script pins it to the target's real
                // address directly. Treating it as a rewritable reference here would proxy-wrap it
                // (since it always resolves same-origin-as-target by construction), which the pin
                // script then reverts, which this observer then rewrites again -- an infinite loop
                // between the two that hangs the tab.
                + "if(el.tagName==='BASE')return;"
                + "try{var raw=el.getAttribute&&el.getAttribute(attr);"
                + "if(shouldSkipAttr(raw))return;"
                + "var abs=resolveAgainstTarget(raw);"
                // Rewriting to our own proxy URL fires another 'attributes' mutation for this same
                // attribute; that pass resolves to SELF_ORIGIN, not TARGET_ORIGIN, so this check
                // is also what stops the observer from rewriting its own output forever.
                + "if(abs&&sameTargetOrigin(abs)){el.setAttribute(attr,buildProxyUrl(abs));}"
                + "}catch(e){}}"
                // An icon set as a CSS background-image via an inline style attribute (this app's
                // own grid rows, built from JSON after load) is neither src= nor href= -- src/href
                // rewriting above never sees it, and it's not a stylesheet file either, so the
                // static CSS rewrite doesn't either. It resolves through the pinned <base> straight
                // to the target's real address and goes there directly, unauthenticated.
                + "function rewriteBackgroundImage(el){"
                + "try{var bg=el.style&&el.style.backgroundImage;"
                + "if(!bg||bg==='none')return;"
                + "var m=/url\\((['\"]?)(.*?)\\1\\)/.exec(bg);"
                + "if(!m)return;var raw=m[2];"
                + "if(shouldSkipAttr(raw)||raw.indexOf(SELF_ORIGIN)===0)return;"
                + "var abs=resolveAgainstTarget(raw);"
                + "if(abs&&sameTargetOrigin(abs)){el.style.backgroundImage='url(\"'+buildProxyUrl(abs)+'\")';}"
                + "}catch(e){}}"
                + "function scanNode(node){"
                + "if(!node||node.nodeType!==1)return;"
                + "for(var i=0;i<REWRITE_ATTRS.length;i++)rewriteAttr(node,REWRITE_ATTRS[i]);"
                + "rewriteBackgroundImage(node);"
                + "if(node.querySelectorAll){"
                + "var els=node.querySelectorAll('[src],[href],[style]');"
                + "for(var j=0;j<els.length;j++){"
                + "for(var k=0;k<REWRITE_ATTRS.length;k++)rewriteAttr(els[j],REWRITE_ATTRS[k]);"
                + "rewriteBackgroundImage(els[j]);"
                + "}}}"
                + "try{new MutationObserver(function(records){"
                + "for(var i=0;i<records.length;i++){var r=records[i];"
                + "if(r.type==='attributes'){"
                + "if(r.attributeName==='style'){rewriteBackgroundImage(r.target);}else{rewriteAttr(r.target,r.attributeName);}"
                + "}else if(r.addedNodes){for(var j=0;j<r.addedNodes.length;j++)scanNode(r.addedNodes[j]);}}"
                + "}).observe(document.documentElement,{childList:true,subtree:true,attributes:true,attributeFilter:['src','href','style']});"
                + "}catch(e){}"
                + "})();</script>";
        return insertAfterHead(html, script);
    }

    private static String jsStringLiteral(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '<': out.append("\\u003C"); break;
                default: out.append(c);
            }
        }
        return out.append('"').toString();
    }

    /** Refusals need the isolation headers too, or the frame shows a browser error instead of why. */
    private static void sendPlain(HttpExchange exchange, int code, String msg) throws IOException {
        byte[] b = msg.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        WebFlutterServer.StaticFileHandler.setCrossOriginHeaders(exchange);
        exchange.sendResponseHeaders(code, b.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(b); }
    }
}

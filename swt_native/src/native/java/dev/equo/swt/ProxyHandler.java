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
import java.util.List;
import java.util.Map;
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
    private static final Map<String, List<String>> TARGET_AUTH_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<String, List<String>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
                    return size() > TARGET_AUTH_CACHE_MAX;
                }
            });

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String target = queryParam(exchange.getRequestURI().getRawQuery(), "url");
            if (target == null || !WebFlutterServer.proxyAllowed(target)) { sendPlain(exchange, 403, "url not allowed"); return; }

            // Headers from Browser.setUrl(url, postData, headers): an iframe navigation can't
            // carry custom HTTP headers at all (a web-platform limit, not an Equo one), so a
            // caller that gates a GET on a header (an auth token an embedded local server
            // expects, say) is unreachable through a plain src= load. The Dart side forwards
            // each "Name: value" line here (one repeated "header" param) and we attach them to
            // the server-side fetch instead. A header HttpURLConnection itself rejects (Host,
            // Content-Length, …) is skipped rather than failing the whole proxy.
            List<String> headerLines = queryParams(exchange.getRequestURI().getRawQuery(), "header");
            if (!headerLines.isEmpty()) {
                TARGET_AUTH_CACHE.put(target, headerLines);
            } else {
                List<String> remembered = TARGET_AUTH_CACHE.get(target);
                if (remembered == null) {
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
            // Content-Type from the page's own fetch/XHR call, so a JSON PUT/POST body is
            // interpreted correctly upstream. Deliberately narrow rather than forwarding every
            // inbound header: a browser sends its own Accept/Accept-Encoding/conditional-cache
            // headers on every request, including the plain navigations that never carry a
            // postData body at all, and blindly relaying those upstream is a wider behavior
            // change than this proxy needs to make to support writes.
            String requestContentType = exchange.getRequestHeaders().getFirst("Content-Type");

            Fetched resp = fetch(target, method, requestBody, requestContentType, headerLines);
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
                html = injectBaseHref(html, resp.finalUrl);
                if (!headerLines.isEmpty()) {
                    html = injectFetchShim(html, originOf(finalUri), headerLines, selfOrigin);
                }
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
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
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
    private static final class Fetched {
        final String finalUrl;
        final int statusCode;
        final byte[] body;
        final String contentType;

        Fetched(String finalUrl, int statusCode, byte[] body, String contentType) {
            this.finalUrl = finalUrl;
            this.statusCode = statusCode;
            this.body = body;
            this.contentType = contentType;
        }
    }

    /**
     * Follows redirects itself: {@link HttpURLConnection} will not follow one that changes
     * protocol (the http -> https hop most sites open with), and the URL the response ended on —
     * which the injected base href has to name — is not something it reports either.
     */
    private static Fetched fetch(String target, String method, byte[] requestBody,
            String requestContentType, List<String> headerLines) throws IOException {
        String url = target;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);
            if (requestContentType != null) {
                try { conn.setRequestProperty("Content-Type", requestContentType); } catch (Exception ignored) { }
            }
            for (String header : headerLines) {
                int colon = header.indexOf(':');
                if (colon <= 0) continue;
                String name = header.substring(0, colon).trim();
                String value = header.substring(colon + 1).trim();
                if (name.isEmpty()) continue;
                try { conn.setRequestProperty(name, value); } catch (Exception ignored) { }
            }
            if (requestBody.length > 0) {
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(requestBody.length);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(requestBody);
                }
            }
            int code = conn.getResponseCode();
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
                return new Fetched(url, code, body, conn.getContentType());
            } finally {
                if (in != null) in.close();
                conn.disconnect();
            }
        }
        throw new IOException("too many redirects for " + target);
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
        String insertion = baseTag + pinScript;
        Matcher m = Pattern.compile("<head[^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
        return m.find() ? html.substring(0, m.end()) + insertion + html.substring(m.end())
                        : insertion + html;
    }

    private static final Pattern RESOURCE_ATTR = Pattern.compile(
            "\\b(src|href)\\s*=\\s*([\"'])(?!(?:data:|mailto:|javascript:|#))([^\"']*)\\2",
            Pattern.CASE_INSENSITIVE);

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
    private static String rewriteResourceUrls(String html, URI baseUri, List<String> headerLines, String selfOrigin) {
        Matcher m = RESOURCE_ATTR.matcher(html);
        // Matcher.appendReplacement/appendTail only gained a StringBuilder overload in Java 9;
        // this class is compiled down to Java 8 for older SWT releases, so StringBuffer it is.
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String replacement = m.group(0);
            try {
                URI resolved = baseUri.resolve(m.group(3));
                String scheme = resolved.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                        && WebFlutterServer.proxyAllowed(resolved.toString())) {
                    StringBuilder proxied = new StringBuilder(selfOrigin).append("/proxy?url=")
                            .append(Java8.urlEncode(resolved.toString(), StandardCharsets.UTF_8));
                    for (String header : headerLines) {
                        proxied.append("&header=").append(Java8.urlEncode(header, StandardCharsets.UTF_8));
                    }
                    replacement = m.group(1) + "=" + m.group(2) + proxied + m.group(2);
                }
            } catch (Exception ignored) { }
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
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
            try {
                String value = m.group(2).trim();
                if (!value.startsWith("data:")) {
                    URI resolved = baseUri.resolve(value);
                    String scheme = resolved.getScheme();
                    if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                            && WebFlutterServer.proxyAllowed(resolved.toString())) {
                        StringBuilder proxied = new StringBuilder(selfOrigin).append("/proxy?url=")
                                .append(Java8.urlEncode(resolved.toString(), StandardCharsets.UTF_8));
                        for (String header : headerLines) {
                            proxied.append("&header=").append(Java8.urlEncode(header, StandardCharsets.UTF_8));
                        }
                        replacement = "url(\"" + proxied + "\")";
                    }
                }
            } catch (Exception ignored) { }
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String originOf(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority();
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
        Matcher m = Pattern.compile("<head[^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
        return m.find() ? html.substring(0, m.end()) + script + html.substring(m.end())
                        : script + html;
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

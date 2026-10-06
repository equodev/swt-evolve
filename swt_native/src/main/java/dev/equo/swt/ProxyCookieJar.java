package dev.equo.swt;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The cookies of the pages the web backend's {@code /proxy} serves. A proxied page reaches the
 * browser from this app's own origin, so the target's cookies can never live in the browser: they
 * are kept here, server-side, sent on every proxied request (each redirect hop included) and
 * answered to the static {@code Browser.getCookie}/{@code setCookie}. Without it a login that
 * spans redirects (an OAuth2 client sending the browser to its identity provider and back) loses
 * its session between hops.
 *
 * <p>One jar per web server, so per Display: sessions never share cookies. RFC 6265 rules for
 * domain, path, expiry and {@code Secure}; values are kept exactly as the server sent them. There
 * is no public suffix list: a single-label {@code Domain} is refused, and a cookie only ever goes
 * to hosts the proxy is allowed to serve.
 */
public final class ProxyCookieJar {

    /** Past this, the oldest cookie goes, as browsers cap theirs. */
    private static final int MAX_COOKIES = 300;

    private static volatile Function<Object, ProxyCookieJar> displayLookup = d -> null;

    /** Registered by the web backend: the jar of a Display's web server, or null off the web. */
    public static void registerDisplayLookup(Function<Object, ProxyCookieJar> fn) {
        if (fn != null) displayLookup = fn;
    }

    /** The jar serving {@code display}'s proxied pages, or null when it is not a web Display. */
    public static ProxyCookieJar forDisplay(Object display) {
        return display != null ? displayLookup.apply(display) : null;
    }

    private static final class Cookie {
        final String name;
        final String value;
        final String domain;
        final boolean hostOnly;
        final String path;
        final boolean secure;
        final long expiresAt;

        Cookie(String name, String value, String domain, boolean hostOnly, String path, boolean secure, long expiresAt) {
            this.name = name;
            this.value = value;
            this.domain = domain;
            this.hostOnly = hostOnly;
            this.path = path;
            this.secure = secure;
            this.expiresAt = expiresAt;
        }

        boolean matches(String host, String requestPath, boolean https) {
            if (secure && !https) return false;
            boolean domainOk = hostOnly ? host.equals(domain) : domainMatches(host, domain);
            return domainOk && pathMatches(requestPath, path);
        }
    }

    private final Predicate<String> applies;
    private final List<Cookie> cookies = new ArrayList<>();

    /** @param applies whether a URL is one this jar serves: the proxy's own authorization check */
    public ProxyCookieJar(Predicate<String> applies) {
        this.applies = applies;
    }

    /** Records the {@code Set-Cookie} values a response to {@code uri} carried. */
    public synchronized void store(URI uri, List<String> setCookieHeaders) {
        if (setCookieHeaders == null) return;
        for (String header : setCookieHeaders) {
            add(uri, header);
        }
    }

    /** The {@code Cookie} request header for {@code uri}, or null when nothing applies. */
    public synchronized String header(URI uri) {
        List<Cookie> matching = matching(uri);
        if (matching.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (Cookie c : matching) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(c.name).append('=').append(c.value);
        }
        return sb.toString();
    }

    /** {@code Browser.getCookie}: the value of {@code name} for {@code url}, or null. */
    public synchronized String get(String name, String url) {
        URI uri = toUri(url);
        if (uri == null || !applies.test(url)) return null;
        for (Cookie c : matching(uri)) {
            if (c.name.equals(name)) return c.value;
        }
        return null;
    }

    /** {@code Browser.setCookie}: false when {@code url} is not one the proxy serves. */
    public synchronized boolean set(String value, String url) {
        URI uri = toUri(url);
        if (uri == null || !applies.test(url)) return false;
        return add(uri, value);
    }

    /** Stores one {@code Set-Cookie} value, replacing its namesake; false when it is refused. */
    private boolean add(URI uri, String header) {
        Cookie parsed = parse(header, uri);
        if (parsed == null) return false;
        for (Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
            Cookie c = it.next();
            if (c.name.equals(parsed.name) && c.domain.equals(parsed.domain) && c.path.equals(parsed.path)) {
                it.remove();
            }
        }
        if (parsed.expiresAt > System.currentTimeMillis()) cookies.add(parsed);
        while (cookies.size() > MAX_COOKIES) cookies.remove(0);
        return true;
    }

    private List<Cookie> matching(URI uri) {
        List<Cookie> out = new ArrayList<>();
        String host = uri.getHost();
        if (host == null) return out;
        host = host.toLowerCase(Locale.ROOT);
        String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
        boolean https = "https".equalsIgnoreCase(uri.getScheme());
        long now = System.currentTimeMillis();
        for (Iterator<Cookie> it = cookies.iterator(); it.hasNext(); ) {
            Cookie c = it.next();
            if (c.expiresAt <= now) {
                it.remove();
            } else if (c.matches(host, path, https)) {
                out.add(c);
            }
        }
        // Longest path first, as browsers send them; stable for equal lengths.
        out.sort((a, b) -> b.path.length() - a.path.length());
        return out;
    }

    private static Cookie parse(String header, URI uri) {
        if (header == null) return null;
        String host = uri.getHost();
        if (host == null) return null;
        host = host.toLowerCase(Locale.ROOT);
        String[] parts = header.split(";");
        int eq = parts[0].indexOf('=');
        if (eq <= 0) return null;
        String name = parts[0].substring(0, eq).trim();
        String value = parts[0].substring(eq + 1).trim();
        if (name.isEmpty()) return null;

        String domain = host;
        boolean hostOnly = true;
        String path = null;
        boolean secure = false;
        Long maxAgeAt = null;
        Long expiresAt = null;
        for (int i = 1; i < parts.length; i++) {
            String attr = parts[i].trim();
            int aeq = attr.indexOf('=');
            String key = (aeq < 0 ? attr : attr.substring(0, aeq)).trim().toLowerCase(Locale.ROOT);
            String val = aeq < 0 ? "" : attr.substring(aeq + 1).trim();
            switch (key) {
                case "domain": {
                    String d = val.startsWith(".") ? val.substring(1) : val;
                    d = d.toLowerCase(Locale.ROOT);
                    if (d.isEmpty()) break;
                    if (d.indexOf('.') < 0 && !d.equals(host)) return null;
                    if (!domainMatches(host, d)) return null;
                    domain = d;
                    hostOnly = false;
                    break;
                }
                case "path":
                    if (val.startsWith("/")) path = val;
                    break;
                case "secure":
                    secure = true;
                    break;
                case "max-age":
                    try {
                        long seconds = Long.parseLong(val);
                        maxAgeAt = seconds <= 0 ? Long.MIN_VALUE : System.currentTimeMillis() + seconds * 1000L;
                    } catch (NumberFormatException ignored) { }
                    break;
                case "expires":
                    expiresAt = parseExpires(val);
                    break;
                default:
                    break;
            }
        }
        if (path == null) path = defaultPath(uri.getRawPath());
        long expiry = maxAgeAt != null ? maxAgeAt : expiresAt != null ? expiresAt : Long.MAX_VALUE;
        return new Cookie(name, value, domain, hostOnly, path, secure, expiry);
    }

    private static Long parseExpires(String val) {
        String[] candidates = {val, val.replace('-', ' ')};
        for (String candidate : candidates) {
            try {
                return ZonedDateTime.parse(candidate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli();
            } catch (Exception ignored) { }
        }
        return null;
    }

    private static String defaultPath(String requestPath) {
        if (requestPath == null || !requestPath.startsWith("/")) return "/";
        int last = requestPath.lastIndexOf('/');
        return last <= 0 ? "/" : requestPath.substring(0, last);
    }

    static boolean domainMatches(String host, String domain) {
        return host.equals(domain) || (host.endsWith("." + domain) && !host.matches("[0-9.]+|\\[.*\\]"));
    }

    static boolean pathMatches(String requestPath, String cookiePath) {
        if (requestPath.equals(cookiePath)) return true;
        if (!requestPath.startsWith(cookiePath)) return false;
        return cookiePath.endsWith("/") || requestPath.charAt(cookiePath.length()) == '/';
    }

    private static URI toUri(String url) {
        try {
            return url == null ? null : URI.create(url);
        } catch (Exception e) {
            return null;
        }
    }
}

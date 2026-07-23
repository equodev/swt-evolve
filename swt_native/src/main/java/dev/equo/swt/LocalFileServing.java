package dev.equo.swt;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lets a web-target Browser display a {@code file://} URL by re-serving it (and any
 * sibling resources it references, by a relative or a root-absolute path) same-origin
 * through {@link WebFlutterServer}'s {@code /local-file/<token>/...} endpoint.
 * <p>
 * Native SWT's Browser is a real OS webview and loads {@code file://} URLs
 * directly; the web-target Browser is an {@code <iframe>}, and browsers refuse
 * to display {@code file://} content framed by a non-{@code file://} page (it
 * silently renders blank, even though the SWT-side navigation events still
 * fire). Routing it through this same-origin endpoint restores parity,
 * including {@code execute}/{@code evaluate}/{@code BrowserFunction}, which all
 * require same-origin content.
 * <p>
 * Only a directory a real {@code Browser.setUrl("file:...")} call itself named
 * becomes servable: {@link #registerIfLocalFile} registers just the file's own
 * parent directory, so this never exposes more than the app already asked to
 * display — the same trust boundary crossed by calling setUrl with that path in
 * the first place, not a new one.
 */
public final class LocalFileServing {

    /** URL path prefix under which {@link #registerIfLocalFile} registrations are served. */
    public static final String URL_PREFIX = "/local-file/";

    private static final Map<String, File> rootsByToken = new ConcurrentHashMap<>();
    private static final Map<String, String> tokensByPath = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private LocalFileServing() {
    }

    /** The token + file-relative-to-its-directory pair Dart appends to {@code /local-file/}. */
    public static final class Served {
        public final String token;
        public final String relativePath;

        Served(String token, String relativePath) {
            this.token = token;
            this.relativePath = relativePath;
        }

        /** {@code "<token>/<relativePath>"}, ready to append to the endpoint's base path. */
        public String tokenPath() {
            return token + "/" + relativePath;
        }
    }

    /**
     * If {@code url} is a {@code file:} URL, registers its parent directory
     * (reusing the same token for repeat navigations to that directory) and
     * returns the token + filename Dart should request instead. Returns
     * {@code null} for any other scheme, or if the URL can't be resolved to an
     * existing file — callers fall back to the normal navigate path.
     */
    public static Served registerIfLocalFile(String url) {
        if (url == null || !url.regionMatches(true, 0, "file:", 0, 5)) return null;
        Path filePath = parseFileUrl(url);
        if (filePath == null) return null;
        File file = filePath.toFile();
        if (!file.isFile()) return null;
        File parent = file.getParentFile();
        if (parent == null) return null;
        try {
            parent = parent.getCanonicalFile();
        } catch (Exception ignored) {
            // Fall back to the non-canonicalized form; resolve() still guards traversal.
        }
        String canonicalKey = parent.getPath();
        String token = tokensByPath.computeIfAbsent(canonicalKey, p -> newToken());
        rootsByToken.put(token, parent);
        return new Served(token, file.getName());
    }

    /**
     * A 128-bit random token, not a sequential id: the endpoint this backs is
     * mounted unconditionally (not gated behind an opt-in flag) and reflects
     * same-origin CORS, so a guessable/enumerable token would let any local
     * process — or, combined with a CORS misconfiguration, any web page — probe
     * small integers and read files back out of a registered directory.
     */
    private static String newToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Resolves {@code token}/{@code relativePath} back to a file, verifying the
     * result stays inside the directory registered for that token (blocks
     * {@code ../} traversal past the app-requested directory). Returns
     * {@code null} if the token is unknown, the path escapes the root, or the
     * resolved file doesn't exist.
     */
    public static File resolve(String token, String relativePath) {
        File root = rootsByToken.get(token);
        if (root == null) return null;
        Path rootPath = root.toPath().normalize();
        Path resolved = rootPath.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootPath)) return null;
        File file = resolved.toFile();
        return file.isFile() ? file : null;
    }

    /**
     * Extracts the registration token from a {@code Referer} header pointing at a
     * {@code /local-file/<token>/...} page, or {@code null} if it isn't one. Lets a caller
     * resolve a root-absolute sub-resource request against the same registered directory as
     * the page that requested it — see {@link #resolve}.
     */
    public static String tokenFromLocalFileReferer(String referer) {
        if (referer == null) return null;
        int start = referer.indexOf(URL_PREFIX);
        if (start < 0) return null;
        String rest = referer.substring(start + URL_PREFIX.length());
        int slash = rest.indexOf('/');
        return slash > 0 ? rest.substring(0, slash) : null;
    }

    /**
     * Parses a {@code file:} URL into a {@link Path}, tolerating the
     * not-strictly-RFC-3986-compliant forms real tools produce in practice —
     * notably Eclipse's {@code FileLocator.toFileURL()}, which emits a single
     * slash ({@code file:/Users/...}) and frequently leaves spaces unescaped.
     * {@code java.net.URI}'s strict parser rejects both, so this works on the
     * raw string instead: it collapses {@code file:} plus any number of
     * following slashes down to one, then tries the result first literally and,
     * if that path doesn't exist, percent-decoded.
     * <p>
     * Also strips a trailing {@code #fragment} or {@code ?query} before touching the
     * filesystem, so a hash-router single-page app's URL still resolves to its file.
     */
    private static Path parseFileUrl(String url) {
        String rest = url.substring("file:".length());
        int end = rest.length();
        int hash = rest.indexOf('#');
        if (hash >= 0 && hash < end) end = hash;
        int query = rest.indexOf('?');
        if (query >= 0 && query < end) end = query;
        rest = rest.substring(0, end);
        int i = 0;
        while (i < rest.length() && rest.charAt(i) == '/') i++;
        String rawPath = "/" + rest.substring(i);
        // A Windows drive-letter path ("file:/C:/foo") collapses to "/C:/foo"
        // above; strip the extra leading slash so it reads as "C:/foo".
        if (rawPath.length() > 2 && rawPath.charAt(2) == ':') {
            rawPath = rawPath.substring(1);
        }
        File direct = new File(rawPath);
        if (direct.exists()) return direct.toPath();
        try {
            File decoded = new File(java.net.URLDecoder.decode(rawPath, StandardCharsets.UTF_8));
            if (decoded.exists()) return decoded.toPath();
        } catch (Exception ignored) {
            // Not percent-encoded (or not validly so); fall through to the literal path.
        }
        return direct.toPath();
    }
}

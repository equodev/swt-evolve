package dev.equo.swt;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * {@link #rewriteLocalResources} does the same for the {@code file:} URLs inside
 * a {@code Browser.setText} document, which a {@code data:} URL cannot load at all.
 * <p>
 * Only a directory the application itself named — by passing it to
 * {@code setUrl("file:...")}, or by embedding it in HTML it asked us to render —
 * becomes servable, so this never exposes more than the app already asked to
 * display: the same trust boundary crossed by making that call in the first
 * place, not a new one.
 */
public final class LocalFileServing {

    /** URL path prefix under which {@link #registerIfLocalFile} registrations are served. */
    public static final String URL_PREFIX = "/local-file/";

    private static final Map<String, File> rootsByToken = new ConcurrentHashMap<>();
    private static final Map<String, String> tokensByPath = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Secondary root per token, for pages whose {@code <base href>} points at a directory
     * other than the file's own (e.g. a temp copy pointing back at its install directory).
     * See {@link #resolve}.
     */
    private static final Map<String, File> baseHrefRootsByToken = new ConcurrentHashMap<>();

    /** Matches an HTML {@code <base href="...">} tag; only the first hit (in the head) matters. */
    private static final Pattern BASE_HREF_PATTERN =
            Pattern.compile("<base\\s+[^>]*href\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    /** Only worth scanning for a {@code <base>} tag in the head, so cap the read. */
    private static final int BASE_HREF_SCAN_LIMIT = 32 * 1024;

    /**
     * Matches a {@code file:} URL as it appears in markup — an attribute value, a CSS
     * {@code url(...)} argument — by running to the first character none of those can contain.
     */
    private static final Pattern FILE_URL_IN_MARKUP = Pattern.compile("file:[^\"'\\s>)]+");

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

    /** A {@code Browser.setText} document rewritten by {@link #rewriteLocalResources}. */
    public static final class ServedHtml {
        /** The HTML, with every {@code file:} URL replaced by its {@code /local-file/} path. */
        public final String html;
        /**
         * The path the document's own {@code <base href>} was rewritten to, or {@code null} if it
         * declared none. Those {@code /local-file/} paths are root-absolute, so the caller has to
         * resolve them against an origin; doing that with this as the base keeps the document's
         * relative URLs resolving where the application meant them to.
         */
        public final String basePath;

        ServedHtml(String html, String basePath) {
            this.html = html;
            this.basePath = basePath;
        }
    }

    /**
     * If {@code url} is a {@code file:} URL, registers its parent directory
     * (reusing the same token for repeat navigations to that directory) and
     * returns the token + filename Dart should request instead. Returns
     * {@code null} for any other scheme, or for a URL that names no path at all —
     * callers fall back to the normal navigate path.
     * <p>
     * The file is deliberately <em>not</em> required to exist yet. A real webview
     * resolves a {@code file:} URL when it loads, so an application may navigate to
     * a template it writes moments later, and does. Refusing to register then would
     * leave the {@code <iframe>} pointing at the raw {@code file:} URL for good —
     * which a browser refuses to load at all ("Not allowed to load local resource"),
     * rendering blank however long the file has existed by the time it is fetched.
     * {@link #resolve} re-checks existence when the request actually arrives, so a
     * path that never appears yields an ordinary 404 instead.
     */
    public static Served registerIfLocalFile(String url) {
        if (url == null || !url.regionMatches(true, 0, "file:", 0, 5)) return null;
        Path filePath = parseFileUrl(url);
        if (filePath == null) return null;
        File file = filePath.toFile();
        if (file.getName().isEmpty() || file.isDirectory()) return null;
        File parent = file.getParentFile();
        if (parent == null) return null;
        String token = registerRoot(parent);
        registerBaseHrefRoot(token, file);
        return new Served(token, file.getName());
    }

    /** Registers {@code dir} as a servable root, reusing the token a previous registration made. */
    private static String registerRoot(File dir) {
        try {
            dir = dir.getCanonicalFile();
        } catch (Exception ignored) {
            // Fall back to the non-canonicalized form; resolve() still guards traversal.
        }
        String token = tokensByPath.computeIfAbsent(dir.getPath(), p -> newToken());
        rootsByToken.put(token, dir);
        return token;
    }

    /**
     * Rewrites every {@code file:} URL in a {@code Browser.setText} document to the
     * {@code /local-file/<token>/...} path that serves it, registering each one on the way.
     * Returns {@code null} when the document references no local file at all.
     * <p>
     * {@link #registerIfLocalFile} covers the page a {@code setUrl} names; this covers the
     * sub-resources of a page handed over as a string, which have the same problem for a
     * different reason. The web-target Browser renders {@code setText} content from a
     * {@code data:} URL, and a {@code data:} document may not load {@code file:} sub-resources
     * at all — so an application that builds HTML around absolute {@code file:} URLs (Eclipse's
     * intro pages reference every image, stylesheet and script that way) gets a page whose
     * markup renders but whose every resource is refused.
     * <p>
     * Registration reuses tokens and prefers the document's own {@code <base href>} directory as
     * the root (see {@link #servedPath}), so a whole themed page usually costs one. As with
     * {@code setUrl}, this exposes only directories the application itself named — here by
     * embedding a path in HTML it asked us to render.
     */
    public static ServedHtml rewriteLocalResources(String html) {
        if (html == null || html.isEmpty()) return null;
        File baseDir = documentBaseDir(html);
        String basePath = baseDir == null ? null : URL_PREFIX + registerRoot(baseDir) + "/";
        Matcher m = FILE_URL_IN_MARKUP.matcher(html);
        StringBuilder out = new StringBuilder(html.length());
        int copied = 0;
        boolean rewrote = false;
        while (m.find()) {
            String served = servedPath(m.group(), baseDir);
            if (served == null) continue;
            out.append(html, copied, m.start()).append(served);
            copied = m.end();
            rewrote = true;
        }
        if (!rewrote) return null;
        out.append(html, copied, html.length());
        return new ServedHtml(out.toString(), basePath);
    }

    /**
     * The directory a document's own {@code <base href="file:...">} names, or {@code null}.
     * Everything under it is served relative to it — see {@link #servedPath}.
     */
    private static File documentBaseDir(String html) {
        Matcher m = BASE_HREF_PATTERN.matcher(html);
        if (!m.find()) return null;
        File target = resolveBaseHrefTarget(m.group(1));
        if (target == null) return null;
        File dir = target.isDirectory() ? target : target.getParentFile();
        if (dir == null || !dir.isDirectory()) return null;
        try {
            return dir.getCanonicalFile();
        } catch (Exception ignored) {
            return dir;
        }
    }

    /**
     * The {@code /local-file/} path serving what a {@code file:} URL names.
     * <p>
     * A resource under {@code baseDir} is served as a path <em>relative to that one root</em>,
     * because a served URL is all a sub-resource of its own has to resolve against: a stylesheet
     * flattened to {@code /local-file/<its-own-token>/x.css} turns its {@code url(../graphics/y.png)}
     * into {@code /local-file/graphics/y.png}, which names no registration at all. Keeping the
     * directory structure under the root the document itself declared makes those resolve where
     * they were meant to. Anything outside that root falls back to its own parent directory.
     */
    private static String servedPath(String fileUrl, File baseDir) {
        Path path = parseFileUrl(fileUrl);
        if (path == null) return null;
        File file = path.toFile();
        if (baseDir != null) {
            String relative = relativize(baseDir, file);
            if (relative != null) return URL_PREFIX + registerRoot(baseDir) + "/" + relative;
        }
        if (file.isDirectory()) return URL_PREFIX + registerRoot(file) + "/";
        File parent = file.getParentFile();
        if (parent == null || file.getName().isEmpty()) return null;
        return URL_PREFIX + registerRoot(parent) + "/" + encodeSegment(file.getName());
    }

    /**
     * {@code file}'s encoded path relative to {@code root}, or {@code null} if it isn't under it.
     * {@code root} itself relativizes to the empty string, so it keeps its trailing slash.
     */
    private static String relativize(File root, File file) {
        Path rootPath = root.toPath().normalize();
        Path filePath;
        try {
            filePath = file.getCanonicalFile().toPath().normalize();
        } catch (Exception ignored) {
            filePath = file.toPath().normalize();
        }
        if (!filePath.startsWith(rootPath)) return null;
        StringBuilder sb = new StringBuilder();
        for (Path segment : rootPath.relativize(filePath)) {
            if (sb.length() > 0) sb.append('/');
            sb.append(encodeSegment(segment.toString()));
        }
        return sb.toString();
    }

    /** Percent-encodes one path segment the way the {@code /local-file/} handler decodes it. */
    private static String encodeSegment(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * If {@code htmlFile} declares a {@code <base href>} pointing at a filesystem
     * location other than its own directory, registers that location's directory
     * as a secondary root for {@code token} (see {@link #baseHrefRootsByToken}).
     * Best-effort: any failure (not HTML, unreadable, unparsable/remote href,
     * target doesn't exist) just skips the fallback, leaving the primary root as
     * the only lookup for that token.
     */
    private static void registerBaseHrefRoot(String token, File htmlFile) {
        String name = htmlFile.getName().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".html") && !name.endsWith(".htm")) return;
        try {
            String head = readHead(htmlFile, BASE_HREF_SCAN_LIMIT);
            Matcher m = BASE_HREF_PATTERN.matcher(head);
            if (!m.find()) return;
            File target = resolveBaseHrefTarget(m.group(1));
            if (target == null) return;
            File dir = target.isDirectory() ? target : target.getParentFile();
            if (dir == null || !dir.isDirectory()) return;
            baseHrefRootsByToken.put(token, dir.getCanonicalFile());
        } catch (Exception ignored) {
            // Best-effort only, see javadoc above.
        }
    }

    /** Reads up to {@code limit} bytes of {@code file} as UTF-8, for a bounded head-of-file scan. */
    private static String readHead(File file, int limit) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int len = (int) Math.min(limit, raf.length());
            byte[] buf = new byte[len];
            raf.readFully(buf);
            return new String(buf, StandardCharsets.UTF_8);
        }
    }

    /**
     * Resolves a {@code <base href>} value to the {@link File} it names, or
     * {@code null} if it's remote ({@code http(s):}/{@code data:}), relative (the
     * primary root already covers that case), or doesn't exist on disk. Accepts
     * both {@code file:} URLs and the root-absolute, scheme-less filesystem paths
     * tools like Eclipse's {@code FileLocator} commonly emit.
     */
    private static File resolveBaseHrefTarget(String href) {
        if (href == null || href.isEmpty()) return null;
        Path p;
        if (href.regionMatches(true, 0, "file:", 0, 5)) {
            p = parseFileUrl(href);
        } else if (href.startsWith("/")) {
            p = Paths.get(href);
        } else {
            return null;
        }
        if (p == null) return null;
        File f = p.toFile();
        return f.exists() ? f : null;
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
     * <p>
     * Falls back to the token's {@code <base href>} directory (see
     * {@link #registerBaseHrefRoot}) when the primary root doesn't have it, treating
     * {@code "/" + relativePath} as the resource's real absolute filesystem path.
     */
    public static File resolve(String token, String relativePath) {
        File root = rootsByToken.get(token);
        if (root != null) {
            Path rootPath = root.toPath().normalize();
            Path resolved = rootPath.resolve(relativePath).normalize();
            if (resolved.startsWith(rootPath)) {
                File file = resolved.toFile();
                if (file.isFile()) return file;
            }
        }
        File baseRoot = baseHrefRootsByToken.get(token);
        if (baseRoot != null) {
            Path basePath = baseRoot.toPath().normalize();
            Path absolute = Paths.get("/" + relativePath).normalize();
            if (absolute.startsWith(basePath)) {
                File file = absolute.toFile();
                if (file.isFile()) return file;
            }
        }
        return null;
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

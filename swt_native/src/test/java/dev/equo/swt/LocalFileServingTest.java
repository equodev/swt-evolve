package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers {@link LocalFileServing#registerIfLocalFile}'s fragment/query handling,
 *  {@link LocalFileServing#rewriteLocalResources} and
 *  {@link LocalFileServing#tokenFromLocalFileReferer}. */
class LocalFileServingTest {

    @Test
    void registerIfLocalFile_withHashFragment_stillResolvesTheRealFile() throws Exception {
        File file = writeTempHtml();
        try {
            LocalFileServing.Served served =
                    LocalFileServing.registerIfLocalFile("file:" + file.getAbsolutePath() + "#/");

            assertThat(served)
                    .as("a trailing #/ fragment (hash-router default route) must not make the "
                            + "file lookup miss")
                    .isNotNull();
            assertThat(served.relativePath).isEqualTo(file.getName());
            File resolved = LocalFileServing.resolve(served.token, served.relativePath);
            assertThat(resolved).as("the token/relativePath pair must resolve to a real file").isNotNull();
            // Compare canonical paths, not File#equals: registerIfLocalFile() canonicalizes the
            // parent directory (e.g. macOS resolves /var/folders/... to /private/var/folders/...),
            // so the resolved File's path legitimately differs in string form from the original.
            assertThat(resolved.getCanonicalFile())
                    .as("the token/relativePath pair must resolve back to the same file")
                    .isEqualTo(file.getCanonicalFile());
        } finally {
            file.delete();
        }
    }

    @Test
    void registerIfLocalFile_withQueryString_stillResolvesTheRealFile() throws Exception {
        File file = writeTempHtml();
        try {
            LocalFileServing.Served served =
                    LocalFileServing.registerIfLocalFile("file:" + file.getAbsolutePath() + "?v=2");

            assertThat(served).isNotNull();
            assertThat(served.relativePath).isEqualTo(file.getName());
        } finally {
            file.delete();
        }
    }

    @Test
    void registerIfLocalFile_withHashAndQuery_stillResolvesTheRealFile() throws Exception {
        File file = writeTempHtml();
        try {
            LocalFileServing.Served served = LocalFileServing.registerIfLocalFile(
                    "file:" + file.getAbsolutePath() + "?v=2#/route");

            assertThat(served).isNotNull();
            assertThat(served.relativePath).isEqualTo(file.getName());
        } finally {
            file.delete();
        }
    }

    @Test
    void registerIfLocalFile_withoutFragment_stillWorks() throws Exception {
        File file = writeTempHtml();
        try {
            LocalFileServing.Served served =
                    LocalFileServing.registerIfLocalFile(file.toURI().toString());
            assertThat(served).isNotNull();
            assertThat(served.relativePath).isEqualTo(file.getName());
        } finally {
            file.delete();
        }
    }

    @Test
    void registerIfLocalFile_forAFileWrittenAfterTheNavigation_stillServesIt() throws Exception {
        // An application may navigate to a template it writes moments later, and does. Requiring
        // the file to exist at setUrl time would leave the iframe on the raw file: URL for good,
        // which a browser refuses to load at all — blank however long the file exists by the time
        // it is fetched.
        File dir = Files.createTempDirectory("equo-late-file").toFile();
        File file = new File(dir, "template.html");

        LocalFileServing.Served served =
                LocalFileServing.registerIfLocalFile("file:" + file.getAbsolutePath());

        assertThat(served).as("a not-yet-written file must still register").isNotNull();
        assertThat(LocalFileServing.resolve(served.token, served.relativePath))
                .as("nothing to serve until the file appears").isNull();

        Files.writeString(file.toPath(), "<html></html>", StandardCharsets.UTF_8);

        assertThat(LocalFileServing.resolve(served.token, served.relativePath))
                .as("the same registration serves the file once it has been written")
                .isNotNull()
                .satisfies(served2 -> assertThat(served2.getCanonicalFile()).isEqualTo(file.getCanonicalFile()));
    }

    @Test
    void rewriteLocalResources_pointsEverySubResourceAtTheServingEndpoint() throws Exception {
        File dir = Files.createTempDirectory("equo-settext").toFile();
        File image = new File(dir, "overview48.png");
        Files.writeString(image.toPath(), "png", StandardCharsets.UTF_8);
        File style = new File(dir, "shared.css");
        Files.writeString(style.toPath(), "body{}", StandardCharsets.UTF_8);

        String html = "<html><head>"
                + "<BASE href=\"file://" + dir.getAbsolutePath() + "/\">"
                + "<LINK rel=\"stylesheet\" href=\"file://" + style.getAbsolutePath() + "\">"
                + "</head><body><IMG src=\"file:" + image.getAbsolutePath() + "\"></body></html>";

        LocalFileServing.ServedHtml served = LocalFileServing.rewriteLocalResources(html);

        assertThat(served).isNotNull();
        assertThat(served.html).as("no file: URL may survive the rewrite").doesNotContain("file:");
        assertThat(served.basePath)
                .as("the document's own <base href> becomes the directory's serving path")
                .isNotNull()
                .startsWith(LocalFileServing.URL_PREFIX)
                .endsWith("/");
        // Both sub-resources must resolve back through the endpoint the rewrite pointed them at.
        assertThat(resolveServed(served.html, "overview48.png")).isEqualTo(image.getCanonicalFile());
        assertThat(resolveServed(served.html, "shared.css")).isEqualTo(style.getCanonicalFile());
    }

    /**
     * A stylesheet's own {@code url(../graphics/x.png)} resolves against the URL it was served
     * from, so flattening it under a per-file token would send that one level above every
     * registration. Serving it under the document's base root keeps the structure it needs.
     */
    @Test
    void rewriteLocalResources_keepsResourcesUnderTheBaseRootAddressableFromTheirOwnRelativeUrls()
            throws Exception {
        File base = Files.createTempDirectory("equo-settext-base").toFile();
        File style = new File(base, "themes/solstice/html/qroot.css");
        style.getParentFile().mkdirs();
        Files.writeString(style.toPath(), "body{background:url(../graphics/bg.png)}", StandardCharsets.UTF_8);
        File graphic = new File(base, "themes/solstice/graphics/bg.png");
        graphic.getParentFile().mkdirs();
        Files.writeString(graphic.toPath(), "png", StandardCharsets.UTF_8);

        String html = "<html><head><BASE href=\"file://" + base.getAbsolutePath() + "/\">"
                + "<LINK rel=\"stylesheet\" href=\"file://" + style.getAbsolutePath() + "\">"
                + "</head><body></body></html>";

        LocalFileServing.ServedHtml served = LocalFileServing.rewriteLocalResources(html);

        assertThat(served).isNotNull();
        assertThat(served.basePath).isNotNull();
        String token = served.basePath.substring(LocalFileServing.URL_PREFIX.length()).replace("/", "");
        assertThat(served.html)
                .as("the stylesheet keeps its path under the base root, not a token of its own")
                .contains(served.basePath + "themes/solstice/html/qroot.css");
        // What the browser asks for after resolving the stylesheet's own url(../graphics/bg.png).
        assertThat(LocalFileServing.resolve(token, "themes/solstice/graphics/bg.png"))
                .isNotNull()
                .satisfies(f -> assertThat(f.getCanonicalFile()).isEqualTo(graphic.getCanonicalFile()));
    }

    @Test
    void rewriteLocalResources_leavesADocumentWithNoLocalResourceAlone() {
        assertThat(LocalFileServing.rewriteLocalResources(
                "<html><body><img src=\"https://example.org/a.png\"></body></html>")).isNull();
    }

    /** Finds the rewritten {@code /local-file/<token>/<name>} reference and resolves it back. */
    private static File resolveServed(String html, String name) throws Exception {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile(java.util.regex.Pattern.quote(LocalFileServing.URL_PREFIX)
                        + "([^/\"']+)/" + java.util.regex.Pattern.quote(name))
                .matcher(html);
        assertThat(m.find()).as("%s must appear as a served path", name).isTrue();
        File resolved = LocalFileServing.resolve(m.group(1), name);
        assertThat(resolved).as("%s must resolve to a real file", name).isNotNull();
        return resolved.getCanonicalFile();
    }

    @Test
    void relativizeBaseHref_keepsAFilesystemBaseUnderThePagesToken() throws Exception {
        // Not canonicalized: a base href written through a symlink (/var on macOS) must still match.
        File dir = Files.createTempDirectory("equo-base-href").toFile();
        File target = new File(dir, "welcome.html");
        Files.write(target.toPath(), "<html></html>".getBytes(StandardCharsets.UTF_8));
        File style = new File(dir, "style.css");
        Files.write(style.toPath(), "body {}".getBytes(StandardCharsets.UTF_8));
        style = style.getCanonicalFile();

        // The platform's own path, and the form a file: URL carries (/C:/... on Windows).
        for (String href : new String[] {target.getAbsolutePath(), target.toURI().getPath()}) {
            File page = new File(Files.createTempDirectory("equo-base-href-page").toFile(), "page.html");
            String html = "<html><base href=\"" + href + "\" /><head></head></html>";
            Files.write(page.toPath(), html.getBytes(StandardCharsets.UTF_8));
            String token = LocalFileServing.registerIfLocalFile(page.toURI().toString()).token;

            assertThat(servedThroughBase(token, "123.html",
                    LocalFileServing.relativizeBaseHref(html, "123.html"), "style.css"))
                    .as("relative to /local-file/<token>/, so resources resolve below the token (%s)", href)
                    .isEqualTo(style);
            assertThat(servedThroughBase(token, "sub/123.html",
                    LocalFileServing.relativizeBaseHref(html, "sub/123.html"), "style.css"))
                    .as("a page below the token's root climbs back to it first (%s)", href)
                    .isEqualTo(style);
        }
    }

    /**
     * Resolves {@code resource} the way a browser does, against the {@code <base href>} of
     * {@code html} served at {@code /local-file/<token>/<pagePath>}, and serves the result.
     */
    private static File servedThroughBase(String token, String pagePath, String html, String resource) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("<base href=\"([^\"]*)\"").matcher(html);
        assertThat(m.find()).as("the page keeps its <base href>").isTrue();
        String tokenRoot = LocalFileServing.URL_PREFIX + token + "/";
        String path = java.net.URI.create("http://localhost" + tokenRoot + pagePath)
                .resolve(m.group(1)).resolve(resource).getPath();
        assertThat(path).as("the resource stays under the page's token").startsWith(tokenRoot);
        return LocalFileServing.resolve(token, path.substring(tokenRoot.length()));
    }

    @Test
    void relativizeBaseHref_leavesOtherBasesAlone() {
        String remote = "<html><base href=\"https://example.com/docs/\"></html>";
        assertThat(LocalFileServing.relativizeBaseHref(remote, "p.html")).isSameAs(remote);
        String missing = "<html><base href=\"/no/such/equo/dir/page.html\"></html>";
        assertThat(LocalFileServing.relativizeBaseHref(missing, "p.html")).isSameAs(missing);
        String missingDrive = "<html><base href=\"/Q:/no/such/equo/dir/page.html\"></html>";
        assertThat(LocalFileServing.relativizeBaseHref(missingDrive, "p.html")).isSameAs(missingDrive);
        String network = "<html><base href=\"//host/share/page.html\"></html>";
        assertThat(LocalFileServing.relativizeBaseHref(network, "p.html")).isSameAs(network);
        String none = "<html><head></head></html>";
        assertThat(LocalFileServing.relativizeBaseHref(none, "p.html")).isSameAs(none);
    }

    /** Both flavours of the parser, whichever platform runs the test. */
    @Test
    void absoluteFilesystemPath_readsDrivePathsOnlyWhereThePlatformHasThem() {
        assertThat(fsPath("/C:/b/x", true)).as("a file: URL's path").isEqualTo("C:/b/x");
        assertThat(fsPath("C:/b/x", true)).isEqualTo("C:/b/x");
        assertThat(fsPath("C:\\b\\x", true)).isEqualTo("C:/b/x");
        assertThat(fsPath("/opt/b/x", true)).isEqualTo("/opt/b/x");
        assertThat(fsPath("/opt/b/x", false)).isEqualTo("/opt/b/x");

        assertThat(fsPath("C:/b/x", false)).as("no drive letters on this platform").isNull();
        assertThat(fsPath("C:b", true)).as("drive-relative").isNull();
        assertThat(fsPath("b/x", true)).as("relative").isNull();
        assertThat(fsPath("//host/share", true)).as("network path").isNull();
        assertThat(fsPath("//host/share", false)).as("network path").isNull();
        assertThat(fsPath("\\\\host\\share", true)).as("network path").isNull();
        assertThat(fsPath("/C:/a\u0000b", true)).as("unrepresentable, not thrown").isNull();
        assertThat(fsPath("/a\u0000b", false)).as("unrepresentable, not thrown").isNull();
    }

    private static String fsPath(String path, boolean drivePaths) {
        java.nio.file.Path p = LocalFileServing.absoluteFilesystemPath(path, drivePaths);
        return p == null ? null : p.toString().replace('\\', '/');
    }

    @Test
    void tokenFromLocalFileReferer_extractsTokenFromLocalFileUrl() {
        String referer = "http://localhost:56591/local-file/AbCdEf123456/LocalPage.html";
        assertThat(LocalFileServing.tokenFromLocalFileReferer(referer)).isEqualTo("AbCdEf123456");
    }

    @Test
    void tokenFromLocalFileReferer_returnsNullForUnrelatedReferer() {
        assertThat(LocalFileServing.tokenFromLocalFileReferer("http://localhost:56591/some/page")).isNull();
    }

    @Test
    void tokenFromLocalFileReferer_returnsNullForNull() {
        assertThat(LocalFileServing.tokenFromLocalFileReferer(null)).isNull();
    }

    @Test
    void tokenFromLocalFileReferer_returnsNullWhenTokenHasNoTrailingSegment() {
        // A bare "/local-file/<token>" with nothing after it isn't a servable page URL.
        assertThat(LocalFileServing.tokenFromLocalFileReferer("http://localhost:56591/local-file/AbCdEf"))
                .isNull();
    }

    private static File writeTempHtml() throws Exception {
        File f = File.createTempFile("equo-local-file-serving", ".html");
        Files.writeString(f.toPath(), "<!doctype html><title>t</title>", StandardCharsets.UTF_8);
        return f;
    }
}

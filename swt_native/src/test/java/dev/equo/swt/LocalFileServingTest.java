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

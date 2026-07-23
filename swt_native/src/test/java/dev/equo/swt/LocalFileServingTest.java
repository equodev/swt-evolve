package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/** Covers {@link LocalFileServing#registerIfLocalFile}'s fragment/query handling and
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
    void tokenFromLocalFileReferer_extractsTokenFromLocalFileUrl() {
        String referer = "http://localhost:56591/local-file/AbCdEf123456/MobileRecorder.html";
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

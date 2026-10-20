package dev.equo.swt;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** The {@code setText} documents a web Browser posts to the app server and then loads. */
@Tag("native-unit")
public class InlineDocumentHandlerNativeTest {

    private HttpServer server;
    private String base;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(InlineDocumentHandler.PATH, new InlineDocumentHandler());
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    public void stopServer() {
        server.stop(0);
    }

    @Test
    public void a_posted_document_is_served_as_html_until_it_is_deleted() throws Exception {
        String path = send(post("<p>loading</p>", "same-origin")).body();

        HttpResponse<String> page = send(HttpRequest.newBuilder(URI.create(base + path)).build());
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.headers().firstValue("Content-Type")).hasValue("text/html; charset=utf-8");
        assertThat(page.body()).isEqualTo("<p>loading</p>");

        send(HttpRequest.newBuilder(URI.create(base + path)).DELETE().build());
        assertThat(send(HttpRequest.newBuilder(URI.create(base + path)).build()).statusCode()).isEqualTo(404);
    }

    @Test
    public void another_site_cannot_add_a_document_to_this_origin() throws Exception {
        assertThat(send(post("<script>alert(1)</script>", "cross-site")).statusCode()).isEqualTo(403);
    }

    @Test
    public void a_document_past_the_size_bound_is_refused() throws Exception {
        char[] html = new char[InlineDocumentHandler.MAX_DOCUMENT_BYTES + 1];
        Arrays.fill(html, 'a');
        assertThat(send(post(new String(html), "same-origin")).statusCode()).isEqualTo(413);
    }

    private HttpRequest post(String html, String fetchSite) {
        return HttpRequest.newBuilder(URI.create(base + "inline-document"))
                .header("Sec-Fetch-Site", fetchSite)
                .POST(HttpRequest.BodyPublishers.ofString(html)).build();
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

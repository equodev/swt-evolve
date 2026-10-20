package dev.equo.swt;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the web backend's {@code /proxy} does to a page so a login inside a {@code Browser} works:
 * POST forms submit through it, and cookies travel with every hop it follows.
 */
@Tag("native-unit")
public class ProxyHandlerNativeTest {

    private static final String SELF = "http://localhost:4000";
    private static final URI PAGE = URI.create("http://127.0.0.1:9000/idp/login");
    /** In a browser's navigation {@code Accept}, absent from {@code HttpURLConnection}'s default one. */
    private static final String BROWSER_ONLY_ACCEPT_TOKEN = "application/xhtml+xml";

    private HttpServer server;
    private HttpServer proxy;
    private String base;

    @BeforeEach
    public void clearProxyOptIn() {
        System.clearProperty("dev.equo.swt.web.proxy");
    }

    @AfterEach
    public void stopServer() {
        if (server != null) server.stop(0);
        if (proxy != null) proxy.stop(0);
    }

    private static String rewriteForms(String html) {
        return ProxyHandler.rewriteFormActions(html, PAGE, Collections.<String>emptyList(), SELF);
    }

    @Test
    public void a_post_form_submits_through_the_proxy_with_its_query_decoded() {
        String html = rewriteForms("<form id=\"login\" method=\"post\" action=\"/idp/authenticate?code=1&amp;tab=2\">");

        assertThat(html).isEqualTo("<form id=\"login\" method=\"post\" action=\"" + SELF
                + "/proxy?url=http%3A%2F%2F127.0.0.1%3A9000%2Fidp%2Fauthenticate%3Fcode%3D1%26tab%3D2\">");
    }

    @Test
    public void a_get_form_keeps_its_action_because_the_browser_replaces_the_query() {
        String get = "<form method=\"get\" action=\"/search\">";
        String noMethod = "<form action=\"/search\">";

        assertThat(rewriteForms(get)).isEqualTo(get);
        assertThat(rewriteForms(noMethod)).isEqualTo(noMethod);
    }

    @Test
    public void only_the_action_attribute_is_rewritten() {
        String html = rewriteForms("<form data-action=\"/x\" method=POST action='/submit'>");

        assertThat(html).startsWith("<form data-action=\"/x\" method=POST action='" + SELF + "/proxy?url=");
    }

    @Test
    public void a_form_posting_to_a_host_the_proxy_does_not_serve_is_left_alone() {
        String html = "<form method=\"post\" action=\"https://not-proxied.test/submit\">";

        assertThat(rewriteForms(html)).isEqualTo(html);
    }

    @Test
    public void a_link_is_proxied_with_its_character_references_decoded() {
        String html = ProxyHandler.rewriteResourceUrls("<a href=\"/next?a=1&amp;b=2\">", PAGE,
                Collections.<String>emptyList(), SELF);

        assertThat(html).isEqualTo("<a href=\"" + SELF + "/proxy?url=http%3A%2F%2F127.0.0.1%3A9000%2Fnext%3Fa%3D1%26b%3D2\">");
    }

    /**
     * The shape of an OAuth2 login: the app sets its session on the hop that redirects to the
     * identity provider, which sets its own; the form posts back with both; the callback rotates
     * the app's session. Each step fails unless the previous hop's cookies arrive.
     */
    @Test
    public void cookies_travel_with_every_redirect_hop_of_a_login() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/start", ex -> redirect(ex, "/idp/login", "SESSION=s1; Path=/"));
        server.createContext("/idp/login", ex -> {
            if (!cookie(ex).contains("SESSION=s1")) { respond(ex, 400, "no session"); return; }
            ex.getResponseHeaders().add("Set-Cookie", "IDP=i1; Path=/idp/");
            respond(ex, 200, "form");
        });
        server.createContext("/idp/authenticate", ex -> {
            if (!"POST".equals(ex.getRequestMethod()) || !cookie(ex).contains("IDP=i1")) { respond(ex, 400, "no idp"); return; }
            redirect(ex, "/callback", null);
        });
        server.createContext("/callback", ex -> {
            if (!cookie(ex).contains("SESSION=s1")) { respond(ex, 400, "no session"); return; }
            redirect(ex, "/done", "SESSION=s2; Path=/");
        });
        server.createContext("/done", ex -> respond(ex, 200, cookie(ex)));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        ProxyCookieJar jar = new ProxyCookieJar(WebFlutterServer::proxyAllowed);

        ProxyHandler.Fetched form = ProxyHandler.fetch(base + "/start", "GET", new byte[0],
                Collections.<String, String>emptyMap(), Collections.<String>emptyList(), jar);
        assertThat(new String(form.body, StandardCharsets.UTF_8)).isEqualTo("form");

        ProxyHandler.Fetched done = ProxyHandler.fetch(base + "/idp/authenticate", "POST",
                "user=u".getBytes(StandardCharsets.UTF_8),
                Collections.singletonMap("Content-Type", "application/x-www-form-urlencoded"),
                Collections.<String>emptyList(), jar);
        assertThat(done.finalUrl).isEqualTo(base + "/done");
        assertThat(new String(done.body, StandardCharsets.UTF_8)).isEqualTo("SESSION=s2");
        assertThat(jar.get("SESSION", base + "/")).isEqualTo("s2");
    }

    @Test
    public void the_page_headers_reach_every_hop_unless_set_url_gives_its_own() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/start", ex -> redirect(ex, "/page", null));
        server.createContext("/page", ex -> respond(ex, 200, ex.getRequestHeaders().getFirst("Accept")
                + " | " + ex.getRequestHeaders().getFirst("Accept-Language")));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        Map<String, String> pageHeaders = new LinkedHashMap<>();
        pageHeaders.put("Accept", "text/html");
        pageHeaders.put("Accept-Language", "es-AR,es;q=0.9");

        assertThat(fetchBody(base + "/start", pageHeaders, Collections.<String>emptyList()))
                .isEqualTo("text/html | es-AR,es;q=0.9");
        assertThat(fetchBody(base + "/start", pageHeaders, Collections.singletonList("Accept: application/json")))
                .isEqualTo("application/json | es-AR,es;q=0.9");
    }

    /** A server that picks the representation from {@code Accept} serves the page a browser would get. */
    @Test
    public void the_page_accept_header_reaches_the_upstream() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/info", ex -> respond(ex, 200,
                String.valueOf(ex.getRequestHeaders().getFirst("Accept")).contains(BROWSER_ONLY_ACCEPT_TOKEN)
                        ? "text/html" : "text/plain",
                "<html><head></head><body></body></html>"));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxy.createContext("/proxy", new ProxyHandler(new ProxyCookieJar(WebFlutterServer::proxyAllowed)));
        proxy.start();

        HttpResponse<String> page = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + proxy.getAddress().getPort() + "/proxy?url="
                                + URLEncoder.encode(base + "/info", StandardCharsets.UTF_8)))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .timeout(Duration.ofSeconds(10))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(page.headers().firstValue("Content-Type")).hasValue("text/html");
        assertThat(page.body()).contains("<base href=\"" + base + "/info\">");
    }

    @Test
    public void the_navigation_shim_runs_first_and_reroutes_only_the_target_origin() {
        String html = ProxyHandler.injectNavigationShim("<html><head><script src=\"app.js\"></script></head></html>",
                "https://id.example.test", Collections.singletonList("Authorization: Bearer t"), SELF);

        assertThat(html).startsWith("<html><head><script>(function(){var nav=window.navigation;");
        assertThat(html.indexOf("navigation")).isLessThan(html.indexOf("app.js"));
        assertThat(html).contains("new URL(\"https://id.example.test\").origin")
                .contains("SELF_ORIGIN=\"" + SELF + "\"")
                .contains("\"Authorization: Bearer t\"")
                .contains("e.formData");
    }

    private static String cookie(HttpExchange ex) {
        String c = ex.getRequestHeaders().getFirst("Cookie");
        return c == null ? "" : c;
    }

    private static void redirect(HttpExchange ex, String location, String setCookie) throws IOException {
        if (setCookie != null) ex.getResponseHeaders().add("Set-Cookie", setCookie);
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
        ex.close();
    }

    private static String fetchBody(String url, Map<String, String> pageHeaders, List<String> setUrlHeaders)
            throws IOException {
        ProxyHandler.Fetched page = ProxyHandler.fetch(url, "GET", new byte[0], pageHeaders, setUrlHeaders,
                new ProxyCookieJar(WebFlutterServer::proxyAllowed));
        return new String(page.body, StandardCharsets.UTF_8);
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        respond(ex, code, "text/plain", body);
    }

    private static void respond(HttpExchange ex, int code, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", contentType);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}

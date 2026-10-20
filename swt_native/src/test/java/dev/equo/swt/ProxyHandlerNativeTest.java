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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final List<HttpServer> proxies = new ArrayList<>();
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private String base;

    @BeforeEach
    public void clearProxyOptIn() {
        System.clearProperty("dev.equo.swt.web.proxy");
    }

    @AfterEach
    public void stopServer() {
        if (server != null) server.stop(0);
        for (HttpServer proxy : proxies) proxy.stop(0);
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
        int proxy = startProxy();

        HttpResponse<String> page = CLIENT.send(HttpRequest.newBuilder(URI.create(
                        "http://127.0.0.1:" + proxy + "/proxy?url="
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

    @Test
    public void an_inline_module_imports_the_target_through_the_path_form() {
        String html = ProxyHandler.injectModuleImportMap(
                "<html><head><script type=\"module\">import { f } from \"/js/a.js\";</script></head></html>", PAGE, SELF);

        Map<String, Object> map = importMap(html);
        assertThat(map.get("imports")).isEqualTo(Collections.singletonMap(
                "http://127.0.0.1:9000/", SELF + "/proxy/http://127.0.0.1:9000/"));
        Map<String, Object> inTarget = new LinkedHashMap<>();
        inTarget.put(SELF + "/", SELF + "/proxy/http://127.0.0.1:9000/");
        inTarget.put(SELF + "/proxy/", SELF + "/proxy/");
        assertThat(map.get("scopes")).isEqualTo(Collections.singletonMap(SELF + "/proxy/http://127.0.0.1:9000/", inTarget));
        assertThat(html.indexOf("importmap")).isLessThan(html.indexOf("type=\"module\""));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void the_page_import_map_is_merged_with_its_addresses_in_the_path_form() {
        String html = ProxyHandler.injectModuleImportMap("<head></head><body><script type=\"importmap\">"
                + "{\"imports\":{\"lib\":\"/vendor/lib.js\",\"cdn\":\"https://not-proxied.test/x.js\"},"
                + "\"integrity\":{\"/vendor/lib.js\":\"sha384-abc\"}}</script></body>", PAGE, SELF);

        assertThat(html).startsWith("<head></head><body><script type=\"importmap\">").doesNotContain("<head><script");
        Map<String, Object> map = importMap(html);
        assertThat((Map<String, Object>) map.get("imports"))
                .containsEntry("lib", SELF + "/proxy/http://127.0.0.1:9000/vendor/lib.js")
                .containsEntry("cdn", "https://not-proxied.test/x.js")
                .containsEntry("http://127.0.0.1:9000/", SELF + "/proxy/http://127.0.0.1:9000/");
        assertThat(map.get("integrity")).isEqualTo(Collections.singletonMap(SELF + "/proxy/http://127.0.0.1:9000/vendor/lib.js", "sha384-abc"));
    }

    @Test
    public void a_page_without_modules_gets_no_import_map() {
        String html = "<html><head><script src=\"app.js\"></script></head></html>";

        assertThat(ProxyHandler.injectModuleImportMap(html, PAGE, SELF)).isEqualTo(html);
    }

    @Test
    public void a_module_script_is_served_in_the_path_form_and_a_classic_one_is_not() {
        String html = ProxyHandler.rewriteResourceUrls(
                "<script type=\"module\" src=\"/js/a.js?v=1&amp;w=2\"></script><script src=\"/js/b.js\"></script>",
                PAGE, Collections.<String>emptyList(), SELF);

        assertThat(ProxyHandler.rewriteModuleScripts(html, SELF)).isEqualTo(
                "<script type=\"module\" src=\"" + SELF + "/proxy/http://127.0.0.1:9000/js/a.js?v=1&amp;w=2\"></script>"
                + "<script src=\"" + SELF + "/proxy?url=http%3A%2F%2F127.0.0.1%3A9000%2Fjs%2Fb.js\"></script>");
    }

    @Test
    public void a_path_form_request_fetches_the_url_in_its_path_with_its_query() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/js/a.js", ex -> respond(ex, 200, "export const q = '" + ex.getRequestURI().getRawQuery() + "';"));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();

        HttpResponse<String> module = get(startProxy(), "/proxy/" + base + "/js/a.js?url=x&header=y&v=1");

        assertThat(module.statusCode()).isEqualTo(200);
        assertThat(module.body()).isEqualTo("export const q = 'url=x&header=y&v=1';");
    }

    @Test
    public void a_module_gets_the_header_lines_of_its_page_only_on_the_same_server() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> respond(ex, 200, String.valueOf(ex.getRequestHeaders().getFirst("X-Token"))));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        int proxy = startProxy();
        int otherProxy = startProxy();

        get(proxy, "/proxy?url=" + base + "/page&header=X-Token%3A%20t1");

        assertThat(get(proxy, "/proxy/" + base + "/js/a.js").body()).isEqualTo("t1");
        assertThat(get(otherProxy, "/proxy/" + base + "/js/a.js").body()).isEqualTo("null");
    }

    @Test
    public void an_asset_is_cached_as_the_origin_says_and_a_page_never_is() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/cached.js", ex -> {
            ex.getResponseHeaders().add("Cache-Control", "max-age=600");
            ex.getResponseHeaders().add("Vary", "Accept-Language");
            respond(ex, 200, "1");
        });
        server.createContext("/uncached.js", ex -> respond(ex, 200, "2"));
        server.createContext("/page", ex -> {
            ex.getResponseHeaders().add("Cache-Control", "max-age=600");
            respond(ex, 200, "text/html", "<html><head></head></html>");
        });
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        int proxy = startProxy();

        HttpResponse<String> cached = get(proxy, "/proxy?url=" + base + "/cached.js");
        assertThat(cached.headers().firstValue("Cache-Control")).hasValue("max-age=600");
        assertThat(cached.headers().firstValue("Vary")).hasValue("Accept-Language");
        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/uncached.js")).isEqualTo("no-store");
        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/page")).isEqualTo("no-store");
    }

    @Test
    public void a_response_the_browser_cache_key_cannot_tell_apart_is_never_cached() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/style.css", ex -> {
            ex.getResponseHeaders().add("Cache-Control", "max-age=600");
            respond(ex, 200, "text/css", "body{}");
        });
        server.createContext("/private.js", ex -> {
            ex.getResponseHeaders().add("Cache-Control", "private, max-age=600");
            ex.getResponseHeaders().add("Vary", "Cookie");
            respond(ex, 200, "1");
        });
        server.createContext("/moved.js", ex -> redirect(ex, "/style.css", null));
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        int proxy = startProxy();

        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/style.css&header=X-Token%3A%20t1")).isEqualTo("no-store");
        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/private.js")).isEqualTo("no-store");
        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/moved.js")).isEqualTo("no-store");
        assertThat(cacheControl(proxy, "/proxy?url=" + base + "/style.css?unauthenticated")).isEqualTo("max-age=600");
    }

    @Test
    public void only_a_navigation_waits_for_a_sibling_request_with_headers() {
        assertThat(ProxyHandler.isNavigation(null)).isTrue();
        assertThat(ProxyHandler.isNavigation("iframe")).isTrue();
        assertThat(ProxyHandler.isNavigation("document")).isTrue();
        assertThat(ProxyHandler.isNavigation("object")).isTrue();
        assertThat(ProxyHandler.isNavigation("style")).isFalse();
        assertThat(ProxyHandler.isNavigation("script")).isFalse();
        assertThat(ProxyHandler.isNavigation("empty")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void a_module_from_another_proxied_origin_gets_its_own_mapping() {
        String other = "http://127.0.0.2:9001";
        String html = ProxyHandler.rewriteModuleScripts(ProxyHandler.rewriteResourceUrls(
                "<head></head><script type=\"module\" src=\"" + other + "/lib/a.js\"></script>", PAGE,
                Collections.<String>emptyList(), SELF), SELF);

        Map<String, Object> map = importMap(ProxyHandler.injectModuleImportMap(html, PAGE, SELF));

        assertThat((Map<String, Object>) map.get("imports"))
                .containsEntry(other + "/", SELF + "/proxy/" + other + "/")
                .containsEntry("http://127.0.0.1:9000/", SELF + "/proxy/http://127.0.0.1:9000/");
        assertThat((Map<String, Object>) map.get("scopes")).containsKey(SELF + "/proxy/" + other + "/");
    }

    @Test
    public void a_classic_script_with_a_dynamic_import_gets_the_import_map() {
        String html = ProxyHandler.injectModuleImportMap(
                "<head></head><script>import(\"/js/a.js\").then(m => m.run());</script>", PAGE, SELF);

        assertThat(importMap(html)).containsKey("imports");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void every_page_import_map_moves_to_the_path_form_and_a_broken_one_is_kept() {
        String html = ProxyHandler.injectModuleImportMap("<head></head>"
                + "<script type=\"importmap\">{\"imports\":{\"a\":\"/a.js\"},\"integrity\":{\"/a.js\":\"sha384-a\"}}</script>"
                + "<script type=\"importmap\">{\"imports\":{\"b\":\"/b.js\"}}</script>"
                + "<script type=\"importmap\">{not json</script>", PAGE, SELF);

        Matcher maps = Pattern.compile("<script type=\"importmap\">(.*?)</script>").matcher(html);
        assertThat(maps.find()).isTrue();
        Map<String, Object> first = (Map<String, Object>) EvalJson.parse(maps.group(1));
        assertThat((Map<String, Object>) first.get("imports")).containsEntry("a", SELF + "/proxy/http://127.0.0.1:9000/a.js");
        assertThat(first.get("integrity")).isEqualTo(Collections.singletonMap(SELF + "/proxy/http://127.0.0.1:9000/a.js", "sha384-a"));
        assertThat(maps.find()).isTrue();
        Map<String, Object> second = (Map<String, Object>) EvalJson.parse(maps.group(1));
        assertThat(second.get("imports")).isEqualTo(Collections.singletonMap("b", SELF + "/proxy/http://127.0.0.1:9000/b.js"));
        assertThat(maps.find()).isTrue();
        assertThat(maps.group(1)).isEqualTo("{not json");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> importMap(String html) {
        Matcher m = Pattern.compile("<script type=\"importmap\">(.*?)</script>").matcher(html);
        assertThat(m.find()).isTrue();
        return (Map<String, Object>) EvalJson.parse(m.group(1));
    }

    private int startProxy() throws IOException {
        HttpServer proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxy.createContext("/proxy", new ProxyHandler(new ProxyCookieJar(WebFlutterServer::proxyAllowed)));
        proxy.start();
        proxies.add(proxy);
        return proxy.getAddress().getPort();
    }

    private HttpResponse<String> get(int proxyPort, String pathAndQuery) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + proxyPort + pathAndQuery))
                .timeout(Duration.ofSeconds(10)).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String cacheControl(int proxyPort, String pathAndQuery) throws Exception {
        return get(proxyPort, pathAndQuery).headers().firstValue("Cache-Control").orElse(null);
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

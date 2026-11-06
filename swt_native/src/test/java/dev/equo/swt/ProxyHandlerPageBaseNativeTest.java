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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A page served through {@code /proxy} loads what it loads from its own server: relative references
 * resolve against its own {@code <base href>}, its script requests come back through the proxy, and a
 * response its server sent without a type reaches the browser unchanged.
 */
@Tag("native-unit")
public class ProxyHandlerPageBaseNativeTest {

    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final String FETCH_SHIM = "XMLHttpRequest.prototype.open=";
    private static final String DOM_REWRITE = "REWRITE_ATTRS";

    private HttpServer server;
    private HttpServer proxy;
    private ProxyHandler handler;
    private String origin;

    @BeforeEach
    public void startServers() throws IOException {
        System.clearProperty("dev.equo.swt.web.proxy");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/app/form/index.html", ex -> respond(ex, "text/html",
                "<html><head><base href=\"/\"><script src=\"main.js\" type=\"module\"></script>"
                        + "<script src=\"polyfills.js\"></script></head><body></body></html>"));
        server.createContext("/app/plain.html", ex -> respond(ex, "text/html",
                "<html><head><script src=\"main.js\"></script></head></html>"));
        server.createContext("/media/icons.woff2", ex -> respond(ex, null, "wOF2<head>\u0001\u0002"));
        server.createContext("/untyped/spaced", ex -> respond(ex, null,
                "\n  <!DOCTYPE html><html><head></head><body></body></html>"));
        server.createContext("/untyped/bom", ex -> respond(ex, null,
                "﻿<!DOCTYPE html><html><head></head><body></body></html>"));
        server.start();
        origin = "http://127.0.0.1:" + server.getAddress().getPort();
        proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        handler = new ProxyHandler(new ProxyCookieJar(WebFlutterServer::proxyAllowed));
        proxy.createContext("/proxy", handler);
        proxy.start();
    }

    @AfterEach
    public void stopServers() {
        server.stop(0);
        proxy.stop(0);
    }

    @Test
    public void a_page_with_its_own_base_loads_its_relative_scripts_from_that_base() throws Exception {
        String html = get("/proxy?url=" + enc(origin + "/app/form/index.html")).body();
        String self = "http://localhost:" + proxy.getAddress().getPort();

        assertThat(html).contains("src=\"" + self + "/proxy/" + origin + "/main.js\"");
        assertThat(html).contains("src=\"" + self + "/proxy?url=" + enc(origin + "/polyfills.js") + "\"");
        Matcher firstBase = Pattern.compile("<base href=\"([^\"]*)\"").matcher(html);
        assertThat(firstBase.find()).isTrue();
        assertThat(firstBase.group(1)).isEqualTo(origin + "/");
    }

    @Test
    public void a_request_from_a_page_showing_its_own_path_goes_to_the_page_server() throws Exception {
        get("/proxy?url=" + enc(origin + "/app/form/index.html"));
        String self = "http://localhost:" + proxy.getAddress().getPort();

        assertThat(handler.ownHostTarget(self + "/app/form/index.html", URI.create("/rest/items?page=2")))
                .isEqualTo(origin + "/rest/items?page=2");
    }

    @Test
    public void a_page_without_header_lines_sends_its_script_requests_through_the_proxy() throws Exception {
        String html = get("/proxy?url=" + enc(origin + "/app/plain.html")).body();

        assertThat(html).contains(FETCH_SHIM).doesNotContain(DOM_REWRITE);
    }

    @Test
    public void a_page_with_header_lines_also_rewrites_its_resource_elements() throws Exception {
        String html = get("/proxy?url=" + enc(origin + "/app/plain.html") + "&header=" + enc("X-Token: t")).body();

        assertThat(html).contains(FETCH_SHIM).contains(DOM_REWRITE);
    }

    @Test
    public void a_response_without_a_type_reaches_the_browser_unchanged() throws Exception {
        HttpResponse<byte[]> font = CLIENT.send(request("/proxy?url=" + enc(origin + "/media/icons.woff2")),
                HttpResponse.BodyHandlers.ofByteArray());

        assertThat(font.body()).isEqualTo("wOF2<head>\u0001\u0002".getBytes(StandardCharsets.UTF_8));
        assertThat(font.headers().firstValue("Content-Type")).isEmpty();
    }

    @Test
    public void an_untyped_page_starting_with_whitespace_or_a_bom_is_still_rewritten_as_html() throws Exception {
        assertThat(get("/proxy?url=" + enc(origin + "/untyped/spaced")).body()).contains(FETCH_SHIM);
        assertThat(get("/proxy?url=" + enc(origin + "/untyped/bom")).body()).contains(FETCH_SHIM);
    }

    private HttpResponse<String> get(String pathAndQuery) throws Exception {
        return CLIENT.send(request(pathAndQuery), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest request(String pathAndQuery) {
        return HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + proxy.getAddress().getPort() + pathAndQuery))
                .timeout(Duration.ofSeconds(10)).build();
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void respond(HttpExchange ex, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (contentType != null) ex.getResponseHeaders().add("Content-Type", contentType);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}

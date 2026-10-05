package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The Java half of the static {@link Browser#getCookie}/{@link Browser#setCookie}: what is asked of
 * the Flutter side, and what its reply turns into. The renderer is replaced by a comm that answers
 * each cookie request at once, so this runs without a webview, which the harness cannot always
 * create, leaving the end-to-end cookie tests skipped there.
 */
@Tag("native-unit")
class BrowserCookiesNativeTest {

    private final AnsweringComm comm = new AnsweringComm();
    private Display display;
    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge(comm));
        display = new Display();
        shell = new Shell(display);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("setCookie sends the parsed cookie, HttpOnly and Secure included")
    void setCookie_sendsTheParsedCookie() {
        new Browser(shell, SWT.NONE);
        comm.reply = "true";

        boolean set = Browser.setCookie("session=abc; Domain=example.com; Path=/app; Secure; HttpOnly",
                "https://example.com/app/login");

        assertThat(set).isTrue();
        JsonObject args = comm.lastRequest("setCookie");
        assertThat(args.get("name").getAsString()).isEqualTo("session");
        assertThat(args.get("value").getAsString()).isEqualTo("abc");
        assertThat(args.get("domain").getAsString()).isEqualTo("example.com");
        assertThat(args.get("path").getAsString()).isEqualTo("/app");
        assertThat(args.get("secure").getAsBoolean()).isTrue();
        assertThat(args.get("httpOnly").getAsBoolean()).isTrue();
        assertThat(args.has("expires")).as("a cookie without Max-Age is a session cookie").isFalse();
    }

    @Test
    @DisplayName("a cookie without domain or path takes the URL's host and the root path")
    void setCookie_defaultsDomainAndPathFromTheUrl() {
        new Browser(shell, SWT.NONE);
        comm.reply = "true";

        assertThat(Browser.setCookie("plain=1", "http://localhost:8080")).isTrue();

        JsonObject args = comm.lastRequest("setCookie");
        assertThat(args.get("domain").getAsString()).isEqualTo("localhost");
        assertThat(args.get("path").getAsString()).isEqualTo("/");
        assertThat(args.get("httpOnly").getAsBoolean()).isFalse();
    }

    @Test
    @DisplayName("Max-Age becomes an absolute expiry in seconds since the epoch")
    void setCookie_maxAgeBecomesExpires() {
        new Browser(shell, SWT.NONE);
        comm.reply = "true";
        double before = System.currentTimeMillis() / 1000.0;

        Browser.setCookie("persistent=1; Max-Age=60", "https://example.com/");

        double expires = comm.lastRequest("setCookie").get("expires").getAsDouble();
        assertThat(expires).isBetween(before + 59, System.currentTimeMillis() / 1000.0 + 61);
    }

    @Test
    @DisplayName("setCookie is false when the Flutter side could not set it")
    void setCookie_falseWhenRefused() {
        new Browser(shell, SWT.NONE);
        comm.reply = "false";

        assertThat(Browser.setCookie("session=abc", "https://example.com/")).isFalse();
    }

    @Test
    @DisplayName("getCookie asks for the name and URL and returns the value replied")
    void getCookie_returnsTheReply() {
        new Browser(shell, SWT.NONE);
        comm.reply = "abc";

        String value = Browser.getCookie("JSESSIONID", "https://example.com/web/");

        assertThat(value).isEqualTo("abc");
        JsonObject args = comm.lastRequest("getCookie");
        assertThat(args.get("name").getAsString()).isEqualTo("JSESSIONID");
        assertThat(args.get("url").getAsString()).isEqualTo("https://example.com/web/");
    }

    @Test
    @DisplayName("getCookie is null when there is no such cookie")
    void getCookie_nullWhenAbsent() {
        new Browser(shell, SWT.NONE);
        comm.reply = null;

        assertThat(Browser.getCookie("missing", "https://example.com/")).isNull();
    }

    @Test
    @DisplayName("with no live Browser nothing is sent: getCookie is null and setCookie is false")
    void noLiveBrowser_sendsNothing() {
        new Browser(shell, SWT.NONE).dispose();
        comm.reply = "true";

        assertThat(Browser.getCookie("session", "https://example.com/")).isNull();
        assertThat(Browser.setCookie("session=abc", "https://example.com/")).isFalse();
        assertThat(comm.requests).isZero();
    }

    /** Answers each cookie request with {@link #reply}, as the Flutter side would. */
    private static final class AnsweringComm extends RecordingComm {

        volatile String reply;
        int requests;
        private final java.util.Map<String, JsonObject> lastByOp = new java.util.HashMap<>();

        @Override
        public void send(String eventName, byte[] payload) {
            super.send(eventName, payload);
            String op = eventName.substring(eventName.lastIndexOf('/') + 1);
            if (!op.equals("getCookie") && !op.equals("setCookie")) return;
            requests++;
            JsonObject args = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
            lastByOp.put(op, args);
            Event event = new Event();
            event.text = reply;
            answer("/cookie/" + args.get("reqId").getAsString(), event);
        }

        @SuppressWarnings("unchecked")
        private void answer(String suffix, Event event) {
            handlers.entrySet().stream()
                    .filter(e -> e.getKey().endsWith(suffix))
                    .findFirst()
                    .map(e -> (Consumer<Event>) e.getValue())
                    .orElseThrow(() -> new IllegalStateException("no reply handler for " + suffix))
                    .accept(event);
        }

        JsonObject lastRequest(String op) {
            assertThat(lastByOp).as("a %s request was sent", op).containsKey(op);
            return lastByOp.get(op);
        }
    }
}

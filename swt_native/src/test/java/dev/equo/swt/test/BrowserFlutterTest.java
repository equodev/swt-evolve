package dev.equo.swt.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import dev.equo.swt.harness.BrowserKit;
import dev.equo.swt.harness.FlutterHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.AfterParameterizedClassInvocation;
import org.junit.jupiter.params.BeforeParameterizedClassInvocation;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Harness port of {@code BrowserWebApiTest}: exercises every {@link Browser} API/behavior, but
 * against the <em>Dart-backed</em> Browser rendered by a <b>real Flutter web client</b> driven
 * through {@link FlutterHarness} (instead of the native CEF Browser in a Chromium standalone window).
 *
 * <p>As in the original, each test asserts the expected SWT semantics; a failing/erroring test simply
 * marks that API as not working in this mode — the JUnit report is the support matrix. Everything is
 * offline (a local {@link HttpServer}) and time-bounded so nothing hangs.
 *
 * <h3>How the harness differs from the native suite</h3>
 * <ul>
 *   <li><b>Boot</b>: {@link FlutterHarness#show(Control)} boots a headless
 *       Chrome rendering the Flutter web build and the Dart-backed widget tree, instead of a native
 *       CEF window. Run native instead with {@code -Dharness.client=native}.</li>
 *   <li><b>Navigation is state-driven</b>: {@code setUrl}/{@code setText} mark the widget dirty; the
 *       VBrowser state push is what makes the Flutter webview load. So we {@link FlutterHarness#flush()}
 *       after each {@code setUrl}/{@code setText} to push that state. {@code execute}/{@code evaluate}/
 *       {@code back}/{@code forward} go straight over the comm and need no flush.</li>
 *   <li><b>Listeners</b> fire via {@code Display.asyncExec} (and early-return once the widget is
 *       disposed), so {@link #pumpUntil} drains them by driving {@code display.readAndDispatch()} —
 *       the same loop the native suite used.</li>
 *   <li><b>Read-back</b>: the harness can additionally read the <em>rendered</em> VBrowser state
 *       ({@code url}/{@code text}/{@code javascriptEnabled}) straight off the live Flutter widget via
 *       {@link FlutterHarness#queryState}; the {@code *_reachesRenderedState} tests use that.</li>
 * </ul>
 *
 * <p>Tests are grouped per API area in {@code @Nested} classes; the outer {@code @AfterEach} runs for
 * every nested test. {@code Open/Close window} runs last ({@code @Order}) because
 * {@code window.close()} can tear down the shared window.
 *
 * <pre>./gradlew :swt-evolve:swt_native:webTest                          # Flutter web build + headless Chrome
 * ./gradlew :swt-evolve:swt_native:webTest -Dharness.client=native  # native engine instead</pre>
 */
@Tag("flutter-it")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
// One @ParameterizedClass run covers every (Browser package, proxy) combination in a single
// execution. The render mode (web vs Chromium standalone window) stays the gradle -Dmode.chromium
// switch — it drives JVM launch flags (-XstartOnFirstThread), classpath and CEF init, so it can't
// vary per class within one JVM. PER_CLASS so the per-variant fixture (harness, display, browser)
// lives in instance state, booted fresh in @BeforeParameterizedClassInvocation per (kit, proxy).
@ParameterizedClass(name = "{0}, proxy={1}")
@MethodSource("variants")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BrowserFlutterTest {

    /** The Browser package under test (org.eclipse.swt.browser vs com.equo.chromium.swt). */
    @Parameter(0)
    BrowserKit kit;
    /** Whether the same-origin reverse proxy is enabled (gates the scripting/same-origin tests). */
    @Parameter(1)
    boolean proxy;

    static Stream<Arguments> variants() {
        return Stream.of(
                arguments(Named.of("swt", BrowserKit.swt()), true),
                arguments(Named.of("swt", BrowserKit.swt()), false),
                arguments(Named.of("chromium", BrowserKit.chromium()), true),
                arguments(Named.of("chromium", BrowserKit.chromium()), false));
    }

    FlutterHarness flutter;
    /** Saved {@code dev.equo.swt.web.proxy} so each variant can override it and restore afterwards. */
    private String savedProxyProp;

    protected static final long ACTION_TIMEOUT = 3_000;
    /** A pause used to confirm something did NOT happen (e.g. a blocked navigation). */
    protected static final long NEGATIVE_WAIT = 5_00;
    /**
     * Allow a real fetch + iframe paint + the page's on-load postMessage to arrive — generous because
     * the shared webview's previous (possibly blocked) navigation and the cancellable
     * {@code onNavigationRequest} round-trip can delay the next load.
     */
    protected static final long IFRAME_LOAD_TIMEOUT = 5_000;

    Display display;
    Shell shell;
    private HttpServer server;
    protected String base; // http://localhost:<port>
    /** What the /post server actually received — lets setUrl_withPostData assert server-side
     *  instead of via evaluate() (unsupported on the web iframe backend). */
    private final AtomicReference<String> postBody = new AtomicReference<>();
    private final AtomicReference<String> postHeader = new AtomicReference<>();
    /** Current test's method name, appended to every {@link #url} as ?test= and rendered by the
     *  fixture pages — so when watching the window you can tell a fresh load from a stale one. */
    protected volatile String currentTest = "?";

    /**
     * A single Browser, created <em>before</em> {@link FlutterHarness#show} so it is part of the
     * widget tree the Flutter client renders at ClientReady. (A Browser added <em>after</em> show()
     * is not picked up by the live client — its child would never render, so every navigation would
     * hang.) Tests therefore share one browser and one webview; most start by {@link #load}-ing a
     * known page, and listeners that could leak across tests (e.g. a navigation-blocking
     * {@code changing} listener) are removed in-test.
     */
    BrowserKit.Handle browser;

    @BeforeParameterizedClassInvocation
    void boot() throws Exception {
        System.setProperty("dev.equo.swt.web.crossOriginIsolated", "false");
        // Per-variant proxy: WebFlutterServer reads dev.equo.swt.web.proxy at start() (inside show()),
        // so set it before booting and restore it afterwards. "all" = proxy on, empty = off.
        savedProxyProp = System.getProperty("dev.equo.swt.web.proxy");
        System.setProperty("dev.equo.swt.web.proxy", proxy ? "all" : "");
        startServer();
        // init() wires this harness as the global bridge + Config.forceEquo() BEFORE any widget is
        // created, so the widgets below are Dart-backed and route through the harness.
        flutter = new FlutterHarness();
        flutter.init();
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser flutter test");
        shell.setLayout(new FillLayout());
        shell.setSize(1024, 768);
        browser = kit.newBrowser(shell, SWT.NONE);
        // Lay out the shell so the Browser gets real bounds (FillLayout → fills the client area).
        // Without this the webview renders at 0x0 (NoLayout gives it tightFor(0,0) + a ClipRect), so
        // the page loads but is invisible. Must happen before show() so the initial render has bounds.
        shell.open();
        shell.layout(true, true);
        flutter.show(shell); // boot the Flutter web client for the shell + browser, await ClientReady
    }

    /** The Browser must be laid out to a non-zero size, else the webview renders blank (see boot()). */
    @Test
    @DisplayName("browser is laid out to a visible size")
    void browser_isRenderedWithSize() {
        Map<String, Object> resp = flutter.queryState(browser.raw());
        assertThat(resp.get("found")).as("Browser is rendered in the Flutter client").isEqualTo(true);
        Object bounds = state(resp).get("bounds");
        assertThat(bounds).as("Browser has rendered bounds").isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> b = (Map<String, Object>) bounds;
        assertThat(((Number) b.get("width")).intValue()).as("rendered width").isGreaterThan(0);
        assertThat(((Number) b.get("height")).intValue()).as("rendered height").isGreaterThan(0);
    }

    @AfterParameterizedClassInvocation
    void teardown() {
        if (display != null && !display.isDisposed()) display.dispose();
        if (server != null) server.stop(0);
        if (flutter != null) flutter.teardown();
        if (savedProxyProp == null) System.clearProperty("dev.equo.swt.web.proxy");
        else System.setProperty("dev.equo.swt.web.proxy", savedProxyProp);
    }

    @BeforeEach
    void captureTestName(TestInfo info) {
        currentTest = info.getTestMethod().map(m -> m.getName()).orElse(info.getDisplayName());
    }

    @AfterEach
    void drain() {
        // Drain the browser-listener callbacks DartBrowser delivers via Display.asyncExec so a late
        // one can't fire during the next test.
        while (display.readAndDispatch()) { /* drain queued asyncExec runnables */ }
    }

    /**
     * True only in Chromium standalone-window mode ({@code -Ddev.equo.swt.mode=chromium}). The popup
     * window events (open/close/visibility) and the HTTP-auth dialog originate from the
     * {@code ChromiumStandaloneLauncher}'s real OS window; with a headless web/native renderer
     * ({@code isChromium=false}) there is no such window, so those tests can't run.
     */
    protected static boolean isChromium() {
        return "chromium".equalsIgnoreCase(System.getProperty("dev.equo.swt.mode"));
    }

    private static void assumeChromium() {
        Assumptions.assumeTrue(isChromium(),
                "requires Chromium standalone-window mode (-Ddev.equo.swt.mode=chromium)");
    }

    /**
     * Skip in the proxy-off variant. The scripting / same-origin features (evaluate, execute,
     * BrowserFunction, Title/StatusText, and rendering external URLs in the iframe) only work when
     * the same-origin reverse proxy is enabled; without it the cross-origin iframe can't be scripted.
     */
    private void assumeProxy() {
        Assumptions.assumeTrue(proxy, "requires the same-origin proxy (proxy=true variant)");
    }

    /**
     * Asserts the embedded page <em>actually renders</em> in the webview — not merely that the URL
     * reached the Flutter state. The loaded page posts an on-load ping (relayed to Java); a page that
     * never runs (e.g. a cross-origin {@code <iframe>} refused by COOP/COEP) yields no ping and fails.
     *
     * <p>Runs first ({@code @Order(0)}) on a clean webview: a blocked cross-origin load wedges the
     * single shared webview for subsequent loads, so {@code setText} (same-origin, must render) runs
     * before {@code setUrl} (cross-origin, may be blocked).
     */
    @Nested
    @Order(0)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Rendering {

        @Test
        @Order(1)
        @DisplayName("setText content renders in the iframe (validates the load-detection handshake)")
        void setText_actuallyRendersPage() {
            // loadHtmlString renders same-origin content, so the page runs and posts its on-load
            // ping — proving both that the embedded page truly renders and that the detection path
            // (page → parent postMessage → Java) works end to end.
            String html = taggedHtml("Inline Page", "HELLO");
            flutter.clearIframeLoads();
            browser.setText(html);
            flutter.flush();
            assertThat(flutter.awaitIframeRendered("Inline Page", IFRAME_LOAD_TIMEOUT))
                    .as("setText content rendered in the iframe (on-load ping received)")
                    .isTrue();
        }

        @Test
        @Order(2)
        @DisplayName("setUrl page renders in the iframe (fails if blocked, e.g. cross-origin)")
        void setUrl_actuallyRendersPage() {
            // Without the proxy /a is cross-origin and refused by COOP/COEP, so it can't render here.
            assumeProxy();
            flutter.clearIframeLoads();
            browser.setUrl(url("/a"));
            flutter.flush();
            // The page posts its on-load ping only if it truly ran in the webview. A cross-origin
            // iframe refused by COOP/COEP (or X-Frame-Options) never does, so this fails — unlike
            // setUrl_reachesRenderedState, which only checks the URL reached the Flutter state.
            assertThat(flutter.awaitIframeRendered("Page A", IFRAME_LOAD_TIMEOUT))
                    .as("embedded /a rendered in the iframe (on-load ping received)")
                    .isTrue();
        }
    }

    @Nested
    @Order(1)
    class Navigation {

        @Test
        void setUrl_getUrl() {
            load(browser, url("/a"));
            assertThat(browser.getUrl()).contains("/a");
        }

        @Test
        void setUrl_reachesRenderedState() {
            browser.setUrl(url("/a"));
            flutter.flush();
            // The rendered VBrowser url settles asynchronously, and a late navigationState push from
            // the previous test's navigation can transiently leave a stale url, so poll until /a is
            // reflected rather than reading once.
            AtomicReference<Object> renderedUrl = new AtomicReference<>();
            boolean reached = pumpUntil(() -> {
                Map<String, Object> r = flutter.queryState(browser.raw());
                if (!Boolean.TRUE.equals(r.get("found"))) return false;
                renderedUrl.set(state(r).get("url"));
                return renderedUrl.get() != null && renderedUrl.get().toString().contains("/a");
            }, ACTION_TIMEOUT);
            assertThat(reached).as("url reached the live Flutter widget (was " + renderedUrl.get() + ")").isTrue();
            // Visual aid: -Dharness.holdMs=8000 -Dharness.web.headless=false keeps the window open so
            // the iframe has time to fetch + paint /a (otherwise teardown kills Chrome in ~300ms).
            holdOpen();
        }

        @Test
        void back_returnsToPreviousPage() {
            load(browser, url("/a"));
            load(browser, url("/b"));
            assertThat(browser.back()).isTrue();
            // Poll getUrl() (the assertion target, pushed via navigationState) rather than a
            // changed-listener: the back navigation reliably updates getUrl, while waiting on a
            // single changed event was flaky under CI load.
            pumpUntil(() -> browser.getUrl() != null && browser.getUrl().contains("/a"), ACTION_TIMEOUT);
            assertThat(browser.getUrl()).contains("/a");
        }

        @Test
        void forward_returnsToNextPage() {
            load(browser, url("/a"));
            load(browser, url("/b"));
            browser.back();
            pumpUntil(() -> browser.isForwardEnabled(), ACTION_TIMEOUT);
            assertThat(browser.forward()).isTrue();
            pumpUntil(() -> browser.getUrl() != null && browser.getUrl().contains("/b"), ACTION_TIMEOUT);
            assertThat(browser.getUrl()).contains("/b");
        }

        @Test
        void isBackEnabled_trueAfterNavigation() {
            load(browser, url("/a"));
            load(browser, url("/b"));
            assertThat(browser.isBackEnabled()).isTrue();
        }

        @Test
        void isForwardEnabled_trueAfterBack() {
            load(browser, url("/a"));
            load(browser, url("/b"));
            browser.back();
            pumpUntil(() -> browser.isForwardEnabled(), ACTION_TIMEOUT);
            assertThat(browser.isForwardEnabled()).isTrue();
        }

        @Test
        void refresh_firesCompletedAgain() {
            load(browser, url("/a"));
            CountDownLatch again = new CountDownLatch(1);
            ProgressListener pl = ProgressListener.completedAdapter(e -> again.countDown());
            browser.addProgressListener(pl);
            browser.refresh();
            assertThat(awaitLatch(again, ACTION_TIMEOUT)).as("completed after refresh()").isTrue();
        }

        @Test
        void stop_doesNotThrow() {
            load(browser, url("/a"));
            browser.stop();
        }

        @Test
        void setUrl_withPostData() throws Exception {
            postBody.set(null);
            postHeader.set(null);
            CountDownLatch done = new CountDownLatch(1);
            ProgressListener pl = ProgressListener.completedAdapter(e -> done.countDown());
            browser.addProgressListener(pl);
            try {
                browser.setUrl(url("/post"), "field=equo", new String[] { "X-Equo: test" });
                flutter.flush();
                awaitLatch(done, ACTION_TIMEOUT);
            } finally {
                browser.removeProgressListener(pl);
            }
            // Assert server-side that the POST carried the body + header, rather than reading it
            // back with evaluate() (the web iframe backend doesn't support JS execution).
            pumpUntil(() -> postBody.get() != null, ACTION_TIMEOUT);
            assertThat(postBody.get()).as("POST body received by the server").contains("field=equo");
            assertThat(postHeader.get()).as("custom header received by the server").contains("test");
        }

        @Test
        void redirect_followsToFinalUrl() {
            // The final-URL read-back relies on the proxy resolving the redirected (same-origin) URL.
            assumeProxy();
            load(browser, url("/a"));
            AtomicReference<String> loc = new AtomicReference<>();
            browser.addLocationListener(LocationListener.changedAdapter(e -> loc.set(e.location)));
            browser.setUrl(url("/redirect"));
            flutter.flush();
            pumpUntil(() -> loc.get() != null && loc.get().contains("/b"), ACTION_TIMEOUT);
            assertThat(loc.get()).as("redirect resolves to the final /b URL").contains("/b");
        }
    }

    @Nested
    @Order(2)
    class Content {

        @Test
        void setText_getText() {
            String html = taggedHtml("T", "<p id='m'>MARKER-12345</p>");
            CountDownLatch done = new CountDownLatch(1);
            browser.addProgressListener(ProgressListener.completedAdapter(e -> done.countDown()));
            assertThat(browser.setText(html)).isTrue();
            flutter.flush();
            awaitLatch(done, ACTION_TIMEOUT);
            assertThat(browser.getText()).contains("MARKER-12345");
        }

        @Test
        void setText_trusted() {
            String html = taggedHtml("T", "TRUSTED-OK");
            CountDownLatch done = new CountDownLatch(1);
            browser.addProgressListener(ProgressListener.completedAdapter(e -> done.countDown()));
            assertThat(browser.setText(html, true)).isTrue();
            flutter.flush();
            awaitLatch(done, ACTION_TIMEOUT);
            assertThat(browser.getText()).contains("TRUSTED-OK");
        }

        @Test
        void setText_reachesRenderedState() {
            String html = taggedHtml("T", "RENDERED-MARKER");
            browser.setText(html);
            flutter.flush();
            // Poll the rendered state rather than reading once: it settles asynchronously and a late
            // push from a prior test's navigation can transiently leave a stale value.
            AtomicReference<Object> renderedText = new AtomicReference<>();
            boolean reached = pumpUntil(() -> {
                Map<String, Object> r = flutter.queryState(browser.raw());
                if (!Boolean.TRUE.equals(r.get("found"))) return false;
                renderedText.set(state(r).get("text"));
                return renderedText.get() != null && renderedText.get().toString().contains("RENDERED-MARKER");
            }, ACTION_TIMEOUT);
            assertThat(reached).as("text reached the live Flutter widget (was " + renderedText.get() + ")").isTrue();
        }

    }

    // PER_CLASS so /a is loaded ONCE for the whole group (@BeforeAll), not per test: every test here
    // just reads back via evaluate() against the same loaded page, so re-navigating + awaiting the
    // on-load ping per test added ~2s each for no benefit.
    @Nested
    @Order(3)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Scripting {

        // Load /a once and wait until the page actually rendered+ran (its on-load ping) before any
        // evaluate(). load()'s Progress/completed alone is flaky: a stale completed from a prior
        // navigation can satisfy it while the iframe is still mid-load, so evaluate() scripts a
        // transitional/cross-origin frame and throws. The ping is keyed to the page ("Page A"), so it
        // can't be satisfied by a stale event — see loadScriptablePageA().
        @BeforeAll
        void loadScriptablePage() {
            assumeProxy();
            loadScriptablePageA();
        }

        // evaluate() must honour the SWT type contract: a JS value maps to
        //   null/undefined -> null,  boolean -> Boolean,  number -> Double,
        //   string -> String,  array -> Object[] (elements mapped recursively).
        // The tests below cover each, easiest (scalars) to hardest (nested arrays).

        @Test
        void execute_thenEvaluateReadsBack() throws Exception {
            assertThat(browser.execute("window.__equo = 42;")).isTrue();
            flutter.pumpClient();
            assertThat(browser.evaluate("window.__equo")).isEqualTo(42.0);
        }

        @Test
        void evaluate_arithmetic() throws Exception {
            assertThat(browser.evaluate("1+1")).isEqualTo(2.0);
        }

        @Test
        void evaluate_trusted() throws Exception {
            assertThat(browser.evaluate("2*21", true)).isEqualTo(42.0);
        }

        @Test
        void evaluate_numberMapsToDouble_swtContract() throws Exception {
            // A JS number must come back as java.lang.Double.
            assertThat(browser.evaluate("1+1")).isInstanceOf(Double.class);
        }

        @Test
        void evaluate_null() throws Exception {
            assertThat(browser.evaluate("null")).isNull();
        }

        @Test
        void evaluate_undefined() throws Exception {
            // JS undefined has no value; SWT maps it to Java null.
            assertThat(browser.evaluate("void 0")).isNull();
        }

        @Test
        void evaluate_boolean() throws Exception {
            assertThat(browser.evaluate("1 < 2")).isEqualTo(Boolean.TRUE);
            assertThat(browser.evaluate("1 > 2")).isEqualTo(Boolean.FALSE);
        }

        @Test
        void evaluate_string() throws Exception {
            assertThat(browser.evaluate("'he\"llo\\n'")).isEqualTo("he\"llo\n");
        }

        @Test
        void evaluate_array() throws Exception {
            Object r = browser.evaluate("[1, 2, 3]");
            assertThat(r).isInstanceOf(Object[].class);
            assertThat((Object[]) r).containsExactly(1.0, 2.0, 3.0);
        }

        @Test
        void evaluate_mixedArray() throws Exception {
            Object r = browser.evaluate("['a', true, 7, null]");
            assertThat(r).isInstanceOf(Object[].class);
            assertThat((Object[]) r).containsExactly("a", Boolean.TRUE, 7.0, null);
        }

        @Test
        void evaluate_nestedArray() throws Exception {
            Object r = browser.evaluate("[1, ['a', true], null]");
            assertThat(r).isInstanceOf(Object[].class);
            Object[] arr = (Object[]) r;
            assertThat(arr[0]).isEqualTo(1.0);
            assertThat(arr[1]).isInstanceOf(Object[].class);
            assertThat((Object[]) arr[1]).containsExactly("a", Boolean.TRUE);
            assertThat(arr[2]).isNull();
        }
    }

    @Nested
    @Order(4)
    class State {

        @Test
        void getBrowserType_nonEmpty() {
            assertThat(browser.getBrowserType()).isNotEmpty();
        }

        @Test
        void javascriptEnabled_roundTrip() {
            browser.setJavascriptEnabled(false);
            assertThat(browser.getJavascriptEnabled()).isFalse();
            browser.setJavascriptEnabled(true);
            assertThat(browser.getJavascriptEnabled()).isTrue();
        }
    }

    @Nested
    @Order(5)
    @DisplayName("Cookies (static)")
    class Cookies {

        @Test
        void setCookie_getCookie_roundTrip() {
            boolean set = kit.setCookie("equoTest=1; path=/", base);
            Assumptions.assumeTrue(set, "static cookie API not functional in this runtime");
            assertThat(kit.getCookie("equoTest", base)).isEqualTo("1");
        }

        @Test
        void clearSessions_doesNotThrow() {
            kit.clearSessions();
        }
    }

    @Nested
    @Order(6)
    @DisplayName("LocationListener")
    class Location {

        @Test
        @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
        void locationChanging_canBlockNavigation() {
            load(browser, url("/a"));
            AtomicReference<String> seen = new AtomicReference<>();
            // Shared browser: remove this navigation-blocking listener afterwards so it can't block
            // /b in later tests.
            LocationListener blocker = LocationListener.changingAdapter(e -> {
                seen.set(e.location);
                if (e.location != null && e.location.contains("/b")) e.doit = false;
            });
            CountDownLatch reachedB = new CountDownLatch(1);
            LocationListener watcher = LocationListener.changedAdapter(e -> {
                if (e.location != null && e.location.contains("/b")) reachedB.countDown();
            });
            browser.addLocationListener(blocker);
            browser.addLocationListener(watcher);
            try {
                browser.setUrl(url("/b"));
                flutter.flush();
                pumpUntil(() -> reachedB.getCount() == 0, NEGATIVE_WAIT);
                assertThat(seen.get()).as("changing fired for /b").contains("/b");
                assertThat(reachedB.getCount()).as("navigation to /b should be blocked (doit=false)").isEqualTo(1);
            } finally {
                browser.removeLocationListener(blocker);
                browser.removeLocationListener(watcher);
            }
        }

        @Test
        void locationChanging_allowsNavigation() {
            load(browser, url("/a"));
            AtomicBoolean fired = new AtomicBoolean();
            browser.addLocationListener(LocationListener.changingAdapter(e -> fired.set(true)));
            CountDownLatch done = new CountDownLatch(1);
            browser.addProgressListener(ProgressListener.completedAdapter(e -> done.countDown()));
            browser.setUrl(url("/b"));
            flutter.flush();
            assertThat(awaitLatch(done, ACTION_TIMEOUT)).as("navigation completes when allowed").isTrue();
            assertThat(fired).isTrue();
        }

        @Test
        void locationChanged_fires() {
            load(browser, url("/a"));
            AtomicReference<String> loc = new AtomicReference<>();
            browser.addLocationListener(LocationListener.changedAdapter(e -> loc.set(e.location)));
            browser.setUrl(url("/b"));
            flutter.flush();
            pumpUntil(() -> loc.get() != null && loc.get().contains("/b"), ACTION_TIMEOUT);
            assertThat(loc.get()).contains("/b");
        }
    }

    @Nested
    @Order(7)
    @DisplayName("ProgressListener")
    class Progress {

        @Test
        void progressChanged_fires() {
            AtomicBoolean changed = new AtomicBoolean();
            browser.addProgressListener(ProgressListener.changedAdapter(e -> changed.set(true)));
            browser.setUrl(url("/a"));
            flutter.flush();
            pumpUntil(changed::get, ACTION_TIMEOUT);
            assertThat(changed).isTrue();
        }

        @Test
        void progressCompleted_fires() {
            CountDownLatch done = new CountDownLatch(1);
            browser.addProgressListener(ProgressListener.completedAdapter(e -> done.countDown()));
            browser.setUrl(url("/a"));
            flutter.flush();
            assertThat(awaitLatch(done, ACTION_TIMEOUT)).isTrue();
        }
    }

    @Nested
    @Order(8)
    @DisplayName("TitleListener")
    class Title {

        @BeforeEach
        void requireProxy() { assumeProxy(); }

        @Test
        void titleChanged_fires() {
            AtomicReference<String> title = new AtomicReference<>();
            browser.addTitleListener((TitleListener) e -> title.set(e.title));
            browser.setUrl(url("/a"));
            flutter.flush();
            pumpUntil(() -> "Page A".equals(title.get()), ACTION_TIMEOUT);
            assertThat(title.get()).isEqualTo("Page A");
        }
    }

    @Nested
    @Order(9)
    @DisplayName("StatusTextListener")
    class StatusText {

        // Needs a scriptable frame (execute dispatches a DOM event); see loadScriptablePageA().
        @BeforeEach
        void loadScriptablePage() {
            assumeProxy();
            loadScriptablePageA();
        }

        @Test
        void statusTextChanged_fires() {
            AtomicReference<String> status = new AtomicReference<>();
            browser.addStatusTextListener(e -> status.set(e.text));
            // window.status read-back is finicky in headless Chrome and an execute can momentarily
            // hit a not-yet-ready frame, so re-issue it until the StatusText event lands. Vary the
            // value each attempt so the Dart-side change-dedup can't swallow a retry.
            boolean fired = false;
            for (int attempt = 0; attempt < 10 && !fired; attempt++) {
                browser.execute("window.status='hovered-A-" + attempt + "';");
                fired = pumpUntil(() -> status.get() != null && !status.get().isEmpty(), 500);
            }
            assertThat(fired).as("StatusTextListener.changed fired").isTrue();
        }
    }

    @Nested
    @Order(10)
    @DisplayName("AuthenticationListener")
    class Authentication {

        @Test
        void authentication_fires() {
            // Unsupported in every mode: the web iframe backend has no 401 hook, and the Chromium
            // standalone API (ISubscriber) exposes no auth-credentials event — HTTP auth needs CEF
            // RequestHandler.getAuthCredentials, which isn't surfaced. Skip (not a failure).
            Assumptions.abort("HTTP authentication not supported by the Dart/CEF Browser backend");
            AtomicBoolean fired = new AtomicBoolean();
            browser.addAuthenticationListener(e -> {
                fired.set(true);
                e.user = "user";
                e.password = "passwd";
            });
            browser.setUrl(url("/auth"));
            flutter.flush();
            pumpUntil(fired::get, ACTION_TIMEOUT);
            assertThat(fired).as("AuthenticationListener.authenticate fires on a 401").isTrue();
        }
    }

    // Run last (@Order): window.close() can tear down the shared window and break subsequent loads.
    @Nested
    @Order(Integer.MAX_VALUE)
    @DisplayName("Open/Close window")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OpenCloseWindow {

        @Test
        @Order(1)
        void visibilityWindow_fires() {
            assumeChromium();
            AtomicBoolean shown = new AtomicBoolean();
            browser.addVisibilityWindowListener(VisibilityWindowListener.showAdapter(e -> shown.set(true)));
            browser.addVisibilityWindowListener(VisibilityWindowListener.hideAdapter(e -> { }));
            browser.setUrl(url("/open"));
            flutter.flush();
            pumpUntil(shown::get, ACTION_TIMEOUT);
            assertThat(shown).as("VisibilityWindow.show fires for a popup window").isTrue();
        }

        @Test
        @Order(2)
        void openWindow_fires() {
            assumeChromium();
            AtomicBoolean opened = new AtomicBoolean();
            browser.addOpenWindowListener(() -> opened.set(true));
            browser.setUrl(url("/open"));
            flutter.flush();
            pumpUntil(opened::get, ACTION_TIMEOUT);
            assertThat(opened).as("OpenWindowListener.open fires on window.open()").isTrue();
        }

        @Test
        @Order(3)
        void openWindow_blocksPopup() {
            assumeChromium();
            AtomicBoolean opened = new AtomicBoolean();
            AtomicBoolean shown = new AtomicBoolean();
            // An OpenWindowListener that provides no Browser to host the popup blocks it (SWT
            // semantics) — the application's way to veto window.open, analogous to
            // LocationListener.changing clearing doit. The kit's listener is observe-only (it never
            // sets event.browser), so it always blocks.
            browser.addOpenWindowListener(() -> opened.set(true));
            // Contrast with visibilityWindow_fires (no OpenWindowListener → popup allowed → a window
            // is created → VisibilityWindow.show fires). When blocked, no window is created, so show
            // must NOT fire.
            browser.addVisibilityWindowListener(VisibilityWindowListener.showAdapter(e -> shown.set(true)));
            browser.setUrl(url("/open"));
            flutter.flush();
            assertThat(pumpUntil(opened::get, ACTION_TIMEOUT))
                    .as("OpenWindowListener.open fires on window.open()").isTrue();
            assertThat(pumpUntil(shown::get, NEGATIVE_WAIT))
                    .as("a blocked popup creates no window → VisibilityWindow.show must not fire").isFalse();
        }

        @Test
        @Order(4)
        void closeWindow_fires() {
            assumeChromium();
            // BLOCKER (embedded iframe model): the Browser's content runs in an iframe inside the
            // Chromium window, and Chromium ignores window.close() for any window it didn't open via
            // script — so the iframe cannot close itself and CEF's onBeforeClose never fires. The
            // CloseWindowListener wiring (EvolveBrowser#onPopupClosed via onBeforeClose) is correct
            // for a real script-opened popup, but this fixture's iframe-self-close can't exercise it.
            Assumptions.abort("CloseWindowListener: an embedded iframe cannot self-close "
                    + "(Chromium ignores window.close() for non-script-opened windows); "
                    + "onBeforeClose only fires for real popup windows.");
            AtomicBoolean closed = new AtomicBoolean();
            browser.addCloseWindowListener((CloseWindowListener) e -> closed.set(true));
            browser.setUrl(url("/close"));
            flutter.flush();
            pumpUntil(closed::get, ACTION_TIMEOUT);
            assertThat(closed).as("CloseWindowListener.close fires on window.close()").isTrue();
        }
    }

    @Nested
    @Order(11)
    @DisplayName("BrowserFunction")
    class BrowserFunctions {

        @BeforeEach
        void requireProxy() { assumeProxy(); }

        @Test
        void browserFunction_invokedFromJs() throws Exception {
            load(browser, url("/a"));
            AtomicBoolean fired = new AtomicBoolean();
            browser.newFunction("equoFn", arguments -> {
                fired.set(true);
                return "ok:" + (arguments != null && arguments.length > 0 ? arguments[0] : "");
            });
//            pumpUntil(() -> false, 300);
            flutter.pumpClient();
            Object result = browser.evaluate("window.equoFn ? window.equoFn('hi') : 'NONE'");
            assertThat(fired).as("BrowserFunction Java callback invoked from JS").isTrue();
            assertThat(result).isEqualTo("ok:hi");
        }

        @Test
        void browserFunction_metadata() {
            BrowserKit.Fn fn = browser.newFunction("equoMeta", a -> null);
            assertThat(fn.getName()).isEqualTo("equoMeta");
            assertThat(fn.getBrowser()).isSameAs(browser.raw());
            assertThat(fn.isDisposed()).isFalse();
            fn.dispose();
            assertThat(fn.isDisposed()).isTrue();
        }
    }

    /**
     * {@code close()}/{@code dispose()} are pure Java widget-lifecycle operations (no render or
     * navigation needed), so these use a <em>transient</em> Browser rather than the shared one — which
     * must stay alive for the other groups. {@code addMenuDetectListener} is a registration smoke
     * check: a context-menu gesture can't be produced headlessly, so only the wiring is verified.
     */
    @Nested
    @Order(12)
    @DisplayName("Lifecycle & menu-detect")
    class Lifecycle {

        @Test
        @DisplayName("close() succeeds and disposes the browser")
        void close_disposesBrowser() {
            BrowserKit.Handle b = kit.newBrowser(shell, SWT.NONE);
            // SWT contract: close() returns true unless a beforeunload handler vetoes; on success the
            // widget is disposed (DartBrowser.close → dispose()).
            assertThat(b.close()).as("close() returns true (not vetoed)").isTrue();
            assertThat(b.isDisposed()).as("close() disposes the Browser").isTrue();
        }

        @Test
        @DisplayName("dispose() marks the browser disposed and is idempotent")
        void dispose_marksDisposed() {
            BrowserKit.Handle b = kit.newBrowser(shell, SWT.NONE);
            assertThat(b.isDisposed()).isFalse();
            b.dispose();
            assertThat(b.isDisposed()).as("disposed after dispose()").isTrue();
            b.dispose(); // second dispose must be a no-op, not throw
        }

        @Test
        @DisplayName("add/removeMenuDetectListener register without error")
        void menuDetectListener_registers() {
            BrowserKit.Handle b = kit.newBrowser(shell, SWT.NONE);
            try {
                MenuDetectListener l = e -> { };
                b.addMenuDetectListener(l);
                b.removeMenuDetectListener(l);
            } finally {
                b.dispose();
            }
        }
    }

    // ------------------------------------------------------------------ harness event-loop helpers

    /**
     * Pumps the SWT event loop until {@code cond} is true or the timeout elapses. Driving
     * {@code display.readAndDispatch()} is what delivers the browser's {@code asyncExec}'d listener
     * callbacks; the short sleep when idle yields for comm messages arriving on the WS thread.
     */
    protected boolean pumpUntil(BooleanSupplier cond, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (!cond.getAsBoolean() && System.currentTimeMillis() < end) {
            // Advance the renderer's loop too: under chromium-standalone on mac, CEF is
            // single-threaded and only progresses when pumped, so without this a page navigated
            // during this wait would never finish loading (readAndDispatch only drives SWT).
            flutter.pumpClient();
            if (!display.readAndDispatch()) {
                try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }
        return cond.getAsBoolean();
    }

    protected boolean awaitLatch(CountDownLatch latch, long timeoutMs) {
        return pumpUntil(() -> latch.getCount() == 0, timeoutMs);
    }

    /**
     * Keeps the Flutter client alive (pumping the loop) for {@code -Dharness.holdMs} so a developer
     * can watch the rendered Browser before teardown kills the client. No-op unless the property is
     * set; pair with {@code -Dharness.web.headless=false} to actually see the window.
     */
    protected void holdOpen() {
        long ms = Long.getLong("harness.holdMs", 0);
        if (ms > 0) pumpUntil(() -> false, ms);
    }

    /**
     * Loads {@code url} and waits for {@code ProgressListener.completed}; retries once, then fails.
     * Unlike the native suite, {@code setUrl} only marks the widget dirty — {@link FlutterHarness#flush()}
     * pushes that state to the Flutter client, which is what actually triggers the navigation.
     */
    protected void load(BrowserKit.Handle b, String url) {
        CountDownLatch done = new CountDownLatch(1);
        ProgressListener pl = ProgressListener.completedAdapter(e -> done.countDown());
        b.addProgressListener(pl);
        try {
            b.setUrl(url);
            flutter.flush();
            if (awaitLatch(done, ACTION_TIMEOUT)) return;
        } finally {
            b.removeProgressListener(pl);
        }
        throw new AssertionError("Timed out loading " + url);
    }

    /**
     * Navigate to {@code /a} and block until the page <em>actually rendered in the iframe</em>, using
     * its own on-load ping (posted by the page via {@code postMessage}; see {@link
     * FlutterHarness#awaitIframeRendered}). Unlike {@code Progress/completed}, the ping is keyed to the
     * specific page ({@code "Page A"}), so it can't be satisfied by a stale completed from a prior
     * navigation — which was letting the scripting tests run {@code evaluate()} against a frame that
     * was still mid-load (and so throw). It's a non-circular signal (no {@code evaluate}) for the
     * scripting tests, which then can rely on the frame being loaded and scriptable.
     */
    protected void loadScriptablePageA() {
        flutter.clearIframeLoads();
        browser.setUrl(url("/a"));
        flutter.flush();
        assertThat(flutter.awaitIframeRendered("Page A", IFRAME_LOAD_TIMEOUT))
                .as("/a rendered in the iframe (on-load ping received)").isTrue();
    }

    protected String url(String path) {
        // Tag every URL with the current test name so the fixture page can render it: a distinct URL
        // per test also defeats any same-URL nav dedup/cache, forcing a real reload each test.
        String sep = path.contains("?") ? "&" : "?";
        return base + path + sep + "test=" + currentTest;
    }

    /**
     * The setText analogue of the served {@link #page} pages: wraps {@code inner} with the given
     * title, the current-test banner (so setText content also shows which test set it — to spot
     * stale renders), and the on-load ping (so {@link FlutterHarness#awaitIframeRendered} works).
     */
    protected String taggedHtml(String title, String inner) {
        String banner = "<div style='position:fixed;top:0;left:0;right:0;background:#0ff;color:#000;"
                + "font:13px monospace;padding:2px 6px;z-index:2147483647'>setText &mdash; test="
                + currentTest + "</div>";
        return "<!doctype html><html><head><title>" + title + "</title></head><body>"
                + banner + inner + LOAD_PING + "</body></html>";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> state(Map<String, Object> queryResponse) {
        Object s = queryResponse.get("state");
        assertThat(s).as("queryResponse has a 'state' (rendered VBrowser.toJson())").isNotNull();
        return (Map<String, Object>) s;
    }

    // ------------------------------------------------------------------ offline fixture server

    private void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        page(server, "/a", "Page A",
                "<h1 id='h'>A</h1><p id='marker'>MARKER-A</p>"
              + "<a id='link' href='" + "REL/b'>to B</a>"
              + "<a id='statusLink' href='#' onmouseover=\"window.status='hovered-A'\">hover</a>");
        page(server, "/b", "Page B", "<h1 id='h'>B</h1><p id='marker'>MARKER-B</p>");
        page(server, "/open", "Opener",
                "<h1>opener</h1><script>setTimeout(function(){window.open('REL/b');}, 50);</script>");
        page(server, "/close", "Closer",
                "<h1>closer</h1><script>setTimeout(function(){window.close();}, 50);</script>");

        server.createContext("/post", ex -> {
            if ("OPTIONS".equals(ex.getRequestMethod())) {
                // CORS preflight for the cross-origin POST carrying a custom header.
                ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                ex.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
                ex.sendResponseHeaders(204, -1);
                ex.close();
                return;
            }
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String hdr = String.join(",", ex.getRequestHeaders().getOrDefault("X-Equo", List.of()));
            postBody.set(body);
            postHeader.set(hdr);
            respond(ex, 200, "text/html",
                    "<!doctype html><html><head><title>Post</title></head><body>"
                            + "<pre id='body'>" + body + "</pre><pre id='hdr'>" + hdr + "</pre></body></html>");
        });
        server.createContext("/redirect", ex -> {
            ex.getResponseHeaders().add("Location", base + "/b");
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        server.createContext("/auth", ex -> {
            List<String> auth = ex.getRequestHeaders().getOrDefault("Authorization", List.of());
            if (auth.isEmpty()) {
                ex.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"equo\"");
                ex.sendResponseHeaders(401, -1);
                ex.close();
            } else {
                respond(ex, 200, "text/html", "<html><head><title>Authed</title></head><body>AUTHED</body></html>");
            }
        });
        server.createContext("/setcookie", ex -> {
            ex.getResponseHeaders().add("Set-Cookie", "equoServer=1; Path=/");
            respond(ex, 200, "text/html", "<html><head><title>Cookie set</title></head><body>SET</body></html>");
        });
        server.createContext("/cookies", ex -> {
            String cookie = String.join("; ", ex.getRequestHeaders().getOrDefault("Cookie", List.of()));
            respond(ex, 200, "text/html",
                    "<html><head><title>Cookies</title></head><body><pre id='cookies'>" + cookie + "</pre></body></html>");
        });
        server.setExecutor(null);
        server.start();
        base = "http://localhost:" + server.getAddress().getPort();
    }

    private static void page(HttpServer s, String path, String title, String body) {
        s.createContext(path, ex -> {
            // Render the requesting test's name (from ?test=) in a fixed banner so a watcher can see
            // whether the page shown belongs to the current test or is a stale leftover.
            String test = queryParam(ex, "test");
            String banner = "<div style='position:fixed;top:0;left:0;right:0;background:#ff0;color:#000;"
                    + "font:13px monospace;padding:2px 6px;z-index:2147483647'>" + path
                    + " &mdash; test=" + test + "</div>";
            respond(ex, 200, "text/html",
                    "<!doctype html><html><head><title>" + title + "</title></head><body>"
                            + banner + body.replace("REL", "") + LOAD_PING + "</body></html>");
        });
    }

    /** Reads a single query parameter from the request URI (no decoding needed — values are test
     *  method names: alphanumeric + underscore). Returns "" when absent. */
    private static String queryParam(HttpExchange ex, String name) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return "";
        for (String kv : q.split("&")) {
            int i = kv.indexOf('=');
            if (i > 0 && kv.substring(0, i).equals(name)) return kv.substring(i + 1);
        }
        return "";
    }

    /**
     * Posted by every fixture page once it actually runs in the webview. The harness relays it to
     * Java (see test_harness_iframe_web.dart); a page blocked from loading (e.g. cross-origin iframe
     * refused) never executes this, so {@link FlutterHarness#awaitIframeRendered} times out.
     */
    private static final String LOAD_PING =
            "<script>try{parent.postMessage('equo-iframe-loaded:'+document.title,'*')}catch(e){}</script>";

    private static void respond(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", type);
        // Allow this cross-origin iframe content to load alongside the Flutter app.
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}

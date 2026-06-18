package dev.equo.internal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.equo.chromium.swt.Browser;
import com.equo.chromium.swt.BrowserFunction;
import com.equo.chromium.swt.OpenWindowListener;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Interactive test harness for the {@link com.equo.chromium.swt.Browser} public API.
 * <p>
 * Each Browser-declared API (listeners, direct methods, statics and {@link BrowserFunction}) gets a
 * row with a <b>Test</b>/<b>Trigger</b> button and a PASS/FAIL/UNKNOWN badge. The detail pane shows
 * what each call is expected to do; the console logs every call result and every event that fires.
 * Listeners whose outcome depends on event fields expose inline controls so the effect of mutating
 * the event (e.g. {@code changing.doit}) is visible.
 * <p>
 * Inherited {@code Composite}/{@code Control} API is intentionally out of scope.
 * <p>
 * Defaults to Chromium standalone mode ({@code dev.equo.swt.mode=chromium}), where the Browser is an
 * iframe; comment out that property to compare which APIs behave differently against the default
 * backend. APIs that are unsupported in the current mode surface as FAIL with their exception.
 */
public class BrowserSnippet {

    enum Verdict { PASS, FAIL, UNKNOWN }

    /** Outcome of a single Test action: a verdict plus a human-readable detail/return value. */
    record Result(Verdict verdict, String detail) {
        static Result of(Verdict v, String d) { return new Result(v, d); }
    }

    private Display display;
    private Shell shell;
    private Browser browser;
    private Text urlText;
    private Label typeLabel;
    private Text console;
    private Text detail;
    private ScrolledComposite scroll;
    private Composite panel;

    /** Listener/callback rows keyed by a stable name; their badge is driven by {@link #markFired}. */
    private final Map<String, Label> firedBadges = new HashMap<>();
    private final Map<String, Integer> firedCounts = new HashMap<>();

    // --- influence controls (created during UI build, read inside listeners) ---
    private Button blockChk;
    private Text blockHost;
    private Button rewriteChk;
    private Text rewriteTarget;
    private Text authUser;
    private Text authPass;
    private Button authCancel;
    private org.eclipse.swt.widgets.Combo openWindowMode;

    // --- BrowserFunction under test ---
    private BrowserFunction browserFunction;

    private static final String TEST_URL = "https://www.eclipse.org";

    /** URLs that are useful for exercising the API (most load inside an iframe). */
    private static final String[] KNOWN_URLS = {
            "https://example.com",                          // minimal page, iframe-friendly
            "https://example.org",
            "https://httpbin.org/",                         // HTTP testing
            "https://httpbin.org/post",                     // for setUrl(url, postData, headers)
            "https://httpbin.org/basic-auth/user/passwd",   // for AuthenticationListener
            "https://httpbin.org/cookies",                  // shows cookies the page receives
            "https://httpbin.org/cookies/set?equoTest=1",   // sets a cookie then redirects
            "https://www.iana.org/help/example-domains",    // for the Location.changing block demo
            "https://www.google.com",                       // popular site (may refuse framing)
            "https://www.wikipedia.org",
    };
    private static final String SAMPLE_HTML =
            "<html><head><title>equo-setText</title></head><body style='font-family:sans-serif'>"
                    + "<h1>setText sample page</h1>"
                    + "<p><a href='https://www.iana.org/help/example-domains'>hover or click me (iana.org)</a></p>"
                    + "<p id='marker'>MARKER-12345</p></body></html>";

    public static void main(String[] args) {
        System.setProperty("chromium.debug", "true");
//        System.setProperty("dev.equo.swt.mode", "chromium");
//        System.setProperty("chromium.debug", "true");
        Config.forceEquo();
        new BrowserSnippet().run();
    }

    private void run() {
        display = new Display();
        shell = new Shell(display);
        shell.setText("Browser API test harness");
        shell.setLayout(fill());

        SashForm vertical = new SashForm(shell, SWT.VERTICAL);

        Composite top = new Composite(vertical, SWT.NONE);
        top.setLayout(grid(1, 0));
        buildToolbar(top);

        SashForm center = new SashForm(top, SWT.HORIZONTAL);
        center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        browser = new Browser(center, SWT.NONE);

        scroll = new ScrolledComposite(center, SWT.V_SCROLL | SWT.BORDER);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);
        panel = new Composite(scroll, SWT.NONE);
        panel.setLayout(grid(1, 6));
        scroll.setContent(panel);
        center.setWeights(new int[] { 60, 40 });

        detail = new Text(vertical, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.BORDER);
        detail.setText("Select a row to see what it does and what to expect.");

        Composite consoleArea = new Composite(vertical, SWT.NONE);
        consoleArea.setLayout(grid(1, 0));
        Composite consoleHeader = new Composite(consoleArea, SWT.NONE);
        consoleHeader.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        consoleHeader.setLayout(new RowLayout(SWT.HORIZONTAL));
        new Label(consoleHeader, SWT.NONE).setText("Event / result console:");
        Button clearBtn = new Button(consoleHeader, SWT.PUSH);
        clearBtn.setText("Clear");
        clearBtn.addListener(SWT.Selection, e -> console.setText(""));
        console = new Text(consoleArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        console.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        vertical.setWeights(new int[] { 70, 12, 18 });

        installListeners();
        registerTests();
        layoutPanel();
        refreshType();

        browser.setUrl(TEST_URL);

        shell.setSize(1200, 820);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    // ------------------------------------------------------------------ toolbar

    private void buildToolbar(Composite parent) {
        Composite toolbar = new Composite(parent, SWT.NONE);
        toolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.center = true;
        toolbar.setLayout(rl);

        button(toolbar, "◀", e -> safe("back()", () -> browser.back()));
        button(toolbar, "▶", e -> safe("forward()", () -> browser.forward()));
        urlText = new Text(toolbar, SWT.BORDER | SWT.SINGLE);
        urlText.setText(TEST_URL);
        urlText.setLayoutData(new RowData(420, SWT.DEFAULT));
        button(toolbar, "Go", e -> safe("setUrl()", () -> browser.setUrl(urlText.getText().trim())));
        urlText.addListener(SWT.DefaultSelection,
                e -> safe("setUrl()", () -> browser.setUrl(urlText.getText().trim())));

        org.eclipse.swt.widgets.Combo known = new org.eclipse.swt.widgets.Combo(toolbar, SWT.READ_ONLY);
        known.add("known URLs…");
        for (String u : KNOWN_URLS) known.add(u);
        known.select(0);
        known.setLayoutData(new RowData(240, SWT.DEFAULT));
        known.addListener(SWT.Selection, e -> {
            int i = known.getSelectionIndex();
            if (i >= 1) {
                String u = KNOWN_URLS[i - 1];
                urlText.setText(u);
                safe("setUrl()", () -> browser.setUrl(u));
            }
        });

        typeLabel = new Label(toolbar, SWT.NONE);
        typeLabel.setText("type: ?");
    }

    /** Updates the toolbar's browser-type label; the type is only known once the browser exists. */
    private void refreshType() {
        if (typeLabel == null || typeLabel.isDisposed() || browser == null || browser.isDisposed()) return;
        try {
            String t = browser.getBrowserType();
            typeLabel.setText("type: " + (t == null || t.isEmpty() ? "?" : t));
        } catch (Throwable t) {
            typeLabel.setText("type: ?");
        }
        typeLabel.requestLayout();
    }

    // ------------------------------------------------------------------ listeners

    private void installListeners() {
        browser.addLocationListener(LocationListener.changingAdapter(e -> {
            String host = blockHost.getText().trim();
            if (blockChk.getSelection() && !host.isEmpty() && e.location != null && e.location.contains(host)) {
                e.doit = false;
                markFired("Location.changing", "BLOCKED " + e.location);
                return;
            }
            String target = rewriteTarget.getText().trim();
            if (rewriteChk.getSelection() && !target.isEmpty() && e.location != null && !e.location.equals(target)) {
                e.doit = false; // SWT changing can only veto; redirect = veto + load the new target
                markFired("Location.changing", "REWRITE " + e.location + " -> " + target);
                display.asyncExec(() -> { if (!browser.isDisposed()) browser.setUrl(target); });
                return;
            }
            markFired("Location.changing", "allow " + e.location);
        }));
        browser.addLocationListener(LocationListener.changedAdapter(e -> {
            if (e.location != null) urlText.setText(e.location);
            markFired("Location.changed", e.location);
        }));
        browser.addTitleListener(e -> {
            shell.setText("Browser API test harness — " + e.title);
            markFired("Title.changed", e.title);
        });
        browser.addProgressListener(ProgressListener.changedAdapter(e ->
                markFired("Progress.changed", e.total > 0 ? (100 * e.current / e.total) + "%" : e.current + "/" + e.total)));
        browser.addProgressListener(ProgressListener.completedAdapter(e -> {
            markFired("Progress.completed", "page loaded");
            refreshType();
        }));
        browser.addStatusTextListener(e -> markFired("StatusText.changed", e.text));
        browser.addAuthenticationListener(e -> {
            if (authCancel.getSelection()) {
                e.doit = false;
                markFired("Authentication.authenticate", "CANCELLED " + e.location);
            } else {
                e.user = authUser.getText();
                e.password = authPass.getText();
                markFired("Authentication.authenticate", "creds for " + e.location);
            }
        });
        browser.addOpenWindowListener(openWindowListener());
        browser.addCloseWindowListener(e -> markFired("CloseWindow.close", "window.close()"));
        browser.addVisibilityWindowListener(VisibilityWindowListener.showAdapter(
                e -> markFired("VisibilityWindow.show", "show")));
        browser.addVisibilityWindowListener(VisibilityWindowListener.hideAdapter(
                e -> markFired("VisibilityWindow.hide", "hide")));
    }

    private OpenWindowListener openWindowListener() {
        return (com.equo.chromium.swt.WindowEvent event) -> {
            String mode = openWindowMode == null ? "provide popup" : openWindowMode.getText();
            switch (mode) {
                case "required=true (veto)":
                    event.required = true;
                    markFired("OpenWindow.open", "required=true (vetoed)");
                    break;
                case "leave null":
                    markFired("OpenWindow.open", "left null");
                    break;
                default: // provide popup
                    Shell popup = new Shell(display);
                    popup.setText("popup window");
                    popup.setLayout(new FillLayout());
                    popup.setSize(640, 480);
                    Browser pb = new Browser(popup, SWT.NONE);
                    pb.addVisibilityWindowListener(VisibilityWindowListener.showAdapter(ev -> {
                        markFired("VisibilityWindow.show", "popup show");
                        popup.open();
                    }));
                    pb.addVisibilityWindowListener(VisibilityWindowListener.hideAdapter(ev -> {
                        markFired("VisibilityWindow.hide", "popup hide");
                        popup.setVisible(false);
                    }));
                    pb.addCloseWindowListener(ev -> {
                        markFired("CloseWindow.close", "popup close");
                        popup.close();
                    });
                    event.browser = pb;
                    markFired("OpenWindow.open", "provided popup Browser");
            }
        };
    }

    // ------------------------------------------------------------------ test registry

    private void registerTests() {
        // --- Navigation ---
        Group nav = group("Navigation");
        test(nav, "back()", "Navigate to the previous history item. Returns true if it could go back.",
                () -> browser.back(), null);
        test(nav, "forward()", "Navigate to the next history item. Returns true if it could go forward.",
                () -> browser.forward(), null);
        test(nav, "isBackEnabled()", "True if a previous history item exists.",
                () -> browser.isBackEnabled(), null);
        test(nav, "isForwardEnabled()", "True if a next history item exists.",
                () -> browser.isForwardEnabled(), null);
        test(nav, "refresh()", "Reload the current page. Watch Progress.* fire.",
                () -> { browser.refresh(); return "invoked"; }, null);
        test(nav, "stop()", "Stop loading/rendering the current page.",
                () -> { browser.stop(); return "invoked"; }, null);

        // --- Content ---
        Group content = group("Content");
        test(content, "setUrl(String)", "Begin loading the toolbar URL. Returns true on success.",
                () -> browser.setUrl(urlText.getText().trim()), isTrue());
        test(content, "setUrl(url,post,headers)", "POST to httpbin with body and a custom header.",
                () -> browser.setUrl("https://httpbin.org/post", "field=equo",
                        new String[] { "X-Equo: test" }), isTrue());
        test(content, "getUrl()", "Return the current URL. Expected: non-empty.",
                () -> browser.getUrl(), nonEmpty());
        test(content, "setText(String)", "Render a sample HTML page (with a title + a link).",
                () -> browser.setText(SAMPLE_HTML), isTrue());
        test(content, "setText(html,trusted)", "Render the sample page with trusted permissions.",
                () -> browser.setText(SAMPLE_HTML, true), isTrue());
        test(content, "getText()", "Return current page HTML. Expected: contains MARKER-12345 (after setText).",
                () -> browser.getText(), o -> contains(o, "MARKER"));

        // --- Scripting ---
        Group script = group("Scripting");
        test(script, "execute(String)",
                "Runs JS in the current document and returns no value. Here it injects a visible green banner "
                        + "into the page DOM (and sets the title); the effect is then read back with evaluate to confirm.",
                () -> {
                    browser.execute(
                            "var d=document.getElementById('equoExec')||document.createElement('div');"
                                    + "d.id='equoExec';d.textContent='execute() ran';"
                                    + "d.style.cssText='position:fixed;top:8px;right:8px;z-index:2147483647;background:#0a0;"
                                    + "color:#fff;padding:6px 10px;font:14px sans-serif;border-radius:4px';"
                                    + "document.body.appendChild(d);document.title='exec-OK';");
                    return browser.evaluate("var e=document.getElementById('equoExec');return e?e.textContent:'NONE';");
                }, o -> "execute() ran".equals(o));
        test(script, "evaluate(\"1+1\")", "Evaluate JS and return the Java value. Expected: 2.0 (Double).",
                () -> browser.evaluate("1+1"), o -> Double.valueOf(2).equals(o));
        test(script, "evaluate(userAgent)", "Evaluate and return a String (navigator.userAgent).",
                () -> browser.evaluate("navigator.userAgent"), nonEmpty());
        test(script, "evaluate(script,trusted)", "Trusted evaluate. Expected: 42.0.",
                () -> browser.evaluate("2*21", true), o -> Double.valueOf(42).equals(o));

        // --- State ---
        Group state = group("State");
        test(state, "getBrowserType()", "Native browser type. Expected: non-empty (e.g. chromium).",
                () -> browser.getBrowserType(), nonEmpty());
        test(state, "getJavascriptEnabled()", "Whether JS will run in subsequently-viewed pages.",
                () -> browser.getJavascriptEnabled(), null);
        test(state, "setJavascriptEnabled(true)", "Enable JS, then read back. Expected: read-back == true.",
                () -> { browser.setJavascriptEnabled(true); return browser.getJavascriptEnabled(); }, isTrue());
        test(state, "setJavascriptEnabled(false)", "Disable JS, then read back. Expected: read-back == false.",
                () -> { browser.setJavascriptEnabled(false); return browser.getJavascriptEnabled(); },
                o -> Boolean.FALSE.equals(o));
        test(state, "getWebBrowser()", "Deprecated: returns the underlying native browser handle (or null).",
                () -> { Object o = browser.getWebBrowser(); return o == null ? "null" : o.getClass().getName(); }, null);

        // --- Cookies (static) ---
        Group cookies = group("Cookies (static)");
        test(cookies, "setCookie(value,url)", "Set 'equoTest=1' for the test URL. Returns true on success.",
                () -> Browser.setCookie("equoTest=1; path=/", TEST_URL), isTrue());
        test(cookies, "getCookie(name,url)", "Read it back. Expected: \"1\" (run setCookie first).",
                () -> Browser.getCookie("equoTest", TEST_URL), o -> "1".equals(o));
        test(cookies, "clearSessions()", "Clear session cookies from all Browser instances (no observable return).",
                () -> { Browser.clearSessions(); return "invoked"; }, null);

        // --- Listeners ---
        Group listeners = group("Listeners (trigger to fire; badge greens on first fire)");
        listenerRow(listeners, "Location.changing",
                "Fires before navigation. event.doit=false cancels it. Use the controls below to block a host "
                        + "or rewrite the target.", "Navigate (setUrl)",
                () -> browser.setUrl(urlText.getText().trim()));
        influenceRow(listeners, c -> {
            blockChk = new Button(c, SWT.CHECK);
            blockChk.setText("block host containing");
            blockHost = new Text(c, SWT.BORDER);
            blockHost.setText("iana.org");
            blockHost.setLayoutData(new RowData(140, SWT.DEFAULT));
            rewriteChk = new Button(c, SWT.CHECK);
            rewriteChk.setText("rewrite navigations to");
            rewriteTarget = new Text(c, SWT.BORDER);
            rewriteTarget.setText("https://example.org");
            rewriteTarget.setLayoutData(new RowData(180, SWT.DEFAULT));
        });
        listenerRow(listeners, "Location.changed", "Fires after the location changed; updates the URL field.",
                "Navigate (setUrl)", () -> browser.setUrl(urlText.getText().trim()));
        listenerRow(listeners, "Title.changed", "Fires when the document title becomes available/changes.",
                "execute(title=...)", () -> browser.execute("document.title='title-' + Date.now()"));
        listenerRow(listeners, "Progress.changed", "Fires as the page loads (current/total).",
                "refresh()", () -> browser.refresh());
        listenerRow(listeners, "Progress.completed", "Fires when the current page finished loading.",
                "refresh()", () -> browser.refresh());
        listenerRow(listeners, "StatusText.changed",
                "Fires when status text changes — typically on hovering a link. Render the sample page, then "
                        + "hover its link.", "setText(sample)", () -> browser.setText(SAMPLE_HTML));
        listenerRow(listeners, "Authentication.authenticate",
                "Fires when a page needs HTTP auth. Supply user/password below, or tick Cancel to abort.",
                "Load basic-auth URL", () -> browser.setUrl("https://httpbin.org/basic-auth/user/passwd"));
        influenceRow(listeners, c -> {
            new Label(c, SWT.NONE).setText("user");
            authUser = new Text(c, SWT.BORDER);
            authUser.setText("user");
            authUser.setLayoutData(new RowData(90, SWT.DEFAULT));
            new Label(c, SWT.NONE).setText("password");
            authPass = new Text(c, SWT.BORDER);
            authPass.setText("passwd");
            authPass.setLayoutData(new RowData(90, SWT.DEFAULT));
            authCancel = new Button(c, SWT.CHECK);
            authCancel.setText("cancel auth (doit=false)");
        });
        listenerRow(listeners, "OpenWindow.open",
                "Fires on window.open(). The combo decides whether to provide a real popup Browser, veto with "
                        + "required=true, or leave it null.", "execute(window.open)",
                () -> browser.execute("window.open('https://example.com')"));
        influenceRow(listeners, c -> {
            new Label(c, SWT.NONE).setText("on open:");
            openWindowMode = new org.eclipse.swt.widgets.Combo(c, SWT.READ_ONLY);
            openWindowMode.setItems("provide popup", "required=true (veto)", "leave null");
            openWindowMode.select(0);
        });
        listenerRow(listeners, "CloseWindow.close", "Fires on window.close() (in the main page or a popup).",
                "execute(window.close)", () -> browser.execute("window.close()"));
        listenerRow(listeners, "VisibilityWindow.show", "Fires when a popup window should be shown (see OpenWindow).",
                "execute(window.open)", () -> browser.execute("window.open('https://example.com')"));
        listenerRow(listeners, "VisibilityWindow.hide", "Fires when a popup window should be hidden.",
                "—", () -> log("hide fires from the popup lifecycle; open one via OpenWindow first"));

        // --- remove*Listener ---
        Group removal = group("Remove listeners");
        test(removal, "removeLocationListener()",
                "Removes a fresh LocationListener (no-op effect here since it was never added).",
                () -> { LocationListener l = LocationListener.changedAdapter(e -> {});
                    browser.removeLocationListener(l); return "invoked"; }, null);
        test(removal, "removeTitleListener()", "Removes a fresh TitleListener.",
                () -> { TitleListener l = e -> {}; browser.removeTitleListener(l); return "invoked"; }, null);
        test(removal, "removeProgressListener()", "Removes a fresh ProgressListener.",
                () -> { ProgressListener l = ProgressListener.completedAdapter(e -> {});
                    browser.removeProgressListener(l); return "invoked"; }, null);
        test(removal, "removeStatusTextListener()", "Removes a fresh StatusTextListener.",
                () -> { StatusTextListener l = e -> {}; browser.removeStatusTextListener(l); return "invoked"; }, null);
        test(removal, "removeAuthenticationListener()", "Removes a fresh AuthenticationListener.",
                () -> { AuthenticationListener l = e -> {}; browser.removeAuthenticationListener(l); return "invoked"; }, null);
        test(removal, "removeCloseWindowListener()", "Removes a fresh CloseWindowListener.",
                () -> { CloseWindowListener l = e -> {}; browser.removeCloseWindowListener(l); return "invoked"; }, null);
        test(removal, "removeVisibilityWindowListener()", "Removes a fresh VisibilityWindowListener.",
                () -> { VisibilityWindowListener l = VisibilityWindowListener.showAdapter(e -> {});
                    browser.removeVisibilityWindowListener(l); return "invoked"; }, null);
        test(removal, "removeOpenWindowListener()",
                "Removes a previously added OpenWindowListener so it is no longer notified when new windows "
                        + "are requested.",
                () -> { browser.removeOpenWindowListener(openWindowListener()); return "invoked"; }, null);

        // --- BrowserFunction ---
        Group fn = group("BrowserFunction");
        test(fn, "new BrowserFunction(b,\"equoFn\")",
                "Registers a JS-callable function named equoFn backed by Java function(Object[]); pages can call "
                        + "window.equoFn(args) and receive the Java return value.",
                () -> { browserFunction = new BrowserFunction(browser, "equoFn") {
                    @Override public Object function(Object[] arguments) {
                        markFired("BrowserFunction.function", Arrays.toString(arguments));
                        return "java:" + (arguments != null && arguments.length > 0 ? arguments[0] : "");
                    }
                };
                    return "created"; }, null);
        listenerRow(fn, "BrowserFunction.function",
                "The Java callback invoked from JS. Trigger calls window.equoFn('hi',42) and expects 'java:hi'.",
                "invoke from JS",
                () -> safe("equoFn invoke", () -> browser.evaluate(
                        "return window.equoFn ? window.equoFn('hi', 42) : 'NO_FUNCTION'")));
        test(fn, "BrowserFunction.getName()", "Expected: \"equoFn\".",
                () -> requireFn().getName(), o -> "equoFn".equals(o));
        test(fn, "BrowserFunction.getBrowser()", "Expected: the same Browser instance.",
                () -> requireFn().getBrowser() == browser, isTrue());
        test(fn, "BrowserFunction.isDisposed()", "Expected: false before dispose.",
                () -> requireFn().isDisposed(), null);
        test(fn, "new BrowserFunction(b,name,top,frames)", "Frame-scoped constructor (top=true, no frame names).",
                () -> { new BrowserFunction(browser, "equoFnTop", true, null); return "created"; }, null);
        test(fn, "BrowserFunction.dispose()",
                "Dispose it; afterwards window.equoFn should be gone (re-invoke ⇒ NO_FUNCTION) and isDisposed()⇒true.",
                () -> { requireFn().dispose();
                    Object after = browser.evaluate("return window.equoFn ? 'STILL_THERE' : 'NO_FUNCTION'");
                    return "isDisposed=" + browserFunction.isDisposed() + ", js=" + after; },
                o -> contains(o, "NO_FUNCTION"));

        // --- Lifecycle (destructive) ---
        Group life = group("Lifecycle (destructive)");
        test(life, "close()",
                "Attempts to dispose the Browser (may be vetoed by onbeforeunload). Disables the harness afterwards.",
                () -> {
                    MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
                    mb.setText("close()");
                    mb.setMessage("close() disposes the Browser widget. Continue?");
                    if (mb.open() != SWT.YES) return "skipped";
                    boolean closed = browser.close();
                    if (closed) disableAll();
                    return closed; }, null);
    }

    private BrowserFunction requireFn() {
        if (browserFunction == null)
            throw new IllegalStateException("create the BrowserFunction first");
        return browserFunction;
    }

    // ------------------------------------------------------------------ row/UI helpers

    private Group group(String title) {
        Group g = new Group(panel, SWT.NONE);
        g.setText(title);
        g.setLayout(grid(3, 4));
        g.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        return g;
    }

    /** A method test row: name · Test button · badge. */
    private void test(Group g, String name, String expected, Callable<Object> call, Predicate<Object> pass) {
        Label nameLabel = new Label(g, SWT.NONE);
        nameLabel.setText(name);
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Button b = new Button(g, SWT.PUSH);
        b.setText("Test");
        Label badge = badge(g);
        nameLabel.addListener(SWT.MouseDown, e -> showDetail(name, expected));
        b.addListener(SWT.Selection, e -> {
            showDetail(name, expected);
            Result r = invoke(name, call, pass);
            setBadge(badge, r.verdict(), r.detail());
        });
    }

    /** A listener/callback row: name · Trigger button · badge (badge driven by {@link #markFired}). */
    private void listenerRow(Group g, String key, String expected, String triggerLabel, Runnable trigger) {
        Label nameLabel = new Label(g, SWT.NONE);
        nameLabel.setText(key);
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Button b = new Button(g, SWT.PUSH);
        b.setText(triggerLabel);
        Label badge = badge(g);
        firedBadges.put(key, badge);
        setBadge(badge, Verdict.UNKNOWN, "waiting");
        nameLabel.addListener(SWT.MouseDown, e -> showDetail(key, expected));
        b.addListener(SWT.Selection, e -> {
            showDetail(key, expected);
            try {
                trigger.run();
                log(key + " · triggered (" + triggerLabel + ")");
            } catch (Throwable t) {
                log(key + " · trigger FAILED: " + t);
            }
        });
    }

    /** Adds a full-width (3-column span) strip of inline influence controls. */
    private void influenceRow(Group g, Consumer<Composite> builder) {
        Composite c = new Composite(g, SWT.NONE);
        c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        RowLayout rl = new RowLayout(SWT.HORIZONTAL);
        rl.center = true;
        c.setLayout(rl);
        builder.accept(c);
    }

    private Label badge(Group g) {
        Label badge = new Label(g, SWT.NONE);
        badge.setLayoutData(new GridData(28, SWT.DEFAULT));
        return badge;
    }

    private void setBadge(Label badge, Verdict v, String detail) {
        badge.setText(v == Verdict.PASS ? "✓" : v == Verdict.FAIL ? "✗" : "?");
        badge.setForeground(color(v));
        if (detail != null) badge.setToolTipText(detail);
    }

    private void markFired(String key, String detail) {
        int n = firedCounts.merge(key, 1, Integer::sum);
        log(key + " → " + detail + "  (fired " + n + ")");
        Label badge = firedBadges.get(key);
        if (badge != null && !badge.isDisposed()) {
            badge.setText("✓");
            badge.setForeground(color(Verdict.PASS));
            badge.setToolTipText("fired " + n + " · last: " + detail);
        }
    }

    // ------------------------------------------------------------------ verdict helpers

    private Result invoke(String name, Callable<Object> call, Predicate<Object> pass) {
        Object out;
        try {
            out = call.call();
        } catch (Throwable t) {
            log(name + " → FAIL: " + t);
            return Result.of(Verdict.FAIL, String.valueOf(t));
        }
        String shown = String.valueOf(out);
        Verdict v = pass == null ? Verdict.UNKNOWN : (pass.test(out) ? Verdict.PASS : Verdict.FAIL);
        log(name + " → " + v + ": " + shown);
        return Result.of(v, shown);
    }

    /** Used by toolbar buttons whose result we only want logged. */
    private void safe(String name, Callable<Object> call) {
        invoke(name, call, null);
    }

    private static Predicate<Object> isTrue() { return o -> Boolean.TRUE.equals(o); }

    private static Predicate<Object> nonEmpty() {
        return o -> o != null && !o.toString().isEmpty();
    }

    private static boolean contains(Object actual, String needle) {
        return actual != null && actual.toString().contains(needle);
    }

    private Color color(Verdict v) {
        switch (v) {
            case PASS: return display.getSystemColor(SWT.COLOR_DARK_GREEN);
            case FAIL: return display.getSystemColor(SWT.COLOR_RED);
            default:   return display.getSystemColor(SWT.COLOR_DARK_GRAY);
        }
    }

    // ------------------------------------------------------------------ misc

    private void showDetail(String name, String expected) {
        detail.setText(name + "\n\n" + expected);
    }

    private void log(String msg) {
        if (console.isDisposed()) return;
        console.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "  " + msg + "\n");
        console.setTopIndex(console.getLineCount() - 1); // auto-scroll to the newest line
    }

    private void disableAll() {
        for (org.eclipse.swt.widgets.Control c : panel.getChildren()) setEnabledDeep(c, false);
        log("Browser closed — harness disabled.");
    }

    private void setEnabledDeep(org.eclipse.swt.widgets.Control c, boolean enabled) {
        c.setEnabled(enabled);
        if (c instanceof Composite) for (org.eclipse.swt.widgets.Control k : ((Composite) c).getChildren())
            setEnabledDeep(k, enabled);
    }

    private void layoutPanel() {
        panel.layout(true, true);
        scroll.setMinSize(panel.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    // ------------------------------------------------------------------ layout factories

    private static GridLayout grid(int cols, int margin) {
        GridLayout gl = new GridLayout(cols, false);
        gl.marginWidth = margin;
        gl.marginHeight = margin;
        return gl;
    }

    private static FillLayout fill() {
        return new FillLayout();
    }

    private Button button(Composite parent, String text, org.eclipse.swt.widgets.Listener onClick) {
        Button b = new Button(parent, SWT.PUSH);
        b.setText(text);
        b.addListener(SWT.Selection, onClick);
        return b;
    }
}

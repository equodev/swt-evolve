package dev.equo.swt.harness;

import java.util.List;
import java.util.Map;

/**
 * {@link FlutterHarness} specialized for the SWT {@code Browser} widget tests
 * ({@code BrowserFlutterTest}). It adds only what those tests need on top of the generic harness:
 * the {@code evolve.test.iframeLoaded} on-load probe, which lets a test wait until an embedded page
 * the Browser widget loaded has actually rendered.
 *
 * <p>Like its base, it runs on both desktop and web (it does not change the rendering mode); it just
 * carries the Browser-widget-specific test affordances.
 */
public class BrowserFlutterHarness extends FlutterHarness {

    private static final String IFRAME_LOADED = "evolve.test.iframeLoaded";

    /** Marks (page titles) of embedded pages that posted their on-load ping. */
    private final List<String> iframeLoads = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    protected void registerCommHandlers() {
        super.registerCommHandlers();
        // On-load pings from embedded pages (posted by the page itself; see test_harness_iframe_web.dart).
        comm().on(IFRAME_LOADED, byte[].class, bytes -> {
            if (bytes == null) return;
            Map<String, Object> m = parseJson(bytes);
            Object page = m == null ? null : m.get("page");
            if (page != null) iframeLoads.add(String.valueOf(page));
        });
    }

    /** Forget prior on-load pings, so a following {@link #awaitIframeRendered} only sees fresh ones. */
    public void clearIframeLoads() {
        iframeLoads.clear();
    }

    /**
     * Awaits the on-load ping an embedded page posts once it actually renders (its
     * {@code document.title} contains {@code expectedTitle}). Returns {@code false} on timeout — the
     * page never rendered (e.g. a cross-origin {@code <iframe>} blocked by COOP/COEP or
     * X-Frame-Options).
     */
    public boolean awaitIframeRendered(String expectedTitle, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (true) {
            for (String p : iframeLoads) if (p.contains(expectedTitle)) return true;
            if (System.currentTimeMillis() >= deadline) return false;
            pumpClient();
        }
    }
}

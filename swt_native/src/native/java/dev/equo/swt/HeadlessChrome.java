package dev.equo.swt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A throwaway Chrome/Chromium instance for automated runs. It owns the launched process — started
 * against a URL in a unique temp profile, with the container-hardening flags that keep a
 * small-{@code /dev/shm} CI container from wedging Chrome during boot — and, on {@link #close()},
 * kills the <em>whole process tree</em> (Chrome spawns zygote / GPU / renderer helpers that a plain
 * {@code destroy()} would orphan) and deletes the profile.
 *
 * <p>Extracted so the two places that drive a browser for automation — the production web server
 * ({@link WebFlutterServer#launchBrowser()} in headless mode) and the test harness
 * ({@code FlutterHarness}) — share one implementation of Chrome discovery, launch flags, and
 * teardown. They have no common superclass but need exactly the same browser plumbing; the only
 * per-caller differences (headless vs. visible, whether to inherit or discard Chrome's very chatty
 * stdio, and which URL to open) are parameters of {@link #launch}.
 */
public final class HeadlessChrome implements AutoCloseable {

    /** What to do with the launched browser's stdout/stderr. */
    public enum Io { INHERIT, DISCARD }

    private Process process;
    private Path profileDir;

    private HeadlessChrome(Process process, Path profileDir) {
        this.process = process;
        this.profileDir = profileDir;
    }

    /** The launched process, so a caller can observe it (e.g. relaunch on a wedged boot). */
    public Process process() {
        return process;
    }

    /**
     * Locate a Chrome/Chromium executable: an explicit path {@code override} first (a real executable;
     * blank or {@code "none"} is ignored), otherwise the usual per-OS install locations. Returns
     * {@code null} if none is found.
     */
    public static String resolveBinary(String override) {
        if (override != null && !override.isEmpty() && !"none".equalsIgnoreCase(override)
                && new File(override).canExecute()) {
            return override;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] candidates = os.contains("mac")
                ? new String[]{"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                               "/Applications/Chromium.app/Contents/MacOS/Chromium"}
                : os.contains("win")
                ? new String[]{System.getenv("ProgramFiles") + "\\Google\\Chrome\\Application\\chrome.exe",
                               System.getenv("ProgramFiles(x86)") + "\\Google\\Chrome\\Application\\chrome.exe",
                               System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe",
                               System.getenv("ProgramFiles(x86)") + "\\Microsoft\\Edge\\Application\\msedge.exe",
                               System.getenv("ProgramFiles") + "\\Microsoft\\Edge\\Application\\msedge.exe"}
                : new String[]{"/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser"};
        for (String c : candidates) if (c != null && new File(c).canExecute()) return c;
        return null;
    }

    /**
     * Launch {@code binary} against {@code url} in a fresh, unique throwaway profile.
     *
     * @param binary   the Chrome/Chromium executable (see {@link #resolveBinary})
     * @param url      the URL to open (callers inject their own — e.g. a blank page to test boot resilience)
     * @param headless run with {@code --headless=new}; {@code false} opens a visible window
     * @param verbose  add {@code --enable-logging=stderr --v=1}
     * @param io       inherit or discard the browser's (very chatty) stdio
     */
    public static HeadlessChrome launch(String binary, String url, boolean headless, boolean verbose, Io io)
            throws IOException {
        // Unique, throwaway profile per run so Chrome's per-user-data-dir singleton can't hand the URL
        // to an already-running instance, and an unclean exit can't leave a crash-restore flag.
        Path profileDir = Files.createTempDirectory("equo-chrome-profile-");
        List<String> cmd = new ArrayList<>();
        cmd.add(binary);
        if (headless) cmd.add("--headless=new");
        if (verbose) {
            cmd.add("--enable-logging=stderr");
            cmd.add("--v=1");
        }
        cmd.add("--disable-gpu");
        cmd.add("--no-first-run");
        cmd.add("--no-default-browser-check");
        cmd.add("--hide-crash-restore-bubble");
        cmd.add("--disable-session-crashed-bubble");
        // Container hardening: the boot occasionally wedges right after Chrome's background-service
        // init (a tiny /dev/shm starves the renderer; the GCM registration and the optimization-guide
        // on-device model — the "TensorFlow Lite XNNPACK delegate" line — are slow, hang-prone, and
        // useless here). Disable that startup work so Chrome goes straight to loading the app.
        cmd.add("--disable-dev-shm-usage");
        cmd.add("--disable-background-networking");
        cmd.add("--disable-component-update");
        cmd.add("--disable-sync");
        cmd.add("--disable-default-apps");
        cmd.add("--no-pings");
        cmd.add("--metrics-recording-only");
        cmd.add("--disable-renderer-backgrounding");
        cmd.add("--disable-backgrounding-occluded-windows");
        cmd.add("--disable-features=OptimizationGuideModelDownloading,OptimizationHints,"
                + "OptimizationHintsFetching,OptimizationTargetPrediction,Translate,MediaRouter,"
                + "InterestFeedContentSuggestions,CalculateNativeWinOcclusion");
        cmd.add("--user-data-dir=" + profileDir);
        cmd.add(url);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (io == Io.INHERIT) {
            pb.inheritIO();
        } else {
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        }
        return new HeadlessChrome(pb.start(), profileDir);
    }

    /**
     * Forcibly terminate the browser <em>and its descendants</em> (Chrome's zygote + renderer/GPU
     * helpers that a plain {@code destroy()} would orphan), then delete the throwaway profile dir.
     * Idempotent.
     */
    @Override
    public void close() {
        if (process != null) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            try {
                process.waitFor(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            process = null;
        }
        if (profileDir != null) {
            // The process is dead (awaited above), so it has released the profile; delete it depth-first.
            try (var paths = Files.walk(profileDir)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            } catch (Exception ignored) {
                // temp dir; the OS will reap it if we couldn't.
            }
            profileDir = null;
        }
    }
}

package dev.equo.swt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Reads the active Eclipse workbench theme ("dark"/"light") from the running workspace's e4 CSS
 * theme preferences, so swt-evolve's own Flutter rendering can match the theme the host IDE
 * actually has configured, instead of guessing independently.
 */
public final class EclipseWorkspaceTheme {
    private EclipseWorkspaceTheme() {
    }

    /** Host OS appearance lookup, replaced in tests so no real process is spawned. */
    static Supplier<String> osAppearance = EclipseWorkspaceTheme::readOsAppearance;

    /** Returns "dark", "light", or null if no Eclipse workspace/theme preference is found. */
    public static String detect() {
        try {
            String workspaceLocation = System.getProperty("osgi.instance.area");
            if (workspaceLocation == null) {
                return null;
            }
            if (workspaceLocation.startsWith("file:")) {
                workspaceLocation = workspaceLocation.substring(5);
            }
            File prefsFile = new File(workspaceLocation,
                    ".metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.e4.ui.css.swt.theme.prefs");
            if (!prefsFile.exists()) {
                return null;
            }
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(prefsFile)) {
                props.load(fis);
            }
            return classify(props.getProperty("themeid"));
        } catch (Exception e) {
            return null;
        }
    }

    /** Maps an e4 theme id to "dark"/"light", or null when it carries no usable variant. */
    static String classify(String themeId) {
        if (themeId == null) {
            return null;
        }
        String lower = themeId.toLowerCase();
        // "System" means "follow the desktop", so it is the OS that holds the answer -- checked
        // before the substring matches below, which would otherwise classify a name like
        // "default (system)" on its other half.
        if (lower.contains("system")) {
            return osAppearance.get();
        }
        if (lower.contains("dark")) {
            return "dark";
        }
        if (lower.contains("light") || lower.contains("default") || lower.contains("classic")) {
            return "light";
        }
        return null;
    }

    /** The desktop's own light/dark setting, or null when it cannot be read (a headless host). */
    private static String readOsAppearance() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.startsWith("mac")) {
            // AppleInterfaceStyle exists only while the desktop is dark; the read fails otherwise.
            String style = run("defaults", "read", "-g", "AppleInterfaceStyle");
            if (style == null) {
                return null;
            }
            return "Dark".equalsIgnoreCase(style) ? "dark" : "light";
        }
        if (os.startsWith("win")) {
            String value = run("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme");
            if (value == null || value.isEmpty()) {
                return null;
            }
            return value.contains("0x0") ? "dark" : "light";
        }
        String scheme = run("gsettings", "get", "org.gnome.desktop.interface", "color-scheme");
        if (scheme == null || scheme.isEmpty()) {
            scheme = run("gsettings", "get", "org.gnome.desktop.interface", "gtk-theme");
        }
        if (scheme == null || scheme.isEmpty()) {
            return null;
        }
        return scheme.toLowerCase().contains("dark") ? "dark" : "light";
    }

    /**
     * Runs a short query command and returns its trimmed output, or null when it cannot be run.
     * Bounded by a timeout because this sits on the Display initialization path.
     */
    private static String run(String... command) {
        Process process = null;
        try {
            process = new ProcessBuilder(command).redirectErrorStream(false).start();
            String output;
            try (InputStream in = process.getInputStream()) {
                output = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                return null;
            }
            return output;
        } catch (Exception e) {
            return null;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}

package dev.equo.swt;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** Reads the light/dark setting of the desktop the JVM runs on, by asking the OS's own settings tool. */
final class DesktopAppearance {
    private DesktopAppearance() {
    }

    /** The desktop's own light/dark setting, or null when it cannot be read (a headless host). */
    static String read() {
        String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
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
        return scheme.toLowerCase(java.util.Locale.ROOT).contains("dark") ? "dark" : "light";
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
                output = new String(Java8.readAllBytes(in), StandardCharsets.UTF_8).trim();
            }
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                return null;
            }
            return output;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}

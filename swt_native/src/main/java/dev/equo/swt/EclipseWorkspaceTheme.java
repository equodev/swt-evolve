package dev.equo.swt;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Reads the active Eclipse workbench theme from the running workspace's e4 CSS theme preferences,
 * so swt-evolve's own Flutter rendering can match the theme the host application actually has
 * configured, instead of guessing independently.
 */
public final class EclipseWorkspaceTheme {
    private EclipseWorkspaceTheme() {
    }

    /** Returns "dark", "light", or null if no Eclipse workspace/theme preference is found. */
    public static String detect() {
        return classify(detectThemeId());
    }

    /**
     * The raw e4 theme id the running workspace is configured with, or null when there is no
     * workspace to read one from.
     */
    public static String detectThemeId() {
        try {
            String workspaceLocation = instanceArea();
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
            String themeId = props.getProperty("themeid");
            return themeId == null || themeId.trim().isEmpty() ? null : themeId;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The running workspace's location, or null before there is one. An Eclipse IDE that is not given
     * {@code -data} sets it only after its Display starts, once the workspace has been chosen.
     */
    static String instanceArea() {
        return System.getProperty("osgi.instance.area");
    }

    /** Maps an e4 theme id to "dark"/"light", or null when it carries no usable variant. */
    static String classify(String themeId) {
        if (themeId == null) {
            return null;
        }
        String lower = themeId.toLowerCase(java.util.Locale.ROOT);
        // "System" names no variant: the system the client runs on decides. Checked before the
        // substring matches below, which would otherwise classify "default (system)" on its other half.
        if (lower.contains("system")) {
            return null;
        }
        if (lower.contains("dark")) {
            return "dark";
        }
        if (lower.contains("light") || lower.contains("default") || lower.contains("classic")) {
            return "light";
        }
        return null;
    }
}

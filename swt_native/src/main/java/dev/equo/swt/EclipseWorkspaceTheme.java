package dev.equo.swt;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Reads the active Eclipse workbench theme ("dark"/"light") from the running workspace's e4 CSS
 * theme preferences, so swt-evolve's own Flutter rendering can match the theme the host IDE
 * actually has configured, instead of guessing independently.
 */
public final class EclipseWorkspaceTheme {
    private EclipseWorkspaceTheme() {
    }

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
            String themeId = props.getProperty("themeid");
            if (themeId == null) {
                return null;
            }
            String lower = themeId.toLowerCase();
            if (lower.contains("dark")) {
                return "dark";
            }
            if (lower.contains("light") || lower.contains("default") || lower.contains("classic")) {
                return "light";
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

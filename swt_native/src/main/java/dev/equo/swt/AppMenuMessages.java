package dev.equo.swt;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Labels for the macOS application menu, resolved against SWT's own message bundle.
 *
 * <p>Upstream renamed these keys in SWT 3.119: before that the menu asked for the English label
 * itself ({@code SWT.getMessage("Hide Others")}), from then on for a {@code SWT_*} key, and only the
 * 3.119 bundle onwards defines the new names. Since a lookup miss yields the key verbatim, asking
 * for a {@code SWT_*} key against an older bundle renders "SWT_HideOthers" as the item text.
 *
 * <p>So each key is resolved under its modern name first and its pre-3.119 name second. That older
 * name is the English label, which makes it the correct last resort when the bundle defines neither
 * -- including when there is no bundle at all.
 */
public final class AppMenuMessages {

    /** Modern key to the name upstream passed for it before 3.119, which is the English label. */
    private static final Map<String, String> LEGACY_KEYS = Map.of(
            "SWT_About", "About",
            "SWT_Preferences", "Preferences...",
            "SWT_Services", "Services",
            "SWT_Hide", "Hide",
            "SWT_HideOthers", "Hide Others",
            "SWT_ShowAll", "Show All",
            "SWT_Quit", "Quit");

    private AppMenuMessages() {
    }

    /** The keys this resolves, so a test can cover the whole menu. */
    public static Set<String> keys() {
        return LEGACY_KEYS.keySet();
    }

    /** The menu item text for one of {@link #keys()}, never the key itself. */
    public static String label(String key) {
        return label(key, bundle());
    }

    static String label(String key, ResourceBundle bundle) {
        String modern = lookup(bundle, key);
        if (modern != null)
            return modern;
        String legacyKey = LEGACY_KEYS.get(key);
        if (legacyKey == null)
            return key;
        String legacy = lookup(bundle, legacyKey);
        return legacy != null ? legacy : legacyKey;
    }

    /**
     * The bundle is read directly rather than through {@code SWT.getMessage}, which reports a
     * missing key and a missing bundle as return values a real translation cannot be told from.
     */
    private static ResourceBundle bundle() {
        try {
            return ResourceBundle.getBundle("org.eclipse.swt.internal.SWTMessages");
        } catch (MissingResourceException noBundle) {
            return null;
        }
    }

    private static String lookup(ResourceBundle bundle, String key) {
        return bundle != null && bundle.containsKey(key) ? bundle.getString(key) : null;
    }
}

package dev.equo.swt;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/** Looks up the system colours the named theme in use sets through {@link NamedThemeSystemColors}. */
public final class ThemeSystemColors {

    private ThemeSystemColors() {
    }

    /** The colour {@code id} takes in the current named theme, or null when no provider sets it. */
    public static Color of(Device device, int id, boolean dark) {
        RGB rgb = rgb(id, dark);
        return rgb == null ? null : new Color(device, rgb);
    }

    /** The RGB {@code id} takes in the current named theme, or null when no provider sets it. */
    public static RGB rgb(int id, boolean dark) {
        return of(Providers.ALL, Config.getConfigFlags().theme_name, dark, id);
    }

    static RGB of(Iterable<NamedThemeSystemColors> providers, String themeName, boolean dark, int id) {
        if (themeName == null) return null;
        for (NamedThemeSystemColors provider : providers) {
            RGB rgb = provider.systemColor(themeName.trim(), dark, id);
            if (rgb != null) return rgb;
        }
        return null;
    }

    /** Loaded once: the providers are the build's, the same for every session. */
    private static final class Providers {
        static final List<NamedThemeSystemColors> ALL = load();

        private static List<NamedThemeSystemColors> load() {
            List<NamedThemeSystemColors> all = new ArrayList<>();
            for (NamedThemeSystemColors provider : ServiceLoader.load(NamedThemeSystemColors.class, NamedThemeSystemColors.class.getClassLoader()))
                all.add(provider);
            return Collections.unmodifiableList(all);
        }
    }
}

package dev.equo.swt;

import com.dslplatform.json.CompiledJson;

import java.util.Map;
import dev.equo.swt.DecorationsAlign;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class ConfigFlags {

    /**
     * System property selecting the rendering surface: {@link #MODE_DESKTOP} (a native top-level
     * window), {@link #MODE_CHROMIUM} (the Equo Chromium standalone window), or unset for the
     * default web surface (a browser). Read live via {@link #mode()} / {@link #isDesktopMode()} /
     * {@link #isChromiumMode()} — it can be set at runtime (e.g. by the desktop bridge), so it is
     * intentionally not cached with the rest of the flags below.
     */
    public static final String MODE_PROPERTY = "dev.equo.swt.mode";
    public static final String MODE_DESKTOP = "desktop";
    public static final String MODE_CHROMIUM = "chromium";

    /** The current rendering mode, or {@code null} when unset (the default web surface). */
    public static String mode() {
        return System.getProperty(MODE_PROPERTY);
    }

    /** True under the desktop-native surface ({@code -Ddev.equo.swt.mode=desktop}). */
    public static boolean isDesktopMode() {
        return MODE_DESKTOP.equalsIgnoreCase(mode());
    }

    /** True under the Equo Chromium standalone surface ({@code -Ddev.equo.swt.mode=chromium}). */
    public static boolean isChromiumMode() {
        return MODE_CHROMIUM.equalsIgnoreCase(mode());
    }

    /** Sets the rendering mode. Centralizes the write the way {@link #mode()} centralizes the read. */
    public static void setMode(String mode) {
        System.setProperty(MODE_PROPERTY, mode);
    }

    public boolean ctabfolder_visible_controls;

    /** Default true (matches the historical behaviour): the CTabFolder topRight/minimize/
     *  maximize/chevron block only appears on hover and floats on top of the tab row without
     *  reserving space. Set to false to keep it always shown and reserving its own width in the
     *  tab row (a Row sibling) instead -- lets a client that relies on an always-visible
     *  topRight toolbar (e.g. via setTopRight) keep every tab reachable instead of some being
     *  hidden underneath that floating overlay. */
    public boolean ctabfolder_topright_auto_hide;

    public boolean image_disable_icons_replacement;

    public String assets_path;

    /**
     * True keeps the application's own icons. Off by default, which lets Evolve substitute an icon
     * with one of ours — the bundled icon set and the name-to-Material-icon map. An
     * {@code assets_path} override applies either way.
     */
    public boolean disable_evolve_icons;

    /**
     * Lets the bundled icon set stand in for an image the application blits itself with
     * {@code GC#drawImage}. Off by default: the set is keyed by the bare filename stem, so any
     * application image sharing a name with one of ours would be replaced by it, drawn at the
     * replacement's own size. Turn it on for an application that wants its GC-painted icons themed.
     * An {@code assets_path} override reaches a blit either way, since Java resolves it up front.
     */
    public boolean gc_icons_replacement;

    public boolean use_swt_colors;

    public boolean disable_swt_canvas_colors;

    public boolean use_swt_fonts;

    public boolean use_special_dropdown_button;

    public boolean preserve_icon_colors;
    
    public String force_theme;
    public String theme_name;
    public String theme_color;
    public Map<String, String> theme_colors_by_widget;
    public boolean show_theme_color_palette;

    public boolean show_scaling_control;
    public DecorationsAlign decorations_align;

    public boolean print_move;

    /** Client-Side Decorations placement and on/off in one value: "toolbar" (default),
     *  "overlay", "floating", or "false" (disabled). */
    public String csd_placement;
    /** Host OS for picking native control styling: "mac", "windows", or "linux". */
    public String csd_os;
    /** Maximize behavior: "direct" (default, Dart calls window.equo.maximize()), "bounds",
     *  "native", or "fullscreen" (the last three delegate to the SWT bridge). */
    public String csd_maximize;

    public static ConfigFlags use_swt_fonts(boolean v) {
        ConfigFlags configFlags = new ConfigFlags();
        configFlags.use_swt_fonts = v;
        return Config.setConfigFlags(configFlags);
    }

    @Override
    public String toString() {
        return "ConfigFlags{" +
                "mode=" + mode() +
                ", ctabfolder_visible_controls=" + ctabfolder_visible_controls +
                ", ctabfolder_topright_auto_hide=" + ctabfolder_topright_auto_hide +
                ", image_disable_icons_replacement=" + image_disable_icons_replacement +
                ", assets_path=" + assets_path +
                ", disable_evolve_icons=" + disable_evolve_icons +
                ", use_swt_colors=" + use_swt_colors +
                ", disable_swt_canvas_colors=" + disable_swt_canvas_colors +
                ", use_swt_fonts=" + use_swt_fonts +
                ", force_theme='" + force_theme + '\'' +
                ", theme_name='" + theme_name + '\'' +
                ", theme_color='" + theme_color + '\'' +
                ", theme_colors_by_widget=" + theme_colors_by_widget +
                ", show_theme_color_palette=" + show_theme_color_palette +
                ", preserve_icon_colors=" + preserve_icon_colors +
                ", show_scaling_control=" + show_scaling_control +
                ", decorations_align='" + decorations_align + '\'' +
                ", csd_placement='" + csd_placement + '\'' +
                ", csd_os='" + csd_os + '\'' +
                ", csd_maximize='" + csd_maximize + '\'' +
                '}';
    }
}

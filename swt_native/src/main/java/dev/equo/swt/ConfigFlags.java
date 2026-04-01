package dev.equo.swt;

import com.dslplatform.json.CompiledJson;

import java.util.Map;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class ConfigFlags {

    public boolean ctabfolder_visible_controls;

    public boolean image_disable_icons_replacement;

    public String assets_path;

    public boolean use_swt_colors;

    public boolean use_swt_fonts;

    public boolean use_special_dropdown_button;

    public boolean preserve_icon_colors;
    
    public String force_theme;
    public String theme_name;
    public String theme_color;
    public Map<String, String> theme_colors_by_widget;
    public boolean show_theme_color_palette;

    public static ConfigFlags use_swt_fonts(boolean v) {
        ConfigFlags configFlags = new ConfigFlags();
        configFlags.use_swt_fonts = v;
        return Config.setConfigFlags(configFlags);
    }

    @Override
    public String toString() {
        return "ConfigFlags{" +
                "ctabfolder_visible_controls=" + ctabfolder_visible_controls +
                ", image_disable_icons_replacement=" + image_disable_icons_replacement +
                ", assets_path=" + assets_path +
                ", use_swt_colors=" + use_swt_colors +
                ", use_swt_fonts=" + use_swt_fonts +
                ", force_theme='" + force_theme + '\'' +
                ", theme_name='" + theme_name + '\'' +
                ", theme_color='" + theme_color + '\'' +
                ", theme_colors_by_widget=" + theme_colors_by_widget +
                ", show_theme_color_palette=" + show_theme_color_palette +
                ", preserve_icon_colors=" + preserve_icon_colors +
                '}';
    }
}

package dev.equo.swt;

import com.dslplatform.json.CompiledJson;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class ConfigFlags {

    public boolean ctabfolder_visible_controls;

    public boolean image_disable_icons_replacement;

    public String assets_path;

    public boolean use_swt_colors;

    public boolean use_swt_fonts;

    public boolean use_special_dropdown_button;

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
                '}';
    }
}

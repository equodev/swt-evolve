package dev.equo.swt;

import com.dslplatform.json.CompiledJson;

@CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
public class ConfigFlags {

    public boolean ctabfolder_visible_controls;

    public boolean image_disable_icons_replacement;

    public String assets_path;

    @Override
    public String toString() {
        return "ConfigFlags{" +
                "ctabfolder_visible_controls=" + ctabfolder_visible_controls +
                ", image_disable_icons_replacement=" + image_disable_icons_replacement +
                ", assets_path=" + assets_path +
                '}';
    }
}

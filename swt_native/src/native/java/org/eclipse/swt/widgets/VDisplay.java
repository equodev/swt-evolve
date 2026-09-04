package org.eclipse.swt.widgets;

import com.dslplatform.json.*;
import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

/**
 * Value object for Display — serializes the list of visible shells to Flutter.
 * Display is NOT a Widget, so VDisplay does NOT extend VWidget.
 */
public class VDisplay {

    public long id;
    public String swt;
    public Shell[] shells;
    public Menu[] popups;

    /**
     * The application menu (macOS only, null elsewhere). The system menu bar is owned by the OS and
     * is out of reach whenever the tree renders in a browser tab, so the same menu is handed to the
     * client for the menu bar Evolve draws inside the window.
     */
    public Menu systemMenu;
    public ToolTip[] tooltips;
    public ConfigFlags config;

    /**
     * The shell SWT considers active, or 0 when none is. Filled in by the Display bridge, which
     * tracks focus; a shell only takes keyboard focus on the client when it is named here.
     */
    public long activeShellId;

    /**
     * The shell that drives (and is slaved to) the viewport, or 0 when none does. Filled in by the
     * Display bridge: parentage, modality, trim and the e4 workbench layout all decide this and
     * none of them reach the client, so the client renders the shell named here full-bleed and
     * every other shell as a window with its own chrome.
     */
    public long mainShellId;

    protected VDisplay() {
    }

    public static VDisplay of(DartDisplay display) {
        VDisplay v = new VDisplay();
        v.id = display.getApi().hashCode();
        v.swt = "Display";
        v.config = Config.getConfigFlags();
        Shell[] all = display._shells();
        ArrayList<Shell> visible = new ArrayList<>();
        for (Shell s : all) {
            if (s != null && !s.isDisposed() && s.getVisible()) {
                visible.add(s);
            }
        }
        v.shells = visible.toArray(Shell[]::new);
        Menu[] displayPopups = display.popups;
        ArrayList<Menu> popupList = new ArrayList<>();
        if (displayPopups != null) {
            for (Menu menu : displayPopups) {
                if (menu != null && !menu.isDisposed()) {
                    popupList.add(menu);
                }
            }
        }
        v.popups = popupList.toArray(Menu[]::new);
        Menu appMenu = DisplayBridgePlatform.systemMenu(display.getApi());
        v.systemMenu = appMenu != null && !appMenu.isDisposed() ? appMenu : null;
        ToolTip[] allTooltips = display._activeTooltips();
        ArrayList<ToolTip> tooltipList = new ArrayList<>();
        if (allTooltips != null) {
            for (ToolTip t : allTooltips) {
                if (t != null && !t.isDisposed()) {
                    tooltipList.add(t);
                }
            }
        }
        v.tooltips = tooltipList.toArray(ToolTip[]::new);
        return v;
    }

    @JsonConverter(target = VDisplay.class)
    public static class DisplayJson implements Configuration {

        private static volatile DslJson<?> dslJson;

        @Override
        public void configure(DslJson json) {
            dslJson = json;
            json.registerWriter(VDisplay.class, (JsonWriter.WriteObject<VDisplay>) (writer, v) -> write(writer, v));
            json.registerReader(VDisplay.class, (JsonReader.ReadObject<VDisplay>) reader -> null);
        }

        public static VDisplay read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, VDisplay v) {
            if (v == null) {
                writer.writeNull();
                return;
            }
            writer.writeByte((byte) '{');
            writer.writeAscii("\"id\":");
            com.dslplatform.json.NumberConverter.serialize(v.id, writer);
            writer.writeAscii(",\"swt\":");
            com.dslplatform.json.StringConverter.serialize(v.swt, writer);
            writer.writeAscii(",\"shells\":");
            if (v.shells == null || v.shells.length == 0) {
                writer.writeAscii("[]");
            } else {
                writer.writeByte((byte) '[');
                for (int i = 0; i < v.shells.length; i++) {
                    if (i > 0) writer.writeByte((byte) ',');
                    VShell.ShellJson.write(writer, v.shells[i]);
                }
                writer.writeByte((byte) ']');
            }
            writer.writeAscii(",\"popups\":");
            if (v.popups == null || v.popups.length == 0) {
                writer.writeAscii("[]");
            } else {
                writer.writeByte((byte) '[');
                for (int i = 0; i < v.popups.length; i++) {
                    if (i > 0) writer.writeByte((byte) ',');
                    VMenu.MenuJson.write(writer, v.popups[i]);
                }
                writer.writeByte((byte) ']');
            }
            if (v.systemMenu != null) {
                writer.writeAscii(",\"systemMenu\":");
                VMenu.MenuJson.write(writer, v.systemMenu);
            }
            writer.writeAscii(",\"tooltips\":");
            if (v.tooltips == null || v.tooltips.length == 0) {
                writer.writeAscii("[]");
            } else {
                writer.writeByte((byte) '[');
                for (int i = 0; i < v.tooltips.length; i++) {
                    if (i > 0) writer.writeByte((byte) ',');
                    VToolTip.ToolTipJson.write(writer, v.tooltips[i]);
                }
                writer.writeByte((byte) ']');
            }
            writer.writeAscii(",\"activeShellId\":");
            com.dslplatform.json.NumberConverter.serialize(v.activeShellId, writer);
            writer.writeAscii(",\"mainShellId\":");
            com.dslplatform.json.NumberConverter.serialize(v.mainShellId, writer);
            writeConfig(writer, v.config);
            writer.writeByte((byte) '}');
        }

        @SuppressWarnings("unchecked")
        private static void writeConfig(JsonWriter writer, ConfigFlags config) {
            DslJson<?> json = dslJson;
            if (config == null) return;
            JsonWriter.WriteObject<ConfigFlags> converter = json == null ? null
                    : (JsonWriter.WriteObject<ConfigFlags>) json.tryFindWriter(ConfigFlags.class);
            if (converter == null) {
                System.err.println("[VDisplay] no ConfigFlags converter; the client will render unthemed");
                return;
            }
            writer.writeAscii(",\"config\":");
            converter.write(writer, config);
        }
    }
}

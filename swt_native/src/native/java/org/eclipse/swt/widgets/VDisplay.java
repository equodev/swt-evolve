package org.eclipse.swt.widgets;

import com.dslplatform.json.*;
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
    public ToolTip[] tooltips;

    protected VDisplay() {
    }

    public static VDisplay of(DartDisplay display) {
        VDisplay v = new VDisplay();
        v.id = display.getApi().hashCode();
        v.swt = "Display";
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

        @Override
        public void configure(DslJson json) {
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
            writer.writeByte((byte) '}');
        }
    }
}

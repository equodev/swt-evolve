package org.eclipse.swt.widgets;

import com.dslplatform.json.*;

/**
 * The popup menus a shell must draw itself, sent on {@code Shell/{id}/Popups}.
 *
 * <p>Popups belong to the Display, and the Display's own client is the only one that draws
 * {@code VDisplay.popups}. A shell in a window of its own is a second client, rooted at that shell
 * and never listening on {@code Display/{id}}, so a popup opened over it would be drawn by the
 * application's window instead — the wrong window entirely, not merely the wrong place in the right
 * one. This carries them to the client that owns the control they were opened for; {@code VDisplay}
 * leaves out the ones sent here, so exactly one client draws each.
 *
 * <p>Write-only, like every value object on this side: the client renders it and never sends one
 * back, so the reader is a stub.
 */
public class VShellPopups {

    public long shellId;
    public Menu[] popups;

    @JsonConverter(target = VShellPopups.class)
    public static class ShellPopupsJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(VShellPopups.class,
                    (JsonWriter.WriteObject<VShellPopups>) (writer, v) -> write(writer, v));
            json.registerReader(VShellPopups.class,
                    (JsonReader.ReadObject<VShellPopups>) reader -> null);
        }

        public static VShellPopups read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, VShellPopups v) {
            if (v == null) {
                writer.writeNull();
                return;
            }
            writer.writeByte((byte) '{');
            writer.writeAscii("\"shellId\":");
            com.dslplatform.json.NumberConverter.serialize(v.shellId, writer);
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
            writer.writeByte((byte) '}');
        }
    }
}

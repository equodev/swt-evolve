package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VCTabFolder extends VComposite {

    public boolean borderVisible;

    public boolean minimized;

    public boolean minimizeVisible;

    public int minimumCharacters;

    public boolean maximized;

    public boolean maximizeVisible;

    public boolean mRUVisible;

    @JsonAttribute(ignore = true)
    public CTabFolderRenderer renderer;

    @JsonAttribute(ignore = true)
    public CTabItem selection;

    public Color selectionBackground;

    public Color selectionForeground;

    public boolean simple;

    public boolean single;

    public int tabHeight;

    public int tabPosition;

    public Control topRight;

    public boolean unselectedCloseVisible;

    public boolean unselectedImageVisible;

    public boolean selectedImageVisible;

    public boolean highlightEnabled;

    public int marginWidth;

    public int marginHeight;

    public int MIN_TAB_WIDTH;

    @JsonConverter(target = CTabFolder.class)
    public static class CTabFolderJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCTabFolder.class, (JsonWriter.WriteObject<DartCTabFolder>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCTabFolder.class, (JsonReader.ReadObject<DartCTabFolder>) reader -> {
                return null;
            });
        }

        public static CTabFolder read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, CTabFolder api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

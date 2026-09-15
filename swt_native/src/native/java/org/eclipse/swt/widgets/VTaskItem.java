package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VTaskItem extends VItem {

    protected VTaskItem() {
    }

    protected VTaskItem(DartTaskItem impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public Menu getMenu() {
        Menu val = ((DartTaskItem) impl).menu;
        if (val != null && !(val.getImpl() instanceof DartMenu))
            return null;
        return val;
    }

    public void setMenu(Menu value) {
        ((DartTaskItem) impl).menu = value;
    }

    @JsonAttribute(ignore = true)
    public Image getOverlayImage() {
        Image val = ((DartTaskItem) impl).overlayImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setOverlayImage(Image value) {
        ((DartTaskItem) impl).overlayImage = value;
    }

    @JsonAttribute(ignore = true)
    public String getOverlayText() {
        return ((DartTaskItem) impl).getOverlayText();
    }

    public void setOverlayText(String value) {
        ((DartTaskItem) impl).overlayText = value;
    }

    @JsonAttribute(ignore = true)
    public int getProgress() {
        return ((DartTaskItem) impl).getProgress();
    }

    public void setProgress(int value) {
        ((DartTaskItem) impl).progress = value;
    }

    @JsonAttribute(ignore = true)
    public int getProgressState() {
        return ((DartTaskItem) impl).getProgressState();
    }

    public void setProgressState(int value) {
        ((DartTaskItem) impl).progressState = value;
    }

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = TaskItem.class)
    public static class TaskItemJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartTaskItem.class, (JsonWriter.WriteObject<DartTaskItem>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartTaskItem.class, (JsonReader.ReadObject<DartTaskItem>) reader -> {
                return null;
            });
        }

        public static TaskItem read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, TaskItem api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

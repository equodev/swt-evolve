package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VViewForm extends VComposite {

    protected VViewForm() {
    }

    protected VViewForm(DartViewForm impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public boolean getBorderVisible() {
        return ((DartViewForm) impl).showBorder;
    }

    public void setBorderVisible(boolean value) {
        ((DartViewForm) impl).showBorder = value;
    }

    @JsonAttribute(ignore = true)
    public Control getContent() {
        Control val = ((DartViewForm) impl).content;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setContent(Control value) {
        ((DartViewForm) impl).content = value;
    }

    @JsonAttribute(ignore = true)
    public Control getTopCenter() {
        Control val = ((DartViewForm) impl).topCenter;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopCenter(Control value) {
        ((DartViewForm) impl).topCenter = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getTopCenterSeparate() {
        return ((DartViewForm) impl).separateTopCenter;
    }

    public void setTopCenterSeparate(boolean value) {
        ((DartViewForm) impl).separateTopCenter = value;
    }

    @JsonAttribute(ignore = true)
    public Control getTopLeft() {
        Control val = ((DartViewForm) impl).topLeft;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopLeft(Control value) {
        ((DartViewForm) impl).topLeft = value;
    }

    @JsonAttribute(ignore = true)
    public Control getTopRight() {
        Control val = ((DartViewForm) impl).topRight;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopRight(Control value) {
        ((DartViewForm) impl).topRight = value;
    }

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = ViewForm.class)
    public static class ViewFormJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartViewForm.class, (JsonWriter.WriteObject<DartViewForm>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartViewForm.class, (JsonReader.ReadObject<DartViewForm>) reader -> {
                return null;
            });
        }

        public static ViewForm read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, ViewForm api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

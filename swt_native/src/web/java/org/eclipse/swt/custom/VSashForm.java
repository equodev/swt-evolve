package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VSashForm extends VComposite {

    protected VSashForm() {
    }

    protected VSashForm(DartSashForm impl) {
        super(impl);
    }

    public Control getMaximizedControl() {
        Control val = ((DartSashForm) impl).maxControl;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setMaximizedControl(Control value) {
        ((DartSashForm) impl).maxControl = value;
    }

    public int getSashWidth() {
        return ((DartSashForm) impl).getSashWidth();
    }

    public void setSashWidth(int value) {
        ((DartSashForm) impl).getApi().SASH_WIDTH = value;
    }

    public int[] getWeights() {
        return ((DartSashForm) impl).weights;
    }

    public void setWeights(int[] value) {
        ((DartSashForm) impl).weights = value;
    }

    @JsonConverter(target = SashForm.class)
    public static class SashFormJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartSashForm.class, (JsonWriter.WriteObject<DartSashForm>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartSashForm.class, (JsonReader.ReadObject<DartSashForm>) reader -> {
                return null;
            });
        }

        public static SashForm read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, SashForm api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

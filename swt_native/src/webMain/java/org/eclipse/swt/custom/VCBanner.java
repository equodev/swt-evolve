package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VCBanner extends VComposite {

    protected VCBanner() {
    }

    protected VCBanner(DartCBanner impl) {
        super(impl);
    }

    public Control getBottom() {
        Control val = ((DartCBanner) impl).bottom;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setBottom(Control value) {
        ((DartCBanner) impl).bottom = value;
    }

    public Control getLeft() {
        Control val = ((DartCBanner) impl).left;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setLeft(Control value) {
        ((DartCBanner) impl).left = value;
    }

    public Control getRight() {
        Control val = ((DartCBanner) impl).right;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setRight(Control value) {
        ((DartCBanner) impl).right = value;
    }

    public Point getRightMinimumSize() {
        return ((DartCBanner) impl).rightMinimumSize;
    }

    public void setRightMinimumSize(Point value) {
        ((DartCBanner) impl).rightMinimumSize = value;
    }

    public int getRightWidth() {
        return ((DartCBanner) impl).getRightWidth();
    }

    public void setRightWidth(int value) {
        ((DartCBanner) impl).rightWidth = value;
    }

    public boolean getSimple() {
        return ((DartCBanner) impl).getSimple();
    }

    public void setSimple(boolean value) {
        ((DartCBanner) impl).simple = value;
    }

    @JsonConverter(target = CBanner.class)
    public static class CBannerJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCBanner.class, (JsonWriter.WriteObject<DartCBanner>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCBanner.class, (JsonReader.ReadObject<DartCBanner>) reader -> {
                return null;
            });
        }

        public static CBanner read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, CBanner api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

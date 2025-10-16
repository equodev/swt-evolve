package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VLink extends VControl {

    protected VLink() {
    }

    protected VLink(DartLink impl) {
        super(impl);
    }

    public Color getLinkForeground() {
        return ((DartLink) impl).linkColor;
    }

    public void setLinkForeground(Color value) {
        ((DartLink) impl).linkColor = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartLink) impl).text;
    }

    public void setText(String value) {
        ((DartLink) impl).text = value;
    }

    @JsonConverter(target = Link.class)
    public static class LinkJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartLink.class, (JsonWriter.WriteObject<DartLink>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartLink.class, (JsonReader.ReadObject<DartLink>) reader -> {
                return null;
            });
        }

        public static Link read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Link api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

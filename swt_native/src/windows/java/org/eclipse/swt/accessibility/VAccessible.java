package org.eclipse.swt.accessibility;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VAccessible {

    protected VAccessible() {
    }

    protected VAccessible(DartAccessible impl) {
        this.impl = impl;
    }

    protected DartAccessible impl;

    public Control getControl() {
        Control val = ((DartAccessible) impl).control;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setControl(Control value) {
        ((DartAccessible) impl).control = value;
    }

    public int getFocus() {
        return ((DartAccessible) impl).focus;
    }

    public void setFocus(int value) {
        ((DartAccessible) impl).focus = value;
    }

    @JsonConverter(target = Accessible.class)
    public static class AccessibleJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartAccessible.class, (JsonWriter.WriteObject<DartAccessible>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartAccessible.class, (JsonReader.ReadObject<DartAccessible>) reader -> {
                return null;
            });
        }

        public static Accessible read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Accessible api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

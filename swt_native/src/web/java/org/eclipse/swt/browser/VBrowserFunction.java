package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VBrowserFunction {

    protected VBrowserFunction() {
    }

    protected VBrowserFunction(DartBrowserFunction impl) {
        this.impl = impl;
    }

    protected DartBrowserFunction impl;

    public Browser getBrowser() {
        Browser val = ((DartBrowserFunction) impl).browser;
        if (val != null && !(val.getImpl() instanceof DartBrowser))
            return null;
        return val;
    }

    public void setBrowser(Browser value) {
        ((DartBrowserFunction) impl).browser = value;
    }

    public String getName() {
        return ((DartBrowserFunction) impl).getName();
    }

    public void setName(String value) {
        ((DartBrowserFunction) impl).name = value;
    }

    @JsonConverter(target = BrowserFunction.class)
    public static class BrowserFunctionJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartBrowserFunction.class, (JsonWriter.WriteObject<DartBrowserFunction>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartBrowserFunction.class, (JsonReader.ReadObject<DartBrowserFunction>) reader -> {
                return null;
            });
        }

        public static BrowserFunction read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, BrowserFunction api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

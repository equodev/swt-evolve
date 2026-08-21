package com.equo.chromium.swt;

import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import org.eclipse.swt.widgets.VComposite;

@CompiledJson()
public class VBrowser extends VComposite {

    protected VBrowser() {
    }

    protected VBrowser(DartBrowser impl) {
        super(impl);
    }

    public boolean getJavascriptEnabled() {
        return ((DartBrowser) impl).getJavascriptEnabled();
    }

    public void setJavascriptEnabled(boolean value) {
        ((DartBrowser) impl).javascriptEnabled = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartBrowser) impl).getText();
    }

    public void setText(String value) {
        ((DartBrowser) impl).text = value;
    }

    public String getUrl() {
        return ((DartBrowser) impl).getUrl();
    }

    public void setUrl(String value) {
        ((DartBrowser) impl).url = value;
    }

    @JsonAttribute(nullable = false)
    public String[] getFunctionNames() {
        return dev.equo.swt.BrowserFunctionRegistry
                .namesFor(dev.equo.swt.FlutterBridge.id(impl))
                .toArray(new String[0]);
    }

    // DSL-JSON only treats a getter as a serializable @CompiledJson property when it is
    // paired with a setter; this field is Java -> Dart only (BrowserFunctionRegistry is
    // the source of truth), so the setter has nothing to do.
    public void setFunctionNames(String[] value) {
    }

    @JsonConverter(target = Browser.class)
    public static class BrowserJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartBrowser.class, (JsonWriter.WriteObject<DartBrowser>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartBrowser.class, (JsonReader.ReadObject<DartBrowser>) reader -> {
                return null;
            });
        }

        public static Browser read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Browser api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

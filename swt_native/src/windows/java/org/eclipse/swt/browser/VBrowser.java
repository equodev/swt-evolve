package org.eclipse.swt.browser;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.program.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

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

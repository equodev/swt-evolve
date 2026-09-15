package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VDateTime extends VComposite {

    protected VDateTime() {
    }

    protected VDateTime(DartDateTime impl) {
        super(impl);
    }

    public int getDay() {
        return ((DartDateTime) impl).getDay();
    }

    public void setDay(int value) {
        ((DartDateTime) impl).day = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getHours() {
        return ((DartDateTime) impl).getHours();
    }

    public void setHours(int value) {
        ((DartDateTime) impl).hours = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getMinutes() {
        return ((DartDateTime) impl).getMinutes();
    }

    public void setMinutes(int value) {
        ((DartDateTime) impl).minutes = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getMonth() {
        return ((DartDateTime) impl).getMonth();
    }

    public void setMonth(int value) {
        ((DartDateTime) impl).month = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getSeconds() {
        return ((DartDateTime) impl).getSeconds();
    }

    public void setSeconds(int value) {
        ((DartDateTime) impl).seconds = value;
    }

    public int getYear() {
        return ((DartDateTime) impl).getYear();
    }

    public void setYear(int value) {
        ((DartDateTime) impl).year = value;
    }

    public static final String DAY = "day";

    public static final String HOURS = "hours";

    public static final String MINUTES = "minutes";

    public static final String MONTH = "month";

    public static final String SECONDS = "seconds";

    public static final String YEAR = "year";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "day":
                Serializer.writeKeyValue(writer, "day", getDay());
                return;
            case "hours":
                Serializer.writeKeyValue(writer, "hours", getHours());
                return;
            case "minutes":
                Serializer.writeKeyValue(writer, "minutes", getMinutes());
                return;
            case "month":
                Serializer.writeKeyValue(writer, "month", getMonth());
                return;
            case "seconds":
                Serializer.writeKeyValue(writer, "seconds", getSeconds());
                return;
            case "year":
                Serializer.writeKeyValue(writer, "year", getYear());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = DateTime.class)
    public static class DateTimeJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDateTime.class, (JsonWriter.WriteObject<DartDateTime>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartDateTime.class, (JsonReader.ReadObject<DartDateTime>) reader -> {
                return null;
            });
        }

        public static DateTime read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, DateTime api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

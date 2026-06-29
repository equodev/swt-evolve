package org.eclipse.swt.graphics;

import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

public class VDevice {

    protected VDevice() {
    }

    protected VDevice(IDevice impl) {
        this.impl = impl;
    }

    protected IDevice impl;

    @JsonAttribute(ignore = true)
    public DeviceData getDeviceData() {
        return ((DartDevice) impl).deviceData;
    }

    public void setDeviceData(DeviceData value) {
        ((DartDevice) impl).deviceData = value;
    }

    public FontData[] getFontList() {
        FontData[] values = ((DartDevice) impl).fontList;
        if (values == null)
            return null;
        ArrayList<FontData> result = new ArrayList<>(values.length);
        for (FontData v : values) if (v != null)
            result.add(v);
        return result.toArray(FontData[]::new);
    }

    public void setFontList(FontData[] value) {
        ((DartDevice) impl).fontList = value;
    }

    public boolean getTracking() {
        return ((DartDevice) impl).tracking;
    }

    public void setTracking(boolean value) {
        ((DartDevice) impl).tracking = value;
    }

    public boolean getWarnings() {
        return ((DartDevice) impl).getWarnings();
    }

    public void setWarnings(boolean value) {
        ((DartDevice) impl).warnings = value;
    }

    @JsonConverter(target = Device.class)
    public static class DeviceJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartDevice.class, (JsonWriter.WriteObject<DartDevice>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartDevice.class, (JsonReader.ReadObject<DartDevice>) reader -> {
                return null;
            });
        }

        public static Device read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Device api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

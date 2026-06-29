package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VPrinter extends VDevice {

    protected VPrinter() {
    }

    protected VPrinter(DartPrinter impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public PrinterData getPrinterData() {
        return ((DartPrinter) impl).data;
    }

    public void setPrinterData(PrinterData value) {
        ((DartPrinter) impl).data = value;
    }

    @JsonConverter(target = Printer.class)
    public static class PrinterJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPrinter.class, (JsonWriter.WriteObject<DartPrinter>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPrinter.class, (JsonReader.ReadObject<DartPrinter>) reader -> {
                return null;
            });
        }

        public static Printer read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Printer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

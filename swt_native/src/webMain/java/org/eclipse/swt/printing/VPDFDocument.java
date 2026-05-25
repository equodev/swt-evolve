package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VPDFDocument extends VDevice {

    protected VPDFDocument() {
    }

    protected VPDFDocument(DartPDFDocument impl) {
        super(impl);
    }

    public double getHeight() {
        return ((DartPDFDocument) impl).getHeight();
    }

    public void setHeight(double value) {
        ((DartPDFDocument) impl).heightInPoints = value;
    }

    public double getWidth() {
        return ((DartPDFDocument) impl).getWidth();
    }

    public void setWidth(double value) {
        ((DartPDFDocument) impl).widthInPoints = value;
    }

    @JsonConverter(target = PDFDocument.class)
    public static class PDFDocumentJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPDFDocument.class, (JsonWriter.WriteObject<DartPDFDocument>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPDFDocument.class, (JsonReader.ReadObject<DartPDFDocument>) reader -> {
                return null;
            });
        }

        public static PDFDocument read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, PDFDocument api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

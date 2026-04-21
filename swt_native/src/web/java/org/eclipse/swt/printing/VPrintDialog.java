package org.eclipse.swt.printing;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VPrintDialog extends VDialog {

    protected VPrintDialog() {
    }

    protected VPrintDialog(DartPrintDialog impl) {
        super(impl);
    }

    public int getEndPage() {
        return ((DartPrintDialog) impl).getEndPage();
    }

    public void setEndPage(int value) {
        ((DartPrintDialog) impl).endPage = value;
    }

    public boolean getPrintToFile() {
        return ((DartPrintDialog) impl).getPrintToFile();
    }

    public void setPrintToFile(boolean value) {
        ((DartPrintDialog) impl).printToFile = value;
    }

    @JsonAttribute(ignore = true)
    public PrinterData getPrinterData() {
        return ((DartPrintDialog) impl).printerData;
    }

    public void setPrinterData(PrinterData value) {
        ((DartPrintDialog) impl).printerData = value;
    }

    public int getScope() {
        return ((DartPrintDialog) impl).getScope();
    }

    public void setScope(int value) {
        ((DartPrintDialog) impl).scope = value;
    }

    public int getStartPage() {
        return ((DartPrintDialog) impl).getStartPage();
    }

    public void setStartPage(int value) {
        ((DartPrintDialog) impl).startPage = value;
    }

    @JsonConverter(target = PrintDialog.class)
    public static class PrintDialogJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartPrintDialog.class, (JsonWriter.WriteObject<DartPrintDialog>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartPrintDialog.class, (JsonReader.ReadObject<DartPrintDialog>) reader -> {
                return null;
            });
        }

        public static PrintDialog read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, PrintDialog api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

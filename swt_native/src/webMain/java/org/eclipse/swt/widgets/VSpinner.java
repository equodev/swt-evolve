package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VSpinner extends VComposite {

    protected VSpinner() {
    }

    protected VSpinner(DartSpinner impl) {
        super(impl);
    }

    public int getDigits() {
        return ((DartSpinner) impl).getDigits();
    }

    public void setDigits(int value) {
        ((DartSpinner) impl).digits = value;
    }

    public int getIncrement() {
        return ((DartSpinner) impl).getIncrement();
    }

    public void setIncrement(int value) {
        ((DartSpinner) impl).increment = value;
    }

    public int getMaximum() {
        return ((DartSpinner) impl).getMaximum();
    }

    public void setMaximum(int value) {
        ((DartSpinner) impl).maximum = value;
    }

    public int getMinimum() {
        return ((DartSpinner) impl).getMinimum();
    }

    public void setMinimum(int value) {
        ((DartSpinner) impl).minimum = value;
    }

    public int getPageIncrement() {
        return ((DartSpinner) impl).getPageIncrement();
    }

    public void setPageIncrement(int value) {
        ((DartSpinner) impl).pageIncrement = value;
    }

    public int getSelection() {
        return ((DartSpinner) impl).getSelection();
    }

    public void setSelection(int value) {
        ((DartSpinner) impl).selection = value;
    }

    public int getTextLimit() {
        return ((DartSpinner) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartSpinner) impl).textLimit = value;
    }

    @JsonConverter(target = Spinner.class)
    public static class SpinnerJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartSpinner.class, (JsonWriter.WriteObject<DartSpinner>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartSpinner.class, (JsonReader.ReadObject<DartSpinner>) reader -> {
                return null;
            });
        }

        public static Spinner read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, Spinner api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

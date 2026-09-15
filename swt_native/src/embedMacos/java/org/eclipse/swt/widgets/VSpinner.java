package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

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

    public static final String DIGITS = "digits";

    public static final String INCREMENT = "increment";

    public static final String MAXIMUM = "maximum";

    public static final String MINIMUM = "minimum";

    public static final String PAGE_INCREMENT = "pageIncrement";

    public static final String SELECTION = "selection";

    public static final String TEXT_LIMIT = "textLimit";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "digits":
                Serializer.writeKeyValue(writer, "digits", getDigits());
                return;
            case "increment":
                Serializer.writeKeyValue(writer, "increment", getIncrement());
                return;
            case "maximum":
                Serializer.writeKeyValue(writer, "maximum", getMaximum());
                return;
            case "minimum":
                Serializer.writeKeyValue(writer, "minimum", getMinimum());
                return;
            case "pageIncrement":
                Serializer.writeKeyValue(writer, "pageIncrement", getPageIncrement());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "textLimit":
                Serializer.writeKeyValue(writer, "textLimit", getTextLimit());
                return;
        }
        super.writeProperty(writer, key);
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

        public static Spinner read(JsonReader<?> reader) throws IOException {
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

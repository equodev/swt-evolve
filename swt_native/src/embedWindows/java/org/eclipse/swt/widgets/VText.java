package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VText extends VScrollable {

    protected VText() {
    }

    protected VText(DartText impl) {
        super(impl);
    }

    public int getCaretPosition() {
        return ((DartText) impl).getCaretPosition();
    }

    public void setCaretPosition(int value) {
        ((DartText) impl).caretPosition = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getDoubleClickEnabled() {
        return ((DartText) impl).getDoubleClickEnabled();
    }

    public void setDoubleClickEnabled(boolean value) {
        ((DartText) impl).doubleClick = value;
    }

    public int getEchoCharacter() {
        return ((DartText) impl).getEchoChar();
    }

    public void setEchoCharacter(int value) {
        ((DartText) impl).echoCharacter = (char) value;
    }

    public boolean getEditable() {
        return ((DartText) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartText) impl).editable = value;
    }

    @JsonAttribute(ignore = true)
    public char[] getHiddenText() {
        return ((DartText) impl).hiddenText;
    }

    public void setHiddenText(char[] value) {
        ((DartText) impl).hiddenText = value;
    }

    public String getMessage() {
        return ((DartText) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartText) impl).message = value;
    }

    public Point getSelection() {
        return ((DartText) impl).getSelection();
    }

    public void setSelection(Point value) {
        ((DartText) impl).selection = value;
    }

    @JsonAttribute(ignore = true)
    public int getTabs() {
        return ((DartText) impl).getTabs();
    }

    public void setTabs(int value) {
        ((DartText) impl).tabs = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartText) impl).getText();
    }

    public void setText(String value) {
        ((DartText) impl).text = value;
    }

    @JsonAttribute(ignore = true)
    public char[] getTextChars() {
        return ((DartText) impl).textChars;
    }

    public void setTextChars(char[] value) {
        ((DartText) impl).textChars = value;
    }

    public int getTextLimit() {
        return ((DartText) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartText) impl).textLimit = value;
    }

    @JsonAttribute(ignore = true)
    public int getTopIndex() {
        return ((DartText) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartText) impl).topIndex = value;
    }

    public static final String CARET_POSITION = "caretPosition";

    public static final String ECHO_CHARACTER = "echoCharacter";

    public static final String EDITABLE = "editable";

    public static final String MESSAGE = "message";

    public static final String SELECTION = "selection";

    public static final String TEXT = "text";

    public static final String TEXT_LIMIT = "textLimit";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "caretPosition":
                Serializer.writeKeyValue(writer, "caretPosition", getCaretPosition());
                return;
            case "echoCharacter":
                Serializer.writeKeyValue(writer, "echoCharacter", getEchoCharacter());
                return;
            case "editable":
                Serializer.writeKeyValue(writer, "editable", getEditable());
                return;
            case "message":
                Serializer.writeKeyValue(writer, "message", getMessage());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "text":
                Serializer.writeKeyValue(writer, "text", getText());
                return;
            case "textLimit":
                Serializer.writeKeyValue(writer, "textLimit", getTextLimit());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Text.class)
    public static class TextJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartText.class, (JsonWriter.WriteObject<DartText>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartText.class, (JsonReader.ReadObject<DartText>) reader -> {
                return null;
            });
        }

        public static Text read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Text api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

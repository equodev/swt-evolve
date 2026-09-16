package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VShell extends VDecorations {

    protected VShell() {
    }

    protected VShell(DartShell impl) {
        super(impl);
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getAlpha() {
        return ((DartShell) impl).getAlpha();
    }

    public void setAlpha(int value) {
        ((DartShell) impl).alpha = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getDarkThemePreferred() {
        return ((DartShell) impl).darkThemePreferred;
    }

    public void setDarkThemePreferred(boolean value) {
        ((DartShell) impl).darkThemePreferred = value;
    }

    @JsonAttribute(nullable = true)
    public Dialog[] getDialogs() {
        return ((DartShell) impl).getDialogs();
    }

    public void setDialogs(Dialog[] value) {
        ((DartShell) impl).dialogs = value;
    }

    public boolean getFullScreen() {
        return ((DartShell) impl).getFullScreen();
    }

    public void setFullScreen(boolean value) {
        ((DartShell) impl).fullScreen = value;
    }

    @JsonAttribute(ignore = true)
    public int getImeInputMode() {
        return ((DartShell) impl).getImeInputMode();
    }

    public void setImeInputMode(int value) {
        ((DartShell) impl).imeInputMode = value;
    }

    public Point getMaximumSize() {
        return ((DartShell) impl).maximumSize;
    }

    public void setMaximumSize(Point value) {
        ((DartShell) impl).maximumSize = value;
    }

    public Point getMinimumSize() {
        return ((DartShell) impl).minimumSize;
    }

    public void setMinimumSize(Point value) {
        ((DartShell) impl).minimumSize = value;
    }

    public boolean getModified() {
        return ((DartShell) impl).getModified();
    }

    public void setModified(boolean value) {
        ((DartShell) impl).modified = value;
    }

    @JsonAttribute(ignore = true)
    public Shell[] getShells() {
        Shell[] values = ((DartShell) impl).shells;
        if (values == null)
            return null;
        ArrayList<Shell> result = new ArrayList<>(values.length);
        for (Shell v : values) if (v != null)
            result.add(v);
        return result.toArray(new Shell[0]);
    }

    public void setShells(Shell[] value) {
        ((DartShell) impl).shells = value;
    }

    public static final String ALPHA = "alpha";

    public static final String DIALOGS = "dialogs";

    public static final String FULL_SCREEN = "fullScreen";

    public static final String MAXIMUM_SIZE = "maximumSize";

    public static final String MINIMUM_SIZE = "minimumSize";

    public static final String MODIFIED = "modified";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alpha":
                Serializer.writeKeyValue(writer, "alpha", getAlpha());
                return;
            case "dialogs":
                Serializer.writeKeyValue(writer, "dialogs", getDialogs());
                return;
            case "fullScreen":
                Serializer.writeKeyValue(writer, "fullScreen", getFullScreen());
                return;
            case "maximumSize":
                Serializer.writeKeyValue(writer, "maximumSize", getMaximumSize());
                return;
            case "minimumSize":
                Serializer.writeKeyValue(writer, "minimumSize", getMinimumSize());
                return;
            case "modified":
                Serializer.writeKeyValue(writer, "modified", getModified());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = Shell.class)
    public static class ShellJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartShell.class, (JsonWriter.WriteObject<DartShell>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartShell.class, (JsonReader.ReadObject<DartShell>) reader -> {
                return null;
            });
        }

        public static Shell read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Shell api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VScrolledComposite extends VComposite {

    protected VScrolledComposite() {
    }

    protected VScrolledComposite(DartScrolledComposite impl) {
        super(impl);
    }

    public boolean getAlwaysShowScrollBars() {
        return ((DartScrolledComposite) impl).getAlwaysShowScrollBars();
    }

    public void setAlwaysShowScrollBars(boolean value) {
        ((DartScrolledComposite) impl).alwaysShowScroll = value;
    }

    public Control getContent() {
        Control val = ((DartScrolledComposite) impl).content;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setContent(Control value) {
        ((DartScrolledComposite) impl).content = value;
    }

    public boolean getExpandHorizontal() {
        return ((DartScrolledComposite) impl).getExpandHorizontal();
    }

    public void setExpandHorizontal(boolean value) {
        ((DartScrolledComposite) impl).expandHorizontal = value;
    }

    public boolean getExpandVertical() {
        return ((DartScrolledComposite) impl).getExpandVertical();
    }

    public void setExpandVertical(boolean value) {
        ((DartScrolledComposite) impl).expandVertical = value;
    }

    public int getMinHeight() {
        return ((DartScrolledComposite) impl).getMinHeight();
    }

    public void setMinHeight(int value) {
        ((DartScrolledComposite) impl).minHeight = value;
    }

    public int getMinSize() {
        return ((DartScrolledComposite) impl).minWidth;
    }

    public void setMinSize(int value) {
        ((DartScrolledComposite) impl).minWidth = value;
    }

    public int getMinWidth() {
        return ((DartScrolledComposite) impl).getMinWidth();
    }

    public void setMinWidth(int value) {
        ((DartScrolledComposite) impl).minWidth = value;
    }

    public Point getOrigin() {
        return ((DartScrolledComposite) impl).getOrigin();
    }

    public void setOrigin(Point value) {
        ((DartScrolledComposite) impl).origin = value;
    }

    public boolean getShowFocusedControl() {
        return ((DartScrolledComposite) impl).getShowFocusedControl();
    }

    public void setShowFocusedControl(boolean value) {
        ((DartScrolledComposite) impl).showFocusedControl = value;
    }

    @JsonConverter(target = ScrolledComposite.class)
    public static class ScrolledCompositeJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartScrolledComposite.class, (JsonWriter.WriteObject<DartScrolledComposite>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartScrolledComposite.class, (JsonReader.ReadObject<DartScrolledComposite>) reader -> {
                return null;
            });
        }

        public static ScrolledComposite read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, ScrolledComposite api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

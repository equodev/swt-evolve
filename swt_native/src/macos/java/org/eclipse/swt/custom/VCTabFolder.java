package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.util.ArrayList;

@CompiledJson()
public class VCTabFolder extends VComposite {

    protected VCTabFolder() {
    }

    protected VCTabFolder(DartCTabFolder impl) {
        super(impl);
    }

    public boolean getBorderVisible() {
        return ((DartCTabFolder) impl).getBorderVisible();
    }

    public void setBorderVisible(boolean value) {
        ((DartCTabFolder) impl).borderVisible = value;
    }

    public CTabItem[] getItems() {
        CTabItem[] values = ((DartCTabFolder) impl).items;
        if (values == null)
            return null;
        ArrayList<CTabItem> result = new ArrayList<>(values.length);
        for (CTabItem v : values) if (v != null)
            result.add(v);
        return result.toArray(CTabItem[]::new);
    }

    public void setItems(CTabItem[] value) {
        ((DartCTabFolder) impl).items = value;
    }

    public boolean getMinimized() {
        return ((DartCTabFolder) impl).getMinimized();
    }

    public void setMinimized(boolean value) {
        ((DartCTabFolder) impl).minimized = value;
    }

    public boolean getShowMin() {
        return ((DartCTabFolder) impl).getMinimizeVisible();
    }

    public void setShowMin(boolean value) {
        ((DartCTabFolder) impl).showMin = value;
    }

    public int getMinChars() {
        return ((DartCTabFolder) impl).getMinimumCharacters();
    }

    public void setMinChars(int value) {
        ((DartCTabFolder) impl).minChars = value;
    }

    public boolean getMaximized() {
        return ((DartCTabFolder) impl).getMaximized();
    }

    public void setMaximized(boolean value) {
        ((DartCTabFolder) impl).maximized = value;
    }

    public boolean getShowMax() {
        return ((DartCTabFolder) impl).getMaximizeVisible();
    }

    public void setShowMax(boolean value) {
        ((DartCTabFolder) impl).showMax = value;
    }

    public boolean getMru() {
        return ((DartCTabFolder) impl).getMRUVisible();
    }

    public void setMru(boolean value) {
        ((DartCTabFolder) impl).mru = value;
    }

    public int getSelectedIndex() {
        return ((DartCTabFolder) impl).selectedIndex;
    }

    public void setSelectedIndex(int value) {
        ((DartCTabFolder) impl).selectedIndex = value;
    }

    public Color getSelectionBackground() {
        return ((DartCTabFolder) impl).selectionBackground;
    }

    public void setSelectionBackground(Color value) {
        ((DartCTabFolder) impl).selectionBackground = value;
    }

    public Color getSelectionForeground() {
        return ((DartCTabFolder) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartCTabFolder) impl).selectionForeground = value;
    }

    public boolean getSimple() {
        return ((DartCTabFolder) impl).getSimple();
    }

    public void setSimple(boolean value) {
        ((DartCTabFolder) impl).simple = value;
    }

    public boolean getSingle() {
        return ((DartCTabFolder) impl).getSingle();
    }

    public void setSingle(boolean value) {
        ((DartCTabFolder) impl).single = value;
    }

    public Control getTopRight() {
        Control val = ((DartCTabFolder) impl).topRight;
        if (val != null && !(val.getImpl() instanceof DartControl))
            return null;
        return val;
    }

    public void setTopRight(Control value) {
        ((DartCTabFolder) impl).topRight = value;
    }

    public int getTopRightAlignment() {
        return ((DartCTabFolder) impl).getTopRightAlignment();
    }

    public void setTopRightAlignment(int value) {
        ((DartCTabFolder) impl).topRightAlignment = value;
    }

    public boolean getShowUnselectedClose() {
        return ((DartCTabFolder) impl).getUnselectedCloseVisible();
    }

    public void setShowUnselectedClose(boolean value) {
        ((DartCTabFolder) impl).showUnselectedClose = value;
    }

    public boolean getShowUnselectedImage() {
        return ((DartCTabFolder) impl).getUnselectedImageVisible();
    }

    public void setShowUnselectedImage(boolean value) {
        ((DartCTabFolder) impl).showUnselectedImage = value;
    }

    public boolean getShowSelectedImage() {
        return ((DartCTabFolder) impl).getSelectedImageVisible();
    }

    public void setShowSelectedImage(boolean value) {
        ((DartCTabFolder) impl).showSelectedImage = value;
    }

    public Color[] getGradientColors() {
        Color[] values = ((DartCTabFolder) impl).gradientColors;
        if (values == null)
            return null;
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(Color[]::new);
    }

    public void setGradientColors(Color[] value) {
        ((DartCTabFolder) impl).gradientColors = value;
    }

    public int[] getGradientPercents() {
        return ((DartCTabFolder) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCTabFolder) impl).gradientPercents = value;
    }

    public boolean getGradientVertical() {
        return ((DartCTabFolder) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCTabFolder) impl).gradientVertical = value;
    }

    @JsonAttribute(ignore = true)
    public Font getOldFont() {
        return ((DartCTabFolder) impl).oldFont;
    }

    public void setOldFont(Font value) {
        ((DartCTabFolder) impl).oldFont = value;
    }

    public Color[] getSelectionGradientColors() {
        Color[] values = ((DartCTabFolder) impl).selectionGradientColors;
        if (values == null)
            return null;
        ArrayList<Color> result = new ArrayList<>(values.length);
        for (Color v : values) if (v != null)
            result.add(v);
        return result.toArray(Color[]::new);
    }

    public void setSelectionGradientColors(Color[] value) {
        ((DartCTabFolder) impl).selectionGradientColors = value;
    }

    public int[] getSelectionGradientPercents() {
        return ((DartCTabFolder) impl).selectionGradientPercents;
    }

    public void setSelectionGradientPercents(int[] value) {
        ((DartCTabFolder) impl).selectionGradientPercents = value;
    }

    public boolean getSelectionGradientVertical() {
        return ((DartCTabFolder) impl).selectionGradientVertical;
    }

    public void setSelectionGradientVertical(boolean value) {
        ((DartCTabFolder) impl).selectionGradientVertical = value;
    }

    @JsonAttribute(ignore = true)
    public Image getSelectionBgImage() {
        return ((DartCTabFolder) impl).selectionBgImage;
    }

    public void setSelectionBgImage(Image value) {
        ((DartCTabFolder) impl).selectionBgImage = value;
    }

    public int getSelectionHighlightBarThickness() {
        return ((DartCTabFolder) impl).selectionHighlightBarThickness;
    }

    public void setSelectionHighlightBarThickness(int value) {
        ((DartCTabFolder) impl).selectionHighlightBarThickness = value;
    }

    public int getFixedTabHeight() {
        return ((DartCTabFolder) impl).fixedTabHeight;
    }

    public void setFixedTabHeight(int value) {
        ((DartCTabFolder) impl).fixedTabHeight = value;
    }

    public boolean getOnBottom() {
        return ((DartCTabFolder) impl).onBottom;
    }

    public void setOnBottom(boolean value) {
        ((DartCTabFolder) impl).onBottom = value;
    }

    public boolean getHighlightEnabled() {
        return ((DartCTabFolder) impl).getHighlightEnabled();
    }

    public void setHighlightEnabled(boolean value) {
        ((DartCTabFolder) impl).highlightEnabled = value;
    }

    @JsonConverter(target = CTabFolder.class)
    public static class CTabFolderJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartCTabFolder.class, (JsonWriter.WriteObject<DartCTabFolder>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartCTabFolder.class, (JsonReader.ReadObject<DartCTabFolder>) reader -> {
                return null;
            });
        }

        public static CTabFolder read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, CTabFolder api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;
import java.util.ArrayList;

@CompiledJson()
public class VCTabFolder extends VComposite {

    protected VCTabFolder() {
    }

    protected VCTabFolder(DartCTabFolder impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public boolean getMRUVisible() {
        return ((DartCTabFolder) impl).getMRUVisible();
    }

    public void setMRUVisible(boolean value) {
        ((DartCTabFolder) impl).mru = value;
    }

    public boolean getBorderVisible() {
        return ((DartCTabFolder) impl).getBorderVisible();
    }

    public void setBorderVisible(boolean value) {
        ((DartCTabFolder) impl).borderVisible = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public boolean getChevronVisible() {
        return ((DartCTabFolder) impl).chevronVisible;
    }

    public void setChevronVisible(boolean value) {
    }

    @JsonAttribute(ignore = true)
    public boolean getDirtyIndicatorStyle() {
        return ((DartCTabFolder) impl).getDirtyIndicatorStyle();
    }

    public void setDirtyIndicatorStyle(boolean value) {
        ((DartCTabFolder) impl).dirtyIndicatorStyle = value;
    }

    @JsonAttribute(ignore = true)
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

    @JsonAttribute(ignore = true)
    public int[] getGradientPercents() {
        return ((DartCTabFolder) impl).gradientPercents;
    }

    public void setGradientPercents(int[] value) {
        ((DartCTabFolder) impl).gradientPercents = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getGradientVertical() {
        return ((DartCTabFolder) impl).gradientVertical;
    }

    public void setGradientVertical(boolean value) {
        ((DartCTabFolder) impl).gradientVertical = value;
    }

    public boolean getHighlight() {
        return ((DartCTabFolder) impl).highlight;
    }

    public void setHighlight(boolean value) {
    }

    public boolean getHighlightEnabled() {
        return ((DartCTabFolder) impl).getHighlightEnabled();
    }

    public void setHighlightEnabled(boolean value) {
        ((DartCTabFolder) impl).highlightEnabled = value;
    }

    public CTabItem[] getItems() {
        CTabItem[] values = ((DartCTabFolder) impl).getItems();
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

    public boolean getMaximizeVisible() {
        return ((DartCTabFolder) impl).getMaximizeVisible();
    }

    public void setMaximizeVisible(boolean value) {
        ((DartCTabFolder) impl).showMax = value;
    }

    public boolean getMaximized() {
        return ((DartCTabFolder) impl).getMaximized();
    }

    public void setMaximized(boolean value) {
        ((DartCTabFolder) impl).maximized = value;
    }

    public boolean getMinimizeVisible() {
        return ((DartCTabFolder) impl).getMinimizeVisible();
    }

    public void setMinimizeVisible(boolean value) {
        ((DartCTabFolder) impl).showMin = value;
    }

    public boolean getMinimized() {
        return ((DartCTabFolder) impl).getMinimized();
    }

    public void setMinimized(boolean value) {
        ((DartCTabFolder) impl).minimized = value;
    }

    public int getMinimumCharacters() {
        return ((DartCTabFolder) impl).getMinimumCharacters();
    }

    public void setMinimumCharacters(int value) {
        ((DartCTabFolder) impl).minChars = value;
    }

    public boolean getSelectedImageVisible() {
        return ((DartCTabFolder) impl).getSelectedImageVisible();
    }

    public void setSelectedImageVisible(boolean value) {
        ((DartCTabFolder) impl).showSelectedImage = value;
    }

    public int getSelection() {
        return ((DartCTabFolder) impl).selectedIndex;
    }

    public void setSelection(int value) {
        ((DartCTabFolder) impl).selectedIndex = value;
    }

    public Color getSelectionBackground() {
        return ((DartCTabFolder) impl).selectionBackground;
    }

    public void setSelectionBackground(Color value) {
        ((DartCTabFolder) impl).selectionBackground = value;
    }

    public int getSelectionBarThickness() {
        return ((DartCTabFolder) impl).selectionHighlightBarThickness;
    }

    public void setSelectionBarThickness(int value) {
        ((DartCTabFolder) impl).selectionHighlightBarThickness = value;
    }

    public Image getSelectionBgImage() {
        Image val = ((DartCTabFolder) impl).selectionBgImage;
        if (val != null && !(val.getImpl() instanceof DartImage))
            return null;
        return val;
    }

    public void setSelectionBgImage(Image value) {
        ((DartCTabFolder) impl).selectionBgImage = value;
    }

    public Color getSelectionForeground() {
        return ((DartCTabFolder) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartCTabFolder) impl).selectionForeground = value;
    }

    @JsonAttribute(ignore = true)
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

    @JsonAttribute(ignore = true)
    public int[] getSelectionGradientPercents() {
        return ((DartCTabFolder) impl).selectionGradientPercents;
    }

    public void setSelectionGradientPercents(int[] value) {
        ((DartCTabFolder) impl).selectionGradientPercents = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getSelectionGradientVertical() {
        return ((DartCTabFolder) impl).selectionGradientVertical;
    }

    public void setSelectionGradientVertical(boolean value) {
        ((DartCTabFolder) impl).selectionGradientVertical = value;
    }

    public boolean getShowChevron() {
        return ((DartCTabFolder) impl).showChevron;
    }

    public void setShowChevron(boolean value) {
    }

    public int getShowListPopupSeq() {
        return ((DartCTabFolder) impl).showListPopupSeq;
    }

    public void setShowListPopupSeq(int value) {
    }

    @JsonAttribute(ignore = true)
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

    @JsonAttribute(ignore = true)
    public int getTabHeight() {
        return ((DartCTabFolder) impl).getTabHeight();
    }

    public void setTabHeight(int value) {
        ((DartCTabFolder) impl).fixedTabHeight = value;
    }

    public int getTabPosition() {
        return ((DartCTabFolder) impl).getTabPosition();
    }

    public void setTabPosition(int value) {
        ((DartCTabFolder) impl).tabPosition = value;
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

    public boolean getUnselectedCloseVisible() {
        return ((DartCTabFolder) impl).getUnselectedCloseVisible();
    }

    public void setUnselectedCloseVisible(boolean value) {
        ((DartCTabFolder) impl).showUnselectedClose = value;
    }

    public boolean getUnselectedImageVisible() {
        return ((DartCTabFolder) impl).getUnselectedImageVisible();
    }

    public void setUnselectedImageVisible(boolean value) {
        ((DartCTabFolder) impl).showUnselectedImage = value;
    }

    public static final String BORDER_VISIBLE = "borderVisible";

    public static final String CHEVRON_VISIBLE = "chevronVisible";

    public static final String HIGHLIGHT = "highlight";

    public static final String HIGHLIGHT_ENABLED = "highlightEnabled";

    public static final String ITEMS = "items";

    public static final String MAXIMIZE_VISIBLE = "maximizeVisible";

    public static final String MAXIMIZED = "maximized";

    public static final String MINIMIZE_VISIBLE = "minimizeVisible";

    public static final String MINIMIZED = "minimized";

    public static final String MINIMUM_CHARACTERS = "minimumCharacters";

    public static final String SELECTED_IMAGE_VISIBLE = "selectedImageVisible";

    public static final String SELECTION = "selection";

    public static final String SELECTION_BACKGROUND = "selectionBackground";

    public static final String SELECTION_BAR_THICKNESS = "selectionBarThickness";

    public static final String SELECTION_BG_IMAGE = "selectionBgImage";

    public static final String SELECTION_FOREGROUND = "selectionForeground";

    public static final String SHOW_CHEVRON = "showChevron";

    public static final String SHOW_LIST_POPUP_SEQ = "showListPopupSeq";

    public static final String SINGLE = "single";

    public static final String TAB_POSITION = "tabPosition";

    public static final String TOP_RIGHT = "topRight";

    public static final String TOP_RIGHT_ALIGNMENT = "topRightAlignment";

    public static final String UNSELECTED_CLOSE_VISIBLE = "unselectedCloseVisible";

    public static final String UNSELECTED_IMAGE_VISIBLE = "unselectedImageVisible";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "borderVisible":
                Serializer.writeKeyValue(writer, "borderVisible", getBorderVisible());
                return;
            case "chevronVisible":
                Serializer.writeKeyValue(writer, "chevronVisible", getChevronVisible());
                return;
            case "highlight":
                Serializer.writeKeyValue(writer, "highlight", getHighlight());
                return;
            case "highlightEnabled":
                Serializer.writeKeyValue(writer, "highlightEnabled", getHighlightEnabled());
                return;
            case "items":
                Serializer.writeKeyValue(writer, "items", getItems());
                return;
            case "maximizeVisible":
                Serializer.writeKeyValue(writer, "maximizeVisible", getMaximizeVisible());
                return;
            case "maximized":
                Serializer.writeKeyValue(writer, "maximized", getMaximized());
                return;
            case "minimizeVisible":
                Serializer.writeKeyValue(writer, "minimizeVisible", getMinimizeVisible());
                return;
            case "minimized":
                Serializer.writeKeyValue(writer, "minimized", getMinimized());
                return;
            case "minimumCharacters":
                Serializer.writeKeyValue(writer, "minimumCharacters", getMinimumCharacters());
                return;
            case "selectedImageVisible":
                Serializer.writeKeyValue(writer, "selectedImageVisible", getSelectedImageVisible());
                return;
            case "selection":
                Serializer.writeKeyValue(writer, "selection", getSelection());
                return;
            case "selectionBackground":
                Serializer.writeKeyValue(writer, "selectionBackground", getSelectionBackground());
                return;
            case "selectionBarThickness":
                Serializer.writeKeyValue(writer, "selectionBarThickness", getSelectionBarThickness());
                return;
            case "selectionBgImage":
                Serializer.writeKeyValue(writer, "selectionBgImage", getSelectionBgImage());
                return;
            case "selectionForeground":
                Serializer.writeKeyValue(writer, "selectionForeground", getSelectionForeground());
                return;
            case "showChevron":
                Serializer.writeKeyValue(writer, "showChevron", getShowChevron());
                return;
            case "showListPopupSeq":
                Serializer.writeKeyValue(writer, "showListPopupSeq", getShowListPopupSeq());
                return;
            case "single":
                Serializer.writeKeyValue(writer, "single", getSingle());
                return;
            case "tabPosition":
                Serializer.writeKeyValue(writer, "tabPosition", getTabPosition());
                return;
            case "topRight":
                Serializer.writeKeyValue(writer, "topRight", getTopRight());
                return;
            case "topRightAlignment":
                Serializer.writeKeyValue(writer, "topRightAlignment", getTopRightAlignment());
                return;
            case "unselectedCloseVisible":
                Serializer.writeKeyValue(writer, "unselectedCloseVisible", getUnselectedCloseVisible());
                return;
            case "unselectedImageVisible":
                Serializer.writeKeyValue(writer, "unselectedImageVisible", getUnselectedImageVisible());
                return;
        }
        super.writeProperty(writer, key);
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

        public static CTabFolder read(JsonReader<?> reader) throws IOException {
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

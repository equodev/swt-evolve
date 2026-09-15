package org.eclipse.swt.custom;

import java.util.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.printing.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VStyledText extends VCanvas {

    protected VStyledText() {
    }

    protected VStyledText(DartStyledText impl) {
        super(impl);
    }

    @JsonAttribute(ignore = true)
    public int getAlignment() {
        return ((DartStyledText) impl).getAlignment();
    }

    public void setAlignment(int value) {
        ((DartStyledText) impl).alignment = value;
    }

    public boolean getAlwaysShowScrollBars() {
        return ((DartStyledText) impl).getAlwaysShowScrollBars();
    }

    public void setAlwaysShowScrollBars(boolean value) {
        ((DartStyledText) impl).alwaysShowScroll = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getBidiColoring() {
        return ((DartStyledText) impl).getBidiColoring();
    }

    public void setBidiColoring(boolean value) {
        ((DartStyledText) impl).bidiColoring = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getBlockSelection() {
        return ((DartStyledText) impl).getBlockSelection();
    }

    public void setBlockSelection(boolean value) {
        ((DartStyledText) impl).blockSelection = value;
    }

    @JsonAttribute(ignore = true)
    public Rectangle getBlockSelectionBounds() {
        return ((DartStyledText) impl).blockSelectionBounds;
    }

    public void setBlockSelectionBounds(Rectangle value) {
        ((DartStyledText) impl).blockSelectionBounds = value;
    }

    public int getBottomMargin() {
        return ((DartStyledText) impl).getBottomMargin();
    }

    public void setBottomMargin(int value) {
        ((DartStyledText) impl).bottomMargin = value;
    }

    public int getCaretOffset() {
        return ((DartStyledText) impl).getCaretOffset();
    }

    public void setCaretOffset(int value) {
        ((DartStyledText) impl).setCaretOffset(value);
    }

    @JsonAttribute(ignore = true)
    public int getColumnX() {
        return ((DartStyledText) impl).columnX;
    }

    public void setColumnX(int value) {
        ((DartStyledText) impl).columnX = value;
    }

    @JsonAttribute(ignore = true)
    public StyledTextContent getContent() {
        return ((DartStyledText) impl).content;
    }

    public void setContent(StyledTextContent value) {
        ((DartStyledText) impl).content = value;
    }

    public boolean getDoubleClickEnabled() {
        return ((DartStyledText) impl).getDoubleClickEnabled();
    }

    public void setDoubleClickEnabled(boolean value) {
        ((DartStyledText) impl).doubleClickEnabled = value;
    }

    public boolean getEditable() {
        return ((DartStyledText) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartStyledText) impl).editable = value;
    }

    @JsonAttribute(ignore = true)
    public FontMetrics getFixedLineMetrics() {
        FontMetrics val = ((DartStyledText) impl).fixedLineMetrics;
        if (val != null && !(val.getImpl() instanceof DartFontMetrics))
            return null;
        return val;
    }

    public void setFixedLineMetrics(FontMetrics value) {
        ((DartStyledText) impl).fixedLineMetrics = value;
    }

    @JsonAttribute(ignore = true)
    public int getHorizontalIndex() {
        return ((DartStyledText) impl).getHorizontalIndex();
    }

    public void setHorizontalIndex(int value) {
        ((DartStyledText) impl).horizontalIndex = value;
    }

    public int getHorizontalPixel() {
        return ((DartStyledText) impl).getHorizontalPixel();
    }

    public void setHorizontalPixel(int value) {
        ((DartStyledText) impl).horizontalScrollOffset = value;
    }

    @JsonAttribute(ignore = true)
    public int getIndent() {
        return ((DartStyledText) impl).getIndent();
    }

    public void setIndent(int value) {
        ((DartStyledText) impl).indent = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getJustify() {
        return ((DartStyledText) impl).getJustify();
    }

    public void setJustify(boolean value) {
        ((DartStyledText) impl).justify = value;
    }

    public int getLeftMargin() {
        return ((DartStyledText) impl).getLeftMargin();
    }

    public void setLeftMargin(int value) {
        ((DartStyledText) impl).leftMargin = value;
    }

    @JsonAttribute(ignore = true)
    public int getLineSpacing() {
        return ((DartStyledText) impl).getLineSpacing();
    }

    public void setLineSpacing(int value) {
        ((DartStyledText) impl).lineSpacing = value;
    }

    public Color getMarginColor() {
        return ((DartStyledText) impl).marginColor;
    }

    public void setMarginColor(Color value) {
        ((DartStyledText) impl).marginColor = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getMouseNavigatorEnabled() {
        return ((DartStyledText) impl).getMouseNavigatorEnabled();
    }

    public void setMouseNavigatorEnabled(boolean value) {
        ((DartStyledText) impl).mouseNavigatorEnabled = value;
    }

    @JsonAttribute(ignore = true)
    public int[] getRanges() {
        return ((DartStyledText) impl).ranges;
    }

    public void setRanges(int[] value) {
        ((DartStyledText) impl).ranges = value;
    }

    public int getRightMargin() {
        return ((DartStyledText) impl).getRightMargin();
    }

    public void setRightMargin(int value) {
        ((DartStyledText) impl).rightMargin = value;
    }

    @JsonAttribute(ignore = true)
    public Point getSelection() {
        return ((DartStyledText) impl).getSelection();
    }

    public void setSelection(Point value) {
        ((DartStyledText) impl).setSelection(value);
    }

    public Color getSelectionBackground() {
        return ((DartStyledText) impl).selectionBackground;
    }

    public void setSelectionBackground(Color value) {
        ((DartStyledText) impl).selectionBackground = value;
    }

    public Color getSelectionForeground() {
        return ((DartStyledText) impl).selectionForeground;
    }

    public void setSelectionForeground(Color value) {
        ((DartStyledText) impl).selectionForeground = value;
    }

    public Point getSelectionRange() {
        return ((DartStyledText) impl).selectionRange;
    }

    public void setSelectionRange(Point value) {
        ((DartStyledText) impl).selectionRange = value;
    }

    @JsonAttribute(ignore = true)
    public int[] getSelectionRanges() {
        return ((DartStyledText) impl).selectionRanges;
    }

    public void setSelectionRanges(int[] value) {
        ((DartStyledText) impl).selectionRanges = value;
    }

    @JsonAttribute(ignore = true)
    public int getStyleRange() {
        return ((DartStyledText) impl).columnX;
    }

    public void setStyleRange(int value) {
        ((DartStyledText) impl).columnX = value;
    }

    @JsonAttribute(ignore = true)
    public StyleRange[] getStyleRanges() {
        StyleRange[] values = ((DartStyledText) impl).getStyleRanges();
        if (values == null)
            return null;
        ArrayList<StyleRange> result = new ArrayList<>(values.length);
        for (StyleRange v : values) if (v != null)
            result.add(v);
        return result.toArray(StyleRange[]::new);
    }

    public void setStyleRanges(StyleRange[] value) {
        ((DartStyledText) impl).setStyleRanges(value);
    }

    @JsonAttribute(ignore = true)
    public int[] getTabStops() {
        return ((DartStyledText) impl).tabs;
    }

    public void setTabStops(int[] value) {
        ((DartStyledText) impl).tabs = value;
    }

    public int getTabs() {
        return ((DartStyledText) impl).getTabs();
    }

    public void setTabs(int value) {
        ((DartStyledText) impl).tabLength = value;
    }

    @JsonAttribute(nullable = false)
    public String getText() {
        return ((DartStyledText) impl).getText();
    }

    public void setText(String value) {
        ((DartStyledText) impl).text = value;
    }

    @JsonAttribute(ignore = true)
    public int getTextLimit() {
        return ((DartStyledText) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartStyledText) impl).textLimit = value;
    }

    @JsonAttribute(ignore = true)
    public int getTopIndex() {
        return ((DartStyledText) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartStyledText) impl).topIndex = value;
    }

    public int getTopMargin() {
        return ((DartStyledText) impl).getTopMargin();
    }

    public void setTopMargin(int value) {
        ((DartStyledText) impl).topMargin = value;
    }

    public int getTopPixel() {
        return ((DartStyledText) impl).getTopPixel();
    }

    public void setTopPixel(int value) {
        ((DartStyledText) impl).topPixel = value;
    }

    @JsonAttribute(ignore = true)
    public int getVerticalScrollOffset() {
        return ((DartStyledText) impl).verticalScrollOffset;
    }

    public void setVerticalScrollOffset(int value) {
        ((DartStyledText) impl).verticalScrollOffset = value;
    }

    public boolean getWordWrap() {
        return ((DartStyledText) impl).getWordWrap();
    }

    public void setWordWrap(boolean value) {
        ((DartStyledText) impl).wordWrap = value;
    }

    @JsonAttribute(ignore = true)
    public int getWrapIndent() {
        return ((DartStyledText) impl).getWrapIndent();
    }

    public void setWrapIndent(int value) {
        ((DartStyledText) impl).wrapIndent = value;
    }

    @JsonAttribute(nullable = true)
    public VStyledTextRenderer getRenderer() {
        DartStyledText dartImpl = (DartStyledText) impl;
        if (dartImpl.renderer == null)
            return null;
        if (!(dartImpl.renderer.getImpl() instanceof DartStyledTextRenderer))
            return null;
        return new VStyledTextRenderer((DartStyledTextRenderer) dartImpl.renderer.getImpl());
    }

    public void setRenderer(VStyledTextRenderer value) {
    }

    public static final String ALWAYS_SHOW_SCROLL_BARS = "alwaysShowScrollBars";

    public static final String BOTTOM_MARGIN = "bottomMargin";

    public static final String CARET_OFFSET = "caretOffset";

    public static final String DOUBLE_CLICK_ENABLED = "doubleClickEnabled";

    public static final String EDITABLE = "editable";

    public static final String HORIZONTAL_PIXEL = "horizontalPixel";

    public static final String LEFT_MARGIN = "leftMargin";

    public static final String MARGIN_COLOR = "marginColor";

    public static final String RENDERER = "renderer";

    public static final String RIGHT_MARGIN = "rightMargin";

    public static final String SELECTION_BACKGROUND = "selectionBackground";

    public static final String SELECTION_FOREGROUND = "selectionForeground";

    public static final String SELECTION_RANGE = "selectionRange";

    public static final String TABS = "tabs";

    public static final String TEXT = "text";

    public static final String TOP_MARGIN = "topMargin";

    public static final String TOP_PIXEL = "topPixel";

    public static final String WORD_WRAP = "wordWrap";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "alwaysShowScrollBars":
                Serializer.writeKeyValue(writer, "alwaysShowScrollBars", getAlwaysShowScrollBars());
                return;
            case "bottomMargin":
                Serializer.writeKeyValue(writer, "bottomMargin", getBottomMargin());
                return;
            case "caretOffset":
                Serializer.writeKeyValue(writer, "caretOffset", getCaretOffset());
                return;
            case "doubleClickEnabled":
                Serializer.writeKeyValue(writer, "doubleClickEnabled", getDoubleClickEnabled());
                return;
            case "editable":
                Serializer.writeKeyValue(writer, "editable", getEditable());
                return;
            case "horizontalPixel":
                Serializer.writeKeyValue(writer, "horizontalPixel", getHorizontalPixel());
                return;
            case "leftMargin":
                Serializer.writeKeyValue(writer, "leftMargin", getLeftMargin());
                return;
            case "marginColor":
                Serializer.writeKeyValue(writer, "marginColor", getMarginColor());
                return;
            case "rightMargin":
                Serializer.writeKeyValue(writer, "rightMargin", getRightMargin());
                return;
            case "selectionBackground":
                Serializer.writeKeyValue(writer, "selectionBackground", getSelectionBackground());
                return;
            case "selectionForeground":
                Serializer.writeKeyValue(writer, "selectionForeground", getSelectionForeground());
                return;
            case "selectionRange":
                Serializer.writeKeyValue(writer, "selectionRange", getSelectionRange());
                return;
            case "tabs":
                Serializer.writeKeyValue(writer, "tabs", getTabs());
                return;
            case "text":
                Serializer.writeKeyValue(writer, "text", getText());
                return;
            case "topMargin":
                Serializer.writeKeyValue(writer, "topMargin", getTopMargin());
                return;
            case "topPixel":
                Serializer.writeKeyValue(writer, "topPixel", getTopPixel());
                return;
            case "wordWrap":
                Serializer.writeKeyValue(writer, "wordWrap", getWordWrap());
                return;
            case "renderer":
                Serializer.writeKeyValue(writer, "renderer", getRenderer());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = StyledText.class)
    public static class StyledTextJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartStyledText.class, (JsonWriter.WriteObject<DartStyledText>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartStyledText.class, (JsonReader.ReadObject<DartStyledText>) reader -> {
                return null;
            });
        }

        public static StyledText read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, StyledText api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

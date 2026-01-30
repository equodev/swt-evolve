package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VStyledTextRenderer {

    public VStyledTextRenderer() {
    }

    public VStyledTextRenderer(DartStyledTextRenderer impl) {
        this.impl = impl;
    }

    protected DartStyledTextRenderer impl;

    public int getAscent() {
        return ((DartStyledTextRenderer) impl).ascent;
    }

    public void setAscent(int value) {
        ((DartStyledTextRenderer) impl).ascent = value;
    }

    public int getAverageCharWidth() {
        return ((DartStyledTextRenderer) impl).averageCharWidth;
    }

    public void setAverageCharWidth(int value) {
        ((DartStyledTextRenderer) impl).averageCharWidth = value;
    }

    public float getAverageLineHeight() {
        return ((DartStyledTextRenderer) impl).averageLineHeight;
    }

    public void setAverageLineHeight(float value) {
        ((DartStyledTextRenderer) impl).averageLineHeight = value;
    }

    public Font getBoldFont() {
        Font val = ((DartStyledTextRenderer) impl).boldFont;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setBoldFont(Font value) {
        ((DartStyledTextRenderer) impl).boldFont = value;
    }

    public Font getBoldItalicFont() {
        Font val = ((DartStyledTextRenderer) impl).boldItalicFont;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setBoldItalicFont(Font value) {
        ((DartStyledTextRenderer) impl).boldItalicFont = value;
    }

    @JsonAttribute(ignore = true)
    public Bullet[] getBullets() {
        Bullet[] values = ((DartStyledTextRenderer) impl).bullets;
        if (values == null)
            return null;
        ArrayList<Bullet> result = new ArrayList<>(values.length);
        for (Bullet v : values) if (v != null)
            result.add(v);
        return result.toArray(Bullet[]::new);
    }

    public void setBullets(Bullet[] value) {
        ((DartStyledTextRenderer) impl).bullets = value;
    }

    public int[] getBulletsIndices() {
        return ((DartStyledTextRenderer) impl).bulletsIndices;
    }

    public void setBulletsIndices(int[] value) {
        ((DartStyledTextRenderer) impl).bulletsIndices = value;
    }

    @JsonAttribute(ignore = true)
    public StyledTextContent getContent() {
        return ((DartStyledTextRenderer) impl).content;
    }

    public void setContent(StyledTextContent value) {
        ((DartStyledTextRenderer) impl).content = value;
    }

    public int getDescent() {
        return ((DartStyledTextRenderer) impl).descent;
    }

    public void setDescent(int value) {
        ((DartStyledTextRenderer) impl).descent = value;
    }

    public boolean getFixedPitch() {
        return ((DartStyledTextRenderer) impl).fixedPitch;
    }

    public void setFixedPitch(boolean value) {
        ((DartStyledTextRenderer) impl).fixedPitch = value;
    }

    public boolean getHasLinks() {
        return ((DartStyledTextRenderer) impl).hasLinks;
    }

    public void setHasLinks(boolean value) {
        ((DartStyledTextRenderer) impl).hasLinks = value;
    }

    public boolean getIdleRunning() {
        return ((DartStyledTextRenderer) impl).idleRunning;
    }

    public void setIdleRunning(boolean value) {
        ((DartStyledTextRenderer) impl).idleRunning = value;
    }

    public Font getItalicFont() {
        Font val = ((DartStyledTextRenderer) impl).italicFont;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setItalicFont(Font value) {
        ((DartStyledTextRenderer) impl).italicFont = value;
    }

    @JsonAttribute(ignore = true)
    public TextLayout[] getLayouts() {
        TextLayout[] values = ((DartStyledTextRenderer) impl).layouts;
        if (values == null)
            return null;
        ArrayList<TextLayout> result = new ArrayList<>(values.length);
        for (TextLayout v : values) if (v != null)
            result.add(v);
        return result.toArray(TextLayout[]::new);
    }

    public void setLayouts(TextLayout[] value) {
        ((DartStyledTextRenderer) impl).layouts = value;
    }

    public int getLineCount() {
        return ((DartStyledTextRenderer) impl).lineCount;
    }

    public void setLineCount(int value) {
        ((DartStyledTextRenderer) impl).lineCount = value;
    }

    public VStyledTextRenderer.VLineSizeInfo[] getLineSizes() {
        DartStyledTextRenderer.LineSizeInfo[] values = ((DartStyledTextRenderer) impl).lineSizes;
        if (values == null)
            return null;
        VStyledTextRenderer.VLineSizeInfo[] result = new VStyledTextRenderer.VLineSizeInfo[values.length];
        for (int i = 0; i < values.length; i++) if (values[i] != null)
            result[i] = new VStyledTextRenderer.VLineSizeInfo(values[i]);
        return result;
    }

    public void setLineSizes(VStyledTextRenderer.VLineSizeInfo[] value) {
        if (value == null) {
            ((DartStyledTextRenderer) impl).lineSizes = null;
            return;
        }
        DartStyledTextRenderer.LineSizeInfo[] result = new DartStyledTextRenderer.LineSizeInfo[value.length];
        for (int i = 0; i < value.length; i++) {
            if (value[i] != null) {
                result[i] = new DartStyledTextRenderer.LineSizeInfo();
                result[i].height = value[i].getHeight();
                result[i].width = value[i].getWidth();
            }
        }
        ((DartStyledTextRenderer) impl).lineSizes = result;
    }

    public boolean getLineSpacingComputing() {
        return ((DartStyledTextRenderer) impl).lineSpacingComputing;
    }

    public void setLineSpacingComputing(boolean value) {
        ((DartStyledTextRenderer) impl).lineSpacingComputing = value;
    }

    public VStyledTextRenderer.VLineInfo[] getLines() {
        DartStyledTextRenderer.LineInfo[] values = ((DartStyledTextRenderer) impl).lines;
        if (values == null)
            return null;
        VStyledTextRenderer.VLineInfo[] result = new VStyledTextRenderer.VLineInfo[values.length];
        for (int i = 0; i < values.length; i++) if (values[i] != null)
            result[i] = new VStyledTextRenderer.VLineInfo(values[i]);
        return result;
    }

    public void setLines(VStyledTextRenderer.VLineInfo[] value) {
        if (value == null) {
            ((DartStyledTextRenderer) impl).lines = null;
            return;
        }
        DartStyledTextRenderer.LineInfo[] result = new DartStyledTextRenderer.LineInfo[value.length];
        for (int i = 0; i < value.length; i++) {
            if (value[i] != null) {
                result[i] = new DartStyledTextRenderer.LineInfo();
                result[i].flags = value[i].getFlags();
                result[i].background = value[i].getBackground();
                result[i].alignment = value[i].getAlignment();
                result[i].indent = value[i].getIndent();
                result[i].wrapIndent = value[i].getWrapIndent();
                result[i].justify = value[i].getJustify();
                result[i].segments = value[i].getSegments();
                result[i].segmentsChars = value[i].getSegmentsChars();
                result[i].tabStops = value[i].getTabStops();
                result[i].verticalIndent = value[i].getVerticalIndent();
            }
        }
        ((DartStyledTextRenderer) impl).lines = result;
    }

    public int getLinesInAverageLineHeight() {
        return ((DartStyledTextRenderer) impl).linesInAverageLineHeight;
    }

    public void setLinesInAverageLineHeight(int value) {
        ((DartStyledTextRenderer) impl).linesInAverageLineHeight = value;
    }

    public int getMaxWidth() {
        return ((DartStyledTextRenderer) impl).maxWidth;
    }

    public void setMaxWidth(int value) {
        ((DartStyledTextRenderer) impl).maxWidth = value;
    }

    public int getMaxWidthLineIndex() {
        return ((DartStyledTextRenderer) impl).maxWidthLineIndex;
    }

    public void setMaxWidthLineIndex(int value) {
        ((DartStyledTextRenderer) impl).maxWidthLineIndex = value;
    }

    public int[] getRanges() {
        return ((DartStyledTextRenderer) impl).ranges;
    }

    public void setRanges(int[] value) {
        ((DartStyledTextRenderer) impl).ranges = value;
    }

    public int[] getRedrawLines() {
        return ((DartStyledTextRenderer) impl).redrawLines;
    }

    public void setRedrawLines(int[] value) {
        ((DartStyledTextRenderer) impl).redrawLines = value;
    }

    public Font getRegularFont() {
        Font val = ((DartStyledTextRenderer) impl).regularFont;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setRegularFont(Font value) {
        ((DartStyledTextRenderer) impl).regularFont = value;
    }

    public int getStyleCount() {
        return ((DartStyledTextRenderer) impl).styleCount;
    }

    public void setStyleCount(int value) {
        ((DartStyledTextRenderer) impl).styleCount = value;
    }

    @JsonAttribute(ignore = true)
    public StyledText getStyledText() {
        StyledText val = ((DartStyledTextRenderer) impl).styledText;
        if (val != null && !(val.getImpl() instanceof DartStyledText))
            return null;
        return val;
    }

    public void setStyledText(StyledText value) {
        ((DartStyledTextRenderer) impl).styledText = value;
    }

    public StyleRange[] getStyles() {
        StyleRange[] values = ((DartStyledTextRenderer) impl).styles;
        if (values == null)
            return null;
        ArrayList<StyleRange> result = new ArrayList<>(values.length);
        for (StyleRange v : values) if (v != null)
            result.add(v);
        return result.toArray(StyleRange[]::new);
    }

    public void setStyles(StyleRange[] value) {
        ((DartStyledTextRenderer) impl).styles = value;
    }

    public StyleRange[] getStylesSet() {
        StyleRange[] values = ((DartStyledTextRenderer) impl).stylesSet;
        if (values == null)
            return null;
        ArrayList<StyleRange> result = new ArrayList<>(values.length);
        for (StyleRange v : values) if (v != null)
            result.add(v);
        return result.toArray(StyleRange[]::new);
    }

    public void setStylesSet(StyleRange[] value) {
        ((DartStyledTextRenderer) impl).stylesSet = value;
    }

    public int getStylesSetCount() {
        return ((DartStyledTextRenderer) impl).stylesSetCount;
    }

    public void setStylesSetCount(int value) {
        ((DartStyledTextRenderer) impl).stylesSetCount = value;
    }

    public int getTabLength() {
        return ((DartStyledTextRenderer) impl).tabLength;
    }

    public void setTabLength(int value) {
        ((DartStyledTextRenderer) impl).tabLength = value;
    }

    public int getTabWidth() {
        return ((DartStyledTextRenderer) impl).tabWidth;
    }

    public void setTabWidth(int value) {
        ((DartStyledTextRenderer) impl).tabWidth = value;
    }

    public int getTopIndex() {
        return ((DartStyledTextRenderer) impl).topIndex;
    }

    public void setTopIndex(int value) {
        ((DartStyledTextRenderer) impl).topIndex = value;
    }

    @CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
    public static class VLineSizeInfo {

        protected VLineSizeInfo() {
        }

        public VLineSizeInfo(DartStyledTextRenderer.LineSizeInfo impl) {
            this.height = impl.height;
            this.width = impl.width;
        }

        private int height;

        public int getHeight() {
            return height;
        }

        public void setHeight(int value) {
            this.height = value;
        }

        private int width;

        public int getWidth() {
            return width;
        }

        public void setWidth(int value) {
            this.width = value;
        }
    }

    @CompiledJson(objectFormatPolicy = CompiledJson.ObjectFormatPolicy.FULL)
    public static class VLineInfo {

        protected VLineInfo() {
        }

        public VLineInfo(DartStyledTextRenderer.LineInfo impl) {
            this.flags = impl.flags;
            this.background = impl.background;
            this.alignment = impl.alignment;
            this.indent = impl.indent;
            this.wrapIndent = impl.wrapIndent;
            this.justify = impl.justify;
            this.segments = impl.segments;
            this.segmentsChars = impl.segmentsChars;
            this.tabStops = impl.tabStops;
            this.verticalIndent = impl.verticalIndent;
        }

        private int flags;

        public int getFlags() {
            return flags;
        }

        public void setFlags(int value) {
            this.flags = value;
        }

        private Color background;

        public Color getBackground() {
            return background;
        }

        public void setBackground(Color value) {
            this.background = value;
        }

        private int alignment;

        public int getAlignment() {
            return alignment;
        }

        public void setAlignment(int value) {
            this.alignment = value;
        }

        private int indent;

        public int getIndent() {
            return indent;
        }

        public void setIndent(int value) {
            this.indent = value;
        }

        private int wrapIndent;

        public int getWrapIndent() {
            return wrapIndent;
        }

        public void setWrapIndent(int value) {
            this.wrapIndent = value;
        }

        private boolean justify;

        public boolean getJustify() {
            return justify;
        }

        public void setJustify(boolean value) {
            this.justify = value;
        }

        private int[] segments;

        public int[] getSegments() {
            return segments;
        }

        public void setSegments(int[] value) {
            this.segments = value;
        }

        private char[] segmentsChars;

        public char[] getSegmentsChars() {
            return segmentsChars;
        }

        public void setSegmentsChars(char[] value) {
            this.segmentsChars = value;
        }

        private int[] tabStops;

        public int[] getTabStops() {
            return tabStops;
        }

        public void setTabStops(int[] value) {
            this.tabStops = value;
        }

        private int verticalIndent;

        public int getVerticalIndent() {
            return verticalIndent;
        }

        public void setVerticalIndent(int value) {
            this.verticalIndent = value;
        }
    }

    public static class StyledTextRendererJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartStyledTextRenderer.class, (JsonWriter.WriteObject<DartStyledTextRenderer>) (writer, impl) -> {
                if (impl == null)
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartStyledTextRenderer.class, (JsonReader.ReadObject<DartStyledTextRenderer>) reader -> {
                return null;
            });
        }

        public static StyledTextRenderer read(JsonReader<?> reader) {
            return null;
        }

        public static void write(JsonWriter writer, StyledTextRenderer api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}

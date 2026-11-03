package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class VStyledTextRenderer implements dev.equo.swt.WireObject {

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

    public Bullet[] getBullets() {
        Bullet[] values = ((DartStyledTextRenderer) impl).bullets;
        if (values == null)
            return null;
        int kept = 0;
        for (Bullet v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        Bullet[] result = new Bullet[kept];
        kept = 0;
        for (Bullet v : values) if (v != null)
            result[kept++] = v;
        return result;
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

    public TextLayout[] getLayouts() {
        TextLayout[] values = ((DartStyledTextRenderer) impl).layouts;
        if (values == null)
            return null;
        int kept = 0;
        for (TextLayout v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TextLayout[] result = new TextLayout[kept];
        kept = 0;
        for (TextLayout v : values) if (v != null)
            result[kept++] = v;
        return result;
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
        int kept = 0;
        for (StyleRange v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        StyleRange[] result = new StyleRange[kept];
        kept = 0;
        for (StyleRange v : values) if (v != null)
            result[kept++] = v;
        return result;
    }

    public void setStyles(StyleRange[] value) {
        ((DartStyledTextRenderer) impl).styles = value;
    }

    public StyleRange[] getStylesSet() {
        StyleRange[] values = ((DartStyledTextRenderer) impl).stylesSet;
        if (values == null)
            return null;
        int kept = 0;
        for (StyleRange v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        StyleRange[] result = new StyleRange[kept];
        kept = 0;
        for (StyleRange v : values) if (v != null)
            result[kept++] = v;
        return result;
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

    public int[] getRanges() {
        return ((DartStyledTextRenderer) impl).wireRanges();
    }

    public int getStyleCount() {
        return ((DartStyledTextRenderer) impl).wireStyleCount();
    }

    public VStyledTextRenderer.VLineInfo[] getLines() {
        return ((DartStyledTextRenderer) impl).wireLines();
    }

    public int[] getLineSpacings() {
        return ((DartStyledTextRenderer) impl).wireLineSpacings();
    }

    public String[] getBulletTexts() {
        return ((DartStyledTextRenderer) impl).wireBulletTexts();
    }

    public static class VLineSizeInfo implements dev.equo.swt.WireObject {

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

        @Override
        public int wireTypeId() {
            return 62;
        }

        @Override
        public void writeWireFields(dev.equo.swt.WidgetWire w) {
            w.key(359);
            w.write((int) getHeight());
            w.key(360);
            w.write((int) getWidth());
        }
    }

    public static class VLineInfo implements dev.equo.swt.WireObject {

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

        @Override
        public int wireTypeId() {
            return 61;
        }

        @Override
        public void writeWireFields(dev.equo.swt.WidgetWire w) {
            w.key(351);
            w.write((int) getFlags());
            w.key(350);
            w.write(getBackground());
            w.key(349);
            w.write((int) getAlignment());
            w.key(352);
            w.write((int) getIndent());
            w.key(358);
            w.write((int) getWrapIndent());
            w.key(353);
            w.write(getJustify());
            w.key(354);
            w.write(getSegments());
            w.key(355);
            w.write(getSegmentsChars());
            w.key(356);
            w.write(getTabStops());
            w.key(357);
            w.write((int) getVerticalIndent());
        }
    }

    @Override
    public int wireTypeId() {
        return 101;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(513);
        w.write((int) getAscent());
        w.key(520);
        w.write((int) getDescent());
        w.key(535);
        w.object((getRegularFont() == null || getRegularFont().isDisposed() ? null : getRegularFont().getImpl() instanceof DartFont ? ((DartFont) getRegularFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getRegularFont()).getImpl()).getValue()));
        w.key(533);
        w.write(getRanges());
        w.key(536);
        w.write((int) getStyleCount());
        w.key(529);
        {
            VStyledTextRenderer.VLineInfo[] a_ = getLines();
            if (a_ == null)
                w.listSize(-1);
            else {
                w.listSize(a_.length);
                for (VStyledTextRenderer.VLineInfo e_ : a_) w.object(e_);
            }
        }
        w.key(528);
        w.write(getLineSpacings());
        w.key(518);
        w.write(getBulletTexts());
    }
}

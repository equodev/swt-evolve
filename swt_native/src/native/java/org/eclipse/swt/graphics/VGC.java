package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;

public class VGC extends VResource {

    protected VGC() {
    }

    protected VGC(DartGC impl) {
        super(impl);
    }

    public boolean getXORMode() {
        return ((DartGC) impl).getXORMode();
    }

    public void setXORMode(boolean value) {
        ((DartGC) impl).XORMode = value;
    }

    public boolean getAdvanced() {
        return ((DartGC) impl).getAdvanced();
    }

    public void setAdvanced(boolean value) {
        ((DartGC) impl).advanced = value;
    }

    public Integer getAlpha() {
        int value = ((DartGC) impl).getAlpha();
        return value == 255 ? null : value;
    }

    public void setAlpha(Integer value) {
        ((DartGC) impl).alpha = value == null ? 255 : value;
    }

    public int getAntialias() {
        return ((DartGC) impl).getAntialias();
    }

    public void setAntialias(int value) {
        ((DartGC) impl).antialias = value;
    }

    public Color getBackground() {
        return ((DartGC) impl).background;
    }

    public void setBackground(Color value) {
        ((DartGC) impl).background = value;
    }

    public Pattern getBackgroundPattern() {
        Pattern val = ((DartGC) impl).backgroundPattern;
        if (val != null && !(val.getImpl() instanceof DartPattern))
            return null;
        return val;
    }

    public void setBackgroundPattern(Pattern value) {
        ((DartGC) impl).backgroundPattern = value;
    }

    public Rectangle getClipping() {
        return ((DartGC) impl).clipping;
    }

    public void setClipping(Rectangle value) {
        ((DartGC) impl).clipping = value;
    }

    public PathData getClippingPath() {
        return ((DartGC) impl).clippingPath;
    }

    public void setClippingPath(PathData value) {
        ((DartGC) impl).clippingPath = value;
    }

    public int[] getClippingRects() {
        return ((DartGC) impl).clippingRects;
    }

    public void setClippingRects(int[] value) {
        ((DartGC) impl).clippingRects = value;
    }

    public Integer getFillRule() {
        int value = ((DartGC) impl).getFillRule();
        return value == 1 ? null : value;
    }

    public void setFillRule(Integer value) {
        ((DartGC) impl).fillRule = value == null ? 1 : value;
    }

    public Font getFont() {
        Font val = ((DartGC) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartGC) impl).font = value;
    }

    public Color getForeground() {
        return ((DartGC) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartGC) impl).foreground = value;
    }

    public Pattern getForegroundPattern() {
        Pattern val = ((DartGC) impl).foregroundPattern;
        if (val != null && !(val.getImpl() instanceof DartPattern))
            return null;
        return val;
    }

    public void setForegroundPattern(Pattern value) {
        ((DartGC) impl).foregroundPattern = value;
    }

    public int getInterpolation() {
        return ((DartGC) impl).getInterpolation();
    }

    public void setInterpolation(int value) {
        ((DartGC) impl).interpolation = value;
    }

    public LineAttributes getLineAttributes() {
        return ((DartGC) impl).lineAttributes;
    }

    public void setLineAttributes(LineAttributes value) {
        ((DartGC) impl).lineAttributes = value;
    }

    public Integer getLineCap() {
        int value = ((DartGC) impl).getLineCap();
        return value == 1 ? null : value;
    }

    public void setLineCap(Integer value) {
        ((DartGC) impl).lineCap = value == null ? 1 : value;
    }

    public int[] getLineDash() {
        return ((DartGC) impl).lineDash;
    }

    public void setLineDash(int[] value) {
        ((DartGC) impl).lineDash = value;
    }

    public Integer getLineJoin() {
        int value = ((DartGC) impl).getLineJoin();
        return value == 1 ? null : value;
    }

    public void setLineJoin(Integer value) {
        ((DartGC) impl).lineJoin = value == null ? 1 : value;
    }

    public Integer getLineStyle() {
        int value = ((DartGC) impl).getLineStyle();
        return value == 1 ? null : value;
    }

    public void setLineStyle(Integer value) {
        ((DartGC) impl).lineStyle = value == null ? 1 : value;
    }

    public int getLineWidth() {
        return ((DartGC) impl).getLineWidth();
    }

    public void setLineWidth(int value) {
        ((DartGC) impl).lineWidth = value;
    }

    public int getStyle() {
        return ((DartGC) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartGC) impl).style = value;
    }

    public int getTextAntialias() {
        return ((DartGC) impl).getTextAntialias();
    }

    public void setTextAntialias(int value) {
        ((DartGC) impl).textAntialias = value;
    }

    public Transform getTransform() {
        Transform val = ((DartGC) impl).transform;
        if (val != null && !(val.getImpl() instanceof DartTransform))
            return null;
        return val;
    }

    public void setTransform(Transform value) {
        ((DartGC) impl).transform = value;
    }

    public static class VGCCopyAreaImageintint implements dev.equo.swt.GcWire.Op {

        public Image image;

        public int x;

        public int y;

        @Override
        public int wireNumber() {
            return 865;
        }

        @Override
        public String signature() {
            return "wii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.image(image);
                w.w(ww);
            }
            w.i(x);
            w.i(y);
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "image" };
        }
    }

    public static class VGCCopyAreaintintintintintintboolean implements dev.equo.swt.GcWire.Op {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;

        public boolean paint;

        @Override
        public int wireNumber() {
            return 866;
        }

        @Override
        public String signature() {
            return "iiiiiib";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(srcX);
            w.i(srcY);
            w.i(width);
            w.i(height);
            w.i(destX);
            w.i(destY);
            w.b(paint);
        }
    }

    public static class VGCDrawArcintintintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;

        @Override
        public int wireNumber() {
            return 867;
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
            w.i(startAngle);
            w.i(arcAngle);
        }
    }

    public static class VGCDrawFocusintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        @Override
        public int wireNumber() {
            return 868;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCDrawImageImageintint implements dev.equo.swt.GcWire.Op {

        public Image image;

        public int x;

        public int y;

        @Override
        public int wireNumber() {
            return 869;
        }

        @Override
        public String signature() {
            return "wii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.image(image);
                w.w(ww);
            }
            w.i(x);
            w.i(y);
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "image" };
        }
    }

    public static class VGCDrawImageImageintintintint implements dev.equo.swt.GcWire.Op {

        public Image image;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;

        @Override
        public int wireNumber() {
            return 870;
        }

        @Override
        public String signature() {
            return "wiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.image(image);
                w.w(ww);
            }
            w.i(destX);
            w.i(destY);
            w.i(destWidth);
            w.i(destHeight);
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "image" };
        }
    }

    public static class VGCDrawImageImageintintintintintintintint implements dev.equo.swt.GcWire.Op {

        public Image image;

        public int srcX;

        public int srcY;

        public int srcWidth;

        public int srcHeight;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;

        @Override
        public int wireNumber() {
            return 871;
        }

        @Override
        public String signature() {
            return "wiiiiiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.image(image);
                w.w(ww);
            }
            w.i(srcX);
            w.i(srcY);
            w.i(srcWidth);
            w.i(srcHeight);
            w.i(destX);
            w.i(destY);
            w.i(destWidth);
            w.i(destHeight);
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "image" };
        }
    }

    public static class VGCDrawLineintintintint implements dev.equo.swt.GcWire.Op {

        public int x1;

        public int y1;

        public int x2;

        public int y2;

        @Override
        public int wireNumber() {
            return 872;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x1);
            w.i(y1);
            w.i(x2);
            w.i(y2);
        }
    }

    public static class VGCDrawOvalintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        @Override
        public int wireNumber() {
            return 873;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCDrawPathPath implements dev.equo.swt.GcWire.Op {

        public Path path;

        @Override
        public int wireNumber() {
            return 874;
        }

        @Override
        public String signature() {
            return "w";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.object((path == null || path.isDisposed() ? null : ((DartPath) path.getImpl()).getValue()));
                w.w(ww);
            }
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "object:VPath" };
        }
    }

    public static class VGCDrawPointintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        @Override
        public int wireNumber() {
            return 875;
        }

        @Override
        public String signature() {
            return "ii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
        }
    }

    public static class VGCDrawPolygonint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        @Override
        public int wireNumber() {
            return 876;
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.ints(pointArray);
        }
    }

    public static class VGCDrawPolylineint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        @Override
        public int wireNumber() {
            return 877;
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.ints(pointArray);
        }
    }

    public static class VGCDrawRectangleintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        @Override
        public int wireNumber() {
            return 878;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCDrawRoundRectangleintintintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;

        @Override
        public int wireNumber() {
            return 879;
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
            w.i(arcWidth);
            w.i(arcHeight);
        }
    }

    public static class VGCDrawTextStringintintint implements dev.equo.swt.GcWire.Op {

        public String string;

        public int x;

        public int y;

        public int flags;

        @Override
        public int wireNumber() {
            return 880;
        }

        @Override
        public String signature() {
            return "siii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.s(string);
            w.i(x);
            w.i(y);
            w.i(flags);
        }
    }

    public static class VGCFillArcintintintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;

        @Override
        public int wireNumber() {
            return 881;
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
            w.i(startAngle);
            w.i(arcAngle);
        }
    }

    public static class VGCFillGradientRectangleintintintintboolean implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        public boolean vertical;

        @Override
        public int wireNumber() {
            return 882;
        }

        @Override
        public String signature() {
            return "iiiib";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
            w.b(vertical);
        }
    }

    public static class VGCFillOvalintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        @Override
        public int wireNumber() {
            return 883;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCFillPathPath implements dev.equo.swt.GcWire.Op {

        public Path path;

        @Override
        public int wireNumber() {
            return 884;
        }

        @Override
        public String signature() {
            return "w";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            {
                dev.equo.swt.WidgetWire ww = w.scratch();
                ww.object((path == null || path.isDisposed() ? null : ((DartPath) path.getImpl()).getValue()));
                w.w(ww);
            }
        }

        @Override
        public String[] wireKinds() {
            return new String[] { "object:VPath" };
        }
    }

    public static class VGCFillPolygonint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        @Override
        public int wireNumber() {
            return 885;
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.ints(pointArray);
        }
    }

    public static class VGCFillRectangleintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        @Override
        public int wireNumber() {
            return 886;
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCFillRoundRectangleintintintintintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;

        @Override
        public int wireNumber() {
            return 887;
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
            w.i(arcWidth);
            w.i(arcHeight);
        }
    }

    public Path getClippingText() {
        return ((DartGC) impl).wireClippingText();
    }

    public float getLineDashOffset() {
        return ((DartGC) impl).wireLineDashOffset();
    }

    public float getBufferScale() {
        return ((DartGC) impl).wireBufferScale();
    }

    public Boolean getThemedCanvas() {
        return ((DartGC) impl).wireThemedCanvas();
    }

    public RGB[] getHoverFills() {
        return ((DartGC) impl).wireHoverFills();
    }

    public RGB[] getSelectionFills() {
        return ((DartGC) impl).wireSelectionFills();
    }

    public static final int XORMODE = 145;

    public static final int ALPHA = 146;

    public static final int ANTIALIAS = 147;

    public static final int BACKGROUND = 148;

    public static final int BACKGROUND_PATTERN = 560;

    public static final int BUFFER_SCALE = 149;

    public static final int CLIPPING = 150;

    public static final int CLIPPING_PATH = 151;

    public static final int CLIPPING_RECTS = 152;

    public static final int CLIPPING_TEXT = 44;

    public static final int FILL_RULE = 153;

    public static final int FONT = 154;

    public static final int FOREGROUND = 155;

    public static final int FOREGROUND_PATTERN = 561;

    public static final int HOVER_FILLS = 1372;

    public static final int INTERPOLATION = 156;

    public static final int LINE_CAP = 157;

    public static final int LINE_DASH = 158;

    public static final int LINE_DASH_OFFSET = 159;

    public static final int LINE_JOIN = 160;

    public static final int LINE_STYLE = 161;

    public static final int LINE_WIDTH = 162;

    public static final int SELECTION_FILLS = 1373;

    public static final int STYLE = 163;

    public static final int THEMED_CANVAS = 1356;

    public static final int TRANSFORM = 562;

    @Override
    public String wireName(int key) {
        switch(key) {
            case XORMODE:
                return "XORMode";
            case ALPHA:
                return "alpha";
            case ANTIALIAS:
                return "antialias";
            case BACKGROUND:
                return "background";
            case BACKGROUND_PATTERN:
                return "backgroundPattern";
            case CLIPPING:
                return "clipping";
            case CLIPPING_PATH:
                return "clippingPath";
            case CLIPPING_RECTS:
                return "clippingRects";
            case FILL_RULE:
                return "fillRule";
            case FONT:
                return "font";
            case FOREGROUND:
                return "foreground";
            case FOREGROUND_PATTERN:
                return "foregroundPattern";
            case INTERPOLATION:
                return "interpolation";
            case LINE_CAP:
                return "lineCap";
            case LINE_DASH:
                return "lineDash";
            case LINE_JOIN:
                return "lineJoin";
            case LINE_STYLE:
                return "lineStyle";
            case LINE_WIDTH:
                return "lineWidth";
            case STYLE:
                return "style";
            case TRANSFORM:
                return "transform";
            case CLIPPING_TEXT:
                return "clippingText";
            case LINE_DASH_OFFSET:
                return "lineDashOffset";
            case BUFFER_SCALE:
                return "bufferScale";
            case THEMED_CANVAS:
                return "themedCanvas";
            case HOVER_FILLS:
                return "hoverFills";
            case SELECTION_FILLS:
                return "selectionFills";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 145:
                w.write(getXORMode());
                return true;
            case 146:
                w.write(getAlpha());
                return true;
            case 147:
                w.write((int) getAntialias());
                return true;
            case 148:
                w.write(getBackground());
                return true;
            case 560:
                w.object((getBackgroundPattern() == null || getBackgroundPattern().isDisposed() ? null : ((DartPattern) getBackgroundPattern().getImpl()).getValue()));
                return true;
            case 150:
                w.write(getClipping());
                return true;
            case 151:
                w.object(getClippingPath());
                return true;
            case 152:
                w.write(getClippingRects());
                return true;
            case 153:
                w.write(getFillRule());
                return true;
            case 154:
                w.object((getFont() == null || getFont().isDisposed() ? null : ((DartFont) getFont().getImpl()).getValue()));
                return true;
            case 155:
                w.write(getForeground());
                return true;
            case 561:
                w.object((getForegroundPattern() == null || getForegroundPattern().isDisposed() ? null : ((DartPattern) getForegroundPattern().getImpl()).getValue()));
                return true;
            case 156:
                w.write((int) getInterpolation());
                return true;
            case 157:
                w.write(getLineCap());
                return true;
            case 158:
                w.write(getLineDash());
                return true;
            case 160:
                w.write(getLineJoin());
                return true;
            case 161:
                w.write(getLineStyle());
                return true;
            case 162:
                w.write((int) getLineWidth());
                return true;
            case 163:
                w.write((int) getStyle());
                return true;
            case 562:
                w.object((getTransform() == null || getTransform().isDisposed() ? null : ((DartTransform) getTransform().getImpl()).getValue()));
                return true;
            case 44:
                w.object((getClippingText() == null || getClippingText().isDisposed() ? null : ((DartPath) getClippingText().getImpl()).getValue()));
                return true;
            case 159:
                w.write(getLineDashOffset());
                return true;
            case 149:
                w.write(getBufferScale());
                return true;
            case 1356:
                w.write(getThemedCanvas());
                return true;
            case 1372:
                {
                    RGB[] a_ = getHoverFills();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (RGB e_ : a_) w.write(e_);
                    }
                }
                return true;
            case 1373:
                {
                    RGB[] a_ = getSelectionFills();
                    if (a_ == null)
                        w.listSize(-1);
                    else {
                        w.listSize(a_.length);
                        for (RGB e_ : a_) w.write(e_);
                    }
                }
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 145:
                {
                    boolean v_ = getXORMode();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(145);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 146:
                {
                    Integer v_ = getAlpha();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(146);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 147:
                {
                    int v_ = getAntialias();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(147);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 148:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(148);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 560:
                {
                    Pattern v_ = getBackgroundPattern();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartPattern) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(560);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 150:
                {
                    Rectangle v_ = getClipping();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(150);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 151:
                {
                    PathData v_ = getClippingPath();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(151);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 152:
                {
                    int[] v_ = getClippingRects();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(152);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 153:
                {
                    Integer v_ = getFillRule();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(153);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 154:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartFont) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(154);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 155:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(155);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 561:
                {
                    Pattern v_ = getForegroundPattern();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartPattern) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(561);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 156:
                {
                    int v_ = getInterpolation();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(156);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 157:
                {
                    Integer v_ = getLineCap();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(157);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 158:
                {
                    int[] v_ = getLineDash();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(158);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 160:
                {
                    Integer v_ = getLineJoin();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(160);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 161:
                {
                    Integer v_ = getLineStyle();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(161);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 162:
                {
                    int v_ = getLineWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(162);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 163:
                {
                    int v_ = getStyle();
                    w.key(163);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 562:
                {
                    Transform v_ = getTransform();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartTransform) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(562);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 44:
                {
                    Path v_ = getClippingText();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : ((DartPath) v_.getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(44);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 159:
                {
                    float v_ = getLineDashOffset();
                    w.key(159);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 149:
                {
                    float v_ = getBufferScale();
                    w.key(149);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1356:
                {
                    Boolean v_ = getThemedCanvas();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1356);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1372:
                {
                    RGB[] v_ = getHoverFills();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1372);
                    {
                        RGB[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (RGB e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1373:
                {
                    RGB[] v_ = getSelectionFills();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1373);
                    {
                        RGB[] a_ = v_;
                        if (a_ == null)
                            w.listSize(-1);
                        else {
                            w.listSize(a_.length);
                            for (RGB e_ : a_) w.write(e_);
                        }
                    }
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 145, 146, 147, 148, 560, 150, 151, 152, 153, 154, 155, 561, 156, 157, 158, 160, 161, 162, 163, 562, 44, 159, 149, 1356, 1372, 1373 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        switch(key) {
            case 163:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}

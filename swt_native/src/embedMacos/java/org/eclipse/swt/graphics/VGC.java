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
        return ((DartGC) impl).backgroundPattern;
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
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
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
        return ((DartGC) impl).foregroundPattern;
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
        return ((DartGC) impl).transform;
    }

    public void setTransform(Transform value) {
        ((DartGC) impl).transform = value;
    }

    public static class VGCCopyAreaImageintint implements dev.equo.swt.GcWire.Op {

        public Image image;

        public int x;

        public int y;

        private static final ThreadLocal<VGCCopyAreaImageintint> REUSED = ThreadLocal.withInitial(VGCCopyAreaImageintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCCopyAreaImageintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "wii";
        }

        @Override
        public int number() {
            return 148;
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

        private static final ThreadLocal<VGCCopyAreaintintintintintintboolean> REUSED = ThreadLocal.withInitial(VGCCopyAreaintintintintintintboolean::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCCopyAreaintintintintintintboolean reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiiiib";
        }

        @Override
        public int number() {
            return 150;
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

        private static final ThreadLocal<VGCDrawArcintintintintintint> REUSED = ThreadLocal.withInitial(VGCDrawArcintintintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawArcintintintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public int number() {
            return 151;
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

        private static final ThreadLocal<VGCDrawFocusintintintint> REUSED = ThreadLocal.withInitial(VGCDrawFocusintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawFocusintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 152;
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

        private static final ThreadLocal<VGCDrawImageImageintint> REUSED = ThreadLocal.withInitial(VGCDrawImageImageintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawImageImageintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "wii";
        }

        @Override
        public int number() {
            return 153;
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

        private static final ThreadLocal<VGCDrawImageImageintintintint> REUSED = ThreadLocal.withInitial(VGCDrawImageImageintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawImageImageintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "wiiii";
        }

        @Override
        public int number() {
            return 154;
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

        private static final ThreadLocal<VGCDrawImageImageintintintintintintintint> REUSED = ThreadLocal.withInitial(VGCDrawImageImageintintintintintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawImageImageintintintintintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "wiiiiiiii";
        }

        @Override
        public int number() {
            return 155;
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

        private static final ThreadLocal<VGCDrawLineintintintint> REUSED = ThreadLocal.withInitial(VGCDrawLineintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawLineintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 156;
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

        private static final ThreadLocal<VGCDrawOvalintintintint> REUSED = ThreadLocal.withInitial(VGCDrawOvalintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawOvalintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 157;
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCDrawPointintint implements dev.equo.swt.GcWire.Op {

        public int x;

        public int y;

        private static final ThreadLocal<VGCDrawPointintint> REUSED = ThreadLocal.withInitial(VGCDrawPointintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawPointintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "ii";
        }

        @Override
        public int number() {
            return 159;
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
        }
    }

    public static class VGCDrawPolygonint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        private static final ThreadLocal<VGCDrawPolygonint> REUSED = ThreadLocal.withInitial(VGCDrawPolygonint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawPolygonint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public int number() {
            return 160;
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.ints(pointArray);
        }
    }

    public static class VGCDrawPolylineint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        private static final ThreadLocal<VGCDrawPolylineint> REUSED = ThreadLocal.withInitial(VGCDrawPolylineint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawPolylineint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public int number() {
            return 161;
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

        private static final ThreadLocal<VGCDrawRectangleintintintint> REUSED = ThreadLocal.withInitial(VGCDrawRectangleintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawRectangleintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 162;
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

        private static final ThreadLocal<VGCDrawRoundRectangleintintintintintint> REUSED = ThreadLocal.withInitial(VGCDrawRoundRectangleintintintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawRoundRectangleintintintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public int number() {
            return 163;
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

        private static final ThreadLocal<VGCDrawTextStringintintint> REUSED = ThreadLocal.withInitial(VGCDrawTextStringintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCDrawTextStringintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "siii";
        }

        @Override
        public int number() {
            return 164;
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

        private static final ThreadLocal<VGCFillArcintintintintintint> REUSED = ThreadLocal.withInitial(VGCFillArcintintintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillArcintintintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public int number() {
            return 183;
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

        private static final ThreadLocal<VGCFillGradientRectangleintintintintboolean> REUSED = ThreadLocal.withInitial(VGCFillGradientRectangleintintintintboolean::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillGradientRectangleintintintintboolean reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiib";
        }

        @Override
        public int number() {
            return 184;
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

        private static final ThreadLocal<VGCFillOvalintintintint> REUSED = ThreadLocal.withInitial(VGCFillOvalintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillOvalintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 185;
        }

        @Override
        public void writeBinary(dev.equo.swt.GcWire w) {
            w.i(x);
            w.i(y);
            w.i(width);
            w.i(height);
        }
    }

    public static class VGCFillPolygonint implements dev.equo.swt.GcWire.Op {

        public int[] pointArray;

        private static final ThreadLocal<VGCFillPolygonint> REUSED = ThreadLocal.withInitial(VGCFillPolygonint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillPolygonint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "I";
        }

        @Override
        public int number() {
            return 187;
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

        private static final ThreadLocal<VGCFillRectangleintintintint> REUSED = ThreadLocal.withInitial(VGCFillRectangleintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillRectangleintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiii";
        }

        @Override
        public int number() {
            return 188;
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

        private static final ThreadLocal<VGCFillRoundRectangleintintintintintint> REUSED = ThreadLocal.withInitial(VGCFillRoundRectangleintintintintintint::new);

        public static /**
         * This thread's op to fill and send.
         */
        VGCFillRoundRectangleintintintintintint reused() {
            return REUSED.get();
        }

        @Override
        public String signature() {
            return "iiiiii";
        }

        @Override
        public int number() {
            return 189;
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

    public float getBufferScaleY() {
        return ((DartGC) impl).wireBufferScaleY();
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

    public static final int XORMODE = 268;

    public static final int ALPHA = 269;

    public static final int ANTIALIAS = 270;

    public static final int BACKGROUND = 271;

    public static final int BUFFER_SCALE = 273;

    public static final int BUFFER_SCALE_Y = 274;

    public static final int CLIPPING = 275;

    public static final int CLIPPING_PATH = 276;

    public static final int CLIPPING_RECTS = 277;

    public static final int FILL_RULE = 279;

    public static final int FONT = 280;

    public static final int FOREGROUND = 281;

    public static final int HOVER_FILLS = 283;

    public static final int INTERPOLATION = 284;

    public static final int LINE_CAP = 285;

    public static final int LINE_DASH = 286;

    public static final int LINE_DASH_OFFSET = 287;

    public static final int LINE_JOIN = 288;

    public static final int LINE_STYLE = 289;

    public static final int LINE_WIDTH = 290;

    public static final int SELECTION_FILLS = 291;

    public static final int STYLE = 292;

    public static final int THEMED_CANVAS = 293;

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
            case LINE_DASH_OFFSET:
                return "lineDashOffset";
            case BUFFER_SCALE:
                return "bufferScale";
            case BUFFER_SCALE_Y:
                return "bufferScaleY";
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
            case 268:
                w.write(getXORMode());
                return true;
            case 269:
                w.write(getAlpha());
                return true;
            case 270:
                w.write((int) getAntialias());
                return true;
            case 271:
                w.write(getBackground());
                return true;
            case 275:
                w.write(getClipping());
                return true;
            case 276:
                w.object(getClippingPath());
                return true;
            case 277:
                w.write(getClippingRects());
                return true;
            case 279:
                w.write(getFillRule());
                return true;
            case 280:
                w.object((getFont() == null || getFont().isDisposed() ? null : getFont().getImpl() instanceof DartFont ? ((DartFont) getFont().getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(getFont()).getImpl()).getValue()));
                return true;
            case 281:
                w.write(getForeground());
                return true;
            case 284:
                w.write((int) getInterpolation());
                return true;
            case 285:
                w.write(getLineCap());
                return true;
            case 286:
                w.write(getLineDash());
                return true;
            case 288:
                w.write(getLineJoin());
                return true;
            case 289:
                w.write(getLineStyle());
                return true;
            case 290:
                w.write((int) getLineWidth());
                return true;
            case 292:
                w.write((int) getStyle());
                return true;
            case 287:
                w.write(getLineDashOffset());
                return true;
            case 273:
                w.write(getBufferScale());
                return true;
            case 274:
                w.write(getBufferScaleY());
                return true;
            case 293:
                w.write(getThemedCanvas());
                return true;
            case 283:
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
            case 291:
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
            case 268:
                {
                    boolean v_ = getXORMode();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(268);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 269:
                {
                    Integer v_ = getAlpha();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(269);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 270:
                {
                    int v_ = getAntialias();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(270);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 271:
                {
                    Color v_ = getBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(271);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 275:
                {
                    Rectangle v_ = getClipping();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(275);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 276:
                {
                    PathData v_ = getClippingPath();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(276);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 277:
                {
                    int[] v_ = getClippingRects();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(277);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 279:
                {
                    Integer v_ = getFillRule();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(279);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 280:
                {
                    Font v_ = getFont();
                    dev.equo.swt.WireObject o_ = (v_ == null || v_.isDisposed() ? null : v_.getImpl() instanceof DartFont ? ((DartFont) v_.getImpl()).getValue() : ((DartFont) org.eclipse.swt.graphics.GraphicsUtils.copyFont(v_).getImpl()).getValue());
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(280);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 281:
                {
                    Color v_ = getForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(281);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 284:
                {
                    int v_ = getInterpolation();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(284);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 285:
                {
                    Integer v_ = getLineCap();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(285);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 286:
                {
                    int[] v_ = getLineDash();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(286);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 288:
                {
                    Integer v_ = getLineJoin();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(288);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 289:
                {
                    Integer v_ = getLineStyle();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(289);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 290:
                {
                    int v_ = getLineWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(290);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 292:
                {
                    int v_ = getStyle();
                    w.key(292);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 287:
                {
                    float v_ = getLineDashOffset();
                    w.key(287);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 273:
                {
                    float v_ = getBufferScale();
                    w.key(273);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 274:
                {
                    float v_ = getBufferScaleY();
                    w.key(274);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 293:
                {
                    Boolean v_ = getThemedCanvas();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(293);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 283:
                {
                    RGB[] v_ = getHoverFills();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(283);
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
            case 291:
                {
                    RGB[] v_ = getSelectionFills();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(291);
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

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 268, 269, 270, 271, 275, 276, 277, 279, 280, 281, 284, 285, 286, 288, 289, 290, 292, 287, 273, 274, 293, 283, 291 });

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
            case 292:
                return true;
        }
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}

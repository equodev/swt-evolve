package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VGC extends VResource {

    protected VGC() {
    }

    protected VGC(DartGC impl) {
        super(impl);
    }

    @JsonAttribute(name = "XORMode")
    public boolean getXORMode() {
        return ((DartGC) impl).getXORMode();
    }

    public void setXORMode(boolean value) {
        ((DartGC) impl).XORMode = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getAdvanced() {
        return ((DartGC) impl).getAdvanced();
    }

    public void setAdvanced(boolean value) {
        ((DartGC) impl).advanced = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getAlpha() {
        return ((DartGC) impl).getAlpha();
    }

    public void setAlpha(int value) {
        ((DartGC) impl).alpha = value;
    }

    @JsonAttribute(ignore = true)
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

    public int getFillRule() {
        return ((DartGC) impl).getFillRule();
    }

    public void setFillRule(int value) {
        ((DartGC) impl).fillRule = value;
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

    @JsonAttribute(ignore = true)
    public Pattern getForegroundPattern() {
        Pattern val = ((DartGC) impl).foregroundPattern;
        if (val != null && !(val.getImpl() instanceof DartPattern))
            return null;
        return val;
    }

    public void setForegroundPattern(Pattern value) {
        ((DartGC) impl).foregroundPattern = value;
    }

    @JsonAttribute(ignore = true)
    public int getInterpolation() {
        return ((DartGC) impl).getInterpolation();
    }

    public void setInterpolation(int value) {
        ((DartGC) impl).interpolation = value;
    }

    @JsonAttribute(ignore = true)
    public LineAttributes getLineAttributes() {
        return ((DartGC) impl).lineAttributes;
    }

    public void setLineAttributes(LineAttributes value) {
        ((DartGC) impl).lineAttributes = value;
    }

    public int getLineCap() {
        return ((DartGC) impl).getLineCap();
    }

    public void setLineCap(int value) {
        ((DartGC) impl).lineCap = value;
    }

    public int[] getLineDash() {
        return ((DartGC) impl).lineDash;
    }

    public void setLineDash(int[] value) {
        ((DartGC) impl).lineDash = value;
    }

    public int getLineJoin() {
        return ((DartGC) impl).getLineJoin();
    }

    public void setLineJoin(int value) {
        ((DartGC) impl).lineJoin = value;
    }

    public int getLineStyle() {
        return ((DartGC) impl).getLineStyle();
    }

    public void setLineStyle(int value) {
        ((DartGC) impl).lineStyle = value;
    }

    public int getLineWidth() {
        return ((DartGC) impl).getLineWidth();
    }

    public void setLineWidth(int value) {
        ((DartGC) impl).lineWidth = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getStyle() {
        return ((DartGC) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartGC) impl).style = value;
    }

    @JsonAttribute(ignore = true)
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

    @CompiledJson()
    public static class VGCCopyAreaImageintint {

        public Image image;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCCopyAreaintintintintintint {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;
    }

    @CompiledJson()
    public static class VGCCopyAreaintintintintintintboolean {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;

        public boolean paint;
    }

    @CompiledJson()
    public static class VGCDrawArcintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCDrawFocusintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawImageImageintint {

        public Image image;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawImageImageintintintint {

        public Image image;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;
    }

    @CompiledJson()
    public static class VGCDrawImageImageintintintintintintintint {

        public Image image;

        public int srcX;

        public int srcY;

        public int srcWidth;

        public int srcHeight;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;
    }

    @CompiledJson()
    public static class VGCDrawLineintintintint {

        public int x1;

        public int y1;

        public int x2;

        public int y2;
    }

    @CompiledJson()
    public static class VGCDrawOvalintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawPathPath {

        public Path path;
    }

    @CompiledJson()
    public static class VGCDrawPointintint {

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawPolygonint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawPolylineint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawRectangleRectangle {

        public Rectangle rect;
    }

    @CompiledJson()
    public static class VGCDrawRectangleintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawRoundRectangleintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;
    }

    @CompiledJson()
    public static class VGCDrawStringStringintint {

        public String string;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawStringStringintintboolean {

        public String string;

        public int x;

        public int y;

        public boolean isTransparent;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintint {

        public String string;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintintboolean {

        public String string;

        public int x;

        public int y;

        public boolean isTransparent;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintintint {

        public String string;

        public int x;

        public int y;

        public int flags;
    }

    @CompiledJson()
    public static class VGCFillArcintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCFillGradientRectangleintintintintboolean {

        public int x;

        public int y;

        public int width;

        public int height;

        public boolean vertical;
    }

    @CompiledJson()
    public static class VGCFillOvalintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillPathPath {

        public Path path;
    }

    @CompiledJson()
    public static class VGCFillPolygonint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCFillRectangleRectangle {

        public Rectangle rect;
    }

    @CompiledJson()
    public static class VGCFillRectangleintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillRoundRectangleintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;
    }

    public static final String XORMODE = "XORMode";

    public static final String ALPHA = "alpha";

    public static final String BACKGROUND = "background";

    public static final String BACKGROUND_PATTERN = "backgroundPattern";

    public static final String CLIPPING = "clipping";

    public static final String CLIPPING_PATH = "clippingPath";

    public static final String CLIPPING_RECTS = "clippingRects";

    public static final String FILL_RULE = "fillRule";

    public static final String FONT = "font";

    public static final String FOREGROUND = "foreground";

    public static final String LINE_CAP = "lineCap";

    public static final String LINE_DASH = "lineDash";

    public static final String LINE_JOIN = "lineJoin";

    public static final String LINE_STYLE = "lineStyle";

    public static final String LINE_WIDTH = "lineWidth";

    public static final String STYLE = "style";

    public static final String TRANSFORM = "transform";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "XORMode":
                Serializer.writeKeyValue(writer, "XORMode", getXORMode());
                return;
            case "alpha":
                Serializer.writeKeyValue(writer, "alpha", getAlpha());
                return;
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "backgroundPattern":
                Serializer.writeKeyValue(writer, "backgroundPattern", getBackgroundPattern());
                return;
            case "clipping":
                Serializer.writeKeyValue(writer, "clipping", getClipping());
                return;
            case "clippingPath":
                Serializer.writeKeyValue(writer, "clippingPath", getClippingPath());
                return;
            case "clippingRects":
                Serializer.writeKeyValue(writer, "clippingRects", getClippingRects());
                return;
            case "fillRule":
                Serializer.writeKeyValue(writer, "fillRule", getFillRule());
                return;
            case "font":
                Serializer.writeKeyValue(writer, "font", getFont());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "lineCap":
                Serializer.writeKeyValue(writer, "lineCap", getLineCap());
                return;
            case "lineDash":
                Serializer.writeKeyValue(writer, "lineDash", getLineDash());
                return;
            case "lineJoin":
                Serializer.writeKeyValue(writer, "lineJoin", getLineJoin());
                return;
            case "lineStyle":
                Serializer.writeKeyValue(writer, "lineStyle", getLineStyle());
                return;
            case "lineWidth":
                Serializer.writeKeyValue(writer, "lineWidth", getLineWidth());
                return;
            case "style":
                Serializer.writeKeyValue(writer, "style", getStyle());
                return;
            case "transform":
                Serializer.writeKeyValue(writer, "transform", getTransform());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = GC.class)
    public static class GCJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartGC.class, (JsonWriter.WriteObject<DartGC>) (writer, impl) -> {
                Serializer.writeResourceWithId(json, writer, impl);
            });
            json.registerReader(DartGC.class, (JsonReader.ReadObject<DartGC>) reader -> {
                return null;
            });
        }

        public static GC read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, GC api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
